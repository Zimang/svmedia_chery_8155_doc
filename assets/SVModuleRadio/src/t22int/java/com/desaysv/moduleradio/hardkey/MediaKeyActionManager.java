package com.desaysv.moduleradio.hardkey;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.ivi.vdb.IVDBusNotify;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.sms.VDEventSms;
import com.desaysv.ivi.vdb.event.id.sms.VDValueAction;
import com.desaysv.ivi.vdb.event.id.sms.VDValueKey;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;

/**
 * @author uidq1846
 * @desc 处理媒体按键的管理
 * @time 2022-12-14 20:07
 */
public class MediaKeyActionManager {
    private static final String TAG = MediaKeyActionManager.class.getSimpleName();
    private Handler handler;
    private static final int KEY_ACTION = 0x01;
    private Context context;
    private boolean hasMulti;

    private static MediaKeyActionManager instance;

    public static MediaKeyActionManager getInstance(){
        if (instance == null){
            synchronized (MediaKeyActionManager.class){
                if (instance == null){
                    instance = new MediaKeyActionManager();
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造方法
     */
    private MediaKeyActionManager(){

    }

    public void init(Context context) {
        this.context = context;
        hasMulti = ProductUtils.hasMulti();
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
        if (keyAction == VDValueAction.ACTION_RELEASE) {//Release 的时候才处理，否则长按事件也会响应
            //短按处理
            // TODO: 2022-12-14 这里需要根据当前音源是不是自己的源再进行处理
            IControlTool controlTool = null;
            String sourceName = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(context);
            Log.d(TAG, "handlerKeyAction: sourceName = " + sourceName);
            if (DsvAudioSDKConstants.FM_SOURCE.equals(sourceName)
                || DsvAudioSDKConstants.AM_SOURCE.equals(sourceName)
                || DsvAudioSDKConstants.DAB_SOURCE.equals(sourceName)) {
                controlTool = ModuleRadioTrigger.getInstance().mRadioControl;
            }

            if (controlTool == null) {
                return;
            }
            switch (keyCode) {
                case VDValueKey.KEYCODE_MEDIA_NEXT:
                    if(hasMulti && (DsvAudioSDKConstants.FM_SOURCE.equals(sourceName)
                            || DsvAudioSDKConstants.DAB_SOURCE.equals(sourceName))){
                        controlTool.processCommand(RadioAction.MULTI_SEEK_FORWARD, ChangeReasonData.CLICK);
                    } else {
                        controlTool.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
                    }
                    break;
                case VDValueKey.KEYCODE_MEDIA_PREVIOUS:
                    if(hasMulti && (DsvAudioSDKConstants.FM_SOURCE.equals(sourceName)
                            || DsvAudioSDKConstants.DAB_SOURCE.equals(sourceName))){
                        controlTool.processCommand(RadioAction.MULTI_SEEK_BACKWARD, ChangeReasonData.CLICK);
                    } else {
                        controlTool.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.CLICK);
                    }
                    break;
                case VDValueKey.KEYCODE_MUSIC:
                    //这里是不是要拉起界面呢？存疑
                    break;
                case VDValueKey.KEYCODE_PLAY:
                    controlTool.processCommand(RadioAction.PLAY, ChangeReasonData.CLICK);
                    break;
                case VDValueKey.KEYCODE_PAUSE:
                    controlTool.processCommand(RadioAction.PAUSE, ChangeReasonData.CLICK);
                    break;
                case VDValueKey.KEYCODE_PLAY_OR_PAUSE:
                    controlTool.processCommand(RadioAction.PLAY_OR_PAUSE, ChangeReasonData.CLICK);
                    break;
            }
        }
    }
}
