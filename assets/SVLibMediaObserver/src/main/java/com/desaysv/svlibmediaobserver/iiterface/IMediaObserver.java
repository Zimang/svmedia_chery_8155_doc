package com.desaysv.svlibmediaobserver.iiterface;

import com.desaysv.svlibmediaobserver.bean.MediaInfoBean;

/**
 * created by znb on 2023-06-15，媒体信息的回调
 */
public interface IMediaObserver {

    /**
     * 界面变化的回调
     * @param pageFlag，界面索引{@link com.desaysv.svlibmediaobserver.bean.AppConstants}
     */
    default void onPageChanged(int pageFlag){

    }

    /**
     * 播放状态的回调
     * @param source，是哪个音源
     * @param isPlaying，是否播放
     */
    default void onPlayStatusChanged(String source, boolean isPlaying){

    }

    /**
     * 专辑变化的回调
     * @param source，是哪个音源
     * @param uri，专辑地址
     */
    default void onAlbumChanged(String source,String uri){

    }

    /**
     * 专辑变化的回调
     * @param source，是哪个音源
     * @param bytes，专辑数据，主要是给DAB用的
     */
    default void onAlbumChanged(String source,byte[] bytes){

    }


    /**
     * 媒体信息的回调
     * @param source，是哪个音源
     * @param mediaInfoBean，封装的媒体信息
     */
    default void onMediaInfoChanged(String source,MediaInfoBean mediaInfoBean){

    }

}
