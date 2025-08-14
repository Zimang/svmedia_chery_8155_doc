package com.desaysv.svaudioapp.application;

import android.app.Application;
import android.util.Log;

import com.desaysv.libdevicestatus.manager.SourceStatusManager;
import com.desaysv.modulebtmusic.manager.ModuleBtMusicTrigger;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.mediacommonlib.base.AppBase;

public class BaseApplication extends AppBase {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BaseApplication","onCreate");
        //todo
        //初始化libRadio相关内容
        DABTrigger.getInstance().initialize();
        //USB设备的初始化，由于后续逻辑都需要根据设备状态来走的，所以设备状态的初始化必须在最前面
        SourceStatusManager.getInstance().initialize(AppBase.mContext);
        //各个Module的初始化
        ModuleRadioTrigger.getInstance().initialize();
        ModuleUSBMusicTrigger.getInstance().initialize();
        ModuleBtMusicTrigger.getInstance().initialize(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
