package com.desaysv.libusbmedia.bean;

/**
 * Created by LZM on 2019-7-6.
 * Comment 用来区分当前的控制器是音乐还是视频
 */
public enum MediaType {
    LOCAL_MUSIC,     //本地音乐
    USB1_MUSIC,     //USB1音乐
    USB2_MUSIC,     //USB2音乐
    USB1_VIDEO,     //USB1视频
    USB2_VIDEO,     //USB2视频
    RECENT_MUSIC,   //最近播放音乐
}
