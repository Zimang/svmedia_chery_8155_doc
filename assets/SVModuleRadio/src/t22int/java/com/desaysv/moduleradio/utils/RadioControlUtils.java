package com.desaysv.moduleradio.utils;

import android.content.Intent;
import android.util.Log;
import android.hardware.radio.RadioManager;
import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libradio.action.RadioControlAction;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.utils.JsonUtils;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;


/**
 * Created by LZM on 2020-3-23
 * Comment 对外实现的媒体控制器，根据当前的音源，判断需要调用什么控制器
 */
public class RadioControlUtils {

    private static final String TAG = "RadioControlUtils";

    private static RadioControlUtils instance;

    private boolean hasAM = true;

    private boolean hasMulti = false;

    public static RadioControlUtils getInstance() {
        if (instance == null) {
            synchronized (RadioControlUtils.class) {
                if (instance == null) {
                    instance = new RadioControlUtils();
                }
            }
        }
        return instance;
    }

    private RadioControlUtils() {
        //T22还没有DAB/FM融合的ui实现，待实现后再放开
        hasAM = ProductUtils.hasAM();
        hasMulti = ProductUtils.hasMulti();
    }

    /**
     * 播放
     *
     * @param source 选中的音源
     */
    public void play(String source) {
        Log.d(TAG, "play: source = " + source);
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
            case DsvAudioSDKConstants.AM_SOURCE:
            case DsvAudioSDKConstants.DAB_SOURCE:
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY, ChangeReasonData.AIDL);
                break;
        }
    }

    /**
     * 播放指定内容
     *
     * @param source 选中的音源
     */
    public void play(String source, String info) {
        Log.d(TAG, "play: source = " + source + ",　info = " + info);
        if (info == null) {
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
            case DsvAudioSDKConstants.AM_SOURCE:
            case DsvAudioSDKConstants.DAB_SOURCE:
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AIDL, changeMediaInfoToRadioMessage(source, info));
                break;
        }
    }

    private RadioMessage changeMediaInfoToRadioMessage(String source, String info) {
        MediaInfoBean mediaInfoBean = JsonUtils.generateObject(info, MediaInfoBean.class);
        RadioMessage radioMessage = new RadioMessage();

        radioMessage.setRadioType(RadioMessage.FM_AM_TYPE);
        radioMessage.setRadioBand(mediaInfoBean.getBand());
        radioMessage.setRadioFrequency(Integer.parseInt(mediaInfoBean.getFreq()));

        if (source.equals(DsvAudioSDKConstants.DAB_SOURCE)) {
            radioMessage.setRadioType(RadioMessage.DAB_TYPE);
            DABMessage dabMessage = new DABMessage(Integer.parseInt(mediaInfoBean.getFreq()), Integer.parseInt(mediaInfoBean.getServiceId()), Integer.parseInt(mediaInfoBean.getComponentId()));
            radioMessage.setDabMessage(dabMessage);
        }


        return radioMessage;
    }

    /**
     * 设置Band，待定，空实现
     *
     * @param source
     * @param info
     */
    public void setBand(String source, String info) {

    }


    /**
     * 暂停
     *
     * @param source 选中的音源
     */
    public void pause(String source) {
        Log.d(TAG, "pause: source = " + source);
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
            case DsvAudioSDKConstants.AM_SOURCE:
            case DsvAudioSDKConstants.DAB_SOURCE:
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PAUSE, ChangeReasonData.AIDL);
                break;
        }
    }

    /**
     * 播放暂停
     *
     * @param source 选中的音源
     */
    public void playOrPause(String source) {
        Log.d(TAG, "playOrPause: source = " + source);
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
            case DsvAudioSDKConstants.AM_SOURCE:
            case DsvAudioSDKConstants.DAB_SOURCE:
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY_OR_PAUSE, ChangeReasonData.AIDL);
                break;
        }
    }

    /**
     * 下一曲
     *
     * @param source 选中的音源
     */
    public void next(String source) {
        Log.d(TAG, "next: source = " + source);
        if (hasMulti){
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_SEEK_FORWARD, ChangeReasonData.AIDL);
                    break;
                case DsvAudioSDKConstants.AM_SOURCE:
                    if (hasAM) {
                        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.AIDL);
                    }
                    break;
            }

        }else {
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.AM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.AIDL);
                    break;
            }
        }
    }

    /**
     * 上一曲
     *
     * @param source 选中的音源
     */
    public void pre(String source) {
        Log.d(TAG, "pre: source = " + source);
        if (hasMulti){
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_SEEK_BACKWARD, ChangeReasonData.AIDL);
                    break;
                case DsvAudioSDKConstants.AM_SOURCE:
                    if (hasAM) {
                        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.AIDL);
                    }
                    break;
            }
        }else {
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.AM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.AIDL);
                    break;
            }
        }
    }

    /**
     * 收藏当前播放的电台，可以按需求，也根据当前的音源，收藏对应的FM或者AM的电台
     *
     * @param source 选中的音源
     */
    public void changeCollect(String source) {
        Log.d(TAG, "collect: source = " + source);
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
            case DsvAudioSDKConstants.AM_SOURCE:
            case DsvAudioSDKConstants.DAB_SOURCE:
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                RadioMessage currentRadio = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();

                //DAB/FM无当前电台、无电台列表收藏不做处理
                if(hasMulti){
                    if (currentRadio.getRadioBand() == RadioMessage.DAB_BAND){
                        if (RadioList.getInstance().getDABEffectRadioMessageList().isEmpty()){
                            Log.d(TAG, "getDABEffectRadioMessageList: isEmpty break");
                            break;
                        }
                    } else {
                        if (currentRadio.getRadioBand() == RadioManager.BAND_FM) {
                            if(RadioList.getInstance().getFMEffectRadioMessageList().isEmpty()){
                                Log.d(TAG, "getFMEffectRadioMessageList: isEmpty break");
                                break;
                            }
                        }
                    }
                }

                if (currentRadio.isCollect()){
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.AIDL, currentRadio);
                    if (iRadioCollectFullCallback != null){
                        iRadioCollectFullCallback.onRadioCancelCollect();
                    }
                }else {
                    if (hasMulti){
                        if (currentRadio.getRadioBand() == RadioManager.BAND_AM){
                            if (!(RadioList.getInstance().getAMCollectRadioMessageList().size() > 29)) {
                                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.AIDL, currentRadio);
                            }else {
                                //ToastUtil.showToast(AppBase.mContext, AppBase.mContext.getResources().getString(R.string.dab_collect_fully));
                                if (iRadioCollectFullCallback != null){
                                    iRadioCollectFullCallback.onRadioCollectFull();
                                }
                            }
                        }else {
                            if (!(RadioList.getInstance().getMultiCollectRadioMessageList().size() > 29)) {
                                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.AIDL, currentRadio);
                            }
                        }
                    }else {
                        if (currentRadio.getRadioBand() == RadioMessage.DAB_BAND){
                            if (!(RadioList.getInstance().getDABCollectRadioMessageList().size() > 19)) {
                                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.AIDL, currentRadio);
                            }else {
                                //ToastUtil.showToast(AppBase.mContext, AppBase.mContext.getResources().getString(R.string.dab_collect_fully));
                                if (iRadioCollectFullCallback != null){
                                    iRadioCollectFullCallback.onRadioCollectFull();
                                }
                            }
                        }else {
                            if (!RadioList.getInstance().isFMAMFullCollect()) {
                                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.AIDL, currentRadio);
                            }else {
                                //ToastUtil.showToast(AppBase.mContext, AppBase.mContext.getResources().getString(R.string.dab_collect_fully));
                                if (iRadioCollectFullCallback != null){
                                    iRadioCollectFullCallback.onRadioCollectFull();
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * 向前步进
     *
     * @param source 选中的音源
     */
    public void stepForward(String source) {
        Log.d(TAG, "stepForward: source = " + source);
        if (hasMulti){
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_STEP_FORWARD, ChangeReasonData.AIDL);
                    break;
                case DsvAudioSDKConstants.AM_SOURCE:
                    if (hasAM){
                        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_FORWARD, ChangeReasonData.AIDL);
                    }
                    break;
            }
        }else {
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.AM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_FORWARD, ChangeReasonData.AIDL);
                    break;
            }
        }
    }

    /**
     * 向后步进
     *
     * @param source 选中的音源
     */
    public void stepBackward(String source) {
        Log.d(TAG, "stepBackward: source = " + source);
        if (hasMulti){
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_STEP_BACKWARD, ChangeReasonData.AIDL);
                    break;
                case DsvAudioSDKConstants.AM_SOURCE:
                    if (hasAM) {
                        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_BACKWARD, ChangeReasonData.AIDL);
                    }
                    break;
            }
        }else {
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.AM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_BACKWARD, ChangeReasonData.AIDL);
                    break;
            }
        }
    }

    /**
     * 启动搜台
     *
     * @param source 选中的音源
     */
    public void startAst(String source) {
        Log.d(TAG, "startAst: source = " + source);
        if (hasMulti){
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.MULTI_AST, ChangeReasonData.AIDL);
                    break;
                case DsvAudioSDKConstants.AM_SOURCE:
                    if (hasAM) {
                        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.AST, ChangeReasonData.AIDL);
                    }
                    break;
            }
        }else {
            switch (source) {
                case DsvAudioSDKConstants.FM_SOURCE:
                case DsvAudioSDKConstants.AM_SOURCE:
                case DsvAudioSDKConstants.DAB_SOURCE:
                case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                    //目前后台扫描只有朝拜指南针会调用，它是指定扫描FM的
                    //后面有其它需求的话，需要从vds-MediaSDK那边传入对应reason，以便区分
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.BACKGROUND_AST, ChangeReasonData.AIDL,ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage());
                    break;
            }
        }
    }

    /**
     * 停止搜台
     *
     * @param source 选中的音源
     */
    public void stopAst(String source) {
        Log.d(TAG, "stopAst: source = " + source);
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
            case DsvAudioSDKConstants.AM_SOURCE:
            case DsvAudioSDKConstants.DAB_SOURCE:
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.AIDL);
                break;
        }
    }


    /**
     * @param source       音源
     * @param isForeground 是否前台
     * @param flag         启动标志
     */
    public void openSource(String source, boolean isForeground, int flag,String reason) {
        Log.d(TAG, "openSource: source = " + source + ", isForeground = " + isForeground + ", flag = " + flag + ",reason:"+reason);
        ChangeReason changeReason;
        if ("mode".equals(reason)) {
            changeReason = ChangeReasonData.MODE_CONTROL;
        } else if ("cards_start".equals(reason)){
            changeReason = ChangeReasonData.AIDL;
        } else {
            if ("boot_resume".equals(reason) && !SPUtlis.getInstance().getRadioPlayPauseStatus()) {
                Log.d(TAG, "openSource: chg changeReason to BOOT_RESUME_NOT_PLAY");
                changeReason = ChangeReasonData.BOOT_RESUME_NOT_PLAY;
            } else {
                changeReason = ChangeReasonData.BOOT_RESUME;
            }
        }
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        changeReason, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage());
                if ("mode".equals(reason)){
                    CurrentRadioInfo.getInstance().setCurrentRadioMessage(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage());
                }
                break;
            case DsvAudioSDKConstants.AM_SOURCE:
                if (hasMulti){
                    if (hasAM){

                    }else {
                        return;
                    }
                }
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        changeReason, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage());
                if ("mode".equals(reason)){
                    CurrentRadioInfo.getInstance().setCurrentRadioMessage(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage());
                }
                break;
            case DsvAudioSDKConstants.DAB_SOURCE:
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        changeReason, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage());
                if ("mode".equals(reason)){
                    CurrentRadioInfo.getInstance().setCurrentRadioMessage(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage());
                }
                break;
            case DsvAudioSDKConstants.LOCAL_RADIO_SOURCE:
                if ("boot_resume".equals(reason) && !SPUtlis.getInstance().getRadioPlayPauseStatus()) {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                            ChangeReasonData.BOOT_RESUME_NOT_PLAY, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                } else {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                            ChangeReasonData.BOOT_RESUME, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                }
                break;
        }
        if (isForeground) {//需要启动到前台
            //因为APP层的Flag定义和SDK的定义不一样，所以要做一下转换
            if (flag == Constants.NavigationFlag.FLAG_PLAY){//1是指列表页
                flag = Constants.NavigationFlag.FLAG_MAIN;
            }
            if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getRadioType() != RadioMessage.DAB_TYPE
                || ((DsvAudioSDKConstants.FM_SOURCE.equals(source) || DsvAudioSDKConstants.AM_SOURCE.equals(source)))){
                if (flag == Constants.NavigationFlag.FLAG_DAB_PLAY){//2是指播放页
                    flag = Constants.NavigationFlag.FLAG_PLAY;
                }
            }
            Intent intent = new Intent();
            intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, source);
            intent.putExtra(Constants.NavigationFlag.KEY, flag);
            AppBase.mContext.startActivity(intent);
        }

    }

    /**
     * 获取DAB logo
     * @param source
     * @return
     */
    public byte[] getCurrentPic(String source){
        Log.d(TAG,"getCurrentPic,source:"+source);
        Log.d(TAG,"getCurrentPic,initSuccess:"+RadioControlAction.getInstance().initSuccess);
        if (RadioControlAction.getInstance().initSuccess){//判断是否已经初始化完成
            if (DsvAudioSDKConstants.DAB_SOURCE.equals(source)){
                DABMessage dabMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().getDabMessage();
                if (dabMessage != null){
                    //优先使用Sls
                    byte[] logoByte = dabMessage.getSlsDataList();
                    Log.d(TAG,"getCurrentPic,优先使用Sls");
                    //次级使用存储的Logo
                    if (logoByte == null){
                        Log.d(TAG,"getCurrentPic,次级使用存储的Logo");
                        logoByte = ListUtils.getOppositeDABLogo(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage());
                    }
                    //最后使用当前获取到的Logo
                    if (logoByte == null){
                        Log.d(TAG,"getCurrentPic,最后使用当前获取到的Logo");
                        logoByte = dabMessage.getLogoDataList();
                    }
                    return logoByte;
                }
                return new byte[0];
            }
        }
        return new byte[0];
    }

    private IRadioCollectFullCallback iRadioCollectFullCallback;
    public interface IRadioCollectFullCallback {
        public void onRadioCollectFull();

        public void onRadioCancelCollect();
    }

    public void setRadioCollectFullCallback(IRadioCollectFullCallback iRadioCollectFullCallback) {
        this.iRadioCollectFullCallback = iRadioCollectFullCallback;
    }

}
