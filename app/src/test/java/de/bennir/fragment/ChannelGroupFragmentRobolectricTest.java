package de.bennir.fragment;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.bennir.FragmentTestCase;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.ui.fragment.ChannelGroupFragment;

import static org.junit.Assert.assertEquals;

@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ChannelGroupFragmentRobolectricTest extends FragmentTestCase<ChannelGroupFragment> {
    private ChannelGroupFragment fragment;

    @Before
    public void setUp() {
        fragment = new ChannelGroupFragment();
    }

    @Test
    public void createFragment() {
        startFragment(fragment);

        TextView text = (TextView) fragment.getActivity().findViewById(R.id.section_label2);

        assertEquals(text.getText(), "Asdf");
    }


}
