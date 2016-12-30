package com.example.prakharagarwal.newsdaily.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by prakharagarwal on 29/12/16.
 */
public class NewsContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.prekshasingla.news.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_ARTICLE = "aricles";
    public static final String PATH_SOURCE = "source";
    public static final String PATH_SELECTED_SOURCE = "selected_source";



    /* Inner class that defines the table contents of the location table */
    public static final class ArticleEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTICLE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTICLE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTICLE;

        // Table name
        public static final String TABLE_NAME = "article";

        public static final String COLUMN_TITLE = "title";

        // In order to uniquely pinpoint the location on the map when we launch the
        // map intent, we store the latitude and longitude as returned by openweathermap.
        public static final String COLUMN_DESCRIPTION = "discription";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_URL_TO_IMAGE = "urlToImage";
        public static final String COLUMN_CATEGORY = "category";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class SourceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SOURCE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SOURCE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SOURCE;

        public static final String TABLE_NAME = "source";

        // Column with the foreign key into the location table.
        public static final String COLUMN_SOURCE_NAME = "name";
        public static final String COLUMN_SOURCE_ID = "id";
        public static final String COLUMN_SOURCE_CATEGORY = "category";


        public static Uri buildSourceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }



    }
    public static final class SelectedSourceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SELECTED_SOURCE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SELECTED_SOURCE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SELECTED_SOURCE;

        public static final String TABLE_NAME = "selected_source";

        // Column with the foreign key into the location table.
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_SOURCE_ID = "id";


        public static Uri buildSourceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }



    }
}

