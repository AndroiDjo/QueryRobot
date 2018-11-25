package com.androidjo.queryrobot;

/*Мозги*/
public class Mind {
    private Spine spine;

    private static Mind mInstance = new Mind();

    private Mind() {
        spine = Spine.getInstance();
    }

    public static Mind getInstance(){
        return mInstance;
    }

    public void startThinking() {
        spine.turnHead(0, 60);
    }

}
