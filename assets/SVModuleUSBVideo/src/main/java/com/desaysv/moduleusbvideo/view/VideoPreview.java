package com.desaysv.moduleusbvideo.view;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.moduleusbvideo.util.ExecutorSingleUtils;
import com.desaysv.moduleusbvideo.util.VideoSizeUtils;
import com.desaysv.usbbaselib.utils.SvMediaDataSource;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Future;

/**
 * 加载MediaPlayer 用于预览图
 */
public class VideoPreview {
    private static final String TAG = "VideoPreview";
    private final RelativeLayout rlPreviewRoot;
    private final TextureView mSeekToImage;
    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private boolean mIsPrepare = false;
    private boolean mNeedShowView = false;
    private String mLastPath = "";
    private int mLastPosition = 0;
    private static final int SHOW_TEXTURE_VIEW = 0x00;
    private static final int SHOW_TEXTURE_WH = SHOW_TEXTURE_VIEW + 1;
    //是否存在播放的画面（即音轨存在视频）
    private boolean hasVideoView = false;

    private Future<?> mGetSelectedTrackSubmit;

    private MyHandler mHandler;

    public VideoPreview(RelativeLayout rlPreviewRoot, TextureView mSeekToImage) {
        this.rlPreviewRoot = rlPreviewRoot;
        this.mSeekToImage = mSeekToImage;
        init();
    }

    private void init() {
        mHandler = new MyHandler(this);
        mSeekToImage.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    /**
     * SurfaceTextureListener
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            initPlay();
            mSurface = new Surface(surface);
            if (mMediaPlayer != null) {
                Log.d(TAG, "onSurfaceTextureAvailable: mSurface = " + mSurface);
                mMediaPlayer.setSurface(mSurface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            surface.release();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    /**
     * 设置背景视频帧的方法
     *
     * @param progress 视频帧的位置
     */
    public void setSeekToImage(final int progress, final String path) {
        setPath(path);
        if (mIsPrepare) {
            mMediaPlayer.seekTo(progress);
        }
        mLastPosition = progress;
    }

    public void setPath(final String path) {
        initPlay();
        if (!mLastPath.equals(path)) {//当传入的视频路径不正确时，需要重新加载
            Log.d(TAG, "setPath: mLastPath  = " + mLastPath + " \npath = " + path);
            Runnable mSetSourceRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // 判断当前文件是否存在
                        File pathFile = new File(path);
                        if (!pathFile.exists()) {
                            Log.e(TAG, "setPath: current file is null ");
                            return;
                        }
                        mIsPrepare = false;
                        hasVideoView = false;
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(new SvMediaDataSource(path));
                        mMediaPlayer.prepareAsync();
                        // 隐藏界面
                        mHandler.sendEmptyMessage(SHOW_TEXTURE_VIEW);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "setPath: ", e);
                    }
                }
            };
            ExecutorSingleUtils.getInstance().submit(mSetSourceRunnable);
