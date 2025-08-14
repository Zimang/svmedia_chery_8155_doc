package com.desaysv.usbpicture.application;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.utils.DeviceUtils;
import com.desaysv.svlibusbdialog.dialog.SourceDialogUtil;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.usbpicture.service.KeepLiveService;
import com.desaysv.usbpicture.trigger.GearBoxControl;
import com.desaysv.usbpicture.trigger.PictureQueryTrigger;
import com.desaysv.usbpicture.trigger.PictureVRControl;
import com.desaysv.usbpicture.trigger.PointTrigger;
import com.desaysv.usbpicture.trigger.VDBRVCControl;

public class SVUSBPictureApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PictureVRControl.getInstance().unSubScribeVR();
        GearBoxControl.getInstance().unRegisterSpiListener();
        //反初始化埋点
        PointTrigger.getInstance().deInit(this);
    }

    private void init(){
        CarInfoProxy.getInstance().init(this);
        CarConfigUtil.getDefault().init(this);
        //初始化
        //USB设备的初始化，由于后续逻辑都需要根据设备状态来走的，所以设备状态的初始化必须在最前面
        DeviceStatusBean.getInstance().setUSB1Connect(
                DeviceUtils.checkUSBDevice(DeviceConstants.DevicePath.USB0_PATH));
        DeviceStatusBean.getInstance().setUSB2Connect(
                DeviceUtils.checkUSBDevice(DeviceConstants.DevicePath.USB1_PATH));
        startForegroundService(new Intent(this, KeepLiveService.class));

        PictureQueryTrigger.getInstance().initialize(this);
        VDBus.getDefault().init(this);
//        PictureVRControl.getInstance().subScribeVR(this);//初始化订阅语义监听//语音走可见即可说
        VDBRVCControl.getInstance().subScribeRVC(this);//初始化RVC监听

        GearBoxControl.getInstance().subScribeSpi(this);
        //初始化USB弹窗
        SourceDialogUtil.getInstance().initDialog(this,false,false);
        QueryStateManager.getInstance().init(this);

        //初始化埋点
        PointTrigger.getInstance().init(this);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        SourceDialogUtil.getInstance().reInitDialog(this);
        super.onConfigurationChanged(newConfig);
    }
}
