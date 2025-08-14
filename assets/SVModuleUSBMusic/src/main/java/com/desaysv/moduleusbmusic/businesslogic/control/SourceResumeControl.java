package com.desaysv.moduleusbmusic.businesslogic.control;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.annotation.NonNull;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleusbmusic.listener.ISourceResume;
import com.desaysv.moduleusbmusic.listener.ResumeAction;
import com.desaysv.moduleusbmusic.utils.MusicControlUtils;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.File;

/**
 * @author uidq1846
 * @desc 音源恢复控制器
 * @time 2023-1-4 15:36
 */
public class SourceResumeControl implements ISourceResume {
    private final String TAG = this.getClass().getSimpleName();
    private StorageManager storageManager;
    private Handler handler;
    private static final int OPEN_USB0_MOUNTED = 0x01;
    private static final int OPEN_USB1_MOUNTED = OPEN_USB0_MOUNTED + 1;
    private boolean isFirstTimeInit = true;

    private SourceResumeControl() {
        initStorageManager();
    }

    private static final class SourceResumeHolder {
        static final ISourceResume sourceResume = new SourceResumeControl();
    }

    public static ISourceResume getInstance() {
        return SourceResumeHolder.sourceResume;
    }

    /**
     * 初始化存储管理者
     */
    private void initStorageManager() {
        storageManager = AppBase.mContext.getSystemService(StorageManager.class);
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                SourceResumeControl.this.handleMessage(msg);
            }
        };
    }

    @Override
    public void openSource(ResumeAction action) {
        Log.d(TAG, "openSource: action = " + action.name());
        switch (action) {
            case USB0_MOUNTED:
                handler.removeMessages(OPEN_USB0_MOUNTED);
                handler.sendEmptyMessageDelayed(OPEN_USB0_MOUNTED, 1000);
                break;
            case USB1_MOUNTED:
                handler.removeMessages(OPEN_USB1_MOUNTED);
                handler.sendEmptyMessageDelayed(OPEN_USB1_MOUNTED, 1000);
                break;
            case USB0_UNMOUNTED:
                handler.removeMessages(OPEN_USB0_MOUNTED);
                break;
            case USB1_UNMOUNTED:
                handler.removeMessages(OPEN_USB1_MOUNTED);
                break;
            default:
                //移除重置
                isFirstTimeInit = false;
                break;
        }
    }

    @Override
    public void openSource(String source, boolean isForeground, int flag, ChangeReason changeReason) {
        MusicControlUtils.getInstance().openSource(source, isForeground, flag, changeReason);
    }

    /**
     * 处理当前handle消息
     *
     * @param msg msg
     */
    private void handleMessage(Message msg) {
        String bootSourceName = AudioFocusUtils.getInstance().getBootResumeSource();
        Log.d(TAG, "openSource: bootSourceName = " + bootSourceName);
        switch (msg.what) {
            case OPEN_USB0_MOUNTED:
                //如果接入的是USB，上个媒体源也是相应音源的话，则执行恢复动作
                if (DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(bootSourceName)) {
                    //如果存在曲目则，恢复拉起界面
                    StorageVolume volume = storageManager.getStorageVolume(new File(DeviceConstants.DevicePath.USB0_PATH));
                    if (volume == null) {
                        Log.e(TAG, "handleMessage: volume is null");
                        return;
                    }
                    FileMessage message = MediaPlayStatusSaveUtils.getInstance().getMediaFileMessage(MediaType.USB1_MUSIC);
                    Log.d(TAG, "openSource: volume = " + volume.getUuid().toLowerCase() + " message = " + message);
                    if (volume.getUuid().toLowerCase().equals(message != null ? message.getDeviceUUID() : null)) {
                        openSource(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, !isFirstTimeInit, Constant.OpenSourceViewType.PLAY_VIEW, ChangeReasonData.BOOT_RESUME);
                    }
                    isFirstTimeInit = false;
                }
                break;
            case OPEN_USB1_MOUNTED:
                if (DsvAudioSDKConstants.USB1_MUSIC_SOURCE.equals(bootSourceName)) {
                    StorageVolume volume = storageManager.getStorageVolume(new File(DeviceConstants.DevicePath.USB1_PATH));
                    if (volume == null) {
                        Log.e(TAG, "handleMessage: volume is null");
                        return;
                    }
                    FileMessage message = MediaPlayStatusSaveUtils.getInstance().getMediaFileMessage(MediaType.USB2_MUSIC);
                    Log.d(TAG, "openSource: volume = " + volume.getUuid().toLowerCase() + " message = " + message);
                    if (volume.getUuid().toLowerCase().equals(message != null ? message.getDeviceUUID() : null)) {
                        openSource(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, !isFirstTimeInit, Constant.OpenSourceViewType.PLAY_VIEW, ChangeReasonData.BOOT_RESUME);
                    }
                    isFirstTimeInit = false;
                }
                break;
        }
    }
}
