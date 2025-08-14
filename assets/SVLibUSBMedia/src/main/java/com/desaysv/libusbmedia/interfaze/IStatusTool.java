package com.desaysv.libusbmedia.interfaze;

import com.desaysv.svliblyrics.lyrics.LyricsRow;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;


import java.util.List;

/**
 * Created by uidp5370 on 2019-1-28.
 * 获取媒体的播放状态，对外提供
 */

public interface IStatusTool {

    /**
     * 获取当前的播放列表
     *
     * @return List<FileMessage>  PlayList
     */
    List<FileMessage> getPlayList();

    /**
     * 获取当前的播放状态
     *
     * @return true：播放中；false：暂停中
     */
    boolean isPlaying();

    /**
     * 获取当前播放的媒体item
     *
     * @return FileMessage CurrentPlayItem当前播放的文件信息
     */
    FileMessage getCurrentPlayItem();

    /**
     * 获取当前的循环模式
     *
     * @return String LoopType循环模式
     * String CYCLE = "CYCLE";
     * String SINGLE = "SINGLE";
     * String RANDOM = "RANDOM";
     */
    String getLoopType();

    /**
     * 获取当前文件的收藏状态
     *
     * @return true：收藏，false：非收藏
     */
    boolean getCollect();

    /**
     * 获取当前的专辑图片
     *
     * @return 专辑图片的数据
     */
    byte[] getAlbumPic();


    /**
     * 获取压缩过的专辑图片，可以通过AIDL对外提供
     *
     * @return 压缩过的专辑图片
     */
    byte[] getCompressionAlbumPic();


    /**
     * 获取当前播放列表的属性
     *
     * @return ALL,        //全部列表
     * ARTIST,     //艺术家列表
     * ALBUM,      //专辑列表
     * FLODER,     //文件夹列表
     * COLLECT     //收藏列表
     */
    CurrentPlayListType getCurrentPlayListType();

    /**
     * 获取当前播放歌曲的时长
     *
     * @return Duration
     */
    int getDuration();

    /**
     * 获取当前播放歌曲的播放时间
     *
     * @return CurrentPlayTime
     */
    int getCurrentPlayTime();

    /**
     * 获取当前播放歌曲在当前播放列表中的位置
     *
     * @return CurrentItemPosition
     */
    int getCurrentItemPosition();


    /**
     * 获取当前播放的歌词列表
     *
     * @return List<LyricsRow>
     */
    List<LyricsRow> getCurrentPlayLyrics();
}
