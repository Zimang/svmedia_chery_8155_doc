package com.desaysv.libusbmedia.interfaze;


import com.desaysv.libusbmedia.bean.MediaPlayType;

/**
 * @author uidp5370
 * @date 2019-6-12
 */

public interface IMediaStatusChange {

    /**
     * ID3信息发送改变的回调
     */
    void onMediaInfoChange();

    /**
     * 播放状态发生变化时触发的回调
     *
     * @param isPlaying true：播放 false：暂停
     */
    void onPlayStatusChange(boolean isPlaying);

    /**
     * 播放时间发生改变时触发的回调
     *
     * @param currentPlayTime 当前的播放时间
     * @param duration        总的播放时间
     */
    void onPlayTimeChange(int currentPlayTime, int duration);

    /**
     * 媒体播放的类型发送改变的回调
     *
     * @param mediaPlayType NORMAL,          //正常的打开
     *                      OPENING,         //打开中，给视频黑屏用
     *                      ERROR,           //打开异常
     *                      NO_VIDEO         //打开的视频文件没有视频流（只针对视频）
     */
    void onMediaTypeChange(MediaPlayType mediaPlayType);

    /**
     * 媒体的专辑图片数据发生改变的时候
     */
    void onAlbumPicDataChange();

    /**
     * 循环模式发送改变的回调
     */
    void onLoopTypeChange();

    /**
     * 歌词文件发生改变
     */
    void onLyricsChange();

    /**
     * 当前播放列表发生变化
     */
    void onPlayListChange();
}
