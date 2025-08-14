package com.desaysv.moduleusbvideo.mediasession;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.desaysv.moduleusbvideo.util.VideoStatusChangeUtil;
import com.desaysv.moduleusbvideo.util.listener.IVideoStatusChange;

import java.util.List;

public class USBVideoMediaBrowserService extends MediaBrowserServiceCompat {

    private static final String TAG = "USBVideoMediaBrowserService";
    private MediaSessionCompat mediaSessionCompat;

    public USBVideoMediaBrowserService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(this, TAG);
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSessionCompat.setActive(true);
        setSessionToken(mediaSessionCompat.getSessionToken());

        VideoStatusChangeUtil.getInstance().addVideoStatusChange(TAG, iVideoStatusChange);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoStatusChangeUtil.getInstance().removeVideoStatusChange(TAG);
    }

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.d(TAG, "onMediaButtonEvent() called with: mediaButtonEvent = [" + mediaButtonEvent + "]");
            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    };


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.d(TAG, "onGetRoot() called with: clientPackageName = [" + clientPackageName + "], clientUid = [" + clientUid + "], rootHints = [" + rootHints + "]");
        return new BrowserRoot(TAG, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren() called with: parentId = [" + parentId + "], result = [" + result + "]");

    }

    public void updatePlayStatus(boolean isPlaying) {
        Log.d(TAG, "updatePlayStatus() called with: isPlaying = [" + isPlaying + "]");
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder().setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, 0, 0);
        mediaSessionCompat.setPlaybackState(builder.build());
    }

    IVideoStatusChange iVideoStatusChange = new IVideoStatusChange() {
        @Override
        public void onVideoPlayStatus(MediaType mediaType, boolean isPlaying) {
            updatePlayStatus(isPlaying);
        }
    };
}