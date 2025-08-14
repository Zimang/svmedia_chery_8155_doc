package com.desaysv.audiosdk.listener;


import com.desaysv.audiosdk.IAudioFocusManager;

/**
 * created by lzm on 2019-10-11
 * purpose: 服务的连接回调
 */
public interface DsvServiceConnectInterface {

    /**
     * 服务连接成功
     *
     * @param audioFocusManager
     */
    void onServiceConnect(IAudioFocusManager audioFocusManager);

    /**
     * 服务连接失败
     */
    void onServiceDisConnect();

}
