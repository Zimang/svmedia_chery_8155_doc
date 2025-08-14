package com.desaysv.videoapp.service;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.localmediasdk.bean.StartSourceIntentBean;
import com.desaysv.mediacommonlib.utils.ServiceUtils;


/**
 * Created by LZM on 2020-3-24
 * Comment 外部控制的服务，通过这个服务，可以启动对应的音源界面
 * TODO: 后续根据配置的module，修改启动的逻辑
 */
public class OutSideControlService extends IntentService {
    private static final String TAG = "Video_OutSideControlService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        //Android 8.0需要在服务启动之后通知其他启动为前台广播，不然会报异常
        ServiceUtils.startForegroundNotification(this, TAG, TAG);
        setIntentRedelivery(false);
    }

    public OutSideControlService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: intent = " + intent);
        handleIntent(intent);
    }


    /**
     * 接收外界的启动action，同时出发内部启动逻辑
     *
     * @param intent 启动的intent
     */
    private void handleIntent(Intent intent) {
        if (null == intent) {
            Log.e(TAG, "handleIntent: intent is null");
            return;
        }
        String action = intent.getAction();
        Log.d(TAG, "handleIntent: action = " + action);
        if (StartSourceIntentBean.DESAYSV_ACTION_START_SOURCE.equals(action)) {
            String source = intent.getStringExtra(StartSourceIntentBean.KEY_START_SOURCE);
            boolean isForeground = intent.getBooleanExtra(StartSourceIntentBean.KEY_IS_FOREGROUND, false);
            boolean isRequestFocus = intent.getBooleanExtra(StartSourceIntentBean.KEY_IS_REQUEST_FOCUS, true);
            String openReason = intent.getStringExtra(StartSourceIntentBean.KEY_OPEN_REASON);
            Log.d(TAG, "handleIntent: isForeground = " + isForeground + " isRequestFocus = " + isRequestFocus + " openReason = " + openReason + " source = " + source);
            // 当前USB视频，不需要service拉起
            /*switch (source) {
                case DsvAudioSDKConstants.USB0_VIDEO_SOURCE:
                    USB1VideoStartControl.getInstance().startVideo(true);
                    break;
                case DsvAudioSDKConstants.USB1_VIDEO_SOURCE:
                    USB2VideoStartControl.getInstance().startVideo(true);
                    break;
            }*/
        }
    }

}
