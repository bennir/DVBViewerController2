package de.bennir;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import org.junit.After;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import de.bennir.dvbviewercontroller2.ui.activity.ControllerActivity;

public class FragmentTestCase<T> {

    private static final String FRAGMENT_TAG = "fragment";

    private ActivityController controller;
    private Activity activity;
    private T fragment;

    public void startFragment(T fragment) {
        this.fragment = fragment;
        controller = Robolectric.buildActivity(ControllerActivity.class);
        activity = (Activity) controller.create().start().visible().get();

        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add((Fragment) fragment, FRAGMENT_TAG).commit();
    }

    @After
    public void destroyFragment() {
        if (fragment != null) {
            FragmentManager manager = activity.getFragmentManager();
            manager.beginTransaction().remove((Fragment) fragment).commit();
            fragment = null;
            activity = null;
        }
    }

    public void pauseAndResumeFragment() {
        controller.pause().resume();
    }

    public T recreateFragment() {
        activity.recreate();
        fragment = (T) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        return fragment;
    }
}
