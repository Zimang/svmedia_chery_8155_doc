package com.desaysv.moduleradio.mediasession;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;

import java.util.List;

public class RadioMediaSessionBrowserService extends MediaBrowserServiceCompat {

    /**
     * 模拟的播放器代替MediaSession.FLAG_EXCLUSIVE_GLOBAL_PRIORITY
     */
    private MediaPlayer mediaPlayer;

    private static final String TAG = "RadioMediaSessionBrowserService";


    private MediaSessionCompat mediaSessionCompat;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(this,"RadioMediaSessionBrowserService");
        mediaSessionCompat.setCallback(sessionCallback);
        //通过焦点来更新是否需要激活
        mediaSessionCompat.setActive(true);
        mediaPlayer = new MediaPlayer();
        Log.d(TAG, "onCreate: new MediaPlayer");
        setSessionToken(mediaSessionCompat.getSessionToken());
        //注册Radio状态的回调
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        //注册Radio列表变化的回调
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("RadioMediaSessionBrowserService",null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    private MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            Log.d(TAG,"onPlay");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG,"onPause");
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.d(TAG,"onSkipToNext");
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.d(TAG,"onSkipToPrevious");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG,"onStop");
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.d(TAG,"onMediaButtonEvent");
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

    };


    private RadioMessage currentMessage;

    private boolean isCurrentPlaying = false;

    IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            //转换为Metadata更新
            RadioMessage tempMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();

            Log.d(TAG, "onCurrentRadioMessageChange,currentMessage:" + tempMessage +", tempMessage:"+currentMessage);
            if (tempMessage.getRadioType() == RadioMessage.DAB_TYPE){
                if (currentMessage != null && currentMessage.getDabMessage() != null && currentMessage.getDabMessage().getFrequency() == tempMessage.getDabMessage().getFrequency()
                        && currentMessage.getDabMessage().getServiceId() == tempMessage.getDabMessage().getServiceId()
                        && currentMessage.getDabMessage().getServiceComponentId() == tempMessage.getDabMessage().getServiceComponentId()
                        && currentMessage.getDabMessage().getLogoLen() == tempMessage.getDabMessage().getLogoLen()) {//频点值相等，说明是同一个

                } else {
                    currentMessage = tempMessage.Clone();
                    MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE,currentMessage.getDabMessage().getShortProgramStationName())
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,currentMessage.getDabMessage().getShortEnsembleLabel());

                    if (currentMessage.getDabMessage().getLogoDataList() != null){
                        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeByteArray(currentMessage.getDabMessage().getLogoDataList(), 0,currentMessage.getDabMessage().getLogoLen()));
                    }
                    RatingCompat ratingCompat = RatingCompat.newHeartRating(currentMessage.isCollect());
                    builder.putRating(MediaMetadataCompat.METADATA_KEY_RATING, ratingCompat);
                    mediaSessionCompat.setMetadata(builder.build());
                }
            }else {
                if (currentMessage != null && currentMessage.getRadioFrequency() == tempMessage.getRadioFrequency()) {//频点值相等，说明是同一个

                } else {
                    currentMessage = tempMessage.Clone();
                    MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, String.valueOf(currentMessage.getRadioFrequency()))
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentMessage.getRadioMessageSource());
                    RatingCompat ratingCompat = RatingCompat.newHeartRating(currentMessage.isCollect());
                    builder.putRating(MediaMetadataCompat.METADATA_KEY_RATING, ratingCompat);

                    //传递RDS内容
                    if (currentMessage.getRdsRadioText() != null){
                        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentMessage.getRdsRadioText().getProgramStationName());
                    }
                    mediaSessionCompat.setMetadata(builder.build());
                    //更新媒体源信息
                    //mediaSessionCompat.sendSessionEvent();
                }
            }
        }
        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange: "+isPlaying);
            PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder().setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,0,0);
            mediaSessionCompat.setPlaybackState(builder.build());
            try {
                if(isPlaying && !mediaPlayer.isPlaying()){
                    Log.d(TAG, "onPlayStatusChange: mediaPlayer start");
                    mediaPlayer.start();
                }else {
                    Log.d(TAG, "onPlayStatusChange: mediaPlayer stop");
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Log.d(TAG, "onPlayStatusChange: error : "+e.getMessage());
            }

            if (isCurrentPlaying == isPlaying){
                //状态相同，不需要更新metadate和媒体源
            }else {
                isCurrentPlaying = isPlaying;
                if (isPlaying) {
                    RadioMessage tempMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
                    Log.d(TAG, "onPlayStatusChange,currentMessage"+tempMessage);
                    MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, String.valueOf(tempMessage.getRadioFrequency()))
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, tempMessage.getRadioMessageSource());
                    RatingCompat ratingCompat = RatingCompat.newHeartRating(tempMessage.isCollect());
                    metadataBuilder.putRating(MediaMetadataCompat.METADATA_KEY_RATING,ratingCompat);
                    mediaSessionCompat.setMetadata(metadataBuilder.build());
                }
            }
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged: ");
        }

        @Override
        public void onSearchStatusChange(final boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange: isSearching = " + isSearching);
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG, "onSeekStatusChange: isSeeking = " + isSeeking);
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG, "onRDSFlagInfoChange: RDSFlagInfo = " + info);
        }

        @Override
        public void onRDSSettingsStatus(RDSSettingsSwitch rdsSettingsSwitch) {
            Log.d(TAG, "onRDSSettingsStatus: rdsSettingsSwitch = " + rdsSettingsSwitch);
        }
    };

    private boolean currentCollect = false;
    private void updateCurrentCollectStatues(){
        RadioMessage currentMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        boolean temp = currentMessage.isCollect();
        Log.d(TAG,"updateCurrentCollectStatues，currentCollect："+currentCollect+", temp: "+temp);
        if (currentCollect ^ temp){//相同为0,不同为1
            currentCollect = temp;

        }else {
            Log.d(TAG,"updateCurrentCollectStatues, is same statues,return");
        }
    }


    private final IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange");
            updateCurrentCollectStatues();
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange");
            updateCurrentCollectStatues();
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG, "onDABCollectListChange");
            updateCurrentCollectStatues();
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange");
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG, "onAMEffectListChange");
        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG, "onDABEffectListChange");
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG, "onFMAllListChange");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG, "onAMAllListChange");
        }

        @Override
        public void onDABAllListChange() {
            Log.d(TAG, "onDABAllListChange");
        }
    };
}
