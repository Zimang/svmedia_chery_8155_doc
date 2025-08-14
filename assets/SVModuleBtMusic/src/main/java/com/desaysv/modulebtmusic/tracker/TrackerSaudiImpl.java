package com.desaysv.modulebtmusic.tracker;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

//import com.lion.datapoint.log.LogDataInterfaceManager;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.presenter.tracking.common.EventInfo;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 沙特埋点
 *
 * @author uidq8677
 */
public class TrackerSaudiImpl implements ITracker {

    private static final String TAG = "BTMusic_TrackerSaudiImpl";

    /**
     * 应用Id。业务采集所属应用Id
     */
    private static final String APP_ID = "BTMusic";
    /**
     * 主机平台
     */
    private static final String SOURCE = "desaysv";
    private boolean isSecurity;
    private Context mContext;

    private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean mIsServiceConnected = false;
    private TrackingPresenter trackingPresenter;
    public TrackerSaudiImpl(Context context) {
        Log.d(TAG, "initialize tracker");
        mContext = context;
        isSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity();
        trackingPresenter = new TrackingPresenter(context,APP_ID,SOURCE);
//        // 初始化埋点的服务
//        try {
//            int mode = LogDataInterfaceManager.POINT_MODE.RELEASE_MODE;
//            // 埋点服务service是否启动的回调
//            LogDataInterfaceManager.LogDataServiceConnection serviceConnection = new LogDataInterfaceManager.LogDataServiceConnection() {
//                @Override
//                public void onServiceConnected() {
//                    Log.d(TAG, "onServiceConnected");
//                    mIsServiceConnected = true;
//                }
//
//                @Override
//                public void onServiceDisconnected() {
//                    Log.d(TAG, "onServiceDisconnected");
//                    mIsServiceConnected = false;
//                }
//            };
//            LogDataInterfaceManager.getInstances().initialize(context.getApplicationContext(), serviceConnection,
//                    context.getPackageName().hashCode(), null, mode);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "init() bindService buriedPoint failed");
//        }
    }

    @Override
    /**
     * 埋点数据拼接
     *
     * @param keyName 埋点key
     * @param field   content的field内容
     * @param value   对应值
     */
    public void trackEvent(String keyName, String field, String value) {
        if (TextUtils.isEmpty(keyName)) {
            Log.e(TAG, "keyName is empty");
            return;
        }
//        if (!mIsServiceConnected) {//服务未连接
//            Log.e(TAG, "services is not connect");
//            return;
//        }
        HashMap<String, String> map = new HashMap<>();
        map.put(field, value);
        map.put("keyName", keyName);
        map.put("app_id", APP_ID);
        map.put("source", SOURCE);
        map.put("Timestamp", mFormat.format(new Date()));
        trackingPresenter.event(new TrackingInfo(keyName, TrackingEventType.APPLICATION_USAGE,isSecurity,map));
    }

    @Override
    public void trackEventPlatform(EventInfo eventInfo) {
        trackingPresenter.event(eventInfo);
    }

    @Override
    public void release() {
        Log.d(TAG, "tracker release");
        if (mContext != null) {
            trackingPresenter.release();
            mContext = null;
        }
    }
}