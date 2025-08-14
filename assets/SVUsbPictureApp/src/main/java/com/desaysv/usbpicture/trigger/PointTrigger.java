package com.desaysv.usbpicture.trigger;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.presenter.tracking.common.AttributeInfo;
import com.desaysv.presenter.tracking.common.EventInfo;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;
import com.desaysv.usbpicture.constant.Point;
import com.desaysv.usbpicture.utils.ProductUtils;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * created by ZNB on 2023-03-08
 * 埋点事件的触发器，用于埋点接口的初始化
 */
public class PointTrigger {

    private static final String TAG = "PointTrigger";
    private static PointTrigger mInstance;
    private final static int MSG_REBIND_SERVICE = 0;
    private final static int REBIND_TIME = 5000;
    private final static int REBIND_COUNT = 99;
    private int mRebindCount = 0;
    private Context mContext;
//    private PointServiceHandler pointServiceHandler;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int myID = 0;//进程号

    private boolean isSaudiPoint = false;

    private boolean isSecurity = false;
    private boolean isT1E = false;

    private TrackingPresenter trackingPresenter;
    public static PointTrigger getInstance(){
        synchronized (PointTrigger.class){
            if (mInstance == null){
                mInstance = new PointTrigger();
            }
            return mInstance;
        }
    }

    public void init(Context context){
        mContext = context;
        CarConfigUtil.getDefault().init(context);
        trackingPresenter = new TrackingPresenter(context,"Picture","desaysv");
        isSaudiPoint = ProductUtils.isSaudiPoint();
        isSecurity =  ProductUtils.isSecurity() || isSaudiPoint;
        isT1E = ProductUtils.isT1EPoint();
        Log.d(TAG, "isSecurity:"+isSecurity);
//        if (isSaudiPoint) {
//            pointServiceHandler = new PointServiceHandler(this);
//            bindService(context);
//            myID = android.os.Process.myPid();
//        }else {
//            DCSdk.init(context);
//        }
    }


//    private void bindService(Context context){
//        try {
//            int mode = LogDataInterfaceManager.POINT_MODE.RELEASE_MODE;
//            LogDataInterfaceManager.getInstances().initialize(context, conn,
//                    context.getPackageName().hashCode(), null, mode);
//            Log.d(TAG, "step 1 初始化 ");
//            mRebindCount = 0;
//        } catch (Exception e) {
//            if (mRebindCount < REBIND_COUNT){
//                pointServiceHandler.sendEmptyMessageDelayed(MSG_REBIND_SERVICE, REBIND_TIME);
//                mRebindCount++;
//            } else {
//                Log.d(TAG, "bindService over " + REBIND_COUNT);
//            }
//            Log.e(TAG, "init() bindService failed", e);
//            Log.d(TAG, "step 1 初始化 异常");
//        }
//    }
//
//    /**
//     * 埋点服务service是否启动的回调
//     */
//    private LogDataInterfaceManager.LogDataServiceConnection conn = new LogDataInterfaceManager.LogDataServiceConnection() {
//        @Override
//        public void onServiceConnected() {
//            Log.d(TAG, "onServiceConnected: ");
//            mRebindCount = 0;
//        }
//
//        @Override
//        public void onServiceDisconnected() {
//            Log.d(TAG, "onServiceDisconnected: ");
//            if (mRebindCount < REBIND_COUNT){
//                pointServiceHandler.sendEmptyMessageDelayed(MSG_REBIND_SERVICE, REBIND_TIME);
//                mRebindCount++;
//            }
//        }
//    };

//    private static class PointServiceHandler extends Handler {
//        private final WeakReference<PointTrigger> mParent;
//
//        private PointServiceHandler(PointTrigger parent) {
//            mParent = new WeakReference<PointTrigger>(parent);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            if (mParent.get() != null) {
//                mParent.get().rebindService();
//            }
//            super.handleMessage(msg);
//        }
//    }
//
//    private void rebindService() {
//        bindService(mContext);
//    }


    public void deInit(Context context){
        //LogDataInterfaceManager.getInstances().deInitialize(context.getPackageName().hashCode());
        trackingPresenter.release();
    }



