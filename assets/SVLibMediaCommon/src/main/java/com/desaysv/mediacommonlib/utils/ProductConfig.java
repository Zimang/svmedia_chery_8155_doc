package com.desaysv.mediacommonlib.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * 一些生产版本的下线配置详情
 * 用于区分不同功能
 */
public class ProductConfig {
    private static final String TAG = "ProductConfig";

    /**
     * 获取主题样式
     * 主题设置之前：
     * com.desaysv.setting.temp.theme.mode
     * 主题设置之后：
     * com.desaysv.setting.theme.mode
     *
     * @param context context
     * @return 当前主题名称
     */
    public static String getTheme(Context context) {
        String themeStr = Settings.System.getString(context.getContentResolver(), "com.desaysv.setting.temp.theme.mode");
        Log.i(TAG, "getTheme: themeStr = " + themeStr);
        return themeStr;
    }

    /**
     * 是否主题2
     *
     * @param context context
     * @return T 主题2 F不是
     */
    public static boolean isTheme2(Context context) {
        return "overlay2".equals(ProductConfig.getTheme(context));
    }

    /**
     * 是否主题3
     *
     * @param context context
     * @return T 主题2 F不是
     */
    public static boolean isTheme3(Context context) {
        return "overlay3".equals(ProductConfig.getTheme(context));
    }
}
