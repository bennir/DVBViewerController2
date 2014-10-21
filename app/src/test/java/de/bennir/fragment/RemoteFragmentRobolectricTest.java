package de.bennir.fragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.bennir.FragmentTestCase;
import de.bennir.dvbviewercontroller2.ui.fragment.RemoteFragment;

import static org.junit.Assert.assertEquals;

@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RemoteFragmentRobolectricTest extends FragmentTestCase<RemoteFragment> {
    private RemoteFragment fragment;

    @Before
    public void setUp() {
        fragment = new RemoteFragment();
    }

    @Test
    public void createFragment() {
        startFragment(fragment);

//        TextView text = (TextView) fragment.getActivity().findViewById(R.id.section_label);

//        assertEquals(text.getText(), "Test");
    }


}
