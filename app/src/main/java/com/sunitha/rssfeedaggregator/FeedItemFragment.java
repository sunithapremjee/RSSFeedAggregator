package com.sunitha.rssfeedaggregator;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunitha.rssfeedaggregator.provider.FeedContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FeedItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "FeedItemFragment";


       private static final String[] PROJECTION = new String[]{
                   FeedContract.Feed._ID,
                   FeedContract.Feed.COLUMN_NAME_FEED_ID,
                   FeedContract.Feed.COLUMN_NAME_TITLE,
                   FeedContract.Feed.COLUMN_NAME_LINK

           };

   // Column indexes. The index of a column in the Cursor is the same as its relative position in
   // the projection.
   /** Column index for _ID */
   static final int COLUMN_ID = 0;
   static final int COLUMN_FEED_ID = 1;
   /** Column index for title */
   static final int COLUMN_TITLE = 2;
   /** Column index for link */
   static final int COLUMN_URL_STRING = 3;

   /**
    * List of Cursor columns to read from when preparing an adapter to populate the ListView.
    */
   private static final String[] FROM_COLUMNS = new String[]{
           FeedContract.Entry.COLUMN_NAME_TITLE,
           FeedContract.Entry.COLUMN_NAME_LINK
   };

   private static final int[] TO_FIELDS = new int[]{
           android.R.id.text1,
           android.R.id.text2};

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    MyFeedItemRecyclerViewAdapter mAdapter = null;
    RecyclerView mRecyclerView  = null;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FeedItemFragment() {
    }


    @SuppressWarnings("unused")
    public static FeedItemFragment newInstance(int columnCount) {
        FeedItemFragment fragment = new FeedItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feeditem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mAdapter = new MyFeedItemRecyclerViewAdapter( mListener);
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mRecyclerView.setAdapter(mAdapter);
            getLoaderManager().initLoader(0, null, this);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
       return new CursorLoader(getActivity(),  // Context
                       FeedContract.Feed.CONTENT_URI, // URI
                       PROJECTION,                // Projection
                       null,                           // Selection
                       null,                           // Selection args
                       null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

    }
    public void restartLoader()
    {
        getLoaderManager().restartLoader(0,null,this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {

        void onListFragmentInteraction(  String FeedID, String currentTitle,String currentUrl , boolean bDelete );
    }
}
