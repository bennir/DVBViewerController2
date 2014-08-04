package de.bennir.dvbviewercontroller2.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
                Intent mIntent = new Intent(getActivity(), ChannelDetailActivity.class);
                mIntent.putExtra(Config.CHANNEL_KEY, channels.get(position));

                startActivity(mIntent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String channelId = String.valueOf(mAdapter.getItem(position).Id);
                Log.d(TAG, "channelId: " + channelId);
                setChannel(channelId);
                return true;
            }
        });
    }

    void setChannel(String channelId) {
        ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

        if (!Config.DVB_HOST.equals("localhost")) {
            mService.setChannel(channelId);
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
