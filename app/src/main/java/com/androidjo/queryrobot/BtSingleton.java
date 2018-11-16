package com.androidjo.queryrobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

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

    private static BtSingleton mInstance = new BtSingleton();

    private BtSingleton(){
        // Private constructor to avoid new instances
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startBt();
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

    public boolean isBtEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void startBt() {
        if(BTinit())
        {
            if(BTconnect())
            {
                deviceConnected=true;
                beginListenForData();
                console("\nConnection Opened!\n");
            }

        }
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

    private void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
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
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    console(string);
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void btCmd (String cmd) {
        if (!deviceConnected) startBt();
        try {
            console(cmd);
            outputStream.write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void console(String msg) {
        if (tv!=null) {
            tv.append(msg);
        }
        Log.i(TAG, msg);
    }
}
