package com.desaysv.moduleradio.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
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
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.view.RDSTADialog;

import java.util.List;

/**
 * created by ZNB for DAB new req on 2022-03-31
 *
 *
 */

public class RadioPopupService extends Service {
    private static final String TAG = "RadioPopupService";

    /**
     * 收音的控制器
     */
    private IControlTool mRadioControl;

    /**
     * 收音的状态获取器
     */
    private IStatusTool mRadioStatusTool;

    private ActivityManager mActivityManager;


    private RDSTADialog rdstaDialog;

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
        ModuleRadioTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(radioStatusChange);
    }

    /**
     * 服务起来的时候，就注册对 TA、Announcement 的监听
     */
    private void init(){
        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(radioStatusChange);
        mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        rdstaDialog = new RDSTADialog(this, R.style.radio_ta_dialogstyle);
        rdstaDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                switchToPreSource(switchFLag,false);
            }
        });
    }


    /**
     * 这个是监听收音状态变化回调过来的消息
     */
    private final IRadioStatusChange radioStatusChange = new IRadioStatusChange() {

        @Override
        public void onCurrentRadioMessageChange() {
            if (rdstaDialog != null && rdstaDialog.isShowing()){
                RadioMessage rdsInfo = mRadioStatusTool.getCurrentRadioMessage();
                Log.d(TAG, "showTADialog,onCurrentRadioMessageChange,rdsInfo: " + rdsInfo);
                rdstaDialog.updateTAContent(rdsInfo != null && rdsInfo.getRdsRadioText() != null && rdsInfo.getRdsRadioText().getRadioText().length() > 0 ? rdsInfo.getRdsRadioText().getRadioText() : rdsInfo.getCalculateFrequencyAndUnit());
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

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {

        }

        @Override
        public void onRDSAnnNotify(RDSAnnouncement rdsAnnouncement) {
                Log.d(TAG,"onRDSAnnNotify,rdsAnnouncement:"+rdsAnnouncement);
                //用于进入STR时，重置弹窗
                if (rdsAnnouncement.getStatus() == -1){
                    switchFLag = -1;
                }
                showTADialog(rdsAnnouncement);
        }
    };

    private void showTADialog(RDSAnnouncement rdsAnnouncement){
        boolean active = rdsAnnouncement.getStatus() == RDSAnnouncement.ANNOUNCEMENT_TYPE_SHOW;
        if (active && canShowFM()) {
                RadioMessage rdsInfo = mRadioStatusTool.getCurrentRadioMessage();
                Log.d(TAG, "showTADialog,rdsInfo: " + rdsInfo);
                if (rdstaDialog != null) {
                    if (!rdstaDialog.isShowing()) {
                        rdstaDialog.show();
                        rdstaDialog.updateTATitle(rdsAnnouncement);
                        rdstaDialog.updateTAContent(rdsInfo != null && rdsInfo.getRdsRadioText() != null && rdsInfo.getRdsRadioText().getRadioText().length() > 0 ? rdsInfo.getRdsRadioText().getRadioText() : rdsInfo.getCalculateFrequencyAndUnit());
                        sendBroadcastToAA(VALUE_TA_OPEN);
                        switchFMIfNeed();
                    }
                }
        }else {
            if (rdstaDialog != null) {
                rdstaDialog.dismiss();
                //switchToPreSource(switchFLag,false);
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,"onConfigurationChanged:"+newConfig);
        if (rdstaDialog != null){//只需要判断一个即可
            rdstaDialog.reShowDialog();
        }
    }

    /**
     * TA的弹窗，需要高优先级弹出并切换到FM/DAB输出
     * 因为交互场景太多，目前先做成媒体焦点类型时弹出
     * 非媒体焦点后续再看如何处理
     * @return
     */


    private boolean canShowFM(){
        needPause = CurrentRadioInfo.getInstance().getPauseReason() != ChangeReasonData.NA;
        Log.d(TAG,"canShowDAB,needPause: " +needPause);
        String currentCarAudioType = AudioFocusUtils.getInstance().getCurrentAudioSourceName(AppBase.mContext);
        Log.d(TAG,"canShowFM,currentCarAudioType: " +currentCarAudioType);
        int currentSourceId = AudioFocusUtils.getInstance().getCurrentSourceId();
        Log.d(TAG,"canShowFM,getCurrentSourceId: " +currentSourceId);
        if (currentSourceId == 43
                || currentSourceId == 30
                || currentSourceId == 32
                || currentSourceId == 201
                || currentSourceId == 63//CP通话
                || currentSourceId == 149//DAB TA的时候也不要弹窗了，容易混乱
                || RadioControlTool.getInstance().isPhoneCallState()){//倒车、通话不可弹窗
            return false;
        }
        //弹窗时，可能有连续多个TA消息上来，而且没有通知让上一个消失，这样会导致这个switchFLag被覆盖掉，从而导致消失弹窗后，不恢复播放的情况
        //因此判断是rds_tts的音源时，直接返回true即可，不要更改这个switchFLag
        if (currentSourceId == 148){
            return true;
        }else {
            switchFLag = -1;
        }
        if (currentCarAudioType != null && !checkAAisTopActivity()){
            if (DsvAudioSDKConstants.FM_SOURCE.equals(currentCarAudioType)){//当前源是FM，无需处理
                switchFLag = FLAG_SWITCH_FM;
            }else if (DsvAudioSDKConstants.AM_SOURCE.equals(currentCarAudioType)){
                switchFLag = FLAG_SWITCH_AM;
            }else if (DsvAudioSDKConstants.DAB_SOURCE.equals(currentCarAudioType)){
                switchFLag = FLAG_SWITCH_DAB;
            }else if("mute_media".equals(currentCarAudioType) || "mute_hardkey".equals(currentCarAudioType)){//如果是mute_media/mute_hardkey，那就判断一下mute之前的是哪个焦点
                String preCarAudioType = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext);
                Log.d(TAG,"canShowFM,preCarAudioType: " +preCarAudioType);
                if (DsvAudioSDKConstants.FM_SOURCE.equals(preCarAudioType)){
                    switchFLag = -1;
                }else {
                    switchFLag = FLAG_SWITCH_OTHER;
                }
            } else {
                switchFLag = FLAG_SWITCH_OTHER;
            }

            return true;
        }else {
            return false;
        }
    }


    /**
     * 切到FM以播放 TA 的内容
     */
    private void switchFMIfNeed(){
        if (switchFLag != -1){
            mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                    ChangeReasonData.RADIO_TTS, mRadioStatusTool.getFMRadioMessage());
        }
    }

    /**
     * 弹窗消失的时候，切换回上一个音源
     * @param switchFLag，需要切回的音源标志
     * @param isDAB，是否是DAB的弹窗
     */
    private void switchToPreSource(int switchFLag, boolean isDAB){
        Log.d(TAG,"switchToPreSource: "+switchFLag + ",isDAB:" + isDAB);
        if (switchFLag != -1) {
            if (isDAB) {
                mRadioControl.processCommand(RadioAction.RELEASE,
                        ChangeReasonData.SOURCE, mRadioStatusTool.getDABRadioMessage());
            } else {
                mRadioControl.processCommand(RadioAction.RELEASE,
                        ChangeReasonData.SOURCE, mRadioStatusTool.getFMRadioMessage());
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
                mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                        needPause ? ChangeReasonData.TTS_RESUME_PAUSE : ChangeReasonData.TTS_RESUME, mRadioStatusTool.getDABRadioMessage());
                break;
            case FLAG_SWITCH_OTHER:
                if (isDAB){
                    mRadioControl.processCommand(RadioAction.RELEASE,
                            ChangeReasonData.SOURCE, mRadioStatusTool.getDABRadioMessage());
                }else {
                    mRadioControl.processCommand(RadioAction.RELEASE,
                            ChangeReasonData.SOURCE, mRadioStatusTool.getFMRadioMessage());
                }
                break;
            default:
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

}