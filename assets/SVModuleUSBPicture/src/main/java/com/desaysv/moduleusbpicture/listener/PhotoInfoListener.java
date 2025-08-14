package com.desaysv.moduleusbpicture.listener;

/**
 * Created by LZM on 2020-4-4
 * Comment
 */
public interface PhotoInfoListener {

    /**
     * 幻灯片的播放状态发送改变
     * @param isPlaying true：播放 false：停止
     */
    void onPlayStatusChange(boolean isPlaying);

    /**
     * 幻灯片的切换时间发送改变
     * @param time 切换的时间
     */
    void onSlidesTimeChange(int time);


}
