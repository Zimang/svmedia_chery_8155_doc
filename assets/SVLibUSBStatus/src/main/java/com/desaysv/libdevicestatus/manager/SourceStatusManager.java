package com.desaysv.libdevicestatus.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;

/**
 * Created by LZM on 2020-11-16
 * Comment 音源是否有效的状态类，主要是由于mode切换，语音控制
 * TODO：这里后面如果需要兼容蓝牙，carplay，carlife，可以加入
 *
 * @author uidp5370
 */
public class SourceStatusManager {

    private static final String TAG = "SourceStatusManager";

    private static SourceStatusManager instance;

    public static SourceStatusManager getInstance() {
        if (instance == null) {
            synchronized (SourceStatusManager.class) {
                if (instance == null) {
                    instance = new SourceStatusManager();
                }
            }
        }
        return instance;
    }

    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        USBProviderManager.getInstance().initialize(context);
        USBDeviceManager.getInstance().initialize(context);
    }


    /**
     * 获取设备对应的音源状态是否有效
     *
     * @param path {@link DeviceConstants.DevicePath}
     * @return true:设备对应的音源有效 false：设备对应的音源无效
     */
    public boolean getDeviceSourceEffect(String path) {
        Log.d(TAG, "getDeviceSourceEffect: path = " + path);
        boolean isEffect = false;
        if (TextUtils.isEmpty(path)) {
            return isEffect;
        }
        switch (path) {
            case DeviceConstants.DevicePath.USB0_PATH:
                isEffect = getUSB0SourceEffect();
                break;
            case DeviceConstants.DevicePath.USB1_PATH:
                isEffect = getUSB1SourceEffect();
                break;
        }
        Log.d(TAG, "getDeviceSourceEffect: isEffect = " + isEffect);
        return isEffect;
    }


    /**
     * 获取USB0的音源是否有效,USB音源有效的判断条件是，设备连接，且有音乐数据
     *
     * @return true：有效 false：无效
     */
    private boolean getUSB0SourceEffect() {
        boolean deviceStatus = DeviceStatusBean.getInstance().isUSB1Connect();
        boolean dataStatus = USBProviderManager.getInstance().getUSB0MusicListCount() > 0;
        Log.d(TAG, "getUSB0SourceEffect: deviceStatus = " + deviceStatus + " dataStatus = " + dataStatus);
        return deviceStatus & dataStatus;
    }


    /**
     * 获取USB1的音源是否有效,USB音源有效的判断条件是，设备连接，且有音乐数据
     *
     * @return true：有效 false：无效
     */
    private boolean getUSB1SourceEffect() {
        boolean deviceStatus = DeviceStatusBean.getInstance().isUSB2Connect();
        boolean dataStatus = USBProviderManager.getInstance().getUSB1MusicListCount() > 0;
        Log.d(TAG, "getUSB1SourceEffect: deviceStatus = " + deviceStatus + " dataStatus = " + dataStatus);
        return deviceStatus & dataStatus;
    }

}
