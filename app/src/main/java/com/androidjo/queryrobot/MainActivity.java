package com.androidjo.queryrobot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidjo.queryrobot.ui.JoystickView;
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
    private ImageView iv;
    private JoystickView joystick;
    private ExecutorService exService;
    private final Handler mHandler = new Handler();
    final Random random = new Random();
    private String nextEmotion = Const.EMO_REGULAR;
    private String curEmotion = Const.EMO_REGULAR;
    private String[] regularEmotions = {"robo_eyes_o_l", "robo_eyes_o_r", "robo_eyes_blink", "robo_eyes_squint"};
    private String[] regularSounds = {"happy", "hmm", "oo", "ooo", "uuu"};
    private String[] myNames = {"query", "queery"};
    long prevCmdTime = System.nanoTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lav = (LottieAnimationView) findViewById(R.id.animation_view);
        tv = (TextView) findViewById(R.id.textView);
        iv = (ImageView) findViewById(R.id.imageView);
        joystick = (JoystickView) findViewById(R.id.joystickView);
        iv.setClickable(true);
        exService = Executors.newCachedThreadPool();
        initFeelings();
        startThinking();
    }

    private void initFeelings() {
        bts = BtSingleton.getInstance();
        bts.setConsoleTV(tv);
        ArduinoCommand ac = new ArduinoCommand();
        bts.setmRoboCommand(ac);
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
        rh.initHearing(this, vc, iv);
        exService.submit(rh);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rh.startRecognition();
            }
        });
        initEmotions();
        initJoystick();
    }

    private class VoiceCommand implements RoboCommand {
        @Override
        public void doCommand(String s) {
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
                tv.setText("");
            }

            if (s.contains(Const.MOVE_FWD)) {
                spine.moveForward(255, 1000, 0);
            } else if (s.contains(Const.MOVE_BACK)) {
                spine.moveBack(255, 1000);
            } else if (s.contains(Const.MOVE_LEFT)) {
                spine.moveLeft(90, 250, 0);
            } else if (s.contains(Const.MOVE_RIGHT)) {
                spine.moveRight(90, 250, 0);
            }

            if (s.contains(Const.HOW_IS_YOUR_NAME)) {
                rs.play(MainActivity.this, getRawIndex(myNames[random.nextInt(myNames.length)]));
            }

            if (s.contains(Const.LATERN)) {
                spine.switchDiod();
            }
        }
    }

    private class ArduinoCommand implements RoboCommand {
        @Override
        public void doCommand(String s) {
            if (s.equals(Const.ARD_LISTEN)) { // включаем распознавание голоса
                rh.startRecognition();
            } else if (s.equals(Const.ARD_BARRIER)) {
                // уперлись в препятствие
            }

        }
    }

    private void initJoystick() {
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if ((System.nanoTime() - prevCmdTime) / 1e6 > 50){
                    Log.d(TAG, "angle=" + Integer.toString(angle) + " strength=" + Integer.toString(strength) + ";");
                    float k = strength / 100f;
                    if (angle > 5 && angle <= 90) {
                        spine.moveForward(Math.round(spine.motorMaxSpeed * k),
                                Math.round(Util.mapRangeToRange(angle, 6, 90, spine.motorMinSpeed, spine.motorMaxSpeed) * k),
                                200, 0);
                    } else if (angle > 90 && angle < 175) {
                        spine.moveForward(Math.round(Util.mapRangeToRange(angle, 91, 174, spine.motorMaxSpeed, spine.motorMinSpeed) * k),
                                Math.round(spine.motorMaxSpeed * k),200, 0);
                    } else if (angle > 185 && angle <=270 && strength > 10) {
                        spine.moveBack(Math.round(Util.mapRangeToRange(angle, 186, 270, spine.motorMinSpeed, spine.motorMaxSpeed) * k),
                                Math.round(spine.motorMaxSpeed*k), 200);
                    } else if (angle > 270 && angle < 355 && strength > 10) {
                        spine.moveBack(Math.round(spine.motorMaxSpeed*k),
                                Math.round(Util.mapRangeToRange(angle, 271, 354, spine.motorMaxSpeed, spine.motorMinSpeed) * k), 200);
                    } else if ((angle <= 5 || angle >= 355) && strength > 40) {
                        spine.moveRight(Util.mapRangeToRange(strength, 0, 100, spine.motorMinSpeed, spine.motorMaxSpeed), 200, 0);
                    } else if ((angle >= 175 && angle <= 185) && strength > 40) {
                        spine.moveLeft(Util.mapRangeToRange(strength, 0, 100, spine.motorMinSpeed, spine.motorMaxSpeed), 200, 0);
                    }

                    prevCmdTime = System.nanoTime();
                }
            }
        });
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
//                   if (!nextEmotion.equals(Const.EMO_REGULAR))
//                        rs.play(MainActivity.this, getRawIndex(regularSounds[random.nextInt(regularSounds.length)]));
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
        spine.moveForward(255, 1000, 15);
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
