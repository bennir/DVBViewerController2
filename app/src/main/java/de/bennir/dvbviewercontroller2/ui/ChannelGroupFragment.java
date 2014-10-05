package de.bennir.dvbviewercontroller2.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
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
import java.util.List;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.TextViewAdapter;
import de.bennir.dvbviewercontroller2.interfaces.RefreshChannels;
import de.bennir.dvbviewercontroller2.interfaces.RequestHost;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.service.ChannelService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChannelGroupFragment extends ProgressListFragment
        implements ControllerActivity.ChannelSuccessCallback {
    private static final String TAG = ChannelGroupFragment.class.toString();

    private ArrayAdapter<String> mAdapter;
    private Context mContext;

    private ArrayList<String> channelGroups = new ArrayList<String>();
    private DVBHost Host;

    private ChannelService channelService;
    private RestAdapter restAdapter;

    private RefreshChannels mRefreshCallback;
    private RequestHost mRequestHostCallback;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mRefreshCallback = (RefreshChannels) activity;
            mRequestHostCallback = (RequestHost) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onRefreshChannelListener, onRequestHostListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        Host = mRequestHostCallback.onRequestHostListener();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Host.getUrl() + "/dvb")
                .build();

        channelService = restAdapter.create(ChannelService.class);

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
        Log.d(TAG, "obtainData");
        setListShown(false);
        channelGroups.clear();

        mRefreshCallback.onRefreshChannelListener();
    }

    public void setChannelGroups(ArrayList<String> groups) {
        Log.d(TAG, "setChannelGroups");
        channelGroups = groups;
    }

    @Override
    public void onChannelSuccess() {
        Log.d(TAG, "onChannelSuccess");
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

        if (requestCode == 1) {
            ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Channel result = null;
            for (String word : words) {
                Channel found = findChannelByName(word, ((ControllerActivity) getActivity()).mChannels);
                if (found != null) {
                    result = found;
                    continue;
                }
            }

            if (result != null) {
                Log.d(TAG, "Switching to: " + result.Name);

                ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

                if (!Host.Name.equals("localhost")) {

                    channelService.setChannel(result, new Callback<Channel>() {
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
        }
    }

    private Channel findChannelByName(String search, ArrayList<Channel> channels) {
        Channel result = null;

        for (Channel chan : channels) {
            String chanName = chan.Name.toLowerCase();
            if (chanName.indexOf(search.toLowerCase()) != -1) {
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
