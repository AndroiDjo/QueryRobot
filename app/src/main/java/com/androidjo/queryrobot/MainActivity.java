package com.androidjo.queryrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidjo.queryrobot.vision.RoboVision;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BtSingleton bts;
    private Spine spine;
    private RoboVision rv;
    private RoboHearing rh;
    private LottieAnimationView lav;
    private TextView tv;
    private ExecutorService exService;

    //голосовые команды
    private static final String LOOK = "смотри";
    private static final String SWITCH_ON = "включи";
    private static final String SWITCH_OFF = "отключи";
    private static final String LATERN = "лампочка";
    private String[] mCommands = {LOOK,SWITCH_ON,SWITCH_OFF,LATERN};
    private String[] animList = {"twirl_particles_loading", "infinite_rainbow", "empty_list", "curved_line_animation", "threed_circle_loader"};
    private int animIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //lav = (LottieAnimationView) findViewById(R.id.animation_view);
        tv = (TextView) findViewById(R.id.textView);
        exService = Executors.newCachedThreadPool();
        initFeelings();
        startThinking();
    }

    private void initFeelings() {
        bts = BtSingleton.getInstance();
        bts.setConsoleTV(tv);
        spine = Spine.getInstance();
        rv = RoboVision.getInstance();
        if (!bts.isBtEnabled()) enableBt();
        if (!bts.isArduinoConnected()) {
            bts.console("Connecting to Arduino...");
            CallbackTask cbt = bts.startBtCallback(new Callback() {
                public void complete() {
                    exService.submit(bts.getRunnableBtListener());
                    spine.turnHead(0, 60);
                }
            });
            exService.submit(cbt);
        }

        rh = RoboHearing.getInstance();
        VoiceCommand vc = new VoiceCommand(this);
        rh.initHearing(this, vc, mCommands);
    }

    private class VoiceCommand implements RoboCommand {
        Activity mActivity;

        VoiceCommand(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void doCommand(String s) {
            if (s.indexOf(LOOK) > -1) {
                rv.initCamera(mActivity);
            } else if (s.indexOf(SWITCH_OFF) > -1) {
                rv.stopCamera();
            } else if (s.indexOf(LATERN) > -1) {
                spine.switchDiod();
            }
        }
    }

    private void startThinking() {

    }

    private void popUp (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void enableBt() {
        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableAdapter, 0);
    }

    public void doAction(View view) {
        /*lav.setAnimation(getResources().getIdentifier(animList[animIndex],"raw", getPackageName()));
        lav.playAnimation();
        if (animIndex >= animList.length-1) animIndex = 0;
        else animIndex++;*/
        rv.initCamera(this);
    }

    public void stopAction(View view) {
        rv.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rv.stopCamera();
    }

}
