package com.desaysv.mediasdk.bean;

import android.text.TextUtils;
import android.util.Log;


import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by LZM on 2019-12-13
 * Comment 将每个音频焦点按不同的应用来区分
 */
public class AudioSourceOriginBean {
    private static final String TAG = "AudioSourceDataBean";

    /**
     * 没有对应的音源，也就是默认值
     */
    public static final String NO_ORIGIN = "no_origin";

    /**
     * 电台的origin，用在媒体控制器里面来区分source
     */
    public static final String RADIO_ORIGIN = "radio_origin";

    /**
     * 音乐的origin，用在媒体控制器里面来区分source
     */
    public static final String MUSIC_ORIGIN = "music_origin";

    /**
     * 音乐有的音频家都
     */
    private static final String[] MUSIC_SOURCES = {
            DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
    };

    /**
     * 电台有的音源
     */
    private static final String[] RADIO_SOURCES = {
            DsvAudioSDKConstants.FM_SOURCE,
            DsvAudioSDKConstants.AM_SOURCE,
    };

    private static final List<String> MUSIC_SOURCES_LIST = Arrays.asList(MUSIC_SOURCES);

    private static final List<String> RADIO_SOURCE_LIST = Arrays.asList(RADIO_SOURCES);

    /**
     * 将音源类型安装上诉集合转换为对应的媒体控制器内的source,这里只检测媒体类型的source
     *
     * @return String 媒体控制内的source
     */
    public static String transformSource(String currentSource) {
        String controlSource = NO_ORIGIN;
        if (!TextUtils.isEmpty(controlSource)) {
            if (MUSIC_SOURCES_LIST.contains(currentSource)) {
                controlSource = MUSIC_ORIGIN;
            } else if (RADIO_SOURCE_LIST.contains(currentSource)) {
                controlSource = RADIO_ORIGIN;
            }
        }
        Log.d(TAG, "transformSource: currentSource = " + currentSource + " controlSource = " + controlSource);
        return controlSource;
    }
}
