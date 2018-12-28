package com.androidjo.queryrobot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;

public class RoboSound {
    private MediaPlayer mp;
    private static RoboSound mInstance = new RoboSound();

    private RoboSound() {

    }

    public static RoboSound getInstance(){
        return mInstance;
    }

    public void stop() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    public void play(Context c, int rid) {
        stop();

        mp = MediaPlayer.create(c, rid);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
            }
        });

        mp.start();
    }
}
