package com.desaysv.localmediasdk.sdk.bean;

/**
 * Created by LZM on 2020-8-24
 * Comment 常量
 *
 * @author uidp5370
 */
public class Constant {

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
    }

    //启动到某个界面的类型定义
    public static class NavigationFlag {

        /**
         * 表示启动到主界面
         */
        public static final int FLAG_MAIN = 0;

        /**
         * 表示启动到播放界面
         */
        public static final int FLAG_PLAY = 1;
    }


    //RadioBand的类型定义
    //这里定义的Band只是对外部使用，Radio应用用的是另外的定义
    //Radio应用需要根据这个进行平替
    public static class RadioBand {

        public static final int BAND_AM = 0;

        public static final int BAND_FM = 1;

        public static final int BAND_DAB = 2;
    }

    public static class ListType {
        /**
         * 收藏列表
         */
        public static final String LIST_COLLECT = "list_collect";
        /**
         * 有效列表
         */
        public static final String LIST_EFFECT = "list_effect";
        /**
         * 全部列表
         */
        public static final String LIST_ALL = "list_all";
    }

    /**
     * 指定启动媒体源时的目标页面
     */
    public static class OpenSourceViewType {
        //默认页，如全部媒体源的首页
        public static final int DEFAULT_VIEW = 0;
        //各个源的列表源，或各个源的首页
        public static final int LIST_VIEW = 1;
        //各个源的播放页
        public static final int PLAY_VIEW = 2;
    }

    /**
     * 指定启动媒体源时是否需要拉起界面
     */
    public static class OpenSourceUI {
        //显示源界面
        public static final boolean SHOW_UI = true;
        //不显示源界面
        public static final boolean NOT_SHOW_UI = false;
    }

    /**
     * 设备连接状态
     */
    public static class DeviceState {
        /**
         * 连接
         */
        public static final String CONNECTED = "CONNECTED";

        /**
         * 未连接
         */
        public static final String DISCONNECTED = "DISCONNECTED";
    }
}
