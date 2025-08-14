package com.desaysv.localmediasdk.sdk.listener;

import com.desaysv.localmediasdk.bean.RemoteBeanList;

/**
 * Created by LZM on 2020-3-18
 * Comment 数据变化的时候，SDK里面会触发这个回调，然后将数据变化回馈给SVMediaService里面注册了这个接口的对象
 */
public interface IMediaInfoCallBack {

    /**
     * 媒体信息发生了改变，变化的是ID3
     *
     * @param source    变化的音源
     * @param mediaInfo 变化消息的json
     */
    void onMediaInfoChange(String source, String mediaInfo);

    /**
     * 专辑图片发生改变的时候触发的回调
     *
     * @param source  变化的音源
     * @param picInfo 变化的图片数据
     */
    void onMediaPicChange(String source, byte[] picInfo);

    /**
     * 媒体的播放状态发生改变时会触发回调
     *
     * @param source   变化的音源
     * @param playInfo 变化数据的json
     */
    void onMediaPlayStatusChange(String source, String playInfo);

    /**
     * 媒体的播放时间发生改变的时候会触发回调
     *
     * @param source   变化的音源
     * @param timeInfo 变化消息的json
     */
    void onMediaPlayTimeChange(String source, String timeInfo);

    /**
     * 电台的收藏状态发送改变会触发的回调
     *
     * @param source      变化的音源
     * @param collectInfo 变化消息的json数据
     */
    void onRadioCollectStatusChange(String source, String collectInfo);

    /**
     * USB音乐的循环模式发生改变触发的回调
     *
     * @param source   变化的音源
     * @param loopInfo 变化的循环模式
     */
    void onMusicLoopTypeChange(String source, String loopInfo);

    /**
     * 通知应用服务绑定成功了，绑定成功之后，自己去获取去全部的播放信息
     */
    void onServiceConnect(String source);

    /**
     * 通知应用服务绑定成功了，绑定成功之后，自己去获取去全部的播放信息
     */
    void onServiceDisConnect(String source);

    /**
     * 媒体的搜索状态发生改变时会触发回调
     *
     * @param source     变化的音源
     * @param searchInfo 变化数据的json
     */
    void onMediaSearchStatusChange(String source, String searchInfo);

    /**
     * 媒体的Seek状态发生改变时会触发回调
     *
     * @param source   变化的音源
     * @param seekInfo 变化数据的json
     */
    void onMediaSeekStatusChange(String source, String seekInfo);

    /**
     * 媒体的设置状态发生改变时会触发回调
     *
     * @param source       变化的音源
     * @param settingsInfo 变化数据的json
     */
    void onMediaSettingsChange(String source, String settingsInfo);

    /**
     * 媒体的设备连接状态
     *
     * @param source 变化的音源
     * @param info   变化数据的json
     */
    void onMediaDeviceStatusChange(String source, String info);

    /**
     * 媒体数据列表变化
     *
     * @param source
     * @param type
     * @param remoteList
     */
    void onRemoteListChanged(String source, String type, RemoteBeanList remoteList);
}
