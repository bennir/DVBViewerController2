package de.bennir.dvbviewercontroller2.ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.R;

public class ChannelDetailActivity extends ListActivity {
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);

        mListView = getListView();
        mImageView = (ImageView) findViewById(R.id.header_imageview);
        mImageView.setImageResource(R.drawable.dvbviewer_controller);

        ArrayList<String> values = new ArrayList<String>();
        for(int i = 0; i < 20; i++) {
            values.add("String " + i);
        }

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        mListView.setAdapter(mAdapter);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) { }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(visibleItemCount == 0) return;
                if(firstVisibleItem != 0) return;


            }
        });
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
        return super.onOptionsItemSelected(item);
    }
}
