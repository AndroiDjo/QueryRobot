package com.androidjo.queryrobot;

import android.util.Log;

public class Motor {
    private BtSingleton bts;
    private String cmdPrefix;
    private int minSpeed;
    private int maxSpeed;
    private int leftMotorCurrentSpeed = 0;
    private int rightMotorCurrentSpeed = 0;

    public Motor(String prefix, int low, int high) {
        cmdPrefix = prefix;
        minSpeed = low;
        maxSpeed = high;
        bts = BtSingleton.getInstance();
    }

    private int getSpeedWithLimits(int speed) {
        int result = speed;
        if (result < minSpeed) result = minSpeed;
        else if (result > maxSpeed) result = maxSpeed;
        return result;
    }

    public void move(int leftMotorDirection, int rightMotorDirection, int time) {
        move(leftMotorDirection, rightMotorDirection, maxSpeed, maxSpeed, time, 0);
    }

    public void move(int leftMotorDirection, int rightMotorDirection, int leftMotorSpeed, int rightMotorSpeed, int time, int stopDistance) {
        leftMotorCurrentSpeed = getSpeedWithLimits(leftMotorSpeed);
        rightMotorCurrentSpeed = getSpeedWithLimits(rightMotorSpeed);
        Log.d("Motor", Integer.toString(leftMotorDirection)+" "+Integer.toString(rightMotorDirection)+" "+Integer.toString(leftMotorSpeed)+" "+Integer.toString(rightMotorSpeed));
        bts.btCmd(cmdPrefix + "&" + Integer.toString(leftMotorDirection) + "&" + Integer.toString(rightMotorDirection) + "&" + Integer.toString(leftMotorCurrentSpeed) +
                "&" + Integer.toString(rightMotorCurrentSpeed) + "&" + Integer.toString(time) + "&" + Integer.toString(stopDistance) + ";");
    }

}
