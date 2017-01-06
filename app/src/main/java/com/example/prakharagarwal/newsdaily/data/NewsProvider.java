package com.example.prakharagarwal.newsdaily.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by prakharagarwal on 29/12/16.
 */
public class NewsProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private NewsDBHelper mOpenHelper;

    static final int ARTICLE = 100;
    static final int SOURCE = 200;
    static final int SELECTED_SOURCE = 300;
    static final int WEATHER = 400;



    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, NewsContract.PATH_ARTICLE, ARTICLE);
        matcher.addURI(authority, NewsContract.PATH_SOURCE, SOURCE);
        matcher.addURI(authority, NewsContract.PATH_SELECTED_SOURCE, SELECTED_SOURCE);
        matcher.addURI(authority,NewsContract.PATH_WEATHER, WEATHER);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDBHelper(getContext());
        return true;

    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"

            case ARTICLE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.ArticleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case SOURCE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.SourceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SELECTED_SOURCE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.SelectedSourceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case ARTICLE:
                return NewsContract.ArticleEntry.CONTENT_TYPE;
            case SOURCE:
                return NewsContract.SourceEntry.CONTENT_ITEM_TYPE;
            case SELECTED_SOURCE:
                return NewsContract.SelectedSourceEntry.CONTENT_ITEM_TYPE;
            case WEATHER:
                return NewsContract.WeatherEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case SELECTED_SOURCE: {
                long _id = db.insert(NewsContract.SelectedSourceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri =NewsContract.SelectedSourceEntry.buildSourceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case WEATHER: {
                long _id = db.insert(NewsContract.WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = NewsContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case SOURCE:
                rowsDeleted = db.delete(
                        NewsContract.SourceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ARTICLE:
                rowsDeleted = db.delete(
                        NewsContract.ArticleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SELECTED_SOURCE:
                rowsDeleted = db.delete(
                        NewsContract.SelectedSourceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER:
                rowsDeleted = db.delete(NewsContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ARTICLE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(NewsContract.ArticleEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case SOURCE:
                db.beginTransaction();
                int returnCount1 = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(NewsContract.SourceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount1++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount1;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }
}
