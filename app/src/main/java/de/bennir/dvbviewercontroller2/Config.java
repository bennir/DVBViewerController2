package de.bennir.dvbviewercontroller2;

/**
 * Created by benni on 29.07.14.
 */
public class Config {
    // DNS-SD Service Type
    public static final String SERVICE_TYPE = "_dvbctrl._tcp.";

    public static String DVB_HOST = "example.com";
    public static int DVB_PORT = 8000;

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
