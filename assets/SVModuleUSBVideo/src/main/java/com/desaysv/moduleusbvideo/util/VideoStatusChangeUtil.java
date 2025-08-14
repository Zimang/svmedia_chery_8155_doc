package com.desaysv.moduleusbvideo.util;

import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.moduleusbvideo.util.listener.IVideoStatusChange;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 用于接收和触发信息处理
 * VR、MediaSession
 * Create by extodc87 on 2023-8-23
 * Author: extodc87
 */
public class VideoStatusChangeUtil {
    private static final String TAG = "VideoStatusChangeUtil";

    private static VideoStatusChangeUtil statusChangeUtil;

    public static VideoStatusChangeUtil getInstance() {
        if (null == statusChangeUtil) {
            statusChangeUtil = new VideoStatusChangeUtil();
        }
        return statusChangeUtil;
    }

    //进行加锁处理
    private ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private Lock writeLock = mReentrantLock.writeLock();


    private final Map<String, IVideoStatusChange> iVideoStatusChangeMap = new HashMap<>();

    public void addVideoStatusChange(String className, IVideoStatusChange iVideoStatusChange) {
        Log.d(TAG, "addVideoStatusChange() called with: className = [" + className + "], iVideoStatusChange = [" + iVideoStatusChange + "]");
        writeLock.lock();
        try {
            iVideoStatusChangeMap.put(className, iVideoStatusChange);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeVideoStatusChange(String className) {
        Log.d(TAG, "removeVideoStatusChange() called with: className = [" + className + "]");
        writeLock.lock();
        try {
            iVideoStatusChangeMap.remove(className);
        } finally {
            writeLock.unlock();
        }
    }

    public void onVideoPlayStatus(MediaType mediaType, boolean isPlaying) {
        Log.d(TAG, "onVideoPlayStatus() called with: mediaType = [" + mediaType + "], isPlaying = [" + isPlaying + "]");
        readLock.lock();
        try {
            for (Map.Entry<String, IVideoStatusChange> entry : iVideoStatusChangeMap.entrySet()) {
                Log.d(TAG, "onVideoPlayStatus: className = " + entry.getKey());
                entry.getValue().onVideoPlayStatus(mediaType, isPlaying);
            }
        } finally {
            readLock.unlock();
        }
    }
}
