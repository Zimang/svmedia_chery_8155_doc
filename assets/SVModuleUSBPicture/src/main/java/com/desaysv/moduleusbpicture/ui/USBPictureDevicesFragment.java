package com.desaysv.moduleusbpicture.ui;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduleusbpicture.R;

import java.lang.ref.WeakReference;


/**
 * Created by LZM on 2019-9-18
 * Comment USB图片的设备界面
 */
public class USBPictureDevicesFragment extends BaseFragment {

    private static final String TAG = "PictureDevicesFragment";

    private static USBPictureDevicesFragment instance;

    public static USBPictureDevicesFragment newInstance() {
        instance = new USBPictureDevicesFragment();
        return instance;
    }

    private static final int UPDATE_DEVICE_STATUS = 1;

    private MyHandler mHandler;

    private static class MyHandler extends Handler {

        private WeakReference<USBPictureDevicesFragment> weakReference;

        MyHandler(USBPictureDevicesFragment pictureDevicesFragment) {
            weakReference = new WeakReference<>(pictureDevicesFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final USBPictureDevicesFragment pictureDevicesFragment = weakReference.get();
            switch (msg.what) {
                case UPDATE_DEVICE_STATUS:
                    pictureDevicesFragment.updateDeviceStatus();
                    break;
            } 
        }
    }

    private Button btnUBS1;
    private Button btnUBS2;

    @Override
    public int getLayoutResID() {
        return R.layout.usb_picture_devices_fragment;
    }

    @Override
    public void initView(View view) {
        btnUBS1 = view.findViewById(R.id.btnUBS1);
        btnUBS2 = view.findViewById(R.id.btnUBS2);
    }

    @Override
    public void initData() {
        mHandler = new MyHandler(this);
        updateDeviceStatus();
    }

    @Override
    public void initViewListener() {
        btnUBS1.setOnClickListener(onClickListener);
        btnUBS2.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnUBS1) {
                Log.d(TAG, "onClick: btnUBS1");
                USB1PictureListActivity.startUSB1PictureListActivity(AppBase.mContext);
            } else if (v.getId() == R.id.btnUBS2) {
                Log.d(TAG, "onClick: btnUBS2");
                USB2PictureListActivity.startUSB2PictureListActivity(AppBase.mContext);
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
    }

    @Override
    public void onStop() {
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
            switch (path){
                case DeviceConstants.DevicePath.USB0_PATH:
                case DeviceConstants.DevicePath.USB1_PATH:
                    mHandler.sendEmptyMessage(UPDATE_DEVICE_STATUS);
                    break;
            }
        }
    };


    private void updateDeviceStatus() {
        if (DeviceStatusBean.getInstance().isUSB1Connect()) {
            btnUBS1.setVisibility(View.VISIBLE);
        } else {
            btnUBS1.setVisibility(View.GONE);
        }
        if (DeviceStatusBean.getInstance().isUSB2Connect()) {
            btnUBS2.setVisibility(View.VISIBLE);
        } else {
            btnUBS2.setVisibility(View.GONE);
        }
    }


}
