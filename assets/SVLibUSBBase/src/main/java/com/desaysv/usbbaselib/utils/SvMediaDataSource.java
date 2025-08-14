package com.desaysv.usbbaselib.utils;

import android.media.MediaDataSource;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * add by uidq1846 on 20201209
 * 媒体文件封装，避免直接持有文件
 */
public class SvMediaDataSource extends MediaDataSource {
    private RandomAccessFile file;
    private static final String TAG = "SvMediaDataSource";

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (position > file.length()) {
            Log.w(TAG, "readAt position = " + position + ", " + buffer.length + ", " + offset + ", " + size + ", length = " + file.length());
            position = file.length();
        }
        file.seek(position);
        return file.read(buffer, offset, size);
    }

    @Override
    public long getSize() throws IOException {
        return file.length();
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public SvMediaDataSource(String path) {
        try {
            file = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "I/O read failed, path not found" + path);
        }
    }
}
