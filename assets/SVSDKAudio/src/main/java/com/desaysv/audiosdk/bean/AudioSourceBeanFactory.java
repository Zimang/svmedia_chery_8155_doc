package com.desaysv.audiosdk.bean;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by LZM on 2019-12-12
 * Comment AudioSourceBean的工厂，由于AudioSourceBean需要和应用调用的原生回调一一对应，所以加入了一个工厂类来获取AudioSourceBean
 */
public class AudioSourceBeanFactory {

    private static final String TAG = "AudioSourceMap";

    private static AudioSourceBeanFactory instance;

    public static AudioSourceBeanFactory getInstance() {
        if (instance == null) {
            synchronized (AudioSourceBeanFactory.class) {
                if (instance == null) {
                    instance = new AudioSourceBeanFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 一个存储AudioSourceBean的HashMap，实现回调的hashCode与AudioSourceBean一一对应
     */
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, AudioSourceBean> AudioSourceBeanMap = new HashMap<>();

    /**
     * 获取AudioSourceBean
     *
     * @param audioType                  音频焦点的类型
     * @param onAudioFocusChangeListener 原生的音频焦点回调
     * @return AudioSourceBean
     */
    public AudioSourceBean getAudioSourceBean(String audioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "getAudioSourceBean: audioType = " + audioType + " onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        if (onAudioFocusChangeListener == null) {
            Log.e(TAG, "getAudioSourceBean: onAudioFocusChangeListener is null");
            throw new RuntimeException();
        }
        int hashCode = onAudioFocusChangeListener.hashCode();
        AudioSourceBean audioSourceBean = AudioSourceBeanMap.get(hashCode);
        if (audioSourceBean == null) {
            audioSourceBean = new AudioSourceBean(audioType, onAudioFocusChangeListener);
            AudioSourceBeanMap.put(hashCode, audioSourceBean);
        }
        Log.d(TAG, "getAudioSourceBean: audioSourceBean = " + audioSourceBean);
        return audioSourceBean;
    }

    /**
     * 获取AudioSourceBean
     *
     * @param onAudioFocusChangeListener android 原生的音频焦点回调
     * @return AudioSourceBean
     */
    public AudioSourceBean getAudioSourceBean(AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "getAudioSourceBean: onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        if (onAudioFocusChangeListener == null) {
            Log.e(TAG, "getAudioSourceBean: onAudioFocusChangeListener is null");
            throw new RuntimeException();
        }
        int hashCode = onAudioFocusChangeListener.hashCode();
        AudioSourceBean audioSourceBean = AudioSourceBeanMap.get(hashCode);
        Log.d(TAG, "getAudioSourceBean: audioSourceBean = " + audioSourceBean);
        return audioSourceBean;
    }

    /**
     * 释放音频焦点的时候，需要将音频焦点从hashMap中移除，所以加入了这一个方法
     *
     * @param onAudioFocusChangeListener android 原生的音频焦点回调
     */
    public void removeAudioSourceBean(AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "removeAudioSourceBean: onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        if (onAudioFocusChangeListener == null) {
            return;
        }
        AudioSourceBeanMap.remove(onAudioFocusChangeListener.hashCode());
    }

    /**
     * 如果服务死掉的话，通知客户端音频焦点全部丢失，且将工厂模式里面的Map全部清除掉
     */
    public void resetFactory() {
        Log.e(TAG, "resetFactory: ");
        for (Integer integer : AudioSourceBeanMap.keySet()) {
            AudioSourceBeanMap.get(integer).mOnAudioFocusChangeListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        }
        AudioSourceBeanMap.clear();
    }

}
