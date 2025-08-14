package com.desaysv.moduleusbpicture.businesslogic.listsearch;

import android.content.ContentResolver;
import android.provider.MediaStore;
import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.datebase.DbUtils;
import com.desaysv.usbbaselib.datebase.MediaDatabaseKey;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1PictureDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2PictureDataSubject;

import java.util.List;


/**
 *
 * @author uidp5370
 * @date 2019-6-3
 * 搜索相应的数据库的对外方法类
 */

public class SearchPictureData {

    private final ContentResolver mContentResolver;
    private final static String TAG = "GetMediaData";

    public SearchPictureData() {
        mContentResolver = AppBase.mContext.getContentResolver();
    }

    private static SearchPictureData instance;

    public static SearchPictureData getInstance() {
        if (instance == null) {
            synchronized (SearchPictureData.class) {
                if (instance == null) {
                    instance = new SearchPictureData();
                }
            }
        }
        return instance;
    }


    /**
     * 获取USB1的图片类别
     *
     * @return musicList USB1的图片列表
     */
    public List<FileMessage> getUSB1PictureData(int scanStatus) {
        List<FileMessage> musicList;

        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID){
            musicList = getPictureDataAndroid(USBConstants.USBPath.USB0_PATH);
        }else {
            musicList = getPictureData(USBConstants.USBPath.USB0_PATH, null);
        }
        Log.d(TAG, "getUSB1PictureData: pictureList = " + musicList.size() + " scanStatus = " + scanStatus);
        USBPictureData.getInstance().addAllUSB1PictureAllList(musicList);
        if (musicList.size() > 0) {
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
            } else {
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHING_HAVE_DATA);
            }
        } else {
            //add by lzm 避免扫描过程中先出现nodata，在出现扫描完成的现象
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.NO_DATA);
            }
        }
        return musicList;
    }

    /**
     * 获取USB2的图片类别
     *
     * @return musicList USB2的图片列表
     */
    public List<FileMessage> getUSB2PictureData(int scanStatus) {
        List<FileMessage> musicList;

        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID){
            musicList = getPictureDataAndroid(USBConstants.USBPath.USB1_PATH);
        }else {
            musicList = getPictureData(USBConstants.USBPath.USB1_PATH, null);
        }
        Log.d(TAG, "getUSB2PictureData: pictureList = " + musicList.size() + " scanStatus = " + scanStatus);
        USBPictureData.getInstance().addAllUSB2PictureAllList(musicList);
        if (musicList.size() > 0) {
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
            } else {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHING_HAVE_DATA);
            }
        } else {
            //add by lzm 避免扫描过程中先出现nodata，在出现扫描完成的现象
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.NO_DATA);
            }
        }
        return musicList;
    }

    /**
     * 根据条件查询相应的图片数据
     *
     * @param usbPath   USB的路径
     * @param sortOrder 限制条件
     * @return 数据列表
     */
    public List<FileMessage> getPictureData(String usbPath, String sortOrder) {
        String selection;
        String[] args;
        selection = MediaDatabaseKey.ROOT_PATH + "=?";
        args = new String[]{usbPath};
        return DbUtils.getPictureDataBaseData(mContentResolver,  USBConstants.ProviderUrl.PIC_DATA_URL, sortOrder, selection, args);
    }

    /**
     * 根据条件查询视频的数据
     *
     * @return 数据列表
     */
    private List<FileMessage> getPictureDataAndroid(String usbPath) {
        String sortOrder = MediaStore.Video.VideoColumns.DISPLAY_NAME + " ASC";
        String selection;
        //这里得过滤逻辑是过滤加载完ID3信息得消息
        selection = MediaStore.Video.VideoColumns.DATA + " like ?";
        String[] args = new String[]{"%" + usbPath + "%"};
        return DbUtils.getPictureDataBaseDataAndroid(mContentResolver, sortOrder, selection, args);
    }

}
