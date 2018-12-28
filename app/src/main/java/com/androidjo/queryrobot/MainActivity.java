package com.androidjo.queryrobot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidjo.queryrobot.vision.RoboVision;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BtSingleton bts;
    private Spine spine;
    private RoboVision rv;
    private RoboHearing rh;
    private RoboSound rs;
    private LottieAnimationView lav;
    private TextView tv;
    private ExecutorService exService;
    private final Handler mHandler = new Handler();
    final Random random = new Random();
    private String nextEmotion = Const.EMO_REGULAR;
    private String curEmotion = Const.EMO_REGULAR;

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
            Const.LOOK_CENTER,
            Const.CONSOLE_ON,
            Const.CONSOLE_OFF
    };
    private String[] regularEmotions = {"robo_eyes_o_l", "robo_eyes_o_r", "robo_eyes_blink", "robo_eyes_squint"};
    private String[] regularSounds = {"happy", "hmm", "oo", "ooo", "uuu"};

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
        rs = RoboSound.getInstance();
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
        initEmotions();
    }

    private class VoiceCommand implements RoboCommand {
        @Override
        public void doCommand(String s) {
            if (true/*checkCmd(s,Const.ROBO_NAME)*/) {

                if (s.contains(Const.LOOK) || s.contains(Const.CAM_ON) || s.contains(Const.ATTENTION)) {
                    exService.submit(rv);
                } else if (s.contains(Const.STOP_LOOK) || s.contains(Const.STOP_LOOK2)) {
                    rv.stopCamera();
                }

                if(s.contains(Const.LOOK_UP)) {
                    spine.turnHead(100);
                } else if(s.contains(Const.LOOK_DOWN)) {
                    spine.turnHead(0);
                } else if(s.contains(Const.LOOK_CENTER)) {
                    spine.turnHead(50);
                }

                if (s.contains(Const.CONSOLE_ON)) {
                    bts.setConsoleTV(tv);
                } else if (s.contains(Const.CONSOLE_OFF)) {
                    bts.setConsoleTV(null);
                }

                if (s.contains(Const.LATERN)) {
                    spine.switchDiod();
                }
            }
        }
    }

    private void startThinking() {

    }

    private int getRawIndex(String asset) {
        return getResources().getIdentifier(asset,"raw", getPackageName());
    }

    private void initEmotions() {
        final Animator.AnimatorListener loopListener = new AnimatorListenerAdapter() {
            @Override public void onAnimationRepeat(Animator animation) {
               if (!curEmotion.equals(nextEmotion)) {
                   lav.setAnimation(getRawIndex(nextEmotion));
                   curEmotion = nextEmotion;
                   //lav.setProgress(0);
                   lav.playAnimation();
                   if (!nextEmotion.equals(Const.EMO_REGULAR))
                        rs.play(MainActivity.this, getRawIndex(regularSounds[random.nextInt(regularSounds.length)]));
               }

               if (!nextEmotion.equals(Const.EMO_REGULAR))
                   nextEmotion = Const.EMO_REGULAR;
            }
        };

        lav.useHardwareAcceleration(true);
        lav.enableMergePathsForKitKatAndAbove(true);
        // анимация уже запущена из layout'а, просто присвоим лиснер
        lav.addAnimatorListener(loopListener);
        nextRegularEmotion();
    }

    private void nextRegularEmotion() {
        int delay = (random.nextInt(7) + 1)*1000;
        mHandler.postDelayed(new Runnable(){
            public void run() {
                if (nextEmotion.equals(Const.EMO_REGULAR)) {
                    nextEmotion = regularEmotions[random.nextInt(regularEmotions.length)];
                }
                nextRegularEmotion();
            }
        }, delay);
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
