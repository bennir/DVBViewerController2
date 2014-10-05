package de.bennir.dvbviewercontroller2.ui;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.devspark.progressfragment.ProgressListFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.EpgInfoAdapter;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.model.EpgInfo;
import de.bennir.dvbviewercontroller2.service.EpgService;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChannelDetailFragment extends ProgressListFragment {
    private static final String TAG = ChannelDetailActivity.class.toString();

    private Context mContext;
    private ListView mListView;
    private EpgInfoAdapter mAdapter;
    private ImageView mImageView;
    private Drawable mActionBarBackgroundDrawable;
    private List<EpgInfo> epg = new ArrayList<EpgInfo>();
    private ActionBar mActionBar;


    private RestAdapter restAdapter;
    private EpgService epgService;

    private DVBHost Host;
    private Channel channel;

    private EndlessScrollListener mScrollListener;

    private Callback mPaletteCallback = new Callback() {
        @Override
        public void onSuccess() {
            Palette palette = Palette.generate(drawableToBitmap(mImageView.getDrawable()));

            PaletteItem item = palette.getVibrantColor();
            if (item == null) item = palette.getDarkVibrantColor();
            if (item == null) item = palette.getDarkMutedColor();
            if (item != null) {
                mActionBarBackgroundDrawable = new ColorDrawable(item.getRgb());
                mActionBarBackgroundDrawable.setAlpha(0);
                mActionBar.setBackgroundDrawable(mActionBarBackgroundDrawable);
                mScrollListener.setActionBarBackgroundDrawable(mActionBarBackgroundDrawable);
            }
        }

        @Override
        public void onError() {

        }
    };

    private Runnable mDemoEpgRunnable = new Runnable() {
        @Override
        public void run() {
            epg = Config.createDemoEpg();

            mAdapter = new EpgInfoAdapter(mContext, epg, Host);
            setListAdapter(mAdapter);
            setListShown(true);

            setupHeader();
        }
    };
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActionBar = getActivity().getActionBar();

        mActionBarBackgroundDrawable = new ColorDrawable(getResources().getColor(R.color.theme_default_primary));
        mActionBarBackgroundDrawable.setAlpha(0);
        mActionBar.setBackgroundDrawable(mActionBarBackgroundDrawable);

        setListShown(false);

        mContext = getActivity().getApplicationContext();

        Host = getArguments().getParcelable(Config.DVBHOST_KEY);
        channel = getArguments().getParcelable(Config.CHANNEL_KEY);
        mActionBar.setTitle(channel.Name);

        mListView = getListView();
        mListView.setCacheColorHint(getResources().getColor(android.R.color.transparent));
        mListView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mListView.setDividerHeight(0);
        mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mListView.setSelector(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        final View mHeader = getActivity().getLayoutInflater().from(mContext).inflate(R.layout.list_header_epg, mListView, false);
        mImageView = (ImageView) mHeader.findViewById(R.id.header_imageview);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Host.getUrl() + "/dvb")
                .build();
        epgService = restAdapter.create(EpgService.class);

        if (epg.isEmpty()) {
            obtainData();
        } else {
            mAdapter = new EpgInfoAdapter(mContext, epg, Host);
            setListAdapter(mAdapter);
            setListShown(true);

            setupHeader();
        }

        mListView.addHeaderView(mHeader);

        mScrollListener = new EndlessScrollListener(mImageView, mActionBarBackgroundDrawable, mHeader) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                EpgInfo lastitem = epg.get(epg.size() - 1);

                obtainData(lastitem.EndTime);
            }
        };
        mListView.setOnScrollListener(mScrollListener);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) return;

                Intent mIntent = new Intent(getActivity(), EpgDescriptionActivity.class);
                mIntent.putExtra(Config.EPG_KEY, mAdapter.getItem(i-1));

                startActivity(mIntent);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            obtainData();

            return true;
        }

        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void obtainData(long endTime) {
        if (!Host.Name.equals("localhost")) {
            epgService.getEpg(channel.ChannelId, String.valueOf(endTime), new retrofit.Callback<ArrayList<EpgInfo>>() {
                @Override
                public void success(ArrayList<EpgInfo> epgInfo, Response response) {
                    mAdapter.addAll(epgInfo);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    private void obtainData() {
        setListShown(false);
        epg.clear();

        if (!Host.Name.equals("localhost")) {
            epgService.getEpg(channel.ChannelId, "Current", new retrofit.Callback<ArrayList<EpgInfo>>() {
                @Override
                public void success(ArrayList<EpgInfo> epgInfo, Response response) {
                    epg = epgInfo;

                    mAdapter = new EpgInfoAdapter(mContext, epg, Host);
                    setListAdapter(mAdapter);
                    setListShown(true);

                    setupHeader();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else {
            mHandler = new Handler();
            mHandler.postDelayed(mDemoEpgRunnable, 3000);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void setupHeader() {
        if (!Host.Name.equals("localhost")) {
            String url = "";

            try {
                url = "http://" + Host.Ip + ":" + Host.Port + "/dvb" +
                        "/Logo/" + URLEncoder.encode(channel.Name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Picasso.with(mContext)
                    .load(url)
                    .into(mImageView, mPaletteCallback);
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.dvbviewer_controller)
                    .into(mImageView, mPaletteCallback);
        }
    }
}
