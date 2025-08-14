package com.desaysv.modulebtmusic.vr;

public class BTMusicVRConstants {

    public class Key {
        /**
         * 指定歌手歌曲播放
         */
        public static final String KEY_PLAY_MUSIC = "playMusic";
        /**
         * 播放模式切换
         */
        public static final String KEY_CONTROL_PLAY_MODE = "controlPlayMode";
        /**
         * 获取当前播放歌曲名称
         */
        public static final String KEY_GET_SONG_NAME = "getSongName";
        /**
         * 歌曲收藏列表控制
         */
        public static final String KEY_CONTROL_COLLECT = "controlCollect";
        /**
         * 列表歌曲播放
         */
        public static final String KEY_CONTROL_PLAY_LIST = "controlPlayList";
        /**
         * 播放指定风格的歌曲
         */
        public static final String KEY_PLAY_MUSIC_GRNRE = "playMusicGenre";
        /**
         * 搜索并播放
         */
        public static final String KEY_SEARCH_MUSIC = "searchMusic";
        /**
         * 查询播放模式
         */
        public static final String KEY_IS_PLAY_MODE_SUPPORTED = "isPlayModeSupported";
        /**
         * 播放控制
         */
        public static final String KEY_CONTROL_PLAY_STATE = "controlPlayState";
        /**
         * 反馈语的key
         */
        public static final String KEY_RESPONSE = "onResponse";
    }

    public class User {
        public static final String USER_ALL = "ALL";
        public static final String USER_LF = "LF";
        public static final String USER_RF = "RF";
        public static final String USER_LB = "LB";
        public static final String USER_RB = "RB";
    }

    public class Position {
        public static final String POSITION_OTHER = "";
        public static final String POSITION_F = "F";
        public static final String POSITION_B = "B";
        public static final String POSITION_LF = "LF";
        public static final String POSITION_LB = "LB";
        public static final String POSITION_RF = "RF";
        public static final String POSITION_RB = "RB";
    }

    /**
     * 指定歌手歌曲播放相关常量
     */
    public class PlayMusic {
        public class Action {
            public static final String ACTION_OPEN = "OPEN";
            public static final String ACTION_CLOSE = "CLOSE";
        }

        public class Source {
            public static final String SOURCE_USB = "USB";
            public static final String SOURCE_USB1 = "USB1";
            public static final String SOURCE_USB2 = "USB2";
            public static final String SOURCE_BT = "BT";
            public static final String SOURCE_NET = "NET";
            public static final String SOURCE_CARPLAY = "CARPLAY";
            public static final String SOURCE_CARLIFE = "CARLIFE";
        }
    }

    /**
     * 播放模式相关常量
     */
    public class PlayMode {
        public class Type {
            public static final String TYPE_ORDER = "ORDER";
            public static final String TYPE_RANDOM = "RANDOM";
            public static final String TYPE_SINGLE = "SINGLE";
            public static final String TYPE_CYCLE = "CYCLE";
        }
    }

    /**
     * 歌曲收藏列表控制相关常量
     */
    public class ControlCollect {
        public class Action {
            public static final String ACTION_OPEN = "OPEN";
            public static final String ACTION_PLAY = "PLAY";
            public static final String ACTION_PRE = "PRE";
            public static final String ACTION_NEXT = "NEXT";
            public static final String ACTION_CANCEL_COLLECT = "CANCEL_COLLECT";
            public static final String ACTION_COLLECT = "COLLECT";
        }
    }

    /**
     * 列表歌曲播放相关常量
     */
    public class ControlPlayList {
        public class Action {
            public static final String ACTION_OPEN = "OPEN";
            public static final String ACTION_CLOSE = "CLOSE";
        }

        public class Type {
            public static final String TYPE_MY = "MY";
            public static final String TYPE_ALL = "ALL";
            public static final String TYPE_ARTIST = "ARTIST";
            public static final String TYPE_RECENT = "RECENT";
        }
    }

    /**
     * 播放指定风格的歌曲相关常量
     */
    public class PlayMusicGenre {
        public class Type {
            public static final String TYPE_ROCK = "ROCK";
            public static final String TYPE_POPULAR = "POPULAR";
            public static final String TYPE_CLASSIC = "CLASSIC";
        }
    }

    /**
     * 搜索并播放相关常量
     */
    public class SearchMusic {
        public class Source {
            public static final String SOURCE_USB = "USB";
            public static final String SOURCE_USB1 = "USB1";
            public static final String SOURCE_USB2 = "USB2";
            public static final String SOURCE_BT = "BT";
            public static final String SOURCE_NET = "NET";
        }
    }

    /**
     * 播放控制相关常量
     */
    public class ControlPlayState {
        public class Action {
            public static final String ACTION_PAUSE = "PAUSE";
            public static final String ACTION_PLAY = "PLAY";
            public static final String ACTION_NEXT = "NEXT";
            public static final String ACTION_PREVIOUS = "PREVIOUS";
            public static final String ACTION_REPEAT = "REPEAT";
        }

        public class Type {
            public static final String TYPE_USB = "USB";
            public static final String TYPE_USB1 = "USB1";
            public static final String TYPE_USB2 = "USB2";
            public static final String TYPE_BT = "BT";
            public static final String TYPE_OTHER = "";
        }
    }

    public static class VRUploadRadioValue {
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
}
