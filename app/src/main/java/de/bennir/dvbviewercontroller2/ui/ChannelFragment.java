package de.bennir.dvbviewercontroller2.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.service.DVBService;

public class ChannelFragment extends Fragment {
    private static final String TAG = ChannelFragment.class.toString();

    private ArrayAdapter<String> mAdapter;
    private Context mContext;

    private DVBService mService;

    private String currentChan = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listview, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);


        currentChan = getArguments().getString(Config.CHANNEL_KEY);

        ControllerActivity act = (ControllerActivity) getActivity();
        act.mTitle = currentChan;
        getActivity().getActionBar().setTitle(currentChan);
    }
}
