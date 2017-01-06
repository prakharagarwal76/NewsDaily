package com.example.prakharagarwal.newsdaily;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    TextView headlineView;
    TextView descView;
    ImageView imageView;
    TextView link;
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_detail, container, false);

        headlineView=(TextView)rootView.findViewById(R.id.detail_headline);
        descView=(TextView)rootView.findViewById(R.id.detail_desc);
        imageView=(ImageView)rootView.findViewById(R.id.detail_image);
        link=(TextView)rootView.findViewById(R.id.detail_url);
        RelativeLayout rel=(RelativeLayout)rootView.findViewById(R.id.detail_rellayout);
        TextView tView=(TextView)rootView.findViewById(R.id.detail_emptyText);
        Bundle arguments=getArguments();
        tView.setVisibility(View.VISIBLE);
        rel.setVisibility(View.INVISIBLE);
        if(arguments!=null){
            tView.setVisibility(View.INVISIBLE);
            rel.setVisibility(View.VISIBLE);
            headlineView.setText(arguments.getString("headline"));
            descView.setText(arguments.getString("desc"));
            link.setText(arguments.getString("url"));
            Picasso.with(getContext()).load(arguments.getString("urlToImage")).into(imageView);
        }else if(getActivity().getIntent().getStringExtra("desc")!=null) {
            tView.setVisibility(View.INVISIBLE);
            rel.setVisibility(View.VISIBLE);
            Intent intent = getActivity().getIntent();
            Picasso.with(getContext()).load(intent.getStringExtra("urlToImage")).into(imageView);
            headlineView.setText(intent.getStringExtra("headline"));
            descView.setText(intent.getStringExtra("desc"));
            link.setText(intent.getStringExtra("url"));
        }

        return rootView;
    }
}
