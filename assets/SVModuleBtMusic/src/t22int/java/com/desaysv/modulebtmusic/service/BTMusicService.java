package com.desaysv.modulebtmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.interfaces.listener.IBTMusicListener;
import com.desaysv.modulebtmusic.manager.BTMusicAudioManager;
import com.desaysv.modulebtmusic.manager.BTMusicManager;
import com.desaysv.svlibmediaobserver.bean.AppConstants;
import com.desaysv.svlibmediaobserver.manager.MediaObserverManager;

import java.util.Locale;

/**
 * 外部与蓝牙音乐模块交互的入口
 */
public class BTMusicService extends Service {
    private static final String TAG = "BTMusicService";

    private static final String ACTION_OPEN_SOURCE = "action.desaysv.modulebtmusic.opensource";

    private static final String PACKAGE_NAME = "com.desaysv.svaudioapp";
    private static final String CLASS_NAME = "com.desaysv.svaudioapp.ui.MainActivity";

    private static final String EXTRA_SOURCE = "source";
    private static final String EXTRA_REASON = "reason";
    private static final String EXTRA_IS_SHOW_UI = "isShowUI";
    private static final String EXTRA_VIEW = "view";

    //source
    private static final int BT_MUSIC = 13;
    //reason: 启动的原因
    private static final int REASON_DEFAULT = 1;//默认形式
    private static final int REASON_MODE = 2;//mode 启动
    private static final int REASON_BOOT = 6;//开机音源恢复
    private static final int REASON_RESUME = 7;//焦点释放音源恢复
    private static final int REASON_CARDS = 8;//主界面卡片启动
    //isShowUI: 是否需要显示前台或者后台
    private static final int FOREGROUND = 1;//需启动界面
    private static final int BACKGROUND = 2;//仅启动音源,不启动界面
    //view: 跳转的目标界面
    private static final int INTENT_DEFAULT_VIEW = 1;//默认形式
    private static final int INTENT_MAIN_VIEW = 2;//媒体源的首页
    private static final int INTENT_PLAY_VIEW = 3;//媒体源的播放页

    private boolean mIsBootResumeMessage = false;//标志是否收到开机源恢复的消息

    private String mCurrentLanguage;
    private int mCurrentUiMode;

