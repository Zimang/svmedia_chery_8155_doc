package com.desaysv.modulebtmusic.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.tracker.ITracker;
import com.desaysv.modulebtmusic.tracker.TrackerRussiaImpl;
import com.desaysv.modulebtmusic.tracker.TrackerSaudiImpl;
import com.desaysv.presenter.tracking.common.AttributeInfo;
import com.desaysv.presenter.tracking.common.EventInfo;
import com.desaysv.presenter.tracking.common.TrackingEventType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 蓝牙音乐数据埋点管理类
 */
public class BTMusicDataServiceManager {
    private static final String TAG = Constants.TAG + "BTMusicDataServiceManager";
    private static volatile BTMusicDataServiceManager mInstance;

    private static final String PROPERTY_KEY_NAME = "keyName";
    private static final String PROPERTY_CONTENT = "content";
    private static final String PROPERTY_APP_ID = "app_id";
    private static final String PROPERTY_SOURCE = "source";
    private static final String PROPERTY_TIMESTAMP = "Timestamp";

    //KeyName_T19C
    public static final String KEY_NAME_OPEN_CLOSE = "BTMusic.N.N.N.OpenCloseClick";//打开关闭蓝牙音乐事件
    public static final String KEY_NAME_MUSIC_OPERATE = "BTMusic.N.N.N.MusicOperate";//音乐操作事件
    public static final String KEY_NAME_SONG_LIST = "BTMusic.SongList.N.SongList";//查看蓝牙音乐列表事件
    public static final String KEY_NAME_OPEN_BT_MUSIC = "BTMusic.N.N.N.OpenClick";//打开蓝牙音乐事件
    public static final String KEY_NAME_CLOSE_BT_MUSIC = "BTMusic.N.N.N.CloseClick";//关闭蓝牙音乐事件

    //KeyName_T22INT
    public static final String KEY_NAME_MUSIC_OPERATE_T22INT = "Changting.BTMusic.N.N.MusicOperate";//音乐操作事件
    public static final String KEY_NAME_OPEN_BT_MUSIC_T22INT = "Changting.BTMusic.N.N.OpenClick";//打开蓝牙音乐事件
    public static final String KEY_NAME_CLOSE_BT_MUSIC_T22INT = "Changting.BTMusic.N.N.CloseClick";//关闭蓝牙音乐事件

    //Field_T19CINT
    public static final String FIELD_OPER_TYPE = "OperType";
    public static final String FIELD_OPER_STYLE = "OperStyle";
    public static final String FIELD_PLAY_OPER_TYPE = "PlayOperType";
    public static final String FIELD_PROGRAM_NAME = "ProgramName";
    public static final String FIELD_AUTHOR = "Author";
    public static final String FIELD_OPEN_TYPE = "OpenType";
    public static final String FIELD_CLOSE_TYPE = "CloseType";

    //Field_T22INT
    public static final String FIELD_OPER_STYLE_T22INT = "OperStyle";
    public static final String FIELD_PLAY_OPER_TYPE_T22INT = "PlayOperType";
    public static final String FIELD_PROGRAM_NAME_T22INT = "ProgramName";
    public static final String FIELD_AUTHOR_T22INT = "Author";
    public static final String FIELD_OPEN_TYPE_T22INT = "OpenType";
    public static final String FIELD_CLOSE_TYPE_T22INT = "CloseType";

    //Value
    public static final String VALUE_OPEN = "0"; //BT_MUSIC的开启
    public static final String VALUE_CLOSE = "1"; //BT_MUSIC的关闭
    public static final String VALUE_POWER_VOICE = "1"; //BT_MUSIC开启和关闭的操作方式--语音,OpenType使用
    public static final String VALUE_POWER_CLICK = "2"; //BT_MUSIC开启和关闭的操作方式--点击,OpenType使用
    public static final String VALUE_OPER_CLICK = "0";  //操作方式--点击,OperType使用
    public static final String VALUE_OPER_VOICE = "1";  //操作方式--语音,OperType使用
    public static final String VALUE_OPER_PLAY = "1";  //BT_MUSIC的操作类型--播放
    public static final String VALUE_OPER_PAUSE = "2";  //BT_MUSIC的操作类型--暂停
    public static final String VALUE_OPER_PREVIOUS = "3";  //BT_MUSIC的操作类型--上一首
    public static final String VALUE_OPER_NEXT = "4";  //BT_MUSIC的操作类型--下一首

    /**
     * T22 model code
     */
    private static final int T22_MODEL_CODE = 2;
    /**
     * T19C model code
     */
    private static final int T19C_MODEL_CODE = 7;
    /**
     * T18FL3 model code
     */
    private static final int T18FL3_MODEL_CODE = 10;
    /**
     * 沙特国家码
     */
    private static final int SAUDI_COUNTRY_CODE = 29;

    //应用ID
    private static final String ID = "ZG02";
    //模块ID
    private static final String LOCATION_ID_1 = "ZG020F0F";

