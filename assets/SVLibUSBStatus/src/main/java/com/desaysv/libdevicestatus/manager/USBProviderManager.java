package com.desaysv.libdevicestatus.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by LZM on 2020-11-16
 * Comment 用来获取USB Provider数据的类
 *
 * @author uidp5370
 */
public class USBProviderManager {

    private static final String TAG = "USBProviderManager";


    private static final Uri USB0_SCAN_STATUS_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb0_scan_status");

    private static final Uri USB1_SCAN_STATUS_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb1_scan_status");

    private static final Uri USB0_MUSIC_COUNT_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb0_music_list_count");


    private static final Uri USB0_VIDEO_COUNT_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb0_video_list_count");

    private static final Uri USB0_PHOTO_COUNT_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb0_photo_list_count");

    private static final Uri USB1_MUSIC_COUNT_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb1_music_list_count");

    private static final Uri USB1_VIDEO_COUNT_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb1_video_list_count");

    private static final Uri USB1_PHOTO_COUNT_URI = Uri.parse("content://com.desaysv.mediaprovider.status/usb1_photo_list_count");

    private static USBProviderManager instance;

    private ContentResolver mContentResolver;

    public static USBProviderManager getInstance() {
        if (instance == null) {
            synchronized (USBProviderManager.class) {
                if (instance == null) {
                    instance = new USBProviderManager();
                }
            }
        }
        return instance;
    }

    private USBProviderManager() {
    }

    /**
     * 初始化，获取USB的数据扫描状态
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        mContentResolver = context.getContentResolver();
    }

    /**
     * 获取USB0的扫描状态
     *
     * @return USB0的扫描状态 USB_STATUS_SCAN_STARTED;USB_STATUS_SCAN_FINISHED;USB_STATUS_SCAN_IDLE;
     */
    public int getUSB0ScanStatus() {
        int scanStatus = -1;
        String type = mContentResolver.getType(USB0_SCAN_STATUS_URI);
        Log.d(TAG, "getUSB0ScanStatus: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            scanStatus = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB0ScanStatus: scanStatus = " + scanStatus);
        return scanStatus;
    }

    /**
     * 获取USB1的扫描状态
     *
     * @return USB1的扫描状态 USB_STATUS_SCAN_STARTED;USB_STATUS_SCAN_FINISHED;USB_STATUS_SCAN_IDLE;
     */
    public int getUSB1ScanStatus() {
        int scanStatus = -1;
        String type = mContentResolver.getType(USB1_SCAN_STATUS_URI);
        Log.d(TAG, "getUSB1ScanStatus: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            scanStatus = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB1ScanStatus: scanStatus = " + scanStatus);
        return scanStatus;
    }


    /**
     * 获取USB0音乐的列表数量
     *
     * @return 列表的数量
     */
    public int getUSB0MusicListCount() {
        int listCount = -1;
        String type = mContentResolver.getType(USB0_MUSIC_COUNT_URI);
        Log.d(TAG, "getUSB0MusicListCount: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            listCount = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB0MusicListCount: listCount = " + listCount);
        return listCount;
    }

    /**
     * 获取USB0视频的列表数量
     *
     * @return 列表的数量
     */
    public int getUSB0VideoListCount() {
        int listCount = -1;
        String type = mContentResolver.getType(USB0_VIDEO_COUNT_URI);
        Log.d(TAG, "getUSB0VideoListCount: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            listCount = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB0VideoListCount: listCount = " + listCount);
        return listCount;
    }

    /**
     * 获取USB0图片的列表数量
     *
     * @return 列表的数量
     */
    public int getUSB0PhotoListCount() {
        int listCount = -1;
        String type = mContentResolver.getType(USB0_PHOTO_COUNT_URI);
        Log.d(TAG, "getUSB0PhotoListCount: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            listCount = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB0PhotoListCount: listCount = " + listCount);
        return listCount;
    }

    /**
     * 获取USB1音乐的列表数量
     *
     * @return 列表的数量
     */
    public int getUSB1MusicListCount() {
        int listCount = -1;
        String type = mContentResolver.getType(USB1_MUSIC_COUNT_URI);
        Log.d(TAG, "getUSB1MusicListCount: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            listCount = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB1MusicListCount: listCount = " + listCount);
        return listCount;
    }

    /**
     * 获取USB1视频的列表数量
     *
     * @return 列表的数量
     */
    public int getUSB1VideoListCount() {
        int listCount = -1;
        String type = mContentResolver.getType(USB1_VIDEO_COUNT_URI);
        Log.d(TAG, "getUSB1VideoListCount: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            listCount = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB1VideoListCount: listCount = " + listCount);
        return listCount;
    }

    /**
     * 获取USB1图片的列表数量
     *
     * @return 列表的数量
     */
    public int getUSB1PhotoListCount() {
        int listCount = -1;
        String type = mContentResolver.getType(USB1_PHOTO_COUNT_URI);
        Log.d(TAG, "getUSB1PhotoListCount: type = " + type);
        if (!TextUtils.isEmpty(type)) {
            listCount = Integer.parseInt(type);
        }
        Log.d(TAG, "getUSB1PhotoListCount: listCount = " + listCount);
        return listCount;
    }

}
