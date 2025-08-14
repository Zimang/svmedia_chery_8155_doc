package com.desaysv.libdevicestatus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;


/**
 * @author uidp5370
 * @date 2019-6-3
 * USB设备状态变化的回调接口
 */

public class USBDeviceReceiver extends BroadcastReceiver {

    private static final String TAG = "USBDeviceReceiver";

    private final static String MEDIA_MOUNTED = Intent.ACTION_MEDIA_MOUNTED;
    private final static String MEDIA_UNMOUNTED = Intent.ACTION_MEDIA_UNMOUNTED;
    private final static String MEDIA_EJECT = Intent.ACTION_MEDIA_EJECT;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = " + action);
        assert action != null;
        switch (action) {
            case MEDIA_MOUNTED:
                String mountedPath = intent.getData().toString();
                Log.d(TAG, "onReceive: MEDIA_MOUNTED mountedPath = " + mountedPath);
                if (mountedPath.contains(DeviceConstants.DevicePath.USB0_PATH)) {
                    DeviceStatusBean.getInstance().setUSB1Connect(true);
                } else if (mountedPath.contains(DeviceConstants.DevicePath.USB1_PATH)) {
                    DeviceStatusBean.getInstance().setUSB2Connect(true);
                }
                break;
            case MEDIA_UNMOUNTED:
                String unMountPath = intent.getData().toString();
                Log.d(TAG, "onReceive: MEDIA_UNMOUNTED unMountPath = " + unMountPath);
                break;
            case MEDIA_EJECT:
                String ejectPath = intent.getData().toString();
                Log.d(TAG, "onReceive: MEDIA_EJECT ejectPath = " + ejectPath);
                if (ejectPath.contains(DeviceConstants.DevicePath.USB0_PATH)) {
                    DeviceStatusBean.getInstance().setUSB1Connect(false);
                } else if (ejectPath.contains(DeviceConstants.DevicePath.USB1_PATH)) {
                    DeviceStatusBean.getInstance().setUSB2Connect(false);
                }
                break;
        }
    }
}
