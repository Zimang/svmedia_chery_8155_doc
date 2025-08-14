package com.desaysv.moduleusbvideo.businesslogic.control;

import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoDate;
import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.desaysv.moduleusbvideo.ui.USB2VideoPlayActivity;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB2VideoDataSubject;

import java.util.List;

/**
 * Created by LZM on 2020-4-17
 * Comment 启动USB2的视频
 *
 * @author uidp5370
 */
public class USB2VideoStartControl extends BaseVideoStartControl {

    private static final String TAG = "USB1VideoStartControl";

    private static final class InstanceHolder {
        static final USB2VideoStartControl instance = new USB2VideoStartControl();
    }

    public static USB2VideoStartControl getInstance() {
        return InstanceHolder.instance;
    }

    private USB2VideoStartControl() {

    }

    /**
     * 获取设备的状态信息
     *
     * @return 设备的状态
     */
    @Override
    protected boolean getDeviceStatus() {
        return DeviceStatusBean.getInstance().isUSB2Connect();
    }

    /**
     * 获取数据的扫描状态
     *
     * @return 数据的扫描状态
     */
    @Override
    protected SearchType getVideoSearchStatus() {
        return USB2VideoDataSubject.getInstance().getUSB2VideoSearchType();
    }

    /**
     * 获取音乐的控制器
     *
     * @return 音乐的控制器
     */
    @Override
    protected IControlTool getVideoControl() {
        return ModuleUSBVideoTrigger.getInstance().USB2VideoControlTool;
    }

    /**
     * 获取音乐的列表
     *
     * @return 音乐的列表
     */
    @Override
    protected List<FileMessage> getVideoList() {
        return USBVideoDate.getInstance().getUSB2VideoAllList();
    }

    /**
     * 启动相应的界面
     */
    @Override
    protected void startView() {
        USB2VideoPlayActivity.startUSB2VideoPlayActivity(AppBase.mContext, ModuleUSBVideoTrigger.getInstance().USB2VideoStatusTool.getCurrentPlayItem().getPath());
    }

    /**
     * 判断能否打开，需要根据数据状态，根据界面显示状态，根据当前的音源状态
     * 能够打开的前提条件，1. 当前是有数据的情况下
     * 由于启动的时候，会先做判断，只有在启动视频的时候，才调用这一个接口，所以，这里就不加判断逻辑了
     *
     * @return true：能够打开；false：不能打开
     */
    @Override
    protected boolean isEnableOpen() {
        SearchType videoSearchType = getVideoSearchStatus();
        Log.d(TAG, "isEnableOpen: videoSearchType = " + videoSearchType);
        return SearchType.SEARCHING_HAVE_DATA == videoSearchType || SearchType.SEARCHED_HAVE_DATA == videoSearchType;
    }
}
