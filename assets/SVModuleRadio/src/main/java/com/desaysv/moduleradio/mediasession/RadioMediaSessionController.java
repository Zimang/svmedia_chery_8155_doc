package com.desaysv.moduleradio.mediasession;


import android.content.ComponentName;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;

public class RadioMediaSessionController {

    private static final String TAG = "RadioMediaSessionController";
    private static RadioMediaSessionController mInstance;

    public static RadioMediaSessionController getInstance() {
        if (mInstance == null) {
            synchronized (RadioMediaSessionController.class) {
                if (mInstance == null) {
                    mInstance = new RadioMediaSessionController();
                }
            }
        }
        return mInstance;
    }

    private RadioMediaSessionController() {
    }


    //初始化MediaSession控制器
    MediaBrowserCompat mediaBrowser;
    public void init(){
        //通过MediaBrowser绑定服务。重点是这个绑定，确保激活对端服务，以使得MediaSession能更新media button给到对应的 RadioMediaSessionBrowserService
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowserCompat(AppBase.mContext, new ComponentName("com.desaysv.svaudioapp", "com.desaysv.moduleradio.mediasession.RadioMediaSessionBrowserService")
                    , new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    super.onConnected();
                    Log.d(TAG, "onConnected");
                    initMediaSession();
                }

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();
                    Log.d(TAG, "onConnectionSuspended");
                }

                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                    Log.d(TAG, "onConnectionFailed");
                }

            }, null);

            mediaBrowser.connect();
        }
    }

    //初始化MediaSession，伪代码，不需要实现
    private MediaControllerCompat currentMediaController;
    private void initMediaSession(){
        Log.d(TAG,"initMediaSession with test");
        // Get a MediaController for the MediaSession.
        currentMediaController =
                new MediaControllerCompat(AppBase.mContext, mediaBrowser.getSessionToken());
        //currentMediaController.registerCallback(callback);

    }
}
