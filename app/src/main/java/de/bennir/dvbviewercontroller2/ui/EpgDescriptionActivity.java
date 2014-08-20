package de.bennir.dvbviewercontroller2.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.EpgInfo;

public class EpgDescriptionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epg_description);

        EpgInfo info = getIntent().getParcelableExtra(Config.EPG_KEY);
        ((TextView) findViewById(android.R.id.text1)).setText(info.Desc);
    }
}
