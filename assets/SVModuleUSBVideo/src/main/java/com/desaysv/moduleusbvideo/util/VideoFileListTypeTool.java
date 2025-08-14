package com.desaysv.moduleusbvideo.util;

import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceConstants;

/**
 * 用于临时存储当前USB 下的 视频模式/文件夹模式
 * Create by extodc87 on 2022-11-3
 * Author: extodc87
 */
public class VideoFileListTypeTool {
    private static final String TAG = "VideoFileListTypeTool";
    private static volatile VideoFileListTypeTool videoFileListTypeUSB1;
    private static volatile VideoFileListTypeTool videoFileListTypeUSB2;

    //define var here
    public static final int STYLE_TYPE_ALL = 0;// All
    public static final int STYLE_TYPE_FOLDER = 1;// folder
    public int styleType = STYLE_TYPE_ALL;// default All

    private final String rootPath;

    /**
     * @param rootPath {@link DeviceConstants.DevicePath#USB0_PATH}
     *                 {@link DeviceConstants.DevicePath#USB1_PATH}
     * @return VideoFileListTypeTool
     */
    public static VideoFileListTypeTool getInstance(String rootPath) {
        if (DeviceConstants.DevicePath.USB0_PATH.equals(rootPath)) {
            if (videoFileListTypeUSB1 == null) {
                synchronized (VideoFileListTypeTool.class) {
                    if (videoFileListTypeUSB1 == null) {
                        videoFileListTypeUSB1 = new VideoFileListTypeTool(rootPath);
                    }
                }
            }
            return videoFileListTypeUSB1;
        } else if (DeviceConstants.DevicePath.USB1_PATH.equals(rootPath)) {
            if (videoFileListTypeUSB2 == null) {
                synchronized (VideoFileListTypeTool.class) {
                    if (videoFileListTypeUSB2 == null) {
                        videoFileListTypeUSB2 = new VideoFileListTypeTool(rootPath);
                    }
                }
            }
            return videoFileListTypeUSB2;
        }
        return null;
    }

    public VideoFileListTypeTool(String rootPath) {
        this.rootPath = rootPath;
    }

    public int getStyleType() {
        Log.d(TAG, "getStyleType: rootPath = " + rootPath + " styleType = " + styleType);
        return styleType;
    }

    public void setStyleType(int styleType) {
        this.styleType = styleType;
    }
}
