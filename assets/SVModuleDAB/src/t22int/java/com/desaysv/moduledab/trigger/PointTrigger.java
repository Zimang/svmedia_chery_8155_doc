package com.desaysv.moduledab.trigger;

import android.content.Context;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;

import java.text.SimpleDateFormat;
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
    private SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:MM");

    private int myID = 0;//进程号

    private boolean isSaudiPoint = false;

    private boolean isSecurity = false;
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
        isSaudiPoint = ProductUtils.isSaudiPoint();
        isSecurity = ProductUtils.isSecurity() || isSaudiPoint;
        CarConfigUtil.getDefault().init(context);
        trackingPresenter = new TrackingPresenter(context,"LocalRadio","desaysv");
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
                }else if (keyName.contains(Point.KeyName.CloseAudioBTClick) || keyName.contains(Point.KeyName.OpenAudioBTClick)){
                    map.put("app_id","BTMusic");
                    appId = "BTMusic";
                }else if (keyName.contains(Point.KeyName.OpenAudioClick) || keyName.contains(Point.KeyName.CloseAudioClick)){
                    map.put("app_id","Changting");
                    appId = "Changting";
                }else {
                    map.put("app_id","LocalRadio");
                    appId = "LocalRadio";
                }
                map.put("Timestamp", String.valueOf(System.currentTimeMillis()));
                trackingPresenter.event(new TrackingInfo(appId,"desaysv",keyName,TrackingEventType.APPLICATION_USAGE,isSecurity,map));
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

}
