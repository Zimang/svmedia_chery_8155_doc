package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc USB音乐埋点方法
 * @time 2023-4-12 20:19
 */
public interface IUsbMusicPoint {
    /**
     * 打开USB
     */
    void open(ContentData... content);

    /**
     * 关闭USB
     */
    void close(ContentData... content);

    /**
     * 打开最近播放
     */
    void openRecently(ContentData... content);

    /**
     * 关闭最近播放
     */
    void closeRecently(ContentData... content);

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
     * 下载
     *
     * @param content content
     */
    void downloadFile(ContentData... content);

    /**
     * 收藏
     *
     * @param content content
     */
    void collect(ContentData... content);

    /**
     * 取消收藏
     *
     * @param content content
     */
    void cancelCollect(ContentData... content);

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

    /**
     * 下载音乐文件夹事件
     */
    void downLoadFolder(ContentData... content);

    /**
     * 接入USB
     */
    void insertUSB();

    /**
     * 移除USB
     */
    void removeUSB();
}
