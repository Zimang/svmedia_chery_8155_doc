package com.desaysv.moduleusbmusic.businesslogic.listdata;

import android.util.Log;

import com.desaysv.moduleusbmusic.bean.FolderItem;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by LZM on 2019-7-4.
 * Comment 用来全局存储USB列表数据的单例
 */
public class USBMusicDate {

    private static final String TAG = "USBMusicDate";

    private static USBMusicDate instance;

    private final List<IListDataChange> musicListChangeList = new ArrayList<>();
    //对应USB列表需要添加读写锁
    //进行加锁处理
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = mReentrantLock.writeLock();

    public static USBMusicDate getInstance() {
        if (instance == null) {
            synchronized (USBMusicDate.class) {
                if (instance == null) {
                    instance = new USBMusicDate();
                }
            }
        }
        return instance;
    }

    /**
     * USB1的全部音乐列表
     */
    private final List<FileMessage> mUSB1MusicAllList = new ArrayList<>();

    /**
     * 清除USB1的数据列表
     */
    void clearUSB1MusicAllList() {
        writeLock.lock();
        try {
            Log.d(TAG, "clearUSB1MusicAllList: ");
            mUSB1MusicAllList.clear();
        } finally {
            writeLock.unlock();
        }
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB1MusicAllListChange();
        }
    }

    /**
     * 添加USB1的数据列表
     *
     * @param fileMessages 文件列表
     */
    void addAllUSB1MusicAllList(List<FileMessage> fileMessages) {
        writeLock.lock();
        try {
            Log.i(TAG, "addAllUSB1MusicAllList: fileMessages = " + fileMessages);
            mUSB1MusicAllList.clear();
            mUSB1MusicAllList.addAll(fileMessages);
        } finally {
            writeLock.unlock();
        }
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB1MusicAllListChange();
        }
    }

    /**
     * 获取USB音乐的全部列表
     *
     * @return mUSB1MusicAllList
     */
    public List<FileMessage> getUSB1MusicAllList() {
        readLock.lock();
        try {
            Log.d(TAG, "getUSB1MusicAllList: mUSB1MusicAllList size = " + mUSB1MusicAllList.size());
            return mUSB1MusicAllList;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * USB2的音乐列表
     */
    private final List<FileMessage> mUSB2MusicAllList = new ArrayList<>();

    /**
     * 清除USB2的音乐列表
     */
    void clearUSB2MusicAllList() {
        Log.d(TAG, "clearUSB2MusicAllList: ");
        mUSB2MusicAllList.clear();
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB2MusicAllListChange();
        }
    }

    /**
     * 添加USB2的音乐列表
     *
     * @param fileMessages 音乐列表
     */
    void addAllUSB2MusicAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addAllUSB2MusicAllList: fileMessages = " + fileMessages);
        mUSB2MusicAllList.clear();
        mUSB2MusicAllList.addAll(fileMessages);
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB2MusicAllListChange();
        }
    }

    /**
     * 获取USB2的音乐列表
     *
     * @return mUSB2MusicAllList USB音乐的全部列表
     */
    public List<FileMessage> getUSB2MusicAllList() {
        Log.d(TAG, "getUSB2MusicAllList: mUSB2MusicAllList size = " + mUSB2MusicAllList.size());
        return mUSB2MusicAllList;
    }

    /**
     * 本地音乐列表
     */
    private final List<FileMessage> mLocalMusicAllList = new ArrayList<>();

    /**
     * 清除本地的音乐列表
     */
    void clearLocalMusicAllList() {
        Log.d(TAG, "clearLocalMusicAllList: ");
        mLocalMusicAllList.clear();
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onLocalMusicListChange();
        }
    }

    /**
     * 添加本地音乐列表
     *
     * @param fileMessages 音乐列表
     */
    void addLocalMusicAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addLocalMusicAllList: fileMessages = " + fileMessages);
        mLocalMusicAllList.clear();
        mLocalMusicAllList.addAll(fileMessages);
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onLocalMusicListChange();
        }
    }

    /**
     * 本地列表
     *
     * @return mLocalMusicAllList 本地列表
     */
    public List<FileMessage> getLocalMusicAllList() {
        Log.d(TAG, "getLocalMusicAllList: mLocalMusicAllList size = " + mLocalMusicAllList.size());
        return mLocalMusicAllList;
    }

    /**
     * 最近播放的音乐列表
     */
    private final List<FileMessage> mRecentMusicAllList = new ArrayList<>();

    /**
     * 清除最近播放的音乐列表
     */
    void clearRecentMusicAllList() {
        Log.d(TAG, "clearRecentMusicAllList: ");
        mRecentMusicAllList.clear();
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onRecentMusicListChange();
        }
    }

    /**
     * 添加最近播放的音乐列表
     *
     * @param fileMessages 音乐列表
     */
    void addRecentMusicAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addRecentMusicAllList: fileMessages = " + fileMessages);
        mRecentMusicAllList.clear();
        mRecentMusicAllList.addAll(fileMessages);
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onRecentMusicListChange();
        }
    }

    /**
     * 获取最近播放的音乐列表
     *
     * @return mRecentMusicAllList 最近播放的音乐列表
     */
    public List<FileMessage> getRecentMusicAllList() {
        Log.d(TAG, "getRecentMusicAllList: mRecentMusicAllList size = " + mRecentMusicAllList.size());
        return mRecentMusicAllList;
    }

    /**
     * 设置音乐列表数据的变化回调
     *
     * @param iMusicListChange 回调
     */
    public void setMusicListChangeListener(IListDataChange iMusicListChange) {
        Log.d(TAG, "setMusicListChangeListener: ");
        musicListChangeList.remove(iMusicListChange);
        musicListChangeList.add(iMusicListChange);
    }

    /**
     * 清除音乐数据的状态回调
     *
     * @param iMusicListChange 音乐数据变化的回调
     */
    public void removeMusicListChangeListener(IListDataChange iMusicListChange) {
        Log.d(TAG, "removeMusicListChangeListener: ");
        musicListChangeList.remove(iMusicListChange);
    }

    /**
     * USB1的全部文件夹列表
     */
    private final Map<String, List<FolderItem>> mUSB1MusicAllFolderMap = new HashMap<>();

    /**
     * 清除USB1的文件夹数据列表
     */
    void clearUSB1MusicAllFolderMap() {
        Log.d(TAG, "clearUSB1MusicAllFolderMap: ");
        mUSB1MusicAllFolderMap.clear();
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB1MusicAllFolderMapChange();
        }
    }

    /**
     * 添加USB1的文件夹数据列表
     *
     * @param fileMessages 文件列表
     */
    void addAllUSB1MusicAllMap(Map<String, List<FolderItem>> fileMessages) {
        Log.d(TAG, "addAllUSB1MusicAllMap: fileMessages = " + fileMessages);
        mUSB1MusicAllFolderMap.clear();
        mUSB1MusicAllFolderMap.putAll(fileMessages);
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB1MusicAllFolderMapChange();
        }
    }

    /**
     * 获取USB1音乐的文件夹全部列表
     *
     * @return mUSB1MusicAllFolderMap
     */
    public Map<String, List<FolderItem>> getUSB1MusicAllMap() {
        Log.d(TAG, "getUSB1MusicAllMap: mUSB1MusicAllFolderMap size = " + mUSB1MusicAllFolderMap.size());
        return mUSB1MusicAllFolderMap;
    }

    /**
     * USB2的全部文件夹列表
     */
    private final Map<String, List<FolderItem>> mUSB2MusicAllFolderMap = new HashMap<>();

    /**
     * 清除USB2的文件夹数据列表
     */
    void clearUSB2MusicAllFolderMap() {
        Log.d(TAG, "clearUSB2MusicAllFolderMap: ");
        mUSB2MusicAllFolderMap.clear();
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB2MusicAllFolderMapChange();
        }
    }

    /**
     * 添加USB2的文件夹数据列表
     *
     * @param fileMessages 文件列表
     */
    void addAllUSB2MusicAllMap(Map<String, List<FolderItem>> fileMessages) {
        Log.d(TAG, "addAllUSB2MusicAllMap: fileMessages = " + fileMessages);
        mUSB2MusicAllFolderMap.clear();
        mUSB2MusicAllFolderMap.putAll(fileMessages);
        for (IListDataChange iListDataChange : musicListChangeList) {
            iListDataChange.onUSB2MusicAllFolderMapChange();
        }
    }

    /**
     * 获取USB2音乐的全部列表
     *
     * @return mUSB2MusicAllFolderMap
     */
    public Map<String, List<FolderItem>> getUSB2MusicAllMap() {
        Log.d(TAG, "getUSB2MusicAllMap: mUSB2MusicAllFolderMap size = " + mUSB2MusicAllFolderMap.size());
        return mUSB2MusicAllFolderMap;
    }

    /**
     * 用来监听数据变化的逻辑，然后回调给各个监听者
     */
    public interface IListDataChange {

        /**
         * USB1音乐文件夹数据变化触发的回调
         */
        void onUSB1MusicAllFolderMapChange();

        /**
         * USB2音乐文件夹数据变化触发的回调
         */
        void onUSB2MusicAllFolderMapChange();

        /**
         * USB1音乐数据变化触发的回调
         */
        void onUSB1MusicAllListChange();

        /**
         * USB2音乐数据变化触发的回调
         */
        void onUSB2MusicAllListChange();

        /**
         * 本地音乐列表变化回调
         */
        void onLocalMusicListChange();

        /**
         * 最近音乐列表变化回调
         */
        void onRecentMusicListChange();
    }
}
