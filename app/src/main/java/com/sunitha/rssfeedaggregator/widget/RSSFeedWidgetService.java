package com.sunitha.rssfeedaggregator.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.sunitha.rssfeedaggregator.R;
import com.sunitha.rssfeedaggregator.net.Entry;
import com.sunitha.rssfeedaggregator.provider.FeedContract;
import com.sunitha.rssfeedaggregator.sync.SyncAdapter;

import java.util.ArrayList;
import java.util.List;

public class RSSFeedWidgetService extends RemoteViewsService {


    public static final String TAG = "RSSFeedWidgetService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        Log.i(TAG, "RemoteViewsFactory");
        return new RSSFeedRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class RSSFeedRemoteViewsFactory implements
            RemoteViewsService.RemoteViewsFactory {


        private Cursor mCursor = null;

        private Context mContext;
        private int mAppWidgetId;

        public RSSFeedRemoteViewsFactory(Context context, Intent intent) {

            Log.i(TAG, "RSSFeedRemoteViewsFactory");
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Initialize the data set.
        public void onCreate() {

           mCursor = getContentResolver().query(FeedContract.Entry.CONTENT_URI, null, null, null, null);
        }

        @Override
        public void onDataSetChanged() {
            mCursor = getContentResolver().query(FeedContract.Entry.CONTENT_URI, null, null, null, FeedContract.Entry.COLUMN_NAME_PUBLISHED + " desc");

        }

        @Override
        public void onDestroy() {

            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

        }

        @Override
        public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }


        // Given the position (index) of a WidgetItem in the array, use the item's text value in
        // combination with the app widget item XML file to construct a RemoteViews object.
        public RemoteViews getViewAt(int position) {

            Log.i(TAG, "getViewAt");

            if( mCursor == null || mCursor.getCount() == 0)
                return null;
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.rss_feed_widget_item);
            if( mCursor.moveToPosition(position) ) {
                Log.i(TAG, mCursor.getString(SyncAdapter.COLUMN_TITLE));
                remoteViews.setTextViewText(R.id.Feed_ItemTitle, mCursor.getString(SyncAdapter.COLUMN_TITLE));
                Time t = new Time();
                t.set(mCursor.getLong(SyncAdapter.COLUMN_PUBLISHED));

                remoteViews.setTextViewText(R.id.FeedEntry_time,t.format("%Y-%m-%d %H:%M"));
            }
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if( mCursor.moveToPosition(position) ){

                return mCursor.getLong( SyncAdapter.COLUMN_ID );
            }
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

    }
}
