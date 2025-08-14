package com.desaysv.moduleusbvideo.ui;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.moduleusbvideo.bean.FolderBean;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoDate;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoFolderListData;
import com.desaysv.moduleusbvideo.util.VideoFileListTypeTool;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB2VideoDataSubject;

import java.util.List;

/**
 * Create by extodc87 on 2022-11-2
 * Author: extodc87
 */
public class USB2VideoListFragment extends BaseVideoListFragment {
    private static final String TAG = "USB2VideoListFragment";

    public static USB2VideoListFragment newInstance() {
        return new USB2VideoListFragment();
    }

    @Override
    protected void attachObserver(Observer observer) {
        USB2VideoDataSubject.getInstance().attachObserver(TAG, observer);
    }

    @Override
    protected void detachObserver(Observer observer) {
        USB2VideoDataSubject.getInstance().detachObserver(TAG);
    }

    @Override
    protected SearchType getSearchType() {
        return USB2VideoDataSubject.getInstance().getUSB2VideoSearchType();
    }

    @Override
    protected List<FolderBean> getFolderBeanList(String path) {
        return USBVideoFolderListData.getInstance().getUSB2VideoFolderList(path);
    }

    @Override
    protected String getRootPath() {
        return DeviceConstants.DevicePath.USB1_PATH;
    }

    @Override
    protected List<FileMessage> getAllVideoList() {
        return USBVideoDate.getInstance().getUSB2VideoAllList();
    }

    @Override
    protected VideoFileListTypeTool getVideoFileListTypeUtils() {
        return VideoFileListTypeTool.getInstance(getRootPath());
    }

    @Override
    protected void startActivity(int styleType, FileMessage fileMessage) {
        USB2VideoPlayActivity.startUSB2VideoPlayActivity(getContext(), fileMessage.getPath());
    }

    @Override
    protected MediaType getMediaType() {
        return MediaType.USB2_VIDEO;
    }
}
