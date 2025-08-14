package com.desaysv.moduleusbmusic.listener;

import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * @author uidq1846
 * @desc 进度回调监听
 * @time 2022-12-23 17:34
 */
public interface ProgressListener {

    /**
     * 当前进度变化回调
     *
     * @param progress progress
     * @param total    total
     */
    void onProgressChange(long progress, long total);

    /**
     * 事件完成成功,某个文件成功
     */
    void onSuccess(FileMessage fileMessage);

    /**
     * 事件失败
     */
    void onFailed(FileMessage fileMessage);

    /**
     * 当前容量限制了，需应用弹窗
     */
    void onSizeLimit(FileMessage fileMessage);

    /**
     * 所有进度完成
     */
    void onFinish();
}