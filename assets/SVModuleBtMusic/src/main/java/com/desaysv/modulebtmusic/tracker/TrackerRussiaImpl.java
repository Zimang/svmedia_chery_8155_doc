package com.desaysv.modulebtmusic.tracker;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.presenter.tracking.common.EventInfo;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;

import java.util.HashMap;

/**
 * 俄罗斯埋点
 *
 * @author uidq8677
 */
public class TrackerRussiaImpl implements ITracker {

    private static final String TAG = "BT_TrackerRussiaImpl";

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
    private TrackingPresenter trackingPresenter;
    public TrackerRussiaImpl(Context context) {
        Log.d(TAG, "initialize tracker");
        mContext = context;
        trackingPresenter = new TrackingPresenter(context,APP_ID,SOURCE);
        isSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity();
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
        /*
         * 0809版本埋点⽅案
         *
         * @param keyName 事件名称，不可空
         * @param appId ⼀级功能编码，不可空
         * @param userId ⽤⼾标识，可空
         * @param source 供应商编码 ，不可空
         * @param field 事件属性列表 key:属性名称 value:属性值, KV 键值对，必须是偶数，可空
         */
        HashMap<String, String> map = new HashMap<>();
        map.put(field, value);
        trackingPresenter.event(new TrackingInfo(keyName, TrackingEventType.APPLICATION_USAGE,isSecurity,map));
    }

    @Override
    public void trackEventPlatform(EventInfo eventInfo) {
        trackingPresenter.event(eventInfo);
    }

    @Override
    public void release() {
        Log.d(TAG, "tracker release");
    }
}
