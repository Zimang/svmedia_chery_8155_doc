// IAudioFocusManager.aidl
package com.desaysv.audiosdk;

import com.desaysv.audiosdk.IOnAudioFocusChangeListener;

interface IAudioFocusManager {

        //申请媒体的音频焦点
       int requestAudioFocus(String audioType,String clineId, IOnAudioFocusChangeListener onAudioFocusChangeListener);

        //释放媒体的音频焦点
       int abandonAudioFocus(String clineId);

}
