package com.desaysv.moduleusbmusic.listener;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 文件删除接口
 * @time 2022-12-24 14:07
 */
public interface IDelete {

    /**
     * 文件删除
     *
     * @param deleteItem FileMessage
     */
    void deleteFile(FileMessage deleteItem);

    /**
     * 文件列表删除
     *
     * @param deleteItems FileMessage
     */
    void deleteFile(List<FileMessage> deleteItems);

    /**
     * 当前文件是否在删除当中
     *
     * @return T 正在删除,删除完成
     */
    boolean isDeleting();

    /**
     * 注册删除监听
     *
     * @param TAG      TAG
     * @param listener listener
     */
    void registerDeleteProgressListener(String TAG, ProgressListener listener);

    /**
     * 取消注册
     *
     * @param TAG TAG
     */
    void unRegisterDeleteProgressListener(String TAG);
}
