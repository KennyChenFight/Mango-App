package com.csim.scu.aibox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.view.activity.MainActivity;

/**
 * Created by kenny on 2018/8/3.
 */

public class ReminderReceiver extends BroadcastReceiver {

    public final static int MORNING_CONCERN = 1000;
    public final static int NOON_CONCERN = 1001;
    public final static int NIGHT_CONCERN = 1002;
    public final static int MORNING_NEED = 1003;

    public interface ReminderMessageReceiver {
        void doConcernTask(String concernType);
        void doReminderTask(String dosomething);
        void doNeedTask();
    }

    private static ReminderMessageReceiver reminderMessageReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(intent.getExtras().toString());
        if (intent.hasExtra("concern")) {
            reminderMessageReceiver.doConcernTask(intent.getStringExtra("concern"));
        }
        if (intent.hasExtra("remind")) {
            reminderMessageReceiver.doReminderTask(intent.getStringExtra("remind"));
        }
        if (intent.hasExtra("need")) {
            reminderMessageReceiver.doNeedTask();
        }
    }

    public void concernRegisterCallback(ReminderMessageReceiver concernMessageReceiver) {
        this.reminderMessageReceiver = concernMessageReceiver;
    }
}
