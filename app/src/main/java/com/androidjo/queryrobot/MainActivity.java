package com.androidjo.queryrobot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SmileFace.OnSmileySelectionListener, SmileFace.OnRatingSelectedListener {

    private SmileFace mSmileFace;
    private static final String TAG = "MainActivity";
    private int currentSmile;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        mSmileFace = (SmileFace) findViewById(R.id.smileView);
        mSmileFace.setOnSmileySelectionListener(this);
        mSmileFace.setOnRatingSelectedListener(this);
        currentSmile = SmileFace.GOOD;
        mSmileFace.setSelectedSmile(currentSmile);
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

    @Override
    public void onSmileySelected(@Face.Smiley int smiley, boolean reselected) {
        switch (smiley) {
            case SmileFace.BAD:
                Log.i(TAG, "Bad");
                break;
            case SmileFace.GOOD:
                Log.i(TAG, "Good");
                break;
            case SmileFace.GREAT:
                Log.i(TAG, "Great");
                break;
            case SmileFace.OKAY:
                Log.i(TAG, "Okay");
                break;
            case SmileFace.TERRIBLE:
                Log.i(TAG, "Terrible");
                break;
            case SmileFace.NONE:
                Log.i(TAG, "None");
                break;
        }
    }

    @Override
    public void onRatingSelected(int level, boolean reselected) {
        Log.i(TAG, "Rated as: " + level + " - " + reselected);
    }

    public void nextEmotion(View view) {
        if (currentSmile == mSmileFace.SMILES_LIST[mSmileFace.SMILES_LIST.length-1]) {
            currentSmile = mSmileFace.SMILES_LIST[0];
        } else {
            currentSmile++;
        }
        mSmileFace.setSelectedSmile(currentSmile, true);
    }

}
