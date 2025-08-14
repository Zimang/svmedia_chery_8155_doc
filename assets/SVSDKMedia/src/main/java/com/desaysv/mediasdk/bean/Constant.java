package com.desaysv.mediasdk.bean;

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
    public static class NavigationFlag{

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
    public static class RadioBand{

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
}
