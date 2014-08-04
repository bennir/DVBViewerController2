package de.bennir.dvbviewercontroller2.ui;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.ChannelAdapter;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class ChannelFragment extends ListFragment
        implements DVBService.ChannelSuccessCallback {
    private static final String TAG = ChannelFragment.class.toString();

    private Context mContext;
    private DVBService mService;
    private String currentChan = "";
    private ListView mListView;
    private View activeView;
    private ChannelAdapter mAdapter;
    List<Channel> channels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listview, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        mContext = getActivity().getApplicationContext();
        currentChan = getArguments().getString(Config.CHANNEL_KEY);
        mListView = getListView();

        ControllerActivity act = (ControllerActivity) getActivity();
        act.mTitle = currentChan;
        getActivity().getActionBar().setTitle(currentChan);

        mService = DVBService.getInstance(mContext);
        mService.addChannelCallback(this);

        channels = mService.channelMap.get(currentChan);

        mAdapter = new ChannelAdapter(mContext, channels, mService);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String channelId = ((TextView) view.findViewById(R.id.channel_item_favid)).getText().toString();
                String channelId = String.valueOf(mAdapter.getItem(position).Id);
                Log.d(TAG, "channelId: " + channelId);
                setChannel(channelId);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showChannelMenu(view);
                return true;
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    clearChannelMenu();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    void setChannel(String channelId) {
        ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

        if (!Config.DVB_HOST.equals("localhost")) {
            mService.setChannel(channelId);
        }
    }

    private void showChannelMenu(View view) {
        clearChannelMenu();
        activeView = view;
        LinearLayout subMenu = (LinearLayout) view.findViewById(R.id.channel_item_submenu);
        subMenu.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.channel_item_progress);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        subMenu.setLayoutParams(params);
    }

    private void clearChannelMenu() {
        if (activeView != null) {
            LinearLayout subMenu = (LinearLayout) activeView.findViewById(R.id.channel_item_submenu);
            subMenu.setVisibility(View.INVISIBLE);
            subMenu.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0));
            activeView = null;
        }
    }

    private void obtainData() {
        mService.channelGroups.clear();

        mService.getChannels();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                obtainData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onChannelSuccess() {
        channels = mService.channelMap.get(currentChan);

        mAdapter = new ChannelAdapter(mContext, channels, mService);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        mService.addChannelCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mService.removeChannelCallback(this);
    }
}
