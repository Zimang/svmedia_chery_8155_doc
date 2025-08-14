package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc 本地音乐埋点方法块
 * @time 2023-4-12 20:19
 */
public interface ILocalMusicPoint {

    /**
     * 打开本地音乐
     */
    void open(ContentData... content);

    /**
     * 关闭本地音乐
     */
    void close(ContentData... content);

    /**
     * 播放动作
     *
     * @param content content
     */
    void play(ContentData... content);

    /**
     * 停止
     *
     * @param content content
     */
    void pause(ContentData... content);

    /**
     * 上一曲
     *
     * @param content content
     */
    void pre(ContentData... content);

    /**
     * 下一曲
     *
     * @param content content
     */
    void next(ContentData... content);

    /**
     * 快进
     *
     * @param content content
     */
    void seekForward(ContentData... content);

    /**
     * 快退
     *
     * @param content content
     */
    void seekBackward(ContentData... content);

    /**
     * 删除
     *
     * @param content content
     */
    void delete(ContentData... content);

    /**
     * 随机播放
     *
     * @param content content
     */
    void randomMode(ContentData... content);

    /**
     * 单曲播放
     *
     * @param content content
     */
    void singleMode(ContentData... content);

    /**
     * 循环播放
     *
     * @param content content
     */
    void cycleMode(ContentData... content);
}
