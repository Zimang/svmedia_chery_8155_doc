package com.desaysv.libusbmedia.utils;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.libusbmedia.bean.MediaType;

/**
 * Created by LZM on 2020-3-24
 * Comment 在jar包提供了获取当前音频类型的媒体接口后又封装一层，以供USB媒体使用
 * @author uidp5370
 */
public class USBFocusUtils {

    private static final String TAG = "USBFocusUtils";

    private static USBFocusUtils instance;

    public static USBFocusUtils getInstance() {
        if (instance == null) {
            synchronized (USBFocusUtils.class) {
                if (instance == null) {
                    instance = new USBFocusUtils();
                }
            }
        }
        return instance;
    }

    private USBFocusUtils() {

    }


    /***
     * 当前的媒体类型是否有效
     * @param mediaType 媒体类型，不是音源
     * @return true：有效 false：无效
     */
    public boolean isCurrentMediaTypeSource(MediaType mediaType) {
        switch (mediaType) {
            case USB1_MUSIC:
                return isUSB1MusicSource();
            case USB1_VIDEO:
                return isUSB1VideoSource();
            case USB2_MUSIC:
                return isUSB2MusicSource();
            case USB2_VIDEO:
                return isUSB2VideoSource();
        }
        return false;
    }

    /**
     * 判断当前音频焦点是否是Local音乐
     *
     * @return true：是，false：不是
     */
    public boolean isLocalMusicSource() {
        return AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext).equals(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE);
    }

    /**
     * 频段当前音频焦点是否是USB1音乐
     *
     * @return true：是，false：不是
     */
    public boolean isUSB1MusicSource() {
        return AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext).equals(DsvAudioSDKConstants.USB0_MUSIC_SOURCE);
    }

    /**
     * 频段当前音频焦点是否是USB2音乐
     *
     * @return true：是，false：不是
     */
    public boolean isUSB2MusicSource() {
        return AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext).equals(DsvAudioSDKConstants.USB1_MUSIC_SOURCE);
    }

    /**
     * 频段当前音频焦点是否是USB1视频
     *
     * @return true：是，false：不是
     */
    public boolean isUSB1VideoSource() {
        return AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext).equals(DsvAudioSDKConstants.USB0_VIDEO_SOURCE);
    }

    /**
     * 频段当前音频焦点是否是USB2视频
     *
     * @return true：是，false：不是
     */
    public boolean isUSB2VideoSource() {
        return AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext).equals(DsvAudioSDKConstants.USB1_VIDEO_SOURCE);
    }


}
