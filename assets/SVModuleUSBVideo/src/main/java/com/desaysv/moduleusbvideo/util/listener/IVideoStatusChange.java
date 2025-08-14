package com.desaysv.moduleusbvideo.util.listener;

import com.desaysv.libusbmedia.bean.MediaType;

/**
 *
 * Create by extodc87 on 2023-8-23
 * Author: extodc87
 */
public interface IVideoStatusChange {

    /**
     * 播放状态发生变化时触发的回调
     *
     * @param mediaType 类型
     * @param isPlaying true：播放 false：暂停
     */
    void onVideoPlayStatus(MediaType mediaType, boolean isPlaying);
}
