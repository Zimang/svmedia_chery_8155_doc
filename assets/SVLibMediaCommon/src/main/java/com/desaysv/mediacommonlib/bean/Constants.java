package com.desaysv.mediacommonlib.bean;

/**
 * created by ZNB on 2022-11-19
 * 定义Intent设置需要的 Key 和 类型
 */
public class Constants {

    //启动某个音源类型的界面
    public static class Source{
        /**
         * 拉起界面使用的Key
         */
        public static final String SOURCE_KEY = "source_key";

        /**
         * 表示要跳转到Radio界面
         */
        public static final String SOURCE_RADIO = "radio";

        /**
         * 表示要跳转到Music界面
         */
        public static final String SOURCE_MUSIC = "music";

        /**
         * 表示要跳转到BT界面
         */
        public static final String SOURCE_BT = "bt";
    }

    //启动到某个界面的类型定义
    public static class NavigationFlag{

        /**
         * key值
         */
        public static final String KEY = "NavigationFlag";

        /**
         * 表示启动到主界面
         */
        public static final int FLAG_MAIN = 0;

        /**
         * 表示启动到播放界面
         */
        public static final int FLAG_PLAY = 1;

        /**
         * 表示启动到DAB播放界面
         */
        public static final int FLAG_DAB_PLAY = 2;

        /**
         * 表示启动到USB对应的Tab
         */
        public static final int FLAG_USB = 3;

        /**
         * 表示启动到DAB播放界面的列表界面
         */
        public static final int FLAG_DAB_PLAY_LIST = 4;

        /**
         * 表示启动到DAB播放界面的EPG界面
         */
        public static final int FLAG_DAB_PLAY_EPG = 5;

        /**
         * 表示启动到Radio列表界面的收藏界面
         */
        public static final int FLAG_RADIO_COLLECT = 6;

        /**
         * 表示语音启动到主界面的FM
         */
        public static final int FLAG_VR_MAIN_FM = 7;

        /**
         * 表示语音启动到主界面的AM
         */
        public static final int FLAG_VR_MAIN_AM = 8;

        /**
         * 表示语音启动到主界面的DAB
         */
        public static final int FLAG_VR_MAIN_DAB = 9;

        /**
         * 表示语音启动到Radio列表界面的收藏界面
         */
        public static final int FLAG_VR_MAIN_COLLECT = 10;

        /**
         * 表示启动到列表界面
         */
        public static final int FLAG_VR_MAIN_LIST = 11;

        /**
         * 表示启动到组合播放界面
         */
        public static final int FLAG_MULTI_PLAY = 12;
    }

    public static class FileMessageData {
        public static final String PATH = "path";
    }

}
