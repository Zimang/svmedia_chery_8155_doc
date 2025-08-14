package com.desaysv.mediaservice.bean;

import android.annotation.SuppressLint;
import android.util.Log;

import com.desaysv.audiosdk.IOnAudioFocusChangeListener;

import java.util.HashMap;

/**
 * Created by LZM on 2019-12-12
 * Comment 这是一类似于工厂模式的类型，主要是用来获取AudioFocusCallBackBean，根据传入的audioType来获取
 */
public class AudioFocusCallBackBeanFactory {

    private static final String TAG = "AudioFocusCallBackBeanFactory";


    private static AudioFocusCallBackBeanFactory instance;

    public static AudioFocusCallBackBeanFactory getInstance() {
        if (instance == null) {
            synchronized (AudioFocusCallBackBeanFactory.class) {
                if (instance == null) {
                    instance = new AudioFocusCallBackBeanFactory();
                }
            }
        }
        return instance;
    }

    /**
     * AudioFocusCallBackBean的HashMap
     */
    private HashMap<String, AudioFocusCallBackBean> AudioFocusCallBackBeanMap = new HashMap<>();

    /**
     * 获取AudioFocusCallBackBean
     *
     * @param audioType                  音频焦点的类型
     * @param onAudioFocusChangeListener AIDL的音频焦点回调
     * @return AudioFocusCallBackBean
     */
    public AudioFocusCallBackBean getAudioFocusCallBackBean(String audioType, String clientId, IOnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "getAudioFocusCallBackBean: audioType = " + audioType + " onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        if (onAudioFocusChangeListener == null) {
            Log.e(TAG, "getAudioFocusCallBackBean: onAudioFocusChangeListener is null");
            return null;
        }
        //更新Client端的ID进行状态获取
        AudioFocusCallBackBean audioFocusCallBackBean = AudioFocusCallBackBeanMap.get(clientId);
        if (audioFocusCallBackBean == null) {
            audioFocusCallBackBean = new AudioFocusCallBackBean(audioType, onAudioFocusChangeListener);
            AudioFocusCallBackBeanMap.put(clientId, audioFocusCallBackBean);
        } else {
            audioFocusCallBackBean.setAudioType(audioType);
            audioFocusCallBackBean.setOnAudioFocusChangeListener(onAudioFocusChangeListener);
        }
        Log.d(TAG, "getAudioFocusCallBackBean: audioFocusCallBackBean = " + audioFocusCallBackBean);
        return audioFocusCallBackBean;
    }

    /**
     * 获取AudioFocusCallBackBean
     *
     * @param clientId 音频回调的hashcode
     * @return AudioFocusCallBackBean
     */
    public AudioFocusCallBackBean getAudioFocusCallBackBean(String clientId) {
        Log.d(TAG, "getAudioFocusCallBackBean: clientId = " + clientId);
        AudioFocusCallBackBean audioFocusCallBackBean = AudioFocusCallBackBeanMap.get(clientId);
        if (audioFocusCallBackBean == null) {
            Log.e(TAG, "getAudioFocusCallBackBean: audioFocusCallBackBean is null");
        }
        Log.d(TAG, "getAudioFocusCallBackBean: audioFocusCallBackBean = " + audioFocusCallBackBean);
        return audioFocusCallBackBean;
    }

    /**
     * 释放焦点的时候，需要将hashMap移除掉
     *
     * @param clientId client端的音频焦点回调的hashcode
     */
    public void removeHashMap(String clientId) {
        Log.d(TAG, "removeHashMap: listenerCode = " + clientId);
        AudioFocusCallBackBeanMap.remove(clientId);
    }

}
