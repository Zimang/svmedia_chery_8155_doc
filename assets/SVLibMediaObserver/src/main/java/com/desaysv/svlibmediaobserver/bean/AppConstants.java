package com.desaysv.svlibmediaobserver.bean;

/**
 * Created by ZNB on 2023-06-15，用于应用内数据传递，所以比较精简
 */
public class AppConstants {


    public static class Source{
        /**
         * 本地音乐的音频焦点
         */
        public static final String LOCAL_MUSIC_SOURCE = "local_music";

        /**
         * USB0音乐的音频焦点
         */
        public static final String USB0_MUSIC_SOURCE = "usb0";

        /**
         * USB1音乐的音频焦点
         */
        public static final String USB1_MUSIC_SOURCE = "usb1";

        /**
         * USB2音乐的音频焦点
         */
        public static final String USB2_MUSIC_SOURCE = "usb2";

        /**
         * USB3音乐的音频焦点
         */
        public static final String USB3_MUSIC_SOURCE = "usb3";

        /**
         * USB3音乐的音频焦点
         */
        public static final String USB4_MUSIC_SOURCE = "usb4";


        /**
         * 蓝牙音乐的音频焦点
         */
        public static final String BT_MUSIC_SOURCE = "bt_music";

        /**
         * AM的音频焦点
         */
        public static final String AM_SOURCE = "am";

        /**
         * FM的音频焦点
         */
        public static final String FM_SOURCE = "fm";

        /**
         * DAB的音频焦点
         */
        public static final String DAB_SOURCE = "dab";

        /**
         * 电台通用动作的类型
         */
        public static final String LOCAL_RADIO_SOURCE = "local_radio";
    }


    public static class Page{
        /**
         * 模块主界面
         */
        public static final int PAGE_MAIN = 0;

        /**
         * 模块播放界面
         */
        public static final int PAGE_PLAY = 1;

        /**
         * 模块其它非主界面
         */
        public static final int PAGE_OTHER = 2;
    }
}
