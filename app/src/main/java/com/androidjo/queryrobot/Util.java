package com.androidjo.queryrobot;

public class Util {

    public static void log(String msg) {
        BtSingleton.getInstance().console(msg);
    }

    public static int mapRangeToRange(int x, int in_min, int in_max, int out_min, int out_max) {
        int result = 0;
        result = (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        return result;
    }
}
