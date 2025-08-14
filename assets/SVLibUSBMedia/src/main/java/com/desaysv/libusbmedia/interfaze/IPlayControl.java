package com.desaysv.libusbmedia.interfaze;

import android.net.Uri;

import com.desaysv.libusbmedia.bean.MediaAction;


/**
 * Created by uidp5370 on 2019-3-4.
 * 媒体播放的控制接口，只对controlTool提供，不对外提供
 */

public interface IPlayControl {

    /**
     * 打开音频
     */
    void openMedia(Uri path);

    /**
     * 释放掉MediaPlayer
     */
    void stop();

    /**
     * 释放掉媒体焦点，并且 释放MediaPlayer
     */
    void release();

    /**
     * 播放音频
     */
    void start();

    /**
     * 停止播放
     */
    void pause();


    /**
     * 跳转
     *
     * @param position
     */
    void seekTo(int position);

    /**
     * 自动跳转，快进快退
     *
     * @param action 快进或者快退
     */
    void autoSeek(MediaAction action);

    /**
     * 搜索音乐
     *
     * @param type 搜索的类型
     * @param Data 搜索的关键字
     * @param path 搜索的路径
     */
    void searchMusicList(String type, String Data, String path);


    /**
     * 设置视频的播放速率
     *
     * @param speed speed
     */
    void setPlaySpeed(float speed);
}
