package com.csim.scu.aibox.callback;

import java.util.Map;

/**
 * Created by kenny on 2018/7/30.
 */

public interface LoginFragmentCallback {

    void sendUserProfileData(Map<String, String> map);
    void connectBluetoothDevice();
    void stopBluetoothDevice();
    void isLoginFragment(boolean state);
}
