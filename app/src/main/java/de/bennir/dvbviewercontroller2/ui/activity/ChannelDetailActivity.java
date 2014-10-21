package de.bennir.dvbviewercontroller2.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import de.bennir.dvbviewercontroller2.ui.fragment.ChannelDetailFragment;

public class ChannelDetailActivity extends Activity {
    private static final String TAG = ChannelDetailActivity.class.toString();


    private DVBHost Host;
    private Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);
        channel = getIntent().getParcelableExtra(Config.CHANNEL_KEY);
        getActionBar().setTitle(channel.Name);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Config.DVBHOST_KEY, Host);
        bundle.putParcelable(Config.CHANNEL_KEY, channel);

        Fragment fragment = new ChannelDetailFragment();
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Config.DVBHOST_KEY, Host);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
