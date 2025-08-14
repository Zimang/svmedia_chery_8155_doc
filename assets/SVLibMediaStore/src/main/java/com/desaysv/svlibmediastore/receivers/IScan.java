package com.desaysv.svlibmediastore.receivers;

import android.content.Context;

/**
 * @author uidq1846
 * @desc 媒体扫描的相关接口
 * @time 2022-11-14 16:03
 */
public interface IScan {

    /**
     * 初始化状态接口
     *
     * @param context context
     */
    void init(Context context);

    /**
     * 数据变化之后发起扫描
     * 例如删除了曲目
     */
    void scan(String path);

    /**
     * 数据变化之后发起扫描
     * 例如删除了曲目
     * 批量删除
     */
    void scan(String[] paths);
}
