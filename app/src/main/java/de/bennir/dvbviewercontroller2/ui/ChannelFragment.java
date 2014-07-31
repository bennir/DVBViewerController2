package de.bennir.dvbviewercontroller2.ui;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.bennir.dvbviewercontroller2.R;

public class ChannelFragment extends ListFragment {
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listview, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mListView = getListView();

        String[] values = new String[] { "ARD", "ZDF", "Private", "Sky HD Buli", "Sky HD Sport", "Sky HD"};

        ArrayList<String> list = new ArrayList<String>();
        for(String val : values) {
            list.add(val);
        }

        mAdapter = new ArrayAdapter<String>(mContext, R.layout.list_item_simple, list);
        mListView.setAdapter(mAdapter);

    }
}
