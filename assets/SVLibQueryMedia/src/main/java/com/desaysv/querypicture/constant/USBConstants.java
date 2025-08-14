package com.desaysv.querypicture.constant;

import android.net.Uri;

/**
 * @author uidp5370
 * @date 2019-3-4
 * 设备状态的产量类
 */
public class USBConstants {

    /**
     * USB路径
     */
    public static class USBPath {
        /**
         * USB0的设备路径
         */
        public static final String USB0_PATH = "/storage/usb0";

        /**
         * USB1的设备路径
         */
        public static final String USB1_PATH = "/storage/usb1";
    }


    /**
     * Provider url的常量
     */
    public static class ProviderUrl {
        /**
         * 音乐数据的UrL
         */
        public static final Uri MUSIC_DATA_URL = Uri.parse("content://com.desaysv.mediaprovider.music/music");

        /**
         * 视频数据的UrL
         */
        public static final Uri VIDEO_DATA_URL = Uri.parse("content://com.desaysv.mediaprovider.video/video");

        /**
         * 图片数据的UrL
         */
        public static final Uri PIC_DATA_URL = Uri.parse("content://com.desaysv.mediaprovider.picture/picture");

    }

    /**
     * 数据库扫描状态的常量
     */
    public static class ProviderScanStatus {

        /**
         * 扫描中
         */
        public static final int SCANNING = 3;

        /**
         * 扫描完成
         */
        public static final int SCAN_FINISHED = 4;

    }


    /**
     * 数据更新广播给过来的key值
     */
    public static class USBBroadcastKey {
        /**
         * Provider更新数据给过来的路径key
         */
        public static final String USB_PATH = "filePathType";

        /**
         * Provider更新给过来扫描状态
         */
        public static final String USB_EXTRA_SCAN_STATUS = "usb_extra_scan_status";
    }

    /**
     * 循环模式
     */
    public static class LoopType {
        /**
         * 列表循环
         */
        public static final String CYCLE = "CYCLE";
        /**
         * 单曲循环
         */
        public static final String SINGLE = "SINGLE";
        /**
         * 随机播放
         */
        public static final String RANDOM = "RANDOM";
        /**
         * 默认值，不循环
         */
        public static final String NULL = "NULL";
    }

    /**
     * Provider广播的Action
     */
    public static class BroadcastAction {
        /**
         * Provider音乐数据刷新的时候，会发送的广播Action，没有带ID3
         */
        public final static String MUSIC_REFRESH_DATA = "music_refresh_data";

        /**
         * Provider图片数据刷新的时候，会发送的广播Action，没有带ID3
         */
        public final static String PICTURE_REFRESH_DATA = "picture_refresh_data";

        /**
         * Provider视频数据刷新的时候，会发送的广播Action，没有带ID3
         */
        public final static String VIDEO_REFRESH_DATA = "video_refresh_data";

        /**
         * Provider音乐数据刷新的时候，会发送的广播Action，带ID3
         */
        public final static String MUSIC_ID3_REFRESH_DATA = "music_id3_refresh_data";

        /**
         * Provider视频数据刷新的时候，会发送的广播Action，带ID3
         */
        public final static String VIDEO_ID3_REFRESH_DATA = "video_id3_refresh_data";
    }

}

