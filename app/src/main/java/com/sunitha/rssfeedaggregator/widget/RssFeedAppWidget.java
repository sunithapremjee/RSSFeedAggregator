package com.sunitha.rssfeedaggregator.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sunitha.rssfeedaggregator.MainActivity;
import com.sunitha.rssfeedaggregator.R;

/**
 * Implementation of App Widget functionality.
 */
public class RssFeedAppWidget extends AppWidgetProvider {

    public static final String TAG = "RssFeedAppWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Log.d(LOG_TAG, "onUpdate");


        for (int i = 0; i < appWidgetIds.length; i++) {

            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);

        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.widget_text);

        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.rss_feed_app_widget);
        remoteViews.setTextViewText(R.id.appwidget_text, widgetText);
        Intent intent = new Intent( context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent( R.id.widget_container, pendingIntent );


        Intent remoteViewsServiceIntent = new Intent( context, RSSFeedWidgetService.class );
        remoteViewsServiceIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId );
        remoteViewsServiceIntent.setData( Uri.parse( remoteViewsServiceIntent.toUri( Intent.URI_INTENT_SCHEME ) ) );
        remoteViews.setRemoteAdapter( R.id.widget_list, remoteViewsServiceIntent );

        appWidgetManager.updateAppWidget( appWidgetId, remoteViews );
    }

}

