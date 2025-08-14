package com.desaysv.moduleusbmusic.listener;

/**
 * @author uidq1846
 * @desc 文件处理接口
 * @time 2022-12-24 14:08
 */
public interface IFileControl {

    /**
     * 初始化
     */
    void init();

    /**
     * 获取文件复制工具
     *
     * @return ICopy
     */
    ICopy getCopyControl();

    /**
     * 获取删除文件控制器
     *
     * @return IDelete
     */
    IDelete getDeleteControl();
}
