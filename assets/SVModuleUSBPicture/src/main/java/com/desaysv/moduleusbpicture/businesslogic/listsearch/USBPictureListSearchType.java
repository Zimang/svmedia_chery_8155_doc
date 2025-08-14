package com.desaysv.moduleusbpicture.businesslogic.listsearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.observer.Observer;

/**
 * @author LZM
 * @date 2019-7-4
 * Comment USB图片的搜索状态
 */
public class USBPictureListSearchType {

    private final String TAG = this.getClass().getSimpleName();

    private static USBPictureListSearchType instance;

    private Context mContext;

    public static USBPictureListSearchType getInstance() {
        if (instance == null) {
            synchronized (USBPictureListSearchType.class) {
                if (instance == null) {
                    instance = new USBPictureListSearchType();
                }
            }
        }
        return instance;
    }

    public void initialize(Context context) {
        mContext = context;
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);

        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID){
            initPictureDataUpdateReceiverWithAndroid();
        }else {
            initVideoDataUpdataReceiver();
        }

        getUSB1PictureData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        getUSB2PictureData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
    }

    private void initVideoDataUpdataReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        //由于图片是没有ID3信息得，所以只有一个广播
        intentFilter.addAction(USBConstants.BroadcastAction.PICTURE_REFRESH_DATA);
        mContext.registerReceiver(USBPictureDataUpdateReceiver, intentFilter);
    }

    private BroadcastReceiver USBPictureDataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (USBConstants.BroadcastAction.PICTURE_REFRESH_DATA.equals(action)) {
                String path = intent.getStringExtra(USBConstants.USBBroadcastKey.USB_PATH);
                int scanStatus = intent.getIntExtra(USBConstants.USBBroadcastKey.USB_EXTRA_SCAN_STATUS,
                        USBConstants.ProviderScanStatus.SCANNING);
                Log.d(TAG, "onReceive: action = " + action + " path = " + path + " scanStatus = " + scanStatus);
                switch (path) {
                    case USBConstants.USBPath.USB0_PATH:
                        getUSB1PictureData(scanStatus);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        getUSB2PictureData(scanStatus);
                        break;
                }
            }
        }
    };

    /**
     * 获取设备的列表
     */
    private DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            switch (path) {
                case DeviceConstants.DevicePath.USB0_PATH:
                    if (!status) {
                        USBPictureData.getInstance().clearUSB1PictureAllList();
                    }
                    break;
                case DeviceConstants.DevicePath.USB1_PATH:
                    if (!status) {
                        USBPictureData.getInstance().clearUSB2PictureAllList();
                    }
                    break;
            }
        }
    };


    private void getUSB1PictureData(final int scanStatus) {
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                SearchPictureData.getInstance().getUSB1PictureData(scanStatus);
            }
        });
    }

    private void getUSB2PictureData(final int scanStatus) {
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                SearchPictureData.getInstance().getUSB2PictureData(scanStatus);
            }
        });
    }


    /**
     * 初始化原生Android MediaProvider的数据扫描广播
     */
    private void initPictureDataUpdateReceiverWithAndroid() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        mContext.registerReceiver(USBPictureDataUpdateReceiverAndroid, intentFilter);
    }


    private BroadcastReceiver USBPictureDataUpdateReceiverAndroid = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = "+ action);
            String path = intent.getData().getPath();
            Log.d(TAG, "onReceive: path = "+ path);
            if (path != null && !path.startsWith("/storage/usb")){
                Log.d(TAG, "Not usb path, return");
                return;
            }
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                switch (path) {
                    case USBConstants.USBPath.USB0_PATH:
                        getUSB1PictureData(USBConstants.ProviderScanStatus.SCANNING);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        getUSB2PictureData(USBConstants.ProviderScanStatus.SCANNING);
                        break;
                }
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                switch (path) {
                    case USBConstants.USBPath.USB0_PATH:
                        getUSB1PictureData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        getUSB2PictureData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
                        break;
                }
            }
        }
    };

}
