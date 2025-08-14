package com.desaysv.moduleusbmusic.ui.activitys;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.mediacommonlib.ui.BaseActivity;

import java.lang.ref.WeakReference;

/**
 * @author uidq1846
 * @desc 基本音乐类
 * @time 2023-1-4 21:41
 */
public abstract class BaseMusicActivity extends BaseActivity {
    protected Handler handler;
    protected static final int UPDATE_TIME = 0x01;
    protected static final int UPDATE_ALBUM = UPDATE_TIME + 1;
    protected static final int UPDATE_SONG_INFO = UPDATE_ALBUM + 1;
    protected static final int UPDATE_PLAY_STATE = UPDATE_SONG_INFO + 1;
    protected static final int UPDATE_LRC = UPDATE_PLAY_STATE + 1;
    protected static final int UPDATE_MEDIA_TYPE = UPDATE_LRC + 1;
    protected static final int UPDATE_PLAY_LIST = UPDATE_MEDIA_TYPE + 1;
    protected static final int UPDATE_CYCLE_TYPE = UPDATE_PLAY_LIST + 1;

    @Override
    public void onStart() {
        super.onStart();
        getMusicControlTool().registerMediaStatusChangeListener(TAG, iMediaStatusChange);
    }

    @Override
    public void onStop() {
        super.onStop();
        //清除所有回调
        handler.removeCallbacksAndMessages(null);
        getMusicControlTool().unregisterMediaStatusChangerListener(TAG);
    }

    @Override
    public void initData() {
        initHandler();
    }

    private void initHandler() {
        handler = new MediaHandler(this);
    }

    /**
     * 更新播放状态
     */
    protected void updatePlayState() {
        Log.d(TAG, "updatePlayState: ");
        handler.removeMessages(UPDATE_PLAY_STATE);
        handler.sendEmptyMessage(UPDATE_PLAY_STATE);
    }

    /**
     * 更新播放时间
     */
    protected void updateTime() {
        handler.removeMessages(UPDATE_TIME);
        handler.sendEmptyMessage(UPDATE_TIME);
    }

    /**
     * 更新专辑图
     */
    protected void updateAlbum() {
        Log.d(TAG, "updateAlbum: ");
        handler.removeMessages(UPDATE_ALBUM);
        handler.sendEmptyMessage(UPDATE_ALBUM);
    }

    /**
     * 更新播放信息
     */
    protected void updateInfo() {
        Log.d(TAG, "updateInfo: ");
        handler.removeMessages(UPDATE_SONG_INFO);
        handler.sendEmptyMessage(UPDATE_SONG_INFO);
    }

    /**
     * 更新循环状态
     */
    protected void updateCycleType() {
        Log.d(TAG, "updateCycleType: ");
        handler.removeMessages(UPDATE_CYCLE_TYPE);
        handler.sendEmptyMessage(UPDATE_CYCLE_TYPE);
    }

    /**
     * 更新播放列表
     */
    protected void updatePlayList() {
        Log.d(TAG, "updatePlayList: ");
        handler.removeMessages(UPDATE_PLAY_LIST);
        handler.sendEmptyMessage(UPDATE_PLAY_LIST);
    }

    /**
     * 音乐状态监听
     * 当前状态是在子线程刷新的，所以需要变更成为主线程
     */
    private final IMediaStatusChange iMediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            updateInfo();
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            updatePlayState();
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            updateTime();
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {
            handler.removeMessages(UPDATE_MEDIA_TYPE);
            Message obtain = Message.obtain();
            obtain.what = UPDATE_MEDIA_TYPE;
            obtain.arg1 = mediaPlayType.ordinal();
            handler.sendMessage(obtain);
        }

        @Override
        public void onAlbumPicDataChange() {
            updateAlbum();
        }

        @Override
        public void onLoopTypeChange() {
            updateCycleType();
        }

        @Override
        public void onLyricsChange() {
            handler.removeMessages(UPDATE_LRC);
            handler.sendEmptyMessage(UPDATE_LRC);
        }

        @Override
        public void onPlayListChange() {
            updatePlayList();
        }
    };

    /**
     * 采用弱引用
     */
    private static class MediaHandler extends Handler {
        WeakReference<BaseMusicActivity> mWeakReference;

        private MediaHandler(BaseMusicActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            BaseMusicActivity activity = mWeakReference.get();
            activity.dispatchMessage(msg, activity);
        }
    }

    /**
     * 分发事件给子view处理
     *
     * @param msg      msg
     * @param activity BaseMusicActivity
     */
    protected abstract void dispatchMessage(Message msg, BaseMusicActivity activity);

    /**
     * 获取具体的控制类
     *
     * @return IGetControlTool
     */
    protected abstract IGetControlTool getMusicControlTool();
}
