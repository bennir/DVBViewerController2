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
import de.bennir.dvbviewercontroller2.model.EpgInfo;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DVBService {
    private static final String TAG = DVBService.class.toString();

    private Context mContext;
    private Config mConfig;
    private static DVBService _instance;

    private RestAdapter restAdapter;
    private ChannelService channelService;
    private CommandService commandService;
    private List<ChannelSuccessCallback> mChannelCallbacks = new ArrayList<ChannelSuccessCallback>();

    public HashMap<String, List<Channel>> channelMap = new HashMap<String, List<Channel>>();
    public List<Channel> channels = new ArrayList<Channel>();
    public ArrayList<String> channelGroups = new ArrayList<String>();

    private DVBService(Context mContext) {
        Log.d(TAG, "DVBService()");

        this.mContext = mContext;
        this.mConfig = Config.getInstance(mContext);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://" + mConfig.getIp() + ":" + mConfig.getPort() + "/dvb")
                .build();

        commandService = restAdapter.create(CommandService.class);
        channelService = restAdapter.create(ChannelService.class);

    }

    public static DVBService getInstance() {
        return _instance;
    }

    public static DVBService getInstance(Context mContext) {
        if (_instance == null) {
            _instance = new DVBService(mContext.getApplicationContext());
        }

        return _instance;
    }

    public void destroy() {
        _instance = null;
    }


    public void sendCommand(DVBCommand cmd) {
        if (!mConfig.getHost().equals("localhost")) {
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

    public void setChannel(String channelId) {
        if (!mConfig.getHost().equals("localhost")) {
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

    public void getChannels() {
        Log.d(TAG, "getChannels()");

        channels.clear();

        Log.d(TAG, mConfig.Host);

//        if (!mConfig.getHost().equals("localhost")) {
//            channelService.getChannels(new Callback<List<Channel>>() {
//                @Override
//                public void success(List<Channel> channels, Response response) {
//                    DVBService.getInstance().channels = channels;
//                    createChannelMap();
//
//                    for(ChannelSuccessCallback cb : mChannelCallbacks) {
//                        if(cb != null) {
//                            cb.onChannelSuccess();
//                        }
//                    }
//                }
//
//                @Override
//                public void failure(RetrofitError error) {
//                    Log.e(TAG, error.toString());
//                }
//            });
//        } else {
//            createDemoChannels();
//
//            for(ChannelSuccessCallback cb : mChannelCallbacks) {
//                if(cb != null) {
//                    cb.onChannelSuccess();
//                }
//            }
//        }
    }

    // TODO: JSON RAW File
    private void createDemoChannels() {
        Log.d(TAG, "createDemoChannels()");
        Channel test = new Channel();
        test.Name = "Das Erste HD";
        test.Group = "ARD";
        EpgInfo epg = new EpgInfo();
        epg.ChannelName = test.Name;
        epg.Desc = "Nachrichten";
        epg.Time = "20:15";
        epg.Title = "Nachrichten";
        test.Epg = epg;

        channels.add(test);

        for (int i = 0; i < 10; i++) {
            test = new Channel();
            test.Name = "NDR HD " + i;
            test.Group = "ARD";
            epg = new EpgInfo();
            epg.ChannelName = test.Name;
            epg.Desc = "Nachrichten";
            epg.Time = "20:15";
            epg.Title = "Nachrichten";
            test.Epg = epg;

            channels.add(test);
        }

        test = new Channel();
        test.Name = "ZDF HD";
        test.Group = "ZDF";
        epg = new EpgInfo();
        epg.ChannelName = test.Name;
        epg.Desc = "Nachrichten";
        epg.Time = "20:15";
        epg.Title = "Nachrichten";
        test.Epg = epg;

        channels.add(test);

        for (int i = 0; i < 10; i++) {
            test = new Channel();
            test.Name = "ZDF Kultur " + i;
            test.Group = "ZDF";
            epg = new EpgInfo();
            epg.ChannelName = test.Name;
            epg.Desc = "Nachrichten";
            epg.Time = "20:15";
            epg.Title = "Nachrichten";
            test.Epg = epg;

            channels.add(test);
        }

        createChannelMap();
    }

    private void createChannelMap() {
        String currentGroup = "";
        List<Channel> channelGroup = new ArrayList<Channel>();

        for (Channel chan : channels) {
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
