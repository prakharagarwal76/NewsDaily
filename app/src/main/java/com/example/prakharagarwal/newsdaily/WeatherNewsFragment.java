package com.example.prakharagarwal.newsdaily;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ScrollingView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prakharagarwal.newsdaily.data.NewsContract;
import com.example.prakharagarwal.newsdaily.data.NewsContract.WeatherEntry;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by prakharagarwal on 03/01/17.
 */
public class WeatherNewsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = WeatherNewsFragment.class.getName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView text_date;
    private Button refresh;
    ImageView icon;
    TextView text_maxTemp;
    TextView text_minTemp;
    TextView text_desc;
    TextView emptyView;
    TextView text_location;
    ScrollView scrollView;
    private static final int MY_PERMISSION_REQUEST_READ_FINE_LOCATION = 501;
    private double mLatitude;
    private double mLongitude;
    String pincode;
    String location;
    Cursor weatherCursor;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        getLoaderManager().initLoader(5, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_reload:
                reloadWeather();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_weathernews, container, false);
        text_date = (TextView) rootView.findViewById(R.id.text_weather_date);
        text_maxTemp = (TextView) rootView.findViewById(R.id.text_weather_high);
        text_minTemp = (TextView) rootView.findViewById(R.id.text_weather_low);
        icon = (ImageView) rootView.findViewById(R.id.image_weather_icon);
        text_desc = (TextView) rootView.findViewById(R.id.text_weather_desc);
        emptyView = (TextView) rootView.findViewById(R.id.text_weather_empty);
        text_location = (TextView) rootView.findViewById(R.id.text_weather_location);
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollview1);

        AdView mAdView = (AdView) rootView.findViewById(R.id.adView_weather);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);


        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(), // Activity
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_READ_FINE_LOCATION);
        }


        return rootView;
    }

    public void reloadWeather() {
        try {
            mGoogleApiClient.reconnect();
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            if (!addresses.isEmpty()) {
                Address add = addresses.get(0);
                Log.d("Address", add.toString());
                pincode = add.getPostalCode();
                int index = add.getMaxAddressLineIndex();
                String addres = "";
                for (int i = 0; i <= index; i++) {
                    addres += add.getAddressLine(i) + " ";
                }

                location = addres + "";

                if (pincode != null) {

                    new FetchWeatherTask().execute(pincode);

                } else {
                    Toast.makeText(getActivity(), getContext().getString(R.string.pincode_not_found), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), getContext().getString(R.string.location_not_available), Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
        }
    }

    public void updateView() {
        if (weatherCursor.moveToFirst()) {
            scrollView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.INVISIBLE);
            weatherCursor.moveToFirst();
            if (text_date != null && text_minTemp != null && text_maxTemp != null && icon != null
                    && text_desc != null && text_location != null) {
                text_date.setText(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATE)));
                long date2 = weatherCursor.getLong(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
                String date1 = getReadableDateString(date2);
                text_maxTemp.setText(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)));
                text_minTemp.setText(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)));
                String iconString = "i" + weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_ICON));

                int iconID = getContext().getResources().getIdentifier(iconString, "drawable", getContext().getPackageName());

                icon.setImageDrawable(getContext().getResources().getDrawable(iconID));
                text_desc.setText(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)));
                text_location.setText(weatherCursor.getString(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOCATION)));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();


                }
                return;
            }


        }
    }

    private String getReadableDateString(long time) {

        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        try {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } catch (SecurityException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "google api client connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(LOG_TAG, "location" + location.toString());
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "google api client connection failed");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] PROJECTION = new String[]{
                WeatherEntry.COLUMN_LOCATION,
                WeatherEntry.COLUMN_ICON,
                WeatherEntry.COLUMN_DATE,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_SHORT_DESC
        };
        return new CursorLoader(getContext(),
                NewsContract.WeatherEntry.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            weatherCursor = data;

            updateView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class FetchWeatherTask extends AsyncTask<String, Void, WeatherData> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time) {

            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }


        private WeatherData getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            final String OWM_CITY = "city";
            final String OWM_CITY_NAME = "name";
            final String OWM_LIST = "list";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_WEATHER = "weather";
            final String OWM_ICON = "icon";
            final String OWM_DESCRIPTION = "main";

            try {
                JSONObject forecastJson = new JSONObject(forecastJsonStr);

                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
                JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
                String cityName = cityJson.getString(OWM_CITY_NAME);
                Time dayTime = new Time();
                dayTime.setToNow();
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
                dayTime = new Time();
                for (int i = 0; i < weatherArray.length(); i++) {
                    long dateTime;
                    double high;
                    double low;
                    String icon;
                    String description;

                    JSONObject dayForecast = weatherArray.getJSONObject(i);
                    dateTime = dayTime.setJulianDay(julianStartDay + i);


                    JSONObject weatherObject =
                            dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    icon = weatherObject.getString(OWM_ICON);

                    description = weatherObject.getString(OWM_DESCRIPTION);
                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    high = temperatureObject.getDouble(OWM_MAX);
                    low = temperatureObject.getDouble(OWM_MIN);
                    WeatherData weatherValues = new WeatherData(dateTime,
                            icon, description, high, low);

                    return weatherValues;
                }
                Log.d(LOG_TAG, "FetchWeatherTask Complete.  Inserted");

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected WeatherData doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 1;

            try {

                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, getContext().getString(R.string.openweather_apikey))
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
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

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(WeatherData result) {
            if (result != null) {

                ContentValues weatherValues = new ContentValues();

                String date1 = getReadableDateString(result.date);

                weatherValues.put(NewsContract.WeatherEntry.COLUMN_DATE, date1);
                weatherValues.put(NewsContract.WeatherEntry.COLUMN_ICON, result.icon);
                weatherValues.put(NewsContract.WeatherEntry.COLUMN_SHORT_DESC, result.description);
                weatherValues.put(NewsContract.WeatherEntry.COLUMN_MAX_TEMP, result.high);
                weatherValues.put(NewsContract.WeatherEntry.COLUMN_MIN_TEMP, result.low);
                weatherValues.put(NewsContract.WeatherEntry.COLUMN_LOCATION, location);
                weatherValues.put(WeatherEntry.COLUMN_POSTAL_CODE, pincode);

                getActivity().getContentResolver().delete(WeatherEntry.CONTENT_URI, null, null);
                Uri insertedUri = getActivity().getContentResolver().insert(
                        NewsContract.WeatherEntry.CONTENT_URI,
                        weatherValues);


            }
        }
    }


}
