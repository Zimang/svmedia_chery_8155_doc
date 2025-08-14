package com.desaysv.audiosdk.utils;

import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by LZM on 2020-8-19
 * Comment 用来识别音源是那种类型的工具类
 *
 * @author uidp5370
 */
public class SourceTypeUtils {

    private static final String TAG = "SourceTypeUtils";

    /**
     * 媒体的音源类型
     */
    public static final String MEDIA_TYPE = "media_type";

    /**
     * 语音类型的音频焦点
     */
    public static final String TTS_TYPE = "tts_type";

    /**
     * 其他类型的音频焦点
     */
    public static final String OTHER_TYPE = "other_type";


    /**
     * 媒体类型的音源
     */
    private static final String[] MEDIA_TYPE_SOURCES = {
            DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB2_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB3_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB0_VIDEO_SOURCE,
            DsvAudioSDKConstants.USB1_VIDEO_SOURCE,
            DsvAudioSDKConstants.USB2_VIDEO_SOURCE,
            DsvAudioSDKConstants.USB3_VIDEO_SOURCE,
            DsvAudioSDKConstants.BT_MUSIC_SOURCE,
            DsvAudioSDKConstants.AM_SOURCE,
            DsvAudioSDKConstants.FM_SOURCE,
            DsvAudioSDKConstants.DAB_SOURCE,
            DsvAudioSDKConstants.CARPALY_MUSIC_SOURCE,
            DsvAudioSDKConstants.CARLIFE_MUSIC_SOURCE,
            DsvAudioSDKConstants.ONLINE_MUSIC_SOURCE,
            DsvAudioSDKConstants.ONLINE_RADIO_SOURCE,
            DsvAudioSDKConstants.ONLINE_PROGRAM,
            DsvAudioSDKConstants.ONLINE_NEWS,
            DsvAudioSDKConstants.AI_MEDIA,
            DsvAudioSDKConstants.IFLYTEK_GAME
    };


    /**
     * TTS类型的音源
     */
    private static final String[] TTS_TYPE_SOURCES = {
            DsvAudioSDKConstants.TTS_SOURCE,
            DsvAudioSDKConstants.BT_PHONE_TTS,
            DsvAudioSDKConstants.VR_SOURCE
    };


    /**
     * 媒体类型的音源列表
     */
    private static final List<String> MEDIA_TYPE_SOURCES_LIST = Arrays.asList(MEDIA_TYPE_SOURCES);

    /**
     * 语音类型的音源列表
     */
    private static final List<String> TTS_TYPE_SOURCES_LIST = Arrays.asList(TTS_TYPE_SOURCES);


    /**
     * 根据音源类型来获取音源焦点的类型
     *
     * @param source 需要判断类型的音源
     * @return 音源类型
     */
    public static String getFocusTypeFormSource(String source) {
        String focusType = OTHER_TYPE;
        if (!TextUtils.isEmpty(source)) {
            if (TTS_TYPE_SOURCES_LIST.contains(source)) {
                focusType = TTS_TYPE;
            } else if (MEDIA_TYPE_SOURCES_LIST.contains(source)) {
                focusType = MEDIA_TYPE;
            }
        }
        Log.d(TAG, "getFocusTypeFormSource: requestSource = " + source + " focusType = " + focusType);
        return focusType;
    }

}
