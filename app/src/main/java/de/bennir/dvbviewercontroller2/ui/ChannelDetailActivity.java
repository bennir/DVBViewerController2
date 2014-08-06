package de.bennir.dvbviewercontroller2.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

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

    private DVBHost Host;
    private Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_listview);

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);
        channel = getIntent().getParcelableExtra(Config.CHANNEL_KEY);

        mListView = getListView();

        ArrayList<String> values = new ArrayList<String>();
        for(int i = 0; i < 20; i++) {
            values.add(channel.Name + " " + i);
        }

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        mListView.setAdapter(mAdapter);

        View mHeader = getLayoutInflater().from(this).inflate(R.layout.activity_channel_detail, mListView, false);

        mImageView = (ImageView) mHeader.findViewById(R.id.header_imageview);
        mImageView.setImageResource(R.drawable.dvbviewer_controller);

        mListView.addHeaderView(mHeader);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == 0) return;
                if (firstVisibleItem != 0) return;

                mImageView.setTranslationY(-mListView.getChildAt(0).getTop() / 2);
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

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
