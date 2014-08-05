package de.bennir.dvbviewercontroller2;

import android.content.Context;
import android.util.Log;

public class Config {
    private static final String TAG = Config.class.toString();

    // DNS-SD Service Type
    public static final String SERVICE_TYPE = "_dvbctrl._tcp.";

    // Keys
    public static final String DVBHOST_KEY = "dvb_host";
    public static final String DVBIP_KEY = "dvb_ip";
    public static final String DVBPORT_KEY = "dvb_port";
    public static final String CHANNEL_KEY = "channel_name";

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

    public String Host = "";
    private String Ip = "";
    private String Port = "";

    private Context mContext;

    private static Config _instance = null;

    private Config(Context context) {
        Log.d(TAG, "Config()");
        this.mContext = context;
    }

    public static Config getInstance(Context mContext) {
        Log.d(TAG, "getInstance()");
        if(_instance == null) {
            _instance = new Config(mContext.getApplicationContext());
        }

        return _instance;
    }

    public String getHost() {
        return Host;
    }

    public void setHost(String host) {
        Host = host;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public String getPort() {
        return Port;
    }

    public void setPort(String port) {
        Port = port;
    }
}
