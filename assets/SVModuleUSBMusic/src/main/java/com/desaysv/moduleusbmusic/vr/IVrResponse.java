package com.desaysv.moduleusbmusic.vr;

import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * @author uidq1846
 * @desc 语音响应器，返回给VR的数据
 * @time 2023-1-17 11:36
 */
public interface IVrResponse {

    /**
     * 上传媒体信息给到讯飞
     *
     * @param fileMessage fileMessage
     */
    void uploadInfo(FileMessage fileMessage);

    /**
     * 上传媒体播放状态给到讯飞
     *
     * @param source    source
     * @param isPlaying isPlaying
     */
    void uploadPlayState(String source, boolean isPlaying);

    /**
     * 上传前后台给到讯飞
     */
    void uploadActiveStatus(String source, boolean isActive);
}
