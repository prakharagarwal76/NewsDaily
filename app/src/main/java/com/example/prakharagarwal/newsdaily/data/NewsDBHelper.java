package com.example.prakharagarwal.newsdaily.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.prakharagarwal.newsdaily.data.NewsContract.SelectedSourceEntry;
import com.example.prakharagarwal.newsdaily.data.NewsContract.SourceEntry;
import com.example.prakharagarwal.newsdaily.data.NewsContract.ArticleEntry;



/**
 * Created by prakharagarwal on 29/12/16.
 */
public class NewsDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "news.db";

    public NewsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ARTICLE_TABLE = "CREATE TABLE " + ArticleEntry.TABLE_NAME + " (" +
                ArticleEntry.COLUMN_TITLE + "  TEXT NOT NULL," +
                ArticleEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                ArticleEntry.COLUMN_URL + " TEXT NOT NULL, " +
                ArticleEntry.COLUMN_URL_TO_IMAGE + " TEXT NOT NULL, " +
                ArticleEntry.COLUMN_CATEGORY + " TEXT NOT NULL " +

                " );";

        final String SQL_CREATE_SOURCE_TABLE = "CREATE TABLE " + SourceEntry.TABLE_NAME + " (" +

                SourceEntry.COLUMN_SOURCE_NAME + " TEXT NOT NULL," +
                SourceEntry.COLUMN_SOURCE_ID + " TEXT NOT NULL," +
                SourceEntry.COLUMN_SOURCE_CATEGORY + " TEXT NOT NULL" +
                ");";
        final String SQL_CREATE_SELECTED_SOURCE_TABLE = "CREATE TABLE " +SelectedSourceEntry.TABLE_NAME + " (" +

                SelectedSourceEntry.COLUMN_CATEGORY + " TEXT NOT NULL," +
                SelectedSourceEntry.COLUMN_SOURCE_ID + " TEXT NOT NULL" +

                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_ARTICLE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SOURCE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SELECTED_SOURCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArticleEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SourceEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SelectedSourceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

