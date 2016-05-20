package com.sunitha.rssfeedaggregator.sync;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.sunitha.rssfeedaggregator.net.RssFeed;
import com.sunitha.rssfeedaggregator.net.RssReader;
import com.sunitha.rssfeedaggregator.provider.FeedContract;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Premjee on 5/5/2016.
 */
public class AddNewFeedTask extends AsyncTask<String, Integer, Long> {
    Context mContext;

    public static final String TAG = "AddNewFeedTask";
    public AddNewFeedTask( Context context)
    {
        mContext = context;
    }
    protected Long doInBackground(String... params) {
        if( params == null )
            return null;

        URL url = null;
        try {
            url = new URL(params[1]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        final ContentResolver contentResolver = mContext.getContentResolver();

        RssFeed feed = null;
        try {
            feed = RssReader.read(url);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (feed == null)
            return null;

        long feedId = addFeed(params[0],params[1]);
        SyncUtils.AddOrUpdateFeedEntry( mContext.getContentResolver(), feed, feedId );
        return feedId;
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(Long result) {

    }
    long addFeed( String title, String urlString ) {

        Log.i(TAG, "insert Feeds: addFeed");
        long feedId;

        // First, check if the feed with this url exists in the db
        Cursor feedCursor = mContext.getContentResolver().query(
                FeedContract.Feed.CONTENT_URI,
                new String[]{FeedContract.Feed._ID},
                FeedContract.Feed.COLUMN_NAME_LINK + " = ?",
                new String[]{urlString},
                null);

        if (feedCursor.moveToFirst()) {
            Log.i(TAG, "Feed link already exists" );
            int feedIdIndex = feedCursor.getColumnIndex(FeedContract.Feed._ID);
            feedId = feedCursor.getLong(feedIdIndex);
        } else {
            Log.i(TAG, "Feed link doesnt exists,adding new feed entry" );
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues feedValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            feedValues.put(FeedContract.Feed.COLUMN_NAME_TITLE, title);
            feedValues.put(FeedContract.Feed.COLUMN_NAME_LINK, urlString);


            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    FeedContract.Feed.CONTENT_URI,
                    feedValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            feedId = ContentUris.parseId(insertedUri);
        }

        feedCursor.close();

        return feedId;
    }
}