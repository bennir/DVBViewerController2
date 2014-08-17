package de.bennir.dvbviewercontroller2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.model.EpgInfo;

public class EpgInfoAdapter extends ArrayAdapter<EpgInfo> {
    private static final String TAG = EpgInfoAdapter.class.toString();

    static class EpgInfoViewHolder {
        TextView Current;
        TextView Time;
        TextView Description;
        TextView ChannelId;
    }

    private List<EpgInfo> epg;
    private Context mContext;
    private DVBHost Host;

    public EpgInfoAdapter(Context context, List<EpgInfo> epg, DVBHost Host) {
        super(context, R.layout.list_item_channel, epg);
        this.epg = epg;
        this.mContext = context;
        this.Host = Host;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        EpgInfoViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_epg, parent, false);

            viewHolder = new EpgInfoViewHolder();
            viewHolder.Current = (TextView) view.findViewById(R.id.epg_item_current_epg);
            viewHolder.Time = (TextView) view.findViewById(R.id.epg_item_time);
            viewHolder.Description = (TextView) view.findViewById(R.id.epg_item_desc);
            viewHolder.ChannelId = (TextView) view.findViewById(R.id.epg_item_channel_id);

            view.setTag(viewHolder);
        } else {
            viewHolder = (EpgInfoViewHolder) view.getTag();
        }

        try {
            viewHolder.Current.setText(URLDecoder.decode(getItem(position).Title, "UTF-8"));
            viewHolder.Description.setText(URLDecoder.decode(getItem(position).Desc, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String time = getItem(position).Date + " " + getItem(position).Time;
        viewHolder.Time.setText(time);
        viewHolder.ChannelId.setText(getItem(position).ChannelId);

        return view;
    }


}