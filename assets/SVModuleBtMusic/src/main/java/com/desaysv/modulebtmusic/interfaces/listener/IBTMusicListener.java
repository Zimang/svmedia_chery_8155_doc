package com.desaysv.modulebtmusic.interfaces.listener;

import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;

/**
 * 通用BTMusicListener接口类
 */
public interface IBTMusicListener {
    /**
     * 通知对应的协议连接
     */
    default void onProfileConnected() {
    }

    /**
     * 通知对应的协议连接
     */
    default void onProfileDisconnected() {
    }

    /**
     * 连接状态改变
     *
     * @param profile eg:Constants.ProfileType.A2DP_SINK {@link Constants.ProfileType}
     * @param address 蓝牙地址
     * @param state   连接状态 {@link Constants.ProfileConnectionState}
     */
    void onConnectionStateChanged(int profile, String address, int state);

    /**
     * 歌曲信息发生改变
     *
     * @param musicInfo 歌曲信息
     */
    void onMusicPlayInfoChanged(SVMusicInfo musicInfo);

    /**
     * 音乐播放状态改变
     *
     * @param state {@link BaseConstants.PlayState}
     */
    void onMusicPlayStateChanged(int state);

    /**
     * 播放进度改变
     *
     * @param progress 播放进度
     * @param duration 总时长
     */
    void onMusicPlayProgressChanged(long progress, long duration);

    /**
     * 播放列表改变（目前此接口只支持三星、iphone）
     */
    void onMusicPlayListChanged();
}
