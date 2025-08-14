package com.desaysv.svliblyrics.lyrics;

import android.content.Context;

import java.util.List;

/**
 * @author uidq1846
 * @desc
 * @time 2020-12-17 14:56
 */
public interface ILyricsView {
    /**
     * 初始化画笔，颜色，字体大小等设置
     */
    void init(Context context);

    /**
     * 设置数据源
     */
    void setLyricsRows(List<LyricsRow> lyricsRow);

    /**
     * 指定时间
     */
    void seekTo(int progress, boolean fromUser);

    /***
     * 设置歌词文字的缩放比例
     */
    void setLyricsScale(float factor);

    /**
     * 重置
     */
    void reset();
}
