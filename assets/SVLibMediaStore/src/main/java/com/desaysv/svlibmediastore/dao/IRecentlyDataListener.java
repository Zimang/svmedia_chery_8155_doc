package com.desaysv.svlibmediastore.dao;

import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * @author uidq1846
 * @desc 最近列表数据监听
 * @time 2022-12-1 13:45
 */
public interface IRecentlyDataListener {

    /**
     * 数据库初始化成功
     */
    void onDataBaseInit();

    /**
     * 更新数据
     *
     * @param fileMessage fileMessage
     */
    void onUpdate(FileMessage... fileMessage);

    /**
     * 删除了哪些数据
     *
     * @param fileMessage fileMessage
     */
    void onDelete(FileMessage... fileMessage);
}
