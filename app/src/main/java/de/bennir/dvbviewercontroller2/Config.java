package de.bennir.dvbviewercontroller2;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.EpgInfo;

public class Config {
    private static final String TAG = Config.class.toString();

    // DNS-SD Service Type
    public static final String SERVICE_TYPE             = "_dvbctrl._tcp.";

    // Keys
    public static final String DVBHOST_KEY              = "dvb_host";
    public static final String CHANNEL_KEY              = "channel_name";
    public static final String GROUP_KEY                = "channel_group";
    public static final String CHANNEL_LIST_KEY         = "channel_list";
    public static final String CHANNEL_GROUP_LIST_KEY   = "channel_group_list";
    public static final String EPG_KEY                  = "channel_epg";

    // DVB Command Values
    public static int MENU      = 111;
    public static int OK        = 73;
    public static int LEFT      = 2000;
    public static int RIGHT     = 2100;
    public static int VOLUP     = 26;
    public static int VOLDOWN   = 27;
    public static int UP        = 78;
    public static int DOWN      = 79;
    public static int BACK      = 84;
    public static int RED       = 74;
    public static int YELLOW    = 76;
    public static int GREEN     = 75;
    public static int BLUE      = 77;

    // Recording Service Constant Ports
    public static String REC_SERVICE_LIVE_STREAM_PORT   = "7522";
    public static String REC_SERVICE_MEDIA_STREAM_PORT  = "8090";

    public static Channel getChannelByName(String channelName, ArrayList<Channel> channels) {
        Channel ret;
        for(Channel chan : channels) {
            if(chan.Name.equals(channelName)) {
                ret = chan;
                return ret;
            }
        }

        return null;
    }

    // TODO: JSON RAW File
    public static ArrayList<Channel> createDemoChannels() {
        Log.d(TAG, "createDemoChannels()");
        ArrayList<Channel> channels = new ArrayList<Channel>();

        Channel test = new Channel();
        test.Name = "Das Erste HD";
        test.Group = "ARD";
        EpgInfo epg = new EpgInfo();
        epg.ChannelId = test.Name;
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
            epg.ChannelId = test.Name;
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
        epg.ChannelId = test.Name;
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
            epg.ChannelId = test.Name;
            epg.Desc = "Nachrichten";
            epg.Time = "20:15";
            epg.Title = "Nachrichten";
            test.Epg = epg;

            channels.add(test);
        }

        return channels;
    }

    public static List<EpgInfo> createDemoEpg() {
        List<EpgInfo> epg = new ArrayList<EpgInfo>();

        EpgInfo entry = new EpgInfo();
        entry.Title = "Tagesschau";
        entry.Time = "20:00";
        entry.Date = "01.01.";
        entry.Duration = "15";
        entry.ChannelId = "Das Erste HD";
        entry.EndTime = 635438340000000000L;
        entry.Desc = "Die Nachrichten und das Wetter und hier steht ganz viel weiteres Interessantes. Interessant? Naja das ist wohl eine Frage der Definition. Lorem Ipsum bla bla ich hasse Latein, kacksprache ey!";

        for(int i = 0; i < 10; i++) {
            epg.add(entry);
        }

        return epg;
    }
}
