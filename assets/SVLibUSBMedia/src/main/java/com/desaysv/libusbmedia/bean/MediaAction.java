package com.desaysv.libusbmedia.bean;

/**
 *
 * @author uidp5370
 * @date 2019-3-4
 * 媒体的控制动作
 */

public enum MediaAction {
    /**
     * 打开媒体文件
     */
    OPEN,
    /**
     * 暂停或者播放
     */
    PLAY_OR_PAUSE,
    /**
     * 播放
     */
    START,
    /**
     * 暂停
     */
    PAUSE,
    /**
     * 停止
     */
    STOP,
    /**
     * 释放，停止加释放音频焦点
     */
    RELEASE,
    /**
     * 下一曲
     */
    NEXT,
    /**
     * 上一曲
     */
    PRE,
    /**
     * 快进
     */
    FAST_FORWARD,
    /**
     * 快退
     */
    REWIND,
    /**
     * 停止快进
     */
    FAST_FORWARD_STOP,
    /**
     * 停止快退
     */
    REWIND_STOP,
    /**
     * 跳转
     */
    SEEKTO,
    /**
     * 切换循环模式
     */
    CHANGE_LOOP_TYPE,
    /**
     * 切换为循环模式
     */
    CYCLE,
    /**
     * 切换为单曲循环
     */
    SINGLE,
    /**
     * 切换为随机播放
     */
    RANDOM
}
