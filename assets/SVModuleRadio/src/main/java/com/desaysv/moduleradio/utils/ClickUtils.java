package com.desaysv.moduleradio.utils;

import android.util.Log;

/**
 * 点击事件的工具类
 */
public class ClickUtils {

    private static final String TAG = "ClickUtils";

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
        Log.d(TAG, "isFastClick: interval = " + interval);
        if (interval >= CLICK_INTERVAL) {
            //只有点击成功，才能记忆时间
            lastClickTime = curClickTime;
            flag = true;
        }
        return flag;
    }

    private static long uiStartTime = 0L;
    public static boolean isAllowClickSpecialAst(){
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        long interval = Math.abs(curClickTime - uiStartTime);
        Log.d(TAG, "isAllowClickSpecialAst: interval = " + interval);
        if (interval >= CLICK_INTERVAL) {
            flag = true;
        }
        return flag;
    }

    public static void setUiStartTime() {
        uiStartTime = System.currentTimeMillis();
    }
}
