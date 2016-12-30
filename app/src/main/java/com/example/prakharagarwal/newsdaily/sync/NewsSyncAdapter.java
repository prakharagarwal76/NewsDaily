package com.example.prakharagarwal.newsdaily.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.prakharagarwal.newsdaily.R;
import com.example.prakharagarwal.newsdaily.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by prakharagarwal on 29/12/16.
 */
public class NewsSyncAdapter  extends AbstractThreadedSyncAdapter {


    public final String LOG_TAG = NewsSyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;

    public static final int SYNC_INTERVAL = 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public NewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mContentResolver = context.getContentResolver();
    }

    public NewsSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        mContentResolver = context.getContentResolver();
    }
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        Cursor cursor=getContext().getContentResolver().query(NewsContract.SelectedSourceEntry.CONTENT_URI,null,null,null,null);
        while(cursor.moveToNext()){
            new SyncTask_GET().execute(cursor.getString(0),cursor.getString(1));
        }

        new SourceSyncTask().execute();

    }


    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }


    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);


        Account newAccount = new Account(
                context.getString(R.string.app_name), "news.com.example");


        if ( null == accountManager.getPassword(newAccount) ) {


            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        NewsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(newAccount,context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public class SyncTask_GET extends AsyncTask<String, Void, String> {


        private final String LOG_TAG = SyncTask_GET.class.getName();
        private  String CATEGORY = null;

        public SyncTask_GET(){
        }

        @Override
        protected String doInBackground(String... s) {

            CATEGORY=s[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

                final String NEWS_BASE_URL =
                        "https://newsapi.org/v1/articles?";
                final String SOURCE_PARAM = "source";
                final String CATEGORY_PARAM = "category";
                final String APPID_PARAM = "apiKey";

                Uri builtUri = Uri.parse(NEWS_BASE_URL).buildUpon()
                        .appendQueryParameter(SOURCE_PARAM, s[1])
                        //.appendQueryParameter(CATEGORY_PARAM, format)
                        .appendQueryParameter(APPID_PARAM, "c4fcb7a68fc74e49ada5bdf985de3c16")
                        .build();

                URL url = new URL(builtUri.toString());

                Log.d("URL",""+url);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {

                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                    Log.e("Error:", "LOCATION_STATUS_SERVER_DOWN");
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.d("Data 1",forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;

        }
        protected void onPostExecute(String result) {
            // Weather information.  Each day's forecast info is an element of the "list" array.
            final String OWM_ARTICLES = "articles";
            final String OWM_SOURCE = "source";
            final String OWM_TITLE = "title";
            final String OWM_DESCRIPTION = "description";
            final String OWM_URL = "url";
            final String OWM_URL_TO_IMAGE = "urlToImage";

            try {
                JSONObject forecastJson = new JSONObject(result);
                Context context = getContext();


                String mSource=forecastJson.getString(OWM_SOURCE);
                JSONArray newsArray = forecastJson.getJSONArray(OWM_ARTICLES);


                Vector<ContentValues> cVVector = new Vector<ContentValues>(newsArray.length());

                for(int i = 0; i < newsArray.length(); i++) {
                    // These are the values that will be collected.
                    String title;
                    String description;
                    String url;
                    String urlToImage;

                    // Get the JSON object representing the day
                    JSONObject article = newsArray.getJSONObject(i);

                    // Cheating to convert this to UTC time, which is what we want anyhow


                    title = article.getString(OWM_TITLE);
                    description = article.getString(OWM_DESCRIPTION);
                    url = article.getString(OWM_URL);
                    urlToImage = article.getString(OWM_URL_TO_IMAGE);



                    ContentValues NewsValues = new ContentValues();

                    NewsValues.put(NewsContract.ArticleEntry.COLUMN_TITLE, title);
                    NewsValues.put(NewsContract.ArticleEntry.COLUMN_DESCRIPTION, description);
                    NewsValues.put(NewsContract.ArticleEntry.COLUMN_URL,url);
                    NewsValues.put(NewsContract.ArticleEntry.COLUMN_URL_TO_IMAGE, urlToImage);
                    NewsValues.put(NewsContract.ArticleEntry.COLUMN_CATEGORY,CATEGORY);


                    cVVector.add(NewsValues);
                }


                if ( cVVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    getContext().getContentResolver().delete(NewsContract.ArticleEntry.CONTENT_URI,"category='"+CATEGORY+"'",null);
                    getContext().getContentResolver().bulkInsert(NewsContract.ArticleEntry.CONTENT_URI, cvArray);

                }
                Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");


            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }

        }


    }
    public class SourceSyncTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = SourceSyncTask.class.getName();

        @Override
        protected  String doInBackground(Void... status) {


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;


            try {

                URL url = new URL("https://newsapi.org/v1/sources?language=en");

                Log.d("URL",""+url);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    // return "null";
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    //setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
                    Log.e("Error:", "LOCATION_STATUS_SERVER_DOWN");
                    //return "LOCATION_STATUS_SERVER_DOWN";
                }
                forecastJsonStr = buffer.toString();
                Log.d("Data",forecastJsonStr);
                return forecastJsonStr;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                //setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                // setLocationStatus(getContext(), LOCATION_STATUS_SERVER_INVALID);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
        protected void onPostExecute(String result) {


            final String OWM_RESULTS = "sources";
            final String OWM_SOURCE_ID = "id";
            final String OWM_SOURCE_NAME = "name";

            final String OWM_CATEGORY = "category";

            try {
                JSONObject forecastJson = new JSONObject(result);
                JSONArray newsArray = forecastJson.getJSONArray(OWM_RESULTS);


                Vector<ContentValues> cVVector = new Vector<ContentValues>(newsArray.length());

                for(int i = 0; i < newsArray.length(); i++) {
                    // These are the values that will be collected.
                    String name;
                    String id;
                    String category;


                    // Get the JSON object representing the day
                    JSONObject article = newsArray.getJSONObject(i);

                    // Cheating to convert this to UTC time, which is what we want anyhow


                    name = article.getString(OWM_SOURCE_NAME);
                    id = article.getString(OWM_SOURCE_ID);
                    category = article.getString(OWM_CATEGORY);




                    ContentValues NewsValues = new ContentValues();

                    NewsValues.put(NewsContract.SourceEntry.COLUMN_SOURCE_NAME, name);
                    NewsValues.put(NewsContract.SourceEntry.COLUMN_SOURCE_ID, id);
                    NewsValues.put(NewsContract.SourceEntry.COLUMN_SOURCE_CATEGORY,category);



                    cVVector.add(NewsValues);
                }


                if ( cVVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    getContext().getContentResolver().delete(NewsContract.SourceEntry.CONTENT_URI,null,null);
                    getContext().getContentResolver().bulkInsert(NewsContract.SourceEntry.CONTENT_URI, cvArray);

                }
                Log.d(LOG_TAG, "Source Sync Complete. " + cVVector.size() + " Inserted");


            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }

        }



    }


}
