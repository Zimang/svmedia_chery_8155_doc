package com.desaysv.usbpicture.trigger;

import static com.desaysv.ivi.extra.project.carinfo.proxy.Constants.SpiMsgType.MSG_ON_CHANGE_EVENT;
import static com.desaysv.ivi.extra.project.carinfo.proxy.Constants.SpiMsgType.MSG_TIMING_ARRIVAL;

import android.content.Context;
import android.util.Log;

import com.desaysv.ivi.extra.project.carinfo.ReadOnlyID;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoHelper;
import com.desaysv.ivi.vdb.event.id.carinfo.VDEventCarInfo;

public class GearBoxControl {

    private static final String TAG = "GearBoxControl";

    private static GearBoxControl mInstance;

    private final CarInfoHelper mCarInfoHelper = new CarInfoHelper();
    private final int[] mReadonlyIds = new int[] {
            ReadOnlyID.ID_GEARBOX_STATE,                    // 变速箱档位
    };

    public static GearBoxControl getInstance(){
        synchronized (GearBoxControl.class){
            if (mInstance == null){
                mInstance = new GearBoxControl();
            }
            return mInstance;
        }
    }

    /**
     * 订阅RVC下发事件
     */
    public void subScribeSpi(Context context){
        Log.d(TAG,"subScribeSpi");
        //设置档位监听
        mCarInfoHelper.listen(VDEventCarInfo.MODULE_READONLY_INFO, mReadonlyIds);
        mCarInfoHelper.start(spiListener);
    }


    /**
     * 注销档位监听
     */
    public void unRegisterSpiListener(){
        Log.d(TAG, "unRegisterSpiListener: ");
        mCarInfoHelper.end();
    }


    private CarInfoHelper.ISpiListener spiListener = new CarInfoHelper.ISpiListener() {
        @Override
        public void onReceiveSpi(int msgType, int moduleId, int cmdId) {
            Log.d(TAG,"onReceiveSpi,msgType = " + msgType + ", moduleId = " + moduleId + ", cmdId = " + cmdId);
            switch (msgType) {
                case MSG_TIMING_ARRIVAL:
                case MSG_ON_CHANGE_EVENT:
                default:
                    break;
            }
        }
    };

}
