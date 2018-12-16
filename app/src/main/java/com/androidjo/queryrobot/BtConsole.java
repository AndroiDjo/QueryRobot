package com.androidjo.queryrobot;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.androidjo.queryrobot.ui.JoystickView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class BtConsole extends AppCompatActivity {
    private TextView tv;
    private EditText et;
    private BtSingleton bts;
    private Spine spine;

    private static final String TAG = "BtConsole";

    //BT
    private final String DEVICE_ADDRESS = "00:21:13:02:A0:88";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    byte buffer[];
    boolean stopThread;
    boolean deviceConnected = false;
    long prevBtTime = System.nanoTime();

    //Servo
    private int horServDegr = 90;
    private int verServDegr = 90;
    private int horMiddleDegr = 100;
    private int verMiddleDegr = 115;

    //joystick
    private static final int JOY_STR_MIN = 0;
    private static final int JOY_STR_MAX = 100;

    //Camera & Face detector
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private CameraSource mCameraSource = null;
    private FaceDetector detector = null;
    private static final int RES_X = 320;
    private static final int RES_Y = 240;

    //tmp
    private float maxX = 0f;
    private float maxY = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_console);
        tv = (TextView) findViewById(R.id.BTtextView);
        et = (EditText) findViewById(R.id.BTeditText);
        bts = BtSingleton.getInstance();
        bts.setConsoleTV(tv);
        spine = Spine.getInstance();

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                Log.d(TAG, Integer.toString(strength) + ";");

                if (angle > 90 && angle <= 270) {
                    verServDegr = mapRangeToDegree(angle, 91, 270, 99, 0);
                } else if (angle <= 90) {
                    verServDegr = mapRangeToDegree(angle, 0, 90, 0, 50);
                } else if (angle > 270) {
                    verServDegr = mapRangeToDegree(angle, 271, 360, 50, 99);
                }

                if (angle < 180) {
                    horServDegr = mapRangeToDegree(angle, 0, 179, 180, 20);
                } else if (angle >= 180) {
                    horServDegr = mapRangeToDegree(angle, 180, 360, 20, 180);
                }

                if ((horServDegr != horMiddleDegr || verServDegr != verMiddleDegr) && (System.nanoTime() - prevBtTime) / 1e6 > 50) {
                    //Log.d(TAG, "v" + Integer.toString(verServDegr) + "h" + Integer.toString(horServDegr) + ";");
                    //bts.btCmd("sh" + Integer.toString(horServDegr) + ";sv" + Integer.toString(verServDegr) + ";");
                    spine.turnHead(verServDegr);
                    prevBtTime = System.nanoTime();
                }
            }
        });

    }

    public void initCamera(View view) {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
            startCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    public void stopCamera(View view) {
        if (mCameraSource != null) {
            try {
                mCameraSource.release();
            } catch (NullPointerException ignored) {  }
            mCameraSource = null;
        }
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(RES_X, RES_Y)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker();
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            Log.d(TAG, "x=" + Float.toString(item.getPosition().x) + "y=" + Float.toString(item.getPosition().y));
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            float x = face.getPosition().x + face.getWidth() / 2f;
            float y = face.getPosition().y + face.getHeight() / 2f;
            Log.d(TAG, "x=" + Float.toString(x) + "y=" + Float.toString(y));
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            moveServoToPos(Math.round(x), Math.round(y));
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            super.onMissing(detectionResults);
            Log.d(TAG, "max x=" + Float.toString(maxX) + "y=" + Float.toString(maxY));
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            super.onDone();
        }
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            Log.d(TAG, "startCameraSource");
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera(null);
    }

    private void moveServoToPos(int x, int y) {
        //horServDegr = mapRangeToDegree(x, 0, RES_X, 180, 20);
        //verServDegr = mapRangeToDegree(y, 0, RES_Y, 99, 0);

            int step = 1;
            int affordable = RES_Y / 15;

            if (y > RES_Y / 2 + affordable) {
                spine.turnHeadVertical(-1*step);
                Log.d(TAG, "DOWN");
            } else if (y < RES_Y / 2 - affordable) {
                Log.d(TAG, "UP");
                spine.turnHeadVertical(step);
            } else Log.d(TAG, "STAY");


        //Log.d(TAG, "x="+Integer.toString(x)+" y="+Integer.toString(y));
        //spine.turnHead(horServDegr, verServDegr);
    }

    private int mapRangeToDegree(int x, int in_min, int in_max, int out_min, int out_max) {
        int result = 0;
        result = (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        return servLimit(result);
    }

    public void btConnect(View view) {
        bts.startBt();
        spine.turnHead(50);
    }

    public void btDisconnect(View view) {
        bts.stopBt();
    }

    public void onClickSend(View view) {
        String string = et.getText().toString();
        string.concat("\n");
        et.getText().clear();
        bts.btCmd(string);

    }

    public void consoleClear(View view) {
        tv.setText("");
    }

    public void verServPlus(View view) {
        verServDegr += 5;
        bts.btCmd("sv"+Integer.toString(verServDegr)+";");
    }

    public void verServMinus(View view) {
        verServDegr -= 5;
        bts.btCmd("sv"+Integer.toString(verServDegr)+";");
    }

    public void horServPlus(View view) {
        horServDegr += 5;
        bts.btCmd("sh"+Integer.toString(horServDegr)+";");
    }

    public void horServMinus(View view) {
        horServDegr -= 5;
        bts.btCmd("sh"+Integer.toString(horServDegr)+";");
    }

    public void getSensors(View view) {
        bts.btCmd("sonar;temp;");
    }

    private int servLimit(int degree) {
        int result = degree;
        if (degree < 0) {
            result = 0;
        } else if (degree > 180) {
            result = 180;
        }
        return result;
    }

    public void setHorServPos(int degree) {
        horServDegr = servLimit(degree);
        bts.btCmd("sh"+Integer.toString(horServDegr)+";");
    }

    public void setVerServPos(int degree) {
        verServDegr = servLimit(degree);
        bts.btCmd("sv"+Integer.toString(verServDegr)+";");
    }
}
