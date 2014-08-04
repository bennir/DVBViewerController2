package de.bennir.dvbviewercontroller2.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Channel
        implements Parcelable {
    public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public int Id;
    public String Name;
    public String Group;
    public String ChannelId;
    public EpgInfo Epg;

    public Channel() {}

    private Channel(Parcel in) {
        Id = in.readInt();
        Name = in.readString();
        Group = in.readString();
        ChannelId = in.readString();
        Epg = in.readParcelable(EpgInfo.class.getClassLoader());
    }

    @Override
    public String toString() {
        return Id + ";" + Name + ";" + Group + ";" + ChannelId + ";" + Epg + ";";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeInt(Id);
        out.writeString(Name);
        out.writeString(Group);
        out.writeString(ChannelId);
        out.writeParcelable(Epg, 0);
    }
}
