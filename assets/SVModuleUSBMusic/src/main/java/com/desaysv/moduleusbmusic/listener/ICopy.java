package com.desaysv.moduleusbmusic.listener;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 下载管控接口
 * @time 2022-12-23 17:39
 */
public interface ICopy {

    /**
     * 拷贝单个文件
     *
     * @param oldItem           FileMessage 需要拷贝的文件条目
     * @param newFileFolderPath newFileFolderPath 需要拷贝到的文件目录
     */
    void copyFile(FileMessage oldItem, String newFileFolderPath);

    /**
     * 拷贝媒体列表
     *
     * @param fileMessages     fileMessages
     * @param targetFolderPath targetFolderPath 拷贝到的目录
     */
    void copyFiles(List<FileMessage> fileMessages, String targetFolderPath);

    /**
     * 文件是否已经拷贝
     *
     * @return T 已经拷贝 F 未拷贝
     */
    boolean isCopied(FileMessage fileMessage);

    /**
     * 是否在拷贝当中
     *
     * @return T 当前文件正在拷贝 F当前文件未处于拷贝状态
     */
    boolean isCopying();

    /**
     * 查看空间是否还充足能够存放FileMessage条目
     *
     * @param oldItem oldItem
     * @return boolean
     */
    boolean isStorageAvailable(FileMessage oldItem);

    /**
     * 注册复制进度监听
     *
     * @param TAG      TAG
     * @param listener listener
     */
    void registerCopyProgressListener(String TAG, ProgressListener listener);

    /**
     * 取消复制注册
     *
     * @param TAG TAG
     */
    void unRegisterCopyProgressListener(String TAG);
}
