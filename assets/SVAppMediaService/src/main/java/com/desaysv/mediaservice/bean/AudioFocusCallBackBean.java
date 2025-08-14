package com.desaysv.mediaservice.bean;

import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.desaysv.audiosdk.IOnAudioFocusChangeListener;
import com.desaysv.svmediaservicelogic.utils.AudioFocusManager;

/**
 * Created by LZM on 2019-12-13
 * Comment 用来实现AIDL回调，音频焦点TYPE，和原生回调绑定的对象类
 * @author uidp5370
 */
public class AudioFocusCallBackBean {

    private static final String TAG = "AudioFocusCallBackBean";

    private String mAudioType;

    public String getAudioType() {
        return mAudioType;
    }

    private IOnAudioFocusChangeListener mOnAudioFocusChangeListener;

    /**
     * 设置AudioType，主要是用来实现打印的逻辑
     * @param mAudioType 音源的CarAudioType
     */
    public void setAudioType(String mAudioType) {
        this.mAudioType = mAudioType;
    }

    public AudioFocusCallBackBean(String audioType, IOnAudioFocusChangeListener onAudioFocusChangeListener) {
        mAudioType = audioType;
        setOnAudioFocusChangeListener(onAudioFocusChangeListener);
    }

    /**
     * 由于AIDL回调并不是固定一个对象，而是拷贝所以每次申请音频焦点的时候，会用AIDL回调过来的对象都会是不一样的，
     * 所以需要每次回调都需要设置一遍
     *
     * @param onAudioFocusChangeListener
     */
    public void setOnAudioFocusChangeListener(IOnAudioFocusChangeListener onAudioFocusChangeListener) {
        Log.d(TAG, "setOnAudioFocusChangeListener: onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        //每次设置之前都要键之前的死亡监听去掉，避免回调多次
        if (mOnAudioFocusChangeListener != null) {
            mOnAudioFocusChangeListener.asBinder().unlinkToDeath(deathRecipient, 0);
        }
        mOnAudioFocusChangeListener = onAudioFocusChangeListener;
        try {
            mOnAudioFocusChangeListener.asBinder().linkToDeath(deathRecipient, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Android 原生的音频焦点回调
     */
    public AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange: mAudioType = " + mAudioType + " focusChange = " + focusChange);
            if (mOnAudioFocusChangeListener != null) {
                try {
                    mOnAudioFocusChangeListener.onAudioFocusChange(focusChange);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public String toString() {
        return "AudioFocusCallBackBean{" +
                "mAudioType='" + mAudioType + '\'' +
                ", mOnAudioFocusChangeListener=" + mOnAudioFocusChangeListener +
                '}';
    }


    /**
     * 客户端死亡回调的监听,客户端死亡的话，需要将音频焦点释放掉
     */
    IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            String audioType = getAudioType();
            Log.d(TAG, "binderDied: audioType = " + audioType);
            AudioFocusManager.getInstance().abandonAudioFocus(onAudioFocusChangeListener);

        }
    };


}
