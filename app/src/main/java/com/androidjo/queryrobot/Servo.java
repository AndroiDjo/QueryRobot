package com.androidjo.queryrobot;
/*Управление сервоприводами*/
public class Servo {
    private BtSingleton bts;
    private String cmdPrefix;
    private int lowLimit;
    private int highLimit;

    public Servo() {
        bts = BtSingleton.getInstance();
    }

    public Servo(String prefix, int low, int high) {
        cmdPrefix = prefix;
        lowLimit = low;
        highLimit = high;
        bts = BtSingleton.getInstance();
    }

    public void setCmdPrefix(String prefix) {
        cmdPrefix = prefix;
    }

    public void setLimits(int low, int high) {
        lowLimit = low;
        highLimit = high;
    }

    private int getDegreeWithLimits(int degree) {
        int result = degree;
        if (result < lowLimit) result = lowLimit;
        else if (result > highLimit) result = highLimit;
        return result;
    }

    public void move(int degree) {
        bts.btCmd(cmdPrefix + Integer.toString(getDegreeWithLimits(degree)) + ";");
    }
}
