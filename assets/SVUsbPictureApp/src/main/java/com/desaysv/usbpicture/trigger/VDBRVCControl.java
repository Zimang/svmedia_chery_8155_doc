package com.desaysv.usbpicture.trigger;

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
import com.desaysv.ivi.vdb.event.id.rvc.VDEventRvc;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.svlibusbdialog.dialog.SourceDialogUtil;
import com.desaysv.usbpicture.bean.VRJsonBean;
import com.desaysv.usbpicture.constant.VRAction;
import com.desaysv.usbpicture.trigger.interfaces.IRVCResponseOperator;
import com.desaysv.usbpicture.trigger.interfaces.IVRResponseOperator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZNB on 2022-05-09
 * 语音控制器的处理
 * 统一封装语义的监听和解析操作
 * 应用的各个部分通过注册为实现者，实现其中的操作
 */
public class VDBRVCControl {
    private static final String TAG = "VDBRVCControl";

    private static VDBRVCControl mInstance;
    private Gson mGson = new Gson();
    public static VDBRVCControl getInstance(){
        synchronized (VDBRVCControl.class){
            if (mInstance == null){
                mInstance = new VDBRVCControl();
            }
            return mInstance;
        }
    }


    /**
     * 订阅RVC下发事件
     */
    public void subScribeRVC(Context context){
        Log.d(TAG,"subScribeRVC");
        // 被动获取倒车状态回调（callback）
//        VDBus.getDefault().addSubscribe(VDEventRvc.RVC_STATUS); // 订阅RVC倒车状态事件,
        VDBus.getDefault().addSubscribe(VDEventRvc.VIDEO_SIGNAL); // 订阅倒车摄像头信号事件
        VDBus.getDefault().addSubscribe(VDEventCarState.TOD_STATUS); // 订阅屏幕信号事件
        VDBus.getDefault().addSubscribe(VDEventCarState.POWER_STATUS); // 订阅点火信号事件
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
    }


    /**
     * 取消订阅语RVC下发事件
     */
    public void unSubScribeVRVC(){
        Log.d(TAG,"unSubScribeVR");
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);
    }



    /**
     * RVC事件的监听
     */
    private  VDNotifyListener mVDNotifyListener = new VDNotifyListener() {
        public void onVDNotify(VDEvent event, int threadType) {
            if (threadType == VDThreadType.MAIN_THREAD) { // 主线程回调处理
                Log.i(TAG,"onVDNotify,threadType: "+threadType + ", ID: " + event.getId());
                switch (event.getId()) {
                    case VDEventRvc.VIDEO_SIGNAL:
                        int status = event.getPayload().getInt(VDKey.STATUS);
                        Log.d(TAG,"onVDNotify,status: "+status);
                        updateWithRVC(status);
                        break;
                    case VDEventCarState.TOD_STATUS:
                        boolean screenOff = event.getPayload().getBoolean(VDKey.STATUS);
                        Log.d(TAG,"onVDNotify,screenOff: "+screenOff);
                        SourceDialogUtil.getInstance().reShowDialog(screenOff);
                        break;
                    case VDEventCarState.POWER_STATUS:
                        int powerStatus = event.getPayload().getInt(VDKey.STATUS);
                        Log.d(TAG,"onVDNotify,powerStatus: "+powerStatus);
                        if (powerStatus == VDValueCarState.PowerStatus.STATE_AVN_RUN_ON
                            || powerStatus == VDValueCarState.PowerStatus.STATE_AVN_TIME_ON){
                            SourceDialogUtil.getInstance().reShowDefaultBgDialog();
                        }
                        break;
                }
            }
        }
    };


    private void updateWithRVC(int status){
        Log.d(TAG,"updateWithRVC: "+status);
        for (IRVCResponseOperator operator : operatorList){
            operator.updateWithRVC(status);
        }
    }


    private List<IRVCResponseOperator> operatorList = new ArrayList<>();

    /**
     * 注册 响应RVC 的具体实现者
     * @param operator
     */
    public void registerVRResponseOperator(IRVCResponseOperator operator){
        operatorList.add(operator);
    }

    /**
     * 注销 响应RVC 的具体实现者
     * @param operator
     */
    public void unregisterVRResponseOperator(IRVCResponseOperator operator){
        operatorList.remove(operator);
    }


}
