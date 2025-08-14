package com.desaysv.mediaservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

//import com.desaysv.ivi.platform.app.audio.SvCarAudioManager;
import com.desaysv.mediacommonlib.utils.ServiceUtils;
import com.desaysv.svmediaservicelogic.systemcontrol.BootResumeControl;

/**
 * 开机源恢复统一管理服务
 */
public class BootResumeSourceService extends Service {
    private static final String TAG = "BootResumeSourceService";

    private static final String DESAYSV_ACTION_RESUME_SOURCE = "com.desaysv.action.resumeSource";
    private static final String DESAYSV_ACTION_DEFAULT_SOURCE = "com.desaysv.action.defaultSource";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Andoird 8.0需要在服务启动之后通知其他启动为前台广播，不然会报异常
        ServiceUtils.startForegroundNotification(this, "BootResumeSourceService", "BootResumeSourceService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //处理intent的值
        initHandlerThread(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.d(TAG, "onDestroy: ");
    }


    /**
     * 处理intent的函数，主要是实现了音源恢复
     *
     * @param intent
     */
    private void initHandlerThread(Intent intent) {
        boolean result = handleBootResumeIntent(intent);
        Log.d(TAG, "initHandlerThread: result = " + result);
    }


    /**
     * 处理音源恢复的intent
     *
     * @param intent
     * @return
     */
    private boolean handleBootResumeIntent(Intent intent) {
        if (null == intent) {
            Log.w(TAG, "receive null intent");
            return false;
        }
        final String action = intent.getAction();
        //此处的音源恢复逻辑已经无用，可以注释掉。
        //final String carAudioType = intent.getStringExtra(SvCarAudioManager.KEY_CAR_AUDIO_TYPE);
        final String carAudioType = null;
        if (DESAYSV_ACTION_DEFAULT_SOURCE.equals(action) ||
                DESAYSV_ACTION_RESUME_SOURCE.equals(action)) {
            // 开机源恢复
            Log.d(TAG, "handleBootResumeIntent: action = " + action + " carAudioType = " + carAudioType);
            if (TextUtils.isEmpty(carAudioType)) {
                return false;
            }
            BootResumeControl.getInstance().bootResumeSource(getApplicationContext(),carAudioType);
        }
        return false;
    }

}
