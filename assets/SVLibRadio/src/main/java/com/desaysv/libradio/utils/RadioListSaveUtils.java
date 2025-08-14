package com.desaysv.libradio.utils;

import android.content.Context;
import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.rds.RDSRadioName;

import java.util.List;

/**
 * Created by LZM on 2019-8-7.
 * Comment 保存收音列表的工具类
 * TODO:梳理收音列表的获取和存储，因为用到了数据库，所以必须要做在子线程，那就要考虑，线程之间的关系，和同异步的关系
 */
public class RadioListSaveUtils {

    private static final String TAG = "RadioListSaveUtils";

    private static RadioListSaveUtils instance;

    public static RadioListSaveUtils getInstance() {
        if (instance == null) {
            synchronized (RadioListSaveUtils.class) {
                if (instance == null) {
                    instance = new RadioListSaveUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 收音列表保存工具类的初始化，这个初始化在RadioControlRegister里面已经做了，作为库的初始化
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        RadioMessageSQLUtils.getInstance().initialize(context);
    }

    public static final String AM_COLLECT = "am_collect";
    public static final String AM_EFFECT = "am_effect";
    public static final String FM_COLLECT = "fm_collect";
    public static final String FM_EFFECT = "fm_effect";
    public static final String DAB_COLLECT = "dab_collect";
    public static final String DAB_EFFECT = "dab_effect";
    public static final String ALL_COLLECT = "all_collect";
    public static final String FM_PLAY = "fm_play";
    public static final String AM_PLAY = "am_play";
    public static final String DAB_PLAY = "dab_play";

    /**
     * 保存全部收藏列表
     *
     * @param radioMessageList 全部收藏列表
     */
    public void saveAllCollectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveAllCollectList: ");
        RadioMessageSQLUtils.getInstance().saveList(ALL_COLLECT, radioMessageList);
    }


    /**
     * 保存AM的收藏列表
     *
     * @param radioMessageList AM的收藏列表
     */
    public void saveAMCollectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveAMCollectList: ");
        RadioMessageSQLUtils.getInstance().saveList(AM_COLLECT, radioMessageList);
    }

    /**
     * 保存AM的有效列表
     *
     * @param radioMessageList AM的有效列表
     */
    public void saveAMEffectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveAMEffectList: ");
        RadioMessageSQLUtils.getInstance().saveList(AM_EFFECT, radioMessageList);
    }

