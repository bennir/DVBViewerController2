package de.bennir.dvbviewercontroller2.ui;

import android.app.ActivityOptions;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.ChannelAdapter;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.service.ChannelService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChannelFragment extends ListFragment
        implements ControllerActivity.ChannelSuccessCallback {
    private static final String TAG = ChannelFragment.class.toString();

    private Context mContext;
    private String currentGroup = "";
    private ListView mListView;
    private ChannelAdapter mAdapter;
    private DVBHost Host;

    private ChannelService channelService;
    private RestAdapter restAdapter;

    private List<Channel> channels = new ArrayList<Channel>();

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

        currentGroup = getArguments().getString(Config.GROUP_KEY);
        Host = getArguments().getParcelable(Config.DVBHOST_KEY);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://" + Host.Ip + ":" + Host.Port + "/dvb")
                .build();

        channelService = restAdapter.create(ChannelService.class);

        mListView = getListView();

        ControllerActivity act = (ControllerActivity) getActivity();
        act.mTitle = currentGroup;
        getActivity().getActionBar().setTitle(currentGroup);

        ((ControllerActivity) getActivity()).addChannelCallback(this);

        channels = ((ControllerActivity) getActivity()).channelMap.get(currentGroup);

        mAdapter = new ChannelAdapter(mContext, channels, Host);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mIntent = new Intent(getActivity(), ChannelDetailActivity.class);
                mIntent.putExtra(Config.CHANNEL_KEY, channels.get(position));
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

//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                String channelId = String.valueOf(mAdapter.getItem(position).Id);
//                Log.d(TAG, "channelId: " + channelId);
//                setChannel(channelId);
//                return true;
//            }
//        });
    }

    void setChannel(String channelId) {
        ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

        if (!Host.Name.equals("localhost")) {
            Channel channel = new Channel();
            channel.ChannelId = channelId;

            channelService.setChannel(channel, new Callback<Channel>() {
                @Override
                public void success(Channel dvbCommand, Response response) {
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, error.toString());
                }
            });
        }
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
        channels = ((ControllerActivity) getActivity()).channelMap.get(currentGroup);

        mAdapter = new ChannelAdapter(mContext, channels, Host);
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
}
