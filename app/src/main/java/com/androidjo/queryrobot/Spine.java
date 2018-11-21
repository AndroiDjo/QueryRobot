package com.androidjo.queryrobot;
/*Позвоночник: управление групповыми действиями*/
public class Spine {
    private Servo horServo;
    private Servo verServo;

    private static Spine mInstance = new Spine();

    private Spine() {
        horServo = new Servo("sh", 20, 180);
        verServo = new Servo("sv", 30, 180);
    }

    public static Spine getInstance(){
        return mInstance;
    }

    public void turnHead(int horD, int verD) {
        horServo.move(horD);
        verServo.move(verD);
    }
}
