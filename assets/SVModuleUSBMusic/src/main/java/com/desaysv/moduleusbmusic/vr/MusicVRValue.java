package com.desaysv.moduleusbmusic.vr;

/**
 * @author uidq1846
 * @desc 媒体音乐的Key值
 * @time 2023-2-7 15:09
 */
public class MusicVRValue {

    /**
     * 指定歌手歌曲播放的Action字段值定义
     */
    public static class PlayMusicAction {

        /**
         * 播放歌曲
         */
        public static final String OPEN = "OPEN";

        /**
         * 关闭歌曲
         */
        public static final String CLOSE = "CLOSE";
    }

    /**
     * 语音关于音源划分值
     */
    public static class Source {

        public static final String FAVORITE = "FAVORITE";

        /**
         * 优先级为：本机-USB-蓝牙
         */
        public static final String ALL = "ALL";

        /**
         * 本地音乐
         */
        public static final String LOCAL = "LOCAL";

        /**
         * USB
         */
        public static final String USB = "USB";

        /**
         * USB1
         */
        public static final String USB1 = "USB1";

        /**
         * USB2
         */
        public static final String USB2 = "USB2";

        /**
         * BT
         */
        public static final String BT = "BT";

        /**
         * NET
         */
        public static final String NET = "NET";

        /**
         * CARPLAY
         */
        public static final String CARPLAY = "CARPLAY";

        /**
         * CARLIFE
         */
        public static final String CARLIFE = "CARLIFE";
    }

    /**
     * 播放模式类型
     */
    public static class PlayModeType {

        /**
         * 顺序播放 ORDER
         */
        public static final String ORDER = "ORDER";

        /**
         * 随机播放
         */
        public static final String RANDOM = "RANDOM";

        /**
         * 单曲循环
         */
        public static final String SINGLE = "SINGLE";

        /**
         * 循环播放
         */
        public static final String CYCLE = "CYCLE";
    }

    /**
     * 歌曲收藏列表控制
     */
    public static class CollectAction {

        /**
         * 打开
         */
        public static final String OPEN = "OPEN";

        /**
         * 播放歌曲
         */
        public static final String PLAY = "PLAY";

        /**
         * PRE
         */
        public static final String PRE = "PRE";

        /**
         * NEXT
         */
        public static final String NEXT = "NEXT";

        /**
         * CANCEL_COLLECT
         */
        public static final String CANCEL_COLLECT = "CANCEL_COLLECT";

        /**
         * COLLECT
         */
        public static final String COLLECT = "COLLECT";
    }

    /**
     * 列表歌曲播放
     */
    public static class ControlPLayListAction {

        /**
         * 播放歌曲
         */
        public static final String OPEN = "OPEN";

        /**
         * 关闭歌曲
         */
        public static final String CLOSE = "CLOSE";
    }

    /**
     * 列表歌曲播放
     */
    public static class ControlPLayListType {

        /**
         * MY
         */
        public static final String MY = "MY";

        /**
         * ALL
         */
        public static final String ALL = "ALL";

        /**
         * ARTIST
         */
        public static final String ARTIST = "ARTIST";

        /**
         * RECENT
         */
        public static final String RECENT = "RECENT";
    }

    /**
     * 播放指定风格
     */
    public static class GenreType {

        /**
         * ROCK
         */
        public static final String ROCK = "ROCK";

        /**
         * POPULAR
         */
        public static final String POPULAR = "POPULAR";

        /**
         * CLASSIC
         */
        public static final String CLASSIC = "CLASSIC";
    }

    /**
     * 播放控制
     */
    public static class PlayStateAction {

        /**
         * PAUSE
         */
        public static final String PAUSE = "PAUSE";

        /**
         * PLAY
         */
        public static final String PLAY = "PLAY";

        /**
         * NEXT
         */
        public static final String NEXT = "NEXT";

        /**
         * PREVIOUS
         */
        public static final String PREVIOUS = "PREVIOUS";

        /**
         * REPEAT
         */
        public static final String REPEAT = "REPEAT";

        /**
         * OPEN
         */
        public static final String OPEN = "OPEN";
    }

    /**
     * 播放控制
     */
    public static class PlayStateType {

        /**
         * USB
         */
        public static final String USB = "USB";

        /**
         * REPEAT
         */
        public static final String USB_1 = "USB_1";

        /**
         * USB_2
         */
        public static final String USB_2 = "USB_2";

        /**
         * BT
         */
        public static final String BT = "BT";

        /**
         * 语音控制CarPlay
         */
        public static final String CRAPLAY = "CARPLAY";

        /**
         * 语音控制CarLife
         */
        public static final String CARLIFE = "CARLIFE";

        /**
         * CURRENT
         */
        public static final String CURRENT = "";
    }

    /**
     * 播放状态
     */
    public static class SceneStatus {

        /**
         * 播放当中
         */
        public static final String PLAYING = "playing";

        /**
         * 停止状态
         */
        public static final String PAUSED = "paused";
    }

    /**
     * 活动状态
     * 前台
     * 后台
     * 暂时不需要关注
     */
    public static class ActiveStatus {

        /**
         * 应用在前台
         */
        public static final String FOREGROUND = "fg";

        /**
         * 应用在后台
         */
        public static final String BACKGROUND = "bg";

        /**
         * 暂时不需要关注
         */
        public static final String NO_EXISTS = "noExists";
    }

    /**
     * 启动App动作
     */
    public static class SkipAppAction {

        /**
         * 启动App
         */
        public static final String OPEN = "OPEN";

        /**
         * 关闭
         */
        public static final String CLOSE = "CLOSE";
    }
}
