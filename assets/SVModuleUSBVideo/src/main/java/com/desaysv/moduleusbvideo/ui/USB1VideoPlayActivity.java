package com.desaysv.moduleusbvideo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoDate;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoFolderListData;
import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.desaysv.moduleusbvideo.util.VideoFileListTypeTool;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;
import java.util.Objects;

/**
 * Created by LZM on 2019-9-20
 * Comment USB1视频的播放界面
 */
public class USB1VideoPlayActivity extends BaseVideoPlayActivity {
    private static final String TAG = "USB1VideoPlayActivity";

    public static void startUSB1VideoPlayActivity(Context context, String path) {
        Log.d(TAG, "startUSB1VideoPlayActivity:");
        startUSB1VideoPlayActivity(context, true, path);
    }

    public static void startUSB1VideoPlayActivity(Context context, boolean isNeedToAutoPlay, String path) {
        Log.d(TAG, "startUSB1VideoPlayActivity: isNeedToAutoPlay = " + isNeedToAutoPlay + " path = " + path);
        Intent intent = new Intent(context, USB1VideoPlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_NEED_TO_AUTO_PLAY, isNeedToAutoPlay);
        intent.putExtra(AUTO_PLAY_PATH, path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean isConnect() {
        return DeviceStatusBean.getInstance().isUSB1Connect();
    }

    @Override
    protected void autoPlay() {
        Log.d(TAG, "autoPlay: isNeedToAutoPlay = " + isNeedToAutoPlay);
        if (!isNeedToAutoPlay) {
            return;
        }
        setPlayList();
        getVideoControl().processCommand(MediaAction.OPEN, ChangeReasonData.AUTO_PLAY, getPlayPosition(currentPath));
    }

    /**
     * 给VideoControl设置播放列表
     */
    @Override
    protected void setPlayList() {
        int styleType = Objects.requireNonNull(VideoFileListTypeTool.getInstance(DeviceConstants.DevicePath.USB0_PATH)).getStyleType();
        Log.d(TAG, "setPlayList: autoPlay: styleType = " + styleType + " , currentPath: " + currentPath);
        if (VideoFileListTypeTool.STYLE_TYPE_ALL == styleType) {
            getVideoControl().setPlayList(USBVideoDate.getInstance().getUSB1VideoAllList(), CurrentPlayListType.ALL);
        } else {
            List<FileMessage> usb1VideoFolderPlayList = USBVideoFolderListData.getInstance().getUSB1VideoFolderPlayList(currentPath);
            getVideoControl().setPlayList(usb1VideoFolderPlayList, CurrentPlayListType.FLODER);
        }
    }

    @Override
    protected void initVideoControl() {
        Log.i(TAG, "initVideoControl: USB1");
        ModuleUSBVideoTrigger.getInstance().initUSB1VideoControl(requestVideoPlayer);
    }

    @Override
    protected IControlTool getVideoControl() {
        return ModuleUSBVideoTrigger.getInstance().USB1VideoControlTool;
    }

    @Override
    protected IStatusTool getVideoStatusTool() {
        return ModuleUSBVideoTrigger.getInstance().USB1VideoStatusTool;
    }

    @Override
    protected void registerVideoStatusChangeListener(IMediaStatusChange iMediaStatusChange) {
        Objects.requireNonNull(ModuleUSBVideoTrigger.getInstance().getUSB1VideoControlTool)
                .registerMediaStatusChangeListener(TAG, iMediaStatusChange);
    }

    @Override
    protected void unregisterVideoStatusChangeListener(IMediaStatusChange iMediaStatusChange) {
        Objects.requireNonNull(ModuleUSBVideoTrigger.getInstance().getUSB1VideoControlTool)
                .unregisterMediaStatusChangerListener(TAG);
    }

    /**
     * 设备状态变化的时候，会触发的回调
     */
    private final DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            if (DeviceConstants.DevicePath.USB0_PATH.equals(path) && !status) {
                // U盘拔出，需要先释放播放器
                getVideoControl().processCommand(MediaAction.STOP, ChangeReasonData.USB1_EJECT);
                Log.e(TAG, "onDeviceStatusChange: FINISH 1");
                finish();
            }
        }
    };


    @Override
    protected MediaType getMediaType() {
        return MediaType.USB1_VIDEO;
    }
}
