package de.bennir.dvbviewercontroller2.model;

public class Channel {
    public int Id;
    public String Name;
    public String Group;
    public String ChannelId;
    public EpgInfo Epg;

    @Override
    public String toString() {
        return Id+";"+Name+";"+Group+";"+ChannelId+";"+Epg+";";
    }
}
