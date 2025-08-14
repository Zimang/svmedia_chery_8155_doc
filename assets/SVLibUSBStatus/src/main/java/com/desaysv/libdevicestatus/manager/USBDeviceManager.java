package com.desaysv.libdevicestatus.manager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.receiver.USBDeviceReceiver;
import com.desaysv.libdevicestatus.utils.DeviceUtils;

/**
 * Created by LZM on 2020-11-16
 * Comment USB设备状态的管理类
 *
 * @author uidp5370
 */
public class USBDeviceManager {

    private static final String TAG = "USBDeviceManager";

    private static USBDeviceManager instance;

    public static USBDeviceManager getInstance() {
        if (instance == null) {
            synchronized (USBDeviceManager.class) {
                if (instance == null) {
                    instance = new USBDeviceManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化，USB设备状态
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        initUSBDeviceReceiver(context);
        initDeviceStatus();
    }


    /**
     * 初始化收音状态的监听广播
     *
     * @param context 上下文
     */
    private void initUSBDeviceReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        intentFilter.setPriority(1000);
        context.registerReceiver(new USBDeviceReceiver(), intentFilter);
    }

    /**
     * 启动的时候，先初始化一下USB的设备状态,在注册回调之后，初始化设备状态，可以保证设备状态的准确性
     */
    private void initDeviceStatus() {
        Log.d(TAG, "initDeviceStatus: ");
        DeviceStatusBean.getInstance().setUSB1Connect(
                DeviceUtils.checkUSBDevice(DeviceConstants.DevicePath.USB0_PATH));
        DeviceStatusBean.getInstance().setUSB2Connect(
                DeviceUtils.checkUSBDevice(DeviceConstants.DevicePath.USB1_PATH));
    }

}
