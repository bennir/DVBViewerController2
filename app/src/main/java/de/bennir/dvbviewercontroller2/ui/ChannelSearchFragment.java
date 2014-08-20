package de.bennir.dvbviewercontroller2.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.devspark.progressfragment.ProgressListFragment;

import java.util.ArrayList;
import java.util.Collections;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.adapter.TextViewAdapter;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;

public class ChannelSearchFragment extends ProgressListFragment
        implements ControllerActivity.ChannelSuccessCallback, SearchView.OnQueryTextListener {
    private static final String TAG = ChannelSearchFragment.class.toString();

    private TextViewAdapter mAdapter;
    private Context mContext;
    private ListView mListView;

    private ArrayList<Channel> mChannels = new ArrayList<Channel>();
    private ArrayList<String> mChannelNames = new ArrayList<String>();
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

        ((ControllerActivity) getActivity()).addChannelCallback(this);

        if (mChannels.isEmpty()) {
            obtainData();
        } else {
            mChannelNames.clear();
            for (Channel chan : mChannels) {
                mChannelNames.add(chan.Name);
            }
            Collections.sort(mChannelNames, String.CASE_INSENSITIVE_ORDER);

            mAdapter = new TextViewAdapter(mContext, R.layout.list_item_simple, mChannelNames);
            setListAdapter(mAdapter);

            setListShown(true);
        }

        mListView = getListView();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent mIntent = new Intent(getActivity(), ChannelDetailActivity.class);
                mIntent.putExtra(Config.CHANNEL_KEY, Config.getChannelByName(mAdapter.getItem(i), mChannels));
                mIntent.putExtra(Config.DVBHOST_KEY, Host);

                startActivityForResult(mIntent, 1);
            }
        });
        mListView.setTextFilterEnabled(true);
    }

    private void obtainData() {
        setListShown(false);
        mChannelNames.clear();

        ((ControllerActivity) getActivity()).getChannels();
    }

    @Override
    public void onChannelSuccess() {
        Log.d(TAG, "onChannelSuccess");
        mChannels = ((ControllerActivity) getActivity()).mChannels;
        mChannelNames.clear();

        for (Channel chan : mChannels) {
            mChannelNames.add(chan.Name);
        }
        Collections.sort(mChannelNames, String.CASE_INSENSITIVE_ORDER);

        mAdapter = new TextViewAdapter(mContext, R.layout.list_item_simple, mChannelNames);
        setListAdapter(mAdapter);

        setListShown(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused)
                    searchItem.collapseActionView();
            }
        });
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

        ControllerActivity act = (ControllerActivity) getActivity();

        ((ControllerActivity) getActivity()).addChannelCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        ((ControllerActivity) getActivity()).removeChannelCallback(this);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(TextUtils.isEmpty(s)) {
            mListView.clearTextFilter();
        } else {
            mListView.setFilterText(s);
        }
        return true;
    }
}
