package com.desaysv.moduleusbvideo.businesslogic.listsearch;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.lang.ref.WeakReference;

/**
 * Created by LZM on 2019-7-4.
 * Comment USB视频列表扫描类，主要是扫描provider的数据库，应用启动时调用
 */
public class USBVideoListSearchTrigger {
    private static final String TAG = "USBVideoListSearchTrigger";

    private Context mContext;

    private static final class InstanceHolder {
        @SuppressLint("StaticFieldLeak")
        static final USBVideoListSearchTrigger instance = new USBVideoListSearchTrigger();
    }

    public static USBVideoListSearchTrigger getInstance() {
        return InstanceHolder.instance;
    }

    private USBVideoListSearchTrigger() {

    }

    /**
     * 初始化，在应用启动的时候调用
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        mContext = context;
        myHandler = new MyHandler(this);
        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID) {
            initVideoDataUpdateReceiverWithAndroid();
        } else {
            initVideoDataUpdateReceiver();
        }

        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
        // TODO 当前预留问题，USB视频未启动的状态，U盘第一次插入，U盘插入后，点击视频应用，界面提示，没有数据。
        // 解决方案、加入静态U盘插拔广播，让系统，上电时，初始化应用
        getUSB1VideoData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
        getUSB2VideoData(USBConstants.ProviderScanStatus.SCAN_FINISHED);
    }

    /**
     * 初始化Provider数据扫描状态变化的广播监听器
     */
    private void initVideoDataUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(USBConstants.BroadcastAction.VIDEO_REFRESH_DATA);
        intentFilter.addAction(USBConstants.BroadcastAction.VIDEO_ID3_REFRESH_DATA);
        mContext.registerReceiver(USBVideoDataUpdateReceiver, intentFilter);
    }

    /**
     * Provider数据状态变化广播的接收器，接收到数据状态变化，之后就去扫描数据库里面的数据
     */
    private final BroadcastReceiver USBVideoDataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String path = intent.getStringExtra(USBConstants.USBBroadcastKey.USB_PATH);
            int scanStatus = intent.getIntExtra(USBConstants.USBBroadcastKey.USB_EXTRA_SCAN_STATUS, USBConstants.ProviderScanStatus.SCANNING);
            Log.d(TAG, "onReceive: ");
            if (USBConstants.BroadcastAction.VIDEO_REFRESH_DATA.equals(action)) {
                Log.d(TAG, "onReceive: VIDEO_REFRESH_DATA path = " + path + " scanStatus = " + scanStatus);
                //这里是数据扫描刷新的逻辑，可能没有ID3信息，不过会快很多，可以根据需求选用
            } else if (USBConstants.BroadcastAction.VIDEO_ID3_REFRESH_DATA.equals(action)) {
                Log.d(TAG, "onReceive: VIDEO_ID3_REFRESH_DATA path = " + path + " scanStatus = " + scanStatus);
                switch (path) {
                    case USBConstants.USBPath.USB0_PATH:
                        getUSB1VideoData(scanStatus);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        getUSB2VideoData(scanStatus);
                        break;
                }
            }
        }
    };


    /**
     * 设备状态发生改变的时候会触发的监听
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            switch (path) {
                case DeviceConstants.DevicePath.USB0_PATH:
                    if (!status) {
                        USBVideoDate.getInstance().clearUSB1VideoAllList();
                        USBVideoFolderListData.getInstance().clearUSB1VideoFolderMap();
                    }
                    break;
                case DeviceConstants.DevicePath.USB1_PATH:
                    if (!status) {
                        USBVideoDate.getInstance().clearUSB2VideoAllList();
                        USBVideoFolderListData.getInstance().clearUSB2VideoFolderMap();
                    }
                    break;
            }

        }
    };

    /**
     * 获取USB1的视频文件，开启一个线程池去读取
     *
     * @param scanStatus provider传输过来的扫描状态
     */
    private void getUSB1VideoData(final int scanStatus) {
        Log.d(TAG, "getUSB1VideoData: scanStatus = " + scanStatus);
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                SearchVideoData.getInstance().getUSB1VideoData(scanStatus);
            }
        });
    }


    /**
     * 获取USB2的视频文件，开启一个线程池去读取
     *
     * @param scanStatus provider传输过来的扫描状态
     */
    private void getUSB2VideoData(final int scanStatus) {
        Log.d(TAG, "getUSB2VideoData: scanStatus = " + scanStatus);
        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                SearchVideoData.getInstance().getUSB2VideoData(scanStatus);
            }
        });
    }

    /**
     * 启动一个定时器，进行分段加载
     */
    private static final int MSG_UPDATE_QUERY = 0;
    /**
     * 分段加载 ，间隔时间3s
     */
    private static final int MSG_UPDATE_QUERY_TIME = 3000;
    private MyHandler myHandler;

    private static class MyHandler extends Handler {
        private final WeakReference<USBVideoListSearchTrigger> weakReference;

        public MyHandler(USBVideoListSearchTrigger queryTrigger) {
            weakReference = new WeakReference<>(queryTrigger);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            USBVideoListSearchTrigger usbVideoListSearchTrigger = weakReference.get();
            if (msg.what == MSG_UPDATE_QUERY) {
                String path = (String) msg.obj;
                Log.d(TAG, "handleMessage: path = " + path + " status = " + msg.arg1);
                switch (path) {
                    case USBConstants.USBPath.USB0_PATH:
                        usbVideoListSearchTrigger.getUSB1VideoData(msg.arg1);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        usbVideoListSearchTrigger.getUSB2VideoData(msg.arg1);
                        break;
                }
                // 判断当前搜索中，需要定时执行 MSG_UPDATE_QUERY_TIME
                if (USBConstants.ProviderScanStatus.SCANNING == msg.arg1) {
                    usbVideoListSearchTrigger.myHandler.sendMessageDelayed(
                            usbVideoListSearchTrigger.myHandler.obtainMessage(MSG_UPDATE_QUERY, USBConstants.ProviderScanStatus.SCANNING, -1, path),
                            MSG_UPDATE_QUERY_TIME);
                }
                Log.d(TAG, "handleMessage: end");
            }
        }
    }

    /**
     * 初始化原生Android MediaProvider的数据扫描广播
     */
    private void initVideoDataUpdateReceiverWithAndroid() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        mContext.registerReceiver(USBVideoDataUpdateReceiverAndroid, intentFilter);
    }

    private final BroadcastReceiver USBVideoDataUpdateReceiverAndroid = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);
            String path = intent.getData().getPath();
            Log.d(TAG, "onReceive: path = " + path);
            if (path == null || !path.startsWith("/storage/usb")) {
                Log.d(TAG, "Not usb path, return");
                return;
            }
            myHandler.removeMessages(MSG_UPDATE_QUERY);
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                myHandler.sendMessage(myHandler.obtainMessage(MSG_UPDATE_QUERY, USBConstants.ProviderScanStatus.SCANNING, -1, path));
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                myHandler.sendMessage(myHandler.obtainMessage(MSG_UPDATE_QUERY, USBConstants.ProviderScanStatus.SCAN_FINISHED, -1, path));
            }

        }
    };

}
