package com.androidjo.queryrobot.vision;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import com.androidjo.queryrobot.Spine;
import com.androidjo.queryrobot.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class RoboVision implements Runnable {

    private static final String TAG = "RoboVision";
    private static final int RES_X = 320;
    private static final int RES_Y = 240;
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private CameraSource mCameraSource = null;
    private Spine spine;
    private Activity mActivity;
    private static RoboVision mInstance = new RoboVision();

    private RoboVision() {
        spine = Spine.getInstance();
    }

    public static RoboVision getInstance(){
        return mInstance;
    }

    public void initCamera(Activity activity) {
        mActivity = activity;
    }

    public void stopCamera() {
        Util.log("stop camera");
        if (mCameraSource != null) {
            try {
                mCameraSource.release();
            } catch (NullPointerException ignored) {  }
            mCameraSource = null;
        }
    }

    private void createCameraSource(Activity activity) {
        Context context = activity.getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
        if (!detector.isOperational()) {
            Util.log("Face detector dependencies are not yet available.");
        }
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(RES_X, RES_Y)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    private void startCameraSource(Activity activity) {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity.getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS);
            dlg.show();
        }
        if (mCameraSource != null) {
            Util.log("startCameraSource");
            try {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestCameraPermission(Activity activity) {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(activity, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = activity;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    @Override
    public void run() {
        if (mActivity!=null) {
            int rc = ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                createCameraSource(mActivity);
                startCameraSource(mActivity);
            } else {
                requestCameraPermission(mActivity);
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker();
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {

        private float lastX;
        private float lastY;

        @Override
        public void onNewItem(int faceId, Face face) {
            setFaceCoord(face);
            Util.log("Face detected at x=" + Float.toString(lastX) + "y=" + Float.toString(lastY));
        }

        private void setFaceCoord(Face face) {
            lastX = face.getPosition().x + face.getWidth() / 2f;
            lastY = face.getPosition().y + face.getHeight() / 2f;
        }

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            setFaceCoord(face);
            Log.d(TAG,"x=" + Float.toString(lastX) + "y=" + Float.toString(lastY));
            turnHead(Math.round(lastY));
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            super.onMissing(detectionResults);
            Util.log("Face missing at x=" + Float.toString(lastX) + "y=" + Float.toString(lastY));
        }

        @Override
        public void onDone() {
            super.onDone();
        }

    }

    private void turnHead(int y) {
        int step = 1;
        int affordable = RES_Y / 15;

        if (y > RES_Y / 2 + affordable) {
            spine.turnHeadVertical(-1*step);
            Log.d(TAG, "DOWN");
        } else if (y < RES_Y / 2 - affordable) {
            Log.d(TAG, "UP");
            spine.turnHeadVertical(step);
        } else Log.d(TAG, "STAY");
    }

}
