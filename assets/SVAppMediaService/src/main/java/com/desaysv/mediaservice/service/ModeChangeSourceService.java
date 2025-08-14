package com.desaysv.mediaservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


import androidx.annotation.Nullable;

import com.desaysv.mediacommonlib.utils.ServiceUtils;
import com.desaysv.svmediaservicelogic.systemcontrol.ModeControl;

/**
 * Mode切源统一管理服务
 */
public class ModeChangeSourceService extends IntentService {

    private static final String TAG = "ModeChangeSourceService";

    private static final String DESAYSV_ACTION_CHANGE_SOURCE = "com.desaysv.action.changeSource";

    public ModeChangeSourceService() {
        super("modeChangeSource");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Andoird 8.0需要在服务启动之后通知其他启动为前台广播，不然会报异常
        ServiceUtils.startForegroundNotification(this, "ModeChangeSourceService", "ModeChangeSourceService");
        setIntentRedelivery(false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    /**
     * IntentService里面处理intent的回调
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        if (null == intent) {
            return;
        }
        final String action = intent.getAction();
        if (DESAYSV_ACTION_CHANGE_SOURCE.equals(action)) {
            //TODO：mode逻辑的实现，后面看是否能用放射实现
            ModeControl.getInstance().dealModeFunction(getApplicationContext());
        }
    }


}