//            MediaThreadPoolExecutorUtils.getInstance().submit(mSetSourceRunnable);
        }
        mLastPath = path;
    }

    /**
     * 初始化player
     */
    private void initPlay() {
        if (mMediaPlayer == null) {
            mIsPrepare = false;
            hasVideoView = false;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnInfoListener(onInfoListener);
            if (mSurface != null) {
                Log.d(TAG, "initPlay: mSurface = " + mSurface);
                mMediaPlayer.setSurface(mSurface);
            }
        }
    }

    /**
     * mOnPreparedListener
     */
    private final MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mIsPrepare = true;
            mNeedShowView = true;
            //每次打开新的媒体路径，将进度位置重置为0, 否则在8155平台上会因为两个播放器同时seekTo不一样的位置，从而出现视频解码异常卡界面
            mLastPosition = 0;
            Log.i(TAG, "onPrepared: mLastPosition == " + mLastPosition);
            mp.seekTo(mLastPosition);
            if (mGetSelectedTrackSubmit != null) {
                mGetSelectedTrackSubmit.cancel(true);
            }
            mGetSelectedTrackSubmit = MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onPrepared() run: called mIsPrepare: " + mIsPrepare);
                    //by lym 20230909 视频在不断的切换时，会导致，重新加载和当前切换获取的内容不一致导致：（failure code: -38）错误
                    if (!mIsPrepare) {
                        Log.w(TAG, "onPrepared(): mIsPrepare is false");
                        return;
                    }
                    MediaPlayer.TrackInfo[] trackInfo = mMediaPlayer.getTrackInfo();
                    for (MediaPlayer.TrackInfo mt : trackInfo) {
                        Log.i(TAG, "onPrepared: TrackType = " + mt.getTrackType());
                        if (mt.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
                            hasVideoView = true;
                            break;
                        } else {
                            hasVideoView = false;
                        }
                    }
                    mHandler.removeMessages(SHOW_TEXTURE_VIEW);
                    mHandler.sendEmptyMessageDelayed(SHOW_TEXTURE_VIEW, 300);
                }
            });

        }
    };

    /**
     * mOnSeekCompleteListener
     */
    private final MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.d(TAG, "onSeekComplete: mNeedShowView = " + mNeedShowView + " == getCurrentPosition = " + mp.getCurrentPosition() + " --- " + mp.isPlaying());
            if (mNeedShowView) {
                mHandler.removeMessages(SHOW_TEXTURE_VIEW);
                mHandler.sendEmptyMessageDelayed(SHOW_TEXTURE_VIEW, 300);
                mNeedShowView = false;
            }
        }
    };

    /**
     * 视频尺寸变化时会触发的回调
     */
    private final MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged: width == " + width + " height == " + height);
            int[] videoWH = VideoSizeUtils.getVideoWH(rlPreviewRoot, height, width);
            mHandler.sendMessage(mHandler.obtainMessage(SHOW_TEXTURE_WH, videoWH[0], videoWH[1]));
        }
    };

    /**
     * 媒体播放器第一帧绘画出来会走的回调，这个回调不只走第一帧，还有其他信息会走
     */
    private final MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onInfo: what = " + what);
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // 显示画面
                hasVideoView = true;
            } else /*if (what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING)*/ {
                //表示当前只有音频，无画面的视频文件
                // 隐藏画面
                hasVideoView = false;
            }
            return true;
        }
    };

    /**
     * 媒体播放器播放完成回调
     */
    private final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG, "onCompletion: mLastPosition = " + mLastPosition);
        }
    };

    /**
     * 释放状态
     */
    public void release() {
        Log.i(TAG, "release()");
        mLastPath = "";
        mLastPosition = 0;
        mIsPrepare = false;
        mNeedShowView = false;
        hasVideoView = false;
        if (mMediaPlayer != null) {
            setMediaReset();
            Log.i(TAG, "mMediaPlayer.reset() reset()");
            setMediaRelease();
            Log.i(TAG, "mMediaPlayer.release() release()");
            mMediaPlayer = null;
            mSurface = null;
            Log.i(TAG, "mMediaPlayer = null");
        }
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

    private static class MyHandler extends Handler {
        WeakReference<VideoPreview> weakReference;

        MyHandler(VideoPreview videoPreviewTool) {
            weakReference = new WeakReference<>(videoPreviewTool);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final VideoPreview thumbnailsImageView = weakReference.get();
            if (msg.what == SHOW_TEXTURE_VIEW) {
                if (thumbnailsImageView.mIsPrepare && thumbnailsImageView.hasVideoView) {
                    thumbnailsImageView.mSeekToImage.setAlpha(1);
                } else {
                    thumbnailsImageView.mSeekToImage.setAlpha(0);
                }
            } else if (msg.what == SHOW_TEXTURE_WH) {
                VideoSizeUtils.updateTextureViewSize(thumbnailsImageView.mSeekToImage, msg.arg1, msg.arg2);
            }
        }
    }

}
