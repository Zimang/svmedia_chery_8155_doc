package com.desaysv.usbpicture.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by LZM on 2020-5-18
 * Comment 用来实现数据持久化保存的SP
 */
public class SharedPreferencesUtils {

    private static final String TAG = "SharedPreferencesUtils";

    private static SharedPreferencesUtils instance;

    public static SharedPreferencesUtils getInstance() {
        if (instance == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (instance == null) {
                    instance = new SharedPreferencesUtils();
                }
            }
        }
        return instance;
    }

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;


    /**
     * 数据初始化
     *
     * @param context 上下文，这里需要app的context
     */
    @SuppressLint("CommitPrefEdits")
    public void initialize(Context context) {
        Log.d(TAG, "initialize: context = " + context);
        mSharedPreferences = context.getSharedPreferences(TAG, 0);
        mEditor = mSharedPreferences.edit();
    }

    private static final String NEED_CHECK_SPEED = "NEED_CHECK_SPEED";
    public void saveNeedCheckSpeed(boolean needCheck){
        Log.d(TAG, "saveNeedCheckSpeed: needCheck = " + needCheck);
        mEditor.putBoolean(NEED_CHECK_SPEED, needCheck);
        mEditor.commit();
    }

    public boolean getNeedCheckSpeed() {
        boolean needCheck = mSharedPreferences.getBoolean(NEED_CHECK_SPEED,true);
        Log.d(TAG, "getNeedCheckSpeed: needCheck = " + needCheck);
        return needCheck;
    }
    private boolean needPop = true;

    public void setNeedPop(boolean needPop) {
        this.needPop = needPop;
    }
    public boolean getNeedPop(){
        return needPop;
    }
}
