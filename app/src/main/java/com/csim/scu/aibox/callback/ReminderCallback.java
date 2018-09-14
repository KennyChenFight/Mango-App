package com.csim.scu.aibox.callback;

import android.location.Location;

/**
 * Created by kenny on 2018/8/16.
 */

public interface ReminderCallback {

    void updateReminder();
    void updateLocation(Location location);
}
