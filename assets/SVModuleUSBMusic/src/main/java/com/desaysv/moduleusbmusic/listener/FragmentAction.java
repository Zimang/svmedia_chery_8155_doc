package com.desaysv.moduleusbmusic.listener;

/**
 * @author uidq1846
 * @desc 页面切换动作回调枚举
 * @time 2022-11-18 10:01
 */
public enum FragmentAction {
    /**
     * 需要进入播放界面
     */
    TO_PLAY_FRAGMENT,

    /**
     * 需要退出播放页面
     */
    EXIT_PLAY_FRAGMENT,

    /**
     * 点击文件夹的条目
     */
    TO_USB_FOLDER_VIEW,

    /**
     * 退出文件夹列表页
     */
    EXIT_USB_FOLDER_VIEW
}
