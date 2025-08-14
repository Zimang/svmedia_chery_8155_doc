package com.desaysv.svmediaservicelogic.bean;

import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by LZM on 2020-3-27
 * Comment 不同的音频焦点，根据不同的类型来进行区分
 * @author uidp5370
 */
public class AudioFocusTypeBean {

    private static final String TAG = "AudioSourceDataBean";


    /**
     * 默认的音源，即不需要音源恢复，也不需要特殊设置的媒体音源
     */
    public static final String DEFAULT_MEDIA_TYPE = "default_media_type";

    /**
     * 需要音源恢复的媒体音源
     */
    public static final String BOOT_RESUME_MEDIA_TYPE = "boot_resume_media_type";


    /**
     * 语音类型的音源
     */
    public static final String TTS_TYPE = "tts_type";


    /**
     * 讯飞音乐有的音频家都
     */
    private static final String[] BOOT_RESUME_MEDIA_SOURCES = {
            DsvAudioSDKConstants.BT_MUSIC_SOURCE,
            DsvAudioSDKConstants.AM_SOURCE,
            DsvAudioSDKConstants.FM_SOURCE,
            DsvAudioSDKConstants.DAB_SOURCE,
            DsvAudioSDKConstants.ONLINE_RADIO_SOURCE,
            DsvAudioSDKConstants.ONLINE_PROGRAM,
            DsvAudioSDKConstants.ONLINE_MUSIC_SOURCE,
            DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB2_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB3_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB4_MUSIC_SOURCE,
            DsvAudioSDKConstants.ONLINE_NEWS,
            DsvAudioSDKConstants.AI_MEDIA
    };


    /**
     * 讯飞语音有的音源
     */
    private static final String[] TTS_TYPE_SOURCES = {
            DsvAudioSDKConstants.TTS_SOURCE,
            DsvAudioSDKConstants.BT_PHONE_TTS,
            DsvAudioSDKConstants.VR_SOURCE
    };


    private static final List<String> BOOT_RESUME_MEDIA_SOURCES_LIST = Arrays.asList(BOOT_RESUME_MEDIA_SOURCES);

    private static final List<String> TTS_TYPE_SOURCES_LIST = Arrays.asList(TTS_TYPE_SOURCES);

    /**
     * 根据事情的音频焦点，来区分需要设置为什么类型的音频焦点
     *
     * @return String 媒体控制内的source
     */
    public static String getFocusTypeFormSource(String requestSource) {
        String focusType = DEFAULT_MEDIA_TYPE;
        if (!TextUtils.isEmpty(requestSource)) {
            if (BOOT_RESUME_MEDIA_SOURCES_LIST.contains(requestSource)) {
                focusType = BOOT_RESUME_MEDIA_TYPE;
            } else if (TTS_TYPE_SOURCES_LIST.contains(requestSource)) {
                focusType = TTS_TYPE;
            }
        }
        Log.d(TAG, "getFocusTypeFormSource: requestSource = " + requestSource + " focusType = " + focusType);
        return focusType;
    }

}
