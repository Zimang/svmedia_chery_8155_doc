package com.desaysv.libdevicestatus.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by LZM on 2020-11-16
 * Comment 设备状态使用的工具类
 *
 * @author uidp5370
 */
public class DeviceUtils {

    private static final String TAG = "DeviceUtils";

    /**
     * 检测当前设备是否有USB1的设备
     *
     * @return true：有；false：无
     */
    public static boolean checkUSBDevice(String usbPath) {
        Log.d(TAG, "checkUSBDevice: usbPath = " + usbPath);
        String state = Environment.getExternalStorageState(new File(usbPath));
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(TAG, "checkUSBDevice: MOUNTED");
            return true;
        }
        Log.d(TAG, "checkUSBDevice: UNMOUNTED");
        return false;
    }

}
