package com.desaysv.localmediasdk.listener;

/**
 * Created by LZM on 2020-3-18
 * Comment 服务连接状态变化的回调
 */
public interface IServiceConnectCallback {

    /**
     * 服务连接成功
     */
    void onServiceConnect();

    /**
     * 服务断开连接
     */
    void onServiceDisConnect();

}
