package com.desaysv.moduleusbmusic.businesslogic.listdata;

import android.os.Environment;
import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;


/**
 * Created by LZ on 2020-7-27
 * Comment 用来进行本地存储媒体数据扫描的类
 */
public class LocalSearchMusicData extends BaseSearchMusicData {

    private static final String TAG = "LocalSearchMusicData";

    private static LocalSearchMusicData instance;

    public static LocalSearchMusicData getInstance() {
        if (instance == null) {
            synchronized (LocalSearchMusicData.class) {
                if (instance == null) {
                    instance = new LocalSearchMusicData();
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
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
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
        USBMusicDate.getInstance().addLocalMusicAllList(musicList);
    }

    /**
     * 获取持久化保存的媒体播放类
     *
     * @return 媒体播放类
     */
    @Override
    protected FileMessage getSaveFileMessage() {
        return MediaPlayStatusSaveUtils.getInstance().getMediaFileMessage(MediaType.LOCAL_MUSIC);
    }
}
