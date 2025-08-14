package com.desaysv.moduleradio.hardkey;

import android.content.Context;
import android.hardware.radio.RadioManager;
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
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.utils.CurrentShowFragmentUtil;

import java.util.ArrayList;
import java.util.List;

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
    private boolean hasAM = true;

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
        initHandlerThread();
        initVDService();
        hasMulti = ProductUtils.hasMulti();
        hasAM = ProductUtils.hasAM();
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
                    Log.d(TAG,"isCollectPageOnResume = " + CurrentShowFragmentUtil.isCollectPageOnResume);
                    //如果在收藏列表页面，下一曲是收藏列表的下一曲
                    if (CurrentShowFragmentUtil.isCollectPageOnResume) {
                        RadioMessage currentRadioMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
                        final List<RadioMessage> mCollectList = new ArrayList<>();
                        if (hasMulti) {
                            mCollectList.addAll(RadioList.getInstance().getMultiCollectRadioMessageList());
                            if (hasAM){
                                mCollectList.addAll(RadioList.getInstance().getAMCollectRadioMessageList());
                            }
                        } else {
                            List<RadioMessage> allAM = RadioList.getInstance().getAMCollectRadioMessageList();
                            List<RadioMessage> allFM = RadioList.getInstance().getFMCollectRadioMessageList();
                            mCollectList.addAll(allAM);
                            mCollectList.addAll(allFM);
                        }
                        //收藏数据为空
                        if (mCollectList.isEmpty()){
                            Log.d(TAG,"collectList isEmpty return");
//                            controlTool.processCommand(hasMulti ? RadioAction.MULTI_SEEK_FORWARD : RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
                            return;
                        }
                        Log.d(TAG,"collectList size = " + mCollectList.size());
                        //在收藏列表找到当前播放电台的下一个有效的电台
                        int firstFindPosition;
                        if (currentRadioMessage.isCollect()) {
                            firstFindPosition = indexOfCollect(currentRadioMessage,mCollectList);
                            firstFindPosition = (firstFindPosition + 1) % mCollectList.size();
                        } else {
                            firstFindPosition = 0;
                        }

                        Log.d(TAG,"KEYCODE_MEDIA_NEXT  firstFindPosition = " + firstFindPosition);
                        controlTool.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mCollectList.get(firstFindPosition));
                    } else {
                        controlTool.processCommand(hasMulti ? RadioAction.MULTI_SEEK_FORWARD : RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
                    }
                    break;
                case VDValueKey.KEYCODE_MEDIA_PREVIOUS:
                    Log.d(TAG,"isCollectPageOnResume = " + CurrentShowFragmentUtil.isCollectPageOnResume);
                    //如果在收藏列表页面，上一曲是收藏列表的上一曲
                    if (CurrentShowFragmentUtil.isCollectPageOnResume) {
                        RadioMessage currentRadioMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
                        final List<RadioMessage> mCollectList = new ArrayList<>();
                        if (hasMulti) {
                            mCollectList.addAll(RadioList.getInstance().getMultiCollectRadioMessageList());
                            if (hasAM){
                                mCollectList.addAll(RadioList.getInstance().getAMCollectRadioMessageList());
                            }
                        } else {
                            List<RadioMessage> allAM = RadioList.getInstance().getAMCollectRadioMessageList();
                            List<RadioMessage> allFM = RadioList.getInstance().getFMCollectRadioMessageList();
                            mCollectList.addAll(allAM);
                            mCollectList.addAll(allFM);
                        }
                        //收藏数据为空
                        if (mCollectList.isEmpty()){
                            Log.d(TAG,"collectList isEmpty return");
//                            controlTool.processCommand(hasMulti ? RadioAction.MULTI_SEEK_BACKWARD : RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
                            return;
                        }
                        Log.d(TAG,"collectList size = " + mCollectList.size());
                        //找到上一个有效的收藏电台
                        int firstFindPosition;
                        if (currentRadioMessage.isCollect()) {
                            firstFindPosition = indexOfCollect(currentRadioMessage,mCollectList);
                            firstFindPosition = (firstFindPosition - 1 + mCollectList.size()) % mCollectList.size();
                        } else {
                            firstFindPosition = mCollectList.size() - 1;
                        }

                        Log.d(TAG,"KEYCODE_MEDIA_PREVIOUS  firstFindPosition = " + firstFindPosition);
                        controlTool.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, mCollectList.get(firstFindPosition));
                    } else {
                        controlTool.processCommand(hasMulti ? RadioAction.MULTI_SEEK_BACKWARD : RadioAction.SEEK_BACKWARD, ChangeReasonData.CLICK);
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

    /**
     * 当前电台在收藏列表中的Index
     * @param tempRM
     * @param radioMessageList
     * @return
     */
    private int indexOfCollect(RadioMessage tempRM,List<RadioMessage> radioMessageList) {
        if (tempRM.getRadioType() == RadioMessage.DAB_TYPE) {
            for (int j = 0; j < radioMessageList.size(); j++) {
                if (radioMessageList.get(j).getRadioType() == RadioMessage.DAB_TYPE) {
                    if (tempRM.getDabMessage().getFrequency() == radioMessageList.get(j).getDabMessage().getFrequency()
                            && tempRM.getDabMessage().getServiceId() == radioMessageList.get(j).getDabMessage().getServiceId()
                            && tempRM.getDabMessage().getServiceComponentId() == radioMessageList.get(j).getDabMessage().getServiceComponentId()) {
                        Log.d(TAG,"indexOfCollect:" + tempRM + " index = " + j);
                        return j;
                    }
                }
            }
        } else if (tempRM.getRadioBand() == RadioManager.BAND_FM) {
            for (int j = 0; j < radioMessageList.size(); j++) {
                if (radioMessageList.get(j).getRadioBand() == RadioManager.BAND_FM) {
                    if (tempRM.getRadioFrequency() == radioMessageList.get(j).getRadioFrequency()){
                        Log.d(TAG,"indexOfCollect:" + tempRM + " index = " + j);
                        return j;
                    }
                }
            }
        } else if (tempRM.getRadioBand() == RadioManager.BAND_AM) {
            for (int j = 0; j < radioMessageList.size(); j++) {
                if (radioMessageList.get(j).getRadioBand() == RadioManager.BAND_AM) {
                    if (tempRM.getRadioFrequency() == radioMessageList.get(j).getRadioFrequency()){
                        Log.d(TAG,"indexOfCollect:" + tempRM + " index = " + j);
                        return j;
                    }
                }
            }
        }
        return -1;
    }


}
