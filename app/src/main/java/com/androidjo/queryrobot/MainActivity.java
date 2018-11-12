package com.androidjo.queryrobot;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SmileFace.OnSmileySelectionListener, SmileFace.OnRatingSelectedListener, TextToSpeech.OnInitListener {

    private SmileFace mSmileFace;
    private static final String TAG = "MainActivity";
    private int currentSmile;
    private TextView tv;
    private TextToSpeech tts;
    private Locale locale;

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
        tts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            locale = new Locale("ru");

            int result = tts.setLanguage(locale);
            //int result = mTTS.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Извините, этот язык не поддерживается");
            }

        } else {
            Log.e(TAG, "Ошибка при инициализации голоса");
        }

    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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
        for (Voice v : tts.getVoices()) {
            Log.i(TAG,v.getName());
        }
        String text = "Всем привет. Меня зовут Квэри.";
        Voice v = new Voice("ru-ru-x-dfc#male_1-local", locale, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW, false, null);
        tts.setVoice(v);
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);

        v = new Voice("ru-ru-x-dfc#male_2-local", locale, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW, false, null);
        tts.setVoice(v);
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);

        v = new Voice("ru-ru-x-dfc#male_3-local", locale, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_LOW, false, null);
        tts.setVoice(v);
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

}
