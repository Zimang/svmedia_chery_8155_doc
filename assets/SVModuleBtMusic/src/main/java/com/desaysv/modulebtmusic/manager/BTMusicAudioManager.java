package com.desaysv.modulebtmusic.manager;

import android.car.Car;
import android.car.media.CarAudioManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.utils.ObserverBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BTMusicAudioManager {
    private static final String TAG = Constants.TAG + "BTMusicAudioManager";
    private static volatile BTMusicAudioManager mInstance;
    private final ExecutorService mSingleThreadPool = Executors.newSingleThreadExecutor();
    private final ObserverBuilder<IAudioFocusListener> mObserverBuilder = new ObserverBuilder<>();
    private Context mContext;
    private Car mCar;
    private CarAudioManager mCarAudioManager;

    public static BTMusicAudioManager getInstance() {
        if (mInstance == null) {
            synchronized (BTMusicAudioManager.class) {
                if (mInstance == null) {
                    mInstance = new BTMusicAudioManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        mContext = context;
        mCar = Car.createCar(context);
        mCarAudioManager = (CarAudioManager) mCar.getCarManager(Car.AUDIO_SERVICE);
        mCarAudioManager.setAudioFocusChangeListener(0, new AudioFocusListener());
        Log.d(TAG, "initialize: finish");
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "release: ");
        mObserverBuilder.clearObservers();
        mContext = null;
        mCar = null;
        mCarAudioManager = null;
        Log.d(TAG, "release: finish");
    }

    public void registerListener(IAudioFocusListener listener) {
        mObserverBuilder.registerObserver(listener);
    }

    public void unregisterListener(IAudioFocusListener listener) {
        mObserverBuilder.unregisterObserver(listener);
    }

    /**
     * @return
     */
    public boolean checkBTAudioFocusStatus() {
        if (null == mCarAudioManager) {
            Log.w(TAG, "checkBTAudioFocusStatus: mCarAudioManager == null");
            return false;
        }
        int currentStatus = mCarAudioManager.checkAudioFocusState(AudioAttributes.SOURCE_BT_A2DP);
        Log.i(TAG, "checkBTAudioFocusStatus: currentStatus = " + currentStatus);
        switch (currentStatus) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                return true;
        }
        return false;
    }

    //SOURCE_MEDIA_MUTE
    //SOURCE_HARDKEY_MUTE
    public boolean isInMuteState() {
        if (null == mCarAudioManager) {
            Log.w(TAG, "isInMuteState: mCarAudioManager == null");
            return false;
        }
        int mediaMuteStatus = mCarAudioManager.checkAudioFocusState(AudioAttributes.SOURCE_MEDIA_MUTE);
        int hardKeyMuteStatus = mCarAudioManager.checkAudioFocusState(AudioAttributes.SOURCE_HARDKEY_MUTE);
        Log.i(TAG, "isInMuteState: mediaMuteStatus = " + mediaMuteStatus + ", hardKeyMuteStatus = " + hardKeyMuteStatus);
        return mediaMuteStatus == AudioManager.AUDIOFOCUS_GAIN || hardKeyMuteStatus == AudioManager.AUDIOFOCUS_GAIN;
    }

    private class AudioFocusListener implements CarAudioManager.AudioFocusChangeListener {
        @Override
        public void onAudioFocusGrant(AudioFocusInfo audioFocusInfo, int i) {
            mSingleThreadPool.execute(() -> {
                Integer sourceId = getSourceId(audioFocusInfo, "onAudioFocusGrant");
                if (sourceId == null) {
                    Log.w(TAG, "onAudioFocusGrant: sourceId == null");
                    return;
                }
                mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IAudioFocusListener>) observer -> {
                    observer.onAudioFocusGrant(sourceId);
                });
            });
        }

        @Override
        public void onAudioFocusLoss(AudioFocusInfo audioFocusInfo, boolean b) {
            mSingleThreadPool.execute(() -> {
                Integer sourceId = getSourceId(audioFocusInfo, "onAudioFocusLoss");
                if (sourceId == null) {
                    Log.w(TAG, "onAudioFocusLoss: sourceId == null");
                    return;
                }
                mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IAudioFocusListener>) observer -> {
                    observer.onAudioFocusLoss(sourceId);
                });
            });
        }
    }

    private Integer getSourceId(AudioFocusInfo audioFocusInfo, String tag) {
        if (audioFocusInfo == null) {
            Log.w(TAG, "getSourceId: audioFocusInfo == null,tag=" + tag);
            return null;
        }
        AudioAttributes attributes = audioFocusInfo.getAttributes();
        if (attributes == null) {
            Log.w(TAG, "getSourceId: attributes == null,tag=" + tag);
            return null;
        }
        Bundle bundle = attributes.getBundle();
        if (bundle == null) {
            Log.w(TAG, "getSourceId: bundle == null,tag=" + tag);
            return null;
        }
        String carAudioType = bundle.getString("key_car_audio_type");
        Integer sourceId = bundle.getInt("key_source_id", -1);
        Log.i(TAG, "getSourceId: carAudioType=" + carAudioType + ",sourceId=" + sourceId
                + ",info=" + audioFocusInfo.toString() + ",tag=" + tag);
        return sourceId;
    }

    public boolean isA2dpAudio(Object audioType) {
        if (audioType == null) {
            Log.w(TAG, "isA2dpAudio: audioType == null");
            return false;
        }
        return (int) audioType == AudioAttributes.SOURCE_BT_A2DP;
    }

    public interface IAudioFocusListener {
        void onAudioFocusGrant(Object carAudioType);

        void onAudioFocusLoss(Object carAudioType);
    }
}