    //Event_Id
    private static final String APP_OPEN = "App_Open";
    private static final String APP_CLOSE = "App_Close";

    //Attribute_Id
    private static final String OPS_MODE = "OpsMode";
    private static final String OPS_TIME = "OpsTime";
    private static final String CLS_MODE = "ClsMode";
    private static final String CLS_TIME = "ClsTime";

    //AttributeValue
    private static final String OPS_CLICK = "2";

    //埋点类型
    private static final TrackingEventType APPLICATION_USAGE = TrackingEventType.APPLICATION_USAGE;

    private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private ITracker mTrackerImpl;

    public static BTMusicDataServiceManager getInstance() {
        if (mInstance == null) {
            synchronized (BTMusicDataServiceManager.class) {
                if (mInstance == null) {
                    mInstance = new BTMusicDataServiceManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        if (context == null) {
            Log.i(TAG, "initialize: failed, context is null");
            return;
        }
        Log.i(TAG, "initialize: isT19CINTSeries = " + isT19CINTSeries() + " ,isT22INTSeries = " + isT22INTSeries());
        CarConfigUtil.getDefault().init(context);
        mTrackerImpl = getTrackerStrategyImpl(context);
        Log.i(TAG, "initialize: finish");
    }

    private ITracker getTrackerStrategyImpl(Context context) {
        int modelCode = CarConfigUtil.getDefault().getConfig(com.desaysv.ivi.extra.project.carconfig.Constants.ID_MODEL_CODE);
        int countryCode = CarConfigUtil.getDefault().getConfig(com.desaysv.ivi.extra.project.carconfig.Constants.ID_COUNTRY_OR_REGION);
        Log.d(TAG, "getTrackerStrategyImpl: countryCode = " + countryCode + " ,modelCode = " + modelCode);
//        if (SAUDI_COUNTRY_CODE == countryCode) {}
        if (T18FL3_MODEL_CODE == modelCode || T19C_MODEL_CODE == modelCode || T22_MODEL_CODE == modelCode) {
            // T18FL3/T19C/T22的沙特
            return new TrackerSaudiImpl(context);
        }
        return new TrackerRussiaImpl(context);
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.i(TAG, "release: ");
        if (mTrackerImpl != null) {
            mTrackerImpl.release();
        }
        Log.i(TAG, "release: finish");
    }

    private boolean isT19CINTSeries() {
        return CarConfigUtil.getDefault().isT19C_INT() || CarConfigUtil.getDefault().isT19CEV_INT() || CarConfigUtil.getDefault().isT18FL3_INT()
                || CarConfigUtil.getDefault().isT26_INT() || CarConfigUtil.getDefault().isT1E24_INT() || CarConfigUtil.getDefault().isT1E24_PHEV_INT();
    }

    private boolean isT22INTSeries() {
        return CarConfigUtil.getDefault().isT22_INT() || CarConfigUtil.getDefault().isT18P_INT();
    }

    /**
     * BTMusic开启事件公共埋点
     */
    public void trackEventOpen(String openTypeValue, String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_OPEN_CLOSE,
//                    BTMusicDataServiceManager.FIELD_OPER_STYLE, operStyleValue);
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_OPEN_CLOSE,
//                    BTMusicDataServiceManager.FIELD_OPER_TYPE, VALUE_OPEN);
//        }
        trackEventOpenType(openTypeValue);
    }

    /**
     * BTMusic关闭事件公共埋点
     */
    public void trackEventClose(String closeTypeValue, String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_OPEN_CLOSE,
//                    BTMusicDataServiceManager.FIELD_OPER_STYLE, operStyleValue);
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_OPEN_CLOSE,
//                    BTMusicDataServiceManager.FIELD_OPER_TYPE, VALUE_CLOSE);
//        }
        trackEventCloseType(closeTypeValue);
    }

    /**
     * BTMusic播放事件公共埋点
     */
    public void trackEventPlay(String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE, VALUE_OPER_PLAY);
//            return;
//        } else if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE_T22INT, VALUE_OPER_PLAY);
//        }
//        trackEventOperStyle(operStyleValue);
    }

    /**
     * BTMusic暂停事件公共埋点
     */
    public void trackEventPause(String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE, VALUE_OPER_PAUSE);
//            return;
//        } else if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE_T22INT, VALUE_OPER_PAUSE);
//        }
//        trackEventOperStyle(operStyleValue);
    }

    /**
     * BTMusic下一首事件公共埋点
     */
    public void trackEventNext(String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE, VALUE_OPER_NEXT);
//            return;
//        } else if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE_T22INT, VALUE_OPER_NEXT);
//        }
//        trackEventOperStyle(operStyleValue);
    }

    /**
     * BTMusic上一首事件公共埋点
     */
    public void trackEventPrevious(String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE, VALUE_OPER_PREVIOUS);
//            return;
//        } else if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_PLAY_OPER_TYPE_T22INT, VALUE_OPER_PREVIOUS);
//        }
//        trackEventOperStyle(operStyleValue);
    }

    /**
     * BTMusic音乐名称公共埋点
     */
    public void trackEventProgramName(String mediaTitle) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_PROGRAM_NAME, mediaTitle);
