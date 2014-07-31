package de.bennir.dvbviewercontroller2.service;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBCommand;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DVBService {
    private static final String TAG = DVBService.class.toString();

    private Context mContext;
    private static DVBService _instance;

    private RestAdapter restAdapter;
    private ChannelService channelService;
    private CommandService commandService;
    private ChannelSuccessCallback mChannelCallback;

    public HashMap<String, List<String>> channelGroupMap = new HashMap<String, List<String>>();
    public List<Channel> channels = new ArrayList<Channel>();

    private DVBService(Context mContext) {
        Log.d(TAG, "DVBService()");

        this.mContext = mContext;

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://" + Config.DVB_IP + ":" + Config.DVB_PORT + "/dvb")
                .build();

        commandService = restAdapter.create(CommandService.class);
        channelService = restAdapter.create(ChannelService.class);
    }

    public static DVBService getInstance() {
        return _instance;
    }

    public static DVBService getInstance(Context mContext) {
        Log.d(TAG, "getInstance()");

        if (_instance == null) {
            _instance = new DVBService(mContext);
        }

        return _instance;
    }

    public void destroy() {
        _instance.destroy();
        _instance = null;
    }


    public void sendCommand(DVBCommand cmd) {
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

    public void getChannels() {
        Log.d(TAG, "getChannels()");

        channels.clear();

        channelService.getChannels(new Callback<List<Channel>>() {
            @Override
            public void success(List<Channel> channels, Response response) {
                DVBService.getInstance().channels = channels;
                DVBService.getInstance().mChannelCallback.onChannelSuccess();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    public void addChannelCallback(Fragment fragment) {
        try {
            mChannelCallback = (ChannelSuccessCallback) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement ChannelSuccessCallback.");
        }
    }

    public static interface ChannelSuccessCallback {
        void onChannelSuccess();
    }
}
