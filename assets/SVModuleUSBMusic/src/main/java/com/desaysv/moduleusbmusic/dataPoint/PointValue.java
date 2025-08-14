package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc 埋点的键值
 * @time 2023-4-17 14:07
 */
public class PointValue {

    /**
     * 键值名称
     */
    public static class USBMusicKeyName {

        /**
         * 打开关闭音乐
         */
        public static final String OpenCloseClick = "Changting.LocalMusic.USBMusic.N.OpenCloseClick";

        /**
         * 操作音乐
         */
        public static final String MusicOperate = "Changting.LocalMusic.USBMusic.N.MusicOperate";

        /**
         * 音频播放模式
         */
        public static final String PlayModeClick = "Changting.LocalMusic.USBMusic.PlayMode.PlayModeClick";

        /**
         * 插入/断开USB
         */
        public static final String USBOperate = "Changting.LocalMusic.USBMusic.N.USBOperate";

        /**
         * 下载音乐文件夹事件
         */
        public static final String DownloadClick = "Changting.LocalMusic.USBMusic.Doc.DownloadClick";

        /**
         * 打开/关闭最近播放页面
         */
        public static final String RecentOpenCloseClick = "Changting.LocalMusic.Recent.N.OpenCloseClick";
    }

    /**
     * 键值名称
     */
    public static class LocalMusicKeyName {
        /**
         * 打开
         */
        public static final String OpenClick = "LocalMusic.N.N.N.OpenClick";

        /**
         * 打开
         */
        public static final String CloseClick = "LocalMusic.N.N.N.CloseClick";

        /**
         * 打开/关闭本地音乐
         */
        public static final String OpenCloseClick = "Changting.LocalMusic.Download.N.OpenCloseClick";

        /**
         * 操作事件类型
         */
        public static final String MusicOperate = "Changting.LocalMusic.Download.N.MusicOperate";

        /**
         * 音频播放模式
         */
        public static final String PlayMode = "Changting.LocalMusic.Download.N.PlayMode";
    }

    /**
     * 键值内容的 Field
     */
    public static class Field {
        /**
         * 操作类型
         */
        public static final String OperType = "OperType";

        /**
         * 操作方式
         */
        public static final String OperStyle = "OperStyle";

        /**
         * 操作事件类型
         */
        public static final String PlayOperType = "PlayOperType";

        /**
         * 歌曲名称
         */
        public static final String ProgramName = "ProgramName";

        /**
         * 歌手
         */
        public static final String Author = "Author";

        /**
         * 专辑名
         */
        public static final String Album = "Album";

        /**
         * 音频播放模式
         */
        public static final String PlayMode = "PlayMode";

        /**
         * 文件夹名称
         */
        public static final String DocName = "DocName";
    }

    /**
     * 键值 Field 具体内容
     */
    public static class OperTypeValue {

        /**
         * 打开
         */
        public static final String Open = "0";

        /**
         * 关闭
         */
        public static final String Close = "1";
    }

    /**
     * 键值 Field 具体内容
     */
    public static class OperStyleValue {

        /**
         * 点击
         */
        public static final String Click = "0";

        /**
         * 语音
         */
        public static final String VR = "1";
    }

    /**
     * 键值 Field 具体内容
     */
    public static class PlayOperTypeValue {

        /**
         * 播放
         */
        public static final String Play = "1";

        /**
         * 暂停
         */
        public static final String Pause = "2";

        /**
         * 上一个
         */
        public static final String Pre = "3";

        /**
         * 下一个
         */
        public static final String Next = "4";

        /**
         * 快进
         */
        public static final String SeekForward = "5";

        /**
         * 快退
         */
        public static final String SeekBackward = "6";

        /**
         * 删除
         */
        public static final String Delete = "7";

        /**
         * 下载
         */
        public static final String Download = "7";
    }

    /**
     * 音频播放模式
     */
    public static class PlayModeValue {

        /**
         * 顺序播放
         */
        public static final String Cycle = "1";

        /**
         * 单曲循环
         */
        public static final String Single = "2";

        /**
         * 随机
         */
        public static final String Random = "3";
    }
}
