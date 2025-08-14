package com.desaysv.svmediaservicelogic.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.AudioManager;
import android.media.audiopolicy.AudioPolicy;
import android.os.Bundle;
import android.util.Log;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.svmediaservicelogic.systemcontrol.ModeControl;

/**
 * Created by LZM on 2020-9-27
 * Comment 用来监听整个系统音源变化，mode按键要和音源解耦，需要监听系统整体的音源变化
 *
 * @author uidp5370
 */
public class AudioSourceManager {

    private static final String TAG = "AudioSourceManager";

    private static AudioSourceManager instance;

    public static AudioSourceManager getInstance() {
        if (instance == null) {
            synchronized (AudioSourceManager.class) {
                if (instance == null) {
                    instance = new AudioSourceManager();
                }
            }
        }
        return instance;
    }

    public AudioSourceManager() {

    }

    private Context mContext;


    public void initialize(Context context) {
        mContext = context;
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        AudioPolicy.Builder builder = new AudioPolicy.Builder(mContext);
        builder.setAudioPolicyFocusListener(new AudioPolicyFocusListener());
        AudioPolicy audioPolicy = builder.build();
        int request = audioManager.registerAudioPolicy(audioPolicy);
        Log.d(TAG, "initialize: request = " + request);
        //初始化的时候，将mode的当前音源设置一下
        ModeControl.getInstance().setCurrentModeSource(AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(context));
    }

    /**
     * 底层的音频焦点变化的时候会触发的回调
     */
    private class AudioPolicyFocusListener extends AudioPolicy.AudioPolicyFocusListener {
        @Override
        public void onAudioFocusGrant(AudioFocusInfo audioFocusInfo, int i) {
            super.onAudioFocusGrant(audioFocusInfo, i);
            String audioType = getAudioType(audioFocusInfo);
            Log.d(TAG, "onAudioFocusGrant: audioType = " + audioType);
            //如果是AudioType，则设置mode当前需要切换音源的值
            if (isMusicType(audioFocusInfo)) {
                //如果是媒体类型的音源发生了改变，就需要将mode需要切换的音源进行设置
                ModeControl.getInstance().setCurrentModeSource(audioType);
            }
        }

    }

    /**
     * 获取AudioFocusInfo里面适配的媒体音源
     *
     * @param audioFocusInfo 音频焦点的信息
     * @return 音频焦点信息中的audio_type
     */
    private String getAudioType(AudioFocusInfo audioFocusInfo) {
        String car_audio_type = "";
        int usage = -1;
        AudioAttributes audioAttributes = audioFocusInfo.getAttributes();
        /*if (audioAttributes != null) {
            Bundle bundle = audioAttributes.getPrivateBundle();
            car_audio_type = (String) bundle.get("key_car_audio_type");
            usage = audioAttributes.getUsage();
        }*/
        if (car_audio_type == null) {
            car_audio_type = "";
        }
        Log.d(TAG, "getAudioType: usage = " + usage);
        Log.d(TAG, "getAudioType: car_audio_type = " + car_audio_type);
        return car_audio_type;
    }

    private boolean isMusicType(AudioFocusInfo audioFocusInfo) {
        AudioAttributes audioAttributes = audioFocusInfo.getAttributes();
        return audioAttributes.getUsage() == AudioAttributes.USAGE_MEDIA;

    }

}
