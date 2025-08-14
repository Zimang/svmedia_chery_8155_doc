package com.desaysv.svmediaservicelogic.utils;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.Log;


import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
//import com.desaysv.ivi.platform.app.audio.SvCarAudioManager;
import com.desaysv.svmediaservicelogic.bean.AudioFocusTypeBean;

/**
 * Created by LZM on 2020-3-26
 * Comment AudioAttributes的构建方法，通过不同的音源类型输入，来实现不一样的AudioAttributes的构建
 * 2020 03 27 现阶段需要实现的有三种类型的音频焦点
 * 1. 需要音源恢复的音乐类型的音频焦点
 * 2. TTS类型的音频焦点
 * 3. 默认的音频类型的音频焦点
 *
 * @author uidp5370
 */
@SuppressLint("LongLogTag,NewApi")
public class AudioFocusRequestFactory {

    private static final String TAG = "AudioFocusRequestFactory";

    private static AudioFocusRequestFactory instance;

    //音源恢复的服务名称
    private String mResumeServiceName = "";

    public static AudioFocusRequestFactory getInstance() {
        if (instance == null) {
            synchronized (AudioFocusRequestFactory.class) {
                if (instance == null) {
                    instance = new AudioFocusRequestFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化AudioAttributes的构造器
     *
     * @param resumeServiceName 需要音源恢复的服务名称
     */
    public void initialize(String resumeServiceName) {
        Log.d(TAG, "initialize: resumeServiceName = " + resumeServiceName);
        mResumeServiceName = resumeServiceName;
    }


    /**
     * 根据输入的carAudioType，获取不同类型的AudioFocusRequest
     *
     * @param carAudioType               系统定义的音频焦点类型
     * @param onAudioFocusChangeListener 原生的音频焦点回调，不可能为空
     * @return AudioFocusRequest 生成系统的AudioFocusRequest
     */
    public AudioFocusRequest createdAudioFocusRequest(String carAudioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "createdAudioFocusRequest: carAudioType = " + carAudioType);
        AudioFocusRequest audioFocusRequest;
        switch (AudioFocusTypeBean.getFocusTypeFormSource(carAudioType)) {
            case AudioFocusTypeBean.BOOT_RESUME_MEDIA_TYPE:
                audioFocusRequest = createdBootResumeMediaAudioFocusRequest(carAudioType, onAudioFocusChangeListener);
                break;
            case AudioFocusTypeBean.TTS_TYPE:
                audioFocusRequest = createdTTSAudioFocusRequest(carAudioType, onAudioFocusChangeListener);
                break;
            default:
                audioFocusRequest = createdDefaultMediaFocusRequest(carAudioType, onAudioFocusChangeListener);
                break;
        }
        return audioFocusRequest;
    }


    /**
     * 构造需要音源恢复的AudioFocusRequest
     *
     * @param carAudioType 需要音源恢复的焦点类型
     * @return AudioAttributes  音乐的AudioAttributes
     */
    @SuppressLint("LongLogTag")
    private AudioFocusRequest createdBootResumeMediaAudioFocusRequest(String carAudioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "createdBootResumeMediaAudioFocusRequest: streamType = " + carAudioType + " mResumeServiceName = " + mResumeServiceName);
        /*AudioFocusRequest.Builder focusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
        focusBuilder.setOnAudioFocusChangeListener(onAudioFocusChangeListener);
        focusBuilder.setAcceptsDelayedFocusGain(true);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build();
        SvCarAudioManager.setAttrCarAudioType(audioAttributes, carAudioType);  //设置该焦点是什么焦点
        SvCarAudioManager.setAttrBootResume(audioAttributes, SvCarAudioManager.BOOT_RESUME_ENABLE);  //设置该音频焦点需要源恢复
        SvCarAudioManager.setAttrResumeServiceClassName(audioAttributes, mResumeServiceName); //设置需要音源恢复的音频焦点名称
        focusBuilder.setAudioAttributes(audioAttributes);
        return focusBuilder.build();*/
        return createdDefaultMediaFocusRequest(carAudioType, onAudioFocusChangeListener);
    }

    /**
     * 构造TTS类型的AudioFocusRequest
     *
     * @param carAudioType TTS类型的音频焦点
     * @return AudioAttributes  TTS的AudioAttributes
     */
    private AudioFocusRequest createdTTSAudioFocusRequest(String carAudioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "createdTTSAudioFocusRequest: streamType = " + carAudioType);
        /*AudioFocusRequest.Builder focusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        focusBuilder.setOnAudioFocusChangeListener(onAudioFocusChangeListener);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).build();
        //设置该焦点是什么焦点
        SvCarAudioManager.setAttrCarAudioType(audioAttributes, carAudioType);
        focusBuilder.setAudioAttributes(audioAttributes);
        return focusBuilder.build();*/
        return createdDefaultMediaFocusRequest(carAudioType, onAudioFocusChangeListener);
    }

    /**
     * 构造默认的媒体音源，不需要音源恢复
     *
     * @param carAudioType 不需要音源恢复的音频焦点
     * @return AudioAttributes  音乐的AudioAttributes
     */
    private AudioFocusRequest createdDefaultMediaFocusRequest(String carAudioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "createdDefaultMediaFocusRequest: streamType = " + carAudioType);
        //平台更换接口，这里进行转接下
        int sourceType = AudioAttributes.SOURCE_MUSIC;
        switch (carAudioType) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                sourceType = AudioAttributes.SOURCE_LOCAL_MUSIC;
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                sourceType = AudioAttributes.SOURCE_USB0_MUSIC;
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                sourceType = AudioAttributes.SOURCE_USB1_MUSIC;
                break;
            case DsvAudioSDKConstants.USB0_VIDEO_SOURCE:
                sourceType = AudioAttributes.SOURCE_USB0_VIDEO;
                break;
            case DsvAudioSDKConstants.USB1_VIDEO_SOURCE:
                sourceType = AudioAttributes.SOURCE_USB1_VIDEO;
                break;
            case DsvAudioSDKConstants.AM_SOURCE:
                sourceType = AudioAttributes.SOURCE_AM;
                break;
            case DsvAudioSDKConstants.FM_SOURCE:
                sourceType = AudioAttributes.SOURCE_FM;
                break;
            case DsvAudioSDKConstants.DAB_SOURCE:
                sourceType = AudioAttributes.SOURCE_DAB;
                break;
            default:
                sourceType = AudioAttributes.SOURCE_MUSIC;
                break;
        }
        AudioFocusRequest.Builder focusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
        focusBuilder.setOnAudioFocusChangeListener(onAudioFocusChangeListener);
        focusBuilder.setAcceptsDelayedFocusGain(true);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(sourceType)
                .build();
        //设置该焦点是什么焦点
        //SvCarAudioManager.setAttrCarAudioType(audioAttributes, carAudioType);
        focusBuilder.setAudioAttributes(audioAttributes);
        focusBuilder.setForceDucking(true);
        return focusBuilder.build();
    }
}
