package com.example.prakharagarwal.newsdaily.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.prakharagarwal.newsdaily.DetailActivity;
import com.example.prakharagarwal.newsdaily.MainActivity;
import com.example.prakharagarwal.newsdaily.R;
import com.example.prakharagarwal.newsdaily.data.NewsContract;
import com.squareup.picasso.Target;

import java.util.concurrent.ExecutionException;

/**
 * Created by prakharagarwal on 01/01/17.
 */
public class WidgetRemoteViewsService extends RemoteViewsService{
    public final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();
    String[] PROJECTION = new String[] {
            "rowid _id",
            NewsContract.ArticleEntry.COLUMN_TITLE,
            NewsContract.ArticleEntry.COLUMN_DESCRIPTION,
            NewsContract.ArticleEntry.COLUMN_URL,
            NewsContract.ArticleEntry.COLUMN_URL_TO_IMAGE,
    };
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(NewsContract.ArticleEntry.CONTENT_URI,
                        PROJECTION,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);



                views.setTextViewText(R.id.widget_text,data.getString(1));

                Intent intent = new Intent();

                intent.putExtra("urlToImage",data.getString(4));
                intent.putExtra("headline",data.getString(1));
                intent.putExtra("desc",data.getString(2));
                intent.putExtra("url",data.getString(3));


                views.setOnClickFillInIntent(R.id.widget_list_item,intent);


                return views;
            }



            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
