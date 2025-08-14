package com.desaysv.audiosdk.manager.audiofocusmanager;

import android.content.Context;
import android.media.AudioManager;

import com.desaysv.audiosdk.IAudioFocusManager;

/**
 * Created by LZM on 2019-12-12
 * Comment
 */
public interface IDsvAudioFocusManager {


    /**
     * 初始化
     *
     * @param context
     * @param iAudioFocusManager AIDL接口
     */
    void initialize(Context context, IAudioFocusManager iAudioFocusManager);

    /**
     * 申请音频焦点
     *
     * @param audioType                  定义的媒体类型，fm，usb，am，在线音乐等
     * @param onAudioFocusChangeListener android原生的音频焦点回调
     * @return AudioManager.AUDIOFOCUS_GAIN 等原生定义焦点返回值
     */
    int requestAudioFocus(String audioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener);

    /**
     * 释放音频焦点
     *
     * @param onAudioFocusChangeListener 焦点回调，根据焦点回调来释放音频焦点
     * @return AudioManager.AUDIOFOCUS_GAIN 等原生定义焦点返回值
     */
    int abandonAudioFocus(AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener);


    /**
     * 获取当前媒体音频焦点根据是否已经初始化成功
     *
     * @return
     */
    boolean isInitialize();

}
