package de.bennir.dvbviewercontroller2.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.interfaces.RefreshChannels;
import de.bennir.dvbviewercontroller2.interfaces.RequestHost;
import de.bennir.dvbviewercontroller2.interfaces.SetChannelList;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBCommand;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.model.DVBMenuItem;
import de.bennir.dvbviewercontroller2.service.ChannelService;
import de.bennir.dvbviewercontroller2.service.CommandService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ControllerActivity extends Activity
        implements RefreshChannels, RequestHost {
    private static final String TAG = ControllerActivity.class.toString();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public CharSequence mTitle;

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
    private Fragment mContent;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public ArrayList<Channel> mChannels = new ArrayList<Channel>();
    private DVBHost Host;

    private ChannelService channelService;
    private CommandService commandService;

    private boolean mIsRefreshing = false;
    private List<ChannelSuccessCallback> mChannelCallbacks = new ArrayList<ChannelSuccessCallback>();
    private Runnable mDemoChannelRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "CreateDemoChannels");
            mChannels = Config.createDemoChannels();

            handleChannelCallback();

            mIsRefreshing = false;
        }
    };
    private Handler mHandler;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putParcelableArrayList(Config.CHANNEL_LIST_KEY, mChannels);
        outState.putParcelable(Config.DVBHOST_KEY, Host);

//        getFragmentManager().putFragment(outState, "mContent", mContent);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mChannelCallbacks.clear();

        Log.d(TAG, "Restore Instance State");
        if (mContainer.getTag().equals("three_column") && savedInstanceState.getInt(STATE_SELECTED_POSITION) == 0) {
            mCurrentSelectedPosition++;
        }

        mChannels = savedInstanceState.getParcelableArrayList(Config.CHANNEL_LIST_KEY);
        Host = savedInstanceState.getParcelable(Config.DVBHOST_KEY);
//
//        Log.d(TAG, "Restore Position " + mCurrentSelectedPosition);
////        selectItem(mCurrentSelectedPosition);
//
//        mContent = getFragmentManager().getFragment(savedInstanceState, "mContent");
//        getFragmentManager()
//                .beginTransaction()
//                .replace(R.id.container, mContent)
//                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition( new Slide() );
        getWindow().setExitTransition( new Slide() );

        setContentView(R.layout.activity_controller);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate restore");
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;

//            mContent = getFragmentManager().getFragment(savedInstanceState, "mContent");

            mChannels = savedInstanceState.getParcelableArrayList(Config.CHANNEL_LIST_KEY);
            Host = savedInstanceState.getParcelable(Config.DVBHOST_KEY);
        } else {
            mTitle = getString(R.string.remote);
            mContent = new RemoteFragment();
        }

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Host.getUrl() + "/dvb")
                .build();

        commandService = restAdapter.create(CommandService.class);
        channelService = restAdapter.create(ChannelService.class);

        if (mChannels.isEmpty()) {
            getChannels();
        }

        Log.d(TAG, "Device " + Host.toString());

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

            adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote));
            adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_action_channels));
            adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_action_epg));
            adapter.add(new DVBMenuItem(getString(R.string.timers), R.drawable.ic_action_timers));

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
            if (mContainer.getTag().equals("two_column")) {
                adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote));
            }


            // Tablet Layout, 3 Columns
            if (mContainer.getTag().equals("three_column")) {
                mTitle = getString(R.string.channels);
                adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote));

                mCurrentSelectedPosition++;
            }

            adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_action_channels));
            adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_action_epg));
            adapter.add(new DVBMenuItem(getString(R.string.timers), R.drawable.ic_action_timers));

            mDrawerListView.setAdapter(adapter);
        }

        // Select either the default item (0) or the last selected item.
