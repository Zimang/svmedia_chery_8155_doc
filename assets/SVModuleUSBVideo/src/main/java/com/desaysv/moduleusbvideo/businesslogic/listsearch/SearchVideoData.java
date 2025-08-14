package com.desaysv.moduleusbvideo.businesslogic.listsearch;

import android.content.ContentResolver;
import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbvideo.util.CharacterParser;
import com.desaysv.svlibmediastore.query.VideoQuery;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.datebase.DbUtils;
import com.desaysv.usbbaselib.datebase.MediaDatabaseKey;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1VideoDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2VideoDataSubject;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Created by uidp5370 on 2019-6-3.
 * 搜索相应的数据库的对外方法类
 */
public class SearchVideoData {
    private final ContentResolver mContentResolver;
    private final static String TAG = "SearchVideoData";

    public SearchVideoData() {
        mContentResolver = AppBase.mContext.getContentResolver();
    }

    private static final class InstanceHolder {
        static final SearchVideoData instance = new SearchVideoData();
    }

    public static SearchVideoData getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 搜索USB1的视频列表
     *
     * @return videoList USB1的视频列表
     */
    public List<FileMessage> getUSB1VideoData(int scanStatus) {
        Log.d(TAG, "getUSB1VideoData: start");
        List<FileMessage> videoList;
        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID) {
            videoList = VideoQuery.getInstance().queryMediaList(USBConstants.USBPath.USB0_PATH);
        } else {
            videoList = getVideoData(USBConstants.USBPath.USB0_PATH);
        }
        Collections.sort(videoList, compareTo);
        USBVideoFolderListData.getInstance().addVideoListInUSB1Map(videoList);
        Log.d(TAG, "getUSB1VideoData: videoList = " + videoList.size() + " scanStatus = " + scanStatus);
        USBVideoDate.getInstance().addAllUSB1VideoAllList(videoList);
        if (videoList.size() > 0) {
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.SEARCHED_HAVE_DATA);
            } else {
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.SEARCHING_HAVE_DATA);
            }
        } else {
            //add by lzm 避免扫描过程中先出现nodata，在出现扫描完成的现象
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.NO_DATA);
            } else {
                USB1VideoDataSubject.getInstance().setUSB1VideoSearchType(SearchType.SEARCHING);
            }
        }
        Log.d(TAG, "getUSB1VideoData: end");
        return videoList;
    }

    /**
     * 搜索USB2的视频列表
     *
     * @return videoList USB2的视频列表
     */
    public List<FileMessage> getUSB2VideoData(int scanStatus) {
        Log.d(TAG, "getUSB2VideoData: start");
        List<FileMessage> videoList;
        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID) {
            videoList = VideoQuery.getInstance().queryMediaList(USBConstants.USBPath.USB1_PATH);
        } else {
            videoList = getVideoData(USBConstants.USBPath.USB1_PATH);
        }
        Collections.sort(videoList, compareTo);
        USBVideoFolderListData.getInstance().addVideoListInUSB2Map(videoList);

        Log.d(TAG, "getUSB2VideoData: videoList = " + videoList.size() + " scanStatus = " + scanStatus);
        USBVideoDate.getInstance().addAllUSB2VideoAllList(videoList);
        if (videoList.size() > 0) {
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.SEARCHED_HAVE_DATA);
            } else {
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.SEARCHING_HAVE_DATA);
            }
        } else {
            //add by lzm 避免扫描过程中先出现nodata，在出现扫描完成的现象
            if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED) {
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.NO_DATA);
            } else {
                USB2VideoDataSubject.getInstance().setUSB2VideoSearchType(SearchType.SEARCHING);
            }
        }
        Log.d(TAG, "getUSB2VideoData: end");
        return videoList;
    }


    /**
     * 根据条件查询相应的数据
     *
     * @param usbPath USB的相应路径
     * @return 数据列表
     */
    public List<FileMessage> getVideoData(String usbPath) {
        String sortOrder = null;
        //这里得过滤逻辑是过滤加载完ID3信息得消息
        String selection = MediaDatabaseKey.ROOT_PATH + "=?" + " and " + MediaDatabaseKey.IS_HAS_ID3 + "=?";
        String[] args = new String[]{usbPath, "1"};
        return DbUtils.getVideoDataBaseData(mContentResolver, USBConstants.ProviderUrl.VIDEO_DATA_URL, sortOrder, selection, args);
    }

    /**
     * 排序
     */
    private final Comparator<Object> comparator = Collator.getInstance(Locale.CHINA);
    /**
     * 字母 -> 中文 -> 数字
     */
    private final Comparator<FileMessage> compareTo = new Comparator<FileMessage>() {
        @Override
        public int compare(FileMessage o1, FileMessage o2) {
            String t1 = CharacterParser.getPingYin(o1.getFileName());
            String t2 = CharacterParser.getPingYin(o2.getFileName());
            return comparator.compare(t1, t2);
        }
    };
}