    /**
     * 事件埋点
     *
     * @param keyName   事件id
     */
    public void trackEvent(final String keyName) {
        if (mRebindCount != 0){//不为0，说明服务绑不上
            Log.d(TAG,"trackEvent service not connect");
            return;
        }
        Log.d(TAG,"trackEvent begin");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"trackEvent start");
                HashMap<String, String> map = new HashMap<>();
                map.put("AppName","com.desaysv.usbpicture");
                map.put("keyName", keyName);
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                map.put("source","desaysv");
                map.put("app_id","Picture");
                trackingPresenter.event(new TrackingInfo(keyName,TrackingEventType.APPLICATION_USAGE,isSecurity,map));
                Log.d(TAG,"trackEvent end");
            }
        }).start();

    }




    /**
     * 事件埋点
     *
     * @param keyName   事件id
     * @param filed content事件id
     * @param filedValue content事件值
     */
    public void trackEvent(final String keyName, String filed, final String filedValue) {
        if (mRebindCount != 0){//不为0，说明服务绑不上
            Log.d(TAG,"trackEvent service not connect");
            return;
        }
        if (isSecurity && !(keyName.equals(Point.KeyName.OpenPictureClick) || keyName.equals(Point.KeyName.ClosePictureClick))){
            Log.d(TAG,"isSecurity return");
            return;
        }

        Log.d(TAG,"trackEvent begin");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"trackEvent start");
                HashMap<String, String> map = new HashMap<>();
                map.put(filed, filedValue);
                //map.put("AppName","com.desaysv.usbpicture");
                map.put("keyName", keyName);
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                map.put("source","desaysv");
                map.put("app_id","Picture");
                trackingPresenter.event(new TrackingInfo(keyName,TrackingEventType.APPLICATION_USAGE,isSecurity,map));
                Log.d(TAG,"trackEvent end");
                if (isT1E){
                    sendT1EPoint(keyName.equals(Point.KeyName.OpenPictureClick) ? Point.Filed.Filed_OpsMode : Point.Filed.Filed_ClsMode,
                            Point.FiledValue.FiledValue_CLICK,
                            keyName.equals(Point.KeyName.OpenPictureClick) ? Point.KEY.KEY_App_Open : Point.KEY.KEY_App_Close,
                            keyName.equals(Point.KeyName.OpenPictureClick) ? Point.Filed.Filed_OpsTime : Point.Filed.Filed_ClsTime);
                }
            }
        }).start();

    }


    /**
     * 事件埋点
     *
     * @param keyName   事件id
     * @param filed content事件id
     * @param filedValue content事件值
     * @param filed1 content事件id
     * @param filedValue1 content事件值
     */
    public void trackEvent(final String keyName, String filed, final String filedValue,String filed1, final String filedValue1) {
        if (mRebindCount != 0){//不为0，说明服务绑不上
            Log.d(TAG,"trackEvent service not connect");
            return;
        }
        if (isSecurity){
            Log.d(TAG,"isSecurity return");
            return;
        }

        Log.d(TAG,"trackEvent begin");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"trackEvent start");
                HashMap<String, String> map = new HashMap<>();
                map.put(filed, filedValue);
                map.put(filed1, filedValue1);
                //map.put("AppName","com.desaysv.usbpicture");
                map.put("keyName", keyName);
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                map.put("source","desaysv");
                map.put("app_id","Picture");
                trackingPresenter.event(new TrackingInfo(keyName, TrackingEventType.APPLICATION_USAGE,isSecurity,map));
                Log.d(TAG,"trackEvent end");
            }
        }).start();

    }

    //T1E 的埋点要用这套新的

    /**
     * 功能编码
     */
    private static final String LOCATION_ID = "ZC020F0F";
    /**
     * 应用编码
     */
    private static final String APP_ID = "ZC02";
    /**
     *
     * @param attributeId  属性编码 -- OpsMode
     * @param attributeValue  属性值 -- 1
     * @param eventId     事件编码  -- App_Open
     */
    public void sendT1EPoint(String attributeId,String attributeValue,String eventId,String timeAttributeId) {
        if(trackingPresenter == null){
            return;
        }
        Log.d(TAG,"sendT1EPoint,eventId:"+eventId);
        AttributeInfo attributeInfo = new AttributeInfo(attributeId,LOCATION_ID,attributeValue);
        AttributeInfo timeAttributeInfo = new AttributeInfo(timeAttributeId,LOCATION_ID,format.format(new Date()));
        EventInfo eventInfo = new EventInfo(APP_ID,eventId, TrackingEventType.VEHICLE_SETTING,attributeInfo,timeAttributeInfo);
        trackingPresenter.event(eventInfo);
    }

}
