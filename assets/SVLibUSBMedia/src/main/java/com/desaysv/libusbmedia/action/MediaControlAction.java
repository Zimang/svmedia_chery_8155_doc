package com.desaysv.libusbmedia.action;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.control.MediaControlTool;
import com.desaysv.libusbmedia.interfaze.IMediaStatusTool;
import com.desaysv.libusbmedia.interfaze.IPlayControl;
import com.desaysv.libusbmedia.interfaze.IRequestMediaPlayer;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.libusbmedia.utils.USBFocusUtils;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.utils.SvMediaDataSource;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Future;


/**
 * Created by uidp5370 on 2019-6-12.
 * 媒体动作的执行类
 */

public class MediaControlAction implements IPlayControl, IMediaStatusTool {

    private static final String TAG = "MediaControlAction";
    private static MediaControlAction LocalMusicControlAction;
    private static MediaControlAction USB1MusicControlAction;
    private static MediaControlAction USB2MusicControlAction;
    private static MediaControlAction USB1VideoControlAction;
    private static MediaControlAction USB2VideoControlAction;
    private static MediaControlAction RecentMusicControlAction;
    public static final float DEFAULT_SPEED = 1.0f;
    public static final float DOUBLE_SPEED = 2.0f;
    private MediaType mCurrentMediaType;
    private IRequestMediaPlayer mRequestMediaPlayer;
    private String mCurrentPlayPath;
    private float speed = DEFAULT_SPEED;
    private boolean isPrepare = false;
    private Future<?> mGetSelectedTrackSubmit;

    /**
     * 单例模式，实现多个单例，通过mediaType判断，区分为USB1音乐，USB2音乐，USB1视频，USB2视频
     *
     * @param mediaType USB1_MUSIC; USB2_MUSIC; USB1_VIDEO; USB2_VIDEO;
     * @return 获取到的控制单例
     */
    public static MediaControlAction getInstance(MediaType mediaType) {
        if (mediaType == MediaType.USB1_MUSIC) {
            if (USB1MusicControlAction == null) {
                synchronized (MediaControlAction.class) {
                    if (USB1MusicControlAction == null) {
                        USB1MusicControlAction = new MediaControlAction(mediaType);
                    }
                }
            }
            return USB1MusicControlAction;
        } else if (mediaType == MediaType.USB2_MUSIC) {
            if (USB2MusicControlAction == null) {
                synchronized (MediaControlAction.class) {
                    if (USB2MusicControlAction == null) {
                        USB2MusicControlAction = new MediaControlAction(mediaType);
                    }
                }
            }
            return USB2MusicControlAction;
        } else if (mediaType == MediaType.USB1_VIDEO) {
            if (USB1VideoControlAction == null) {
                synchronized (MediaControlAction.class) {
                    if (USB1VideoControlAction == null) {
                        USB1VideoControlAction = new MediaControlAction(mediaType);
                    }
                }
            }
            return USB1VideoControlAction;
        } else if (mediaType == MediaType.USB2_VIDEO) {
            if (USB2VideoControlAction == null) {
                synchronized (MediaControlAction.class) {
                    if (USB2VideoControlAction == null) {
                        USB2VideoControlAction = new MediaControlAction(mediaType);
                    }
                }
            }
            return USB2VideoControlAction;
        } else if (mediaType == MediaType.LOCAL_MUSIC) {
            if (LocalMusicControlAction == null) {
                synchronized (MediaControlAction.class) {
                    if (LocalMusicControlAction == null) {
                        LocalMusicControlAction = new MediaControlAction(mediaType);
                    }
                }
            }
            return LocalMusicControlAction;
        } else if (mediaType == MediaType.RECENT_MUSIC) {
            if (RecentMusicControlAction == null) {
                synchronized (MediaControlAction.class) {
                    if (RecentMusicControlAction == null) {
                        RecentMusicControlAction = new MediaControlAction(mediaType);
                    }
                }
            }
            return RecentMusicControlAction;
        }
        Log.e(TAG, "getInstance: with error parameter");
        return null;
    }

    private static final int UPDATE_PLAY_TIME = 1;
    private static final int PLAY_STATE_ERROR = 2;
    private static final int SAVE_MEDIA_PLAY_STATUS = 3;
    private static final int CHG_SEEKING_TO_FALSE_VALUE = 4;

    private Handler mHandler;

