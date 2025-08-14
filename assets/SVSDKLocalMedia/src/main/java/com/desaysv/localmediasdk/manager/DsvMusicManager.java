package com.desaysv.localmediasdk.manager;

import android.content.Context;
import android.util.Log;

import com.desaysv.localmediasdk.listener.IServiceConnectCallback;

/**
 * Created by LZM on 2020-3-18
 * Comment 本地媒体的AIDL的初始化入口
 */
public class DsvMusicManager {

    private static final String TAG = "DsvMusicManager";

    private static DsvMusicManager instance;

    public static DsvMusicManager getInstance() {
        if (instance == null) {
            synchronized (DsvMusicManager.class) {
                if (instance == null) {
                    instance = new DsvMusicManager();
                }
            }
        }
        return instance;
    }

    private DsvMusicManager() {

    }

    public void initialize(Context context, IServiceConnectCallback serviceConnectCallback) {
        Log.d(TAG, "initialize: ");
        DsvMusicServiceProxy.getInstance().initialize(context,serviceConnectCallback);
        DsvMusicServiceProxy.getInstance().bindService();
    }

}
