package com.desaysv.moduleradio.Trigger;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.cabin.VDEventCabinLan;
import com.desaysv.ivi.vdb.event.id.cabin.bean.VDCLCommonMessage;
import com.desaysv.ivi.vdb.event.id.carstate.VDEventCarState;
import com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState;
import com.desaysv.ivi.vdb.event.id.device.VDEventVehicleDevice;
import com.desaysv.ivi.vdb.event.id.dsp.VDEventDsp;
import com.desaysv.ivi.vdb.event.id.dsp.VDValueDsp;
import com.desaysv.ivi.vdb.event.id.phonelink.VDEventPhoneLink;
import com.desaysv.ivi.vdb.event.id.phonelink.VDValuePhoneLink;
import com.desaysv.ivi.vdb.event.id.rvc.VDEventRvc;
import com.desaysv.ivi.vdb.event.id.rvc.VDValueRvc;
import com.desaysv.ivi.vdb.event.id.vehicle.VDEventVehicleHal;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDAsr;
import com.desaysv.libradio.action.RadioControlAction;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * created by ZNB on 2023-01-10
 * VDB 事件的监听处理，例如 STR
 */
public class SVVDBControl {
    private static final String TAG = "SVVDBControl";

    private static SVVDBControl mInstance;
    public static SVVDBControl getInstance(){
        synchronized (SVVDBControl.class){
            if (mInstance == null){
                mInstance = new SVVDBControl();
            }
            return mInstance;
        }
    }

