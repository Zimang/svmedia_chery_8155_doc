package com.desaysv.moduledab.trigger;

import android.content.Intent;

import com.desaysv.libradio.control.RadioControlRegister;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduledab.service.DABPopupService;
import com.desaysv.moduledab.utils.ProductUtils;

/**
 * created by ZNB on 2022-10-14
 * 模块触发器，Application启动时，调用这个进行模块的初始化
 */
public class DABTrigger {

    private static DABTrigger instance;

    public IGetControlTool mGetControlTool;
    public IControlTool mRadioControl;
    public IStatusTool mRadioStatusTool;

    public static DABTrigger getInstance(){
        if (instance == null){
            synchronized (DABTrigger.class){
                if (instance == null){
                    instance = new DABTrigger();
                }
            }
        }
        return instance;
    }

    public void initialize(){
        //注册收音的控制器
        mGetControlTool = RadioControlRegister.getInstance().registeredRadioTool();
        //获取收音的控制器
        mRadioControl = mGetControlTool.getControlTool();
        //获取收音的状态获取器
        mRadioStatusTool = mGetControlTool.getStatusTool();
        if (ProductUtils.hasDAB()){
            AppBase.mContext.startService(new Intent(AppBase.mContext, DABPopupService.class));
        }

        //初始化埋点
        PointTrigger.getInstance().init(AppBase.mContext);
    }
}
