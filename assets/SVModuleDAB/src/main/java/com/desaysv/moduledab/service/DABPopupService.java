package com.desaysv.moduledab.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.rds.RDSAnnouncement;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.ServiceUtils;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.dialog.DABAnnDialog;
import com.desaysv.moduledab.dialog.DABEPGDialog;
import com.desaysv.moduledab.fragment.DABFragment;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.EPGUtils;
import com.desaysv.svlibtoast.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * created by ZNB for DAB new req on 2022-03-31
 *
 *
 */

public class DABPopupService extends Service {
    private static final String TAG = "DABPopupService";

    /**
     * 收音的控制器
     */
    private IControlTool mRadioControl;

    /**
     * 收音的状态获取器
     */
    private IStatusTool mRadioStatusTool;

    private ActivityManager mActivityManager;


    private DABAnnDialog dabAnnDialog;

    private DABEPGSchedule subScribeEPG;//预约到达的EPG信息，可从这里得到DAB电台
    private DABEPGDialog dabepgDialog;

    private DelayDismissHandler delayDismissHandler;

    /**
     * 切换回上一个音源的标志位
     * -1: 表示当前已经是需要用的音源，不需要切换
     * 0:  表示需要切换到 FM
     * 1:  表示需要切换到 AM
     * 2:  表示需要切换到 DAB
     * 3:  表示需要切换到其它音源，直接使用 pause 方法即可
     * 因为弹窗的时候，需要切换到对应的音源播放。
     * 如果之前是 AM/DAB/FM，则需要重新 open一次，不能直接 play
     */
    private int switchFLag = -1;
    private static final int FLAG_SWITCH_FM = 0;
    private static final int FLAG_SWITCH_AM = 1;
    private static final int FLAG_SWITCH_DAB = 2;
    private static final int FLAG_SWITCH_OTHER = 3;
    private boolean needPause;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        ServiceUtils.startForegroundNotification(this, "RadioPopupService", "RadioPopupService");
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        DABTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(radioStatusChange);
    }

    /**
     * 服务起来的时候，就注册对 TA、Announcement 的监听
     */
    private void init(){
        mRadioControl = DABTrigger.getInstance().mRadioControl;
        mRadioStatusTool = DABTrigger.getInstance().mRadioStatusTool;
        DABTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(radioStatusChange);
        mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        delayDismissHandler = new DelayDismissHandler(this);

        dabAnnDialog = new DABAnnDialog(this, R.style.dialogstyle);
        dabepgDialog = new DABEPGDialog(this, R.style.dialogstyle);

        dabAnnDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                switchToPreSource(switchFLag,true);
                RadioControlTool.getInstance().setShowingTTS(false);
            }
        });
    }


    /**
     * 这个是监听收音状态变化回调过来的消息
     */
    private final IRadioStatusChange radioStatusChange = new IRadioStatusChange() {

        @Override
        public void onCurrentRadioMessageChange() {
            if (dabAnnDialog != null && dabAnnDialog.isShowing()){
                RadioMessage dabMessage = mRadioStatusTool.getCurrentRadioMessage();
                Log.d(TAG, "showTADialog,onCurrentRadioMessageChange,dabMessage: " + dabMessage);
                if(dabMessage != null && dabMessage.getDabMessage() != null) {
                    String contentText;
                    if(!TextUtils.isEmpty(dabMessage.getDabMessage().getDynamicLabel())){
                        contentText = dabMessage.getDabMessage().getDynamicLabel();
                    } else {
                        contentText = dabMessage.getDabMessage().getProgramStationName();
                    }
                    dabAnnDialog.updateAnnContent(contentText);
                }
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {

        }

        @Override
        public void onAstListChanged(int band) {

        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {

        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {

        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {
            Log.d(TAG,"onAnnNotify,notify:"+notify);//显示状态 0：hide 1：show
            showAnnDialog(notify);
        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {

        }

        @Override
        public void onRDSAnnNotify(RDSAnnouncement rdsAnnouncement) {

        }

        @Override
        public void onDABTimeNotify(DABTime dabTime) {
            Log.d(TAG,"onDABTimeNotify,dabTime:"+dabTime);//dab时间更新通知
            subScribeEPG = EPGUtils.needShowReminder(subScribeEPG,dabTime);
            if (subScribeEPG != null && !subScribeEPG.isHadShow()){
                Log.d(TAG,"needShowReminder:"+subScribeEPG);
                showSubscribeEPG(true);
            }
        }

        @Override
        public void onDABSignalChanged(int signalValue) {
            String currentCarAudioType = AudioFocusUtils.getInstance().getCurrentAudioSourceName(AppBase.mContext);
            Log.d(TAG,"onDABSignalChanged,signalValue:"+signalValue+",currentCarAudioType: "+currentCarAudioType);
            if (DsvAudioSDKConstants.DAB_SOURCE.equals(currentCarAudioType)){
                if(signalValue == 1) {
                    ToastUtil.showToast(AppBase.mContext, R.string.dab_signal_weak);
                }
            }
        }
    };


    private void showSubscribeEPG(boolean show){
        if (show){
            if (!dabepgDialog.isShowing() && canShowEPG()){
                dabepgDialog.show();
                checkShowStyle();
                dabepgDialog.updateListenContent(subScribeEPG);
                subScribeEPG.setHadShow(true);
                //弹出后，从预约列表中移除
                RadioList.getInstance().updateEPGSubscribeList(subScribeEPG);
            }
        }else {
            dabepgDialog.dismiss();
        }
    }

    private void updateTimeout(int time){
        dabepgDialog.updateTimeout(time);
    }


    private void checkShowStyle(){
        dabepgDialog.updateShowStyle(!checkAudioTopActivity());
    }

    /**
     * 判断当前能否显示EPG弹窗
     * 这里需要增加 倒车、紧急呼叫、AVM等等一系列状态的判断
     * @return
     */
    private boolean canShowEPG(){
        int currentSourceId = AudioFocusUtils.getInstance().getCurrentSourceId();
        Log.d(TAG,"canShowEPG,getCurrentSourceId: " +currentSourceId);
        if (currentSourceId == 43
                || currentSourceId == 30
                || currentSourceId == 32
                || checkAAisTopActivity()){//d倒车、通话不可弹窗
            return false;
        }
        //判断当前播放的是否已经是EPG节目对应的电台
        if (currentSourceId == 147){//处于DAB音源
            RadioMessage dabMessage = mRadioStatusTool.getCurrentRadioMessage();
            Log.d(TAG,"canShowEPG,check current dabMessage: " +dabMessage);
            if (subScribeEPG != null &&CompareUtils.isSameDAB(dabMessage,EPGUtils.convertToDABMessage(subScribeEPG))){
                return false;
            }
        }
        return true;
    }





    private void showAnnDialog(DABAnnNotify notify){
        if (notify.getStatus() == 1 && canShowAnn()) {
                RadioMessage dabMessage = mRadioStatusTool.getCurrentRadioMessage();
                Log.d(TAG, "showAnnDialog,dabMessage: " + dabMessage);
                delayDismissHandler.removeMessages(MSG_DELAY_DISMISS);
                if (dabAnnDialog != null) {
                    //content 显示什么？
                    if(dabMessage != null && dabMessage.getDabMessage() != null){
                        String contentText;
                        if(!TextUtils.isEmpty(dabMessage.getDabMessage().getDynamicLabel())){
                            contentText = dabMessage.getDabMessage().getDynamicLabel();
                        } else {
                            contentText = dabMessage.getDabMessage().getProgramStationName();
                        }
                        dabAnnDialog.updateAnnContent(contentText);
                    }
                    if (!dabAnnDialog.isShowing()) {
                        RadioControlTool.getInstance().setShowingTTS(true);
                        dabAnnDialog.show();
                        dabAnnDialog.updateAnnTitle(notify);
                        sendBroadcastToAA(VALUE_TA_OPEN);
                        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.SOURCE);
                        switchDABIfNeed();
                    }else {
                        dabAnnDialog.updateAnnTitle(notify);
                    }
                }
        }else {
            Log.d(TAG, "dismiss AnnDialog");
            //用于进入STR/通话时，消失弹窗
            if (notify.getStatus() == -1 && dabAnnDialog != null && !dabAnnDialog.isShowing()){
                Log.d(TAG, "dismiss AnnDialog with str or phone");
                dismiss();
            }else {
                delayDismissHandler.sendEmptyMessageDelayed(MSG_DELAY_DISMISS,DELAY_DISMISS_TIME);
            }
        }

    }

    public void dismiss(){
        if (dabAnnDialog != null) {
            dabAnnDialog.dismiss();
            Log.d(TAG, "dismiss");
            //switchToPreSource(switchFLag,true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,"onConfigurationChanged:"+newConfig);
        if (dabAnnDialog != null){
            dabAnnDialog.reShowDialog();
        }
    }

    /**
     * TA的弹窗，需要高优先级弹出并切换到FM/DAB输出
     * 因为交互场景太多，目前先做成媒体焦点类型时弹出
     * 非媒体焦点后续再看如何处理
     * @return
     */


    private boolean canShowAnn(){
        needPause = CurrentRadioInfo.getInstance().getPauseReason() != ChangeReasonData.NA;
        String currentCarAudioType = AudioFocusUtils.getInstance().getCurrentAudioSourceName(AppBase.mContext);
        Log.d(TAG,"canShowDAB,currentCarAudioType: " +currentCarAudioType);
        int currentSourceId = AudioFocusUtils.getInstance().getCurrentSourceId();
        Log.d(TAG,"canShowDAB,getCurrentSourceId: " +currentSourceId);
        Log.d(TAG,"canShowDAB,needPause: " +needPause);
        //当前已经在播放TTS，不需要弹窗
        if (currentSourceId == 43/*AudioAttributes.SOURCE_RVC*/
            || currentSourceId == 30
            || currentSourceId == 32
            || currentSourceId == 201
            || currentSourceId == 148//RDS TA的时候也不要弹窗了，容易混乱
            || RadioControlTool.getInstance().isPhoneCallState()){//倒车、通话不可弹窗
            return false;
        }
        //弹窗时，可能有连续多个TA消息上来，而且没有通知让上一个消失，这样会导致这个switchFLag被覆盖掉，从而导致消失弹窗后，不恢复播放的情况
        //因此判断是dab_tts的音源时，直接返回true即可，不要更改这个switchFLag
        if (currentSourceId == 149){
            return true;
        }else {
            switchFLag = -1;
        }

        if (currentCarAudioType != null && !checkAAisTopActivity()){
            if (DsvAudioSDKConstants.DAB_SOURCE.equals(currentCarAudioType)){//当前源是DAB，无需处理
                switchFLag = FLAG_SWITCH_DAB;
            }else if (DsvAudioSDKConstants.AM_SOURCE.equals(currentCarAudioType)){
                switchFLag = FLAG_SWITCH_AM;
            }else if (DsvAudioSDKConstants.FM_SOURCE.equals(currentCarAudioType)){
                switchFLag = FLAG_SWITCH_FM;
            }else if("mute_media".equals(currentCarAudioType) || "mute_hardkey".equals(currentCarAudioType)){//如果是mute_media/mute_hardkey，那就判断一下mute之前的是哪个焦点
                String preCarAudioType = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext);
                Log.d(TAG,"canShowDAB,preCarAudioType: " +preCarAudioType);
                if (DsvAudioSDKConstants.DAB_SOURCE.equals(preCarAudioType)){
                    switchFLag = FLAG_SWITCH_DAB;
                }else {
                    switchFLag = FLAG_SWITCH_OTHER;
                }
            }else {
                switchFLag = FLAG_SWITCH_OTHER;
            }

            return true;
        }else {
            return false;
        }
    }


    /**
     * 切到DAB以播放 弹窗 的内容
     */
    private void switchDABIfNeed(){
        if (switchFLag != -1){
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                    ChangeReasonData.RADIO_TTS, DABTrigger.getInstance().mRadioStatusTool.getDABRadioMessage());
        }
    }

    /**
     * 弹窗消失的时候，切换回上一个音源
     * @param switchFLag，需要切回的音源标志
     * @param isDAB，是否是DAB的弹窗
     */
    private void switchToPreSource(int switchFLag, boolean isDAB){
        Log.d(TAG,"switchToPreSource: "+switchFLag + ",isDAB:" + isDAB);
        if (switchFLag != -1) {//-1表示同一个音源，不需要切换
            if (isDAB) {
                mRadioControl.processCommand(RadioAction.RELEASE,
                        ChangeReasonData.TTS_RESUME, mRadioStatusTool.getDABRadioMessage());
            } else {
                mRadioControl.processCommand(RadioAction.RELEASE,
                        ChangeReasonData.TTS_RESUME, mRadioStatusTool.getFMRadioMessage());
            }
        }
        switch(switchFLag){
            case FLAG_SWITCH_AM:
                mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        needPause ? ChangeReasonData.TTS_RESUME_PAUSE : ChangeReasonData.TTS_RESUME, mRadioStatusTool.getAMRadioMessage());
                break;
            case FLAG_SWITCH_FM:
                mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        needPause ? ChangeReasonData.TTS_RESUME_PAUSE : ChangeReasonData.TTS_RESUME, mRadioStatusTool.getFMRadioMessage());
                break;
            case FLAG_SWITCH_DAB:
            default:
                mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        needPause ? ChangeReasonData.TTS_RESUME_PAUSE : ChangeReasonData.TTS_RESUME, mRadioStatusTool.getDABRadioMessage());
                break;
            case FLAG_SWITCH_OTHER:
                break;
        }
        sendBroadcastToAA(VALUE_TA_CLOSE);
    }

    //检查互联APP 是否在最顶部
    private static final String AA_APP = "com.google.android.projection.sink";
    private static final String CP_APP = "com.desaysv.vehicle.carplayapp";
    public boolean checkAAisTopActivity(){
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(1);
        if (runningTaskInfos.size() <= 0) {
            return false;
        }
        ComponentName topActivity = runningTaskInfos.get(0).topActivity;
        String topPkgName = topActivity.getPackageName();
        Log.d(TAG, "checkAAisTopActivity: topActivity = " + topActivity + " topPkgName =" + topPkgName);
        if (AA_APP.equals(topPkgName) || CP_APP.equals(topPkgName)){
            Log.i(TAG, "checkAAisTopActivity: AA app is TOP");
            return true;
        }
        return false;
    }



    //检查AudioAPP 是否在最顶部
    private static final String AUDIO_APP = "com.desaysv.svaudioapp";
    public boolean checkAudioTopActivity(){
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(1);
        if (runningTaskInfos.size() <= 0) {
            return false;
        }
        ComponentName topActivity = runningTaskInfos.get(0).topActivity;
        String topPkgName = topActivity.getPackageName();
        Log.d(TAG, "checkAudioTopActivity: topActivity = " + topActivity + " topPkgName =" + topPkgName);
        if (AUDIO_APP.equals(topPkgName)){
            Log.i(TAG, "checkAudioTopActivity: Audio app is TOP");
            return true;
        }
        return false;
    }


    /**
     * 发送广播告诉AA TA弹窗的状态
     * AA有问题，长焦点丢失时，不会恢复播放
     * 需要额外通知AA
     * @param state
     */
    private static final String ACTION_TA = "com.desay.rds.ta.action";
    private static final String KEY_TA = "TA_VALUE";
    private static final int VALUE_TA_CLOSE = 0;
    private static final int VALUE_TA_OPEN = 1;
    private void sendBroadcastToAA(int state){
        Log.d(TAG,"sendBroadcastToAA: "+state);
        Intent intent = new Intent();
        intent.setAction(ACTION_TA);
        intent.putExtra(KEY_TA,state);
        intent.setPackage(AA_APP);
        sendBroadcast(intent);
    }

    /**
     * 欧洲路试出现不断 快速闪现 DAB TA 公告的情况，
     * 原因是 DAB的公告信号 不断快速变化。
     * 界面上采用 延时消失弹窗，时间内又来弹窗消息时，移除 消失弹窗的处理，
     * 从体验上做一定的优化
     */
    private static final int DELAY_DISMISS_TIME = 300;//先给一个400毫秒的延时
    private static final int MSG_DELAY_DISMISS = 0;
    private static class DelayDismissHandler extends Handler{
        private WeakReference<DABPopupService> weakReference;

        public DelayDismissHandler(DABPopupService dabPopupService) {
            weakReference = new WeakReference<>(dabPopupService);
        }

        @Override
        public void handleMessage(@NonNull Message msg){
            weakReference.get().dismiss();
        }
    }
}