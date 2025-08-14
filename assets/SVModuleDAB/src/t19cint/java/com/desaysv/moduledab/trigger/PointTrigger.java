package com.desaysv.moduledab.trigger;

import android.content.Context;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.presenter.tracking.common.AttributeInfo;
import com.desaysv.presenter.tracking.common.EventInfo;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;

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
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int myID = 0;//进程号

    private boolean isSaudiPoint = false;

    private boolean isSecurity = false;
    private boolean isT1E = false;
    private String T1E_App_Id = "";
    private String T1E_Location_Id = "";

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
        isSecurity = ProductUtils.isSecurity();
        isSaudiPoint = ProductUtils.isSaudiPoint();
        isT1E = ProductUtils.isT1EPoint();
        CarConfigUtil.getDefault().init(context);
        trackingPresenter = new TrackingPresenter(context,"Changting","desaysv");
        Log.d(TAG, "isSaudiPoint:"+isSaudiPoint);
//        if (isSaudiPoint){
//            pointServiceHandler = new PointServiceHandler(this);
//            bindService(context);
//            myID = android.os.Process.myPid();
//        }else {
//            DCSdk.init(context);
//        }
    }




    public void deInit(Context context){
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
                map.put("AppName","com.desaysv.svaudioapp");
                map.put("source", "desaysv");
                map.put("keyName", keyName);
                map.put("app_id","LocalRadio");
                trackingPresenter.event(new TrackingInfo("LocalRadio","desaysv",keyName,TrackingEventType.APPLICATION_USAGE,isSecurity,map));
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
        if (isSecurity){
            if (Point.Field.CLOSETYPE.equals(filed) || Point.Field.OPENTYPE.equals(filed)){

            }else {
                Log.d(TAG,"isSecurity return");
                return;
            }

        }

        Log.d(TAG,"trackEvent begin");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"trackEvent start");
                HashMap<String, String> map = new HashMap<>();
                map.put(filed, filedValue);
                //map.put("AppName","com.desaysv.svaudioapp");
                map.put("source", "desaysv");
                map.put("keyName", keyName);
                String appId = "";
                if (keyName.contains(Point.KeyName.OpenAudioMusicClick) || keyName.contains(Point.KeyName.CloseAudioMusicClick)){
                    map.put("app_id","LocalMusic");
                    appId = "LocalMusic";
                    T1E_App_Id = APP_ID_MUSIC;
                    T1E_Location_Id = LOCATION_ID_MUSIC;
                }else if (keyName.contains(Point.KeyName.CloseAudioBTClick) || keyName.contains(Point.KeyName.OpenAudioBTClick)){
                    map.put("app_id","BTMusic");
                    appId = "BTMusic";
                    T1E_App_Id = APP_ID_BT;
                    T1E_Location_Id = LOCATION_ID_BT;
                }else if (keyName.contains(Point.KeyName.CloseAudioClick) || keyName.contains(Point.KeyName.OpenAudioClick)){
                    map.put("app_id","Changting");
                    appId = "Changting";
                }else {
                    map.put("app_id","LocalRadio");
                    appId = "LocalRadio";
                    T1E_App_Id = APP_ID_RADIO;
                    T1E_Location_Id = LOCATION_ID_RADIO;
                }
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                trackingPresenter.event(new TrackingInfo(appId,"desaysv",keyName,TrackingEventType.APPLICATION_USAGE,isSecurity,map));
                Log.d(TAG,"trackEvent end");

                if (isT1E){
                    sendT1EPoint(T1E_App_Id,T1E_Location_Id,Point.Field.OPENTYPE.equals(filed) ? Point.Filed.Filed_OpsMode : Point.Filed.Filed_ClsMode,
                            Point.FiledValue.FiledValue_CLICK,
                            Point.Field.OPENTYPE.equals(filed) ? Point.KEY.KEY_App_Open : Point.KEY.KEY_App_Close,
                            Point.Field.OPENTYPE.equals(filed) ? Point.Filed.Filed_OpsTime : Point.Filed.Filed_ClsTime);
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
                map.put("AppName","com.desaysv.svaudioapp");
                map.put("keyName", keyName);
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                map.put("source","desaysv");
                map.put("app_id","LocalRadio");
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
     * @param filed1 content事件id
     * @param filedValue1 content事件值
     */
    public void trackEvent(final String keyName, String filed, final String filedValue,String filed1, final String filedValue1
            ,String filed2, final String filedValue2,String filed3, final String filedValue3) {
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
                map.put(filed2, filedValue2);
                map.put(filed3, filedValue3);
                map.put("AppName","com.desaysv.svaudioapp");
                map.put("keyName", keyName);
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                map.put("app_id","LocalRadio");
                trackingPresenter.event(new TrackingInfo(keyName,TrackingEventType.APPLICATION_USAGE,isSecurity,map));
                Log.d(TAG,"trackEvent end");
            }
        }).start();

    }



    //T1E 的埋点要用这套新的

    /**
     * 功能编码
     */
    private static final String LOCATION_ID_MUSIC = "ZG010F0F";
    private static final String LOCATION_ID_BT = "ZG020F0F";
    private static final String LOCATION_ID_RADIO = "ZG030F0F";
    /**
     * 应用编码
     */
    private static final String APP_ID_MUSIC = "ZG01";
    private static final String APP_ID_BT = "ZG02";
    private static final String APP_ID_RADIO = "ZG03";
    /**
     *
     * @param attributeId  属性编码 -- OpsMode
     * @param attributeValue  属性值 -- 1
     * @param eventId     事件编码  -- App_Open
     */
    public void sendT1EPoint(String appId, String locationId, String attributeId,String attributeValue,String eventId,String timeAttributeId) {
        if(trackingPresenter == null){
            return;
        }
        Log.d(TAG,"sendT1EPoint,appId: " + appId + ",eventId:"+eventId);
        AttributeInfo attributeInfo = new AttributeInfo(attributeId,locationId,attributeValue);
        AttributeInfo timeAttributeInfo = new AttributeInfo(timeAttributeId,locationId,format.format(new Date()));
        EventInfo eventInfo = new EventInfo(appId,eventId, TrackingEventType.VEHICLE_SETTING,attributeInfo,timeAttributeInfo);
        trackingPresenter.event(eventInfo);
    }

}
