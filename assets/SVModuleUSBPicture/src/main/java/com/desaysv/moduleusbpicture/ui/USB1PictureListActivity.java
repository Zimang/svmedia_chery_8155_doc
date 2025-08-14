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
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.statussubject.USB1PictureDataSubject;

/**
 * Created by LZM on 2019-9-18
 * Comment USB1图片的列表界面
 *
 * @author uidp5370
 */
public class USB1PictureListActivity extends BasePictureListActivity {

    private static final String TAG = "USB1PictureListActivity";

    public static void startUSB1PictureListActivity(Context context) {
        Intent intent = new Intent(context, USB1PictureListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    public void initViewListener() {
        super.initViewListener();
    }

    @Override
    protected PictureListAdapter initAdapter() {
        Log.d(TAG, "initAdapter: USB1AllPictureList size = " + USBPictureData.getInstance().getUSB1AllPictureList().size());
        return new PictureListAdapter(this, USBPictureData.getInstance().getUSB1AllPictureList(),
                USBConstants.USBPath.USB0_PATH);
    }

    @Override
    protected void attachObserver(Observer observer) {
        USB1PictureDataSubject.getInstance().attachObserver(TAG, observer);
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
            if (DeviceConstants.DevicePath.USB0_PATH.equals(path) && !status) {
                if (!status) {
                    finish();
                }
            }
        }
    };


}
