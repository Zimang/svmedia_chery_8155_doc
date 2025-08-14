package com.desaysv.audiosdk.utils;

import android.car.Car;
import android.car.media.CarAudioManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;

import java.util.Arrays;

/**
 * Created by LZM on 2020-3-24
 * Comment 提供了一些应用需要使用的基础功能类
 */
public class AudioFocusUtils {

    private static final String TAG = "AudioFocusUtils";
    private CarAudioManager carAudioManager;
    private AudioManager audioManager;
    private Context mContext;

    private static AudioFocusUtils instance;

    public static AudioFocusUtils getInstance() {
        if (instance == null) {
            synchronized (AudioFocusUtils.class) {
                if (instance == null) {
                    instance = new AudioFocusUtils();
                }
            }
        }
        return instance;
    }

    private AudioFocusUtils() {

    }

    /**
     * 初始化，应用onCreate的时候需要初始化一遍
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        if (mContext == null) {
            mContext = context;
            //初始话AudioManager
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            //再初始化CarAudioManager
            //查看源码，发现carService断开的时候，如果没有状态回调，则会kill掉进程
            //cannot bind to car service after max retry 绑定
            Car.createCar(context, null, 0, (car, isRetry) -> {
                //如果isRetry是false，则为onServiceDisconnected触发，true则为onServiceConnected，或者触发
                // Car service can pick up feature changes after restart
                Log.d(TAG, "initialize: isRetry = " + isRetry);
                //服务已经注册才需进行初始化
                if (car.isConnected()) {
                    Log.d(TAG, "initialize: service connected");
                    carAudioManager = (CarAudioManager) car.getCarManager(Car.AUDIO_SERVICE);
                    //当前未有定义文档当中的未带zoneId接口
                    if (carAudioManager == null) {
                        Log.d(TAG, "initialize: service connected but carAudioManager also is null！");
                        return;
                    }
                    //carAudioManager.setAudioFocusChangeListener(0, null);
                }
                if (!isRetry
                        && !car.isConnected()
                        && !car.isConnecting()) {
                    Log.d(TAG, "initialize: service disconnect");
                    //car.connect();
                }
            });
        }
    }


    private CarAudioManager mAudioManager;

    /**
     * 获取当前音频焦点的类型，根据我在A12上的经验，这个方法不准，倒车的时候就获取会出现错误
     *
     * @return car_audio_type 当前音频焦点的类型，usb，fm，am等
     */
    public String getCurrentAudioSourceName(Context context) {
        //获取全部音源信息
        if (mAudioManager == null) {
            Car mCar = Car.createCar(context);
            mAudioManager = (CarAudioManager) mCar.getCarManager(Car.AUDIO_SERVICE);
        }
        int topFocusSourceID = mAudioManager.getTopFocusSourceID();
        String carAudioType = sourceIDToAudioType(topFocusSourceID);
        Log.d(TAG, "getCurrentAudioSourceName: carAudioType = " + carAudioType + " topFocusSourceID = " + topFocusSourceID);
        return carAudioType;
    }


    /**
     * 获取当前音乐类型的音频焦点的类型，其他音源是不会获取到的
     *
     * @return car_audio_type 当前音频焦点的类型，usb，fm，am等
     */
    public String getCurrentMusicAudioSourceName(Context context) {
        if (mAudioManager == null) {
            Car mCar = Car.createCar(context);
            mAudioManager = (CarAudioManager) mCar.getCarManager(Car.AUDIO_SERVICE);
        }
        String carAudioType = sourceIDToAudioType(mAudioManager.getTopMediaFocusSourceID());
        Log.d(TAG, "getCurrentMusicAudioSourceName: carAudioType = " + carAudioType);
        return carAudioType;
    }

    /**
     * 检测音频焦点的状态
     *
     * @param context   上下文
     * @param audioType 需要检测的音源
     * @return 音频焦点的返回值
     */
    public int checkAudioFocusStatus(Context context, String audioType) {
        //fix by lzm 检测音频焦点的状态不能用AIDL提供，会有时延
        int focusStatus = AudioManager.AUDIOFOCUS_LOSS;
        if (mAudioManager == null) {
            Car mCar = Car.createCar(context);
            mAudioManager = (CarAudioManager) mCar.getCarManager(Car.AUDIO_SERVICE);
        }
        Log.d(TAG, "checkAudioFocusStatus: mAudioManager = " + mAudioManager);
        if (mAudioManager != null) {
            focusStatus = mAudioManager.checkAudioFocusState(audioTypeToSourceID(audioType));
        }
        Log.d(TAG, "checkAudioFocusStatus: focusStatus = " + focusStatus);
        return focusStatus;
    }

