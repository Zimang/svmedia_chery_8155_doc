package com.desaysv.moduleusbmusic.dataPoint;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lion.datapoint.log.LogDataInterfaceManager;
import com.liontech.dcsdk.DCSdk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author uidq1846
 * @desc 埋点管理类
 * @time 2023-4-12 22:05
 */
public class PointManager implements IDataPoint {
    private static final String TAG = PointManager.class.getSimpleName();
    private boolean isServiceConnected = false;
    private Handler handler;
    private Context context;
    private final static int REBIND_SERVICE = 0x01;
    private final static int UPLOAD_DATA = REBIND_SERVICE + 1;

    /**
     * 目前除了T22/T19C/T18Fl3等沙特以外的项目，需要使用另一套埋点
     */
    private static final int SAUDI_COUNTRY_CODE = 29;//沙特国家码
    private static final int T22_MODEL_CODE = 2;//t22 model code
    private static final int T19C_MODEL_CODE = 7;//t19c model code
    private static final int T18FL3_MODEL_CODE = 10;//t18fl3 model code
    private boolean isSaudiPoint;
    private boolean isSecurity;

    private TrackingPresenter trackingPresenter;

    private static final class PointManagerHolder {
        static final PointManager pointManager = new PointManager();
    }

    public static IDataPoint getInstance() {
        return PointManagerHolder.pointManager;
    }

    @Override
    public void init(Context context) {
        this.context = context;
        isSaudiPoint = isSaudiPoint();
        isSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity();
        trackingPresenter = new TrackingPresenter(context, "LocalMusic", "desaysv");
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                messageProcess(msg);
            }
        };
        initPointService();
    }

    /**
     * 消息处理
     *
     * @param msg msg
     */
    private void messageProcess(Message msg) {
        switch (msg.what) {
            case REBIND_SERVICE:
                initPointService();
                break;
            case UPLOAD_DATA:
                if (isSecurity || isSaudiPoint) {
                    Log.i(TAG, "messageProcess: isSecurity or isSaudiPoint so do not upload any event!!!");
                    return;
                }
                UploadData data = (UploadData) msg.obj;
                String json = new Gson().toJson(data);
                Log.d(TAG, "messageProcess: json = " + json + " isServiceConnected = " + isServiceConnected + " isSaudiPoint = " + isSaudiPoint);
                try {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("AppName", "com.desaysv.svaudioapp");
                    map.put("source", data.getSource());
                    map.put("app_id", data.getApp_id());
                    String contentJson = data.getContent();
                    JsonObject jsonObject = new Gson().fromJson(contentJson, JsonObject.class);
                    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
                    String[] jsonArray = new String[2 * entries.size() + 2];
                    jsonArray[0] = "AppName";
                    jsonArray[1] = "com.desaysv.svaudioapp";
                    int i = 2;
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        jsonArray[i] = entry.getKey();
                        jsonArray[i + 1] = entry.getValue().getAsString();
                        map.put(jsonArray[i], jsonArray[i + 1]);
                        i = i + 2;
                    }
                    Log.d(TAG, "messageProcess: jsonArray = " + Arrays.toString(jsonArray));
                    trackingPresenter.event(new TrackingInfo(data.getApp_id(), "desaysv", data.getKeyName(), TrackingEventType.APPLICATION_USAGE, isSecurity, map));
//                        DCSdk.event(data.getKeyName(),
//                                data.getApp_id(),
//                                "", data.getSource(),
//                                jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(TAG, "messageProcess: Exception");
                }
                break;
        }
    }

    /**
     * 绑定埋点服务
     */
    private void initPointService() {
        Log.i(TAG, "initPointService: ");
//        if (isSaudiPoint) {
//            LogDataInterfaceManager.getInstances().initialize(context, conn,
//                    context.getPackageName().hashCode(), null, LogDataInterfaceManager.POINT_MODE.RELEASE_MODE);
//        } else {
//            DCSdk.init(context);
//        }
    }

    @Override
    public void uploadData(UploadData data) {
        Message obtain = Message.obtain();
        obtain.obj = data;
        obtain.what = UPLOAD_DATA;
        handler.sendMessage(obtain);
    }

    /**
     * 服务连接
     */
/*    private final LogDataInterfaceManager.LogDataServiceConnection conn = new LogDataInterfaceManager.LogDataServiceConnection() {

        @Override
        public void onServiceConnected() {
            Log.i(TAG, "onServiceConnected: ");
            isServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected() {
            Log.i(TAG, "onServiceDisconnected: ");
            isServiceConnected = false;
            //10s 后重连
            handler.removeMessages(REBIND_SERVICE);
            handler.sendEmptyMessageDelayed(REBIND_SERVICE, 10000);
        }
    };*/

    /**
     * isSaudiPoint
     *
     * @return 是否为沙特国家码
     */
    private boolean isSaudiPoint() {
        return CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION) == SAUDI_COUNTRY_CODE;
    }
}
