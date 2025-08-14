package com.desaysv.svlibusbdialog.manager;

import com.desaysv.svlibusbdialog.observer.USB1DeviceStateObserver;
import com.desaysv.svlibusbdialog.observer.USB2DeviceStateObserver;

/**
 * 设置连接状态的管理器
 */
public class DeviceStateManager {

    private static DeviceStateManager mInstance;

    public static DeviceStateManager getInstance(){
        if (mInstance == null){
            synchronized (DeviceStateManager.class){
                if (mInstance == null){
                    mInstance = new DeviceStateManager();
                }
            }
        }
        return mInstance;
    }

    private int usb1State = -1;
    private int usb2State = -1;


    public int getUsb1State() {
        return usb1State;
    }

    /**
     * 设置usb1连接状态
     * @param usb1State
     */
    public void setUsb1State(int usb1State) {
        this.usb1State = usb1State;
        USB1DeviceStateObserver.getInstance().notifyObserver();
    }

    public int getUsb2State() {
        return usb2State;
    }

    /**
     * 设置usb2连接状态
     * @param usb2State
     */
    public void setUsb2State(int usb2State) {
        this.usb2State = usb2State;
        USB2DeviceStateObserver.getInstance().notifyObserver();
    }
}
