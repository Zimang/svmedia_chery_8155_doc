package com.desaysv.localmediasdk.bean;


/**
 * Created by LZM on 2020-8-3
 * Comment
 *
 * @author uidp5370
 */
public class LocalMediaConstants {

    /**
     * 控制命令
     */
    public static class ControlAction {

        //控制播放的命令
        public static final String PLAY = "play";

        //控制暂停的命令
        public static final String PAUSE = "pause";

        //控制暂停或者播放的命令
        public static final String PLAY_OR_PAUSE = "play_or_pause";

        //控制下一曲的命令
        public static final String NEXT = "next";

        //控制上一曲的命令
        public static final String PRE = "pre";

        //设置时长的命令
        public static final String SEEK_TO = "seek_to";

        //开始快进的命令
        public static final String START_FAST_FORWARD = "start_fast_forward";

        //停止快进的命令
        public static final String STOP_FAST_FORWARD = "stop_fast_forward";

        //开始快退的命令
        public static final String START_REWIND = "start_rewind";

        //停止快退的命令
        public static final String STOP_REWIND = "stop_rewind";

        //收藏的命令
        public static final String CHANGE_COLLECT = "change_collect";

        //设置循环模式的命令
        public static final String CHANGE_LOOP_TYPE = "change_loop_type";

        //打开对应音源的命令
        public static final String OPEN_SOURCE = "open_source";

        //设置Radio对应Band的命令
        public static final String SET_BAND = "set_band";

        //设置并打开对应Media的命令
        public static final String SET_MEDIA = "set_media";

        //开始搜索的命令
        public static final String START_AST = "start_ast";

        //停止搜索的命令
        public static final String STOP_AST = "stop_ast";
    }


    /**
     * 状态获取命令
     */
    public static class StatusAction {

        //播放状态
        public static final String PLAY_STATUS = "play_status";

        //媒体的信息
        public static final String PLAY_INFO = "play_info";

        //播放的时间
        public static final String PLAY_TIME = "play_time";

        //循环模式
        public static final String LOOP_TYPE = "loop_type";

        //收藏状态
        public static final String COLLECT_STATUS = "collect_status";

        //搜索状态
        public static final String SEARCH_STATUS = "search_status";

        //Radio频点值
        public static final String RADIO_FREQ = "radio_freq";

        //根据选择的音源获取该源的RDS/DAB设置状态
        public static final String RADIO_STATUS = "radio_status";

        //seek状态
        public static final String SEEK_STATUS = "seek_status";

        //设置状态
        public static final String SETTINGS_STATUS = "settings_status";

        //设备状态
        public static final String DEVICE_STATUS = "device_status";

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

