package com.desaysv.audiosdk.manager.audiofocusmanager;

import android.car.Car;
import android.car.media.CarAudioManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.util.Log;

import com.desaysv.audiosdk.IAudioFocusManager;
import com.desaysv.audiosdk.bean.AudioSourceBean;
import com.desaysv.audiosdk.bean.AudioSourceBeanFactory;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.audiosdk.utils.SourceTypeUtils;

/**
 * Created by LZM on 2019-12-12
 * Comment 音频焦点的控制入口
 *
 * @author uidp5370
 */
public class DsvAudioFocusManager implements IDsvAudioFocusManager {

    private static final String TAG = "DsvAudioFocusManager";

    private boolean isInitialize = false;

    private static IDsvAudioFocusManager instance;
    private CarAudioManager mCarAudioManager;

    public static IDsvAudioFocusManager getInstance() {
        if (instance == null) {
            synchronized (DsvAudioFocusManager.class) {
                if (instance == null) {
                    instance = new DsvAudioFocusManager();
                }
            }
        }
        return instance;
    }

    private DsvAudioFocusManager() {

    }

    private IAudioFocusManager mAudioFocusManger;

    private Context mContext;

    private AudioManager mAudioManager;

    /**
     * 初始化，需要在服务绑定完成后，去初始化
     *
     * @param context
     * @param iAudioFocusManager AIDL接口
     */
    @Override
    public void initialize(Context context, com.desaysv.audiosdk.IAudioFocusManager iAudioFocusManager) {
        mContext = context;
        mAudioFocusManger = iAudioFocusManager;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        Car mCar = Car.createCar(context);
        mCarAudioManager = (CarAudioManager) mCar.getCarManager(Car.AUDIO_SERVICE);
        isInitialize = true;
    }

    /**
     * 申请音频焦点
     *
     * @param audioType                  定义的媒体类型，fm，usb，am，在线音乐等
     * @param onAudioFocusChangeListener android原生的音频焦点回调
     * @return AudioManager.AUDIOFOCUS_GAIN 等原生定义焦点返回值
     */
    @Override
    public int requestAudioFocus(String audioType, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        int requestStatus = AudioManager.AUDIOFOCUS_LOSS;
        Log.d(TAG, "requestAudioFocus: audioType = " + audioType + " onAudioFocusChangeListener = " + onAudioFocusChangeListener);
        //AudioSourceBean将原生的音频焦点回调和AIDL回调对应起来
        AudioSourceBean audioSourceBean = AudioSourceBeanFactory.getInstance().getAudioSourceBean(audioType, onAudioFocusChangeListener);
        if (audioSourceBean == null) {
            Log.e(TAG, "requestAudioFocus: audioSourceBean is error play check the listener");
            return requestStatus;
        }
        try {
            //add by lzm 只有媒体类型的音频焦点才需要进行音频焦点的逻辑判断，如果是有音源的话，那就直接申请。
            if (SourceTypeUtils.MEDIA_TYPE.equals(SourceTypeUtils.getFocusTypeFormSource(audioType))) {
                requestStatus = mCarAudioManager.checkAudioFocusState(AudioFocusUtils.getInstance().audioTypeToSourceID(audioType));
            }
            Log.d(TAG, "requestAudioFocus: audioType = " + audioType
                    + " mAudioFocusManger = " + mAudioFocusManger + " requestStatus = " + requestStatus);
            //如果音频焦点不是已经获取，或者是降音，那说明音频焦点不在自己手上，需要重新申请，如果是的话，那就说明音频焦点在自己手上，不用重新申请
            if (requestStatus != AudioManager.AUDIOFOCUS_GAIN && requestStatus != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                Log.d(TAG, "requestAudioFocus: action request");
                //将原生的AIDL回调和AudioManager的String组合层特征值
                String clientId = new String(mAudioFocusManger.toString() + onAudioFocusChangeListener.toString());
                Log.d(TAG, "requestAudioFocus: clientId = " + clientId);
                requestStatus = mAudioFocusManger.requestAudioFocus(audioType, clientId, audioSourceBean.onAudioFocusChangeListener);
            } else {
                requestStatus = AudioManager.AUDIOFOCUS_GAIN;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "requestAudioFocus: requestStatus = " + requestStatus);
        return requestStatus;
    }

    /**
     * 释放音频焦点
     *
     * @param onAudioFocusChangeListener 焦点回调，根据焦点回调来释放音频焦点
     * @return AudioManager.AUDIOFOCUS_GAIN 等原生定义焦点返回值
     */
    @Override
    public int abandonAudioFocus(AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        int abandonStatus = AudioManager.AUDIOFOCUS_LOSS;
        AudioSourceBean audioSourceBean = AudioSourceBeanFactory.getInstance().getAudioSourceBean(onAudioFocusChangeListener);
        if (audioSourceBean == null) {
            Log.e(TAG, "abandonAudioFocus: audioSourceBean is error play check the listener");
            return abandonStatus;
        }
        try {
            Log.d(TAG, "abandonAudioFocus: audioSourceBean = " + audioSourceBean);
            //只需要传入回调的clientId就能区分是那个音频焦点
            String clientId = new String(mAudioFocusManger.toString() + onAudioFocusChangeListener.toString());
            Log.d(TAG, "abandonAudioFocus: clientId = " + clientId);
            abandonStatus = mAudioFocusManger.abandonAudioFocus(clientId);
            AudioSourceBeanFactory.getInstance().removeAudioSourceBean(onAudioFocusChangeListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "abandonAudioFocus: abandonStatus = " + abandonStatus);
        return abandonStatus;
    }


    /**
     * 获取当前媒体音频焦点根据是否已经初始化成功
     *
     * @return
     */
    @Override
    public boolean isInitialize() {
        Log.d(TAG, "isInitialize: isInitialize = " + isInitialize);
        return isInitialize;
    }


}
