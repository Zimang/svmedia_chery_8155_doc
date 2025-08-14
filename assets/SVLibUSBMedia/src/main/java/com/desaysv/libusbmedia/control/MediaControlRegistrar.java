package com.desaysv.libusbmedia.control;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.libusbmedia.action.MediaControlAction;
import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.libusbmedia.interfaze.IRequestMediaPlayer;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;

import java.util.Objects;

/**
 * @author LZM
 * @date 2019-6-12
 * 这个类主要是对外获取媒体对外的接口
 */

public class MediaControlRegistrar {

    private static final String TAG = "MediaControlRegistrar";


    private static MediaControlRegistrar instance;

    public static MediaControlRegistrar getInstance() {
        if (instance == null) {
            synchronized (MediaControlRegistrar.class) {
                if (instance == null) {
                    instance = new MediaControlRegistrar();
                }
            }
        }
        return instance;
    }

    private MediaControlRegistrar() {
        initUtils();
    }

    /**
     * 初始化工具类
     */
    private void initUtils() {
        MediaPlayStatusSaveUtils.getInstance().initialize(AppBase.mContext);
        //焦点工具初始化
        AudioFocusUtils.getInstance().initialize(AppBase.mContext);
    }

    /**
     * 注册媒体，获取获取媒体控制器，状态获取，状态回调的接口
     *
     * @param mediaType           USB1_MUSIC,USB2_MUSIC,USB1_VIDEO,USB2_VIDEO,
     * @param iRequestMediaPlayer 传入的回调，主要用来实现mediaplay的传输，由获取方提供mediaplay的实例
     * @return IGetControlTool 媒体获取接口
     */
    public IGetControlTool registeredMediaTool(MediaType mediaType, IRequestMediaPlayer iRequestMediaPlayer) {
        Objects.requireNonNull(MediaControlAction.getInstance(mediaType)).setRequestMediaPlayer(iRequestMediaPlayer);
        if (mediaType == MediaType.USB1_MUSIC) {
            return getUSB1MusicControlTool;
        } else if (mediaType == MediaType.USB2_MUSIC) {
            return getUSB2MusicControlTool;
        } else if (mediaType == MediaType.USB1_VIDEO) {
            return getUSB1VideoControlTool;
        } else if (mediaType == MediaType.USB2_VIDEO) {
            return getUSB2VideoControlTool;
        } else if (mediaType == MediaType.LOCAL_MUSIC) {
            return getLocalMusicControlTool;
        } else if (mediaType == MediaType.RECENT_MUSIC) {
            return getRecentMusicControlTool;
        }
        return null;
    }

    /**
     * USB1音乐控制器
     */
    private final IGetControlTool getUSB1MusicControlTool = new IGetControlTool() {
        @Override
        public IControlTool getControlTool() {
            return MediaControlTool.getInstance(MediaType.USB1_MUSIC);
        }

        @Override
        public IStatusTool getStatusTool() {
            return CurrentPlayInfo.getInstance(MediaType.USB1_MUSIC);
        }

        @Override
        public void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_MUSIC)).registerMediaStatusChangeListener(className, mediaStatusChangeListener);
        }

        @Override
        public void unregisterMediaStatusChangerListener(String className) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_MUSIC)).unregisterMediaStatusChangerListener(className);
        }
    };

    /**
     * USB2音乐控制器
     */
    private final IGetControlTool getUSB2MusicControlTool = new IGetControlTool() {
        @Override
        public IControlTool getControlTool() {
            return MediaControlTool.getInstance(MediaType.USB2_MUSIC);
        }

        @Override
        public IStatusTool getStatusTool() {
            return CurrentPlayInfo.getInstance(MediaType.USB2_MUSIC);
        }

        @Override
        public void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB2_MUSIC)).registerMediaStatusChangeListener(className, mediaStatusChangeListener);
        }

        @Override
        public void unregisterMediaStatusChangerListener(String className) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB2_MUSIC)).unregisterMediaStatusChangerListener(className);
        }
    };

    /**
     * USB1视频控制器
     */
    private final IGetControlTool getUSB1VideoControlTool = new IGetControlTool() {
        @Override
        public IControlTool getControlTool() {
            return MediaControlTool.getInstance(MediaType.USB1_VIDEO);
        }

        @Override
        public IStatusTool getStatusTool() {
            return CurrentPlayInfo.getInstance(MediaType.USB1_VIDEO);
        }

        @Override
        public void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_VIDEO)).registerMediaStatusChangeListener(className, mediaStatusChangeListener);
        }

        @Override
        public void unregisterMediaStatusChangerListener(String className) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB1_VIDEO)).unregisterMediaStatusChangerListener(className);
        }
    };

    /**
     * USB2视频控制器
     */
    private final IGetControlTool getUSB2VideoControlTool = new IGetControlTool() {
        @Override
        public IControlTool getControlTool() {
            return MediaControlTool.getInstance(MediaType.USB2_VIDEO);
        }

        @Override
        public IStatusTool getStatusTool() {
            return CurrentPlayInfo.getInstance(MediaType.USB2_VIDEO);
        }

        @Override
        public void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB2_VIDEO)).registerMediaStatusChangeListener(className, mediaStatusChangeListener);
        }

        @Override
        public void unregisterMediaStatusChangerListener(String className) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.USB2_VIDEO)).unregisterMediaStatusChangerListener(className);
        }
    };

    /**
     * 本地音乐控制器
     */
    private final IGetControlTool getLocalMusicControlTool = new IGetControlTool() {
        @Override
        public IControlTool getControlTool() {
            return MediaControlTool.getInstance(MediaType.LOCAL_MUSIC);
        }

        @Override
        public IStatusTool getStatusTool() {
            return CurrentPlayInfo.getInstance(MediaType.LOCAL_MUSIC);
        }

        @Override
        public void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.LOCAL_MUSIC)).registerMediaStatusChangeListener(className, mediaStatusChangeListener);
        }

        @Override
        public void unregisterMediaStatusChangerListener(String className) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.LOCAL_MUSIC)).unregisterMediaStatusChangerListener(className);
        }
    };

    /**
     * 最近播放音乐控制器
     */
    private final IGetControlTool getRecentMusicControlTool = new IGetControlTool() {
        @Override
        public IControlTool getControlTool() {
            return MediaControlTool.getInstance(MediaType.RECENT_MUSIC);
        }

        @Override
        public IStatusTool getStatusTool() {
            return CurrentPlayInfo.getInstance(MediaType.RECENT_MUSIC);
        }

        @Override
        public void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.RECENT_MUSIC)).registerMediaStatusChangeListener(className, mediaStatusChangeListener);
        }

        @Override
        public void unregisterMediaStatusChangerListener(String className) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(MediaType.RECENT_MUSIC)).unregisterMediaStatusChangerListener(className);
        }
    };
}
