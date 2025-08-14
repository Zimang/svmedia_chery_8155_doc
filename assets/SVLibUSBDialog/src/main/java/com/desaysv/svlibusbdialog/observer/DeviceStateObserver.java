package com.desaysv.svlibusbdialog.observer;


import com.desaysv.svlibusbdialog.iinterface.IDeviceStateChangedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询媒体库状态的观察者
 */
public class DeviceStateObserver {

    private final String TAG = this.getClass().getSimpleName();
    private static DeviceStateObserver mInstance;

    public static DeviceStateObserver getInstance(){
        if (mInstance == null){
            synchronized (DeviceStateObserver.class){
                if (mInstance == null){
                    mInstance = new DeviceStateObserver();
                }
            }
        }
        return mInstance;
    }

    private Map<String, IDeviceStateChangedListener> listenerMap = new HashMap<>();

    /**
     * 注册观察者
     * @param clkName
     */
    public void attachObserver(String clkName, IDeviceStateChangedListener listener){
        if (!listenerMap.containsKey(clkName)){
            listenerMap.put(clkName,listener);
        }

    }

    /**
     * 注销观察者
     * @param clkName
     */
    public void detachObserver(String clkName){
        listenerMap.remove(clkName);
    }

    /**
     * 通知观察者数据变化
     */
    public void notifyObserver(){
        for (IDeviceStateChangedListener listener : listenerMap.values()){
            listener.onDeviceStateUpdate();
        }
    }

}
