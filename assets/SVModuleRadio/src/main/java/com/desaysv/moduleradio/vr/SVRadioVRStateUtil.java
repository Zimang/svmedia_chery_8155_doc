package com.desaysv.moduleradio.vr;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.constants.RadioConstants;

import java.util.List;

/**
 * created by ZNB on 2023-02-24
 * 语音控制时，对应控制的Radio状态
 */
public class SVRadioVRStateUtil {
    private static final String TAG = "SVRadioVRStateUtil";
    private static boolean hasDAB = false;

    private static SVRadioVRStateUtil instance;

    public static SVRadioVRStateUtil getInstance(){
        if (instance == null){
            synchronized (SVRadioVRStateUtil.class){
                if (instance == null){
                    instance = new SVRadioVRStateUtil();
                    hasDAB = ProductUtils.hasDAB();
                }
            }
        }
        return instance;
    }

    /**
     * 私有构造方法
     */
    private SVRadioVRStateUtil(){

    }

    /**
     * 判断当前是否处于Radio界面
     * @return
     */
    public boolean isOnRadioSource(){
            if (isAudioTop()){//当前是Audio的界面，进一步判断是否是Radio界面
                if (Constants.Source.SOURCE_RADIO.equals(currentSource)){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
    }


    /**
     * 判断当前是否处于Radio收藏列表界面
     * @return
     */
    public boolean isOnRadioCollect(){
        if (isAudioTop()){//当前是Audio的界面，进一步判断是否是Radio界面
            if (Constants.Source.SOURCE_RADIO.equals(currentSource)){//当前是Radio的界面，进一步判断是否是Radio收藏界面
                if (hasDAB){
                    if (RadioConstants.TABWithDAB.POSITION_COLLECT == currentTab){
                        return true;
                    }else {
                        return false;
                    }
                }else {
                    if (RadioConstants.TABWithoutDAB.POSITION_COLLECT == currentTab){
                        return true;
                    }else {
                        return false;
                    }
                }
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    public RadioMessage getAdjacentRadioMessage() {
        return adjacentRadioMessage;
    }

    public void setAdjacentRadioMessage(RadioMessage adjacentRadioMessage) {
        this.adjacentRadioMessage = adjacentRadioMessage;
    }

    private RadioMessage adjacentRadioMessage;
    /**
     * 根据频点值判断目标电台在Radio中的状态
     * @param targetMessage
     * @return
     */
    public int checkTargetState(RadioMessage targetMessage){
            Log.d(TAG,"checkTargetState,targetMessage:"+targetMessage);
            if (targetMessage.getRadioBand() == RadioManager.BAND_AM){//处理AM
                //判断是否在频率范围
                if (targetMessage.getRadioFrequency() > RadioConfig.AM_MAX || targetMessage.getRadioFrequency() < RadioConfig.AM_MIN){
                    return SVRadioVRConstant.TargetState.STATE_OVER_RANGE;
                }

                //判断是否属于有效值，无效的话，去列表中查找最相近的
                int targetFreq = targetMessage.getRadioFrequency();

                if ((targetFreq - RadioConfig.AM_MIN) % RadioConfig.AM_STEP == 0){

                }else {
                    int diffFreq = 10000000;//targetFreq 和有效列表的每个频点值 的 差值，用于查找两者最接近的那个频点
                    boolean isOnEffectList = false;
                    for (RadioMessage radioMessage: RadioList.getInstance().getAMEffectRadioMessageList()){
                        if (targetFreq == radioMessage.getRadioFrequency()){
                            isOnEffectList = true;
                            break;
                        }
                        if (diffFreq > Math.abs(targetFreq - radioMessage.getRadioFrequency())){
                            diffFreq = Math.abs(targetFreq - radioMessage.getRadioFrequency());
                            adjacentRadioMessage = radioMessage.Clone();//把最相近的那个赋值给adjacentRadioMessage
                        }
                    }
                    if (!isOnEffectList){
                        if (adjacentRadioMessage == null){
                            adjacentRadioMessage = new RadioMessage(RadioManager.BAND_AM,RadioConfig.AM_MIN);
                        }
                        return SVRadioVRConstant.TargetState.STATE_NOT_AM_EFFECT;
                    }
                }

                //判断是否已经在播放
                RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
                if (targetMessage.getRadioFrequency() == radioMessage.getRadioFrequency() && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()){
                    return SVRadioVRConstant.TargetState.STATE_AM_PLAYED;
                }

            }else if (targetMessage.getRadioBand() == RadioManager.BAND_FM){//处理AM
                //判断是否在频率范围
                if (targetMessage.getRadioFrequency() > RadioConfig.FM_MAX || targetMessage.getRadioFrequency() < RadioConfig.FM_MIN){
                    return SVRadioVRConstant.TargetState.STATE_OVER_RANGE;
                }

                //判断是否属于有效值，无效的话，去列表中查找最相近的
                int targetFreq = targetMessage.getRadioFrequency();
                if ((targetFreq - RadioConfig.FM_MIN) % RadioConfig.FM_STEP == 0){

                }else {
                    int diffFreq = 10000000;//targetFreq 和有效列表的每个频点值 的 差值，用于查找两者最接近的那个频点
                    boolean isOnEffectList = false;
                    for (RadioMessage radioMessage : RadioList.getInstance().getFMEffectRadioMessageList()) {
                        if (targetFreq == radioMessage.getRadioFrequency()) {
                            isOnEffectList = true;
                            break;
                        }
                        if (diffFreq > Math.abs(targetFreq - radioMessage.getRadioFrequency())) {
                            diffFreq = Math.abs(targetFreq - radioMessage.getRadioFrequency());
                            adjacentRadioMessage = radioMessage.Clone();//把最相近的那个赋值给adjacentRadioMessage
                        }
                    }
                    if (!isOnEffectList) {
                        if (adjacentRadioMessage == null) {
                            adjacentRadioMessage = new RadioMessage(RadioManager.BAND_FM, RadioConfig.FM_MIN);
                        }
                        return SVRadioVRConstant.TargetState.STATE_NOT_FM_EFFECT;
                    }
                }

                //判断是否已经在播放
                RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
                if (targetMessage.getRadioFrequency() == radioMessage.getRadioFrequency() && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()){
                    return SVRadioVRConstant.TargetState.STATE_FM_PLAYED;
                }
            }
            //可以正常执行播放
            return SVRadioVRConstant.TargetState.STATE_PLAY;
    }

    /**
     * 获取系统顶层界面的包名
     *
     * @return Audio应用是否前台
     */
    public  boolean isAudioTop() {
        String topPackageName;
        ActivityManager activityManager = (ActivityManager) (AppBase.mContext.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        ComponentName componentName = runningTaskInfos.get(0).topActivity;
        topPackageName = componentName.getPackageName();
        Log.d(TAG, "getTopActivityPackageName: topPackageName = " + topPackageName);
        return "com.desaysv.svaudioapp".equals(topPackageName);
    }

    /**
     * 主界面切换源时，对应更新这个
     */
    private String currentSource = Constants.Source.SOURCE_RADIO;

    public void setCurrentSource(String source){
        Log.d(TAG,"setCurrentSourceL:"+source);
        currentSource = source;
    }

    private String getCurrentSource(){
        return currentSource;
    }


    /**
     * Radio界面切换tab时，对应更新这个
     */
    private int currentTab = -1;

    public void setCurrentTab(int tab){
        Log.d(TAG,"setCurrentTab:"+tab);
        currentTab = tab;
    }

    public int getCurrentTab(){
        return currentTab;
    }

}
