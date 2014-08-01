package de.bennir.dvbviewercontroller2.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.DVBMenuItem;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class ControllerActivity extends Activity {
    private static final String TAG = ControllerActivity.class.toString();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    public DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private FrameLayout mDrawer;
    private FrameLayout mContainer;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private DVBService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Config.DVB_HOST = getIntent().getStringExtra(Config.DVBHOST_KEY);
        Config.DVB_IP = getIntent().getStringExtra(Config.DVBIP_KEY);
        Config.DVB_PORT = getIntent().getStringExtra(Config.DVBPORT_KEY);

        mService = DVBService.getInstance(getApplicationContext());

        Log.d(TAG, "Device " + Config.DVB_HOST + " (" + Config.DVB_IP + ":" + Config.DVB_PORT + ")");

        mContainer = (FrameLayout) findViewById(R.id.container);

        MenuAdapter adapter = new MenuAdapter(getApplicationContext());

        mDrawerListView = (ListView) findViewById(R.id.drawer_list);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Phone Layout with Navigation Drawer
        if (mDrawerLayout != null) {
            mTitle = getString(R.string.remote);

            adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_ab_up_white));
            adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_ab_up_white));
            adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_ab_up_white));
            adapter.add(new DVBMenuItem(getString(R.string.timers), R.drawable.ic_ab_up_white));

            mDrawerListView.setAdapter(adapter);

            mDrawer = (FrameLayout) findViewById(R.id.drawer);

            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            // set up the drawer's list view with items and click listener

            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);


            // ActionBarDrawerToggle ties together the the proper interactions
            // between the navigation drawer and the action bar app icon.
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                    /* host Activity */
                    mDrawerLayout,                    /* DrawerLayout object */
                    R.drawable.ic_ab_drawer,             /* nav drawer image to replace 'Up' caret */
                    R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                    R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
            ) {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);

                    invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);

                    if (!mUserLearnedDrawer) {
                        // The user manually opened the drawer; store this flag to prevent auto-showing
                        // the navigation drawer automatically in the future.
                        mUserLearnedDrawer = true;
                        SharedPreferences sp = PreferenceManager
                                .getDefaultSharedPreferences(getApplicationContext());
                        sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                    }

                    invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                }
            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);

            // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
            // per the navigation drawer design guidelines.
            if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
                mDrawerLayout.openDrawer(mDrawer);
            }

            // Defer code dependent on restoration of previous instance state.
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });
        } else {
            // Tablet Layout, 2 Columns
            mTitle = getString(R.string.remote);
            if(mContainer.getTag().equals("two_column")) {
                adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_ab_up_white));
            }


            // Tablet Layout, 3 Columns
            if(mContainer.getTag().equals("three_column")) {
                mTitle = getString(R.string.channels);
                adapter.add(new DVBMenuItem("three_column", R.drawable.ic_ab_up_white));

                mCurrentSelectedPosition++;
            }

            adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_ab_up_white));
            adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_ab_up_white));
            adapter.add(new DVBMenuItem(getString(R.string.timers), R.drawable.ic_ab_up_white));

            mDrawerListView.setAdapter(adapter);
        }

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawer);
        }

        Fragment fragment;

        int pos = position;
        // Phone Layout with Navigation Drawer
//        if (mDrawerLayout == null && mContainer.getTag().equals("three_column")) {
//            pos++;
//        }

        switch (pos) {
            case 0:
                mTitle = getString(R.string.remote);
                fragment = new RemoteFragment();
                break;
            case 1:
                mTitle = getString(R.string.channels);
                fragment = new ChannelGroupFragment();
                break;
            case 2:
                mTitle = getString(R.string.epg);
                fragment = new RemoteFragment();
                break;
            case 3:
                mTitle = getString(R.string.timers);
                fragment = new RemoteFragment();
                break;
            default:
                mTitle = getString(R.string.remote);
                fragment = new RemoteFragment();
                break;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawer);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.controller, menu);
            restoreActionBar();
            return true;
        }

        if (mDrawerLayout != null) {
//            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.

        // Phone Layout with Navigation Drawer
        if (mDrawerLayout != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.

        // Phone Layout with Navigation Drawer
        if (mDrawerLayout != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Phone Layout with Navigation Drawer
        if (mDrawerLayout != null) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "Restore Instance State");
        if(mContainer.getTag().equals("three_column") && savedInstanceState.getInt(STATE_SELECTED_POSITION) == 0) {
            mCurrentSelectedPosition++;
            selectItem(mCurrentSelectedPosition);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mService.destroy();
    }

    private class MenuAdapter extends ArrayAdapter<DVBMenuItem> {

        public MenuAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(getItem(position).getTitle().equals("three_column")) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item_null, null);
            } else {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.list_item_menu, null);
                }

                TextView title = (TextView) convertView.findViewById(R.id.menu_title);
                ImageView icon = (ImageView) convertView.findViewById(R.id.menu_icon);

                title.setText(getItem(position).getTitle());
                icon.setImageDrawable(getResources().getDrawable(getItem(position).getIcon()));
            }

            return convertView;
        }

    }

}
