package com.desaysv.moduleusbmusic.dataPoint;

import android.content.Context;

/**
 * @author uidq1846
 * @desc 用于提供埋点的接口
 * @time 2023-4-12 14:09
 */
public interface IDataPoint {

    /**
     * 初始化
     *
     * @param context context
     */
    void init(Context context);

    /**
     * 通知更新埋点
     *
     * @param data data
     */
    void uploadData(UploadData data);
}
