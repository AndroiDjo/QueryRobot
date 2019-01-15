package com.androidjo.queryrobot;
/*Управление сервоприводами*/
public class Servo {
    private BtSingleton bts;
    private String cmdPrefix;
    private int lowLimit;
    private int highLimit;
    private int currentDegree = 0;
    private int prevDegree = -1;

    public Servo(String prefix, int low, int high) {
        cmdPrefix = prefix;
        lowLimit = low;
        highLimit = high;
        bts = BtSingleton.getInstance();
    }

    public int getCurrentDegree() {
        return currentDegree;
    }

    private int getDegreeWithLimits(int degree) {
        int result = degree;
        if (result < lowLimit) result = lowLimit;
        else if (result > highLimit) result = highLimit;
        return result;
    }

    private void sendCmd(int degree) {
        if (prevDegree!=degree) {
            bts.btCmd(cmdPrefix + Integer.toString(degree) + ";");
            prevDegree = degree;
        }
    }

    public void move(int degree) {
        currentDegree = getDegreeWithLimits(degree);
        sendCmd(currentDegree);
    }

    public void step(int step) {
        currentDegree = getDegreeWithLimits(currentDegree+step);
        sendCmd(currentDegree);
    }
}
