package com.desaysv.moduleusbpicture.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.moduleusbpicture.adapter.PhotoPagerAdapter;
import com.desaysv.moduleusbpicture.businesslogic.listsearch.USBPictureData;


/**
 * Created by LZM on 2019-9-18
 * Comment USB2图片的浏览界面
 * @author uidp5370
 */
public class USB2PictureActivity extends BasePictureActivity {

    private static final String TAG = "USB2PictureActivity";

    public static void startUSB2PictureActivity(Context context, int position) {
        Log.d(TAG, "startUSB2PictureActivity: position = " + position);
        Intent intent = new Intent(context, USB2PictureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_POSITION, position);
        context.startActivity(intent);
    }

    @Override
    protected PhotoPagerAdapter initAdapter() {
        PhotoPagerAdapter photoPagerAdapter = new PhotoPagerAdapter(this);
        photoPagerAdapter.setPhotoList("usb2_photo",USBPictureData.getInstance().getUSB2AllPictureList());
        return photoPagerAdapter;
    }

    @Override
    public void initViewListener() {
        super.initViewListener();
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
