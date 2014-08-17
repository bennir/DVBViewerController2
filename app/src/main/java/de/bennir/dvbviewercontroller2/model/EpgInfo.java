package de.bennir.dvbviewercontroller2.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EpgInfo
        implements Parcelable {
    public static final Creator<EpgInfo> CREATOR = new Creator<EpgInfo>() {
        @Override
        public EpgInfo createFromParcel(Parcel in) {
            return new EpgInfo(in);
        }

        @Override
        public EpgInfo[] newArray(int size) {
            return new EpgInfo[size];
        }
    };

    public String Time;
    public long EndTime;
    public String ChannelId;
    public String Title;
    public String Desc;
    public String Duration;
    public String Date;

    public EpgInfo() {}

    private EpgInfo(Parcel in) {
        Time = in.readString();
        EndTime = in.readLong();
        ChannelId = in.readString();
        Title = in.readString();
        Desc = in.readString();
        Duration = in.readString();
        Date = in.readString();
    }

    @Override
    public String toString() {
        return Time + ";" + EndTime + ";" + ChannelId + ";" + Title + ";" + Desc + ";" + Duration + ";" + Date + ";";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(Time);
        out.writeLong(EndTime);
        out.writeString(ChannelId);
        out.writeString(Title);
        out.writeString(Desc);
        out.writeString(Duration);
        out.writeString(Date);
    }
}
