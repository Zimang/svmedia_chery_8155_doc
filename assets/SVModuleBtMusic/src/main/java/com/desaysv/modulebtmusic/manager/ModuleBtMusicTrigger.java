package com.desaysv.modulebtmusic.manager;

import android.content.Context;
import android.util.Log;

import com.desaysv.modulebtmusic.Constants;

public class ModuleBtMusicTrigger {
    private static final String TAG = Constants.TAG + "ModuleBtMusicTrigger";
    private static volatile ModuleBtMusicTrigger mInstance;
    private Context mContext;

    public static ModuleBtMusicTrigger getInstance() {
        if (mInstance == null) {
            synchronized (ModuleBtMusicTrigger.class) {
                if (mInstance == null) {
                    mInstance = new ModuleBtMusicTrigger();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        BTMusicManager.getInstance().initialize(context);
        BTMusicAudioManager.getInstance().initialize(context);
        BTMusicVRAdapterManager.getInstance().initialize(context);
        BTMusicDataServiceManager.getInstance().initialize(context);
//        BTMusicHardKeyAdapterManager.getInstance().initialize(context);
        Log.d(TAG, "initialize: finish");
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "release: ");
        BTMusicManager.getInstance().release();
        BTMusicAudioManager.getInstance().release();
        BTMusicVRAdapterManager.getInstance().release();
        BTMusicDataServiceManager.getInstance().release();
//        BTMusicHardKeyAdapterManager.getInstance().release();
        Log.d(TAG, "release: finish");
    }
}
