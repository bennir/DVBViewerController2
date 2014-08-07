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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;

public class ChannelDetailActivity extends ListActivity {
    private static final String TAG = ChannelDetailActivity.class.toString();

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ImageView mImageView;
    private Drawable mActionBarBackgroundDrawable;

    private DVBHost Host;
    private Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);
        channel = getIntent().getParcelableExtra(Config.CHANNEL_KEY);

        final View mHeader = getLayoutInflater().from(this).inflate(R.layout.list_header_epg, mListView, false);
        mImageView = (ImageView) mHeader.findViewById(R.id.header_imageview);
        mImageView.setImageResource(R.drawable.dvbviewer_controller);

        Palette palette = Palette.generate(drawableToBitmap(mImageView.getDrawable()));

//        mActionBarBackgroundDrawable = new ColorDrawable(getResources().getColor(R.color.theme_default_primary));
        PaletteItem item = palette.getVibrantColor();
        if (item == null) item = palette.getDarkVibrantColor();
        mActionBarBackgroundDrawable = new ColorDrawable(item.getRgb());
        mActionBarBackgroundDrawable.setAlpha(0);

        getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        getActionBar().setTitle(channel.Name);

        mListView = getListView();

        ArrayList<String> values = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            values.add(channel.Name + " " + i);
        }

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        mListView.setAdapter(mAdapter);

        mListView.addHeaderView(mHeader);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == 0) return;
                if (firstVisibleItem != 0) {
                    mActionBarBackgroundDrawable.setAlpha(255);
                    return;
                }

                mImageView.setTranslationY(-mListView.getChildAt(0).getTop() / 2);

                int top = Math.abs(mHeader.getTop());
                int headerViewHeight = mHeader.getMeasuredHeight();

                float ratio = (float) Math.min(Math.max(0, top), headerViewHeight) / headerViewHeight;
                int newAlpha = (int) (ratio * 255);

                mActionBarBackgroundDrawable.setAlpha(newAlpha);
            }
        });
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
        getMenuInflater().inflate(R.menu.channel_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
