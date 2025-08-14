package com.desaysv.svmediaservicelogic;

import android.content.Context;
import android.util.Log;

import com.desaysv.libdevicestatus.manager.SourceStatusManager;
import com.desaysv.mediasdk.manager.DsvSourceMediaManager;
import com.desaysv.svmediaservicelogic.manager.AudioSourceManager;

/**
 * Created by LZM on 2020-3-18
 * Comment 媒体启动的触发器，媒体服务启动的时候，就调用这个trigger，然后进行远程AIDL连接的初始化，后面看是否能用反射的方式实现
 * @author uidp5370
 */
public class MediaControlTrigger {

    private static final String TAG = "MediaControlTrigger";

    private static MediaControlTrigger instance;


    public static MediaControlTrigger getInstance() {
        if (instance == null) {
            synchronized (MediaControlTrigger.class) {
                if (instance == null) {
                    instance = new MediaControlTrigger();
                }
            }
        }
        return instance;
    }

    private MediaControlTrigger() {

    }

    /**
     * 触发器，触发外部SDK的全部初始化
     */
    public void startTrigger(Context context) {
        Log.d(TAG, "startTrigger: ");
        //初始化设备状态
        SourceStatusManager.getInstance().initialize(context);
        //初始化音源控制器
        DsvSourceMediaManager.getInstance().initialize(context);
        //初始化系统音频焦点的监听器
        AudioSourceManager.getInstance().initialize(context);
    }

}
