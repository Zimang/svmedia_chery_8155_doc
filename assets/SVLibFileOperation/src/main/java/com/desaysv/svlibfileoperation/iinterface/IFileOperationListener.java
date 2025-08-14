package com.desaysv.svlibfileoperation.iinterface;

/**
 * created by ZNB on 2022-08-08
 * 文件操作处理的回调接口
 */
public interface IFileOperationListener {
    /**
     * 导出状态的回调
     * @param state 导出处理的状态
     * @param current 正在导出第几个
     * @param total 一共需要导出多少个
     */
    void onExportState(int state,int current, int total);

    /**
     * 删除状态的回调
     * @param state 删除处理的状态
     * @param current 正在删除第几个
     * @param total 一共需要删除多少个
     */
    void onDeleteState(int state,int current, int total);
}
