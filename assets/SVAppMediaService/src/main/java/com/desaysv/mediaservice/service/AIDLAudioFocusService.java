package com.desaysv.mediaservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.audiosdk.IAudioFocusManager;
import com.desaysv.audiosdk.IOnAudioFocusChangeListener;
import com.desaysv.mediacommonlib.utils.ServiceUtils;
import com.desaysv.mediaservice.manager.AudioFocusControlManager;

/**
 * Created by LZM on 2019-12-12
 * Comment 音频焦点申请的AIDL
 * @author uidp5370
 */
public class AIDLAudioFocusService extends Service {


    private static final String TAG = "AIDLAudioFocusService";

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceUtils.startForegroundNotification(this, "AIDLAudioFocusService", "AIDLAudioFocusService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: intent = " + intent + " iBinder = " + iBinder);
        return iBinder;
    }

    /**
     * 申请媒体音频焦点的AIDL
     */
    private IAudioFocusManager.Stub iBinder = new IAudioFocusManager.Stub() {
        @Override
        public int requestAudioFocus(String audioType, String clientId, IOnAudioFocusChangeListener onAudioFocusChangeListener) throws RemoteException {
            Log.d(TAG, "requestAudioFocus: audioType = " + audioType + " clientId = " + clientId);
            return AudioFocusControlManager.getInstance().requestAudioFocus(audioType, clientId, onAudioFocusChangeListener);
        }

        @Override
        public int abandonAudioFocus(String clientId) throws RemoteException {
            Log.d(TAG, "abandonAudioFocus: clientId = " + clientId);
            return AudioFocusControlManager.getInstance().abandonAudioFocus(clientId);
        }

    };


}
