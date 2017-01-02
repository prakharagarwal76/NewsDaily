package com.example.prakharagarwal.newsdaily;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.prakharagarwal.newsdaily.data.NewsContract;
import com.example.prakharagarwal.newsdaily.sync.NewsSyncAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prakharagarwal on 29/12/16.
 */
public class BusinessNewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public  Cursor articleCursor;
    ArticleAdapter adapter;
    RecyclerView mRecyclerView;
    public BusinessNewsFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(2, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article, container, false);

        // articleCursor=((MainActivity)getActivity()).getArticleCursor("general");
        String[] PROJECTION = new String[] {
                "rowid _id",
                NewsContract.ArticleEntry.COLUMN_TITLE,
                NewsContract.ArticleEntry.COLUMN_DESCRIPTION,
                NewsContract.ArticleEntry.COLUMN_URL,
                NewsContract.ArticleEntry.COLUMN_URL_TO_IMAGE,
        };
        //articleCursor=getActivity().getContentResolver().query(NewsContract.ArticleEntry.CONTENT_URI,PROJECTION,"category='general'",null,null);
        // adapter= new ArticleAdapter(getContext(),articleCursor);
        AdView mAdView = (AdView)rootView.findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        mRecyclerView=(RecyclerView) rootView.findViewById(R.id.article_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setAdapter(adapter);

        Spinner spinner=(Spinner)rootView.findViewById(R.id.spinner);

        final Cursor cursor2= getActivity().getContentResolver().query(NewsContract.SourceEntry.CONTENT_URI,null,null,null,null);
        Cursor cur=getActivity().getContentResolver().query(NewsContract.SelectedSourceEntry.CONTENT_URI,null,null,null,null);
        String selectedSource=null;
        int pos=0;
        if(cur!=null)
        { cur.moveToFirst();
        while(cur.moveToNext()){
            if(cur.getString(0).equals("business"))
                selectedSource=cur.getString(1);
        }}
//            Log.e("selectedSource",selectedSource);
        List<String> categories = new ArrayList<String>();
        final List<String> categoriesID = new ArrayList<String>();
        int flag=0;
        if(cursor2!=null)
        {cursor2.moveToFirst();
        while(cursor2.moveToNext()){

            if(cursor2.getString(2).equals("business"))
            {
                categories.add(cursor2.getString(0));

                categoriesID.add(cursor2.getString(1));
                if(selectedSource!=null)
                {
                    if(selectedSource.equals(cursor2.getString(1))){
                        pos=flag;
                        Log.e("pos",""+pos);
                    }
                }
                flag++;
            }
        }}


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(pos);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ContentValues cv= new ContentValues();
                String ID=categoriesID.get(position);

                cv.put(NewsContract.SelectedSourceEntry.COLUMN_CATEGORY, "business");
                cv.put(NewsContract.SelectedSourceEntry.COLUMN_SOURCE_ID, ID);
                getActivity().getContentResolver().delete(NewsContract.SelectedSourceEntry.CONTENT_URI,"category='business'",null);
                getActivity().getContentResolver().insert(NewsContract.SelectedSourceEntry.CONTENT_URI,cv);
                NewsSyncAdapter.syncImmediately(getContext());
                getLoaderManager().restartLoader(2,null,BusinessNewsFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] PROJECTION = new String[] {
                "rowid _id",
                NewsContract.ArticleEntry.COLUMN_TITLE,
                NewsContract.ArticleEntry.COLUMN_DESCRIPTION,
                NewsContract.ArticleEntry.COLUMN_URL,
                NewsContract.ArticleEntry.COLUMN_URL_TO_IMAGE,
        };
        Log.e("inside on create loader","yoyo");
        return new CursorLoader(getContext(),
                NewsContract.ArticleEntry.CONTENT_URI,
                PROJECTION,
                "category='business'",
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        articleCursor=data;
        //articleCursor.notify();
        adapter=new ArticleAdapter(getContext(),articleCursor);
        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);
        Log.e("inside on finisloader","yoyo");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.e("inside on restart","yoyo");
    }
}