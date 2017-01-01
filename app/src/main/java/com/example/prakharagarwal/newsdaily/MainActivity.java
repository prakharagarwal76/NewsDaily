package com.example.prakharagarwal.newsdaily;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.prakharagarwal.newsdaily.data.NewsContract;
import com.example.prakharagarwal.newsdaily.sync.NewsSyncAdapter;

public class MainActivity extends AppCompatActivity implements ArticleAdapter.Callback {
    ViewPager viewPager;
    TabLayout tabLayout;
    ViewPagerAdapter viewPagerAdapter;
    Boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NewsSyncAdapter.initializeSyncAdapter(this);
        Cursor c=getContentResolver().query(NewsContract.SourceEntry.CONTENT_URI,null,null,null,null);
        if(findViewById(R.id.detailContainer)!=null){
            mTwoPane=true;
            if(savedInstanceState!=null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailContainer, new DetailActivityFragment(), "DETAILFRAGMEN_TAG")
                        .commit();
            }
        }else {
            mTwoPane=false;
        }
        if(!c.moveToNext()) {
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Please wait while the news is being loaded...");
            progress.show();

            Runnable progressRunnable = new Runnable() {

                @Override
                public void run() {
                    progress.cancel();
                    viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                    viewPagerAdapter.addFragment(new GeneralNewsFragment(), "GENERAL");
                    viewPagerAdapter.addFragment(new BusinessNewsFragment(), "BUSINESS");
                    viewPagerAdapter.addFragment(new EntertainmentNewsFragment(), "ENTERTAINMENT");


                    viewPager = (ViewPager) findViewById(R.id.viewpager);
                    tabLayout = (TabLayout) findViewById(R.id.tablayout);
                    viewPager.setAdapter(viewPagerAdapter);
                    tabLayout.setupWithViewPager(viewPager);
                }
            };

            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 5000);
        }else{
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPagerAdapter.addFragment(new GeneralNewsFragment(), "GENERAL");
            viewPagerAdapter.addFragment(new BusinessNewsFragment(), "BUSINESS");
            viewPagerAdapter.addFragment(new EntertainmentNewsFragment(), "ENTERTAINMENT");


            viewPager = (ViewPager) findViewById(R.id.viewpager);
            tabLayout = (TabLayout) findViewById(R.id.tablayout);
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
        }



    }
    @Override
    public void onItemSelected(Cursor cursor,int position){
        if(mTwoPane){
            Bundle arguments=new Bundle();
            cursor.moveToPosition(position);
            arguments.putString("urlToImage",cursor.getString(4));
            arguments.putString("headline",cursor.getString(1));
            arguments.putString("desc",cursor.getString(2));
            arguments.putString("url",cursor.getString(3));
            DetailActivityFragment fragment=new DetailActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailContainer, fragment, "DetailActivityFragment.TAG")
                    .commit();
        }else{
            Intent intent= new Intent(this,new DetailActivity().getClass());
            intent.putExtra("urlToImage",cursor.getString(4));
            intent.putExtra("headline",cursor.getString(1));
            intent.putExtra("desc",cursor.getString(2));
            intent.putExtra("url",cursor.getString(3));
            startActivity(intent);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}