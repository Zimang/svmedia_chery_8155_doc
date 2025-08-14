package com.desaysv.moduleusbmusic.businesslogic.control;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.ICopy;
import com.desaysv.moduleusbmusic.listener.IDelete;
import com.desaysv.moduleusbmusic.listener.IFileControl;
import com.desaysv.moduleusbmusic.listener.ProgressListener;
import com.desaysv.moduleusbmusic.utils.MusicTool;
import com.desaysv.svlibmediastore.receivers.MediaScanStateManager;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author uidq1846
 * @desc 文件删除控制器
 * @time 2022-12-23 18:55
 */
public class CopyDeleteControl implements ICopy, IDelete, IFileControl {
    private final String TAG = CopyDeleteControl.class.getSimpleName();
    //用以记录已经拷贝的条目信息
    private final Map<String, FileMessage> copiedMap = new HashMap<>();
    //复制进度监听
    private final Map<String, ProgressListener> copyProgressListenerMap = new HashMap<>();
    //删除进度监听
    private final Map<String, ProgressListener> deleteProgressListenerMap = new HashMap<>();
    //总共已经使用的大小
    private long totalUsedSize = 0;
    //容量大小限制 2G
    private final static long COPY_LIMIT_SIZE = 2 * 1024 * 1024 * 1024L;
    //进行加锁处理
    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock readLock = reentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = reentrantLock.writeLock();
    //拷贝通知读写锁
    private final ReentrantReadWriteLock copyReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock copyReadLock = copyReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock copyWriteLock = copyReentrantLock.writeLock();
    //删除通知读写锁
    private final ReentrantReadWriteLock deleteReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock deleteReadLock = deleteReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock deleteWriteLock = deleteReentrantLock.writeLock();
    //是否删除当中
    private boolean isDeleting = false;
    //是否复制当中
    private boolean isAllCoping = false;
    //单个条目的复制
    private boolean isItemCoping = false;
    //是否第一次加载数据
    private boolean firstTimeInitCopiedMap = true;
    private Handler handler;

    private CopyDeleteControl() {
    }

    private static final class IFileControlHolder {
        static final IFileControl iFileControl = new CopyDeleteControl();
    }

    public static IFileControl getInstance() {
        return IFileControlHolder.iFileControl;
    }

