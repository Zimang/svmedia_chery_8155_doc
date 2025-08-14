package com.desaysv.libdevicestatus.listener;

/**
 * Created by LZM on 2020-11-16
 * Comment 设备连接状态的监听接口
 *
 * @author uidp5370
 */
public interface DeviceListener {

    /**
     * 设备状态变化时候，会触发回调
     *
     * @param path   设备的路径，包括了USB，蓝牙，CarPlay,CarLife
     * @param status true:连接，false：断开
     */
    void onDeviceStatusChange(String path, boolean status);
}
