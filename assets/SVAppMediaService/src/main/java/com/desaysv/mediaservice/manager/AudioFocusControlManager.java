package com.desaysv.mediaservice.manager;

import android.media.AudioManager;
import android.util.Log;

import com.desaysv.mediaservice.bean.AudioFocusCallBackBean;
import com.desaysv.mediaservice.bean.AudioFocusCallBackBeanFactory;
import com.desaysv.audiosdk.IOnAudioFocusChangeListener;
import com.desaysv.svmediaservicelogic.utils.AudioFocusManager;

/**
 * Created by LZM on 2019-12-13
 * Comment 音频焦点申请的管理类
 */
public class AudioFocusControlManager {

    private static final String TAG = "AudioFocusControlManage";

    private static AudioFocusControlManager instance;

    public static AudioFocusControlManager getInstance() {
        if (instance == null) {
            synchronized (AudioFocusControlManager.class) {
                if (instance == null) {
                    instance = new AudioFocusControlManager();
                }
            }
        }
        return instance;
    }

    /**
     * 申请音频焦点
     *
     * @param audioType                   音频焦点类型
     * @param clientId                    client端的回调的唯一值
     * @param iOnAudioFocusChangeListener AIDL回调
     * @return Android 申请音频焦点的返回状态
     */
    public int requestAudioFocus(String audioType, String clientId, IOnAudioFocusChangeListener iOnAudioFocusChangeListener) {
        Log.d(TAG, "requestAudioFocus: audioType = " + audioType + " iOnAudioFocusChangeListener = " + iOnAudioFocusChangeListener);

        int requestStatus = AudioManager.AUDIOFOCUS_LOSS;
        //根据回调的hashcode进行数据存储，特征值就是回调的hashcode
        AudioFocusCallBackBean audioFocusCallBackBean =
                AudioFocusCallBackBeanFactory.getInstance().getAudioFocusCallBackBean(audioType, clientId, iOnAudioFocusChangeListener);
        if (audioFocusCallBackBean == null) {
            Log.e(TAG, "requestAudioFocus: audioFocusCallBackBean is null");
            return requestStatus;
        }
        requestStatus = AudioFocusManager.getInstance().requestAudioFocus(audioFocusCallBackBean.getAudioType(), audioFocusCallBackBean.onAudioFocusChangeListener);
        Log.d(TAG, "requestAudioFocus: audioType = " + audioType + " requestStatus = " + requestStatus);
        return requestStatus;
    }

    /**
     * 释放音频焦点
     *
     * @param clientId 音频焦点回调的HashCode
     * @return 释放音频焦点的状态
     */
    public int abandonAudioFocus(String clientId) {
        Log.d(TAG, "abandonAudioFocus: clientId = " + clientId);
        int abandonStatus = AudioManager.AUDIOFOCUS_LOSS;
        //根据回调的hashcode进行获取，特征值就是回调的hashcode
        AudioFocusCallBackBean audioFocusCallBackBean =
                AudioFocusCallBackBeanFactory.getInstance().getAudioFocusCallBackBean(clientId);
        if (audioFocusCallBackBean == null) {
            Log.e(TAG, "abandonAudioFocus: audioFocusCallBackBean is null");
            return abandonStatus;
        }
        abandonStatus = AudioFocusManager.getInstance().abandonAudioFocus(audioFocusCallBackBean.onAudioFocusChangeListener);
        AudioFocusCallBackBeanFactory.getInstance().removeHashMap(clientId);
        Log.d(TAG, "abandonAudioFocus: clientId = " + clientId + " abandonStatus = " + abandonStatus);
        return abandonStatus;
    }


}
