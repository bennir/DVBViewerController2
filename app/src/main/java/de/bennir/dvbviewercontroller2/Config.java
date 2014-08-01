package de.bennir.dvbviewercontroller2;

public class Config {
    // DNS-SD Service Type
    public static final String SERVICE_TYPE = "_dvbctrl._tcp.";

    // Keys
    public static final String DVBHOST_KEY = "dvb_host";
    public static final String DVBIP_KEY = "dvb_ip";
    public static final String DVBPORT_KEY = "dvb_port";
    public static final String CHANNEL_KEY = "channel_name";

    public static String DVB_HOST = "";
    public static String DVB_IP = "";
    public static String DVB_PORT = "";

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
}
