package com.desaysv.modulebtmusic.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.desaysv.ivi.vdb.IVDBusNotify;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.sms.VDEventSms;
import com.desaysv.ivi.vdb.event.id.sms.VDValueAction;
import com.desaysv.ivi.vdb.event.id.sms.VDValueKey;
import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;

public class BTMusicHardKeyAdapterManager {
    private static final String TAG = "BTMusicHardKeyAdapterManager";

    private Context mContext;
    private static volatile BTMusicHardKeyAdapterManager mInstance;

    public static BTMusicHardKeyAdapterManager getInstance() {
        if (mInstance == null) {
            synchronized (BTMusicHardKeyAdapterManager.class) {
                if (mInstance == null) {
                    mInstance = new BTMusicHardKeyAdapterManager();
                }
            }
        }
        return mInstance;
    }

    public void initialize(Context context) {
        Log.i(TAG, "initialize: ");
        mContext = context;
        VDBus.getDefault().registerVDBindListener(mVDBindListener);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.SMS);
    }

    public void release() {
        Log.i(TAG, "release: ");
        VDBus.getDefault().unregisterVDBindListener(mVDBindListener);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.SMS);
    }

    private final VDBindListener mVDBindListener = new VDBindListener() {
        @Override
        public void onVDConnected(VDServiceDef.ServiceType serviceType) {
            if (serviceType == VDServiceDef.ServiceType.SMS) {
                Log.i(TAG, "bindService success");
                //订阅获取需要的按键事件
                Bundle payload = new Bundle();
                payload.putIntArray(VDKey.TYPE, new int[]{VDValueKey.KEYCODE_MUTE});
                VDEvent event = new VDEvent(VDEventSms.ID_SMS_KEY_EVENT, payload);
                VDBus.getDefault().subscribe(event, mIVDBusNotify);
            }
        }

        @Override
        public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
            if (serviceType == VDServiceDef.ServiceType.SMS) {
                Log.w(TAG, "bindService fail, bind again");
                VDBus.getDefault().bindService(VDServiceDef.ServiceType.SMS);
            }
        }
    };

    private final IVDBusNotify.Stub mIVDBusNotify = new IVDBusNotify.Stub() {
        @Override
        public void onVDBusNotify(VDEvent vdEvent) throws RemoteException {
            if (vdEvent == null) {
                Log.w(TAG, "onVDBusNotify: vdEvent is null");
                return;
            }
            if (vdEvent.getId() == VDEventSms.ID_SMS_KEY_EVENT) {
                Bundle payload = vdEvent.getPayload();
                if (payload == null) {
                    Log.w(TAG, "onVDBusNotify: payload is null");
                    return;
                }
                int keyCode = payload.getInt(VDKey.TYPE);
                int keyAction = payload.getInt(VDKey.ACTION);//长按、短按
                Log.i(TAG, "onVDBusNotify: keyCode = " + keyCode + ", keyAction = " + keyAction);
                if (keyCode == VDValueKey.KEYCODE_MUTE) {//静音按键
                    switch (keyAction) {
                        case VDValueAction.ACTION_RELEASE://按键短按释放
                            boolean a2DPConnected = BTMusicManager.getInstance().isA2DPConnected();
                            boolean avrcpConnected = BTMusicManager.getInstance().isAVRCPConnected();
                            Log.i(TAG, "onVDBusNotify: a2DPConnected = " + a2DPConnected + ", avrcpConnected = " + avrcpConnected);
                            if (!a2DPConnected || !avrcpConnected) {//蓝牙音乐未连接时不执行以下逻辑
                                return;
                            }
                            boolean isInMuteState = BTMusicAudioManager.getInstance().isInMuteState();
                            Log.i(TAG, "onVDBusNotify: isInMuteState = " + isInMuteState);
                            if (isInMuteState) {//当前在静音状态，若蓝牙音乐在播放状态，需要暂停
                                SVMusicInfo musicPlayInfo = BTMusicManager.getInstance().getMusicPlayInfo();
                                if (musicPlayInfo != null && BTMusicManager.getInstance().isPlayingState(musicPlayInfo.getPlayState())) {
                                    BTMusicManager.getInstance().pause();
                                }
                            } else {//非静音状态
                                if (BTMusicAudioManager.getInstance().checkBTAudioFocusStatus()) {//检查当前焦点是否在蓝牙音乐上，在蓝牙音乐上时才恢复播放
                                    BTMusicManager.getInstance().play();
                                }
                            }
                            break;
                        case VDValueAction.ACTION_LONG_RELEASE://按键长按释放
                        default:
                            break;
                    }
                }
            }
        }
    };
}
