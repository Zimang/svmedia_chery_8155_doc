package com.desaysv.svaudioapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.localmediasdk.sdk.bean.StartSourceIntentBean;

/**
 * @author uidq1846
 * @desc 用于处理来自MediaSDK当中启动控制媒体等操作服务
 * @time 2022-12-29 10:53
 */
public class MediaSdkControlService extends IntentService {
    private final static String TAG = MediaSdkControlService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        setIntentRedelivery(false);
    }

    @Override
    public int onStartCommand(@androidx.annotation.Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(@androidx.annotation.Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * TAG Used to name the worker thread, important only for debugging.
     */
    public MediaSdkControlService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: intent = " + intent);
        handleIntent(intent);
    }

    /**
     * 接收外界的启动action，同时触发内部启动逻辑
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
            Log.d(TAG, "handleIntent: isForeground = " + isForeground + " source = " + source + " isRequestFocus = " + isRequestFocus + " openReason = " + openReason);
            switch (source) {
                case DsvAudioSDKConstants.BT_MUSIC_SOURCE:
                    //启动蓝牙的
                    break;
                case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:

                    break;
                case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:

                    break;
                default:
                    Log.w(TAG, "handleIntent: not effect source");
                    break;
            }
        }
    }
}
