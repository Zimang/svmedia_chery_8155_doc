package com.desaysv.libdevicestatus.bean;

import android.util.Log;

import com.desaysv.libdevicestatus.listener.DeviceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by LZM on 2020-11-16
 * Comment 设备状态的统筹类，然后里面实现了数据状态的监听，实现了观察者模式
 * TODO：如果项目上需要蓝牙，CarLife,CarPlay的设备状态监听，也可以加入这里
 *
 * @author uidp5370
 */
public class DeviceStatusBean {

    private static final String TAG = "DeviceStatusBean";

    private static DeviceStatusBean instance;

    public static DeviceStatusBean getInstance() {
        if (instance == null) {
            synchronized (DeviceStatusBean.class) {
                if (instance == null) {
                    instance = new DeviceStatusBean();
                }
            }
        }
        return instance;
    }

    /**
     * 进行加锁处理
     */
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    /**
     * 读锁，排斥写操作
     */
    private final Lock readLock = mReentrantLock.readLock();
    /**
     * 写锁，排斥读与写操作
     */
    private final Lock writeLock = mReentrantLock.writeLock();

    /**
     * 设备状态的变化回调列表
     */
    private final List<DeviceListener> mDeviceListeners = new ArrayList<>();

    /**
     * 添加设备状态的监听回调
     *
     * @param deviceListener mDeviceListeners
     */
    public void addDeviceListener(DeviceListener deviceListener) {
        writeLock.lock();
        try {
            if (deviceListener != null) {
                mDeviceListeners.remove(deviceListener);
                mDeviceListeners.add(deviceListener);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 移除设备状态的监听回调
     *
     * @param deviceListener deviceListener
     */
    public void removeDeviceListener(DeviceListener deviceListener) {
        writeLock.lock();
        try {
            if (deviceListener != null) {
                mDeviceListeners.remove(deviceListener);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * USB0设备是否连接
     */
    private boolean isUSB1Connect = false;

    /**
     * USB1设备是否连接
     */
    private boolean isUSB2Connect = false;


    /**
     * 设置USB0的连接状态
     *
     * @param USB1Connect true：连接 false：未连接
     */
    public void setUSB1Connect(boolean USB1Connect) {
        Log.d(TAG, "setUSB1Connect: US0Connect = " + USB1Connect);
        isUSB1Connect = USB1Connect;
        updateDeviceStatus(DeviceConstants.DevicePath.USB0_PATH, isUSB1Connect);
    }

    /**
     * 获取USB0的连接状态
     *
     * @return Boolean true：连接 false：未连接
     */
    public boolean isUSB1Connect() {
        Log.d(TAG, "isUSB1Connect: isUSB1Connect = " + isUSB1Connect);
        return isUSB1Connect;
    }

    /**
     * 设置USB1的连接状态
     *
     * @param USB2Connect true：连接 false：未连接
     */
    public void setUSB2Connect(boolean USB2Connect) {
        Log.d(TAG, "setUSB2Connect: USB1Connect = " + USB2Connect);
        isUSB2Connect = USB2Connect;
        updateDeviceStatus(DeviceConstants.DevicePath.USB1_PATH, isUSB2Connect);
    }

    /**
     * 获取USB1的连接状态
     *
     * @return Boolean true：连接 false：未连接
     */
    public boolean isUSB2Connect() {
        Log.d(TAG, "isUSB2Connect: isUSB2Connect = " + isUSB2Connect);
        return isUSB2Connect;
    }

    /**
     * 更新设备状态，并且触发回调
     *
     * @param path   路径
     * @param status true：连接 false：未连接
     */
    private void updateDeviceStatus(String path, boolean status) {
        Log.d(TAG, "updateDeviceStatus: path = " + path + " status = " + status);
        readLock.lock();
        try {
            for (DeviceListener deviceListener : mDeviceListeners) {
                deviceListener.onDeviceStatusChange(path, status);
            }
        } finally {
            readLock.unlock();
        }
    }

}
