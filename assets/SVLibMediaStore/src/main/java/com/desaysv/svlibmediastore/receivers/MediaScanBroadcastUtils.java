package com.desaysv.svlibmediastore.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by LZM on 2020-3-9
 * Comment 用来发送媒体消息变化的广播的工具类
 */
public class MediaScanBroadcastUtils {

    private static final String TAG = "MediaScanBroadcastUtils";

    //provider 通知对应的apk数据库数据变化，刷新数据
    private final static String MUSIC_REFRESH_DATA = "music_refresh_data";
    private final static String PICTURE_REFRESH_DATA = "picture_refresh_data";
    private final static String VIDEO_REFRESH_DATA = "video_refresh_data";

    private final static String MUSIC_ID3_REFRESH_DATA = "music_id3_refresh_data";
    private final static String VIDEO_ID3_REFRESH_DATA = "video_id3_refresh_data";

    private static final String USB_PATH = "filePathType";
    private static final String USB_EXTRA_SCAN_STATUS = "usb_extra_scan_status";

    /**
     * 发送音乐数据更新的广播，这个是没有ID3更新的广播，就只是文件递归的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendMusicDataUpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendMusicDataUpdateMsg: usbPath = " + usbPath + " usbScanStatus = " + usbScanStatus);
        Intent musicIntent = new Intent(MUSIC_REFRESH_DATA);
        musicIntent.putExtra(USB_PATH, usbPath);
        musicIntent.putExtra(USB_EXTRA_SCAN_STATUS, usbScanStatus);
        context.sendBroadcast(musicIntent);
    }

    /**
     * 发送图片数据更新广播，这个是没有ID3更新的广播，就只是文件递归的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendPicDataUpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendPicDataUpdateMsg: usbPath = " + usbPath + " usbScanStatus = " + usbScanStatus);
        Intent picIntent = new Intent(PICTURE_REFRESH_DATA);
        picIntent.putExtra(USB_PATH, usbPath);
        picIntent.putExtra(USB_EXTRA_SCAN_STATUS, usbScanStatus);
        context.sendBroadcast(picIntent);
    }

    /**
     * 发送视频数据更新的广播，这个是没有ID3更新的广播，就只是文件递归的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendVideoDataUpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendVideoDataUpdateMsg: usbPath = " + usbPath + " usbScanStatus = " + usbScanStatus);
        Intent videoIntent = new Intent(VIDEO_REFRESH_DATA);
        videoIntent.putExtra(USB_PATH, usbPath);
        videoIntent.putExtra(USB_EXTRA_SCAN_STATUS, usbScanStatus);
        context.sendBroadcast(videoIntent);
    }


    /**
     * 发送全部媒体数据跟新的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendAllDataUpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendAllDataUpdateMsg: ");
        sendMusicDataUpdateMsg(context, usbPath, usbScanStatus);
        sendPicDataUpdateMsg(context, usbPath, usbScanStatus);
        sendVideoDataUpdateMsg(context, usbPath, usbScanStatus);
    }

    /**
     * 发送全部媒体数据以及ID3数据状态变化的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendAllDataWithID3UpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendAllDataWithID3UpdateMsg: ");
        sendMusicDataUpdateMsg(context, usbPath, usbScanStatus);
        sendPicDataUpdateMsg(context, usbPath, usbScanStatus);
        sendVideoDataUpdateMsg(context, usbPath, usbScanStatus);
        sendMusicID3UpdateMsg(context, usbPath, usbScanStatus);
        sendVideoID3UpdateMsg(context, usbPath, usbScanStatus);
    }

    /**
     * 发送音乐ID3数据扫描状态刷新的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendMusicID3UpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendMusicID3UpdateMsg: usbPath = " + usbPath + " usbScanStatus = " + usbScanStatus);
        Intent musicIntent = new Intent(MUSIC_ID3_REFRESH_DATA);
        musicIntent.putExtra(USB_PATH, usbPath);
        musicIntent.putExtra(USB_EXTRA_SCAN_STATUS, usbScanStatus);
        context.sendBroadcast(musicIntent);
    }


    /**
     * 发送视频ID3数据扫描状态刷新的广播
     *
     * @param context       上下文
     * @param usbPath       扫描USB的路径
     * @param usbScanStatus 扫描状态
     */
    static void sendVideoID3UpdateMsg(Context context, String usbPath, int usbScanStatus) {
        Log.d(TAG, "sendVideoID3UpdateMsg: usbPath = " + usbPath + " usbScanStatus = " + usbScanStatus);
        Intent videoIntent = new Intent(VIDEO_ID3_REFRESH_DATA);
        videoIntent.putExtra(USB_PATH, usbPath);
        videoIntent.putExtra(USB_EXTRA_SCAN_STATUS, usbScanStatus);
        context.sendBroadcast(videoIntent);
    }


}
