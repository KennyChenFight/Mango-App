package com.csim.scu.aibox.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.csim.scu.aibox.log.Logger;


public class SpeechRecognizerManager {

    private Context context;
    private SpeechRecognizer speechRecognizer;
    private onListener listener;

    public SpeechRecognizerManager(Context context) {
        this.context = context;
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
    }

    public void startListenering() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizer.startListening(intent);
    }

    public void cancelSpeechRecognizer() {
        speechRecognizer.cancel();
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    public void setSpeechRecognizerListener(onListener listener) {
        this.listener = listener;
        speechRecognizer.setRecognitionListener(new SpeechRecognizerListener());
    }

    private class SpeechRecognizerListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Logger.d("開始講話");
        }

        @Override
        public void onBeginningOfSpeech() {
            //Logger.d("剛開始講話");
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            Logger.d("結束講話");
        }

        @Override
        public void onError(int i) {
            listener.onError(i);
        }

        @Override
        public void onResults(Bundle bundle) {
            listener.onResults(bundle);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }

    public interface onListener {
        void onResults(Bundle bundle);
        void onError(int i);
    }
}
