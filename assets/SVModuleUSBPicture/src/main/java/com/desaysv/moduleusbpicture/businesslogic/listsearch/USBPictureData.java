package com.desaysv.moduleusbpicture.businesslogic.listsearch;

import android.util.Log;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2019-7-4.
 * Comment USB图片列表数据
 */
public class USBPictureData {

    private static final String TAG = "USBPictureData";

    private static USBPictureData instance;

    public static USBPictureData getInstance() {
        if (instance == null) {
            synchronized (USBPictureData.class) {
                if (instance == null) {
                    instance = new USBPictureData();
                }
            }
        }
        return instance;
    }

    private List<FileMessage> mUSB1PictureAllList = new ArrayList<>();

    public void clearUSB1PictureAllList() {
        Log.d(TAG, "clearUSB1PictureAllList: ");
        mUSB1PictureAllList.clear();
    }

    public void addAllUSB1PictureAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addAllUSB1PictureAllList: fileMessages size = " + fileMessages.size());
        mUSB1PictureAllList.clear();
        mUSB1PictureAllList.addAll(fileMessages);
    }

    public List<FileMessage> getUSB1AllPictureList() {
        Log.d(TAG, "getUSB1AllPictureList: mUSB1AllPictureList size = " + mUSB1PictureAllList.size());
        return mUSB1PictureAllList;
    }


    private List<FileMessage> mUSB2PictureAllList = new ArrayList<>();

    public void clearUSB2PictureAllList() {
        Log.d(TAG, "clearUSB2PictureAllList: ");

    }

    public void addAllUSB2PictureAllList(List<FileMessage> fileMessages) {
        Log.d(TAG, "addAllUSB2PictureAllList: fileMessages size = " + fileMessages.size());
        mUSB2PictureAllList.clear();
        mUSB2PictureAllList.addAll(fileMessages);
    }

    public List<FileMessage> getUSB2AllPictureList() {
        Log.d(TAG, "getUSB2AllPictureList: mUSB2AllPictureList size = " + mUSB2PictureAllList.size());
        return mUSB2PictureAllList;
    }

}
