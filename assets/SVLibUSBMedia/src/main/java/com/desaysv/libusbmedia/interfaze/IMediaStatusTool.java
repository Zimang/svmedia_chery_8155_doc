package com.desaysv.libusbmedia.interfaze;

/**
 * Created by LZM on 2019-8-20.
 * Comment 获取媒体状态的回调，这个接口只对infor提供数据，不对外提供
 */
public interface IMediaStatusTool {

    /**
     * 当前的播放状态
     *
     * @return true：播放中；false：暂停中
     */
    boolean isPlaying();

    /**
     * 当前播放的总时间
     *
     * @return Duration
     */
    int getDuration();

    /**
     * 当前播放的时间
     *
     * @return PlayTime
     */
    int getCurrentPlayTime();

}
