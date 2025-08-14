package com.desaysv.modulebtmusic.util;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

/**
 * 用于多主体题区分的工具类
 */
public class ThemeUtil {
    private static final String TAG = "ThemeUtil";

    private static final String OVERLAY_1 = "overlay1";
    private static final String OVERLAY_2 = "overlay2";

    public static final int THEME_ONE = 0;
    public static final int THEME_SECOND = 1;

    public static int getTheme(Context context) {
        String themeStr = Settings.System.getString(context.getContentResolver(), "com.desaysv.setting.theme.mode");
        if (TextUtils.isEmpty(themeStr)) {
            Log.d(TAG, "getTheme: themeStr is null, default use theme 1");
            return THEME_ONE;
        }
        Log.d(TAG, "getTheme: themeStr = " + themeStr);
        switch (themeStr) {
            case OVERLAY_2:
                return THEME_SECOND;
            case OVERLAY_1:
            default:
                Log.d(TAG, "getTheme: default themeStr");
                return THEME_ONE;
        }
    }


}
