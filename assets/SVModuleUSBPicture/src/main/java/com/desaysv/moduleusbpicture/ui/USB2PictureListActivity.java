package com.desaysv.moduleusbpicture.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.moduleusbpicture.adapter.PictureListAdapter;
import com.desaysv.moduleusbpicture.businesslogic.listsearch.USBPictureData;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.statussubject.USB2PictureDataSubject;
import com.desaysv.usbbaselib.observer.Observer;

/**
 * Created by LZM on 2019-9-18
 * Comment USB2图片的列表界面
 */
public class USB2PictureListActivity extends BasePictureListActivity {

    private static final String TAG = "USB2PictureListActivity";

    public static void startUSB2PictureListActivity(Context context) {
        Intent intent = new Intent(context, USB2PictureListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    public void initViewListener() {
        super.initViewListener();
    }

    @Override
    protected PictureListAdapter initAdapter() {
        Log.d(TAG, "initAdapter: USB2AllPictureList size = " + USBPictureData.getInstance().getUSB2AllPictureList().size());
        return new PictureListAdapter(this, USBPictureData.getInstance().getUSB2AllPictureList(),
                USBConstants.USBPath.USB1_PATH);
    }

    @Override
    protected void attachObserver(Observer observer) {
        USB2PictureDataSubject.getInstance().attachObserver(TAG, observer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
    }

    /**
     * 设备状态变化的监听回调
     */
    private DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            if (DeviceConstants.DevicePath.USB1_PATH.equals(path) && !status) {
                if (!status) {
                    finish();
                }
            }
        }
    };


}
