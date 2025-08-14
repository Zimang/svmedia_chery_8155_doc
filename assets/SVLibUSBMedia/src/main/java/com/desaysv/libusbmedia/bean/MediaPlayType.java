package com.desaysv.libusbmedia.bean;

/**
 * Created by uidp5370 on 2019-6-14.
 * 枚举，判断媒体文件的状态，通知界面更新UI
 */

public enum MediaPlayType {

    NORMAL,          //正常的打开
    OPENING,         //打开中，给视频黑屏用
    ERROR,           //打开异常
    NO_VIDEO         //打开的视频文件没有视频流（只针对视频）
}
