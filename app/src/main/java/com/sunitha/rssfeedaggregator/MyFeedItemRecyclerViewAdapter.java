package com.sunitha.rssfeedaggregator;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sunitha.rssfeedaggregator.FeedItemFragment.OnListFragmentInteractionListener;
import com.sunitha.rssfeedaggregator.provider.FeedProvider;


import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.sunitha.rssfeedaggregator.provider.FeedContract.Feed} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 */
public class MyFeedItemRecyclerViewAdapter extends RecyclerView.Adapter<MyFeedItemRecyclerViewAdapter.ViewHolder> {

    public static final String TAG = "MyFeedItemRecyclerViewAdapter";
    private final OnListFragmentInteractionListener mListener;
    private Cursor mCursor;

    public MyFeedItemRecyclerViewAdapter(OnListFragmentInteractionListener listener) {

        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_feeditem, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToPosition(position);

            holder.Feed_ID = mCursor.getString(FeedItemFragment.COLUMN_ID);
            holder.mIdView.setText(mCursor.getString(FeedItemFragment.COLUMN_TITLE));
            holder.mContentView.setText(mCursor.getString(FeedItemFragment.COLUMN_URL_STRING));

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListFragmentInteraction(holder.Feed_ID,
                            holder.mIdView.getText().toString(),
                            holder.mContentView.getText().toString(),true);

                }}
                );

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {

                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.Feed_ID,
                                holder.mIdView.getText().toString(),
                                holder.mContentView.getText().toString(),false);
                    }
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();

    }

    public Cursor getCursor() {
        return mCursor;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public String Feed_ID;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageButton mImageButton;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mImageButton = (ImageButton)view.findViewById(R.id.imageDeleteView);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
