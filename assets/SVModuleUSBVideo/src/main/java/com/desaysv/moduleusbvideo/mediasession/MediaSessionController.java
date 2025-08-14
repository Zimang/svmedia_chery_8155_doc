package com.desaysv.moduleusbvideo.mediasession;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class MediaSessionController {

    private String TAG = "MediaSessionController";
    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private static final String PKG_NAME = "com.desaysv.videoapp";
    private static final String SERVICE_NAME = "com.desaysv.moduleusbvideo.mediasession.USBVideoMediaBrowserService";

    public MediaSessionController() {
        super();
    }

    public void connect(Context context){
        Log.d(TAG, "connect: ");
        initMediaBrowserCompat(context);
    }


    private void initMediaBrowserCompat(final Context context) {
        if (mMediaBrowserCompat == null) {
            mMediaBrowserCompat = new MediaBrowserCompat(context, new ComponentName(PKG_NAME, SERVICE_NAME), new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    super.onConnected();
                    Log.d(TAG, "onConnected: ");
                    try {
                        initMediaController(context);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();
                    Log.d(TAG, "onConnectionSuspended: ");
                }

                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                    Log.d(TAG, "onConnectionFailed: ");
                }
            }, null);

            mMediaBrowserCompat.connect();
        }
    }


    private void initMediaController(Context context) throws RemoteException {
        mMediaControllerCompat = new MediaControllerCompat(context, mMediaBrowserCompat.getSessionToken());
        //设置监听服务端的媒体信息和状态回调
        mMediaControllerCompat.registerCallback(new MediaControllerCompat.Callback() {
            @Override
            public void onSessionReady() {
                super.onSessionReady();
                Log.d(TAG, "onSessionReady: ");
            }

            @Override
            public void onSessionDestroyed() {
                super.onSessionDestroyed();
                Log.d(TAG, "onSessionDestroyed: ");
            }

            @Override
            public void onSessionEvent(String event, Bundle extras) {
                super.onSessionEvent(event, extras);
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
            }
        });
    }

    //客户端控制服务端媒体播放
    public void play() {
        mMediaControllerCompat.getTransportControls().play();
    }

    //客户端控制服务端媒体暂停
    public void pause() {
        mMediaControllerCompat.getTransportControls().pause();
    }


}