    private boolean mIsRePlayBtMusic = true;
    private static final int mDelayResetTime = 300;//延时重置标志位

    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        BTMusicManager.getInstance().registerListener(mBTMusicListener, false);
        setServiceForeground();
        mHandler = new Handler();
        mCurrentLanguage = Locale.getDefault().getLanguage();
        mCurrentUiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        BTMusicManager.getInstance().unregisterListener(mBTMusicListener);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "handleIntent: intent is null");
            return;
        }
        String action = intent.getAction();
        if (TextUtils.equals(action, ACTION_OPEN_SOURCE)) {
            handleBTMusicOpenSource(intent);
        }
    }

    private void handleBTMusicOpenSource(Intent intent) {
        int source = intent.getIntExtra(EXTRA_SOURCE, -1);
        int reason = intent.getIntExtra(EXTRA_REASON, -1);
        int isShowUI = intent.getIntExtra(EXTRA_IS_SHOW_UI, -1);
        int view = intent.getIntExtra(EXTRA_VIEW, -1);
        Log.i(TAG, "handleBTMusicOpenSource: source = " + source + ", reason = " + reason + ", isShowUI = " + isShowUI + ", view = " + view);
        if (source != BT_MUSIC) {
            Log.w(TAG, "handleBTMusicOpenSource: source is not bt music");
            return;
        }
        switch (reason) {
            case REASON_MODE:
            case REASON_CARDS:
                Log.i(TAG, "handleBTMusicOpenSource: play");
                BTMusicManager.getInstance().play();
                if (isShowUI == FOREGROUND) {//需要打开界面
                    Intent startIntent = new Intent();
                    startIntent.setClassName(PACKAGE_NAME, CLASS_NAME);
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startIntent.putExtra(com.desaysv.mediacommonlib.bean.Constants.Source.SOURCE_KEY,
                            com.desaysv.mediacommonlib.bean.Constants.Source.SOURCE_BT);
                    if (view == INTENT_MAIN_VIEW) {
                        startIntent.putExtra(com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.KEY,
                                com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.FLAG_MAIN);
                    } else if (view == INTENT_PLAY_VIEW) {
                        startIntent.putExtra(com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.KEY,
                                com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.FLAG_PLAY);
                    }
                    startActivity(startIntent);
                }
                break;
            case REASON_BOOT:
                Log.i(TAG, "handleBTMusicOpenSource: boot resume play");
                mIsBootResumeMessage = true;
                if (BTMusicManager.getInstance().isAVRCPConnected()) {
                    //若当前AVRCP已经连接上，直接下发播放，同时会申请焦点
                    Log.i(TAG, "handleBTMusicOpenSource: AVRCP connected");
                    BTMusicManager.getInstance().play();
                    mIsBootResumeMessage = false;//标志位可复位
                } else {
                    BTMusicManager.getInstance().requestBTMusicAudioFocus();//AVRCP未连接，先申请焦点，待AVRCP连接上时再作逻辑处理
                }
                break;
        }
    }

    private final IBTMusicListener mBTMusicListener = new IBTMusicListener() {
        @Override
        public void onConnectionStateChanged(int profile, String address, int state) {
            if (profile == BaseConstants.ProfileType.AVRCP_CONTROLLER
                    && state == Constants.ProfileConnectionState.STATE_CONNECTED) {
                Log.i(TAG, "onConnectionStateChanged: AVRCP connected, address = " + address);
                boolean isFocusOnBTMusic = BTMusicAudioManager.getInstance().checkBTAudioFocusStatus();
                Log.i(TAG, "onConnectionStateChanged: isFocusOnBTMusic = " + isFocusOnBTMusic + ", mIsBootResumeMessage = " + mIsBootResumeMessage);
                //AVRCP连接上时，优先判断当前是否有收到开机音源恢复的消息mIsBootResumeMessage是否未复位，再去判断当前焦点是否还在蓝牙音乐上，在此规避收到开机源恢复消息时AVRCP还未连接上导致蓝牙音乐未播放的问题
                if (mIsBootResumeMessage) {
                    if (isFocusOnBTMusic) {
                        BTMusicManager.getInstance().play();//当前mIsBootResumeMessage未复位且焦点还在蓝牙音乐上，AVRCP连接上时还需要执行源恢复播放的逻辑
                    }
                    mIsBootResumeMessage = false;//AVRCP连接上时，无论是否执行play操作，都需要将该标志位恢复置为false
                }
            }
            if (profile == BaseConstants.ProfileType.A2DP_SINK) {
                Log.i(TAG, "onConnectionStateChanged: A2DP connection changed, state = " + state + ", address = " + address);
                //updateMiniPlayer
                SVMusicInfo musicInfo;
                if (state == BaseConstants.ProfileConnectionState.STATE_CONNECTED) {
                    SVMusicInfo musicPlayInfo = BTMusicManager.getInstance().getMusicPlayInfo();
                    if (musicPlayInfo == null || TextUtils.isEmpty(musicPlayInfo.getMediaTitle())) {
                        musicInfo = new SVMusicInfo();
                        musicInfo.setMediaTitle(getString(R.string.string_no_source));
                    } else {
                        musicInfo = musicPlayInfo;
                    }
                } else {
                    musicInfo = new SVMusicInfo();
                    musicInfo.setMediaTitle(getString(R.string.string_no_source));
                }
                MediaObserverManager.getInstance().setMediaInfo(AppConstants.Source.BT_MUSIC_SOURCE,
                        changeRadioMessageToAppMedia(musicInfo), false);
            }
        }

        @Override
        public void onMusicPlayInfoChanged(SVMusicInfo musicInfo) {
            if (musicInfo == null) {
                Log.w(TAG, "onMusicPlayInfoChanged: musicInfo is null");
                return;
            }
            if (BTMusicManager.getInstance().isA2DPConnected()) {
                //updateMiniPlayer
                MediaObserverManager.getInstance().setMediaInfo(AppConstants.Source.BT_MUSIC_SOURCE,
                        changeRadioMessageToAppMedia(musicInfo), false);
            }
        }

        @Override
        public void onMusicPlayStateChanged(int state) {
        }

        @Override
        public void onMusicPlayProgressChanged(long progress, long duration) {
        }

        @Override
        public void onMusicPlayListChanged() {
        }
    };


    private com.desaysv.svlibmediaobserver.bean.MediaInfoBean changeRadioMessageToAppMedia(SVMusicInfo musicInfo) {
        com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder appBuilder = new com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder();
        appBuilder.setSource(AppConstants.Source.BT_MUSIC_SOURCE);
        appBuilder.setPlaying(musicInfo.getPlayState() == BaseConstants.PlayState.STATE_PLAYING);
        appBuilder.setTitle(musicInfo.getMediaTitle());
//        appBuilder.setPath(musicInfo.getMediaUri().toString());
        return appBuilder.Build();
    }

    private void setServiceForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        "BTMusicService",
                        "消息通知",
                        NotificationManager.IMPORTANCE_LOW));
                Notification notification = new Notification.Builder(
                        this,
                        "BTMusicService").
                        setContentTitle("开启BTMusicService服务").build();
                startForeground(1, notification);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String language = Locale.getDefault().getLanguage();
        if (!TextUtils.equals(mCurrentLanguage, language)) {
            Log.i(TAG, "onConfigurationChanged: language changed");
            mCurrentLanguage = language;
            mIsRePlayBtMusic = false;
            BTMusicManager.getInstance().setReplayBtMusicFlag(mIsRePlayBtMusic);
            mHandler.removeCallbacks(mDelayResetRunnable);
            mHandler.postDelayed(mDelayResetRunnable, mDelayResetTime);
        }
        int uiMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (uiMode != mCurrentUiMode) {//白天UI_MODE_NIGHT_NO 黑夜UI_MODE_NIGHT_YES
            Log.i(TAG, "onConfigurationChanged: uiMode changed");
            mCurrentUiMode = uiMode;
            mIsRePlayBtMusic = false;
            BTMusicManager.getInstance().setReplayBtMusicFlag(mIsRePlayBtMusic);
            mHandler.removeCallbacks(mDelayResetRunnable);
            mHandler.postDelayed(mDelayResetRunnable, mDelayResetTime);
        }
    }

    private final Runnable mDelayResetRunnable = () -> {
        Log.i(TAG, "mDelayResetRunnable: reset");
        mIsRePlayBtMusic = true;
        BTMusicManager.getInstance().setReplayBtMusicFlag(mIsRePlayBtMusic);
    };
}
