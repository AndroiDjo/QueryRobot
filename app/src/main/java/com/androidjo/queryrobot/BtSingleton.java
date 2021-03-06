package com.androidjo.queryrobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BtSingleton {
    private BluetoothAdapter mBluetoothAdapter;
    private static final String TAG = "BtSingleton";
    private TextView tv;
    private static final String DEVICE_ADDRESS = "00:21:13:02:A0:88";
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private static BluetoothDevice device;
    private static BluetoothSocket socket;
    private static OutputStream outputStream;
    private static InputStream inputStream;
    private static boolean deviceConnected = false;
    private static byte buffer[];
    private static boolean stopThread;
    private RoboCommand mRoboCommand;

    private static BtSingleton mInstance = new BtSingleton();

    private BtSingleton(){
        // Private constructor to avoid new instances
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BtSingleton getInstance(){
        return mInstance;
    }

    public void setBluetoothAdapter(BluetoothAdapter adapter){
        mBluetoothAdapter = adapter;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return mBluetoothAdapter;
    }

    public void setConsoleTV(TextView view) {
        tv = view;
    }

    public void setmRoboCommand(RoboCommand c) {
        mRoboCommand = c;
    }

    public boolean isBtEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isArduinoConnected() {
        return deviceConnected;
    }

    public void startBt() {
        if(BTinit()) {
            if(BTconnect()) {
                deviceConnected=true;
                console("Arduino connected");
            }
        }
    }

    public CallbackTask startBtCallback(Callback cb) {
        CallbackTask cbt = new CallbackTask(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread && !deviceConnected) {
                    try {
                        startBt();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        stopThread = true;
                    }
                }
            }
        }, cb);

        return cbt;
    }

    public void stopBt() {
        try {
            stopThread = true;
            outputStream.close();
            inputStream.close();
            socket.close();
            deviceConnected = false;
            console("\nConnection Closed!\n");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean BTinit()
    {
        boolean found=false;
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
        {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            if(bondedDevices.isEmpty())
            {
                console("Please Pair the Device first");
            }
            else
            {
                for (BluetoothDevice iterator : bondedDevices)
                {
                    if(iterator.getAddress().equals(DEVICE_ADDRESS))
                    {
                        device=iterator;
                        found=true;
                        break;
                    }
                }
            }
        }
        return found;
    }

    private boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return connected;
    }

    public Runnable getRunnableBtListener() {
        Runnable r = new Runnable() {
            public void run() {
                beginListenForData();
            }
        };
        return r;
    }

    private void beginListenForData()
    {
        stopThread = false;
        buffer = new byte[1024];
        while(!Thread.currentThread().isInterrupted() && !stopThread)
        {
            try
            {
                int byteCount = inputStream.available();
                if(byteCount > 0)
                {
                    byte[] rawBytes = new byte[byteCount];
                    inputStream.read(rawBytes);
                    final String string=new String(rawBytes,"UTF-8");
                    mRoboCommand.doCommand(string);
                    console(string);
                }
            }
            catch (IOException ex)
            {
                stopThread = true;
            }
        }
    }

    public void btCmd (String cmd) {
        try {
            outputStream.write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void console(String msg) {
        if (tv!=null) {
            final String s = msg;
            final int lineAmount = 20;
            tv.post(new Runnable() {
                public void run() {
                    if (tv != null) {
                        tv.append(s + "\n");
                        Layout l = tv.getLayout();
                        if (l != null) {
                            final int scrollAmount = tv.getLayout().getLineTop(tv.getLineCount()) - tv.getHeight();
                            if (scrollAmount > 0)
                                tv.scrollTo(0, scrollAmount);
                            else
                                tv.scrollTo(0, 0);
                            if (tv.getLineCount() > lineAmount) {
                                tv.getEditableText().delete(0, tv.getText().toString().indexOf("\n") + 1);
                            }
                        }
                    }
                }
            });
        }
        Log.i(TAG, msg);
    }
}
