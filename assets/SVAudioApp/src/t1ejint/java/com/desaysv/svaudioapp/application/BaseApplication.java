package com.desaysv.svaudioapp.application;

import android.app.Application;
import android.util.Log;

import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.Trigger.RemoveRecentTrigger;
import com.desaysv.mediacommonlib.base.AppBase;

public class BaseApplication extends AppBase {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BaseApplication","onCreate");
        //todo
        //初始化libRadio相关内容
        DABTrigger.getInstance().initialize();
        //各个Module的初始化
        ModuleRadioTrigger.getInstance().initialize();
        RemoveRecentTrigger.getInstance().init();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("BaseApplication","onTerminate");
        RemoveRecentTrigger.getInstance().deinit();
    }
}
