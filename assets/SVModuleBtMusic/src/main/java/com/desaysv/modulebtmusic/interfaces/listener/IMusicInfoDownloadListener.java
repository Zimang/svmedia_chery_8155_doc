package com.desaysv.modulebtmusic.interfaces.listener;

import com.desaysv.modulebtmusic.bean.SVMusicInfo;

import java.util.List;

/**
 * 通用蓝牙音乐播放列表的监听器
 */
public interface IMusicInfoDownloadListener {

    /**
     * @param musicInfoList 蓝牙音乐播放列表
     * @param index         从0开始
     * @param total         播放列表的音乐总数
     */
    void onDownloadProgress(List<SVMusicInfo> musicInfoList, int index, int total);

}