    /**
     * 初始化 VDB 监听事件
     */
    public void init(Context context){
        Log.i(TAG,"init");
        VDBus.getDefault().init(context);
        VDBus.getDefault().addSubscribe(VDEventCarState.POWER_STATUS); // 订阅电源状态事件,
        VDBus.getDefault().addSubscribe(VDEventRvc.RVC_STATUS); // 订阅倒车事件
        VDBus.getDefault().addSubscribe(VDEventVR.VR_STATUS); // 订阅语音唤醒状态事件
        VDBus.getDefault().addSubscribe(VDEventPhoneLink.PHONE_CALL_STATE); // 订阅来电状态事件

        //判断vehicle device服务是否已经连接
        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.VEHICLE_DEVICE)) {
            Log.i(TAG, "VDServiceDef.ServiceType.VEHICLE_DEVICE is connected");
            VDBus.getDefault().addSubscribe(VDEventVehicleDevice.PROJECT_VEHICLE_PROPERTY_CONFIG_UPDATE);
        }
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
        VDBus.getDefault().subscribeCommit(); // 提交订阅

        sp = context.getSharedPreferences(spName,MODE_PRIVATE);
        editor = sp.edit();
        needResumeRadioFromSTRReboot = sp.getBoolean(spKey,false);
        Log.i(TAG,"init,needResumeRadioFromSTRReboot："+needResumeRadioFromSTRReboot);
        mHandler = new MyHandler(this);

        registerFactoryListener(context);

        /**
         * 注册状态监听，在这里只是为了STR退出时重新初始化的操作
         */
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(new IRadioStatusChange() {
            @Override
            public void onCurrentRadioMessageChange() {

            }

            @Override
            public void onPlayStatusChange(boolean isPlaying) {

            }

            @Override
            public void onAstListChanged(int band) {

            }

            @Override
            public void onSearchStatusChange(boolean isSearching) {

            }

            @Override
            public void onSeekStatusChange(boolean isSeeking) {

            }

            @Override
            public void onAnnNotify(DABAnnNotify notify) {

            }

            @Override
            public void onRDSFlagInfoChange(RDSFlagInfo info) {

            }

            @Override
            public void onInitSuccessCallback() {
                Log.i(TAG,"onInitSuccessCallback");
                Bundle payload = new Bundle();
                payload.putInt(VDKey.SOURCE, 8/*VDValueCarState.SOURCE_RADIO_BROADCAST*/);
                payload.putInt(VDKey.EVENT, 11/*VDValueCarState.EVENT_RADIO_INITED*/);
                VDEvent event = new VDEvent(VDEventCarState.INJECT_APP_EVENT, payload);
                VDBus.getDefault().set(event);
                mHandler.sendEmptyMessageDelayed(MSG_REOPEN,50);
            }
        });

    }

    /**
     * VDB事件的监听
     */
    private int curState = -1;//
    private boolean needResumeRadio = false;

    private boolean needResumeRadioFromSTRReboot = false;//如果STR状态下重启(例如断电等)，因为临时方案释放了焦点，会导致重启后不自动播放
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private static final String spName = "needResumeRadio";
    private static final String spKey = "needResumeRadioKey";

    private VDNotifyListener mVDNotifyListener = new VDNotifyListener() {
        public void onVDNotify(VDEvent event, int threadType) {
            if (threadType == VDThreadType.MAIN_THREAD) { // 主线程回调处理
                Log.i(TAG,"onVDNotify,threadType: "+threadType + ", ID: " + event.getId());
                switch (event.getId()) {
                    case VDEventCarState.POWER_STATUS:
                        if (event.getPayload() != null) {
                            int tempState = event.getPayload().getInt(VDKey.STATUS);
                            Log.i(TAG,"POWER_STATUS,curState:"+curState+", tempState:"+tempState);
                            if (curState == VDValueCarState.PowerStatus.STATE_AVN_STR && tempState != VDValueCarState.PowerStatus.STATE_AVN_STR){
                                //此条件满足，说明是从 STR模式退出
                                Log.i(TAG,"Exit STR");
                                mHandler.sendEmptyMessageDelayed(MSG_INIT,100);
                            }else if (tempState == VDValueCarState.PowerStatus.STATE_AVN_STR){
                                Log.i(TAG,"enter STR");
                                RadioControlAction.getInstance().resetInit();
                                //临时方案，进入STR的时候，切到media通道，不要用tuner,同时释放Radio的焦点
                                if (RadioControlTool.getInstance().checkCurrentRadioSourceWithSTR()){
                                    needResumeRadio = true;
                                    editor.putBoolean(spKey,true);
                                    editor.apply();
                                    //RadioControlTool.getInstance().releaseFocusWithSTR(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                                    RadioControlTool.getInstance().notifyLossForTTSWithSTR();
                                    sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                                    Log.i(TAG,"enter STR,当前是电台的焦点");
                                }else if (RadioControlTool.getInstance().checkHadRadioTTSWithSTR()){//进入STR的时候，没有应用持有焦点，但是最后一次是RDS_TTS或者DAB_TTS
                                    sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                                    Log.i(TAG,"enter STR,没有焦点，但是最后一次是RDS_TTS或者DAB_TTS");
                                }else if (RadioControlTool.getInstance().checkRDSTTSSourceWithSTR()){//进入STR的时候，如果焦点是RDS_TTS，那么需要释放焦点，切换通道
                                    //RadioControlTool.getInstance().releaseRDSTTSFocusWithSTR();
                                    sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                                    RadioControlTool.getInstance().notifyLossForTTSWithSTR();
                                    Log.i(TAG,"enter STR,当前是RDS_TTS的焦点");
                                }else if (RadioControlTool.getInstance().checkHadRadioTTSWithSTR()){//进入STR的时候，如果焦点是DAB_TTS，那么需要释放焦点，切换通道
                                    //RadioControlTool.getInstance().releaseDABTTSFocusWithSTR();
                                    sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                                    RadioControlTool.getInstance().notifyLossForTTSWithSTR();
                                    Log.i(TAG,"enter STR,当前是DAB_TTS的焦点");
                                }
                            }else if (tempState == VDValueCarState.PowerStatus.STATE_AVN_OFF){//待机状态
                                Log.i(TAG,"enter STATE_AVN_OFF");
                                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
                            }else if (tempState == VDValueCarState.PowerStatus.STATE_AVN_SLEEP){//power重启
                                Log.i(TAG,"enter STATE_AVN_SLEEP");
                                if (RadioControlTool.getInstance().checkCurrentRadioSourceWithSTR()){
                                    editor.putBoolean(spKey,true);
                                    editor.apply();
                                    //RadioControlTool.getInstance().releaseFocusWithSTR(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                                    sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                                }
                            }
                            curState = tempState;
                        }
                        break;
                    case VDEventVehicleDevice.PROJECT_VEHICLE_PROPERTY_CONFIG_UPDATE:
                        Log.i(TAG,"CONFIG_UPDATE");
                        RadioConfig.getInstance().checkAndSetPreRegion();
                        break;
                    case VDEventRvc.RVC_STATUS:
                        int status = event.getPayload().getInt(VDKey.STATUS);
                        Log.i(TAG,"RVC_STATUS,+status:"+status);
                        if (VDValueRvc.RvcStatus.RVC == status){
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
                        }
                        break;
                    case VDEventVR.VR_STATUS:
                        VDAsr asr = event.getPayload().getParcelable(VDKey.STATUS);
                        Log.i(TAG,"VR_STATUS,status:"+asr.getValue());
//                        if (asr.getValue() == VDValueVR.AsrStatus.ACTIVE){
//                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PAUSE, ChangeReasonData.VR_CONTROL);
//                        }else if (asr.getValue() == VDValueVR.AsrStatus.INACTIVE){
//                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.VR_CONTROL);
//                        }
                        break;
                    case VDEventPhoneLink.PHONE_CALL_STATE:
                        int phoneCallState = event.getPayload().getInt(VDKey.STATUS);
                        Log.i(TAG,"PHONE_CALL_STATE,status:"+phoneCallState);
                        if (phoneCallState == VDValuePhoneLink.PhoneCallState.INCOMING || phoneCallState == VDValuePhoneLink.PhoneCallState.OUTGOING || phoneCallState == VDValuePhoneLink.PhoneCallState.ACTIVE){
                            RadioControlTool.getInstance().notifyLossForTTS();
                            RadioControlTool.getInstance().setPhoneCallState(true);
                        }else {
                            RadioControlTool.getInstance().setPhoneCallState(false);
                        }
                        break;
                }
            }
        }
    };
    private MyHandler mHandler;
    private static final int MSG_INIT = 0;
    private static final int MSG_REOPEN = 1;

    private static class MyHandler extends Handler {
        WeakReference<SVVDBControl> weakReference;
        MyHandler(SVVDBControl svvdbControl) {
            weakReference = new WeakReference<>(svvdbControl);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.i(TAG,"Exit STR initialize,"+msg.what);
            switch (msg.what){
                case MSG_INIT:
                    RadioControlAction.getInstance().initialize();
                    break;
                case MSG_REOPEN:
                    //临时方案，退出STR的时候，切回tuner通道
                    RadioControlAction.getInstance().setfromSTR(false);
                    if (weakReference.get().needResumeRadio){//这个为true说明进入STR之前是Radio的焦点
                        Log.i(TAG,"handleMessage,needResumeRadio");
                        weakReference.get().needResumeRadio = false;
                        weakReference.get().editor.putBoolean(spKey,false).apply();
                        //不需要手动切通道，焦点申请成功会自动切，这样可以避免焦点申请不成功时，通道被切到Tuner，导致下次进入STR再退出时，还是有杂音
                        //sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_TUNER, 1);
                        if (SPUtlis.getInstance().getRadioPlayPauseStatus()) {
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.BOOT_RESUME, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                        } else {
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.BOOT_RESUME_NOT_PLAY, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                        }
                    }else if (weakReference.get().needResumeRadioFromSTRReboot){//这个为true说明进入STR后直接重启，重启之前是Radio的焦点
                        Log.i(TAG,"handleMessage,needResumeRadioFromSTRReboot");
                        weakReference.get().editor.putBoolean(spKey,false).apply();
                        if (SPUtlis.getInstance().getRadioPlayPauseStatus()) {
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.BOOT_RESUME, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                        } else {
                            ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.BOOT_RESUME_NOT_PLAY, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                        }
                    }else {
                        //不需要此时再执行播放，否则有可能会导致杂音
                        //ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.RECOVER_SOURCE, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static final void sendVDBusEvent(int eventId, int type, int value) {
        Log.i(TAG,"sendVDBusEvent");
        Bundle bundle = new Bundle();
        bundle.putInt(VDKey.TYPE, type);
        bundle.putInt(VDKey.VALUE, value);
        VDEvent vdEvent = new VDEvent(eventId, bundle);
        VDBus.getDefault().set(vdEvent);
    }

    /**
     * 注册恢复出厂设置的监听
     */
    private static final String FACTORY_KEY = "com.desaysv.setting.reset";
    private void registerFactoryListener(Context context){
        Log.i(TAG,"registerFactoryListener");
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(Settings.System.getUriFor(FACTORY_KEY), true, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                int factory = Settings.System.getInt(contentResolver, "com.desaysv.setting.reset", 0);
                Log.i(TAG,"registerFactoryListener,onchange,factory："+factory);
                if (factory == 1){
                    RadioControlAction.getInstance().resetInit();
                    //临时方案，恢复出厂设置的时候，切到media通道，不要用tuner,同时释放Radio的焦点//按照fwk的反馈,释放焦点也会引起通道切换到tuner,释放焦点和切换通道是异步的
                    if (RadioControlTool.getInstance().checkCurrentRadioSourceWithSTR()){
                        //RadioControlTool.getInstance().releaseFocusWithSTR(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                        sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                        Log.i(TAG,"registerFactoryListener,当前是电台的焦点");
                    }else if (RadioControlTool.getInstance().checkHadRadioTTSWithSTR()){//进入STR的时候，没有应用持有焦点，但是最后一次是RDS_TTS或者DAB_TTS
                        sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                        Log.i(TAG,"registerFactoryListener,没有焦点，但是最后一次是RDS_TTS或者DAB_TTS");
                    }else if (RadioControlTool.getInstance().checkRDSTTSSourceWithSTR()){//进入STR的时候，如果焦点是RDS_TTS，那么需要释放焦点，切换通道
                        //RadioControlTool.getInstance().releaseRDSTTSFocusWithSTR();
                        sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                        Log.i(TAG,"registerFactoryListener,当前是RDS_TTS的焦点");
                    }else if (RadioControlTool.getInstance().checkHadRadioTTSWithSTR()){//进入STR的时候，如果焦点是DAB_TTS，那么需要释放焦点，切换通道
                        //RadioControlTool.getInstance().releaseDABTTSFocusWithSTR();
                        sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                        Log.i(TAG,"registerFactoryListener,当前是DAB_TTS的焦点");
                    }
                }
            }
        });
    }

}
