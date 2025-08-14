package com.desaysv.moduleusbpicture.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.desaysv.moduleusbpicture.widget.ControlViewPager;

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

    //幻灯片播放时间的key值
    private static final String SLIDES_TIME = "slides_time";


    /**
     * 保存幻灯片播放的时间
     *
     * @param time 时间
     */
    public void saveSlidesTime(int time) {
        Log.d(TAG, "saveSlidesTime: time = " + time);
        mEditor.putInt(SLIDES_TIME, time);
        mEditor.commit();
    }


    /**
     * 获取幻灯片播放的时间
     *
     * @return int 幻灯片切换的时间
     */
    public int getSlidesTime() {
        int time = mSharedPreferences.getInt(SLIDES_TIME, ControlViewPager.FIVES_SECONDS);
        Log.d(TAG, "getSlidesTime: time = " + time);
        return time;
    }


}
