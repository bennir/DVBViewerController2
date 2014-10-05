package de.bennir.dvbviewercontroller2.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.ChannelAdapter;
import de.bennir.dvbviewercontroller2.interfaces.RefreshChannels;
import de.bennir.dvbviewercontroller2.interfaces.RequestHost;
import de.bennir.dvbviewercontroller2.interfaces.SetChannelList;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;

public class ChannelFragment extends ListFragment
        implements ControllerActivity.ChannelSuccessCallback, SetChannelList {
    private static final String TAG = ChannelFragment.class.toString();

    private Context mContext;
    private String currentGroup = "";
    private ListView mListView;
    private ChannelAdapter mAdapter;
    private DVBHost Host;

    private List<Channel> mChannels = new ArrayList<Channel>();

    private RefreshChannels mRefreshCallback;
    private RequestHost mRequestHostCallback;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mRefreshCallback = (RefreshChannels) activity;
            mRequestHostCallback = (RequestHost) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onRefreshChannelListener, onRequestHostListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listview, container, false);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(Config.GROUP_KEY, currentGroup);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            Log.d(TAG, "onActivityCreated restore");
            currentGroup = savedInstanceState.getString(Config.GROUP_KEY);
        } else {
            currentGroup = getArguments().getString(Config.GROUP_KEY);
        }

        Log.d(TAG, "onActivityCreated " + currentGroup);

        mContext = getActivity().getApplicationContext();
        mListView = getListView();

        ControllerActivity act = (ControllerActivity) getActivity();
        act.mTitle = currentGroup;
        act.getActionBar().setTitle(currentGroup);

        act.addChannelCallback(this);

        mRefreshCallback.onRequestChannelListener(this);
        Host = mRequestHostCallback.onRequestHostListener();

        mAdapter = new ChannelAdapter(mContext, mChannels, Host, getActivity());
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mIntent = new Intent(getActivity(), ChannelDetailActivity.class);
                mIntent.putExtra(Config.CHANNEL_KEY, mChannels.get(position));
                mIntent.putExtra(Config.DVBHOST_KEY, Host);

                Bundle bundle;
                if (Build.VERSION.SDK_INT >= 21) {
                    ImageView logo = (ImageView) parent.findViewById(R.id.channel_item_logo);
                    parent.setTransitionGroup(false);

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), logo, "header_image");
                    bundle = options.toBundle();
                } else {
                    bundle = new Bundle();
                }
                startActivityForResult(mIntent, 1, bundle);
            }
        });
    }

    private void obtainData() {
        ((ControllerActivity) getActivity()).getChannels();
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
        Log.d(TAG, "onChannelSuccess " + mChannels.size());

        mAdapter = new ChannelAdapter(mContext, mChannels, Host, getActivity());
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((ControllerActivity) getActivity()).addChannelCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        ((ControllerActivity) getActivity()).removeChannelCallback(this);
    }

    @Override
    public void onSetChannelListener(ArrayList<Channel> channels) {
        Log.d(TAG, "onSetChannelListener");
        mChannels = Channel.createChannelMap(channels).get(currentGroup);
    }
}
