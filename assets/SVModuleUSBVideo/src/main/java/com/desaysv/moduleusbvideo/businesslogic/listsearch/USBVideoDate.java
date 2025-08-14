package com.desaysv.moduleusbvideo.businesslogic.listsearch;

import android.util.Log;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2019-7-4.
 * Comment USB视频列表数据
 * TODO：后面可以根据需求加入列表数据变化的回调以及数据监听
 */
public class USBVideoDate {
    private static final String TAG = "USBVideoDate";

    private static final class InstanceHolder {
        static final USBVideoDate instance = new USBVideoDate();
    }

    public static USBVideoDate getInstance() {
        return InstanceHolder.instance;
    }

    /************************************************** USB 1 *************************************************/

    private final List<FileMessage> mUSB1VideoAllList = new ArrayList<>();

    /**
     * 添加全部的USB1视频数据
     *
     * @param fileMessages 需要添加的USB文件列表
     */
    void addAllUSB1VideoAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addAllUSB1VideoAllList: fileMessages size = " + fileMessages.size());
        mUSB1VideoAllList.clear();
        mUSB1VideoAllList.addAll(fileMessages);
    }

    /**
     * 清楚全部的USB1视频数据
     */
    void clearUSB1VideoAllList() {
        Log.d(TAG, "clearUSB1VideoAllList: ");
        mUSB1VideoAllList.clear();
    }

    /**
     * 获取全部的USB1视频数据
     *
     * @return USB1的视频列表
     */
    public List<FileMessage> getUSB1VideoAllList() {
        Log.d(TAG, "getUSB1AllVideoList: mUSB1AllVideoList = " + mUSB1VideoAllList);
        return mUSB1VideoAllList;
    }


    /************************************************** USB 2 *************************************************/

    private final List<FileMessage> mUSB2VideoAllList = new ArrayList<>();

    /**
     * 添加全部的USB2视频列表
     *
     * @param fileMessages 需要添加的视频列表
     */
    void addAllUSB2VideoAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addAllUSB2VideoAllList: fileMessages size = " + fileMessages.size());
        mUSB2VideoAllList.clear();
        mUSB2VideoAllList.addAll(fileMessages);
    }

    /**
     * 清除全部的USB2视频列表
     */
    void clearUSB2VideoAllList() {
        Log.d(TAG, "clearUSB2VideoAllList: ");
        mUSB2VideoAllList.clear();
    }

    /**
     * 获取USB2的视频列表
     *
     * @return USB2的视频列表
     */
    public List<FileMessage> getUSB2VideoAllList() {
        Log.d(TAG, "getUSB2AllVideoList: mUSB2AllVideoList = " + mUSB2VideoAllList);
        return mUSB2VideoAllList;
    }

}
