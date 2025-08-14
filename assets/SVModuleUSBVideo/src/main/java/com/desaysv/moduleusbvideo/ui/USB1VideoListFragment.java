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
import com.desaysv.usbbaselib.statussubject.USB1VideoDataSubject;

import java.util.List;

/**
 * Create by extodc87 on 2022-11-2
 * Author: extodc87
 */
public class USB1VideoListFragment extends BaseVideoListFragment {
    private static final String TAG = "USB1VideoListFragment";

    public static USB1VideoListFragment newInstance() {
        return new USB1VideoListFragment();
    }

    @Override
    protected void attachObserver(Observer observer) {
        USB1VideoDataSubject.getInstance().attachObserver(TAG, observer);
    }

    @Override
    protected void detachObserver(Observer observer) {
        USB1VideoDataSubject.getInstance().detachObserver(TAG);
    }

    @Override
    protected SearchType getSearchType() {
        return USB1VideoDataSubject.getInstance().getUSB1VideoSearchType();
    }

    @Override
    protected List<FolderBean> getFolderBeanList(String path) {
        return USBVideoFolderListData.getInstance().getUSB1VideoFolderList(path);
    }

    @Override
    protected String getRootPath() {
        return DeviceConstants.DevicePath.USB0_PATH;
    }

    @Override
    protected List<FileMessage> getAllVideoList() {
        return USBVideoDate.getInstance().getUSB1VideoAllList();
    }

    @Override
    protected VideoFileListTypeTool getVideoFileListTypeUtils() {
        return VideoFileListTypeTool.getInstance(getRootPath());
    }

    @Override
    protected void startActivity(int styleType, FileMessage fileMessage) {
        USB1VideoPlayActivity.startUSB1VideoPlayActivity(getContext(), fileMessage.getPath());
    }

    @Override
    protected MediaType getMediaType() {
        return MediaType.USB1_VIDEO;
    }
}
