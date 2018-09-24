package com.csim.scu.aibox.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


import com.csim.scu.aibox.log.Logger;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechManager implements TextToSpeech.OnInitListener{

    private TextToSpeech textToSpeech;
    private onDone listener;

    public TextToSpeechManager(Context context) {
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = textToSpeech.setLanguage(Locale.TAIWAN);
            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED){
                Logger.d("TTS語言數據丟失或不支持");
            }
            Logger.d("onInit");
        }
    }

    public void setTextToSpeechListener(onDone listener) {
        this.listener = listener;
        textToSpeech.setOnUtteranceProgressListener(new TextToSpeechListener());
    }

    public void speak(String response) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            // 值越大，聲音越尖
            textToSpeech.setPitch(0.9f);
            // 值越大，講話速度越快
            textToSpeech.setSpeechRate(0.6f);
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            textToSpeech.speak(response, TextToSpeech.QUEUE_ADD, map);
        }
    }

    public void shutDownTextToSpeech(){
        textToSpeech.shutdown();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    private class TextToSpeechListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {

        }

        @Override
        public void onDone(String s) {
            listener.onDone(s);
            Logger.d("onDone");
        }

        @Override
        public void onError(String s) {
            Logger.e("onError");
        }
    }

    public interface onDone {
        void onDone(String s);
    }
}
