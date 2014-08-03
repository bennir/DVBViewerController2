package de.bennir.dvbviewercontroller2.model;

public class EpgInfo {
    public String Time;
    public String ChannelName;
    public String Title;
    public String Desc;
    public String Duration;

    @Override
    public String toString() {
        return Time + ";" + ChannelName + ";" + Title + ";" + Desc + ";" + Duration + ";";
    }
}