//        }
//        if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_PROGRAM_NAME_T22INT, mediaTitle);
//        }
    }

    /**
     * BTMusic作者名称公共埋点
     */
    public void trackEventAuthor(String author) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_AUTHOR, author);
//        } else
//        if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_AUTHOR_T22INT, author);
//        }
    }

    /**
     * 操作方式
     * 0--点击
     * 1--语音
     */
    public void trackEventOperStyle(String operStyleValue) {
//        if (isT19CINTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE,
//                    BTMusicDataServiceManager.FIELD_OPER_STYLE, operStyleValue);
//        }
//        if (isT22INTSeries()) {
//            trackEvent(BTMusicDataServiceManager.KEY_NAME_MUSIC_OPERATE_T22INT,
//                    BTMusicDataServiceManager.FIELD_OPER_STYLE_T22INT, operStyleValue);
//        }
    }

    public void trackEventOpenType(String openTypeValue) {
        if (isT19CINTSeries()) {
            AttributeInfo attributeInfoMode = new AttributeInfo(OPS_MODE, LOCATION_ID_1, OPS_CLICK);
            AttributeInfo attributeInfoTime = new AttributeInfo(OPS_TIME, LOCATION_ID_1, mFormat.format(new Date()));
            trackEventPlatform(ID, APP_OPEN, APPLICATION_USAGE, attributeInfoMode);
            trackEventPlatform(ID, APP_OPEN, APPLICATION_USAGE, attributeInfoTime);
            trackEvent(BTMusicDataServiceManager.KEY_NAME_OPEN_BT_MUSIC,
                    BTMusicDataServiceManager.FIELD_OPEN_TYPE, openTypeValue);
        } else if (isT22INTSeries()) {
            trackEvent(BTMusicDataServiceManager.KEY_NAME_OPEN_BT_MUSIC,
                    BTMusicDataServiceManager.FIELD_OPEN_TYPE_T22INT, openTypeValue);
        }
    }

    public void trackEventCloseType(String closeTypeValue) {
        if (isT19CINTSeries()) {
            AttributeInfo attributeInfoMode = new AttributeInfo(CLS_MODE, LOCATION_ID_1, OPS_CLICK);
            AttributeInfo attributeInfoTime = new AttributeInfo(CLS_TIME, LOCATION_ID_1, mFormat.format(new Date()));
            trackEventPlatform(ID, APP_CLOSE, APPLICATION_USAGE, attributeInfoMode);
            trackEventPlatform(ID, APP_CLOSE, APPLICATION_USAGE, attributeInfoTime);
            trackEvent(BTMusicDataServiceManager.KEY_NAME_CLOSE_BT_MUSIC,
                    BTMusicDataServiceManager.FIELD_CLOSE_TYPE, closeTypeValue);
        } else if (isT22INTSeries()) {
            trackEvent(BTMusicDataServiceManager.KEY_NAME_CLOSE_BT_MUSIC,
                    BTMusicDataServiceManager.FIELD_CLOSE_TYPE_T22INT, closeTypeValue);
        }
    }

    /**
     * 添加德赛操作数据
     */
    public void trackEvent(String keyName, String field, String value) {
        Log.d(TAG, "addDesayEventData() called, keyName = " + keyName + ", field = " + field + ", value = " + value + " 20240428");
        if (mTrackerImpl == null) {
            Log.e(TAG, "trackEvent: mTrackerImpl is null");
            return;
        }
        if (CarConfigUtil.getDefault().isNeedSecurity()) {
            if (!TextUtils.equals(KEY_NAME_OPEN_BT_MUSIC, keyName) && !TextUtils.equals(KEY_NAME_CLOSE_BT_MUSIC, keyName)) {
                Log.d(TAG, "Return Unexpected KeyName, keyName = " + keyName + ", field = " + field + ", value = " + value);
                return;
            }
        }
        mTrackerImpl.trackEvent(keyName, field, value);
    }

    /**
     * 添加德赛操作数据(平台)
     * 当前使用的项目:T1E24
     */
    //埋点事件对象，参数依次为：应用编码、事件编码、埋点类型、功能编码、属性编码、属性值
    private void trackEventPlatform(String id, String eventId, TrackingEventType trackingEventType, AttributeInfo attributeInfo) {
        if (mTrackerImpl == null) {
            Log.w(TAG, "trackEventPlatform: mTrackerImpl is null");
            return;
        }
        Log.d(TAG, "trackEventPlatform: id = " + id + " ,eventId = " + eventId + " ,trackingEventType = " + trackingEventType + " ,attributeInfo = " + attributeInfo.toString());
        EventInfo eventInfo = new EventInfo(id, eventId, trackingEventType, attributeInfo);
        mTrackerImpl.trackEventPlatform(eventInfo);
    }
}
