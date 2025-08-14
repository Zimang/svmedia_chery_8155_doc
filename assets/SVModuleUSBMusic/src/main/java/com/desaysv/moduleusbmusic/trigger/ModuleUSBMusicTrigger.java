package com.desaysv.moduleusbmusic.trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.util.Log;

import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.control.MediaControlRegistrar;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.libusbmedia.interfaze.IRequestMediaPlayer;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicListSearchTrigger;
import com.desaysv.moduleusbmusic.dataPoint.PointManager;
import com.desaysv.moduleusbmusic.hardkeys.MediaKeyActionManager;
import com.desaysv.moduleusbmusic.service.MediaPlaybackService;
import com.desaysv.moduleusbmusic.utils.ImageUtils;
import com.desaysv.moduleusbmusic.utils.MusicStatusUtils;
import com.desaysv.moduleusbmusic.vr.MusicVrManager;
import com.desaysv.svlibmediastore.dao.RecentlyMusicDao;
import com.desaysv.svlibmediastore.query.MusicQuery;
import com.desaysv.svlibmediastore.receivers.MediaScanStateManager;


/**
 * Created by ${LZM} on 2019-6-3.
 * Comment USB音乐的app，一些音乐特有的初始化都是在这里实现的
 */

public class ModuleUSBMusicTrigger {

    private static final String TAG = "ModuleUSBMusicTrigger";

    private static ModuleUSBMusicTrigger instance;

    public static ModuleUSBMusicTrigger getInstance() {
        if (instance == null) {
            synchronized (ModuleUSBMusicTrigger.class) {
                if (instance == null) {
                    instance = new ModuleUSBMusicTrigger();
                }
            }
        }
        return instance;
    }

    private ModuleUSBMusicTrigger() {

    }

    //最近播放的音乐控制工具获取器
    public IGetControlTool getRecentMusicControlTool;

    //最近播放的音乐控制工具
    public IControlTool RecentMusicControlTool;

    //最近播放的音乐状态获取器
    public IStatusTool RecentMusicStatusTool;

    //Local的音乐控制工具获取器
    public IGetControlTool getLocalMusicControlTool;

    //本地的音乐控制工具
    public IControlTool LocalMusicControlTool;

    //本地的音乐状态获取器
    public IStatusTool LocalMusicStatusTool;

    //USB1的音乐控制工具获取器
    public IGetControlTool getUSB1MusicControlTool;

    //USB1的音乐控制工具
    public IControlTool USB1MusicControlTool;

    //USB1的音乐状态获取器
    public IStatusTool USB1MusicStatusTool;

    //USB2的音乐控制工具获取器
    public IGetControlTool getUSB2MusicControlTool;

    //USB2的音乐控制工具
    public IControlTool USB2MusicControlTool;

    //USB2的音乐状态获取器
    public IStatusTool USB2MusicStatusTool;

    //本地音乐的媒体播放器
    private MediaPlayer mRecentMusicPlayer;

    //本地音乐的媒体播放器
    private MediaPlayer mLocalMusicPlayer;

    //USB1音乐的媒体播放器
    private MediaPlayer mUSB1MusicPlayer;

    //USB2音乐的媒体播放器
    private MediaPlayer mUSB2MusicPlayer;


