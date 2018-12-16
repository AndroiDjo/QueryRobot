package com.androidjo.queryrobot;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.androidjo.queryrobot.recognizer.DataFiles;
import com.androidjo.queryrobot.recognizer.Grammar;
import com.androidjo.queryrobot.recognizer.PhonMapper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class RoboHearing implements RecognitionListener, Runnable{

    private static final String TAG = "RoboHearing";
    private SpeechRecognizer recognizer;
    private static final String COMMAND_SEARCH = "command";
    private RoboCommand mRoboCommand;
    private String[] mCommands;
    private Activity mActivity;
    private final Handler mHandler = new Handler();
    private static RoboHearing mInstance = new RoboHearing();

    private RoboHearing() {

    }

    public static RoboHearing getInstance(){
        return mInstance;
    }

    public void initHearing(Activity activity, RoboCommand rc, String[] commands) {
        if (mRoboCommand==null) {
            mRoboCommand = rc;
            mCommands = commands;
            mActivity = activity;
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
//        if (recognizer.getSearchName().equals(COMMAND_SEARCH)) {
//            recognizer.stop();
//        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        post(1000, mStopRecognitionCallback);
        String text = hypothesis.getHypstr();
        Util.log("partial "+text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        String text = hypothesis != null ? hypothesis.getHypstr() : null;
        mHandler.removeCallbacks(mStopRecognitionCallback);
        if (text != null) {
            Util.log("result "+text);
            mRoboCommand.doCommand(text);
        }
        recognizer.startListening(COMMAND_SEARCH);
    }

    @Override
    public void onError(Exception e) {
        Util.log("error "+e.getLocalizedMessage());
    }

    @Override
    public void onTimeout() {
        Util.log("timeout");
    }

    @Override
    public void run() {
        if (mActivity!=null)
            setupRecognizer(mActivity);
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<Activity> activityReference;
        WeakReference<RoboHearing> hearingReference;
        SetupTask(Activity activity, RoboHearing rh) {
            this.activityReference = new WeakReference<>(activity);
            this.hearingReference = new WeakReference<>(rh);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                AssetManager am = activityReference.get().getAssets();
                PhonMapper phonMapper = new PhonMapper(am.open("dict/ru/hotwords"));
                Grammar grammar = new Grammar(hearingReference.get().mCommands, phonMapper);
                DataFiles dataFiles = new DataFiles(activityReference.get().getPackageName(), "ru");
                File hmmDir = new File(dataFiles.getHmm());
                File dict = new File(dataFiles.getDict());
                File jsgf = new File(dataFiles.getJsgf());

                String[] assetFiles = am.list("hmm/ru");
                for (String fromFile : assetFiles) {
                    File toFile = new File(hmmDir.getAbsolutePath() + "/" + fromFile);
                    InputStream in = am.open("hmm/ru/" + fromFile);
                    FileUtils.copyInputStreamToFile(in, toFile);
                }

                saveFile(jsgf, grammar.getJsgf());
                saveFile(dict, grammar.getDict());
                hearingReference.get().recognizer = SpeechRecognizerSetup.defaultSetup()
                        .setAcousticModel(hmmDir)
                        .setDictionary(dict)
                        .getRecognizer();
                hearingReference.get().recognizer.addGrammarSearch(COMMAND_SEARCH, jsgf);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception ex) {
            if (ex != null) {
                ex.printStackTrace();
                Util.log(ex.getMessage());
            } else {
                hearingReference.get().recognizer.addListener(hearingReference.get());
                hearingReference.get().recognizer.startListening(COMMAND_SEARCH);
                Util.log("Готов к распознаванию речи");
            }
        }

        private void saveFile(File f, String content) throws IOException {
            File dir = f.getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Cannot create directory: " + dir);
            }
            FileUtils.writeStringToFile(f, content, "UTF8");
        }
    }

    private void setupRecognizer(Activity activity) {
        new SetupTask(activity, this).execute();
    }

    private void post(long delay, Runnable task) {
        mHandler.postDelayed(task, delay);
    }

    private final Runnable mStopRecognitionCallback = new Runnable() {
        @Override
        public void run() {
            stopRecognition();
        }
    };

    public synchronized void stopRecognition() {
        if (recognizer == null) return;
        recognizer.stop();
    }

}