    /**
     * add by lzm 检测当前音频焦点是否为获取状态
     *
     * @param streamType 需要检测的音源
     * @return ture：当前为获取的状态 false：当前不是获取的状态
     */
    public boolean checkAudioFocusIsHas(Context context, String streamType) {
        int currentStatus = checkAudioFocusStatus(context, streamType);
        //add by lzm 去掉none数据，因为没有焦点获取到的就是none
        boolean isHasFocus = (currentStatus == AudioManager.AUDIOFOCUS_GAIN
                || currentStatus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                || currentStatus == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        Log.d(TAG, "checkAudioFocusIsHas: currentStatus = " + currentStatus + " streamType = " + streamType + " isHasFocus = " + isHasFocus);
        return isHasFocus;
    }

    /**
     * CarAudioType 转换 SOURCE ID
     *
     * @param audioType 需要检测的音源
     * @return SOURCE ID
     */
    public int audioTypeToSourceID(String audioType) {
        switch (audioType) {
            case DsvAudioSDKConstants.AM_SOURCE:
                return AudioAttributes.SOURCE_AM;
            case DsvAudioSDKConstants.FM_SOURCE:
                return AudioAttributes.SOURCE_FM;
            case DsvAudioSDKConstants.DAB_SOURCE:
                return AudioAttributes.SOURCE_DAB;//需要frw添加
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_LOCAL_MUSIC;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_USB0_MUSIC;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_USB1_MUSIC;
            case DsvAudioSDKConstants.USB0_VIDEO_SOURCE:
                return AudioAttributes.SOURCE_USB0_VIDEO;
            case DsvAudioSDKConstants.USB1_VIDEO_SOURCE:
                return AudioAttributes.SOURCE_USB1_VIDEO;
            case DsvAudioSDKConstants.CARLIFE_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_CL_MEDIA;
            case DsvAudioSDKConstants.CARPALY_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_CP_MEDIA;
            case DsvAudioSDKConstants.BT_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_BT_A2DP;
            case DsvAudioSDKConstants.ONLINE_MUSIC_SOURCE:
                return AudioAttributes.SOURCE_ONLINE_MUSIC;
            case DsvAudioSDKConstants.ONLINE_RADIO_SOURCE:
                return AudioAttributes.SOURCE_ONLINE_NETWORKSTATION;
            case DsvAudioSDKConstants.RDS_TTS_SOURCE:
                return AudioAttributes.SOURCE_RDS_TTS;
            case DsvAudioSDKConstants.DAB_TTS_SOURCE:
                return AudioAttributes.SOURCE_DAB_TTS;
        }
        return AudioAttributes.SOURCE_MUSIC;
    }


    /**
     * SOURCE ID 转换 CarAudioType
     *
     * @param sourceID 需要检测的音源id
     * @return audiotype
     */
    public String sourceIDToAudioType(int sourceID) {
        switch (sourceID) {
            case AudioAttributes.SOURCE_AM:
                return DsvAudioSDKConstants.AM_SOURCE;
            case AudioAttributes.SOURCE_FM:
                return DsvAudioSDKConstants.FM_SOURCE;
            case AudioAttributes.SOURCE_RDS_TTS:
                return DsvAudioSDKConstants.RDS_TTS_SOURCE;
            case AudioAttributes.SOURCE_DAB_TTS:
                return DsvAudioSDKConstants.DAB_TTS_SOURCE;
            case AudioAttributes.SOURCE_DAB://需要frw添加
                return DsvAudioSDKConstants.DAB_SOURCE;
            case AudioAttributes.SOURCE_LOCAL_MUSIC:
                return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_USB0_MUSIC:
                return DsvAudioSDKConstants.USB0_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_USB1_MUSIC:
                return DsvAudioSDKConstants.USB1_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_USB0_VIDEO:
                return DsvAudioSDKConstants.USB0_VIDEO_SOURCE;
            case AudioAttributes.SOURCE_USB1_VIDEO:
                return DsvAudioSDKConstants.USB1_VIDEO_SOURCE;
            case AudioAttributes.SOURCE_CL_MEDIA:
                return DsvAudioSDKConstants.CARLIFE_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_CP_MEDIA:
                return DsvAudioSDKConstants.CARPALY_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_BT_A2DP:
                return DsvAudioSDKConstants.BT_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_ONLINE_MUSIC:
                return DsvAudioSDKConstants.ONLINE_MUSIC_SOURCE;
            case AudioAttributes.SOURCE_ONLINE_NETWORKSTATION:
                return DsvAudioSDKConstants.ONLINE_RADIO_SOURCE;
        }
        //如果没匹配zhe
        return DsvAudioSDKConstants.OTHERS_NO_MEDIA_SOURCE;
    }

    /**
     * 申请焦点
     *
     * @param sourceId            sourceId
     * @param focusChangeListener focusChangeListener
     * @return 焦点申请情况
     */
    public int requestFocus(int sourceId, AudioManager.OnAudioFocusChangeListener focusChangeListener) {
        AudioFocusRequest.Builder focusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
        focusBuilder.setOnAudioFocusChangeListener(focusChangeListener);
        focusBuilder.setAcceptsDelayedFocusGain(true);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(sourceId)
                .build();
        //设置该焦点是什么焦点
        //SvCarAudioManager.setAttrCarAudioType(audioAttributes, carAudioType);
        focusBuilder.setAudioAttributes(audioAttributes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            focusBuilder.setForceDucking(true);
        }
        return audioManager.requestAudioFocus(focusBuilder.build());
    }


    /**
     * 申请TTS焦点
     *
     * @param sourceId            sourceId
     * @param focusChangeListener focusChangeListener
     * @return 焦点申请情况
     */
    public int requestFocusWithTTS(int sourceId, AudioManager.OnAudioFocusChangeListener focusChangeListener) {
        AudioFocusRequest.Builder focusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        focusBuilder.setOnAudioFocusChangeListener(focusChangeListener);
        focusBuilder.setAcceptsDelayedFocusGain(true);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(sourceId)
                .build();
        //设置该焦点是什么焦点
        //SvCarAudioManager.setAttrCarAudioType(audioAttributes, carAudioType);
        focusBuilder.setAudioAttributes(audioAttributes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            focusBuilder.setForceDucking(true);
        }
        return audioManager.requestAudioFocus(focusBuilder.build());
    }

    /**
     * 释放焦点
     *
     * @param sourceId            sourceId
     * @param focusChangeListener focusChangeListener
     * @return 焦点申请情况
     */
    public int releaseFocus(int sourceId, AudioManager.OnAudioFocusChangeListener focusChangeListener) {
        AudioFocusRequest.Builder focusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        focusBuilder.setOnAudioFocusChangeListener(focusChangeListener);
        return audioManager.abandonAudioFocusRequest(focusBuilder.build());
    }


    /**
     * 获取当前音频焦点的类型，根据我在A12上的经验，这个方法不准，倒车的时候就获取会出现错误
     *
     * @return car_audio_type 当前音频焦点的类型，usb，fm，am等
     */
    public String getCurrentAudioSourceName() {
        String carAudioType = sourceIDToAudioType(getCurrentMusicAudioSourceId());
        Log.d(TAG, "getCurrentAudioSourceName: carAudioType = " + carAudioType);
        return carAudioType;
    }

    /**
     * 获取当前音乐类型的音频焦点的类型，其他音源是不会获取到的
     *
     * @return car_audio_type 当前音频焦点的类型，usb，fm，am等
     */
    public int getCurrentMusicAudioSourceId() {
        if (carAudioManager == null) {
            Log.d(TAG, "getCurrentMusicAudioSourceId: is not connect to carAudioManager!!!");
            return -1;
        }
        int topMediaFocusSourceID = carAudioManager.getTopMediaFocusSourceID();
        Log.d(TAG, "getCurrentMusicAudioSourceId: topMediaFocusSourceID = " + topMediaFocusSourceID);
        return topMediaFocusSourceID;
    }


    /**
     * 获取当前的音频焦点的类型
     *
     * @return car_audio_type 当前音频焦点的类型，usb，fm，am等
     */
    public int getCurrentSourceId() {
        if (carAudioManager == null) {
            Log.d(TAG, "getCurrentSourceId: is not connect to carAudioManager!!!");
            return -1;
        }
        int topFocusSourceID = carAudioManager.getTopFocusSourceID();
        Log.d(TAG, "getCurrentSourceId: getTopFocusSourceID = " + topFocusSourceID);
        return topFocusSourceID;
    }

    /**
     * 获取开机需恢复的音源的MediaType
     *
     * @return String 需要音源恢复的源
     */
    public String getBootResumeSource() {
        if (getBootResumeSourceIdList() == null) {
            Log.d(TAG, "getBootResumeSource: getBootResumeSourceIdList is null");
            return DsvAudioSDKConstants.OTHERS_NO_MEDIA_SOURCE;
        } else {
            Log.d(TAG, "getBootResumeSource: sourceList =  " + Arrays.toString(getBootResumeSourceIdList()));
            if (getBootResumeSourceIdList().length < 1) {
                return DsvAudioSDKConstants.OTHERS_NO_MEDIA_SOURCE;
            } else {
                return sourceIDToAudioType(getBootResumeSourceIdList()[0]);
            }
        }
    }

    /**
     * 获取开机需恢复的音源的列表
     *
     * @return int[]
     */
    public int[] getBootResumeSourceIdList() {
        if (carAudioManager == null) {
            Log.d(TAG, "getBootResumeSourceIdList: is not connect to carAudioManager!!!");
            return new int[]{};
        }
        return carAudioManager.getBootResumeSourceList();
    }
}
