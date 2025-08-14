package com.desaysv.audiosdk.bean;

import android.media.AudioManager;
import android.os.RemoteException;
import android.util.Log;

import com.desaysv.audiosdk.IOnAudioFocusChangeListener;

/**
 * Created by LZM on 2019-12-12
 * Comment 定义了一个类来进行音频焦点的转化，里面存储了音频的类型，应用调用的原生音频焦点回调，以及AIDL传输的AIDL音频回调
 */
public class AudioSourceBean {

    private static final String TAG = "AudioSourceBean";

    /**
     * 音频的焦点类型，如fm,am,usb0
     */
    private String mAudioType;

    /**
     * 应用申请的原生音频焦点回调
     */
    public AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

    /**
     * 构造函数
     *
     * @param audioType                  音频焦点类型
     * @param onAudioFocusChangeListener 应用的原生回调
     */
    AudioSourceBean(String audioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "AudioSourceBean: audioType = " + audioType + "  onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        mAudioType = audioType;
        mOnAudioFocusChangeListener = onAudioFocusChangeListener;
    }

    /**
     * AIDL回调，一个AudioSourceBean对应一个AIDL回调，并且和应用申请的原生回调绑定在一起
     */
    public IOnAudioFocusChangeListener.Stub onAudioFocusChangeListener = new IOnAudioFocusChangeListener.Stub() {
        @Override
        public void onAudioFocusChange(int focusStatus) throws RemoteException {
            Log.d(TAG, "onAudioFocusChange: mAudioType = " + mAudioType + " focusStatus = " + focusStatus);
            if (mOnAudioFocusChangeListener != null) {
                mOnAudioFocusChangeListener.onAudioFocusChange(focusStatus);
            } else {
                Log.e(TAG, "onAudioFocusChange: mOnAudioFocusChangeListener is null");
            }
        }
    };

    public String getAudioType() {
        return mAudioType;
    }

    @Override
    public String toString() {
        return "AudioSourceBean{" +
                "mAudioType='" + mAudioType + '\'' +
                ", mOnAudioFocusChangeListener=" + mOnAudioFocusChangeListener +
                '}';
    }
}
