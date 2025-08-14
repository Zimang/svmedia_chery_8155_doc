// IAudioFocusManager.aidl
package com.desaysv.audiosdk;


interface IOnAudioFocusChangeListener {

    oneway void onAudioFocusChange(int focusStatus);

}
