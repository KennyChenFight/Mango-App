package com.csim.scu.aibox.util;

/**
 * Created by kenny on 2018/10/17.
 */

public class Click {
    // 兩次點擊按钮之間的點擊間隔不能少於1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
