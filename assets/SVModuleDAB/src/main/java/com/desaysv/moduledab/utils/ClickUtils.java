package com.desaysv.moduledab.utils;

import android.util.Log;

public class ClickUtils {

    /**
     * item  点击消抖时间
     */
    private static final long CLICK_INTERVAL = 300L;
    private static long lastClickTime = 0L;

    /**
     * 计算时间是否在间隔时间外
     */
    public static boolean isAllowClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        long interval = Math.abs(curClickTime - lastClickTime);
        Log.d("ClickUtils", "isFastClick: interval = " + interval);
        if (interval >= CLICK_INTERVAL) {
            //只有点击成功，才能记忆时间
            lastClickTime = curClickTime;
            flag = true;
        }
        return flag;
    }
}
