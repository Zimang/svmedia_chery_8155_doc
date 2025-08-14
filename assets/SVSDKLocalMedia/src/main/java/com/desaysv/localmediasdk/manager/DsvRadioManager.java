package com.desaysv.localmediasdk.manager;

import android.content.Context;
import android.util.Log;

import com.desaysv.localmediasdk.listener.IServiceConnectCallback;

/**
 * Created by LZM on 2020-3-18
 * Comment 本地媒体的AIDL的初始化入口
 */
public class DsvRadioManager {

    private static final String TAG = "DsvMusicManager";

    private static DsvRadioManager instance;

    public static DsvRadioManager getInstance() {
        if (instance == null) {
            synchronized (DsvRadioManager.class) {
                if (instance == null) {
                    instance = new DsvRadioManager();
                }
            }
        }
        return instance;
    }

    private DsvRadioManager() {

    }

    public void initialize(Context context, IServiceConnectCallback serviceConnectCallback) {
        Log.d(TAG, "initialize: ");
        DsvRadioServiceProxy.getInstance().initialize(context, serviceConnectCallback);
        DsvRadioServiceProxy.getInstance().bindService();
    }

}
