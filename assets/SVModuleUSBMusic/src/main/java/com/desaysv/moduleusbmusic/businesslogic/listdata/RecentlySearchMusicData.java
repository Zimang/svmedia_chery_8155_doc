package com.desaysv.moduleusbmusic.businesslogic.listdata;

import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.svlibmediastore.dao.RecentlyMusicDao;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;


/**
 * Created by LZ on 2020-7-27
 * Comment 用来进行本地存储媒体数据扫描的类
 */
public class RecentlySearchMusicData extends BaseSearchMusicData {

    private static final String TAG = "RecentlySearchMusicData";

    private static RecentlySearchMusicData instance;

    public static RecentlySearchMusicData getInstance() {
        if (instance == null) {
            synchronized (RecentlySearchMusicData.class) {
                if (instance == null) {
                    instance = new RecentlySearchMusicData();
                }
            }
        }
        return instance;
    }

    /**
     * 获取扫描的路径
     *
     * @return USBConstants.USBPath.LOCAL_PATH
     */
    @Override
    protected String getScanPath() {
        return "";
    }

    /**
     * 将列表数据保存起来，并且触发数据状态的变化回调
     *
     * @param scanStatus USB数据的扫描状态
     * @param musicList  需要存储的音乐列表
     */
    @Override
    protected void addUSBMusicList(int scanStatus, List<FileMessage> musicList) {
        Log.d(TAG, "addUSBMusicList: scanStatus = " + scanStatus);
        USBMusicDate.getInstance().addRecentMusicAllList(musicList);
    }

    /**
     * 获取持久化保存的媒体播放类
     *
     * @return 媒体播放类
     */
    @Override
    protected FileMessage getSaveFileMessage() {
        return MediaPlayStatusSaveUtils.getInstance().getMediaFileMessage(MediaType.RECENT_MUSIC);
    }

    @Override
    public void getUSBMusicData(int scanStatus) {
        List<FileMessage> musicList = RecentlyMusicDao.getInstance().queryAll();
        Log.d(TAG, "getUSBMusicData: musicList = " + musicList.size() + " scanStatus = " + scanStatus);
        //将USB的列表数据，完全添加进去，并触发回调
        addUSBMusicList(scanStatus, musicList);
    }
}
