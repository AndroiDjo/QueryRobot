package com.androidjo.queryrobot;

public class Util {

    public static void log(String msg) {
        BtSingleton.getInstance().console(msg);
    }
}
