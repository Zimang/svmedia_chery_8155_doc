package com.desaysv.moduleusbvideo.util;

import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;

/**
 * 用于转接，当前视频播放的状态信息
 * 语音、MediaSession
 * Create by extodc87 on 2023-8-23
 * Author: extodc87
 */
public class MediaStatusChangeListenerImpl implements IMediaStatusChange {
    private static final String TAG = "MediaStatusChangeListenerImpl";

    private MediaType mediaType;

    public MediaStatusChangeListenerImpl(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public void onMediaInfoChange() {

    }

    @Override
    public void onPlayStatusChange(boolean isPlaying) {
        Log.d(TAG, "onPlayStatusChange() called with: mediaType = [" + mediaType + "], isPlaying = [" + isPlaying + "]");
        VideoStatusChangeUtil.getInstance().onVideoPlayStatus(mediaType, isPlaying);
    }

    @Override
    public void onPlayTimeChange(int currentPlayTime, int duration) {

    }

    @Override
    public void onMediaTypeChange(MediaPlayType mediaPlayType) {

    }

    @Override
    public void onAlbumPicDataChange() {

    }

    @Override
    public void onLoopTypeChange() {

    }

    @Override
    public void onLyricsChange() {

    }

    @Override
    public void onPlayListChange() {

    }
}
