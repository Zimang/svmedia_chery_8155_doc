package com.desaysv.moduleradio.vr;

/**
 * created by ZNB on 2022-12-23
 * 定义语音的一些常量，例如 Key 和 Action等
 */
public class SVRadioVRConstant {

    /**
     * Key是唯一的
     */
    public static class Key{
        /**
         * 收听广播
         */
        public static final String PLAY_BY_BAND = "playByBand";

        /**
         * 扫描电台
         */
        public static final String SCAN_BAND = "scanBand";

        /**
         * 收藏列表
         */
        public static final String CONTROL_COLLECT = "controlCollection";

        /**
         * 收藏列表
         */
        public static final String CONTROL_COLLECT_COMMON = "controlCollect";

        /**
         * 播放控制
         */
        public static final String CONTROL_PLAY = "controlPlayState";

        /**
         * 播放列表控制
         */
        public static final String CONTROL_PLAY_LIST = "controlPlayingList";

        /**
         * 播放列表控制
         */
        public static final String CONTROL_PLAY_LIST1 = "controlPlayList";

        /**
         * 主动获取收音状态
         */
        public static final String REQUEST_STATUS = "requestRadioStatus";
    }

    /**
     * 不同的Key对应的Action可能一样
     */
    public static class Action{
        /**
         * play操作，需要对应具体的Key来处理
         */
        public static final String PLAY = "PLAY";

        /**
         * open操作，需要对应具体的Key来处理
         */
        public static final String OPEN = "OPEN";

        /**
         * close操作，需要对应具体的Key来处理
         */
        public static final String CLOSE = "CLOSE";

        /**
         * previous操作，需要对应具体的Key来处理
         */
        public static final String PREVIOUS = "PREVIOUS";

        /**
         * next操作，需要对应具体的Key来处理
         */
        public static final String NEXT = "NEXT";

        /**
         * cancel collect操作，需要对应具体的Key来处理
         */
        public static final String CANCEL_COLLECT = "CANCEL_COLLECT";

        /**
         * collect，需要对应具体的Key来处理
         */
        public static final String COLLECT = "COLLECT";

        /**
         * pause操作，需要对应具体的Key来处理
         */
        public static final String PAUSE = "PAUSE";

        /**
         * dislike操作，需要对应具体的Key来处理
         */
        public static final String DISLIKE = "DISLIKE";

    }

    /**
     * 对应操作的类型
     */
    public static class Type{
        /**
         * FM类型，指定处理FM
         */
        public static final String FM = "FM";

        /**
         * AM类型，指定处理FM
         */
        public static final String AM = "AM";

        /**
         * DAB类型，指定处理DAB
         */
        public static final String DAB = "DAB";

        /**
         * RECENTLY类型，指定处理播放最近
         */
        public static final String RECENTLY = "RECENTLY";

        /**
         * 无限制类型，默认处理当前
         */
        public static final String UNLIMITED = "UNLIMITED";

    }

    /**
     * 语音电台上传状态
     */
    public static class VRUploadRadioStatus {
        /**
         * 播放信息（讯飞平台）
         */
        public static final String VR_RADIO_PLAY_RESPONSE = "notifyRadioPlayInfo";
        /**
         * 收藏列表
         */
        public static final String VR_RADIO_COLLECT_LIST_RESPONSE = "notifyRadioCollectList";
        /**
         * 扫描状态状态（讯飞平台）
         */
        public static final String VR_RADIO_SCAN_RESPONSE = "notifyRadioScanStatus";
        /**
         * seek状态（讯飞平台）
         */
        public static final String VR_RADIO_SEEK_RESPONSE = "notifyRadioSeekStatus";

    }

    public static class VRUploadRadioValue{
        /**
         * 播放
         */
        public static final String VR_RADIO_STATUE_PLAY = "playing";
        /**
         * 暂停
         */
        public static final String VR_RADIO_STATUE_PAUSE = "paused";
        /**
         * 前台
         */
        public static final String VR_RADIO_STATUE_FG = "fg";
        /**
         * 后台
         */
        public static final String VR_RADIO_STATUE_BG = "bg";
        /**
         * 不存在
         */
        public static final String VR_RADIO_STATUE_NO_EXITS = "noExists";
    }

    /**
     * 目标电台在Radio的状态
     */
    public static class TargetState{
        /**
         * 超出频点范围
         */
        public static final int STATE_OVER_RANGE = 0;

        /**
         * 不在有效列表
         */
        public static final int STATE_NOT_FM_EFFECT = 1;

        /**
         * 不在有效列表
         */
        public static final int STATE_NOT_AM_EFFECT = 2;

        /**
         * 已经在播放
         */
        public static final int STATE_FM_PLAYED = 3;

        /**
         * 已经在播放
         */
        public static final int STATE_AM_PLAYED = 4;

        /**
         * 需要播放
         */
        public static final int STATE_PLAY = 5;
    }

}
