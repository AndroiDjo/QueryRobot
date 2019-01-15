package com.androidjo.queryrobot;

import android.app.Activity;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class RoboHearing implements RecognitionListener, Runnable{

    private static final String TAG = "RoboHearing";
    private SpeechRecognizer recognizer;
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "слушай квэри";
    private static final String COMMAND_SEARCH = "command";
    private RoboCommand mRoboCommand;
    private Activity mActivity;
    private final Handler mHandler = new Handler();
    private ImageView iv;
    private static RoboHearing mInstance = new RoboHearing();

    private RoboHearing() {

    }

    public static RoboHearing getInstance(){
        return mInstance;
    }

    public void initHearing(Activity activity, RoboCommand rc, ImageView i) {
        if (mRoboCommand==null) {
            mRoboCommand = rc;
            mActivity = activity;
            iv = i;
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            startKWS();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        Util.log("partial "+text);
        if (text.equals(KEYPHRASE))
            startRecognition();
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Util.log("onResult");
        String text = hypothesis != null ? hypothesis.getHypstr() : null;
        mHandler.removeCallbacks(mStopRecognitionCallback);
        if (text != null) {
            Util.log("result "+text);
            mRoboCommand.doCommand(text);
        }
    }

    public synchronized void startRecognition() {
        if (recognizer == null || COMMAND_SEARCH.equals(recognizer.getSearchName())) return;
        recognizer.stop();
        iv.setBackgroundResource(R.drawable.mic);
        recognizer.startListening(COMMAND_SEARCH, 3000);
        post(3000, mStopRecognitionCallback);
    }

    public synchronized void startKWS() {
        if (recognizer == null || KWS_SEARCH.equals(recognizer.getSearchName())) return;
        recognizer.stop();
        iv.setBackgroundResource(R.drawable.mic_mute);
        recognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void onError(Exception e) {
        Util.log("error "+e.getLocalizedMessage());
    }

    @Override
    public void onTimeout() {
        Util.log("timeout");
        startKWS();
    }

    @Override
    public void run() {
        if (mActivity!=null)
            setupRecognizer(mActivity);
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "acoustic_model"))
                .setDictionary(new File(assetsDir, "dict.dic"))
                //.setKeywordThreshold(1e-7f)
                .setSampleRate(16000)
                .setBoolean("-remove_noise", true)
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        recognizer.addGrammarSearch(COMMAND_SEARCH, new File(assetsDir, "lm.jsgf"));
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
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                hearingReference.get().setupRecognizer(assetDir);
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
                hearingReference.get().recognizer.startListening(KWS_SEARCH);
                Util.log("Готов к распознаванию речи");
            }
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
        if (recognizer == null || KWS_SEARCH.equals(recognizer.getSearchName())) return;
        recognizer.stop();
    }

}
