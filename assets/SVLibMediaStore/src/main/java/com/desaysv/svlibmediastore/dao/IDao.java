package com.desaysv.svlibmediastore.dao;

import android.content.Context;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 媒体最近列表数据库Dao类
 * @time 2022-11-30 15:57
 */
public interface IDao {

    /**
     * 初始化控制工具
     *
     * @param context context
     */
    void init(Context context);

    /**
     * 更新或者插入数据
     *
     * @param mediaFiles FileMessage
     */
    void update(FileMessage... mediaFiles);

    /**
     * 删除数据
     *
     * @param mediaFiles FileMessage
     */
    void delete(FileMessage... mediaFiles);

    /**
     * 获取数据列表
     *
     * @return List<FileMessage>
     */
    List<FileMessage> queryAll();

    /**
     * 注册变化监听
     *
     * @param flag     flag
     * @param listener listener
     */
    void registerListener(String flag, IRecentlyDataListener listener);

    /**
     * 取消注册监听
     *
     * @param flag flag
     */
    void unRegisterListener(String flag);

    /**
     * 获取列表当中最新的一条数据所处的时间
     *
     * @return long
     */
    long getLatestModified();
}
