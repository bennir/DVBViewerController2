package de.bennir.dvbviewercontroller2.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.devspark.progressfragment.ProgressListFragment;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.TextViewAdapter;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;

public class ChannelGroupFragment extends ProgressListFragment
        implements ControllerActivity.ChannelSuccessCallback {
    private static final String TAG = ChannelGroupFragment.class.toString();

    private ArrayAdapter<String> mAdapter;
    private Context mContext;

    private ArrayList<Channel> mChannels = new ArrayList<Channel>();
    private ArrayList<String> channelGroups = new ArrayList<String>();
    private DVBHost Host;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        Host = getArguments().getParcelable(Config.DVBHOST_KEY);

        mChannels = getArguments().getParcelableArrayList(Config.CHANNEL_LIST_KEY);
        channelGroups = getArguments().getStringArrayList(Config.CHANNEL_GROUP_LIST_KEY);

        ((ControllerActivity) getActivity()).addChannelCallback(this);

        if (channelGroups.isEmpty()) {
            obtainData();
        } else {
            mAdapter = new TextViewAdapter(mContext, R.layout.list_item_simple, channelGroups);
            setListAdapter(mAdapter);

            setListShown(true);
        }

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment fragment = new ChannelFragment();

                Bundle bundle = new Bundle();
                bundle.putString(Config.GROUP_KEY, channelGroups.get(i));
                bundle.putParcelable(Config.DVBHOST_KEY, Host);
                bundle.putParcelableArrayList(Config.CHANNEL_LIST_KEY, mChannels);
                bundle.putStringArrayList(Config.CHANNEL_GROUP_LIST_KEY, channelGroups);
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                                R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                                R.animator.card_flip_left_in, R.animator.card_flip_left_out
                        )
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void obtainData() {
        setListShown(false);
        channelGroups.clear();

        ((ControllerActivity) getActivity()).getChannels();
    }

    @Override
    public void onChannelSuccess() {
        mAdapter = new TextViewAdapter(mContext, R.layout.list_item_simple, channelGroups);
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
            case R.id.menu_voice_action:
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, "1");
                try {
                    startActivityForResult(i, 1);
                } catch (Exception e) {

                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) {
            ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Channel found = null;
            for(String word : words) {
                Log.d(TAG, "Search: " + word);
                found = findChannelByName(word, mChannels);
            }

            if(found != null) {
                Log.d(TAG, "Chan: " + found.toString());
            }
        }
    }

    private Channel findChannelByName(String search, ArrayList<Channel> channels) {
        Log.d(TAG, "findChannelByName() " + channels.size());
        Channel result = null;

        for(Channel chan : channels) {
            String chanName = chan.Name.toLowerCase();
            Log.d(TAG, "findChannelByName: " + chanName + " == " + search + " ?");
            if(chanName.indexOf(search.toLowerCase()) != -1) {
                result = chan;
                continue;
            }
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        ControllerActivity act = (ControllerActivity) getActivity();
        act.mTitle = getString(R.string.channels);

        ((ControllerActivity) getActivity()).addChannelCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        ((ControllerActivity) getActivity()).removeChannelCallback(this);
    }

}
