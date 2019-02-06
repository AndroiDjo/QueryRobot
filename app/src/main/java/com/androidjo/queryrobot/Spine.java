package com.androidjo.queryrobot;
/*Позвоночник: управление групповыми действиями*/
public class Spine {
    private Servo verServo;
    private Motor motor;
    private boolean diodIsOn = true;
    private BtSingleton bts;
    public int motorMinSpeed = 70;
    public int motorMaxSpeed = 255;

    private static Spine mInstance = new Spine();

    private Spine() {
        bts = BtSingleton.getInstance();
        verServo = new Servo("sv", 0, 99);
        motor = new Motor("mot", motorMinSpeed, motorMaxSpeed);
    }

    public static Spine getInstance(){
        return mInstance;
    }

    public void turnHead(int verD) {
        verServo.move(verD);
    }

    public void turnHeadVertical(int step) {
        verServo.step(step);
    }

    public void switchDiod() {
        diodIsOn = !diodIsOn;
        bts.btCmd((diodIsOn?"0":"1")+";");
    }

    public void moveForward(int speed, int time, int stopDistance) {
        motor.move(1, 1, speed, speed, time, stopDistance);
    }

    public void moveForward(int leftMotorSpeed, int rightMotorSpeed, int time, int stopDistance) {
        motor.move(1, 1, leftMotorSpeed, rightMotorSpeed, time, stopDistance);
    }

    public void moveBack(int speed, int time) {
        motor.move(-1, -1, speed, speed, time, 0);
    }

    public void moveBack(int leftMotorSpeed, int rightMotorSpeed, int time) {
        motor.move(-1, -1, leftMotorSpeed, rightMotorSpeed, time, 0);
    }

    public void moveRight(int speed, int time, int stopDistance) {
        motor.move(1, -1, speed, speed, time, stopDistance);
    }

    public void moveLeft(int speed, int time, int stopDistance) {
        motor.move(-1, 1, speed, speed, time, stopDistance);
    }

    public void stopMoving() {
        motor.move(0, 0, 0, 0, 0, 0);
    }
}
