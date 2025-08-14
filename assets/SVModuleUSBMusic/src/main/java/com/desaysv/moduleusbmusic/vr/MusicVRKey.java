package com.desaysv.moduleusbmusic.vr;

/**
 * @author uidq1846
 * @desc 媒体音乐的Key值
 * @time 2023-2-7 15:09
 */
public class MusicVRKey {

    /**
     * 指定歌名、专辑等歌曲播放
     */
    public static final String KEY_PLAY_SPECIFIC_MUSIC = "PlaySpecificMusic";

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
    public static final String KEY_CONTROL_PLAYLIST = "controlPlayList";

    /**
     * 播放指定风格的歌曲
     */
    public static final String KEY_PLAY_MUSIC_GENRE = "playMusicGenre";

    /**
     * 搜索并播放
     */
    public static final String KEY_SEARCH_MUSIC = "searchMusic";

    /**
     * 查询播放模式
     */
    public static final String KEY_IS_PLAY_MODE_SUPPORTED = "isPlayModeSupported";

    /**
     * 查询播放状态
     */
    public static final String KEY_GET_PLAY_STATUS = "getPlayStatus";

    /**
     * 播放控制
     */
    public static final String KEY_CONTROL_PLAY_STATE = "controlPlayState";

    /**
     * 打开应用状态
     */
    public static final String KEY_SKIP_MUSIC_APP = "skipMusicApp";


    /**
     * =============================================================================================================================
     * 分割线，下边是反馈状态给到
     * <p>
     * 上传音乐状态（讯飞平台）
     */
    public static final String VR_MUSIC_STATUS_RESPONSE = "notifyMusicStatus";

    /**
     * 上传音乐信息（讯飞平台）
     */
    public static final String VR_MUSIC_INFO_RESPONSE = "notifyPlayMusicInfo";

    /**
     * 上传媒体（音乐）列表
     */
    public static final String VR_MUSIC_LIST_RESPONSE = "notifyMusicList";

    /**
     * 上传播放状态（讯飞平台）
     */
    public static final String VR_MUSIC_PLAY_RESPONSE = "notifyPlayStatus";

    /**
     * 上传A2DP状态（讯飞平台）
     */
    public static final String VR_MUSIC_A2DP_RESPONSE = "notifyA2dpStatus";

    /**
     * 上传USB链接状态（讯飞平台）
     */
    public static final String VR_MUSIC_USB_RESPONSE = "notifyUsbStatus";

    /**
     * 上传source状态（讯飞平台）
     */
    public static final String VR_MUSIC_SOURCE_RESPONSE = "notifySourceStatus";
}
