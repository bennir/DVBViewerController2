package de.bennir.dvbviewercontroller2.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
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

public class ControllerActivity extends Activity {
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

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public HashMap<String, List<Channel>> channelMap = new HashMap<String, List<Channel>>();
    public ArrayList<Channel> mChannels = new ArrayList<Channel>();
    public ArrayList<String> channelGroups = new ArrayList<String>();
    private DVBHost Host;

    private ChannelService channelService;
    private CommandService commandService;

    private List<ChannelSuccessCallback> mChannelCallbacks = new ArrayList<ChannelSuccessCallback>();
    private Runnable mDemoChannelRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "CreateDemoChannels");
            mChannels = Config.createDemoChannels();
            createChannelMap();

            for (ChannelSuccessCallback cb : mChannelCallbacks) {
                if (cb != null) {
                    cb.onChannelSuccess();
                }
            }
        }
    };
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://" + Host.Ip + ":" + Host.Port + "/dvb")
                .build();

        commandService = restAdapter.create(CommandService.class);
        channelService = restAdapter.create(ChannelService.class);

        getChannels();

        Log.d(TAG, "Device " + Host.Name + " (" + Host.Ip + ":" + Host.Port + ")");

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
                adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_ab_up_white));
            }


            // Tablet Layout, 3 Columns
            if (mContainer.getTag().equals("three_column")) {
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
        Bundle bundle = new Bundle();

        switch (pos) {
            case 0:
                mTitle = getString(R.string.remote);
                fragment = new RemoteFragment();
                break;
            case 1:
                mTitle = getString(R.string.channels);
                fragment = new ChannelGroupFragment();

                bundle.putParcelable(Config.DVBHOST_KEY, Host);
                bundle.putParcelableArrayList(Config.CHANNEL_LIST_KEY, mChannels);
                bundle.putStringArrayList(Config.CHANNEL_GROUP_LIST_KEY, channelGroups);

                fragment.setArguments(bundle);
                break;
            case 2:
                mTitle = getString(R.string.epg);
                fragment = new ChannelSearchFragment();

                bundle.putParcelable(Config.DVBHOST_KEY, Host);
                bundle.putParcelableArrayList(Config.CHANNEL_LIST_KEY, mChannels);

                fragment.setArguments(bundle);
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

    public void getChannels() {
        Log.d(TAG, "getChannels()");

        channelGroups.clear();
        mChannels.clear();

        if (!Host.Name.equals("localhost")) {
            channelService.getChannels(new Callback<ArrayList<Channel>>() {
                @Override
                public void success(ArrayList<Channel> channels, Response response) {
                    mChannels = channels;
                    createChannelMap();

                    for (ChannelSuccessCallback cb : mChannelCallbacks) {
                        if (cb != null) {
                            cb.onChannelSuccess();
                        }
                    }
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

    private void createChannelMap() {
        String currentGroup = "";
        List<Channel> channelGroup = new ArrayList<Channel>();

        for (Channel chan : mChannels) {
            // Channel Group Names
            if (!channelGroups.contains(chan.Group)) {
                channelGroups.add(chan.Group);
            }

            // Channel Map Group->List<Channel>
            if (chan.Group.equals(currentGroup)) {
                channelGroup.add(chan);
            } else {
                if (currentGroup.equals("")) {
                    currentGroup = chan.Group;
                    channelGroup.add(chan);
                } else {
                    channelMap.put(currentGroup, channelGroup);
                    channelGroup = new ArrayList<Channel>();

                    currentGroup = chan.Group;
                    channelGroup.add(chan);
                }
            }
        }
        channelMap.put(currentGroup, channelGroup);
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

        if(requestCode == 1) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "Restore Instance State");
        if (mContainer.getTag().equals("three_column") && savedInstanceState.getInt(STATE_SELECTED_POSITION) == 0) {
            mCurrentSelectedPosition++;
            selectItem(mCurrentSelectedPosition);
        }
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

                title.setText(getItem(position).getTitle());
                icon.setImageDrawable(getResources().getDrawable(getItem(position).getIcon()));
            }

            return convertView;
        }

    }

    public void addChannelCallback(Fragment fragment) {
        try {
            if(!mChannelCallbacks.contains((ChannelSuccessCallback) fragment)) {
                mChannelCallbacks.add((ChannelSuccessCallback) fragment);
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement ChannelSuccessCallback.");
        }
    }

    public void removeChannelCallback(Fragment fragment) {
        try {
            mChannelCallbacks.remove((ChannelSuccessCallback) fragment);
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement ChannelSuccessCallback.");
        }
    }

    public static interface ChannelSuccessCallback {
        void onChannelSuccess();
    }
}
