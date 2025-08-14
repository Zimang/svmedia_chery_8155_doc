package com.desaysv.moduleusbvideo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.presenter.tracking.common.AttributeInfo;
import com.desaysv.presenter.tracking.common.EventInfo;
import com.desaysv.presenter.tracking.common.TrackingEventType;
import com.desaysv.presenter.tracking.common.TrackingInfo;
import com.desaysv.presenter.tracking.presenter.TrackingPresenter;

/**
 * 数据埋点
 * Create by extodc87 on 2023-3-22
 * Author: extodc87
 */
public class LionPointSdkUtil {
    private static final String TAG = "LionPointSdkUtil";
    boolean isSecurity;
    boolean isSaudi;


    private static final class InstanceHolder {
        @SuppressLint("StaticFieldLeak")
        static final LionPointSdkUtil instance = new LionPointSdkUtil();
    }

    public static LionPointSdkUtil getInstance() {
        return LionPointSdkUtil.InstanceHolder.instance;
    }


    public static final String LION_OPEN_CLICK = "Video.N.N.N.OpenClick";
    public static final String LION_CLOSE_CLICK = "Video.N.N.N.CloseClick";
    public static final String LION_LOAD_ANOMALY = "Video.Page.N.N.LoadAnomaly";
    public static final String LION_VIDEO_OPERATE = "Video.N.N.N.VideoOperate";

    public static final String LION_VOLUME_SET = "Video.Voice.N.N.VolumeSet";


    public static final String FIELD_OPEN_TYPE = "OpenType";
    public static final String FIELD_CLOSE_TYPE = "CloseType";
    public static final String FIELD_PAGE_NAME = "PageName";
    public static final String FIELD_PLAY_OPER_TYPE = "PlayOperType";
    public static final String FIELD_VIDEO_NAME = "VideoName";
    public static final String FIELD_VOLUME_RESULT = "VolumeResult";

    /**
     * 应用打开事件
     */
    public static final String KEY_APP_OPEN = "App_Open";

    /**
     * 应用关闭事件
     */
    public static final String KEY_APP_CLOSE = "App_Close";

    /**
     * 应用打开方式
     */
    public static final String FILED_OPSMODE = "OpsMode";

    /**
     * 应用关闭方式
     */
    public static final String FILED_CLSMODE = "ClsMode";

    /**
     * 功能编码
     */
    private static final String LOCATION_ID = "ZC040F0F";
    /**
     * 应用编码
     */
    private static final String APP_ID = "ZC04";

    /**
     * 视频-语音打开/关闭
     * USB视频-语音打开，是不会直接打开到播放界面的。是打开到列表
     */
    public static final String FIELD_VOICE_OPEN_CLOSE = "1";

    /**
     * 视频-点击打开/关闭
     */
    public static final String FIELD_CLICK_OPEN_CLOSE = "2";


    /**
     * 视频 播放
     */
    public static final String FIELD_PLAY = "1";
    /**
     * 视频 暂停
     */
    public static final String FIELD_PAUSE = "2";
    /**
     * 视频 上一曲
     */
    public static final String FIELD_PRE = "3";
    /**
     * 视频 下一曲
     */
    public static final String FIELD_NEXT = "4";


    private static final int T22_MODEL_CODE = 2;
    private static final int T19C_MODEL_CODE = 7;
    private static final int T18FL3_MODEL_CODE = 10;
    private static final int SAUDI_COUNTRY_CODE = 29;


    private TrackingPresenter point;

    public void initialize(Context mContext) {
        isSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity();
        isSaudi = CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION) == SAUDI_COUNTRY_CODE;
        Log.i(TAG, "initialize() called with: mContext = [" + mContext + "], isSecurity = [" + isSecurity + "]"+" ; isSaudi = "+isSaudi);
        point = new TrackingPresenter(mContext, "Video", "desaysv");
    }


    public void sendLionPoint(String keyName, String field, String filedValue) {
        if (null == point) {
            Log.e(TAG, "sendLionPoint: point is null");
            return;
        }

        Log.d(TAG, "sendLionPoint: keyName "+keyName+" ; field "+field+" ; filedValue "+filedValue+" ; isSecurity "+isSecurity+" ; isSaudi "+isSaudi);
        if (isSecurity || isSaudi) {
            if (!(LION_OPEN_CLICK.equals(keyName) || LION_CLOSE_CLICK.equals(keyName))) {
                return;
            }
        }

        TrackingInfo trackingInfo = new TrackingInfo(keyName, TrackingEventType.APPLICATION_USAGE, isSecurity, field, filedValue);
        Log.d(TAG, "sendLionPoint: "+trackingInfo);
        point.event(trackingInfo);
    }

    /**
     * 新增平台化埋点方案，目前只适用 isT1E24_INT || isT1E24_PHEV_INT
     *
     * @param attributeId  属性编码 -- OpsMode
     * @param attributeValue  属性值 -- 1
     * @param eventId     事件编码  -- App_Open
     */
    public void sendPlatformLionPoint(String attributeId, String attributeValue, String eventId) {
        if (null == point) {
            Log.e(TAG, "sendPlatformLionPoint: point is null");
            return;
        }
        AttributeInfo attributeInfo = new AttributeInfo(attributeId, LOCATION_ID, attributeValue);
        EventInfo eventInfo = new EventInfo(APP_ID, eventId, TrackingEventType.APPLICATION_USAGE, attributeInfo);
        Log.d(TAG, "sendPlatformLionPoint eventInfo: " + eventInfo);
        point.event(eventInfo);
    }

    public void unInitialize() {
        if (null != point) {
            point.release();
        }
    }
}
