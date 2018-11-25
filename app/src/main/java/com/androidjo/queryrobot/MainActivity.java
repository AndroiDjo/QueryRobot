package com.androidjo.queryrobot;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BtSingleton bts;
    private LottieAnimationView lav;
    private Mind mind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lav = (LottieAnimationView) findViewById(R.id.animation_view);

        bts = BtSingleton.getInstance();
        if (!bts.isBtEnabled()) enableBt();
        bts.startBt();
        popUp("Bluetooth started");

        mind = Mind.getInstance();
        mind.startThinking();
    }

    private void popUp (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void enableBt() {
        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableAdapter, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sql_editor) {
            Intent intent = new Intent(MainActivity.this, DatabaseActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_bt_console) {
            Intent intent = new Intent(MainActivity.this, BtConsole.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doAction(View view) {
        lav.setAnimation(getResources().getIdentifier("curved_line_animation","raw", getPackageName()));
        lav.playAnimation();
    }

}
