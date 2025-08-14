package com.desaysv.libusbmedia.interfaze;

import android.media.MediaPlayer;

/**
 * Created by uidp5370 on 2019-6-12.
 * 注册媒体播放器的接口，这个接口在应用调用register的时候就需要传入实例，供库获取应用提供的mediaplayer
 */

public interface IRequestMediaPlayer {

    /**
     * 请求meidiaplayer
     *
     * @return MediaPlayer
     */
    MediaPlayer requestMediaPlayer();

    /**
     * 注销MediaPlayer
     */
    void destroyMediaPlayer();

}
