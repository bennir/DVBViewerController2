package de.bennir.dvbviewercontroller2.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.service.ChannelService;
import de.bennir.dvbviewercontroller2.ui.StreamActivity;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChannelAdapter extends ArrayAdapter<Channel> {
    private static final String TAG = ChannelAdapter.class.toString();

    static class ChannelViewHolder {
        TextView name;
        TextView epg;
        TextView favid;
        ProgressBar progress;
        ImageView logo;
        Button switchChannel;
        Button stream;
    }

    private List<Channel> channels;
    private Context mContext;
    private DVBHost Host;
    private ChannelService channelService;
    private RestAdapter restAdapter;
    private Activity mActivity;

    public ChannelAdapter(Context context, List<Channel> channels, DVBHost host, Activity activity) {
        super(context, R.layout.list_item_channel, channels);
        this.channels = channels;
        this.mContext = context;
        this.Host = host;
        this.mActivity = activity;

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Host.getUrl() + "/dvb")
                .build();

        channelService = restAdapter.create(ChannelService.class);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
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
            viewHolder.switchChannel = (Button) view.findViewById(R.id.channel_item_switch_channel);
            viewHolder.stream = (Button) view.findViewById(R.id.channel_item_watch_stream);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ChannelViewHolder) view.getTag();
        }

        viewHolder.name.setText(channels.get(position).Name);

        if(channels.get(position).Epg != null) {
            viewHolder.epg.setText(channels.get(position).Epg.Time + " - " + channels.get(position).Epg.Title);
        }
        viewHolder.favid.setText(String.valueOf(channels.get(position).Id));

        viewHolder.switchChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

                if (!Host.Name.equals("localhost")) {
                    String channelId = String.valueOf(getItem(position).Id);
                    Channel channel = new Channel();
                    channel.ChannelId = channelId;

                    channelService.setChannel(channel, new Callback<Channel>() {
                        @Override
                        public void success(Channel dvbCommand, Response response) {
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e(TAG, error.toString());
                        }
                    });
                }
            }
        });

        viewHolder.stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(mActivity, StreamActivity.class);
                mIntent.putExtra(Config.CHANNEL_KEY, getItem(position));
                mIntent.putExtra(Config.DVBHOST_KEY, Host);

                mActivity.startActivityForResult(mIntent, 2);
            }
        });

        /**
         * Duration Progress
         */
        if (!Host.Name.equals("localhost")) {
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

        if (!Host.Name.equals("localhost")) {
            String url = "";

            try {
                url = "http://" + Host.Ip + ":" + Host.Port + "/dvb" +
                        "/Logo/" + URLEncoder.encode(channels.get(position).Name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            Picasso.with(mContext)
                    .load(url)
                    .into(viewHolder.logo);
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.dvbviewer_controller)
                    .into(viewHolder.logo);
        }

        return view;
    }


}