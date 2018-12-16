package com.androidjo.queryrobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
    private String[] mCommands = {
            Const.ROBO_NAME,
            Const.LOOK,
            Const.STOP_LOOK,
            Const.STOP_LOOK2,
            Const.CAM_ON,
            Const.LATERN,
            Const.ATTENTION,
            Const.LOOK_UP,
            Const.LOOK_DOWN,
            Const.LOOK_CENTER
    };
    private String[] animList = {"twirl_particles_loading", "infinite_rainbow", "empty_list", "curved_line_animation", "threed_circle_loader"};
    private int animIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lav = (LottieAnimationView) findViewById(R.id.animation_view);
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
                    spine.turnHead(60);
                }
            });
            exService.submit(cbt);
        }
        rv.initCamera(this);
        rh = RoboHearing.getInstance();
        VoiceCommand vc = new VoiceCommand();
        rh.initHearing(this, vc, mCommands);
        exService.submit(rh);
    }

    private class VoiceCommand implements RoboCommand {
        @Override
        public void doCommand(String s) {
            if (checkCmd(s,Const.ROBO_NAME)) {

                if (checkCmd(s,Const.LOOK) || checkCmd(s,Const.CAM_ON) || checkCmd(s,Const.ATTENTION)) {
                    exService.submit(rv);
                } else if (checkCmd(s,Const.STOP_LOOK) || checkCmd(s,Const.STOP_LOOK2)) {
                    rv.stopCamera();
                }

                if(checkCmd(s, Const.LOOK_UP)) {
                    spine.turnHead(100);
                } else if(checkCmd(s, Const.LOOK_DOWN)) {
                    spine.turnHead(0);
                } else if(checkCmd(s, Const.LOOK_CENTER)) {
                    spine.turnHead(50);
                }

                if (checkCmd(s, Const.LATERN)) {
                    spine.switchDiod();
                }
            }
        }
    }

    private boolean checkCmd(String source, String command) {
        return (source.indexOf(command) > -1);
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
        exService.submit(rv);
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
        rh.stopRecognition();
    }

}
