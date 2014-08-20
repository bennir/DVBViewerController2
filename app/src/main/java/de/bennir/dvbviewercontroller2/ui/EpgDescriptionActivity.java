package de.bennir.dvbviewercontroller2.ui;

import android.app.Activity;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.EpgInfo;

public class EpgDescriptionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epg_description);

        EpgInfo info = getIntent().getParcelableExtra(Config.EPG_KEY);

        String desc = "";
        try {
            desc = URLDecoder.decode(info.Desc, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ((TextViewEx) findViewById(android.R.id.text1)).setText(desc, true);
    }
}
