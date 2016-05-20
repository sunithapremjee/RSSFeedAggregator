package com.sunitha.rssfeedaggregator;

import android.accounts.Account;

import android.appwidget.AppWidgetManager;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.sunitha.rssfeedaggregator.provider.FeedContract;
import com.sunitha.rssfeedaggregator.sync.AddNewFeedTask;

import com.sunitha.rssfeedaggregator.sync.SyncAdapter;
import com.sunitha.rssfeedaggregator.sync.SyncUtils;




public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FeedItemFragment.OnListFragmentInteractionListener,
        AddNewFeedFragment.OnFragmentInteractionListener,
        SignInFragment.OnSignInFragmentInteractionListener{

    public static final String TAG = "MainActivity";
    public static final  String signinFragmentTag = "SIGNIN_FRAGMENTB_TAG";
    // Instance fields
    Account mAccount;

    Fragment mCurrentFragment = null;
    Toolbar mToolbar;
    ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    Menu mMenu;
    String mCurrentUrl,mCurrentTitle;
    TextView mEmailText;
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdView mAdView = (AdView) findViewById(R.id.adView);


        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

       SyncUtils.CreateSyncAccount(getApplicationContext());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                mToolbar,  /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) ;
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerToggle.setDrawerIndicatorEnabled(false);

        FragmentManager fragmentManager = getSupportFragmentManager();

        mCurrentFragment = new SignInFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mCurrentFragment,signinFragmentTag)
                .commit();


        mNavigationView= (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mEmailText= (TextView)findViewById(R.id.emailtextView);
    }

    @Override
    public void onBackPressed() {

        Log.e(TAG, "onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if( mCurrentFragment.getTag() == signinFragmentTag) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if( id == R.id.menu_refresh ) {
            new AddNewFeedTask(getApplicationContext()).execute(new String[]{mCurrentTitle, mCurrentUrl});
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment = null;
        String FRAGMENTTAG = null;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_FeedList) {
            mToolbar.setTitle(getString(R.string.list_feeds));

            nextFragment = new FeedItemFragment();
        } else if (id == R.id.nav_AddFeed) {
            mToolbar.setTitle(getString(R.string.new_feed));
            nextFragment = new AddNewFeedFragment();

        } else if (id == R.id.nav_About) {
            mToolbar.setTitle(getString(R.string.about_app));
            nextFragment = new AboutFragment();
        }
        else if( id == R.id.nav_signout)
        {
            mToolbar.setTitle(getString(R.string.signout_app));
            nextFragment = new SignInFragment();
            FRAGMENTTAG = signinFragmentTag;
        }

        mCurrentFragment = nextFragment;
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment,FRAGMENTTAG)
                .commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction( String FeedID , String currentTitle, String currentUrl, boolean bDelete ) {

        mCurrentUrl = currentUrl;
        mCurrentTitle = currentTitle;

        Log.i(TAG, mCurrentTitle + mCurrentUrl );
        if( bDelete )
        {
            SyncUtils.deleteFeedandEntries( getContentResolver(), FeedID );
            SyncAdapter.updateWidgets(getApplicationContext());
            ((FeedItemFragment) mCurrentFragment).restartLoader();
        }
        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Log.i(TAG, FeedID);
            Fragment nextFragment = EntryListFragment.newInstance(FeedID);

            mToolbar.setTitle(mCurrentTitle);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nextFragment).addToBackStack(mCurrentFragment.getClass().getName())
                    .commit();
            mCurrentFragment = nextFragment;
        }


    }

    @Override
    public void onAddNewFeedFragmentInteraction(String title, String Url ) {
        Log.i(TAG, "onAddNewFeedFragmentInteraction");

        if( URLUtil.isValidUrl(Url) ) {
            new AddNewFeedTask(getApplicationContext()).execute(new String[]{title, Url});

            // Setting the package ensures that only components in our app will receive the broadcast
            Intent dataUpdatedIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .setPackage(getApplicationContext().getPackageName());
            getApplicationContext().sendBroadcast(dataUpdatedIntent);
            Toast.makeText(getApplicationContext(), getString(R.string.feed_add_successful), Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getApplicationContext(),getString(R.string.invalid_url),Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSignInFragmentInteraction( boolean signedIn , String emailId ) {
        Log.i(TAG, "onSignInFragmentInteraction" + emailId);

        mDrawerToggle.setDrawerIndicatorEnabled(signedIn);


        if( signedIn)
            mEmailText.setText(emailId );

    }
}
