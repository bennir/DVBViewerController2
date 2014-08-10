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
        TextView Duration;
        TextView Description;
        ProgressBar Progress;
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
            viewHolder.Duration = (TextView) view.findViewById(R.id.epg_item_duration);
            viewHolder.Description = (TextView) view.findViewById(R.id.epg_item_desc);
            viewHolder.Progress = (ProgressBar) view.findViewById(R.id.epg_item_progress);
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
        String time = getItem(position).Time + "-" + getItem(position).EndTime;
        viewHolder.Time.setText(time);
        viewHolder.Duration.setText(getItem(position).Duration);
        viewHolder.ChannelId.setText(getItem(position).ChannelId);

        if (position == 0) viewHolder.Progress.setVisibility(View.VISIBLE);

        /**
         * Duration Progress
         */
        if (!Host.Name.equals("localhost")) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            String curTime = format.format(new Date());

            String startTime = getItem(position).Time;
            String duration = getItem(position).Duration;

            Date curDate;
            Date startDate;
            Date durDate = new Date();
            long diff = 0;

            if (!startTime.equals("")) {
                try {
                    curDate = format.parse(curTime);
                    startDate = format.parse(startTime);
                    durDate = format.parse(duration);

                    diff = curDate.getTime() - startDate.getTime();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }

            double elapsed = (diff / 1000 / 60);
            long durMinutes = (durDate.getHours() * 60 + durDate.getMinutes());

            viewHolder.Progress.setProgress(Double.valueOf((elapsed / durMinutes * 100)).intValue());
        } else {
            viewHolder.Progress.setProgress(Double.valueOf(new Random().nextInt(100)).intValue());
        }


        return view;
    }


}