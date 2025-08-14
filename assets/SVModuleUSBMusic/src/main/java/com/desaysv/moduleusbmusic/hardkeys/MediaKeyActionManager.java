package com.desaysv.moduleusbmusic.hardkeys;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.ivi.vdb.IVDBusNotify;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.sms.VDEventSms;
import com.desaysv.ivi.vdb.event.id.sms.VDValueAction;
import com.desaysv.ivi.vdb.event.id.sms.VDValueKey;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * @author uidq1846
 * @desc 处理媒体按键的管理
 * @time 2022-12-14 20:07
 */
public class MediaKeyActionManager implements IKeyAction {
    private static final String TAG = "Music_" + MediaKeyActionManager.class.getSimpleName();
    private Handler handler;
    private static final int KEY_ACTION = 0x01;
    private Context context;

    private MediaKeyActionManager() {
    }

    private static final class KeyActionHolder {
        static final IKeyAction keyAction = new MediaKeyActionManager();
    }

    public static IKeyAction getInstance() {
        return KeyActionHolder.keyAction;
    }

    @Override
    public void init(Context context) {
        this.context = context;
        initHandlerThread();
        initVDService();
    }

    /**
     * 先绑定SMS的VDS
     */
    private void initVDService() {
        Log.d(TAG, "initVDService: ");
        VDBus.getDefault().init(context);
        VDBus.getDefault().registerVDBindListener(new VDBindListener() {
            @Override
            public void onVDConnected(VDServiceDef.ServiceType serviceType) {
                //执行些media vds启动时需初始化的工作
                //订阅VDB事件
                Log.d(TAG, "onVDConnected: serviceType = " + serviceType);
                if (VDServiceDef.ServiceType.SMS == serviceType) {
                    registerMediaKey();
                }
            }

            @Override
            public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
                Log.d(TAG, "onVDDisconnected: serviceType = " + serviceType);
            }
        });
        //绑定服务
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.SMS);
    }

    /**
     * 初始化Handler线程
     */
    private void initHandlerThread() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == KEY_ACTION) {
                    handlerKeyAction(msg);
                }
            }
        };
    }

    /**
     * 注册媒体按键
     */
    private void registerMediaKey() {
        Log.d(TAG, "registerMediaKey: ");
        Bundle payload = new Bundle();
        //添加需要监听的媒体按键
        payload.putIntArray(VDKey.TYPE,
                new int[]{
                        VDValueKey.KEYCODE_MEDIA_NEXT,
                        VDValueKey.KEYCODE_MEDIA_PREVIOUS,
                        VDValueKey.KEYCODE_MUSIC,
                        VDValueKey.KEYCODE_PLAY,
                        VDValueKey.KEYCODE_PAUSE,
                        VDValueKey.KEYCODE_PLAY_OR_PAUSE,
                });
        VDBus.getDefault().subscribe(new VDEvent(VDEventSms.ID_SMS_KEY_EVENT, payload), mIVDBusNotify);
    }


    /**
     * 硬按键回调处理
     */
    private final IVDBusNotify.Stub mIVDBusNotify = new IVDBusNotify.Stub() {

        @Override
        public void onVDBusNotify(VDEvent vdEvent) throws RemoteException {
            if (vdEvent.getId() == VDEventSms.ID_SMS_KEY_EVENT) {
                //keycode值  eg：{@link VDValueKey#KEYCODE_HOME}
                int keyCode = vdEvent.getPayload().getInt(VDKey.TYPE);
                //keyAction eg：{@link VDValueAction#ACTION_PRESS}
                int keyAction = vdEvent.getPayload().getInt(VDKey.ACTION);
                Log.d(TAG, "onVDBusNotify: keyCode = " + keyCode + " keyAction = " + keyAction);
                Message obtain = Message.obtain();
                obtain.what = KEY_ACTION;
                obtain.arg1 = keyCode;
                obtain.arg2 = keyAction;
                handler.sendMessage(obtain);
            }
        }
    };

    /**
     * 处理按键事件
     */
    private void handlerKeyAction(Message msg) {
        int keyCode = msg.arg1;
        int keyAction = msg.arg2;
        if (keyAction == VDValueAction.ACTION_RELEASE) {
            //短按处理
            // TODO: 2022-12-14 这里需要根据当前音源是不是自己的源再进行处理
            IControlTool controlTool = null;
            String sourceName = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(context);
            Log.d(TAG, "handlerKeyAction: sourceName = " + sourceName);
            if (DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName)) {
                controlTool = ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool;
            } else if (DsvAudioSDKConstants.USB1_MUSIC_SOURCE.equals(sourceName)) {
                controlTool = ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool;
            } else if (DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName)) {
                controlTool = ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool;
            }
            if (controlTool == null) {
                return;
            }
            int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
            Log.d(TAG, "handlerKeyAction: mediaType = " + mediaType);
            //当前已经是媒体源了，如果当前是最近播放控制器，需要特殊处理下
            if (MediaType.RECENT_MUSIC.ordinal() == mediaType) {
                controlTool = ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getControlTool();
            }
            switch (keyCode) {
                case VDValueKey.KEYCODE_MEDIA_NEXT:
                    controlTool.processCommand(MediaAction.NEXT, ChangeReasonData.HARD_KEY);
                    if (isLocalPoint(sourceName)) {
                        LocalMusicPoint.getInstance().next(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    } else {
                        UsbMusicPoint.getInstance().next(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    }
                    break;
                case VDValueKey.KEYCODE_MEDIA_PREVIOUS:
                    controlTool.processCommand(MediaAction.PRE, ChangeReasonData.HARD_KEY);
                    if (isLocalPoint(sourceName)) {
                        LocalMusicPoint.getInstance().pre(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    } else {
                        UsbMusicPoint.getInstance().pre(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    }
                    break;
                case VDValueKey.KEYCODE_MUSIC:
                    //这里是不是要拉起界面呢？存疑
                    break;
                case VDValueKey.KEYCODE_PLAY:
                    controlTool.processCommand(MediaAction.START, ChangeReasonData.HARD_KEY);
                    if (isLocalPoint(sourceName)) {
                        LocalMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    } else {
                        UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    }
                    break;
                case VDValueKey.KEYCODE_PAUSE:
                    controlTool.processCommand(MediaAction.PAUSE, ChangeReasonData.HARD_KEY);
                    if (isLocalPoint(sourceName)) {
                        LocalMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    } else {
                        UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                    }
                    break;
                case VDValueKey.KEYCODE_PLAY_OR_PAUSE:
                    if (ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().isPlaying()) {
                        if (isLocalPoint(sourceName)) {
                            LocalMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                        } else {
                            UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                        }
                    } else {
                        if (isLocalPoint(sourceName)) {
                            LocalMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                        } else {
                            UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem()));
                        }
                    }
                    controlTool.processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.HARD_KEY);
                    break;
            }
        }
    }

    /**
     * 当前是否是本地源
     *
     * @param sourceName sourceName
     * @return T 是 F 否
     */
    private boolean isLocalPoint(String sourceName) {
        return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName);
    }

    /**
     * 封装曲目信息
     *
     * @param currentPlayItem currentPlayItem
     * @return ContentData[]
     */
    private ContentData[] getContentData(FileMessage currentPlayItem) {
        ContentData[] contentData = new ContentData[4];
        contentData[0] = new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click);
        contentData[1] = new ContentData(PointValue.Field.ProgramName, currentPlayItem.getName());
        contentData[2] = new ContentData(PointValue.Field.Author, currentPlayItem.getAuthor());
        contentData[3] = new ContentData(PointValue.Field.Album, currentPlayItem.getAlbum());
        return contentData;
    }
}