    /**
     * 整个Module功能的参数化入口
     */
    public void initialize() {
        Log.d(TAG, "initialize: start");
        //初始化音乐的控制工具
        initMusicControl();
        //初始化数据库工具
        RecentlyMusicDao.getInstance().init(AppBase.mContext);
        //初始化图片加载工具
        ImageUtils.getInstance().init(AppBase.mContext);
        //初始化暂存状态
        MusicSetting.getInstance().init();
        // 是否网络安全认证版本
        //boolean needCyberSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity();
        //Log.i(TAG, "initialize: needCyberSecurity: " + needCyberSecurity);
        MusicQuery.getInstance().setDataType(true);
        //初始化数据扫描
        MediaScanStateManager.getInstance().init(AppBase.mContext);
        //文件拷贝初始化
        CopyDeleteControl.getInstance().init();
        //初始化列表数据
        USBMusicListSearchTrigger.getInstance().initialize(AppBase.mContext);
        //初始化AIDL的音乐控制器
        MusicStatusUtils.getInstance().initialize();
        //初始化按键管理类
        MediaKeyActionManager.getInstance().init(AppBase.mContext);
        //初始化语音
        MusicVrManager.getInstance().init(AppBase.mContext);
        //初始化数据埋点
        PointManager.getInstance().init(AppBase.mContext);
        //启动mediaSession服务
        startMediaSessionService();
        registerLanguage();
        Log.d(TAG, "initialize: end");
    }

    /**
     * 注册语言广播变化监听
     */
    private void registerLanguage() {
        IntentFilter langIntentFilter = new IntentFilter();
        langIntentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        AppBase.mContext.registerReceiver(languageBroadcastReceiver, langIntentFilter);
    }

