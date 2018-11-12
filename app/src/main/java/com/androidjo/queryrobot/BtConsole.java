package com.androidjo.queryrobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BtConsole extends AppCompatActivity {
    private TextView tv;
    private EditText et;

    private static final String TAG = "BtConsole";

    //BT
    private final String DEVICE_ADDRESS="00:21:13:02:A0:88";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    byte buffer[];
    boolean stopThread;
    boolean deviceConnected=false;
    long prevBtTime = System.nanoTime();

    //Servo
    private int horServDegr = 90;
    private int verServDegr = 90;
    private int horMiddleDegr = 100;
    private int verMiddleDegr = 115;

    //joystick
    private static final int JOY_STR_MIN = 0;
    private static final int JOY_STR_MAX = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_console);
        tv = (TextView) findViewById(R.id.BTtextView);
        et = (EditText) findViewById(R.id.BTeditText);

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                console(Integer.toString(strength)+";");

                if (angle > 90 && angle <= 270) {
                    verServDegr = mapRangeToDegree(angle, 91, 270, 50, 180);
                } else if (angle <= 90) {
                    verServDegr = mapRangeToDegree(angle, 0, 90, 115, 50);
                } else if (angle > 270) {
                    verServDegr = mapRangeToDegree(angle, 271, 360, 180, 115);
                }

                if (angle < 180) {
                    horServDegr = mapRangeToDegree(angle, 0, 179, 180, 20);
                } else if (angle >= 180) {
                    horServDegr = mapRangeToDegree(angle, 180, 360, 20, 180);
                }

                if ((horServDegr != horMiddleDegr || verServDegr != verMiddleDegr) && (System.nanoTime() - prevBtTime) / 1e6 > 50){
                    Log.d(TAG, "v" + Integer.toString(verServDegr) + "h" + Integer.toString(horServDegr) + ";");
                    btCmd("sh" + Integer.toString(horServDegr) + ";sv" + Integer.toString(verServDegr) + ";");
                    prevBtTime = System.nanoTime();
                }
            }
        });
    }

    private int mapRangeToDegree(int x, int in_min, int in_max, int out_min, int out_max) {
        int result = 0;
        result = (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        return servLimit(result);
    }

    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
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
        return found;
    }

    public boolean BTconnect()
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

    void beginListenForData()
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
                                    Log.d(TAG, string);
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

    public void btMain() {
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

    private void console(String msg) {
        if (tv!=null) {
            tv.append(msg);
        }
    }

    public void btConnect(View view) {
        btMain();
    }

    public void btDisconnect(View view) {
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

    public void onClickSend(View view) {
        if (!deviceConnected) {
            btMain();
            return;
        }

        String string = et.getText().toString();
        string.concat("\n");
        et.getText().clear();
        btCmd(string);
        console("\nSent Data:"+string+"\n");

    }

    public void consoleClear(View view) {
        tv.setText("");
    }

    public void btCmd (String cmd) {
        if (!deviceConnected) {
            btMain();
            return;
        }

        try {
            outputStream.write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void verServPlus(View view) {
        verServDegr += 5;
        btCmd("sv"+Integer.toString(verServDegr)+";");
    }

    public void verServMinus(View view) {
        verServDegr -= 5;
        btCmd("sv"+Integer.toString(verServDegr)+";");
    }

    public void horServPlus(View view) {
        horServDegr += 5;
        btCmd("sh"+Integer.toString(horServDegr)+";");
    }

    public void horServMinus(View view) {
        horServDegr -= 5;
        btCmd("sh"+Integer.toString(horServDegr)+";");
    }

    public void getSensors(View view) {
        btCmd("sonar;temp;");
    }

    private int servLimit(int degree) {
        int result = degree;
        if (degree < 0) {
            result = 0;
        } else if (degree > 180) {
            result = 180;
        }
        return result;
    }

    public void setHorServPos(int degree) {
        horServDegr = servLimit(degree);
        btCmd("sh"+Integer.toString(horServDegr)+";");
    }

    public void setVerServPos(int degree) {
        verServDegr = servLimit(degree);
        btCmd("sv"+Integer.toString(verServDegr)+";");
    }
}
