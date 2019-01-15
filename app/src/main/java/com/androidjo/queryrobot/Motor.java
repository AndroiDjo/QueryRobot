package com.androidjo.queryrobot;

public class Motor {
    private BtSingleton bts;
    private String cmdPrefix;
    private int minSpeed;
    private int maxSpeed;
    private int currentSpeed = 0;

    public Motor(String prefix, int low, int high) {
        cmdPrefix = prefix;
        minSpeed = low;
        maxSpeed = high;
        bts = BtSingleton.getInstance();
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    private int getSpeedWithLimits(int speed) {
        int result = speed;
        if (result < minSpeed) result = minSpeed;
        else if (result > maxSpeed) result = maxSpeed;
        return result;
    }

    public void move(int leftMotorDirection, int rightMotorDirection, int motorSpeed, int time, int stopDistance) {
        currentSpeed = getSpeedWithLimits(motorSpeed);
        bts.btCmd(cmdPrefix + "&" + Integer.toString(leftMotorDirection) + "&" + Integer.toString(rightMotorDirection) + "&" + Integer.toString(currentSpeed) +
                "&" + Integer.toString(time) + "&" + Integer.toString(stopDistance) + ";");
    }

}