    /**
     * 构造函数
     *
     * @param mediaType USB1_MUSIC; USB2_MUSIC; USB1_VIDEO; USB2_VIDEO;
     */
    private MediaControlAction(MediaType mediaType) {
        mCurrentMediaType = mediaType;
        HandlerThread thread = new HandlerThread(mCurrentMediaType + "");
        thread.start();
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG, "handleMessage: what = " + msg.what);
                switch (msg.what) {
                    case UPDATE_PLAY_TIME:
                        updatePlayTime();
                        break;
                    case PLAY_STATE_ERROR:
                        Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.NEXT, ChangeReasonData.ON_ERROR);
                        break;
                    case SAVE_MEDIA_PLAY_STATUS:
                        saveCurrentMediaStatus(mCurrentPlayPath);
                        break;
                    case CHG_SEEKING_TO_FALSE_VALUE:
                        isSeekingTo = false;
                        if (!mHandler.hasMessages(UPDATE_PLAY_TIME)) {
                            mHandler.sendEmptyMessageDelayed(UPDATE_PLAY_TIME, UPDATE_TIME_INTERVAL);
                        }
                        break;
                }

            }
        };
    }


    /******************************************控制接口的回调***********************************/

    /**
     * 打开媒体的接口
     *
     * @param path 媒体的路径
     */
    @Override
    public synchronized void openMedia(Uri path) {
        mHandler.removeCallbacksAndMessages(null);
        mCurrentPlayPath = path.getPath();
        Log.d(TAG, "openMedia: path = " + path + " mCurrentPlayPath = " + mCurrentPlayPath);
        try {
            synchronized (MediaControlAction.class) {//说明，由于采用的子线程，mMediaPlayer会出现空指针的情况，需同步操作
                //MediaControlTool的文件是否存在判断由这里最终执行
                File file = new File(mCurrentPlayPath);
                boolean existsState;
                String mediaState = "";
                if (!(existsState = file.exists()) || !Environment.MEDIA_MOUNTED.equals(mediaState = Environment.getExternalStorageState(file))) {
                    Log.e(TAG, "openMedia: mCurrentPlayPath is no exists！！！ existsState = " + existsState + " mediaState = " + mediaState);
                    //如果是最近播放，则需要停止所有控制器的播放
                    playErrorFile();
                    return;
                }
                initMediaPlayer();
                Log.d(TAG, "openMedia: mMediaPlayer = " + mMediaPlayer + " mediaState = " + mediaState);
                isPrepare = false;
                //此时更新下当前断点记忆点，避免出现切换其它曲目时又出现播放上一曲ID80606442
                saveCurrentMediaStatusWhenOpen(mCurrentPlayPath);
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(new SvMediaDataSource(mCurrentPlayPath));
                //@throws IllegalStateException if it is called in an invalid state
                mMediaPlayer.prepareAsync();
                Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setMediaPlayType(MediaPlayType.OPENING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "openMedia: open error ", e);
            playErrorFile();
        }
    }

    /**
     * 停止媒体播放器的播放
     */
    @Override
    public void stop() {
        Log.i(TAG, "stop: ");
        // 防止U盘拔出和界面退出都执行了保存时间，最后一次获取的时间时0;刷新了最后的保存
        if (null != mMediaPlayer) {
            // 释放播放器前,保存最后的时间
            saveCurrentMediaStatus(mCurrentPlayPath);
        }
        mHandler.removeCallbacksAndMessages(null);
        removeSaveMediaStatus();
        releaseMediaPlayer(true);
        //FIXME:stop的时候也要设置媒体当前的播放信息
        Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setPlaying(false);
        //停止更新时间
        stopUpdatePlayTime();
    }

    /**
     * 停止媒体播放器的播放，本来有加入一个音频焦点逻辑的释放，不过这边不清楚G6音频焦点逻辑具体要怎么处理，所以这一个先不加
     */
    @Override
    public void release() {
        Log.i(TAG, "release: ");
        mHandler.removeCallbacksAndMessages(null);
        removeSaveMediaStatus();
        releaseMediaPlayer(true);
    }

    /**
     * 开始播放动作
     */
    @Override
    public void start() {
        Log.d(TAG, "start: isPrepare  = " + isPrepare + ", mMediaPlayer is null: " + (mMediaPlayer == null));
        if (mMediaPlayer == null || !isPrepare) {
            Log.e(TAG, "start: mMediaPlayer is NULL");
            Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.OPEN, ChangeReasonData.UN_PREPARE, MediaControlTool.NEED_TO_FIND_THE_SAVE_MEDIA);
            return;
        }
        mMediaPlayer.start();
        //mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(speed));
        Log.d(TAG, "start: mMediaPlayer.start：speed = " + speed);
        Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setPlaying(true);
        //因为存在最近播放音乐，音源刚好是本地或者最近音乐。所以播放时更新下媒体信息
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
        //如果当前的mCurrentMediaType和实际上一个Open的不对应，则刷新下info信息
        if (mediaType != mCurrentMediaType.ordinal()) {
            Log.i(TAG, "start: mediaType = " + mediaType + " mCurrentMediaType = " + mCurrentMediaType.ordinal());
            Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setCurrentPlayItem(Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).getCurrentPlayItem());
            //更新下当前的播放源
            MusicSetting.getInstance().putInt(SourceTypeUtils.MEDIA_TYPE, mCurrentMediaType.ordinal());
        }
        startUpdatePlayTime();
        //由于pause会移除掉SAVE_MEDIA_PLAY_STATUS，所以开启播放之后需要重新启动状态
        //但是不能够立即开始
        //放在START有个问题，当多次触发open时，如果第一次的open执行到onPrepared，但还未SEEk到断点
        //此时第二个OPEN打开相同的则会调用start，而把当前时间状态给设置为了0.
        mHandler.sendEmptyMessageDelayed(SAVE_MEDIA_PLAY_STATUS, 4000);
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        Log.d(TAG, "pause: mMediaPlayer = " + mMediaPlayer + ", isPrepare = " + isPrepare);
        if (mMediaPlayer == null || !isPrepare) {
            Log.e(TAG, "pause: mMediaPlayer is NULL");
            Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setPlaying(false);
            return;
        }
        if (mMediaPlayer.isPlaying()) {//add by Lz Player is stared that can pause.
            mMediaPlayer.pause();
            Log.d(TAG, "pause: mMediaPlayer.pause()");
            Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setPlaying(false);
        }
        removeSaveMediaStatus();
        //停止更新时间
        stopUpdatePlayTime();
    }

    /**
     * 跳转到指定位置
     *
     * @param position 需要跳转的位置
     */
    @Override
    public void seekTo(int position) {
        Log.d(TAG, "seekTo: mMediaPlayer = " + mMediaPlayer + ", isPrepare = " + isPrepare + ", position = " + position);
        if (mMediaPlayer == null || !isPrepare) {
            Log.e(TAG, "seekTo: mMediaPlayer is NULL");
            return;
        }
        //停止更新时间
        stopUpdatePlayTime();
        isSeekingTo = true;
        //为避免seekTo失败不调用onSeekComplete，不更新时间，这里延迟1秒修改isSeekingTo为false，同时判断没有更新时间的消息就延迟500ms更新时间
        chgSeekingToValueAndUpdateTime();
        //* @throws IllegalStateException if the internal player engine has not been initialized
        mMediaPlayer.seekTo(position);
        //拖动需要立即保存状态
        //当前把保存媒体状态的位置放置到seekTo当中最为合适
        saveCurrentMediaStatus(mCurrentPlayPath);
        Log.d(TAG, "seekTo: mMediaPlayer.seekTo()");
    }

    /**
     * 快进快退
     *
     * @param action 快进或者快退
     */
    @Override
    public void autoSeek(MediaAction action) {
        Log.d(TAG, "autoSeek: action = " + action + " mMediaPlayer = " + mMediaPlayer);
        if (mMediaPlayer == null) {
            return;
        }
        switch (action) {
            case FAST_FORWARD:
                mRewind = false;
                mFastForwaed = true;
                playTime = getCurrentPlayTime();
                mHandler.post(new ChangeSpeedRunnable());
                break;
            case FAST_FORWARD_STOP:
                mFastForwaed = false;
                break;
            case REWIND:
                mFastForwaed = false;
                mRewind = true;
                playTime = getCurrentPlayTime();
                mHandler.post(new ChangeSpeedRunnable());
                break;
            case REWIND_STOP:
                mRewind = false;
                break;
        }
    }

    /**
     * 搜索媒体列表
     *
     * @param type 搜索的类型
     * @param Data 搜索的关键字
     * @param path 搜索的路径
     */
    @Override
    public void searchMusicList(String type, String Data, String path) {

    }

    @Override
    public void setPlaySpeed(float speed) {
        Log.d(TAG, "setPlaySpeed: speed = " + speed);
        this.speed = speed;
    }


    /**********************************************播放信息获取接口***********************************/

    /**
     * 当前是否播放，只提供给infor，不对外提供
     *
     * @return true 播放； false 暂停
     */
    @Override
    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        //add by Lz @throws IllegalStateException if the internal player engine has not been initialized or has been released.
        if (isPrepare) {
            return mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }


    /**
     * 获取当前播放媒体的总时长，只提供给infor，不对外提供
     *
     * @return Duration
     */
    @Override
    public int getDuration() {
        Log.d(TAG, "getDuration: mMediaPlayer = " + mMediaPlayer + ", isPrepare = " + isPrepare);
        if (mMediaPlayer == null || !isPrepare) {
            Log.e(TAG, "getDuration: mMediaPlayer is NULL or isPrepare is false");
            return 0;
        }
        return mMediaPlayer.getDuration();
    }

    /**
     * 获取当前播放的时间，只提供给Info，不对外提供
     *
     * @return 当前歌曲播放的位置
     */
    @Override
    public int getCurrentPlayTime() {
        Log.d(TAG, "getCurrentPlayTime: mMediaPlayer = " + mMediaPlayer + ", isPrepare = " + isPrepare);
        if (mMediaPlayer == null || !isPrepare) {
            Log.e(TAG, "getCurrentPlayTime: mMediaPlayer is NULL or isPrepare is false");
            return 0;
        }
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 设置获取媒体播放的接口，由于媒体播放器是由外部提供的。
     * TODO:这个需要看一视频界面会不会出现内存泄漏，如果出现了，就需要加入一个释放的方法
     *
     * @param mRequestMediaPlayer 获取媒体播放器的接口
     */
    public void setRequestMediaPlayer(IRequestMediaPlayer mRequestMediaPlayer) {
        this.mRequestMediaPlayer = mRequestMediaPlayer;
    }


    /**********************************************************播放器回调及初始化*********************************************/

    private MediaPlayer mMediaPlayer;

    /**
     * 媒体播放器的初始化
     */
    private void initMediaPlayer() {
        if (mMediaPlayer != mRequestMediaPlayer.requestMediaPlayer()) {
            Log.d(TAG, "initMediaPlayer: init set MediaPlayer");
            mMediaPlayer = mRequestMediaPlayer.requestMediaPlayer();
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setOnErrorListener(onErrorListener);
            mMediaPlayer.setOnInfoListener(onInfoListener);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        }
    }

    /**
     * 媒体播放器播放完成回调
     */
    private final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG, "onCompletion: ");
            Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.NEXT, ChangeReasonData.ON_COMPLETE);
        }
    };

    /**
     * 媒体播放器出现异常的回调
     */
    private final MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onError: what = " + what);
            //如果是服务挂了的话则不是视频资源的问题
            isPrepare = false;
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                pause();
                //出现100错误的时候，无需重置播放路径和时间，否则并非恢复播放当前曲目
                releaseMediaPlayer(false);
                Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.OPEN, ChangeReasonData.ON_ERROR, Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).getCurrentPlayPosition());
            } else {
                playErrorFile();
            }
            return true;
        }
    };


    /**
     * 媒体播放器第一帧绘画出来会走的回调，这个回调不只走第一帧，还有其他信息会走，具体自己查
     */
    private final MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onInfo: what = " + what);
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setMediaPlayType(MediaPlayType.NORMAL);
            } else if (what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING) {
                //表示当前只有音频，无画面的视频文件
                Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setMediaPlayType(MediaPlayType.NO_VIDEO);
            }
            return true;
        }
    };

    /**
     * 媒体播放器准备好的回调
     */
    private final MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mp) {
            Log.d(TAG, "onPrepared: ");
            isPrepare = true;
            Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.SEEKTO, ChangeReasonData.ON_PREPARE, 0);
            Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.START, ChangeReasonData.ON_PREPARE);
            //getSelectedTrack查看源码，存在着IO操作，有ANR风险，采用子线程处理
            if (mGetSelectedTrackSubmit != null) {
                mGetSelectedTrackSubmit.cancel(true);
            }
            //setDataSource之后，在getSelectedTrack之前mediaPlayer不能reset，否则底层会崩
            Log.d(TAG, "onPrepared: mMediaPlayer " + mMediaPlayer);
            if (mMediaPlayer != null) {
                mGetSelectedTrackSubmit = MediaThreadPoolExecutorUtils.getInstance().submit(() -> {
                    int type = mp.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO);
                    Log.d(TAG, "onPrepared: type = " + type);
                    if (type == -1) {
                        Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setMediaPlayType(MediaPlayType.NO_VIDEO);
                    }
                });
            }
        }
    };

    /**
     * 媒体播放器播放完毕的回调
     */
    private final MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
