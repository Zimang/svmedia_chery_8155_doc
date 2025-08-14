package com.desaysv.svlibusbdialog.manager;

import com.desaysv.svlibusbdialog.observer.USB1QueryStateObserver;
import com.desaysv.svlibusbdialog.observer.USB2QueryStateObserver;

/**
 * 设置连接状态的管理器
 */
public class ScanStateManager {

    private static ScanStateManager mInstance;

    public static ScanStateManager getInstance(){
        if (mInstance == null){
            synchronized (ScanStateManager.class){
                if (mInstance == null){
                    mInstance = new ScanStateManager();
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
     * 设置usb1扫描状态
     * @param usb1State
     */
    public void setUsb1State(int usb1State) {
        this.usb1State = usb1State;
    }

    public int getUsb2State() {
        return usb2State;
    }

    /**
     * 设置usb2扫描状态
     * @param usb2State
     */
    public void setUsb2State(int usb2State) {
        this.usb2State = usb2State;
    }
}
