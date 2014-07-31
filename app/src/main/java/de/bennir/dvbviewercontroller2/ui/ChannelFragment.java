package de.bennir.dvbviewercontroller2.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.devspark.progressfragment.ProgressListFragment;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class ChannelFragment extends ProgressListFragment
        implements DVBService.ChannelSuccessCallback {
    private static final String TAG = ChannelFragment.class.toString();

    private ArrayAdapter<String> mAdapter;
    private Context mContext;
    private ArrayList<String> groups = new ArrayList<String>();

    private DVBService mService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mService = DVBService.getInstance(getActivity().getApplicationContext());
        mService.addChannelCallback(this);

        obtainData();
    }

    private void obtainData() {
        setListShown(false);
        groups.clear();

        mService.getChannels();
    }

    @Override
    public void onChannelSuccess() {
        Log.d(TAG, "onChannelSuccess: " + mService.channels.size());

        for (Channel chan : mService.channels) {
            if (!groups.contains(chan.Group)) {
                groups.add(chan.Group);
            }
        }

        mAdapter = new ArrayAdapter<String>(mContext, R.layout.list_item_simple, groups);
        setListAdapter(mAdapter);

        setListShown(true);
    }

    @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                obtainData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
