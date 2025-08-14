package com.desaysv.libradio.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.rds.RDSRadioName;
import com.desaysv.libradio.datebase.RadioDataBaseKey;
import com.desaysv.libradio.datebase.SQLOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2019-8-9.
 * Comment 列表保存的工具类，主要实现数据的插入和删除
 */
public class RadioMessageSQLUtils {

    private static final String TAG = "RadioMessageSQLUtils";

    private SQLOpenHelper mSQLOpenHelper;
    private Context mContect;
    private SQLiteDatabase mDatabase;

    //开启一个子线程来存储数据库
    private Handler mHandler;

    private String tableName = "RADIO_MESSAGE_TABLE";

    private static RadioMessageSQLUtils instance;

    public static RadioMessageSQLUtils getInstance() {
        if (instance == null) {
            synchronized (RadioMessageSQLUtils.class) {
                if (instance == null) {
                    instance = new RadioMessageSQLUtils();
                }
            }
        }
        return instance;
    }

    private RadioMessageSQLUtils(){
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    /**
     * 初始化
     *
     * @param context AppBase.mContext
     */
    void initialize(Context context) {
        mContect = context;
        mSQLOpenHelper = SQLOpenHelper.getInstance(context);
        mDatabase = mSQLOpenHelper.getWritableDatabase();
    }

    /**
     * 保存列表，这里的数据保存已经做在子线程了
     *
     * @param type             AM_COLLECT; AM_EFFECT; FM_COLLECT; FM_EFFECT;
     * @param radioMessageList 需要保存的收音列表
     */
    void saveList(final String type, final List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveList: type = " + type + " radioMessageList = " + radioMessageList);
        deleteData(new String[]{RadioDataBaseKey.TYPE}, new String[]{type});
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //add by lzm 之前谢玉说这里会出现ConcurrentModificationException的异常，所以加入一个对象锁，看是否能够规避
                synchronized (radioMessageList){
                    mDatabase.beginTransaction();
                    for (RadioMessage radioMessage : radioMessageList) {
                        insertData(type, radioMessage);
                    }
                    mDatabase.setTransactionSuccessful();
                    mDatabase.endTransaction();
                }
            }
        });

    }

    /**
     * 获取收音列表，这里是要看调用这个方法是在那个线程，不允许在主线程调用，因为数据库涉及到了IO操作
     *
     * @param type AM_COLLECT; AM_EFFECT; FM_COLLECT; FM_EFFECT;
     * @return
     */
    List<RadioMessage> getList(String type) {
        List<RadioMessage> radioMessageList = new ArrayList<>();
        radioMessageList.clear();

        Cursor cursor = mDatabase.query(tableName, null, RadioDataBaseKey.TYPE + "=?",
                new String[]{type}, null, null, null);
        if (cursor != null) {
            if (RadioListSaveUtils.DAB_COLLECT.equals(type) || RadioListSaveUtils.DAB_EFFECT.equals(type) || RadioListSaveUtils.DAB_PLAY.equals(type)){
                while (cursor.moveToNext()) {
                    int frequency = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.FREQUENCY));
                    long serviceId = cursor.getLong(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_ID));
                    int serviceComponentId = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_COMPONENT_ID));

                    DABMessage dabMessage = new DABMessage(frequency,serviceId,serviceComponentId);

                    dabMessage.setProgramStationName(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.PROGRAM_STATION_NAME)));
                    dabMessage.setEnsembleId(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.ENSEMBLE_ID)));
                    dabMessage.setSubServiceFlag(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.SUB_SERVICE_FLAG)));
                    dabMessage.setEnsembleLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.ENSEMBLE_LABEL)));
                    dabMessage.setProgramType(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.PROGRAM_TYPE)));
                    dabMessage.setDynamicLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.DYNAMIC_LABEL)));
                    dabMessage.setDynamicPlusLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.DYNAMIC_PLUS_LABEL)));
                    dabMessage.setShortProgramStationName(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.PROGRAM_STATION_SHORT_NAME)));
                    dabMessage.setShortEnsembleLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.ENSEMBLE_LABEL_SHORT_NAME)));
                    RadioMessage radioMessage = new RadioMessage(dabMessage);
                    radioMessageList.add(radioMessage);
                }
            }else if (RadioListSaveUtils.ALL_COLLECT.equals(type)){
                while (cursor.moveToNext()) {
                    long serviceIdFirst = cursor.getLong(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_ID));
                    Log.d(TAG, "ALL_COLLECT,serviceId:" + serviceIdFirst);
                    RadioMessage radioMessage = new RadioMessage();
                    if (serviceIdFirst != 0) {
                        int frequency = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.FREQUENCY));
                        long serviceId = cursor.getLong(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_ID));
                        int serviceComponentId = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_COMPONENT_ID));
                        DABMessage dabMessage = new DABMessage(frequency, serviceId, serviceComponentId);
                        dabMessage.setProgramStationName(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.PROGRAM_STATION_NAME)));
                        dabMessage.setEnsembleId(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.ENSEMBLE_ID)));
                        dabMessage.setSubServiceFlag(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.SUB_SERVICE_FLAG)));
                        dabMessage.setEnsembleLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.ENSEMBLE_LABEL)));
                        dabMessage.setProgramType(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.PROGRAM_TYPE)));
                        dabMessage.setDynamicLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.DYNAMIC_LABEL)));
                        dabMessage.setDynamicPlusLabel(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.DYNAMIC_PLUS_LABEL)));
                        dabMessage.setShortProgramStationName(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.PROGRAM_STATION_SHORT_NAME)));
                        radioMessage.setDabMessage(dabMessage);
                        radioMessage.setRadioType(RadioMessage.DAB_TYPE);
                    } else {
                        radioMessage.setRadioBand(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.BAND)));
                        radioMessage.setRadioFrequency(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.FREQUENCY)));
                    }
                    radioMessageList.add(radioMessage);
                }

            }else {
                while (cursor.moveToNext()) {
                    RadioMessage radioMessage = new RadioMessage();
                    radioMessage.setRadioBand(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.BAND)));
                    radioMessage.setRadioFrequency(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.FREQUENCY)));
                    radioMessageList.add(radioMessage);
                }
            }

            cursor.close();
        }
        Log.d(TAG, "getList: type = " + type + " radioMessageList = " + radioMessageList);
        Log.d(TAG, "getList: radioMessageList.size = " + radioMessageList.size());
        return radioMessageList;
    }

    /**
     * 在某个列表中插入数据
     *
     * @param type         AM_COLLECT; AM_EFFECT; FM_COLLECT; FM_EFFECT;
     * @param radioMessage 需要保存在列表中的收音信息
     */
    private void insertData(String type, RadioMessage radioMessage) {
        Log.d(TAG, "insertData: type = " + type);
        if (RadioListSaveUtils.DAB_COLLECT.equals(type) || RadioListSaveUtils.DAB_EFFECT.equals(type) || RadioListSaveUtils.DAB_PLAY.equals(type)){
            if (radioMessage == null || radioMessage.getDabMessage() == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(RadioDataBaseKey.TYPE, type);
            values.put(RadioDataBaseKey.BAND, radioMessage.getRadioBand());
            values.put(RadioDataBaseKey.FREQUENCY, radioMessage.getDabMessage().getFrequency());
            values.put(RadioDataBaseKey.ENSEMBLE_ID, radioMessage.getDabMessage().getEnsembleId());
            values.put(RadioDataBaseKey.PROGRAM_STATION_NAME, radioMessage.getDabMessage().getProgramStationName());
            values.put(RadioDataBaseKey.ENSEMBLE_LABEL, radioMessage.getDabMessage().getEnsembleLabel());
            values.put(RadioDataBaseKey.PROGRAM_TYPE, radioMessage.getDabMessage().getProgramType());
            values.put(RadioDataBaseKey.SERVICE_ID, radioMessage.getDabMessage().getServiceId());
            values.put(RadioDataBaseKey.SERVICE_COMPONENT_ID, radioMessage.getDabMessage().getServiceComponentId());
            values.put(RadioDataBaseKey.DYNAMIC_LABEL, radioMessage.getDabMessage().getDynamicLabel());
            values.put(RadioDataBaseKey.DYNAMIC_PLUS_LABEL, radioMessage.getDabMessage().getDynamicPlusLabel());
            values.put(RadioDataBaseKey.PROGRAM_STATION_SHORT_NAME, radioMessage.getDabMessage().getShortProgramStationName());
            values.put(RadioDataBaseKey.ENSEMBLE_LABEL_SHORT_NAME, radioMessage.getDabMessage().getShortEnsembleLabel());
            values.put(RadioDataBaseKey.SUB_SERVICE_FLAG, radioMessage.getDabMessage().getSubServiceFlag());
            mDatabase.insert(tableName, null, values);
        }else if (RadioListSaveUtils.ALL_COLLECT.equals(type)){
            if (radioMessage == null) {
                return;
            }
            if (radioMessage.getDabMessage() != null) {
                ContentValues values = new ContentValues();
                values.put(RadioDataBaseKey.TYPE, type);
                values.put(RadioDataBaseKey.BAND, radioMessage.getRadioBand());
                values.put(RadioDataBaseKey.FREQUENCY, radioMessage.getDabMessage().getFrequency());
                values.put(RadioDataBaseKey.ENSEMBLE_ID, radioMessage.getDabMessage().getEnsembleId());
                values.put(RadioDataBaseKey.PROGRAM_STATION_NAME, radioMessage.getDabMessage().getProgramStationName());
                values.put(RadioDataBaseKey.ENSEMBLE_LABEL, radioMessage.getDabMessage().getEnsembleLabel());
                values.put(RadioDataBaseKey.PROGRAM_TYPE, radioMessage.getDabMessage().getProgramType());
                values.put(RadioDataBaseKey.SERVICE_ID, radioMessage.getDabMessage().getServiceId());
                values.put(RadioDataBaseKey.SERVICE_COMPONENT_ID, radioMessage.getDabMessage().getServiceComponentId());
                values.put(RadioDataBaseKey.DYNAMIC_LABEL, radioMessage.getDabMessage().getDynamicLabel());
                values.put(RadioDataBaseKey.DYNAMIC_PLUS_LABEL, radioMessage.getDabMessage().getDynamicPlusLabel());
                values.put(RadioDataBaseKey.PROGRAM_STATION_SHORT_NAME, radioMessage.getDabMessage().getShortProgramStationName());
                values.put(RadioDataBaseKey.SUB_SERVICE_FLAG, radioMessage.getDabMessage().getSubServiceFlag());
                mDatabase.insert(tableName, null, values);
            }else {
                ContentValues values = new ContentValues();
                values.put(RadioDataBaseKey.TYPE, type);
                values.put(RadioDataBaseKey.BAND, radioMessage.getRadioBand());
                values.put(RadioDataBaseKey.FREQUENCY, radioMessage.getRadioFrequency());
                mDatabase.insert(tableName, null, values);
            }
        } else {
            if (radioMessage == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(RadioDataBaseKey.TYPE, type);
            values.put(RadioDataBaseKey.BAND, radioMessage.getRadioBand());
            values.put(RadioDataBaseKey.FREQUENCY, radioMessage.getRadioFrequency());
            mDatabase.insert(tableName, null, values);
        }
    }

    /**
     * 删除列表里面的数据
     */
    private void deleteData(String[] keys, String[] values) {
        String sql = "";
        if (keys == null) {
            return;
        }
        if (keys.length == 1) {
            sql = sql + keys[0] + "=?";
        } else if (keys.length > 1) {
            for (int i = 0; i < keys.length; i++) {
                if (i == keys.length - 1) {
                    sql = sql + "=?and ";
                } else if (i == 0) {
                    sql = keys[i] + "=?";
                } else {
                    sql = sql + "=?";
                }
            }
        }
        mDatabase.delete(tableName, sql, values);
    }


    /**
     * 获取已经订阅的EPG列表
     * @return
     */
    public List<DABEPGSchedule> getSubscribeEPGList(){
        List<DABEPGSchedule> subscribeList = new ArrayList<>();
        Cursor cursor = mDatabase.query(RadioDataBaseKey.TABLE_EPG, null, null,null,null,null,"null");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                DABEPGSchedule dabepgSchedule = new DABEPGSchedule();
                dabepgSchedule.setServiceId(cursor.getLong(cursor.getColumnIndex(RadioDataBaseKey.EPG_SERVICE_ID)));
                dabepgSchedule.setFreq(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.EPG_FREQ)));
                dabepgSchedule.setServiceComponentId(cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.EPG_SERVICE_COMPONENT_ID)));
                dabepgSchedule.setProgramName(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_PROGRAM_NAME)));
                dabepgSchedule.setYear(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_YEAR)));
                dabepgSchedule.setMonth(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_MONTH)));
                dabepgSchedule.setDay(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_DAY)));
                dabepgSchedule.setHour(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_HOUR)));
                dabepgSchedule.setMin(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_MIN)));
                dabepgSchedule.setSec(cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.EPG_SEC)));
                subscribeList.add(dabepgSchedule);
            }
            cursor.close();
        }
        Log.d(TAG, "getSubscribeEPGList: subscribeList = " + subscribeList);
        return subscribeList;
    }

    /**
     * 保存或删除订阅数据
     * @param dabepgSchedule 订阅的EPG
     * @param subscribe true:订阅; false:取消订阅
     */
    public void saveSubscribe(DABEPGSchedule dabepgSchedule, boolean subscribe){
        if (dabepgSchedule == null){
            return;
        }
        Log.d(TAG,"saveSubscribe:"+subscribe);
        if (subscribe){
            ContentValues values = new ContentValues();
            values.put(RadioDataBaseKey.EPG_SERVICE_ID, dabepgSchedule.getServiceId());
            values.put(RadioDataBaseKey.EPG_FREQ, dabepgSchedule.getFreq());
            values.put(RadioDataBaseKey.EPG_SERVICE_COMPONENT_ID, dabepgSchedule.getServiceComponentId());
            values.put(RadioDataBaseKey.EPG_PROGRAM_NAME, dabepgSchedule.getProgramName());
            values.put(RadioDataBaseKey.EPG_YEAR, dabepgSchedule.getYear());
            values.put(RadioDataBaseKey.EPG_MONTH, dabepgSchedule.getMonth());
            values.put(RadioDataBaseKey.EPG_DAY, dabepgSchedule.getDay());
            values.put(RadioDataBaseKey.EPG_HOUR, dabepgSchedule.getHour());
            values.put(RadioDataBaseKey.EPG_MIN, dabepgSchedule.getMin());
            values.put(RadioDataBaseKey.EPG_SEC, dabepgSchedule.getSec());
            mDatabase.insert(RadioDataBaseKey.TABLE_EPG, null, values);
        }else {
            String sql = RadioDataBaseKey.EPG_SERVICE_ID + "=? And "
                    + RadioDataBaseKey.EPG_FREQ + "=? And "
                    + RadioDataBaseKey.EPG_SERVICE_COMPONENT_ID + "=? And "
                    + RadioDataBaseKey.EPG_PROGRAM_NAME + "=? And "
                    + RadioDataBaseKey.EPG_YEAR + "=? And "
                    + RadioDataBaseKey.EPG_MONTH + "=? And "
                    + RadioDataBaseKey.EPG_DAY + "=? And "
                    + RadioDataBaseKey.EPG_HOUR + "=? And "
                    + RadioDataBaseKey.EPG_MIN + "=? And "
                    + RadioDataBaseKey.EPG_SEC + "=?";

            String[] values = new String[]{String.valueOf(dabepgSchedule.getServiceId()), String.valueOf(dabepgSchedule.getFreq()), String.valueOf(dabepgSchedule.getServiceComponentId())
                    ,dabepgSchedule.getProgramName(),
                    dabepgSchedule.getYear(),dabepgSchedule.getMonth(),dabepgSchedule.getDay(),dabepgSchedule.getHour(),dabepgSchedule.getMin(),dabepgSchedule.getSec()};

            mDatabase.delete(RadioDataBaseKey.TABLE_EPG,sql,values);
        }
    }



    /**
     * 获取搜索历史
     * @return
     */
    public List<String> getRadioSearchHistoryList(){
        List<String> searchList = new ArrayList<>();
        Cursor cursor = mDatabase.query(RadioDataBaseKey.TABLE_SEARCH, null, null,null,null,null,null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String searchName = cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.RADIO_SEARCH_NAME));
                searchList.add(searchName);
            }
            cursor.close();
        }
        Log.d(TAG, "getRadioSearchList: searchList = " + searchList);
        return searchList;
    }

    /**
     * 保存当前最新搜索历史
     * @param currentHistoryList 当前列表
     */
    public void saveOrUpdateRadioSearchHistory(List<String> currentHistoryList){
        Log.d(TAG,"saveOrUpdateRadioSearchHistory,currentHistoryList:"+currentHistoryList);
        deleteRadioSearchHistory();//每次都只保存当前最新的列表，因此清空原有数据
        for (String name : currentHistoryList){
            ContentValues values = new ContentValues();
            values.put(RadioDataBaseKey.RADIO_SEARCH_NAME, name);
            mDatabase.insert(RadioDataBaseKey.TABLE_SEARCH, null, values);
        }
    }

    public void deleteRadioSearchHistory(){
        mDatabase.delete(RadioDataBaseKey.TABLE_SEARCH,null,null);
    }

    /**
     * 获取当前的RDS名称列表
     * @return
     */
    public List<RDSRadioName> getRDSNameList(){
        List<RDSRadioName> rdsRadioNameList = new ArrayList<>();
        Cursor cursor = mDatabase.query(RadioDataBaseKey.TABLE_RDS_NAME, null, null,null,null,null,null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int freq = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.FREQUENCY));
                String rdsName = cursor.getString(cursor.getColumnIndex(RadioDataBaseKey.RDS_STATION_NAME));
                RDSRadioName rdsRadioName = new RDSRadioName();
                rdsRadioName.setFrequency(freq);
                rdsRadioName.setProgramStationName(rdsName);
                rdsRadioNameList.add(rdsRadioName);
            }
            cursor.close();
        }
        Log.d(TAG, "getRDSNameList: rdsRadioNameList = " + rdsRadioNameList);
        return rdsRadioNameList;
    }

    /**
     * 更新当前RDS名称列表
     * @param currentRDSNameList 当前列表
     */
    public void updateRdsNameList(List<RDSRadioName> currentRDSNameList){
        Log.d(TAG,"updateRdsName:"+currentRDSNameList);
        deleteRDSNameList();//每次都只保存当前最新的列表，因此清空原有数据
        for (RDSRadioName name : currentRDSNameList){
            ContentValues values = new ContentValues();
            values.put(RadioDataBaseKey.FREQUENCY, name.getFrequency());
            values.put(RadioDataBaseKey.RDS_STATION_NAME, name.getProgramStationName());
            mDatabase.insert(RadioDataBaseKey.TABLE_RDS_NAME, null, values);
        }
    }

    public void deleteRDSNameList(){
        mDatabase.delete(RadioDataBaseKey.TABLE_RDS_NAME,null,null);
    }



    /**
     * 获取当前的DAB logo列表
     * @return
     */
    public List<DABLogo> getDABLogoList(){
        List<DABLogo> DABLogoList = new ArrayList<>();
        Cursor cursor = mDatabase.query(RadioDataBaseKey.TABLE_DAB_LOGO, null, null,null,null,null,null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int freq = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.FREQUENCY));
                long serviceId =  cursor.getLong(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_ID));
                int serviceComponentId = cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.SERVICE_COMPONENT_ID));
                int logolen =  cursor.getInt(cursor.getColumnIndex(RadioDataBaseKey.DAB_LOGO_LEN));
                byte[] logoData = cursor.getBlob(cursor.getColumnIndex(RadioDataBaseKey.DAB_LOGO_DATA));
                DABLogo dabLogo = new DABLogo();
                dabLogo.setFrequency(freq);
                dabLogo.setServiceId(serviceId);
                dabLogo.setServiceComponentId(serviceComponentId);
                dabLogo.setLogoLen(logolen);
                dabLogo.setLogoDataList(logoData);
                DABLogoList.add(dabLogo);
            }
            cursor.close();
        }
        Log.d(TAG, "getDABLogoList: DABLogoList = " + DABLogoList);
        return DABLogoList;
    }

    /**
     * 更新当前DABlogo列表
     * @param currentDABLogoList 当前列表
     */
    public void updateDABLogoList(List<DABLogo> currentDABLogoList){
        Log.d(TAG,"updateDABLogoList:"+currentDABLogoList);
        deleteDABLogoList(currentDABLogoList);//每次都只保存当前最新的列表，因此清空原有数据
        for (DABLogo dabLogo : currentDABLogoList){
            ContentValues values = new ContentValues();
            values.put(RadioDataBaseKey.FREQUENCY, dabLogo.getFrequency());
            values.put(RadioDataBaseKey.SERVICE_ID, dabLogo.getServiceId());
            values.put(RadioDataBaseKey.SERVICE_COMPONENT_ID, dabLogo.getServiceComponentId());
            values.put(RadioDataBaseKey.DAB_LOGO_LEN, dabLogo.getLogoLen());
            values.put(RadioDataBaseKey.DAB_LOGO_DATA, dabLogo.getLogoDataList());
            mDatabase.insert(RadioDataBaseKey.TABLE_DAB_LOGO, null, values);
        }
    }

    public void deleteDABLogoList(List<DABLogo> currentDABLogoList){
        if (currentDABLogoList != null && currentDABLogoList.size() > 0) {
            mDatabase.delete(RadioDataBaseKey.TABLE_DAB_LOGO, null, null);
        }
    }
}