//        selectItem(mCurrentSelectedPosition);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, mContent)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAfterTransition();
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawer);
        }

        int pos = position;
        Bundle bundle = new Bundle();
        String mFragmentTag = "";

        switch (pos) {
            case 0:
                mTitle = getString(R.string.remote);
                mContent = new RemoteFragment();
                mFragmentTag = "fragment_remote";
                break;
            case 1:
                mTitle = getString(R.string.channels);
                mContent = new ChannelGroupFragment();

                bundle.putParcelable(Config.DVBHOST_KEY, Host);
                mContent.setArguments(bundle);
                if (!mChannels.isEmpty()) {
                    ((ChannelGroupFragment) mContent).setChannelGroups(Channel.createChannelGroups(mChannels));
                }
                mFragmentTag = "fragment_channelgroup";
                break;
            case 2:
                mTitle = getString(R.string.epg);
                mContent = new ChannelSearchFragment();

                bundle.putParcelable(Config.DVBHOST_KEY, Host);
                bundle.putParcelableArrayList(Config.CHANNEL_LIST_KEY, mChannels);

                mContent.setArguments(bundle);
                mFragmentTag = "fragment_channelsearch";
                break;
            case 3:
                mTitle = getString(R.string.timers);
                mContent = new RemoteFragment();
                mFragmentTag = "fragment_remote";
                break;
            default:
                mTitle = getString(R.string.remote);
                mContent = new RemoteFragment();
                mFragmentTag = "fragment_remote";
                break;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mContent, mFragmentTag)
                .commit();
    }

    public void getChannels() {
        Log.d(TAG, "getChannels()");

        mIsRefreshing = true;

        mChannels.clear();

        if (!Host.Name.equals("localhost")) {
            channelService.getChannels(new Callback<ArrayList<Channel>>() {
                @Override
                public void success(ArrayList<Channel> channels, Response response) {
                    mChannels = channels;

                    handleChannelCallback();

                    mIsRefreshing = false;
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else {
            mHandler = new Handler();
            mHandler.postDelayed(mDemoChannelRunnable, 3000);
        }
    }

    private void handleChannelCallback() {
        ChannelGroupFragment channelGroupFrag = (ChannelGroupFragment) getFragmentManager().findFragmentByTag("fragment_channelgroup");
        if (channelGroupFrag != null) {
            channelGroupFrag.setChannelGroups(Channel.createChannelGroups(mChannels));
        }

        for (ChannelSuccessCallback cb : mChannelCallbacks) {
            if (cb != null) {
                cb.onChannelSuccess();
            }
        }
    }

    public void sendCommand(DVBCommand cmd) {
        if (!Host.Name.equals("localhost")) {
            commandService.sendCommand(cmd, new Callback<DVBCommand>() {
                @Override
                public void success(DVBCommand dvbCommand, Response response) {
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, error.toString());
                }
            });
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Host = data.getParcelableExtra(Config.DVBHOST_KEY);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            sendCommand(new DVBCommand(Config.LEFT));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            sendCommand(new DVBCommand(Config.RIGHT));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
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
    public void onRefreshChannelListener() {
        if (!mIsRefreshing) {
            // Deliver Current Channels first, updated Later
            if (mChannels != null) {
                handleChannelCallback();
            }

            getChannels();
        }
    }

    @Override
    public void onRequestChannelListener(Fragment fragment) {
        try {
            SetChannelList callback = (SetChannelList) fragment;
            callback.onSetChannelListener(mChannels);
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement onRefreshChannelListener, onRequestHostListener");
        }
    }

    @Override
    public DVBHost onRequestHostListener() {
        return Host;
    }

    private class MenuAdapter extends ArrayAdapter<DVBMenuItem> {

        public MenuAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItem(position).getTitle().equals("three_column")) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item_null, null);
            } else {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.list_item_menu, null);
                }

                TextView title = (TextView) convertView.findViewById(R.id.menu_title);
                ImageView icon = (ImageView) convertView.findViewById(R.id.menu_icon);

                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
                title.setTypeface(tf);
                title.setText(getItem(position).getTitle());
                icon.setImageDrawable(getResources().getDrawable(getItem(position).getIcon()));
            }

            return convertView;
        }

    }

    public void addChannelCallback(Fragment fragment) {
        try {
            if (!mChannelCallbacks.contains((ChannelSuccessCallback) fragment)) {
                Log.d(TAG, "addChannelCallback: " + fragment.getClass().getName());
                mChannelCallbacks.add((ChannelSuccessCallback) fragment);
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement ChannelSuccessCallback.");
        }
    }

    public void removeChannelCallback(Fragment fragment) {
        try {
            Log.d(TAG, "removeChannelCallback: " + fragment.getClass().getName());
            mChannelCallbacks.remove((ChannelSuccessCallback) fragment);
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement ChannelSuccessCallback.");
        }
    }

    public static interface ChannelSuccessCallback {
        void onChannelSuccess();
    }
}
