package com.desaysv.mediaservice.application;


import android.util.Log;


import com.desaysv.mediaservice.manager.KeyControlManager;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.svmediaservicelogic.MediaControlTrigger;
import com.desaysv.svmediaservicelogic.utils.AudioFocusManager;

/**
 * Created by LZM on 2019-10-14
 * Comment Media对外提供的SDK，现在只是实现USB设备状态的获取。后面会实现音频焦点的申请，以及音源恢复，mode键的实现等对外提供的功能
 */
public class SVMediaServiceApplication extends AppBase {

    private static final String TAG = "SVMediaServiceApplicati";

    private static final String RESUME_SERVICE_NAME = "com.desaysv.mediaservice.service.BootResumeSourceService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        //媒体工具类的初始化
        AudioFocusManager.getInstance().initialize(getApplicationContext(),RESUME_SERVICE_NAME);
        //按键控制的初始化
        KeyControlManager.getInstance().initialize(getApplicationContext());
        //触发一些外部的媒体控制器
        //TODO:这部分是与业务逻辑管理的地方，后面看是否用反射的方法来实现
        MediaControlTrigger.getInstance().startTrigger(getApplicationContext());
    }
}
