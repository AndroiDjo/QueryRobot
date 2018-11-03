package ru.rt.south.rosbot;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements SmileFace.OnSmileySelectionListener, SmileFace.OnRatingSelectedListener {

    private SmileFace mSmileFace;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSmileFace = (SmileFace) findViewById(R.id.smileView);
        mSmileFace.setOnSmileySelectionListener(this);
        mSmileFace.setOnRatingSelectedListener(this);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "MetalMacabre.ttf");
        mSmileFace.setTypeface(typeface);
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

}
