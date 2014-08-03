package de.bennir.dvbviewercontroller2.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressListFragment;

import java.util.ResourceBundle;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class ChannelGroupFragment extends ProgressListFragment
        implements DVBService.ChannelSuccessCallback {
    private static final String TAG = ChannelGroupFragment.class.toString();

    private ArrayAdapter<String> mAdapter;
    private Context mContext;

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

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment fragment = new ChannelFragment();

                Bundle data = new Bundle();
                data.putString(Config.CHANNEL_KEY, mService.channelGroups.get(i));
                fragment.setArguments(data);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void obtainData() {
        setListShown(false);
        mService.channelGroups.clear();

        mService.getChannels();
    }

    @Override
    public void onChannelSuccess() {
        Log.d(TAG, "onChannelSuccess: " + mService.channels.size());

        mAdapter = new ArrayAdapter<String>(mContext, R.layout.list_item_simple, mService.channelGroups);
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

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");
        ControllerActivity act = (ControllerActivity) getActivity();
        act.mTitle = getString(R.string.channels);


    }
}
