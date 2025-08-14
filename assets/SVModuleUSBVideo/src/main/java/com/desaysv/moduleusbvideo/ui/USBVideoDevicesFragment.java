package com.desaysv.moduleusbvideo.ui;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.base.VideoBaseFragment;

import java.lang.ref.WeakReference;


/**
 * Created by LZM on 2019-9-18
 * Comment USB视频的设备界面
 *
 * @author uidp5370
 */
public class USBVideoDevicesFragment extends VideoBaseFragment {
    private static final String TAG = "USBVideoDevicesFragment";

    public static USBVideoDevicesFragment newInstance() {
        return new USBVideoDevicesFragment();
    }

    private static final int UPDATE_DEVICE_STATUS = 1;

    private MyHandler mHandler;

    private static class MyHandler extends Handler {

        private final WeakReference<USBVideoDevicesFragment> weakReference;

        MyHandler(USBVideoDevicesFragment usbVideoDevicesFragment) {
            weakReference = new WeakReference<>(usbVideoDevicesFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final USBVideoDevicesFragment usbVideoDevicesFragment = weakReference.get();
            switch (msg.what) {
                case UPDATE_DEVICE_STATUS:
                    usbVideoDevicesFragment.updateDeviceStatus();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLayoutResID() {
        return R.layout.usb_video_devices_fragment;
    }

    @Override
    public void initView(View view) {
    }

    @Override
    public void initData() {
        mHandler = new MyHandler(this);
        updateDeviceStatus();
    }

    @Override
    public void initViewListener() {

    }

    @Override
    public void onStart() {
        super.onStart();
//        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
    }

    @Override
    public void onStop() {
        super.onStop();
//        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
    }

    /*private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            *//*if (v.getId() == R.id.btnUBS1) {
                Log.d(TAG, "onClick: btnUBS1");
                USB1VideoPlayActivity.startUSB1VideoPlayActivity(AppBase.mContext);
            } else if (v.getId() == R.id.btnUBS2) {
                Log.d(TAG, "onClick: btnUBS2");
                USB2VideoPlayActivity.startUSB2VideoPlayActivity(AppBase.mContext);
            }*//*
        }
    };*/

    /**
     * 设备状态发生改变的时候，触发的回调
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            switch (path) {
                case DeviceConstants.DevicePath.USB0_PATH:
                case DeviceConstants.DevicePath.USB1_PATH:
                    mHandler.sendEmptyMessage(UPDATE_DEVICE_STATUS);
                    break;
            }
        }
    };


    private void updateDeviceStatus() {
       /* if (DeviceStatusBean.getInstance().isUSB1Connect()) {
            btnUBS1.setVisibility(View.VISIBLE);
        } else {
            btnUBS1.setVisibility(View.GONE);
        }
        if (DeviceStatusBean.getInstance().isUSB2Connect()) {
            btnUBS2.setVisibility(View.VISIBLE);
        } else {
            btnUBS2.setVisibility(View.GONE);
        }*/
    }


}
