package com.desaysv.usbpicture.trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.querypicture.QueryManager;
import com.desaysv.svlibpicturebean.manager.PictureListManager;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.statussubject.SearchType;
import com.desaysv.usbbaselib.statussubject.USB1PictureDataSubject;
import com.desaysv.usbbaselib.statussubject.USB2PictureDataSubject;
import com.desaysv.usbpicture.bean.MessageBean;
import com.desaysv.usbpicture.observer.USB1ScanObserver;
import com.desaysv.usbpicture.observer.USB2ScanObserver;



import java.lang.ref.WeakReference;

/**
 * USB图片的查询，专职处理设备挂载状态、数据库扫描状态等情况下的数据更新
 */
public class PictureQueryTrigger {
    private static final String TAG = "PictureQueryTrigger";
    private static PictureQueryTrigger mInstance;

    public static PictureQueryTrigger getInstance(){
        synchronized (PictureQueryTrigger.class){
            if (mInstance == null){
                mInstance = new PictureQueryTrigger();
            }
            return mInstance;
        }
    }

    public void initialize(Context context){
        myHandler = new MyHandler(this);
        QueryManager.getInstance().init(context);
        USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
        USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
        initDataUpdateReceiver(context);
    }

    private void initDataUpdateReceiver(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        context.registerReceiver(USBPictureDataUpdateReceiver, intentFilter);
    }

    /**
     * 设备状态变化的监听回调
     */
    private DeviceListener deviceListener = new DeviceListener() {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            switch (path){
                case DeviceConstants.DevicePath.USB0_PATH:
                    if(!status){
                        PictureListManager.getInstance().clearAllUSB1List();
                        USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.NO_DATA);
                    }else {
                        USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHING);
                        PictureListManager.getInstance().setUsb1ScanStatus(USBConstants.ProviderScanStatus.SCANNING);
                        notifyScanStatusChanged(USBConstants.ProviderScanStatus.SCANNING,path);
                    }
                    break;
                case DeviceConstants.DevicePath.USB1_PATH:
                    if(!status){
                        PictureListManager.getInstance().clearAllUSB2List();
                        USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.NO_DATA);
                    }else {
                        USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHING);
                        PictureListManager.getInstance().setUsb2ScanStatus(USBConstants.ProviderScanStatus.SCANNING);
                        notifyScanStatusChanged(USBConstants.ProviderScanStatus.SCANNING,path);
                    }
                    break;
            }
        }
    };



    private BroadcastReceiver USBPictureDataUpdateReceiver = new BroadcastReceiver() {
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
                notifyScanStatusChanged(USBConstants.ProviderScanStatus.SCANNING,path);
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                notifyScanStatusChanged(USBConstants.ProviderScanStatus.SCAN_FINISHED,path);
            }
        }
    };

    /**
     * 通知扫描状态发生了变化
     * @param scanStatus
     * @param path
     */
    private void notifyScanStatusChanged(int scanStatus, String path){
        if (scanStatus == USBConstants.ProviderScanStatus.SCANNING) {
            myHandler.setPath(path);
            myHandler.setScanStatus(scanStatus);
            myHandler.sendEmptyMessage(MSG_UPDATE_QUERY);
            if (path != null && path.contains(USBConstants.USBPath.USB0_PATH)){
                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHING);
            }else if (path != null && path.contains(USBConstants.USBPath.USB1_PATH)){
                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHING);
            }
        }else if (scanStatus == USBConstants.ProviderScanStatus.SCAN_FINISHED){
            myHandler.removeMessages(MSG_UPDATE_QUERY);
            handleQuery(path,scanStatus);
            if (path != null && path.contains(USBConstants.USBPath.USB0_PATH)){
//                USB1PictureDataSubject.getInstance().setUSB1PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
            }else if (path != null && path.contains(USBConstants.USBPath.USB1_PATH)){
//                USB2PictureDataSubject.getInstance().setUSB2PictureSearchType(SearchType.SEARCHED_HAVE_DATA);
            }
        }
    }

    /**
     * 查询操作
     * @param path
     */
    protected void handleQuery(String path, int scanStatus){
        Log.d(TAG,"handleQuery,scanStatus："+scanStatus);
        if (!checkUSBStatus(path)){
            myHandler.removeMessages(MSG_UPDATE_QUERY);
            return;
        }
        if (path != null && path.contains(USBConstants.USBPath.USB0_PATH)) {
            if (USB1ScanObserver.getInstance().getObserverSize() > 0){
                USB1ScanObserver.getInstance().setScanStatus(scanStatus);
            }else {
                QueryManager.getInstance().startQueryPictureWithUSB1Scan(scanStatus);
            }
        } else if (path != null && path.contains(USBConstants.USBPath.USB1_PATH)) {
            if (USB2ScanObserver.getInstance().getObserverSize() > 0){
                USB2ScanObserver.getInstance().setScanStatus(scanStatus);
            }else {
                QueryManager.getInstance().startQueryPictureWithUSB2Scan(scanStatus);
            }
        }
    }

    /**
     * 启动一个定时器，进行分段加载
     */
    private static final int MSG_UPDATE_QUERY = 0;
    private MyHandler myHandler;
    private static class MyHandler extends Handler {
        private WeakReference<PictureQueryTrigger> weakReference;

        public void setPath(String path) {
            this.path = path;
        }

        public void setScanStatus(int scanStatus) {
            this.scanStatus = scanStatus;
        }

        private int scanStatus;
        private String path;
        public MyHandler(PictureQueryTrigger queryTrigger) {
            weakReference = new WeakReference<>(queryTrigger);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_QUERY:
                    weakReference.get().handleQuery(path,scanStatus);
                    sendEmptyMessageDelayed(MSG_UPDATE_QUERY, 3000);
                    break;
            }
        }
    };

    /**
     * 判断
     * @param path
     * @return
     */
    protected boolean checkUSBStatus(String path){
        if (path != null && path.contains(USBConstants.USBPath.USB0_PATH)){
            return DeviceStatusBean.getInstance().isUSB1Connect();
        }else if (path != null && path.contains(USBConstants.USBPath.USB1_PATH)){
            return DeviceStatusBean.getInstance().isUSB2Connect();
        }
        return true;
    }
}
