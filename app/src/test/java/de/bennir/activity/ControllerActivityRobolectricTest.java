package de.bennir.activity;

import android.app.Activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.bennir.dvbviewercontroller2.ControllerActivity;

import static org.junit.Assert.assertTrue;

@Config(manifest = "./src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class ControllerActivityRobolectricTest {

    @Config(emulateSdk = 18)
    @Test
    public void testSomething() throws Exception {
        Activity activity = Robolectric.buildActivity(ControllerActivity.class).create().get();
        assertTrue(activity != null);
    }
}
