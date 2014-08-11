package de.bennir.dvbviewercontroller2.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

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

public class ChannelDetailActivity extends ListActivity {
    private static final String TAG = ChannelDetailActivity.class.toString();

    private ListView mListView;
    private EpgInfoAdapter mAdapter;
    private ImageView mImageView;
    private Drawable mActionBarBackgroundDrawable;
    private List<EpgInfo> epg = new ArrayList<EpgInfo>();


    private RestAdapter restAdapter;
    private EpgService epgService;

    private DVBHost Host;
    private Channel channel;
    private Callback mPaletteCallback = new Callback() {
        @Override
        public void onSuccess() {
            Palette palette = Palette.generate(drawableToBitmap(mImageView.getDrawable()));

            PaletteItem item = palette.getVibrantColor();
            if (item == null) item = palette.getDarkVibrantColor();
            if (item == null) item = palette.getDarkMutedColor();
            if (item != null) {
                mActionBarBackgroundDrawable = new ColorDrawable(item.getRgb());
                getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
            }
        }

        @Override
        public void onError() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);
        channel = getIntent().getParcelableExtra(Config.CHANNEL_KEY);
        mListView = getListView();
        final View mHeader = getLayoutInflater().from(this).inflate(R.layout.list_header_epg, mListView, false);
        mImageView = (ImageView) mHeader.findViewById(R.id.header_imageview);

        setupHeader();

        getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        getActionBar().setTitle(channel.Name);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://" + Host.Ip + ":" + Host.Port + "/dvb")
                .build();
        epgService = restAdapter.create(EpgService.class);

        if (epg.isEmpty()) {
            obtainData();
        } else {
            mAdapter = new EpgInfoAdapter(getApplicationContext(), epg, Host);
        }

        mListView.addHeaderView(mHeader);

        mListView.setOnScrollListener(new EndlessScrollListener(mImageView, mActionBarBackgroundDrawable, mHeader) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // TODO: load more
            }
        });
    }

    private void setupHeader() {
        mActionBarBackgroundDrawable = new ColorDrawable(getResources().getColor(R.color.theme_default_primary));
        mActionBarBackgroundDrawable.setAlpha(0);

        if (!Host.Name.equals("localhost")) {
            String url = "";

            try {
                url = "http://" + Host.Ip + ":" + Host.Port + "/dvb" +
                        "/Logo/" + URLEncoder.encode(channel.Name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Picasso.with(getApplicationContext())
                    .load(url)
                    .into(mImageView, mPaletteCallback);
        } else {
            Picasso.with(getApplicationContext())
                    .load(R.drawable.dvbviewer_controller)
                    .into(mImageView, mPaletteCallback);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Config.DVBHOST_KEY, Host);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
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
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void obtainData() {
        epg.clear();

        if (!Host.Name.equals("localhost")) {
            epgService.getEpg(channel.ChannelId, "Current", new retrofit.Callback<ArrayList<EpgInfo>>() {
                @Override
                public void success(ArrayList<EpgInfo> epgInfo, Response response) {
                    epg = epgInfo;

                    mAdapter = new EpgInfoAdapter(getApplicationContext(), epg, Host);
                    mListView.setAdapter(mAdapter);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else {
            epg = Config.createDemoEpg();

            mAdapter = new EpgInfoAdapter(getApplicationContext(), epg, Host);
            mListView.setAdapter(mAdapter);
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
}
