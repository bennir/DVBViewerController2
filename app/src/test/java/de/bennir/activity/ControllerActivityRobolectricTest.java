package de.bennir.activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import de.bennir.dvbviewercontroller2.ui.activity.ControllerActivity;

import static org.junit.Assert.assertTrue;

@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ControllerActivityRobolectricTest {
    private ActivityController controller;
    private ControllerActivity activity;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(ControllerActivity.class);
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    private void create() {
        activity = (ControllerActivity) controller.create().start().visible().get();
    }

    @Test
    public void testActivity() throws Exception {
        create();
        assertTrue(activity != null);
    }

}
