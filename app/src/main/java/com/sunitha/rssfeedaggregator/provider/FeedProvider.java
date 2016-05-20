/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sunitha.rssfeedaggregator.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.sunitha.rssfeedaggregator.common.db.SelectionBuilder;


public class FeedProvider extends ContentProvider {
    FeedDatabase mDatabaseHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = FeedContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_FEEDS = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_FEEDS_ID = 2;


    public static final int ROUTE_ENTRIES = 3;

    public static final int ROUTE_ENTRIES_ID = 4;

    public static final int ROUTE_ENTRY_WITH_FEED = 5;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "feeds", ROUTE_FEEDS);
        sUriMatcher.addURI(AUTHORITY, "feeds/*", ROUTE_FEEDS_ID);

        sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }

    private static final SQLiteQueryBuilder sEntryByFeedSettingQueryBuilder;

    static{
        sEntryByFeedSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sEntryByFeedSettingQueryBuilder.setTables(
                FeedContract.Entry.TABLE_NAME + " INNER JOIN " +
                        FeedContract.Feed.TABLE_NAME +
                        " ON " + FeedContract.Entry.TABLE_NAME +
                        "." + FeedContract.Entry.COLUMN_FEED_KEY +
                        " = " + FeedContract.Feed.TABLE_NAME +
                        "." + FeedContract.Feed._ID);
    }
    private static final String sFeedSettingSelection =
            FeedContract.Feed.TABLE_NAME+
                    "." + FeedContract.Feed._ID + " = ? ";



    private Cursor getEntryByFeedSetting(Uri uri, String[] projection, String sortOrder) {
        String feedSetting = FeedContract.Entry.getFeedSettingFromUri(uri);


        String[] selectionArgs;
        String selection;

        selection = sFeedSettingSelection;
        selectionArgs = new String[]{feedSetting};


        return sEntryByFeedSettingQueryBuilder.query(mDatabaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new FeedDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_FEEDS:
                return FeedContract.Feed.CONTENT_TYPE;
            case ROUTE_FEEDS_ID:
                return FeedContract.Feed.CONTENT_ITEM_TYPE;
            case ROUTE_ENTRIES:
                return FeedContract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return FeedContract.Entry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_FEEDS_ID:
                // Return a single Feed, by ID.
                String id = uri.getLastPathSegment();
                builder.where(FeedContract.Feed._ID + "=?", id);
            case ROUTE_FEEDS:
                // Return all known Feeds.
                builder.table(FeedContract.Feed.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_ENTRIES_ID:
                // Return a single entry, by ID.
                String id1 = uri.getLastPathSegment();
                builder.where(FeedContract.Entry._ID + "=?", id1);
            case ROUTE_ENTRIES:
                // Return all known entries.
                builder.table(FeedContract.Entry.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c1 = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx1 = getContext();
                assert ctx1 != null;
                c1.setNotificationUri(ctx1.getContentResolver(), uri);
                return c1;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_FEEDS:
                long id = db.insertOrThrow(FeedContract.Feed.TABLE_NAME, null, values);
                result = Uri.parse(FeedContract.Feed.CONTENT_URI + "/" + id);
                break;
            case ROUTE_FEEDS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_ENTRIES:
                long id1 = db.insertOrThrow(FeedContract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(FeedContract.Entry.CONTENT_URI + "/" + id1);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_FEEDS:
                count = builder.table(FeedContract.Feed.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_FEEDS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(FeedContract.Feed.TABLE_NAME)
                        .where(FeedContract.Feed._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES:
                count = builder.table(FeedContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id1 = uri.getLastPathSegment();
                count = builder.table(FeedContract.Entry.TABLE_NAME)
                       .where(FeedContract.Entry._ID + "=?", id1)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_FEEDS:
                count = builder.table(FeedContract.Feed.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_FEEDS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(FeedContract.Feed.TABLE_NAME)
                        .where(FeedContract.Feed._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES:
                count = builder.table(FeedContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id1 = uri.getLastPathSegment();
                count = builder.table(FeedContract.Entry.TABLE_NAME)
                        .where(FeedContract.Entry._ID + "=?", id1)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class FeedDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "feeds.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "feed" table. */
        private static final String SQL_CREATE_FEEDS =
                "CREATE TABLE " + FeedContract.Feed.TABLE_NAME + " (" +
                        FeedContract.Feed._ID + " INTEGER PRIMARY KEY," +
                        FeedContract.Feed.COLUMN_NAME_FEED_ID + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Feed.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Feed.COLUMN_NAME_LINK + TYPE_TEXT + ")";

        /** SQL statement to drop "Feed" table. */
        private static final String SQL_DELETE_FEEDS =
                "DROP TABLE IF EXISTS " + FeedContract.Feed.TABLE_NAME;

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedContract.Entry.TABLE_NAME + " (" +
                        FeedContract.Entry._ID + " INTEGER PRIMARY KEY," +
                        FeedContract.Entry.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                        FeedContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER + COMMA_SEP +
                        FeedContract.Entry.COLUMN_FEED_KEY + TYPE_TEXT + COMMA_SEP +
                        "FOREIGN KEY(" + FeedContract.Entry.COLUMN_FEED_KEY + ") REFERENCES " +
                        FeedContract.Feed.TABLE_NAME + "(" + FeedContract.Feed._ID  + "))";

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedContract.Entry.TABLE_NAME;


        public FeedDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_FEEDS);
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            db.execSQL(SQL_DELETE_FEEDS);
            onCreate(db);
        }
    }
}
