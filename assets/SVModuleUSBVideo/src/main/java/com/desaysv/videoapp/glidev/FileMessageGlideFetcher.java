package com.desaysv.videoapp.glidev;


import androidx.annotation.NonNull;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;


/**
 * 用于解析视频缩略图
 */
public class FileMessageGlideFetcher implements DataFetcher<InputStream> {
    private static final String TAG = "FileMessageGlideFetcher";
    private final FileMessage fileMessage;
    private volatile boolean mIsCanceled = false;

    public FileMessageGlideFetcher(FileMessage FileMessage) {
        this.fileMessage = FileMessage;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        MediaMetadataRetriever mmr = null;
        try {
            if (mIsCanceled) {
                Log.e(TAG, "loadData: current is Canceled , " + fileMessage);
                callback.onLoadFailed(new Exception("current is Canceled , " + fileMessage));
                return;
            }
            if (null == fileMessage) {
                Log.e(TAG, "loadData: FileMessage is null , " + fileMessage);
                callback.onLoadFailed(new Exception("FileMessage is null , " + fileMessage));
                return;
            }
            File file = new File(fileMessage.getPath());
            if (!file.exists()) {
                Log.e(TAG, "current java.io.FileNotFoundException: open failed: ENOENT (No such file or directory) , " + fileMessage);
                callback.onLoadFailed(new Exception("current java.io.FileNotFoundException: open failed: ENOENT (No such file or directory) , " + fileMessage));
                return;
            }
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(file.getAbsolutePath());
            Bitmap frameAtTime = mmr.getFrameAtTime();
            if (null == frameAtTime) {
                Log.e(TAG, "loadData: frameAtTime is null , " + fileMessage);
                callback.onLoadFailed(new Exception("frameAtTime is null , " + fileMessage));
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            frameAtTime.compress(Bitmap.CompressFormat.PNG, 100, baos);
            InputStream result = new ByteArrayInputStream(baos.toByteArray());
            callback.onDataReady(result);
        } catch (Exception e) {
            Log.d(TAG, "loadData: " + e.getMessage() + ", " + fileMessage, e);
            callback.onLoadFailed(e);
        } finally {
            if (mmr != null) {
                mmr.release();
//                Log.d(TAG, "loadData: mmr.release()");
            }
        }
    }

    @Override
    public void cleanup() {
//        Log.d(TAG, "cleanup: ");
    }

    @Override
    public void cancel() {
//        Log.d(TAG, "cancel: ");
        mIsCanceled = true;
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
