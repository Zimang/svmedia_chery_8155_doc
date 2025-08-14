package com.desaysv.moduleusbmusic.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;

import java.util.List;

/**
 * @author uidq1846
 * @desc 媒体浏览器服务
 * @time 2023-8-16 21:26
 */
public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String TAG = MediaPlaybackService.class.getSimpleName();
    private MediaSessionCompat mediaSessionCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        initMediaSession();
        registerMusicPlayStateListener();
    }

    /**
     * 注册媒体音源播放状态回调
     */
    private void registerMusicPlayStateListener() {
        Log.d(TAG, "registerMusicPlayStateListener: ");
        ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.registerMediaStatusChangeListener(TAG, mediaStatusChange);
        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.registerMediaStatusChangeListener(TAG, mediaStatusChange);
    }

    /**
     * 取消注册媒体音源播放状态回调
     */
    private void unRegisterMusicPlayStateListener() {
        Log.d(TAG, "unRegisterMusicPlayStateListener: ");
        ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.unregisterMediaStatusChangerListener(TAG);
        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.unregisterMediaStatusChangerListener(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unRegisterMusicPlayStateListener();
        if (mediaSessionCompat != null) {
            mediaSessionCompat.release();
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        //控制客户端连接,clientPackageName是否能够连接
        Log.d(TAG, "onGetRoot: clientPackageName = " + clientPackageName + " clientUid = " + clientUid);
        return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren: parentId = " + parentId);
        //  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
        }
    }

    /**
     * 初始化媒体会话
     */
    private void initMediaSession() {
        Log.d(TAG, "initMediaSession: ");
        // Create a MediaSessionCompat
        mediaSessionCompat = new MediaSessionCompat(this, TAG);
        // Enable callbacks from MediaButtons and TransportControls
        mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mediaSessionCompat.setPlaybackState(stateBuilder.build());
        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSessionCompat.setCallback(callback);
        //Sets if this session is currently active and ready to receive commands.
        // If set to false your session's controller may not be discoverable.
        // You must set the session to active before it can start receiving media button events or transport commands.
        mediaSessionCompat.setActive(true);
        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSessionCompat.getSessionToken());
    }

    /**
     * MediaSessionCompat.Callback
     */
    private final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            Log.d(TAG, "onPlay: ");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "onPause: ");
        }
    };

    /**
     * 媒体播放状态回调
     */
    private final IMediaStatusChange mediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {

        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange: isPlaying = " + isPlaying);
            //这里需要注意的是，当前本地切换usb的时候，都会回调，不过理论上是先停止再播放的
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                    .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, 0, 0);
            mediaSessionCompat.setPlaybackState(stateBuilder.build());
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {

        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {

        }

        @Override
        public void onAlbumPicDataChange() {

        }

        @Override
        public void onLoopTypeChange() {

        }

        @Override
        public void onLyricsChange() {

        }

        @Override
        public void onPlayListChange() {

        }
    };
}

