package com.androidjo.queryrobot;
/*Позвоночник: управление групповыми действиями*/
public class Spine {
    private Servo horServo;
    private Servo verServo;
    private boolean diodIsOn = false;
    private BtSingleton bts;

    private static Spine mInstance = new Spine();

    private Spine() {
        bts = BtSingleton.getInstance();
        horServo = new Servo("sh", 20, 180);
        verServo = new Servo("sv", 0, 99);
    }

    public static Spine getInstance(){
        return mInstance;
    }

    public void turnHead(int horD, int verD) {
        //horServo.move(horD);
        verServo.move(verD);
    }

    public void turnHeadVertical(int step) {
        verServo.step(step);
    }

    public void switchDiod() {
        diodIsOn = !diodIsOn;
        bts.btCmd(diodIsOn?"0":"1");
    }
}
