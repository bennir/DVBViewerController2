package de.bennir.dvbviewercontroller2.ui;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.Channel;
import de.bennir.dvbviewercontroller2.model.DVBHost;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class StreamActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = StreamActivity.class.toString();

    private DVBHost Host;
    private Channel channel;
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;

        setContentView(R.layout.activity_stream);

        //TODO: get Recording Service IP
        //TODO: remove Action Bar
        //TODO: Immersive Mode

        Host = getIntent().getParcelableExtra(Config.DVBHOST_KEY);
        channel = getIntent().getParcelableExtra(Config.CHANNEL_KEY);
        getActionBar().setTitle(channel.Name);

        mVideoView = (VideoView) findViewById(R.id.video);

        String recIp = "127.0.0.1";
        String url = "http://" + recIp + ":" + Config.REC_SERVICE_LIVE_STREAM_PORT + "/upnp/channelstream/" + channel.Id + ".ts";

        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.requestFocus();
        mVideoView.setVideoURI(Uri.parse(url));
        mVideoView.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mVideoView.stopPlayback();

        Intent intent = new Intent();
        intent.putExtra(Config.DVBHOST_KEY, Host);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }

    @Override
    public void onPrepared(MediaPlayer arg0) {
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, mVideoView.getVideoAspectRatio());
        mVideoView.start();
//        mProgress.setVisibility(View.GONE);
//        mProgressInfo.setText("Buffering...");
    }

    /* (non-Javadoc)
     * @see io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener#onBufferingUpdate(io.vov.vitamio.MediaPlayer, int)
     */
    @Override
    public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
        if (mVideoView.isPlaying() || arg1 == 100) {
//            mProgressInfo.setVisibility(View.GONE);
            return;
        }
//        mProgressInfo.setText("Buffering...  " + arg1 + " %");
        if (!mVideoView.isPlaying() && arg1 > 10) {
//            mProgressInfo.setVisibility(View.GONE);
            Log.i(TAG, "Buffer Size: " + arg1);
            mVideoView.start();
        }

    }
}
