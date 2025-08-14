package com.desaysv.moduleusbmusic.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.svliblyrics.lyrics.LyricsRow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Time: 2020-12-23
 * Author: EXTODC24
 * Description: 歌词获取工具类
 */
public class LyricsUtil {
    private static final String TAG = "LyricsUtil";
    //需添加读写锁
    //进行加锁处理
    private ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private Lock writeLock = mReentrantLock.writeLock();
    private List<IMusicLyricChange> musicLyricChangeList = new ArrayList<>();
    private static LyricsUtil instance;

    public static LyricsUtil getInstance() {
        if (instance == null) {
            synchronized (LyricsUtil.class) {
                if (instance == null) {
                    instance = new LyricsUtil();
                }
            }
        }
        return instance;
    }

    private static final int PARSING_LYRICS = 1;
    private Context context;
    private List<LyricsRow> lyricList;
    //子线程
    private HandlerThread mHandlerThread;
    private Handler mainHandler;

    public LyricsUtil() {
        this.context = AppBase.mContext;
        init();
    }

    private void init() {
        Log.d(TAG, "init: ");
        mHandlerThread = new HandlerThread("mHandlerThread");
        mHandlerThread.start();
        mainHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case PARSING_LYRICS:
                        getCurrentLyrics((int) msg.obj);
                        break;
                    default:
                        Log.d(TAG, "handleMessage: DEFAULT");
                        break;
                }
            }
        };
    }

    /**
     * 开始解析
     */
    public void start(int time) {
        if (lyricList == null || lyricList.isEmpty()) {
            Log.d(TAG, "start: lyricList is null!!! or empty");
            return;
        }
        if (!mHandlerThread.isAlive()) {
            Log.d(TAG, "start: thread is dead, return!");
            return;
        }
        Message message = mainHandler.obtainMessage();
        message.what = PARSING_LYRICS;
        message.obj = time;
        mainHandler.sendMessage(message);
    }

    /**
     * 停止线程
     */
    public void stop() {
        Log.d(TAG, "stop: ");
        mainHandler.removeMessages(PARSING_LYRICS);
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置歌词列表
     *
     * @param lyricList
     */
    public void setLyricList(List<LyricsRow> lyricList) {
        this.lyricList = lyricList;
    }

    /**
     * 返回当前时间的歌词
     */
    public void getCurrentLyrics(int time) {
        readLock.lock();
        try {
            Log.d(TAG, "getCurrentLyrics: time = " + time);
            // 如果歌词列表为null或空，返回无歌词
            if (lyricList == null || lyricList.isEmpty()) {
                // 当歌词列表=null或者空的时候，返回index=-1
                for (IMusicLyricChange iMusicLyricChange : musicLyricChangeList) {
                    iMusicLyricChange.onMusicLyricChange(context.getString(R.string.usb_music_no_lyrics), -1);
                }
                return;
            }
            int size = lyricList.size();
            String result;
            for (int index = size - 1; index >= 0; index--) {
                LyricsRow lyricsRow = lyricList.get(index);
                //如果当前已经是这个位置则不再响应,这里还有个问题，就是当前的lyricsRows并非时长小到大排序的
                if (time >= lyricsRow.getTime()) {
                    result = lyricsRow.getContent();
                    Log.d(TAG, "getCurrentLyrics: result = " + result + ", index = " + index + " musicLyricChangeList = " + musicLyricChangeList);
                    // 通知注册者歌词改变了
                    for (IMusicLyricChange iMusicLyricChange : musicLyricChangeList) {
                        iMusicLyricChange.onMusicLyricChange(result, index);
                    }
                    break;
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 用来监听数据变化的逻辑，然后回调给各个监听者
     */
    public interface IMusicLyricChange {

        /**
         * 歌词数据变化触发的回调
         */
        void onMusicLyricChange(String lyrics, int lyricsIndex);
    }

    /**
     * 设置音乐列表数据的变化回调
     *
     * @param iMusicLyricChange 回调
     */
    public void setMusicLyricChangeListener(IMusicLyricChange iMusicLyricChange) {
        writeLock.lock();
        try {
            Log.d(TAG, "setMusicLyricChangeListener: " + iMusicLyricChange.toString());
            musicLyricChangeList.remove(iMusicLyricChange);
            musicLyricChangeList.add(iMusicLyricChange);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清除音乐数据的状态回调
     *
     * @param iMusicLyricChange 音乐数据变化的回调
     */
    public void removeMusicLyricChangeListener(IMusicLyricChange iMusicLyricChange) {
        writeLock.lock();
        try {
            Log.d(TAG, "removeMusicFolderMapChangeListener: " + iMusicLyricChange.toString());
            musicLyricChangeList.remove(iMusicLyricChange);
        } finally {
            writeLock.unlock();
        }
    }
}
