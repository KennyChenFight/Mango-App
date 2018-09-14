package com.csim.scu.aibox.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by kenny on 2018/8/29.
 */

public class PhoneStateReceiver extends BroadcastReceiver {

    public interface PhoneStateMessageReceiver {
        void resume();
    }

    private static PhoneStateReceiver.PhoneStateMessageReceiver phoneStateMessageReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            Log.d("Kenny", "撥出");
        }
        else {
            Log.d("Kenny", "來電");
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d("Kenny", incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("Kenny", "掛斷");
                    phoneStateMessageReceiver.resume();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("Kenny", "接聽");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("Kenny", "來電,來電號碼:" + incomingNumber);
                    break;
            }
        }

    };

    public void resumeRegisterCallback(PhoneStateMessageReceiver phoneStateMessageReceiver) {
        this.phoneStateMessageReceiver = phoneStateMessageReceiver;
    }
}
