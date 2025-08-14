package com.desaysv.moduleusbmusic.utils;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Locale;

public class LocaleUtils {

    private static final String TAG = "LocaleUtils";

    private static final String HEBREW_LOCALE = "iw"; //希伯来语

    private static final String SPAIN_LOCALE = "es"; //西班牙语

    private final Locale mLocale;

    private static LocaleUtils instance;

    public static LocaleUtils getInstance() {
        if (instance == null) {
            synchronized (LocaleUtils.class) {
                if (instance == null) {
                    instance = new LocaleUtils();
                }
            }
        }
        return instance;
    }

    private LocaleUtils() {
        mLocale = Locale.getDefault();
        Log.d(TAG, "LocaleUtils: mLocale = " + mLocale);
        //updateLanguage(new Locale("es", "ES", ""));
    }

    /**
     * 是否希伯来语
     *
     * @return T F
     */
    public boolean isHebrewLocale() {
        return isLocale(HEBREW_LOCALE);
    }

    /**
     * 是否西班牙语
     *
     * @return T F
     */
    public boolean isSpainLocale() {
        return isLocale(SPAIN_LOCALE);
    }

    /**
     * 判断对应输入语言是不是当前系统设置的语言
     *
     * @param language language
     * @return T F
     */
    public boolean isLocale(String language) {
        if (TextUtils.isEmpty(language)) {
            return false;
        }
        //切换语言，所以每次都要Locale.getDefault()
        Locale locale = Locale.getDefault();
        String localeLanguage = locale.getLanguage();
        Log.d(TAG, "isLocale: localeLanguage = " + localeLanguage + " language = " + language + " country = " + locale.getCountry());
        return localeLanguage.equals(new Locale(language).getLanguage());
    }

    /**
     * 用于切换语言调试，不对外提供
     * @param locale locale
     */
    private void updateLanguage(Locale locale) {
        try {
            Object objIActMag;
            Class<?> clzIActMag = Class.forName("android.app.IActivityManager");
            Class<?> clzActMagNative = Class
                    .forName("android.app.ActivityManagerNative");
            @SuppressLint("DiscouragedPrivateApi")
            Method mtdActMagNative$getDefault = clzActMagNative
                    .getDeclaredMethod("getDefault");
            objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
            // objIActMag = amn.getConfiguration();
            @SuppressLint("DiscouragedPrivateApi")
            Method mtdIActMag$getConfiguration = clzIActMag
                    .getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
            // set the locale to the new value
            if (config != null) {
                config.locale = locale;
            }
            //持久化  config.userSetLocale = true;
            Class<?> clzConfig = Class
                    .forName("android.content.res.Configuration");
            java.lang.reflect.Field userSetLocale = clzConfig
                    .getField("userSetLocale");
            userSetLocale.set(config, true);
            //如果有阿拉伯语，必须加上，否则阿拉伯语与其它语言切换时，布局与文字方向不会改变
            Method setLayoutDirection = clzConfig
                    .getDeclaredMethod("setLayoutDirection", Locale.class);
            setLayoutDirection.invoke(config, locale);
            // 此处需要声明权限:android.permission.CHANGE_CONFIGURATION
            // 会重新调用 onCreate();
            Class[] clzParams = {Configuration.class};
            Method mtdIActMag$updateConfiguration = clzIActMag
                    .getDeclaredMethod("updateConfiguration", clzParams);
            mtdIActMag$updateConfiguration.invoke(objIActMag, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
