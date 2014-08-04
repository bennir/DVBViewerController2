package de.bennir.dvbviewercontroller2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class ChannelAdapter extends ArrayAdapter<Channel> {
    private static final String TAG = ChannelAdapter.class.toString();

    static class ChannelViewHolder {
        TextView name;
        TextView epg;
        TextView favid;
        ProgressBar progress;
        ImageView logo;
    }

    private List<Channel> channels;
    private Context mContext;
    private DVBService mService;

    public ChannelAdapter(Context context, List<Channel> channels, DVBService service) {
        super(context, R.layout.list_item_channel, channels);
        this.channels = channels;
        this.mContext = context;
        this.mService = service;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ChannelViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_channel, parent, false);

            viewHolder = new ChannelViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.channel_item_name);
            viewHolder.epg = (TextView) view.findViewById(R.id.channel_item_current_epg);
            viewHolder.favid = (TextView) view.findViewById(R.id.channel_item_favid);
            viewHolder.progress = (ProgressBar) view.findViewById(R.id.channel_item_progress);
            viewHolder.logo = (ImageView) view.findViewById(R.id.channel_item_logo);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ChannelViewHolder) view.getTag();
        }

        viewHolder.name.setText(channels.get(position).Name);

        if(channels.get(position).Epg != null) {
            viewHolder.epg.setText(channels.get(position).Epg.Time + " - " + channels.get(position).Epg.Title);
        }
        viewHolder.favid.setText(String.valueOf(channels.get(position).Id));

        /**
         * Duration Progress
         */
        if (!Config.DVB_HOST.equals("localhost")) {
            if(channels.get(position).Epg != null) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String curTime = format.format(new Date());

                String startTime = channels.get(position).Epg.Time;
                String duration = channels.get(position).Epg.Duration;

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

                viewHolder.progress.setProgress(Double.valueOf((elapsed / durMinutes * 100)).intValue());
            } else {
                viewHolder.progress.setVisibility(View.GONE);
            }
        } else {
            viewHolder.progress.setProgress(Double.valueOf(new Random().nextInt(100)).intValue());
        }

        if (!Config.DVB_HOST.equals("localhost")) {
            String url = "";

            try {
                url = "http://" + Config.DVB_IP + ":" + Config.DVB_PORT + "/dvb" +
                        "/Logo/" + URLEncoder.encode(channels.get(position).Name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            Picasso.with(mContext)
                    .load(url)
                    .into(viewHolder.logo);
        }

        return view;
    }


}