    /**
     * 保存FM的收藏列表
     *
     * @param radioMessageList FM的收藏列表
     */
    public void saveFMCollectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveFMCollectList: ");
        RadioMessageSQLUtils.getInstance().saveList(FM_COLLECT, radioMessageList);
    }

    /**
     * 保存FM的有效列表
     *
     * @param radioMessageList FM的有效列表
     */
    public void saveFMEffectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveFMEffectList: ");
        RadioMessageSQLUtils.getInstance().saveList(FM_EFFECT, radioMessageList);
    }

    /**
     * 保存DAB的收藏列表
     *
     * @param radioMessageList DAB的收藏列表
     */
    public void saveDABCollectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveDABCollectList: ");
        RadioMessageSQLUtils.getInstance().saveList(DAB_COLLECT, radioMessageList);
    }

    /**
     * 保存DAB的有效列表
     *
     * @param radioMessageList DAB的有效列表
     */
    public void saveDABEffectList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveDABEffectList: ");
        RadioMessageSQLUtils.getInstance().saveList(DAB_EFFECT, radioMessageList);
    }

    /**
     * 保存FM的播放列表
     *
     * @param radioMessageList FM的播放列表
     */
    public void saveFMPlayList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveFMPlayList: ");
        RadioMessageSQLUtils.getInstance().saveList(FM_PLAY, radioMessageList);
    }

    /**
     * 保存AM的播放列表
     *
     * @param radioMessageList AM的播放列表
     */
    public void saveAMPlayList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveAMPlayList: ");
        RadioMessageSQLUtils.getInstance().saveList(AM_PLAY, radioMessageList);
    }

    /**
     * 保存DAB的播放列表
     *
     * @param radioMessageList DAB的播放列表
     */
    public void saveDABPlayList(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "saveDABPlayList: ");
        RadioMessageSQLUtils.getInstance().saveList(DAB_PLAY, radioMessageList);
    }

    /**
     * 获取保存的 FM播放列表
     * @return
     */
    public List<RadioMessage> getFMPlayList(){
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(FM_PLAY);
        Log.d(TAG, "getFMPlayList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取保存的 AM播放列表
     * @return
     */
    public List<RadioMessage> getAMPlayList(){
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(AM_PLAY);
        Log.d(TAG, "getAMPlayList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取保存的 DAB播放列表
     * @return
     */
    public List<RadioMessage> getDABPlayList(){
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(DAB_PLAY);
        Log.d(TAG, "getDABPlayList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }


    /**
     * 获取全部收藏列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getAllCollectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(ALL_COLLECT);
        Log.d(TAG, "getAllCollectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取AM的收藏列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getAMCollectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(AM_COLLECT);
        Log.d(TAG, "getAMCollectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取AM的有效列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getAMEffectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(AM_EFFECT);
        Log.d(TAG, "getAMEffectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取FM的收藏列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getFMCollectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(FM_COLLECT);
        Log.d(TAG, "getFMCollectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取FM的有效列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getFMEffectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(FM_EFFECT);
        Log.d(TAG, "getFMEffectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取DAB的收藏列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getDABCollectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(DAB_COLLECT);
        Log.d(TAG, "getDABCollectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取DAB的有效列表
     *
     * @return radioMessageList
     */
    public List<RadioMessage> getDABEffectList() {
        List<RadioMessage> radioMessageList = RadioMessageSQLUtils.getInstance().getList(DAB_EFFECT);
        Log.d(TAG, "getDABEffectList: size = " + radioMessageList.size() + " radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    //增加EPG订阅的接口

    /**
     * 订阅EPG
     * @param dabepgSchedule
     * @param subscribe
     */
    public void saveSubscribe(DABEPGSchedule dabepgSchedule, boolean subscribe){
        Log.d(TAG, "saveSubscribe: ");
        RadioMessageSQLUtils.getInstance().saveSubscribe(dabepgSchedule,subscribe);
    }

    /**
     * 返回保存的EPG订阅数据
     * @return
     */
    public List<DABEPGSchedule> getSubscribeEPGList(){
        List<DABEPGSchedule> listDTOS = RadioMessageSQLUtils.getInstance().getSubscribeEPGList();
        Log.d(TAG, "getSubscribeEPGList: "+listDTOS);
        return listDTOS;
    }


    public List<String> getRadioSearchHistoryList(){
        List<String> searchHistoryList = RadioMessageSQLUtils.getInstance().getRadioSearchHistoryList();
        Log.d(TAG, "getRadioSearchHistoryList: "+searchHistoryList);
        return searchHistoryList;
    }

    public void saveRadioSearchHistory(List<String> currentHistoryList){
        RadioMessageSQLUtils.getInstance().saveOrUpdateRadioSearchHistory(currentHistoryList);
    }

    public void deleteRadioSearchHistory(){
        RadioMessageSQLUtils.getInstance().deleteRadioSearchHistory();
    }


    public List<RDSRadioName> getRDSNameList(){
        List<RDSRadioName> rdsNameList = RadioMessageSQLUtils.getInstance().getRDSNameList();
        Log.d(TAG, "getRDSNameList: "+rdsNameList);
        return rdsNameList;
    }

    public void updateRdsNameList(List<RDSRadioName> currentRDSNameList){
        RadioMessageSQLUtils.getInstance().updateRdsNameList(currentRDSNameList);
    }

    public void deleteRdsNameList(){
        RadioMessageSQLUtils.getInstance().deleteRDSNameList();
    }

    public List<DABLogo> getDABLogoList(){
        List<DABLogo> dablogList = RadioMessageSQLUtils.getInstance().getDABLogoList();
        Log.d(TAG, "getDABLogoList: "+dablogList);
        return dablogList;
    }

    public void updateDABLogoList(List<DABLogo> currentDABLogoList){
        RadioMessageSQLUtils.getInstance().updateDABLogoList(currentDABLogoList);
    }
}
