package org.droidplanner.android.cameraStreaming;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.droidplanner.android.R;
import org.opencv.android.OpenCVLoader;

/**
 * Created by ahcorde on 27/1/15.
 */
public class CameraStreaming_fragment extends Fragment {

    public CameraStreaming_fragment() {
    }

    ThreadCamera thread_camera;
    ImageView image = null;
    UIUpdater mUIUpdater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camerastreaming, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        image = (ImageView) rootView.findViewById(R.id.id_cameraStreaming);

        thread_camera = new ThreadCamera();
        thread_camera.start();
        mUIUpdater = new UIUpdater(new Runnable() {
            @Override
            public void run() {
                image.setImageBitmap(thread_camera.getImage());
            }
        });
        mUIUpdater.startUpdates();

        return rootView;
    }
}