    @Override
    public void init() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        clearTempFiles();
        USBMusicDate.getInstance().setMusicListChangeListener(iMusicListChange);
    }

    @Override
    public ICopy getCopyControl() {
        return this;
    }

    @Override
    public IDelete getDeleteControl() {
        return this;
    }

    @Override
    public void copyFile(final FileMessage oldItem, final String newFileFolderPath) {
        //异步进行
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                isItemCoping = true;
                if (!isStorageAvailable(oldItem)) {
                    notifyCopySizeLimit(oldItem);
                    isItemCoping = false;
                    notifyCopyFinish();
                    return;
                }
                copy(oldItem, newFileFolderPath, oldItem.getSize());
                isItemCoping = false;
                if (!TextUtils.isEmpty(newFileFolderPath)) {
                    MediaScanStateManager.getInstance().scan(newFileFolderPath);
                }
                notifyCopyFinish();
            }
        });
    }

    @Override
    public void copyFiles(final List<FileMessage> fileMessages, final String targetFolderPath) {
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                isAllCoping = true;
                //开一个定时器，每10s刷新下
                scanFileDelayed(targetFolderPath, 10000);
                long totalSize = 0;
                for (FileMessage oldItem : fileMessages) {
                    //计算总大小
                    totalSize += oldItem.getSize();
                }
                Log.i(TAG, "copyFiles: totalSize = " + totalSize);
                for (FileMessage oldItem : fileMessages) {
                    if (!isStorageAvailable(oldItem)) {
                        notifyCopySizeLimit(oldItem);
                        break;
                    }
                    copy(oldItem, targetFolderPath, totalSize);
                }
                isAllCoping = false;
                //移除掉所有信息
                handler.removeCallbacksAndMessages(null);
                if (!TextUtils.isEmpty(targetFolderPath)) {
                    MediaScanStateManager.getInstance().scan(targetFolderPath);
                }
                notifyCopyFinish();
            }
        });
    }

    /**
     * 避免下载太多音乐时刷新太慢
     *
     * @param targetFolderPath targetFolderPath
     * @param mTime            mTime
     */
    private void scanFileDelayed(String targetFolderPath, int mTime) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "copyFiles 10s scan targetFolderPath = " + targetFolderPath);
                if (!TextUtils.isEmpty(targetFolderPath)) {
                    MediaScanStateManager.getInstance().scan(targetFolderPath);
                }
                if (isCopying()) {
                    scanFileDelayed(targetFolderPath, mTime);
                }
            }
        }, mTime);
    }

    final long[] tempProgress = new long[1];

    /**
     * 具体拷贝操作
     *
     * @param oldItem           oldItem
     * @param newFileFolderPath newFileFolderPath
     */
    @SuppressLint("NewApi")
    private void copy(final FileMessage oldItem, final String newFileFolderPath, final long totalUsedSize) {
        UsbMusicPoint.getInstance().downloadFile(getContentData(oldItem));
        String oldFilePath = oldItem.getPath();
        if (TextUtils.isEmpty(oldFilePath)) {
            Log.w(TAG, "copy: path is empty!!!");
            notifyCopyFailed(oldItem);
            return;
        }
        if (isCopied(oldItem)) {
            Log.w(TAG, "copy: file already copied!!!");
            notifyCopySuccess(oldItem);
            return;
        }
        String newPath = newFileFolderPath + oldFilePath.substring(oldFilePath.lastIndexOf("/"));
        boolean copyFileState = MusicTool.copyFile(oldFilePath, newPath, new FileUtils.ProgressListener() {
            @Override
            public void onProgress(long progress) {
                Log.d(TAG, "onProgress() copyFileState called with: progress = [" + progress + "]");
                // TODO: 2022-12-24
                notifyCopyProgressChange(progress, totalUsedSize);
                tempProgress[0] = progress;
            }
        });
        if (copyFileState) {
            //原来拷贝成功才需要开始拷贝歌词
            int index = oldFilePath.lastIndexOf(".");
            if (index != -1) {
                String lrcOldFilePath = oldFilePath.substring(0, index) + ".lrc";
                String lrcNewPath = newFileFolderPath + lrcOldFilePath.substring(oldFilePath.lastIndexOf("/"));
                boolean copyLrcState = MusicTool.copyFile(lrcOldFilePath, lrcNewPath, new FileUtils.ProgressListener() {
                    @Override
                    public void onProgress(long progress) {
                        // TODO: 2022-12-24
                        Log.d(TAG, "onProgress: lrc progress = " + progress);
                    }
                });
                Log.d(TAG, "copy: copyLrcState = " + copyLrcState);
            }
            addCopiedItem(oldItem);
            notifyCopySuccess(oldItem);
        } else {
            notifyCopyFailed(oldItem);
        }
        //如果需要更新完曲目就刷新，解开注释
        /*if (!TextUtils.isEmpty(newPath)) {
            MediaScanStateManager.getInstance().scan(newPath);
        }*/
    }

    @Override
    public boolean isCopied(FileMessage fileMessage) {
        Log.d(TAG, "isCopied: key = " + fileMessage.getFileName());
        return copiedMap.containsKey(fileMessage.getFileName());
    }

    private void addCopiedItem(FileMessage fileMessage) {
        //无记录才需存入
        writeLock.lock();
        try {
            if (!copiedMap.containsKey(fileMessage.getFileName())) {
                FileMessage put = copiedMap.put(fileMessage.getFileName(), fileMessage);
                Log.d(TAG, "addCopiedItem: put = " + put + " key = " + fileMessage.getFileName());
                totalUsedSize += fileMessage.getSize();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 去除已经记忆的条目
     *
     * @param fileMessage fileMessage
     */
    private void deleteCopiedItem(FileMessage fileMessage) {
        writeLock.lock();
        try {
            FileMessage remove = copiedMap.remove(fileMessage.getFileName());
            Log.d(TAG, "deleteCopiedItem: remove = " + remove);
            totalUsedSize -= fileMessage.getSize();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 计算剩余空间是否可用
     *
     * @return T 可用  F 不可用
     */
    @Override
    public boolean isStorageAvailable(FileMessage oldItem) {
        long availableSize = COPY_LIMIT_SIZE - totalUsedSize;
        if (availableSize < oldItem.getSize()) {
            Log.w(TAG, "isStorageAvailable: available = " + availableSize + " , need to delete old files");
            return false;
        }
        return true;
    }

    /**
     * 清理本地目录当中的未下载完整的文件
     */
    private void clearTempFiles() {
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                String localMusicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                //计算剩余容量
                File file = new File(localMusicPath);
                if (file.exists() && file.isDirectory()) {
                    File[] files = file.listFiles();
                    Log.d(TAG, "clearTempFiles: files = " + Arrays.toString(files));
                    if (files == null) {
                        return;
                    }
                    for (File fileItem : files) {
                        Log.i(TAG, "clearTempFiles: exists = " + fileItem.exists() + " length = " + fileItem.length() + " path = " + fileItem.getPath());
                        //如果是过度文件则删除
                        if (fileItem.getPath().endsWith(".svtemp")) {
                            boolean delete = fileItem.delete();
                            Log.d(TAG, "clearTempFiles: delete = " + delete);
                        }
                    }
                }
                Log.d(TAG, "clearTempFiles: totalUsedSize = " + totalUsedSize);
            }
        });
    }

    /**
     * 数据变化监听
     */
    private final USBMusicDate.IListDataChange iMusicListChange = new USBMusicDate.IListDataChange() {
        @Override
        public void onUSB1MusicAllFolderMapChange() {

        }

        @Override
        public void onUSB2MusicAllFolderMapChange() {

        }

        @Override
        public void onUSB1MusicAllListChange() {

        }

        @Override
        public void onUSB2MusicAllListChange() {

        }

        @Override
        public void onLocalMusicListChange() {
            Log.d(TAG, "onLocalMusicListChange: firstTimeInitCopiedMap = " + firstTimeInitCopiedMap);
            if (!firstTimeInitCopiedMap) {
                return;
            } else {
                firstTimeInitCopiedMap = false;
            }
            MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                @Override
                public void run() {
                    List<FileMessage> localMusicAllList = USBMusicDate.getInstance().getLocalMusicAllList();
                    for (FileMessage fileMessage : localMusicAllList) {
                        addCopiedItem(fileMessage);
                    }
                }
            });
        }

        @Override
        public void onRecentMusicListChange() {

        }
    };

    @Override
    public void deleteFile(final FileMessage deleteItem) {
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                isDeleting = true;
                delete(deleteItem);
                isDeleting = false;
                if (!TextUtils.isEmpty(deleteItem.getPath())) {
                    MediaScanStateManager.getInstance().scan(deleteItem.getPath().substring(0, deleteItem.getPath().lastIndexOf("/")));
                }
                notifyDeleteFinish();
            }
        });
    }

    @Override
    public void deleteFile(final List<FileMessage> deleteItems) {
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                isDeleting = true;
                for (int i = 0; i < deleteItems.size(); i++) {
                    FileMessage fileMessage = deleteItems.get(i);
                    delete(fileMessage);
                    //下标从0开始，所以进度+1
                    notifyDeleteProgressChange(i + 1, deleteItems.size());
                }
                isDeleting = false;
                if (!deleteItems.isEmpty() && !TextUtils.isEmpty(deleteItems.get(0).getPath())) {
                    MediaScanStateManager.getInstance().scan(deleteItems.get(0).getPath().substring(0, deleteItems.get(0).getPath().lastIndexOf("/")));
                }
                notifyDeleteFinish();
            }
        });
    }

    /**
     * 删除文件
     *
     * @param deleteItem deleteItem
     */
    private void delete(FileMessage deleteItem) {
        String deleteItemPath = deleteItem.getPath();
        if (TextUtils.isEmpty(deleteItemPath)) {
            Log.w(TAG, "deleteFile: path is empty");
            notifyDeleteFailed(deleteItem);
            return;
        }
        File file = new File(deleteItemPath);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                //删除歌词文件
                //原来拷贝成功才需要开始拷贝歌词
                int index = deleteItemPath.lastIndexOf(".");
                if (index != -1) {
                    String lrcDeleteItemPath = deleteItemPath.substring(0, index) + ".lrc";
                    File lrcFile = new File(lrcDeleteItemPath);
                    if (lrcFile.exists() && lrcFile.isFile()) {
                        Log.d(TAG, "delete: lrc state = " + lrcFile.delete());
                    }
                }
                notifyDeleteSuccess(deleteItem);
                deleteCopiedItem(deleteItem);
                //删除成功，通知刷新媒体库
                //MediaScanStateManager.getInstance().scan(deleteItemPath.substring(0, deleteItemPath.lastIndexOf("/")));
                LocalMusicPoint.getInstance().delete(getContentData(deleteItem));
                return;
            }
        }
        //返回失败
        notifyDeleteFailed(deleteItem);
    }

    @Override
    public boolean isCopying() {
        return isAllCoping || isItemCoping;
    }

    @Override
    public void registerCopyProgressListener(String TAG, ProgressListener listener) {
        Log.d(TAG, "registerCopyProgressListener: TAG = " + TAG + " listener = " + listener);
        copyWriteLock.lock();
        try {
            copyProgressListenerMap.put(TAG, listener);
        } finally {
            copyWriteLock.unlock();
        }
    }

    @Override
    public void unRegisterCopyProgressListener(String TAG) {
        Log.d(TAG, "unRegisterCopyProgressListener: TAG = " + TAG);
        copyWriteLock.lock();
        try {
            copyProgressListenerMap.remove(TAG);
        } finally {
            copyWriteLock.unlock();
        }
    }

    @Override
    public boolean isDeleting() {
        return isDeleting;
    }

    @Override
    public void registerDeleteProgressListener(String TAG, ProgressListener listener) {
        Log.d(TAG, "registerDeleteProgressListener: TAG = " + TAG + " listener = " + listener);
        deleteWriteLock.lock();
        try {
            deleteProgressListenerMap.put(TAG, listener);
        } finally {
            deleteWriteLock.unlock();
        }
    }

    @Override
    public void unRegisterDeleteProgressListener(String TAG) {
        Log.d(TAG, "unRegisterDeleteProgressListener: TAG = " + TAG);
        deleteWriteLock.lock();
        try {
            deleteProgressListenerMap.remove(TAG);
        } finally {
            deleteWriteLock.unlock();
        }
    }

    /**
     * 通知复制进度
     */
    private void notifyCopyProgressChange(long progress, long total) {
        copyReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : copyProgressListenerMap.entrySet()) {
                entry.getValue().onProgressChange(progress, total);
            }
        } finally {
            copyReadLock.unlock();
        }
    }

    /**
     * 通知某个文件下载成功
     */
    private void notifyCopySuccess(FileMessage fileMessage) {
        copyReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : copyProgressListenerMap.entrySet()) {
                entry.getValue().onSuccess(fileMessage);
            }
        } finally {
            copyReadLock.unlock();
        }
    }

    /**
     * 通知某个文件下载失败
     */
    private void notifyCopyFailed(FileMessage fileMessage) {
        copyReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : copyProgressListenerMap.entrySet()) {
                entry.getValue().onFailed(fileMessage);
            }
        } finally {
            copyReadLock.unlock();
        }
    }

    /**
     * 通知某个文件下载空间限制
     */
    private void notifyCopySizeLimit(FileMessage fileMessage) {
        copyReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : copyProgressListenerMap.entrySet()) {
                entry.getValue().onSizeLimit(fileMessage);
            }
        } finally {
            copyReadLock.unlock();
        }
    }

    /**
     * 通知所有文件下载完毕
     */
    private void notifyCopyFinish() {
        copyReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : copyProgressListenerMap.entrySet()) {
                Log.d(TAG, "notifyCopyFinish: ");
                entry.getValue().onFinish();
            }
        } finally {
            copyReadLock.unlock();
        }
    }

    /**
     * 通知删除进度
     */
    private void notifyDeleteProgressChange(long progress, long total) {
        deleteReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : deleteProgressListenerMap.entrySet()) {
                entry.getValue().onProgressChange(progress, total);
            }
        } finally {
            deleteReadLock.unlock();
        }
    }

    /**
     * 通知某个文件删除成功
     */
    private void notifyDeleteSuccess(FileMessage fileMessage) {
        deleteReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : deleteProgressListenerMap.entrySet()) {
                entry.getValue().onSuccess(fileMessage);
            }
        } finally {
            deleteReadLock.unlock();
        }
    }

    /**
     * 通知某个文件删除失败
     */
    private void notifyDeleteFailed(FileMessage fileMessage) {
        deleteReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : deleteProgressListenerMap.entrySet()) {
                entry.getValue().onFailed(fileMessage);
            }
        } finally {
            deleteReadLock.unlock();
        }
    }

    /**
     * 通知所有文件删除完毕
     */
    private void notifyDeleteFinish() {
        deleteReadLock.lock();
        try {
            for (Map.Entry<String, ProgressListener> entry : deleteProgressListenerMap.entrySet()) {
                Log.d(TAG, "notifyDeleteFinish: ");
                entry.getValue().onFinish();
            }
        } finally {
            deleteReadLock.unlock();
        }
    }

    /**
     * 封装曲目信息
     *
     * @param currentPlayItem currentPlayItem
     * @return ContentData[]
     */
    private ContentData[] getContentData(FileMessage currentPlayItem) {
        ContentData[] contentData = new ContentData[4];
        contentData[0] = new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click);
        contentData[1] = new ContentData(PointValue.Field.ProgramName, currentPlayItem.getName());
        contentData[2] = new ContentData(PointValue.Field.Author, currentPlayItem.getAuthor());
        contentData[3] = new ContentData(PointValue.Field.Album, currentPlayItem.getAlbum());
        return contentData;
    }
}
