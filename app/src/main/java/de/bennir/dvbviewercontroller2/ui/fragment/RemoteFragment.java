package de.bennir.dvbviewercontroller2.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.bennir.dvbviewercontroller2.Config;
import de.bennir.dvbviewercontroller2.R;
import de.bennir.dvbviewercontroller2.model.DVBCommand;
import de.bennir.dvbviewercontroller2.ui.activity.ControllerActivity;

public class RemoteFragment extends Fragment {
    private static final String TAG = RemoteFragment.class.toString();
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_remote, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        ImageView remote = (ImageView) getActivity().findViewById(
                R.id.remote);

        remote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int coords[] = new int[2];
                    v.getLocationOnScreen(coords);

                    int x = (int) event.getRawX() - coords[0];
                    int y = (int) event.getRawY() - coords[1];

                    if (x < 0 || y < 0) {
                        return false;
                    }

                    ImageView img = (ImageView) getActivity().findViewById(
                            R.id.remote_touchmap);
                    Bitmap bitmap = ((BitmapDrawable) img.getDrawable())
                            .getBitmap();

                    double scaleWidthRatio = (double) img.getWidth() / (double) bitmap.getWidth();
                    double scaleHeightRatio = (double) img.getHeight() / (double) bitmap.getHeight();

                    int scaleX = (int) (x / scaleWidthRatio);
                    int scaleY = (int) (y / scaleHeightRatio);

                    if (scaleX > bitmap.getWidth() || scaleY > bitmap.getHeight()) {
                        return false;
                    }

                    int pixel = bitmap.getPixel(scaleX, scaleY);

                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);

                    /**
                     * Button Events
                     */

                    int command = -1;

                    // Chan+
                    if (red == 119 && blue == 119 && green == 119) {
                        command = Config.UP;
                    }
                    // Chan-
                    if (red == 0 && blue == 0 && green == 0) {
                        command = Config.DOWN;
                    }
                    // Vol+
                    if (red == 49 && blue == 49 && green == 49) {
                        command = Config.RIGHT;
                    }
                    // Vol-
                    if (red == 204 && blue == 204 && green == 204) {
                        command = Config.LEFT;
                    }
                    // Menu
                    if (red == 0 && blue == 255 && green == 255) {
                        command = Config.MENU;
                    }
                    // Ok
                    if (red == 255 && blue == 255 && green == 0) {
                        command = Config.OK;
                    }
                    // Back
                    if (red == 255 && blue == 0 && green == 168) {
                        command = Config.BACK;
                    }
                    // Red
                    if (red == 255 && blue == 0 && green == 0) {
                        command = Config.RED;
                    }
                    // Yellow
                    if (red == 255 && blue == 0 && green == 255) {
                        command = Config.YELLOW;
                    }
                    // Green
                    if (red == 0 && blue == 0 && green == 255) {
                        command = Config.GREEN;
                    }
                    // Blue
                    if (red == 0 && blue == 255 && green == 0) {
                        command = Config.BLUE;
                    }

                    if (command != -1) {
                        ((Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);

                        DVBCommand cmd = new DVBCommand(command);

                        ((ControllerActivity) getActivity()).sendCommand(cmd);
                    }
                }

                return true;
            }
        });

    }
}
