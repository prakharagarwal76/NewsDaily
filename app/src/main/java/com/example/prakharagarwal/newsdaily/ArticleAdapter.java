package com.example.prakharagarwal.newsdaily;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by prakharagarwal on 29/12/16.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    public ArticleAdapter(Context context,Cursor cursor){
        mContext=context;
        mCursor=cursor;
    }
    public interface Callback{
        void onItemSelected(Cursor cursor,int position);
    }

    @Override
    public ArticleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carditem_article, parent, false);
            view.setFocusable(true);
            return new ArticleAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ArticleAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        Picasso.with(mContext).load(mCursor.getString(4)).into(holder.I1);
        holder.t2.setText(mCursor.getString(1));


    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    public class ArticleAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView I1;
        public final TextView t2;

        public ArticleAdapterViewHolder(View view) {
            super(view);
            I1=(ImageView)view.findViewById(R.id.articleThumbnail);
            t2=(TextView)view.findViewById(R.id.article_text);
            view.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            ((Callback) mContext).onItemSelected(mCursor,adapterPosition);


        }
    }

}