    /**
     * 语言变化广播监听
     */
    private final BroadcastReceiver languageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);
            if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                //主动通知下当前的播放信息 BUG20240606_03275 仪表不会主动更新信息
                IStatusTool statusTool = getCurrentIGetControlTool().getStatusTool();
                if (statusTool instanceof CurrentPlayInfo) {
                    CurrentPlayInfo info = (CurrentPlayInfo) statusTool;
                    info.setCurrentPlayItem(statusTool.getCurrentPlayItem());
                }
            }
        }
    };

    /**
     * 启动mediaSession服务,用于设置状态，避免按键强制分发
     */
    private void startMediaSessionService() {
        Intent intent = new Intent("android.media.browse.MediaBrowserService");
        intent.setClass(AppBase.mContext, MediaPlaybackService.class);
        AppBase.mContext.startService(intent);
    }

    /**
     * 初始化控制工具
     */
    private void initMusicControl() {
        initLocalMusicControl();
        //初始化USB1音乐的控制工具
        initUSB1MusicControl();
        //初始化USB2音乐的控制工具
        initUSB2MusicControl();
        //初始化USB2音乐的控制工具
        initRecentMusicControl();
    }

    /**
     * 初始化最近播放音乐
     */
    private void initRecentMusicControl() {
        getRecentMusicControlTool = MediaControlRegistrar.getInstance().registeredMediaTool(MediaType.RECENT_MUSIC, requestRecentMusicPlayer);
        RecentMusicControlTool = getRecentMusicControlTool.getControlTool();
        RecentMusicStatusTool = getRecentMusicControlTool.getStatusTool();
    }

    /**
     * 初始化本地音乐控制器
     */
    private void initLocalMusicControl() {
        getLocalMusicControlTool = MediaControlRegistrar.getInstance().registeredMediaTool(MediaType.LOCAL_MUSIC, requestLocalMusicPlayer);
        LocalMusicControlTool = getLocalMusicControlTool.getControlTool();
        LocalMusicStatusTool = getLocalMusicControlTool.getStatusTool();
    }

    /**
     * 初始化USB1音乐的控制工具
     */
    private void initUSB1MusicControl() {
        //初始化USB1的媒体控制器，将媒体播放器回调设置进去，用来实现媒体播放器的获取实例
        getUSB1MusicControlTool = MediaControlRegistrar.getInstance().registeredMediaTool(MediaType.USB1_MUSIC, requestUSB1MusicPlayer);
        USB1MusicControlTool = getUSB1MusicControlTool.getControlTool();
        USB1MusicStatusTool = getUSB1MusicControlTool.getStatusTool();
    }

    /**
     * 初始化USB2音乐的控制工具
     */
    private void initUSB2MusicControl() {
        getUSB2MusicControlTool = MediaControlRegistrar.getInstance().registeredMediaTool(MediaType.USB2_MUSIC, requestUSB2MusicPlayer);
        USB2MusicControlTool = getUSB2MusicControlTool.getControlTool();
        USB2MusicStatusTool = getUSB2MusicControlTool.getStatusTool();
    }

    /**
     * 最近播放音乐的播放器获取回调
     */
    private IRequestMediaPlayer requestRecentMusicPlayer = new IRequestMediaPlayer() {
        @Override
        public MediaPlayer requestMediaPlayer() {
            initRecentPlayer();
            return mRecentMusicPlayer;
        }

        @Override
        public void destroyMediaPlayer() {
            mRecentMusicPlayer = null;
        }
    };

    /**
     * 本地音乐的播放器获取回调
     */
    private IRequestMediaPlayer requestLocalMusicPlayer = new IRequestMediaPlayer() {
        @Override
        public MediaPlayer requestMediaPlayer() {
            initLocalPlayer();
            return mLocalMusicPlayer;
        }

        @Override
        public void destroyMediaPlayer() {
            mLocalMusicPlayer = null;
        }
    };

    /**
     * USB1音乐的播放器获取回调
     */
    private IRequestMediaPlayer requestUSB1MusicPlayer = new IRequestMediaPlayer() {
        @Override
        public MediaPlayer requestMediaPlayer() {
            initUSB1Player();
            return mUSB1MusicPlayer;
        }

        @Override
        public void destroyMediaPlayer() {
            mUSB1MusicPlayer = null;
        }
    };

    /**
     * 初始化player
     */
    private void initRecentPlayer() {
        if (mRecentMusicPlayer == null) {
            Log.d(TAG, "initRecentPlayer: ");
            mRecentMusicPlayer = new MediaPlayer();
        }
    }

    /**
     * 初始化player
     */
    private void initLocalPlayer() {
        if (mLocalMusicPlayer == null) {
            Log.d(TAG, "initLocalPlayer: ");
            mLocalMusicPlayer = new MediaPlayer();
        }
    }

    /**
     * 初始化player
     */
    private void initUSB1Player() {
        if (mUSB1MusicPlayer == null) {
            Log.d(TAG, "initUSB1Player: ");
            mUSB1MusicPlayer = new MediaPlayer();
        }
    }


    /**
     * USB2音乐的播放器获取回调
     */
    private IRequestMediaPlayer requestUSB2MusicPlayer = new IRequestMediaPlayer() {
        @Override
        public MediaPlayer requestMediaPlayer() {
            initUSB2Player();
            return mUSB2MusicPlayer;
        }

        @Override
        public void destroyMediaPlayer() {
            mUSB2MusicPlayer = null;
        }
    };

    /**
     * 初始化player
     */
    private void initUSB2Player() {
        if (mUSB2MusicPlayer == null) {
            Log.d(TAG, "initUSB2Player: ");
            mUSB2MusicPlayer = new MediaPlayer();
        }
    }

    /**
     * 获取当前的获取器
     */
    public IGetControlTool getCurrentIGetControlTool() {
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
        Log.d(TAG, "getCurrentIGetControlTool: mediaType = " + mediaType);
        if (MediaType.USB1_MUSIC.ordinal() == mediaType) {
            return getUSB1MusicControlTool;
        } else if (MediaType.LOCAL_MUSIC.ordinal() == mediaType) {
            return getLocalMusicControlTool;
        } else if (MediaType.USB2_MUSIC.ordinal() == mediaType) {
            return getUSB2MusicControlTool;
        } else if (MediaType.RECENT_MUSIC.ordinal() == mediaType) {
            return getRecentMusicControlTool;
        }
        return getUSB1MusicControlTool;
    }

    //增加这个接口给MainActivity的埋点使用
    public boolean isPlaying() {
        return getCurrentIGetControlTool() != null && getCurrentIGetControlTool().getStatusTool() != null
                && getCurrentIGetControlTool().getStatusTool().isPlaying();
    }
}
