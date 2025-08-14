package com.desaysv.svmediaservicelogic.utils;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.ArrayMap;
import android.util.Log;



/**
 *
 * @author LZM
 * @date 2019-7-11
 * Comment 音频焦点的申请类，将平台的焦点申请逻辑尽可能的封装为原生类型的
 * 这个类只有在服务中可用，用来申请音频寄到
 */
public class AudioFocusManager {

    private static final String TAG = "AudioFocusManager";

    private static AudioFocusManager instance;

    public static AudioFocusManager getInstance() {
        if (instance == null) {
            synchronized (AudioFocusManager.class) {
                if (instance == null) {
                    instance = new AudioFocusManager();
                }
            }
        }
        return instance;
    }

    private AudioManager mAudioManager;

    private Context mContext;

    /**
     * 初始化，应用onCreate的时候需要初始化一遍
     */
    public void initialize(Context context, String resumeServiceName) {
        Log.d(TAG, "initialize: resumeServiceName = " + resumeServiceName);
        if (mContext == null) {
            mContext = context;
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            AudioFocusRequestFactory.getInstance().initialize(resumeServiceName);
        }
    }


    private ArrayMap<Integer, AudioFocusRequest> mFocusRequestMap = new ArrayMap<>();


    /**
     * 音频焦点的申请逻辑，主要实现了将平台的音频焦点对外实现为原生的形式
     *
     * @param onAudioFocusChangeListener 音频焦点的回调(自己写的，不可能为空)
     * @param streamType                 音频焦点的类型，usb，fm，am等
     * @return 焦点的请求状态
     */
    public int requestAudioFocus(String streamType,
                                 AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "requestAudioFocus: streamType = " + streamType + " onAudioFocusChangeListener = " + onAudioFocusChangeListener.hashCode());
        int status = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest focusRequest = mFocusRequestMap.get(onAudioFocusChangeListener.hashCode());
            if (focusRequest == null) {
                focusRequest = AudioFocusRequestFactory.getInstance().createdAudioFocusRequest(streamType,
                        onAudioFocusChangeListener);
                mFocusRequestMap.put(onAudioFocusChangeListener.hashCode(), focusRequest);
            }
            status = mAudioManager.requestAudioFocus(focusRequest);
        }

        Log.d(TAG, "requestAudioFocus: status = " + status + " onAudioFocusChangeListener = " + onAudioFocusChangeListener.hashCode());
        return status;
    }

    /**
     * 释放音频焦点
     *
     * @param onAudioFocusChangeListener 音频焦点的回调
     */
    public int abandonAudioFocus(AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        int abandonStatus = AudioManager.AUDIOFOCUS_LOSS;
        Log.d(TAG, "abandonAudioFocus: onAudioFocusChangeListener = " + onAudioFocusChangeListener.hashCode());

        AudioFocusRequest audioFocusRequest = mFocusRequestMap.get(onAudioFocusChangeListener.hashCode());
        Log.d(TAG, "abandonAudioFocus: audioFocusRequest = " + audioFocusRequest);
        if (audioFocusRequest == null) {
            return abandonStatus;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            abandonStatus = mAudioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        return abandonStatus;
    }

}
