package com.desaysv.moduledab.common;

/**
 * created by ZNB on 2022-10-17
 * 定义统一的消息事件定义等
 */
public class DABMsg {
    public static final int MSG_UPDATE_RADIO = 0;//更新当前电台
    public static final int MSG_UPDATE_PLAY_STATUES = 1;//更新当前播放状态
    public static final int MSG_UPDATE_DAB_EFFECT_LIST = 2;//更新DAB有效列表
    public static final int MSG_UPDATE_DAB_COLLECT_LIST = 3;//更新DAB收藏列表
    public static final int MSG_CLICK_DAB = 4;//点击列表某个DAB时的消息，用于消抖
    public static final int MSG_COLLECT_DAB = 5;//点击列表某个DAB收藏时的消息，用于消抖
    public static final int MSG_UPDATE_SEARCH = 6;//更新当前搜索状态
    public static final int MSG_SCAN_TIMEOUT = 7;//搜索超时
    public static final int MSG_UPDATE_LOGO = 8;//更新Logo

    public static final int MSG_NO_SIGNAL_TIMEOUT = 9;//No_Signal自动消失
    public static final int NO_SIGNAL_TIMEOUT = 3000;//No_Signal自动消失

    public static final int MSG_NO_SCROLL = 10;//无滚动操作
    public static final int NO_SCROLL_TIMEOUT = 5000;//无滚动操作5s后才允许自动跳转列表位置
}
