package com.desaysv.audiosdk.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.desaysv.audiosdk.bean.AudioSourceBeanFactory;
import com.desaysv.audiosdk.utils.MultiTaskTimer;
import com.desaysv.audiosdk.IAudioFocusManager;
import com.desaysv.audiosdk.listener.DsvServiceConnectInterface;

/**
 * created by lzm on 2019-10-11
 * purpose:
 */
public class DsvAudioFocusServiceProxy {

    private static final String TAG = "DsvAudioFocusServiceProxy";

    private static DsvAudioFocusServiceProxy instance;

    private Context mContext = null;

    private IAudioFocusManager binderAidl;

    /**
     * 定时器
     */
    private MultiTaskTimer mTimer = null;
    /**
     * 定时器id
     */
    private final static int TIMER_ID_REBIND_SERVICE = 0;
    private final static int TIMER_TIME_REBIND_SERVICE = 5000;

    public static DsvAudioFocusServiceProxy getInstance() {
        if (instance == null) {
            synchronized (DsvAudioFocusServiceProxy.class) {
                if (instance == null) {
                    instance = new DsvAudioFocusServiceProxy();
                }
            }
        }
        return instance;
    }

    private DsvAudioFocusServiceProxy() {
        mTimer = new MultiTaskTimer(iTimerTaskHandler);
    }

    public void initialize(Context context) {
        mContext = context;
    }


    public void bindMediaService() {
        Log.d(TAG, "bindMediaService: binderAidl = " + binderAidl);
        try {
            Intent service = new Intent();
            service.setClassName("com.desaysv.mediaservice",
                    "com.desaysv.mediaservice.service.AIDLAudioFocusService");
            mContext.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resBindService();
        }
    }

    private void resBindService() {
        //启动重连服务定时器
        if (binderAidl == null && mTimer != null) {
            mTimer.setTimeTask(TIMER_ID_REBIND_SERVICE, TIMER_TIME_REBIND_SERVICE);
        }
    }

    private MultiTaskTimer.ITimerTaskHandler iTimerTaskHandler = new MultiTaskTimer.ITimerTaskHandler() {
        @Override
        public void onTimerTaskHandle(int taskId) {
            if (taskId == TIMER_ID_REBIND_SERVICE) {
                if (mTimer != null && mTimer.isTaskExist(TIMER_ID_REBIND_SERVICE))
                    mTimer.cancelTimeTask(TIMER_ID_REBIND_SERVICE);
                bindMediaService();
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binderAidl = IAudioFocusManager.Stub.asInterface(iBinder);
            Log.d(TAG, "onServiceConnected: binderAidl = " + binderAidl);
            //绑定成功，取消定时器
            if (mTimer != null && mTimer.isTaskExist(TIMER_ID_REBIND_SERVICE)) {
                mTimer.cancelTimeTask(TIMER_ID_REBIND_SERVICE);
            }
            if (mServiceConnectCallback != null) {
                mServiceConnectCallback.onServiceConnect(binderAidl);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
            binderAidl = null;
            //服务断开的时候，需要将音频焦点的数据清除
            AudioSourceBeanFactory.getInstance().resetFactory();
            if (mServiceConnectCallback != null) {
                mServiceConnectCallback.onServiceDisConnect();
            }
            //启动重连服务定时器
            if (mTimer != null) {
                mTimer.setTimeTask(TIMER_ID_REBIND_SERVICE, TIMER_TIME_REBIND_SERVICE);
            }
        }
    };

    private DsvServiceConnectInterface mServiceConnectCallback;

    public void setServiceConnectCallback(DsvServiceConnectInterface serviceConnectCallback) {
        if (mServiceConnectCallback == null) {
            mServiceConnectCallback = serviceConnectCallback;
        }
    }

}
