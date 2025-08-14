package com.desaysv.audiosdk.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.desaysv.audiosdk.BuildConfig;
import com.desaysv.audiosdk.IAudioFocusManager;
import com.desaysv.audiosdk.listener.DsvServiceConnectInterface;

/**
 * created by lzm on 2019-10-11
 * purpose: 初始化SDK入口
 */
public class DsvAudioSDKManager {

    private static final String TAG = "DsvAudioSDKManager";

    @SuppressLint("StaticFieldLeak")
    private static DsvAudioSDKManager instance = null;

    private Context mContext;

    private DsvServiceConnectInterface mServiceConnectCallback;

    public static DsvAudioSDKManager getInstance(){
        if (instance == null){
            synchronized (DsvAudioSDKManager.class){
                if (instance == null){
                    instance = new DsvAudioSDKManager();
                }
            }
        }
        return instance;
    }

    /**
     * 德赛媒体SDK初始化
     * @param context 上下文
     * @param serviceConnectCallback 服务连接状态回调
     */
    public void initialize(Context context, DsvServiceConnectInterface serviceConnectCallback ){
        //Log.d(TAG, "SDK Version is " + BuildConfig.VERSION_NAME);
        Log.d(TAG, "initialize: ");
        mContext = context;
        mServiceConnectCallback = serviceConnectCallback;
        DsvAudioFocusServiceProxy.getInstance().initialize(context);
        DsvAudioFocusServiceProxy.getInstance().setServiceConnectCallback(this.serviceConnectCallback);
        DsvAudioFocusServiceProxy.getInstance().bindMediaService();
    }

    private DsvServiceConnectInterface serviceConnectCallback = new DsvServiceConnectInterface() {
        @Override
        public void onServiceConnect(IAudioFocusManager iAudioFocusManager) {
            mServiceConnectCallback.onServiceConnect(iAudioFocusManager);
        }

        @Override
        public void onServiceDisConnect() {
            mServiceConnectCallback.onServiceDisConnect();
        }
    };




}
