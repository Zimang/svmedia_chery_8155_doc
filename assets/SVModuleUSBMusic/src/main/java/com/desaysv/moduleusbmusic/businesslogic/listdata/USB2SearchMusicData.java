package com.desaysv.moduleusbmusic.businesslogic.listdata;

import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.moduleusbmusic.utils.MusicTool;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB2MusicDataSubject;

import java.util.List;


/**
 * Created by LZM on 2020-7-27
 * Comment 用来进行USB数据扫描的类
 */
public class USB2SearchMusicData extends BaseSearchMusicData {

    private static final String TAG = "USB2SearchMusicData";

    private static USB2SearchMusicData instance;

    public static USB2SearchMusicData getInstance() {
        if (instance == null) {
            synchronized (USB2SearchMusicData.class) {
                if (instance == null) {
                    instance = new USB2SearchMusicData();
                }
            }
        }
        return instance;
    }

    /**
     * 获取扫描的路径
     *
     * @return FILE_PATH_USB_A； FILE_PATH_USB_B；
     */
    @Override
    protected String getScanPath() {
        return USBConstants.USBPath.USB1_PATH;
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
        USBMusicDate.getInstance().addAllUSB2MusicAllList(musicList);
        if (musicList.size() > 0) {
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB2MusicDataSubject.getInstance().setUSB2MusicSearchType(SearchType.SEARCHED_HAVE_DATA);
                //暂时全部扫描完成再这里进行文件夹数据才分
                USBMusicDate.getInstance().addAllUSB2MusicAllMap(MusicTool.getMusicAllMapFromFileMessages(musicList));
            } else {
                USB2MusicDataSubject.getInstance().setUSB2MusicSearchType(SearchType.SEARCHING_HAVE_DATA);
            }
        } else {
            //add by lzm 避免扫描过程中先出现nodata，在出现扫描完成的现象
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB2MusicDataSubject.getInstance().setUSB2MusicSearchType(SearchType.NO_DATA);
            }
        }
    }

    /**
     * 获取持久化保存的媒体播放类
     *
     * @return 媒体播放类
     */
    @Override
    protected FileMessage getSaveFileMessage() {
        return MediaPlayStatusSaveUtils.getInstance().getMediaFileMessage(MediaType.USB2_MUSIC);
    }

}