//            CurrentPlayInfo.getInstance(mCurrentMediaType).updatePlayTime(mp.getCurrentPosition(), mp.getDuration());
            //更新时间
            Log.d(TAG, "onSeekComplete:");
            isSeekingTo = false;
            startUpdatePlayTime();
            saveCurrentMediaStatus(mCurrentPlayPath);
        }
    };

    /**********************************************************界面刷新回调**********************************************/

    /**
     * 播放了错误的文件
     */
    private void playErrorFile() {
        Log.d(TAG, "playErrorFile:");
        pause();
        //状态出错时，任何mediaPlayer的API都会导致问题，所以此状态需要重置
        isPrepare = false;
        //add by lzm播放到异常歌曲的时候，需要将记忆的播放时间清零
        saveCurrentMediaPlayTime(0, "");
        mHandler.removeMessages(PLAY_STATE_ERROR);
        Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setMediaPlayType(MediaPlayType.ERROR);
        //当前焦点需在媒体处才需恢复播放
        mHandler.sendEmptyMessageDelayed(PLAY_STATE_ERROR, 1500L);
    }

    /**
     * 释放媒体播放器
     */
    private void releaseMediaPlayer(boolean needResetPlayStatus) {
        Log.d(TAG, "releaseMediaPlayer: " + needResetPlayStatus);
        isPrepare = false;
        if (mMediaPlayer != null) {
            Log.d(TAG, "releaseMediaPlayer: release");
            if (needResetPlayStatus) {
                Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setCurrentPlayItem(new FileMessage());
                Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).setCurrentPlayPosition(0);
            }
            setMediaReset();
            setMediaRelease();
            mMediaPlayer = null;
            Log.d(TAG, "releaseMediaPlayer: release is null");
        }
        if (mRequestMediaPlayer != null) {
            Log.d(TAG, "releaseMediaPlayer: destroyMediaPlayer");
            mRequestMediaPlayer.destroyMediaPlayer();
        }
        setPlaySpeed(1.0f);
    }

    private void setMediaReset() {
        Log.i(TAG, "mMediaPlayer setMediaReset");
        try {
            mMediaPlayer.reset();
        } catch (IllegalStateException e) {
            Log.e(TAG, "mMediaPlayer reset err");
        }
    }

    private void setMediaRelease() {
        if (mMediaPlayer != null) {
            Log.i(TAG, "mMediaPlayer setMediaRelease");
            mMediaPlayer.release();
        }
    }

    /**
     * 保存当前媒体的状态
     *
     * @param path 当前媒体的路径
     */
    private void saveCurrentMediaStatus(String path) {
        Log.d(TAG, "saveCurrentMediaStatus: path = " + path + " getCurrentPosition = " + getCurrentPlayTime());
        if (path != null && !path.isEmpty()) {
            MediaPlayStatusSaveUtils.getInstance().saveMediaPlayPath(mCurrentMediaType, path);
            //将当前播放的FileMessage进行序列号保存
            MediaPlayStatusSaveUtils.getInstance().saveMediaFileMessage(mCurrentMediaType, Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).getCurrentPlayItem());
            saveCurrentMediaPlayTime(getCurrentPlayTime(), path);
        }
        mHandler.removeMessages(SAVE_MEDIA_PLAY_STATUS);
        mHandler.sendEmptyMessageDelayed(SAVE_MEDIA_PLAY_STATUS, 4000);
    }

    /**
     * 仅保存路径等信息，不包含时间
     * 用于open的时候就需记忆住当前的路径信息
     *
     * @param path path
     */
    private void saveCurrentMediaStatusWhenOpen(String path) {
        String mediaPlayPath = MediaPlayStatusSaveUtils.getInstance().getMediaPlayPath(mCurrentMediaType);
        //当前要播放的和断点记忆相同则不做处理,避免断点和时间不匹配
        if (mediaPlayPath.equals(path)) {
            Log.e(TAG, "saveCurrentMediaStatusWhenOpen: current is same");
            return;
        }
        //当歌曲切换时，时间也需匹配更新
        if (path != null && !path.isEmpty()) {
            // 保存上次播放路径
            MediaPlayStatusSaveUtils.getInstance().saveLastMediaPlayPath(mCurrentMediaType, mediaPlayPath);
            // 保存当前播放路径
            MediaPlayStatusSaveUtils.getInstance().saveMediaPlayPath(mCurrentMediaType, path);
            //将当前播放的FileMessage进行序列号保存
            MediaPlayStatusSaveUtils.getInstance().saveMediaFileMessage(mCurrentMediaType, Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).getCurrentPlayItem());
            saveCurrentMediaPlayTime(getCurrentPlayTime(), path);
        }
    }

    /**
     * 保存当前的播放时间
     *
     * @param position 需要保存的时间
     */
    private void saveCurrentMediaPlayTime(int position, String path) {
        Log.d(TAG, "saveCurrentMediaPlayTime: position = " + position);
        MediaPlayStatusSaveUtils.getInstance().saveMediaPlayTime(mCurrentMediaType, position, path);
    }

    /**
     * 停止保存当前媒体的状态
     */
    private void removeSaveMediaStatus() {
        mHandler.removeMessages(SAVE_MEDIA_PLAY_STATUS);
    }


    /***********************************************快进快退的方法*************************************************************/

    private boolean mFastForwaed = false;   //是否快进
    private boolean mRewind = false;        //是否快退
    private int playTime = 0;

    /**
     * 快进快退的Runnable
     */
    private class ChangeSpeedRunnable implements Runnable {
        @Override
        public void run() {

            //   int currentPosition = mVideoView.getCurrentPosition();
            int duration = getDuration();
            if (!USBFocusUtils.getInstance().isCurrentMediaTypeSource(mCurrentMediaType)) {
                return;
            }
            if (mFastForwaed) {
                playTime = playTime + 10000;
            } else if (mRewind) {
                playTime = playTime - 10000;
            }
            Log.d(TAG, "ChangeSpeedRunnable: playTime = " + playTime + " duration = " + duration + " mFastForwaed = " + mFastForwaed + " mRewind = " + mRewind);
            if (!mFastForwaed && !mRewind) {
                return;
            }
            if (playTime > duration) {
                Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.NEXT, ChangeReasonData.AUTO_SEEK);
                return;
            } else if (playTime < 0) {
                Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.SEEKTO, ChangeReasonData.AUTO_SEEK, 0);
                return;
            }
            Objects.requireNonNull(MediaControlTool.getInstance(mCurrentMediaType)).processCommand(MediaAction.SEEKTO, ChangeReasonData.AUTO_SEEK, playTime);
            mHandler.postDelayed(new ChangeSpeedRunnable(), 200);
        }
    }

    /**********************************************时间刷新回调*************************************************************/

    //是否需要底层不断回调
    private static final Boolean IS_NEED_TO_UPDATE_TIME = true;

    //更新时间的间隔
    private static final int UPDATE_TIME_INTERVAL = 500;

    //是否在seek
    private boolean isSeekingTo = false;

    /**
     * 更新播放时间，播放的时候，定时回调
     */
    private void updatePlayTime() {
        if (mMediaPlayer != null) {
            Objects.requireNonNull(CurrentPlayInfo.getInstance(mCurrentMediaType)).updatePlayTime(getCurrentPlayTime(), getDuration());
        }
        if (IS_NEED_TO_UPDATE_TIME) {
            mHandler.removeMessages(UPDATE_PLAY_TIME);
            mHandler.sendEmptyMessageDelayed(UPDATE_PLAY_TIME, UPDATE_TIME_INTERVAL);
        }
    }

    /**
     * 开始更新播放时间
     */
    private void startUpdatePlayTime() {
        Log.d(TAG, "startUpdatePlayTime: ");
        mHandler.removeMessages(UPDATE_PLAY_TIME);
        if (!isSeekingTo) {
            mHandler.sendEmptyMessage(UPDATE_PLAY_TIME);
        }
    }

    /**
     * 停止更新播放时间
     */
    private void stopUpdatePlayTime() {
        Log.d(TAG, "stopUpdatePlayTime: ");
        mHandler.removeMessages(UPDATE_PLAY_TIME);
    }

    private void chgSeekingToValueAndUpdateTime() {
        Log.d(TAG, "chgSeekingToValueAndUpdateTime: ");
        mHandler.removeMessages(CHG_SEEKING_TO_FALSE_VALUE);
        mHandler.sendEmptyMessageDelayed(CHG_SEEKING_TO_FALSE_VALUE,1000);
    }

}


