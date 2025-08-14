package com.desaysv.moduleradio.Trigger;

import android.content.Intent;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.libradio.control.RadioControlRegister;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.hardkey.MediaKeyActionManager;
import com.desaysv.moduleradio.mediasession.RadioMediaSessionController;
import com.desaysv.moduleradio.service.RadioPopupService;
import com.desaysv.moduleradio.utils.RadioStatusUtils;
import com.desaysv.moduleradio.vr.SVRadioVRControl;
import com.desaysv.svlibmediaobserver.manager.MediaObserverManager;


/**
 * Created by LZM on 2019-7-10.
 * Comment 收音module的启动器，收音module的一切初始化都是从这里开始的
 */

public class ModuleRadioTrigger {

    private static final String TAG = "ModuleRadioApplication";

    private static ModuleRadioTrigger instance;

    public static ModuleRadioTrigger getInstance() {
        if (instance == null) {
            synchronized (ModuleRadioTrigger.class) {
                if (instance == null) {
                    instance = new ModuleRadioTrigger();
                }
            }
        }
        return instance;
    }

    private ModuleRadioTrigger() {

    }

    //收音的控制器
    public IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    public IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    public IGetControlTool mGetControlTool;

    public void initialize() {
        Log.d(TAG, "onCreate: ");
        //注册收音的控制器
        mGetControlTool = RadioControlRegister.getInstance().registeredRadioTool();
        //获取收音的控制器
        mRadioControl = mGetControlTool.getControlTool();
        //获取收音的状态获取器
        mGetRadioStatusTool = mGetControlTool.getStatusTool();
        //媒体数据检测状态初始化，专门给SVMediaService使用
        RadioStatusUtils.getInstance().initialize();
        if (ProductUtils.hasRDS()){
            AppBase.mContext.startService(new Intent(AppBase.mContext, RadioPopupService.class));
        }
        //初始语音控制器
        SVRadioVRControl.getInstance().init();

        //初始化媒体按键控制器
        MediaKeyActionManager.getInstance().init(AppBase.mContext);

        //初始化VDB监听器
        SVVDBControl.getInstance().init(AppBase.mContext);

        //初始化埋点
        PointTrigger.getInstance().init(AppBase.mContext);

        MediaObserverManager.getInstance().init(AppBase.mContext);

        //初始化RadioMediaSession
        RadioMediaSessionController.getInstance().init();

        SPUtlis.getInstance().initialize(AppBase.mContext);
    }

    //增加这个接口给MainActivity的埋点使用
    public boolean isPlaying(){

        return mGetRadioStatusTool != null && mGetRadioStatusTool.isPlaying();
    }
}
