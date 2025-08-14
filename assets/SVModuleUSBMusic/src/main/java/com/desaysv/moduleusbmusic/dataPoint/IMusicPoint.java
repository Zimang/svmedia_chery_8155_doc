package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc 音乐埋点接口
 * @time 2023-4-12 20:12
 */
public interface IMusicPoint {

    /**
     * 提交数据
     *
     * @param keyName keyName
     * @param content content
     */
    void upload(String keyName, String content);

    /**
     * 提交数据
     *
     * @param keyName keyName
     * @param content content
     * @param app_id  app_id
     * @param source  source
     */
    void upload(String keyName, String content, String app_id, String source);
}
