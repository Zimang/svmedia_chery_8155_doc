package com.desaysv.moduleusbmusic.businesslogic.listdata;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.moduleusbmusic.businesslogic.control.SourceResumeControl;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.listener.ResumeAction;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.utils.MusicStatusUtils;
import com.desaysv.svlibmediastore.dao.IRecentlyDataListener;
import com.desaysv.svlibmediastore.dao.RecentlyMusicDao;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1MusicDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2MusicDataSubject;

/**
 * Created by LZM on 2019-7-4.
 * Comment USB音乐的搜索
 */
public class USBMusicListSearchTrigger {

    private final String TAG = this.getClass().getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static USBMusicListSearchTrigger instance;

    private Context mContext;

    public static USBMusicListSearchTrigger getInstance() {
        if (instance == null) {
            synchronized (USBMusicListSearchTrigger.class) {
                if (instance == null) {
                    instance = new USBMusicListSearchTrigger();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化USB的设备状态回调，和应用启动的时候，进行一次数据扫描
     *
     * @param context 上下文，application的上下文
     */
    public void initialize(Context context) {
        mContext = context;
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
        initMusicDataUpdateReceiver();
        getUSB1MusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        getUSB2MusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        getLocalMusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        initRecentDataUpdateListener();
        getRecentMusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
    }

    /**
     * 初始化数据监听
     */
    private void initRecentDataUpdateListener() {
        RecentlyMusicDao.getInstance().registerListener(TAG, listener);
    }

    /**
     * 监听数据变化
     */
    private final IRecentlyDataListener listener = new IRecentlyDataListener() {
        @Override
        public void onDataBaseInit() {
            getRecentMusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        }

        @Override
        public void onUpdate(FileMessage... fileMessage) {
            getRecentMusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        }

        @Override
        public void onDelete(FileMessage... fileMessage) {
            getRecentMusicData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        }
    };

    /**
     * 初始化provider的数据扫描广播
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void initMusicDataUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        //音乐文件递归到的时候，就会触发的广播
        intentFilter.addAction(USBConstants.BroadcastAction.MUSIC_REFRESH_DATA);
        //音乐数据ID3更新的时候，会触发的广播
        intentFilter.addAction(USBConstants.BroadcastAction.MUSIC_ID3_REFRESH_DATA);
        mContext.registerReceiver(USBMusicDataUpdateReceiver, intentFilter);
    }

    /**
     * provider的数据扫描广播
     */
    private final BroadcastReceiver USBMusicDataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action = " + action);
            String path = intent.getStringExtra(USBConstants.USBBroadcastKey.USB_PATH);
            int scanStatus = intent.getIntExtra(USBConstants.USBBroadcastKey.USB_EXTRA_SCAN_STATUS,
                    USBConstants.ProviderScanStatus.SCANNING);
            if (USBConstants.BroadcastAction.MUSIC_REFRESH_DATA.equals(action)) {
                Log.i(TAG, "onReceive: MUSIC_REFRESH_DATA path = " + path + " scanStatus = " + scanStatus);
                //这里是数据扫描到就会发送更新的广播，ID3信息不是一定有得，不过扫描速度会快很多，
            } else if (USBConstants.BroadcastAction.MUSIC_ID3_REFRESH_DATA.equals(action)) {
                Log.i(TAG, "onReceive: MUSIC_ID3_REFRESH_DATA path = " + path + " scanStatus = " + scanStatus);
                switch (path) {
                    case USBConstants.USBPath.USB0_PATH:
                        getUSB1MusicData(scanStatus);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        getUSB2MusicData(scanStatus);
                        break;
                    case USBConstants.USBPath.LOCAL_PATH:
                        getLocalMusicData(scanStatus);
                        break;
                }
            }
        }
    };

    /**
     * 设备状态的变化回调
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            switch (path) {
                case DeviceConstants.DevicePath.USB0_PATH:
                    if (status) {
                        //ADD BY LZM 接入U盘之后，要把数据状态变为扫描状态，不然会出现数据状态和扫描状态不一致的问题
                        getUSB1MusicData(USBConstants.ProviderScanStatus.SCANNING);
                        USB1MusicDataSubject.getInstance().setUSB1MusicSearchType(SearchType.SEARCHING);
                        MusicStatusUtils.getInstance().notifyDeviceState(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, Constant.DeviceState.CONNECTED);
                        SourceResumeControl.getInstance().openSource(ResumeAction.USB0_MOUNTED);
                        UsbMusicPoint.getInstance().insertUSB();
                    } else {
                        //设备断开之后，清除列表，并且停止播放
                        USBMusicDate.getInstance().clearUSB1MusicAllList();
                        USBMusicDate.getInstance().clearUSB1MusicAllFolderMap();
                        ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.STOP, ChangeReasonData.USB1_EJECT);
                        releaseRecentRes(USBConstants.USBPath.USB0_PATH);
                        MusicStatusUtils.getInstance().notifyDeviceState(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, Constant.DeviceState.DISCONNECTED);
                        SourceResumeControl.getInstance().openSource(ResumeAction.USB0_UNMOUNTED);
                        UsbMusicPoint.getInstance().removeUSB();
                    }
                    break;
                case DeviceConstants.DevicePath.USB1_PATH:
                    if (status) {
                        getUSB2MusicData(USBConstants.ProviderScanStatus.SCANNING);
                        // ADD BY LZM 接入U盘之后，要把数据状态变为扫描状态，不然会出现数据状态和扫描状态不一致的问题
                        USB2MusicDataSubject.getInstance().setUSB2MusicSearchType(SearchType.SEARCHING);
                        MusicStatusUtils.getInstance().notifyDeviceState(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, Constant.DeviceState.CONNECTED);
                        SourceResumeControl.getInstance().openSource(ResumeAction.USB1_MOUNTED);
                    } else {
                        //设备断开之后，清除列表，并且停止播放
                        USBMusicDate.getInstance().clearUSB2MusicAllList();
                        USBMusicDate.getInstance().clearUSB2MusicAllFolderMap();
                        ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.STOP, ChangeReasonData.USB2_EJECT);
                        releaseRecentRes(USBConstants.USBPath.USB1_PATH);
                        MusicStatusUtils.getInstance().notifyDeviceState(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, Constant.DeviceState.DISCONNECTED);
                        SourceResumeControl.getInstance().openSource(ResumeAction.USB1_UNMOUNTED);
                    }
                    break;
            }
        }

        /**
         * 释放最近播放资源
         */
        private void releaseRecentRes(String usbPath) {
            String recentPlayPath = ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getStatusTool().getCurrentPlayItem().getPath();
            Log.i(TAG, "releaseRecentRes: recentPlayPath = " + recentPlayPath);
            //如果最近播放当前播放的是
            if (recentPlayPath.startsWith(usbPath)) {
                ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.STOP, ChangeReasonData.USB1_EJECT);
            }
        }
    };

