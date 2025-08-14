package com.desaysv.svlibmediastore.dao;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.svlibmediastore.database.MediaFileDatabase;
import com.desaysv.svlibmediastore.entities.MediaFile;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author uidq1846
 * @desc RecentlyMusicDao
 * @time 2022-12-1 10:57
 */
public class RecentlyMusicDao implements IDao {
    private final String TAG = RecentlyMusicDao.class.getSimpleName();
    private IMediaFileDao mediaFileDao;
    private final Map<String, IRecentlyDataListener> listenerMap = new HashMap<>();
    //进行加锁处理
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = mReentrantLock.writeLock();
    private static IDao dao;
    private boolean isInit = false;
    //当前数据库大小
    private final int SIZE_LIMIT = 500;
    //数据库里边最近的一首曲目系统时间
    private long latestModified = System.currentTimeMillis();

    private RecentlyMusicDao() {
    }

    public static IDao getInstance() {
        if (dao == null) {
            synchronized (RecentlyMusicDao.class) {
                if (dao == null) {
                    dao = new RecentlyMusicDao();
                }
            }
        }
        return dao;
    }

    @Override
    public void init(Context context) {
        //数据库比较耗时
        MediaThreadPoolExecutorUtils.getInstance().submit(() -> {
            MediaFileDatabase database = Room.databaseBuilder(context, MediaFileDatabase.class, "media_file").build();
            mediaFileDao = database.getMediaFileDao();
            isInit = true;
            notifyInit();
            deleteLimitItem();
        });
    }

    @Override
    public void update(FileMessage... mediaFiles) {
        if (!isInit) {
            Log.w(TAG, "update: dao has no init");
            return;
        }
        MediaThreadPoolExecutorUtils.getInstance().submit(() -> {
            //一般是一条一条存，所以节约开销，如此写
            if (mediaFiles.length == 1) {
                //如果传输的是空值不存
                FileMessage mediaFile = mediaFiles[0];
                if (mediaFile.getId() <= 0) {
                    Log.w(TAG, "update: mediaFile is error " + mediaFile);
                    return;
                }
                mediaFileDao.update(MediaFile.fileMessageToMediaFile(mediaFile));
            } else {
                MediaFile[] files = new MediaFile[mediaFiles.length];
                for (int i = 0; i < mediaFiles.length; i++) {
                    FileMessage mediaFile = mediaFiles[i];
                    if (mediaFile.getId() <= 0) {
                        Log.w(TAG, "update: mediaFile is error, to next " + mediaFile);
                        continue;
                    }
                    files[i] = MediaFile.fileMessageToMediaFile(mediaFile);
                }
                mediaFileDao.update(files);
            }
            //通知数据变化
            notifyUpdate(mediaFiles);
        });
    }

    @Override
    public void delete(FileMessage... mediaFiles) {
        if (!isInit) {
            Log.w(TAG, "delete: dao has no init");
            return;
        }
        MediaThreadPoolExecutorUtils.getInstance().submit(() -> {
            //一般是一条一条存，所以节约开销，如此写
            if (mediaFiles.length == 1) {
                FileMessage mediaFile = mediaFiles[0];
                if (mediaFile.getId() <= 0) {
                    Log.w(TAG, "delete: mediaFile is error " + mediaFile);
                    return;
                }
                mediaFileDao.delete(MediaFile.fileMessageToMediaFile(mediaFile));
            } else {
                MediaFile[] files = new MediaFile[mediaFiles.length];
                for (int i = 0; i < mediaFiles.length; i++) {
                    FileMessage mediaFile = mediaFiles[i];
                    if (mediaFile.getId() <= 0) {
                        Log.w(TAG, "delete: mediaFile is error, to next " + mediaFile);
                        continue;
                    }
                    files[i] = MediaFile.fileMessageToMediaFile(mediaFile);
                }
                mediaFileDao.delete(files);
            }
            //通知数据变化
            notifyDetect(mediaFiles);
        });
    }

    @Override
    public List<FileMessage> queryAll() {
        //限制500条
        List<MediaFile> mediaFiles = mediaFileDao.queryAll(500);
        Log.d(TAG, "queryAll: mediaFiles = " + mediaFiles);
        List<FileMessage> fileMessages = new ArrayList<>();
        for (MediaFile mediaFile : mediaFiles) {
            fileMessages.add(MediaFile.mediaFileToFileMessage(mediaFile));
        }
        Log.d(TAG, "queryAll: fileMessages = " + fileMessages.size());
        if (!fileMessages.isEmpty()) {
            setLatestModified(fileMessages.get(0).getLastModified());
        }
        return fileMessages;
    }

    /**
     * 获取列表当中最新的一条数据所处的时间
     *
     * @return long
     */
    @Override
    public long getLatestModified() {
        return latestModified;
    }

    /**
     * 配置列表当中最新的一条数据所处的时间
     *
     * @param latestModified latestModified
     */
    public void setLatestModified(long latestModified) {
        Log.d(TAG, "setLatestModified: latestModified = " + latestModified);
        this.latestModified = latestModified;
    }

    /**
     * 通知文件删除
     */
    private void notifyInit() {
        readLock.lock();
        try {
            for (Map.Entry<String, IRecentlyDataListener> entry : listenerMap.entrySet()) {
                Log.d(TAG, "notifyInit: className = " + entry.getKey());
                entry.getValue().onDataBaseInit();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知文件删除
     */
    private void notifyUpdate(FileMessage... mediaFiles) {
        readLock.lock();
        try {
            for (Map.Entry<String, IRecentlyDataListener> entry : listenerMap.entrySet()) {
                Log.d(TAG, "notifyUpdate: className = " + entry.getKey());
                entry.getValue().onUpdate(mediaFiles);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知文件删除
     */
    private void notifyDetect(FileMessage... mediaFiles) {
        readLock.lock();
        try {
            for (Map.Entry<String, IRecentlyDataListener> entry : listenerMap.entrySet()) {
                Log.d(TAG, "notifyDetect: className = " + entry.getKey());
                entry.getValue().onDelete(mediaFiles);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void registerListener(String flag, IRecentlyDataListener listener) {
        writeLock.lock();
        try {
            listenerMap.put(flag, listener);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unRegisterListener(String flag) {
        writeLock.lock();
        try {
            listenerMap.remove(flag);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 删除超过限制的条目
     */
    private void deleteLimitItem() {
        //每次进程创建，删除后续的条目
        mediaFileDao.delete(mediaFileDao.queryAll(501, -1));
        Log.d(TAG, "deleteLimitItem: finish");
    }
}
