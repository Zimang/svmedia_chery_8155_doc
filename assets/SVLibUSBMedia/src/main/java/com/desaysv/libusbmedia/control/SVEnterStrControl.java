package com.desaysv.libusbmedia.control;

import android.content.Context;
import android.util.Log;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.carstate.VDEventCarState;
import com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState;
import com.desaysv.ivi.vdb.event.id.device.VDEventVehicleDevice;
import com.desaysv.ivi.vdb.event.id.phonelink.VDEventPhoneLink;
import com.desaysv.ivi.vdb.event.id.rvc.VDEventRvc;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDAsr;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;

/**
 * created by ZNB on 2023-01-10
 * 进入STR监听，用于释放mediaplay
 */
public class SVEnterStrControl {
    private static final String TAG = "SVEnterStrControl";

    private static SVEnterStrControl mInstance;
    public static SVEnterStrControl getInstance(){
        synchronized (SVEnterStrControl.class){
            if (mInstance == null){
                mInstance = new SVEnterStrControl();
            }
            return mInstance;
        }
    }

    /**
     * 初始化 VDB 监听事件
     */
    public void init(Context context){
        Log.d(TAG,"init");
        VDBus.getDefault().init(context);

        //判断vehicle device服务是否已经连接
        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.VEHICLE_DEVICE)) {
            Log.i(TAG, "VDServiceDef.ServiceType.VEHICLE_DEVICE is connected");
            VDBus.getDefault().addSubscribe(VDEventVehicleDevice.PROJECT_VEHICLE_PROPERTY_CONFIG_UPDATE);
        }
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
        VDBus.getDefault().subscribeCommit(); // 提交订阅
    }

    /**
     * VDB事件的监听
     */
    private int curState = -1;//

    private VDNotifyListener mVDNotifyListener = new VDNotifyListener() {
        public void onVDNotify(VDEvent event, int threadType) {
            if (threadType == VDThreadType.MAIN_THREAD) { // 主线程回调处理
                Log.i(TAG,"onVDNotify,threadType: "+threadType + ", ID: " + event.getId());
                switch (event.getId()) {
                    case VDEventCarState.POWER_STATUS:
                        if (event.getPayload() != null) {
                            int tempState = event.getPayload().getInt(VDKey.STATUS);
                            Log.d(TAG,"POWER_STATUS,curState:"+curState+", tempState:"+tempState);
                            if (curState == VDValueCarState.PowerStatus.STATE_AVN_STR && tempState != VDValueCarState.PowerStatus.STATE_AVN_STR){
                                //此条件满足，说明是从 STR模式退出
                                Log.d(TAG,"Exit STR");
                            }else if (tempState == VDValueCarState.PowerStatus.STATE_AVN_STR){
                                Log.d(TAG,"enter STR");
                                MediaControlTool.getInstance(MediaType.USB1_VIDEO).processCommand(MediaAction.RELEASE, ChangeReasonData.ENTER_STR_RELEASE);
                            }else if (tempState == VDValueCarState.PowerStatus.STATE_AVN_OFF){//待机状态
                                Log.d(TAG,"enter STATE_AVN_OFF");
                            }else if (tempState == VDValueCarState.PowerStatus.STATE_AVN_SLEEP){//power重启
                                Log.d(TAG,"enter STATE_AVN_SLEEP");
                            }
                            curState = tempState;
                        }
                        break;
                    case VDEventVehicleDevice.PROJECT_VEHICLE_PROPERTY_CONFIG_UPDATE:
                        Log.d(TAG,"CONFIG_UPDATE");
                        break;
                    case VDEventRvc.RVC_STATUS:
                        int status = event.getPayload().getInt(VDKey.STATUS);
                        Log.d(TAG,"RVC_STATUS,+status:"+status);
                        break;
                    case VDEventVR.VR_STATUS:
                        VDAsr asr = event.getPayload().getParcelable(VDKey.STATUS);
                        Log.d(TAG,"VR_STATUS,status:"+asr.getValue());
                        break;
                    case VDEventPhoneLink.PHONE_CALL_STATE:
                        int phoneCallState = event.getPayload().getInt(VDKey.STATUS);
                        Log.d(TAG,"PHONE_CALL_STATE,status:"+phoneCallState);
                        break;
                }
            }
        }
    };
}