    /**
     * 获取USB1的音乐列表
     *
     * @param scanStatus provider的扫描状态
     */
    private void getUSB1MusicData(final int scanStatus) {
        Log.i(TAG, "getUSB1MusicData: scanStatus = " + scanStatus);
        //启动一个进程，进行数据扫描
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                USB1SearchMusicData.getInstance().getUSBMusicData(scanStatus);
            }
        });
    }

    /**
     * 获取USB2的音乐列表
     *
     * @param scanStatus provider的扫描状态
     */
    private void getUSB2MusicData(final int scanStatus) {
        Log.d(TAG, "getUSB2MusicData: scanStatus = " + scanStatus);
        //启动一个进程，进行数据扫描
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                USB2SearchMusicData.getInstance().getUSBMusicData(scanStatus);

            }
        });
    }

    /**
     * 获取内部存储数据
     *
     * @param scanStatus provider的扫描状态
     */
    private void getLocalMusicData(final int scanStatus) {
        Log.d(TAG, "getLocalMusicData: scanStatus = " + scanStatus);
        //启动一个进程，进行数据扫描
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                LocalSearchMusicData.getInstance().getUSBMusicData(scanStatus);
            }
        });
    }

    /**
     * 获取最近播放列表数据
     *
     * @param scanStatus provider的扫描状态
     */
    private void getRecentMusicData(final int scanStatus) {
        Log.d(TAG, "getRecentMusicData: scanStatus = " + scanStatus);
        //启动一个进程，进行数据扫描
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                RecentlySearchMusicData.getInstance().getUSBMusicData(scanStatus);
            }
        });
    }
}
