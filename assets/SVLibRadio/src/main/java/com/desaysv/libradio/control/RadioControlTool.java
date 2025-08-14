package com.desaysv.libradio.control;

import android.hardware.radio.RadioManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.libradio.action.RadioControlAction;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.rds.RDSAnnouncement;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IPlayControl;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;

import org.jetbrains.annotations.Nullable;


/**
 * Created by LZM on 2019-7-10.
 * Comment 媒体的控制逻辑，业务逻辑的子类，由于项目需求导致的逻辑不符合，可以在这里扩展
 */
public class RadioControlTool implements IControlTool {
    private static final String TAG = "RadioControlTool";
    private static RadioControlTool instance;
    private final IPlayControl mRadioControlTool;
    private final CurrentRadioInfo mCurrentRadioInfo;
    private static boolean hasMulti = false;
    //焦点获取后是否需要
    private boolean resumeOnFocusGain = true;
    private final Handler mHandler;
    private final Handler mSynHandler;
    private static final int DEFAULT_WHAT = 1;
    private final AudioManager.OnAudioFocusChangeListener mAMOnAudioFocusChangeListener = new FocusChangeListener();
    private final AudioManager.OnAudioFocusChangeListener mFMOnAudioFocusChangeListener = new FocusChangeListener();
    private final AudioManager.OnAudioFocusChangeListener mDabOnAudioFocusChangeListener = new FocusChangeListener();

    private final AudioManager.OnAudioFocusChangeListener mFMTTSOnAudioFocusChangeListener = new FocusChangeListener();
    private final AudioManager.OnAudioFocusChangeListener mDabTTSOnAudioFocusChangeListener = new FocusChangeListener();

    private RadioMessage mCurrentRadioMessageBeforeAst;

    public static RadioControlTool getInstance() {
        if (instance == null) {
            synchronized (RadioControlTool.class) {
                if (instance == null) {
                    instance = new RadioControlTool();
                }
            }
        }
        return instance;
    }

    private RadioControlTool() {
        mRadioControlTool = RadioControlAction.getInstance();
        mCurrentRadioInfo = CurrentRadioInfo.getInstance();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dealMessage(msg);
            }
        };
        HandlerThread synThread = new HandlerThread(TAG + "synThread");
        //搜台时 thread被占用，对于一些需要同步执行的action,新建一个synThread同步执行。
        synThread.start();
        mSynHandler = new Handler(synThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dealMessageNow(msg);
            }
        };
        hasMulti = CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }

    /**
     * 统一接收外部控制的逻辑入口
     *
     * @param radioAction  控制动作 上一曲，下一曲等，具体看MediaAction
     * @param changeReason 发送控制的原因，具体看ChangeReasonData
     */
    @Override
    public void processCommand(RadioAction radioAction, ChangeReason changeReason) {
        Log.d(TAG, "processCommand: radioAction = " + radioAction + " changeReason = " + changeReason);
        RadioControlAction.getInstance().setIsMultiSearchInterrupt(true);
        RadioControlAction.getInstance().notifyMultiSearchModeLock();
        RadioControlAction.getInstance().notifySearchModeLock(true);
        mHandler.sendMessage(mHandler.obtainMessage(DEFAULT_WHAT, new MessageData(radioAction, changeReason)));
    }

    /**
     * 统一接收外部控制的逻辑入口
     *
     * @param radioAction  控制动作 OPEN_RADIO，需要传入radioMessage
     * @param changeReason 发送控制的原因，具体看ChangeReasonData
     * @param radioMessage 需要播放的收音信息
     */
    @Override
    public void processCommand(final RadioAction radioAction, final ChangeReason changeReason, final RadioMessage radioMessage) {
        Log.d(TAG, "processCommand: radioAction = " + radioAction + " changeReason = " + changeReason + " radioMessage = " + radioMessage);
//        if (radioAction == RadioAction.CANCEL_COLLECT || radioAction == RadioAction.COLLECT) {
//            mSynHandler.sendMessage(mSynHandler.obtainMessage(DEFAULT_WHAT, new MessageData(radioAction, changeReason, radioMessage)));
//        } else {
        if (radioAction == RadioAction.OPEN_RADIO) {
            RadioControlAction.getInstance().setIsMultiSearchInterrupt(true);
            RadioControlAction.getInstance().notifyMultiSearchModeLock();
            RadioControlAction.getInstance().notifySearchModeLock(true);

            //当出现异常情况导底层没有返回onProgramInfoChanged时，导致应用一直在等锁时，考虑在用户主动操作的时候，主动进行解锁操作，避免应用一直无法使用的情况出现-补丁
            if (changeReason != ChangeReasonData.BOOT_RESUME && changeReason != ChangeReasonData.BOOT_RESUME_NOT_PLAY) {
                RadioControlAction.getInstance().forceNotifyLock();
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(DEFAULT_WHAT, new MessageData(radioAction, changeReason, radioMessage)));
//        }
    }

    /**
     * 命令的执行模式
     *
     * @param radioAction  控制动作 上一曲，下一曲等，具体看MediaAction
     * @param changeReason 发送控制的原因，具体看ChangeReasonData
     * @param object       Object     控制的参数,传递任意参数，由具体的业务线约定裁决
     */
    @Override
    public void processCommand(RadioAction radioAction, ChangeReason changeReason, Object object) {
        Log.d(TAG, "processCommand: radioAction = " + radioAction + " changeReason = " + changeReason + " object = " + object);
        if (radioAction == RadioAction.SET_RDS_SETTING_SWITCH) {
            mSynHandler.removeMessages(DEFAULT_WHAT);
            mSynHandler.sendMessage(mHandler.obtainMessage(DEFAULT_WHAT, new MessageData(radioAction, changeReason, object)));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(DEFAULT_WHAT, new MessageData(radioAction, changeReason, object)));
        }
    }

    private void dealMessageNow(Message message) {
        MessageData messageData = (MessageData) message.obj;
        RadioAction radioAction = messageData.radioAction;
        ChangeReason changeReason = messageData.changeReason;
        RadioMessage radioMessage = messageData.radioMessage;
        Log.d(TAG, "dealMessageNow: radioAction = " + radioAction + " changeReason = " + changeReason + " radioMessage = " + radioMessage);
        switch (radioAction) {
            case COLLECT:
                if (isEnableChangeRadioCollect()) {
                    collectRadioMessage(radioMessage);
                }
                break;
            case CANCEL_COLLECT:
                if (isEnableChangeRadioCollect()) {
                    cancelCollectRadioMessage(radioMessage);
                }
                break;
            case SET_RDS_SETTING_SWITCH:
                mRadioControlTool.setRDSSettingsStatus((RDSSettingsSwitch) messageData.object);
                break;
        }
    }

    /**
     * 将控制命令移入子线程，实现消息队列的处理
     *
     * @param message 传入的消息
     */
    private void dealMessage(Message message) {
        MessageData messageData = (MessageData) message.obj;
        RadioAction radioAction = messageData.radioAction;
        ChangeReason changeReason = messageData.changeReason;
        RadioMessage radioMessage = messageData.radioMessage;
        Log.i(TAG, "dealMessage: radioAction = " + radioAction + " changeReason = " + changeReason + " radioMessage = " + radioMessage);
        switch (radioAction) {
            case OPEN_RADIO:
                currentOpenReason = changeReason;
                if (changeReason == ChangeReasonData.BOOT_RESUME || changeReason == ChangeReasonData.BOOT_RESUME_NOT_PLAY){
                    RadioControlAction.getInstance().waitRadioInitSuccess();
                }
                if (changeReason == ChangeReasonData.TTS_RESUME || changeReason == ChangeReasonData.TTS_RESUME_PAUSE){//TTS恢复的，需要通知底层Open一次，以切换band
                    requestFocus(radioMessage);
                    mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
                    mRadioControlTool.openRadio(radioMessage);
                }else if (isEnableOpen(changeReason, radioMessage)) {
                    //这里要设置一下当前播放的频率，不然后面播放的时候申请音频焦点的话，就没办法获取到当前播放的收音信息了
                    //这里先注释掉试一下，理论上不会有问题，因为后面播放都是要等到tuned之后，才能播放的，如果有问题,这里要打开,
                    //并且这个CurrentRadioMessage是变化就会设置的,所以应该没有问题
                    //那要想办法搜台过程中,切换AM和FM导致的界面闪动
                    if (changeReason == ChangeReasonData.AUTO_SEEK || changeReason == ChangeReasonData.UI_START
                            || changeReason == ChangeReasonData.VR_CONTROL
                            || changeReason == ChangeReasonData.BOOT_RESUME
                            || changeReason == ChangeReasonData.BOOT_RESUME_NOT_PLAY
                            || changeReason == ChangeReasonData.CLICK
                            || ((changeReason == ChangeReasonData.CLICK_ITEM
                                || changeReason == ChangeReasonData.NEXT || changeReason == ChangeReasonData.PRE) && isMultiAMSwitch(radioMessage))) {//刻度尺滑动、TAB切页导致的变化需要马上设置，避免抖动
                        Log.i(TAG, "dealMessage: radioAction = " + "setCurrentRadioMessage ");
                        mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
                    }
                    if (changeReason == ChangeReasonData.RADIO_TTS){
                        mRadioControlTool.openTTSRadio(radioMessage);
                    }else {
                        mRadioControlTool.openRadio(radioMessage);
                        if (changeReason == ChangeReasonData.BOOT_RESUME_NOT_PLAY) {
                            mCurrentRadioInfo.setPauseReason(ChangeReasonData.BOOT_RESUME_NOT_PLAY);
                        } else {
                            mCurrentRadioInfo.setPauseReason(ChangeReasonData.NA);
                        }
                    }
                }
                break;
            case AST:
                SPUtlis.getInstance().saveShowCollectListMode(false);
                currentAstReason = changeReason;
                mRadioControlTool.ast();
                break;
            case SPECIFIES_AST:
                //SPECIFIES_AST不需要判断焦点，后面播放有做判断
                if (/*isEnableRequestFocus(changeReason, radioMessage)*/true) {
                    isEnableRequestFocus(changeReason, radioMessage);
                    currentAstReason = changeReason;
                    mRadioControlTool.specifiesAst(radioMessage);
                }
                break;
            case BACKGROUND_AST:
                currentAstReason = changeReason;
                mRadioControlTool.specifiesAst(radioMessage);
                break;
            case STOP_AST:
                mRadioControlTool.stopAst();
                break;
            case PLAY:
                if (isEnablePlay(changeReason)) {
                    mCurrentRadioInfo.setPauseReason(ChangeReasonData.NA);
                    if (changeReason == ChangeReasonData.RADIO_TTS){
                        mRadioControlTool.playTTS();
                    }else {
                        mRadioControlTool.play();
                    }
                }
                break;
            case PAUSE:
                mRadioControlTool.pause();
                if (isEnablePause(changeReason)) {
                    mCurrentRadioInfo.setPauseReason(changeReason);
                }
                break;
            case PLAY_OR_PAUSE:
                playOrPause(changeReason);
                break;
            case SEEK_FORWARD:
                if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                    mRadioControlTool.multiSeekForward();
                } else {
                    if (isEnableRequestFocus(changeReason)) {
                        mRadioControlTool.seekForward();
                    }
                }
                break;
            case SEEK_BACKWARD:
                if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                    mRadioControlTool.multiSeekBackward();
                } else {
                    if (isEnableRequestFocus(changeReason)) {
                        mRadioControlTool.seekBackward();
                    }
                }
                break;
            case STEP_FORWARD:
                if (isEnableRequestFocus(changeReason)) {
                    mRadioControlTool.stepForward();
                }
                break;
            case STEP_BACKWARD:
                if (isEnableRequestFocus(changeReason)) {
                    mRadioControlTool.stepBackward();
                }
                break;
            case AST_ALL:
                astAll();
                break;
            case CHANGE_COLLECT:
                if (isEnableChangeRadioCollect()) {
                    changeCollectRadioMessage(radioMessage);
                }
                break;
            case COLLECT:
                if (isEnableChangeRadioCollect()) {
                    collectRadioMessage(radioMessage);
                }
                break;
            case CANCEL_COLLECT:
                if (isEnableChangeRadioCollect()) {
                    cancelCollectRadioMessage(radioMessage);
                }
                break;
            case SET_RDS_SETTING_SWITCH:
                mRadioControlTool.setRDSSettingsStatus((RDSSettingsSwitch) messageData.object);
                break;
            case SET_DAB_ANN_SWITCH:
                mRadioControlTool.setDABAnnStatus((DABAnnSwitch) messageData.object);
                break;
            case START_STEP_MODE:
                //开始进入步进模式
                mRadioControlTool.startStepMode();
                break;
            case STOP_STEP_MODE:
                //退出快速步进模式
                mRadioControlTool.stopStepMode();
                break;
            case START_FAST_STEP_NEXT:
                //开始快进模式
                mRadioControlTool.startFastNextStep();
                break;
            case STOP_FAST_STEP_NEXT:
                //停止快进模式
                mRadioControlTool.stopFastNextStep();
                break;
            case START_FAST_STEP_PRE:
                //开始进入快退模式
                mRadioControlTool.startFastPreStep();
                break;
            case STOP_FAST_STEP_PRE:
                //退出快退模式
                mRadioControlTool.stopFastPreStep();
                break;
            case RELEASE:
                currentOpenReason = changeReason;
                if (isEnablePause(changeReason)){
                    mCurrentRadioInfo.setPauseReason(changeReason);
                }
                mRadioControlTool.pause();
                releaseFocusWithTTS(radioMessage);
                break;
            case GET_DAB_INFO:
                mCurrentRadioInfo.getCurrentRadioMessageWithEPG();
                break;
            case MULTI_AST:
                SPUtlis.getInstance().saveShowCollectListMode(false);
                if (searchEnableRequestFocus(mCurrentRadioInfo.getCurrentRadioMessage())){
                    currentAstReason = changeReason;
                    mRadioControlTool.multiAst();
                }
                break;
            case MULTI_SEEK_FORWARD:
                mRadioControlTool.multiSeekForward();
                break;
            case MULTI_SEEK_BACKWARD:
                mRadioControlTool.multiSeekBackward();
                break;
            case MULTI_STEP_FORWARD:
                mRadioControlTool.multiStepForward();
                break;
            case MULTI_STEP_BACKWARD:
                mRadioControlTool.multiStepBackward();
                break;
            case SET_QUALITY_CONDITION:
                mRadioControlTool.setQualityCondition((RadioParameter) messageData.object);
                break;
        }
    }

    /**
     * 是否是DAB/FM切换AM AM切换DAB/FM
     *
     * @return
     */
    private boolean isMultiAMSwitch(RadioMessage radioMessage) {
        if (SPUtlis.getInstance().getIsShowCollectListMode()) {
            if (hasMulti) {
                if ((mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_FM
                        || mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_FM_HD
                        || mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() == RadioMessage.DAB_BAND)
                        && (radioMessage.getRadioBand() == RadioManager.BAND_AM || radioMessage.getRadioBand() == RadioManager.BAND_AM_HD)) {
                    return true;
                }
                if ((radioMessage.getRadioBand() == RadioManager.BAND_FM
                        || radioMessage.getRadioBand() == RadioManager.BAND_FM_HD
                        || radioMessage.getRadioBand() == RadioMessage.DAB_BAND)
                        && (mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_AM || mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() == RadioManager.BAND_AM_HD)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 能否更改收音的收藏状态
     *
     * @return true：能够 false：不能
     */
    private boolean isEnableChangeRadioCollect() {
        boolean isTuned = mCurrentRadioInfo.getTuned();
        Log.d(TAG, "isEnableChangeRadioCollect: isTuned = " + isTuned);
        return true;//RDS电台会自动跳台(HardLink)，此时有可能不是Tune的
    }

    /**
     * 能否打开，这里不涉及音频焦点，只有在mute和unMute的时候才会涉及音频焦点
     *
     * @param changeReason 触发播放的原因
     * @param radioMessage 播放的当前频段
     * @return true 能够打开; false 不能打开
     */
    private boolean isEnableOpen(ChangeReason changeReason, RadioMessage radioMessage) {
        if (!((changeReason.getReason().equals(ChangeReasonData.RADIO_TTS.getReason()) && radioMessage.getRadioType() == RadioMessage.DAB_TYPE) || changeReason.getReason().equals(ChangeReasonData.MODE_CONTROL.getReason())
                || changeReason.getReason().equals(ChangeReasonData.BOOT_RESUME.getReason())
                || changeReason.getReason().equals(ChangeReasonData.BOOT_RESUME_NOT_PLAY.getReason())
                || changeReason.getReason().equals(ChangeReasonData.AST_CHANGE_PLAY.getReason()))
                && isOpenSameRadio(radioMessage, changeReason)) {
            return false;
        }
        if (changeReason.getReason().equals(ChangeReasonData.RADIO_TTS.getReason())){
            requestFocusWithTTS(radioMessage);
        }else {
            if (changeReason.getReason().equals(ChangeReasonData.TTS_RESUME.getReason())){//Radio_TTS导致的恢复打开操作，需要检查是否可以打破暂停
                if (changeReason.getPrority() > mCurrentRadioInfo.getPauseReason().getPrority()) {
                    Log.d(TAG,"TTS_RESUME, can not break pause");
                    return false;
                }
            }
            if (changeReason.getReason().equals(ChangeReasonData.CLICK_ITEM.getReason()) || changeReason.getReason().equals(ChangeReasonData.UI_START.getReason())|| changeReason.getReason().equals(ChangeReasonData.CLICK.getReason())){//列表选项点击时，需要判断焦点是否申请成功
                return requestFocus(radioMessage);
            }
            if (changeReason.getReason().equals(ChangeReasonData.AST_CHANGE_PLAY.getReason())){
                if (!checkCurrentRadioSource()) {
                    return false;
                }
            }
            requestFocus(radioMessage);
        }
        return true;
    }

    /**
     * 由于重复设置频点，出现pop音，所以这个需要加入
     *
     * @param radioMessage 打开的频率
     * @return true or false
     */
    private boolean isOpenSameRadio(RadioMessage radioMessage, ChangeReason changeReason) {
        Log.d(TAG, "isOpenSameRadio: radioMessage = " + radioMessage);
        RadioMessage currentRadio = mCurrentRadioInfo.getCurrentRadioMessage();
        Log.d(TAG, "isOpenSameRadio: getCurrentRadioMessage = " + currentRadio);
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE && currentRadio.getRadioType() == RadioMessage.DAB_TYPE) {
            //如果都是DAB电台，那么需要根据DAB的属性来判断
            if ((changeReason != ChangeReasonData.BOOT_RESUME || mCurrentRadioInfo.isPlaying()) && currentRadio.getDabMessage() != null && radioMessage.getDabMessage().getServiceId() == currentRadio.getDabMessage().getServiceId()
                    && radioMessage.getDabMessage().getFrequency() == currentRadio.getDabMessage().getFrequency()
                    && radioMessage.getDabMessage().getServiceComponentId() == currentRadio.getDabMessage().getServiceComponentId()) {
                processCommand(RadioAction.PLAY, changeReason);
                return true;
            }

        } else {
            if (radioMessage != null && mCurrentRadioInfo.getCurrentRadioMessage() != null &
                    radioMessage.getRadioFrequency() == mCurrentRadioInfo.getCurrentRadioMessage().getRadioFrequency()) {
                //打开相同的频率，直接解除mute就好
                processCommand(RadioAction.PLAY, changeReason);
                return true;
            }
        }
        return false;
    }


    /**
     * 播放或者暂停，里面做了判断播放状态的逻辑
     *
     * @param changeReason 操作这个动作的原因
     */
    private void playOrPause(ChangeReason changeReason) {
        Log.d(TAG, "playOrPause: isPlaying = " + mCurrentRadioInfo.isPlaying());
        if (mCurrentRadioInfo.isPlaying()) {
            processCommand(RadioAction.PAUSE, changeReason);
        } else {
            processCommand(RadioAction.PLAY, changeReason);
        }
    }

    /**
     * 能否播放
     *
     * @param changeReason 触发播放的原因
     * @return true 能够播放; false 不能播放
     */
    private boolean isEnablePlay(ChangeReason changeReason) {
        Log.d(TAG, "isEnablePlay: changeReason = " + changeReason + " getPauseReason = " + mCurrentRadioInfo.getPauseReason());
        if (changeReason.getPrority() > mCurrentRadioInfo.getPauseReason().getPrority() ||
                (changeReason == ChangeReasonData.AST_CHANGE_PLAY && ChangeReasonData.TTS_RESUME_PAUSE == currentOpenReason) ||
                (changeReason == ChangeReasonData.AST_CHANGE_PLAY && ChangeReasonData.UI_START == currentAstReason && mCurrentRadioInfo.getPauseReason() != ChangeReasonData.NA)) {
            return false;
        }
        //add by lzm 如果是由于搜索完 成导致的，而且音频焦点还不在自己手上，就不能恢复播放了
        if (changeReason == ChangeReasonData.AST_CHANGE_PLAY || changeReason == ChangeReasonData.RECOVER_SOURCE) {
            if (changeReason == ChangeReasonData.RECOVER_SOURCE){//时序问题，初始化恢复播放会导致打断搜索
                if (mCurrentRadioInfo.isSearching()){
                    return false;
                }
            }
            if (!checkCurrentRadioSource()) {
                return false;
            }
        }
        if (changeReason == ChangeReasonData.RADIO_TTS){
            //实际路试情况来看，这里不应该根据当前的来，因为可能当前是DAB(FM)，但是TA是FM(DAB)的，
            //TA时申请的焦点是肯定能申请到的，申请不到的情况(通话/倒车等)已经做了过滤，这里直接返回true即可
            if (mCurrentRadioInfo.getCurrentRadioMessage().getRadioType() != RadioMessage.DAB_TYPE) {
                requestFocusWithTTS(mCurrentRadioInfo.getCurrentRadioMessage());
            }
            return true;
        }
        return requestFocus(mCurrentRadioInfo.getCurrentRadioMessage());
    }

    /**
     * 检测搜索结束之后，能否解除静音，只要在FM的音频焦点下，就可以解除
     *
     * @return boolean
     */
    public boolean checkCurrentRadioSource() {
        boolean isHasFocus =
                (AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.AM_SOURCE) == AudioManager.AUDIOFOCUS_GAIN
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.FM_SOURCE) == AudioManager.AUDIOFOCUS_GAIN
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.DAB_SOURCE) == AudioManager.AUDIOFOCUS_GAIN
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.AM_SOURCE) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.FM_SOURCE) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.DAB_SOURCE) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
        Log.d(TAG, "checkCurrentRadioSource: isHasFocus = " + isHasFocus);
        return isHasFocus;
    }

    /**
     * 进入STR的时候，判断是否处于Radio的音源
     * @return
     */
    public boolean checkCurrentRadioSourceWithSTR() {
        boolean isHasFocus =
                (AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.AM_SOURCE) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.FM_SOURCE) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.DAB_SOURCE) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.AM_SOURCE) == AudioManager.AUDIOFOCUS_GAIN
                || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.FM_SOURCE) == AudioManager.AUDIOFOCUS_GAIN
                || AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.DAB_SOURCE) == AudioManager.AUDIOFOCUS_GAIN);
        Log.d(TAG, "checkCurrentRadioSourceWithSTR: isHasFocus = " + isHasFocus);
        return isHasFocus;
    }

    /**
     * 能否暂停
     *
     * @param changeReason 触发播放的原因
     * @return true 能够暂停; false 不能暂停
     */
    private boolean isEnablePause(ChangeReason changeReason) {
        Log.d(TAG, "isEnablePause: changeReason = " + changeReason);
        if (changeReason.getPrority() > mCurrentRadioInfo.getPauseReason().getPrority()) {
            return false;
        }
        return true;
    }

    /**
     * 能否申请焦点
     * 用于切换上下电台/上下step的状态时先申请焦点
     *
     * @param changeReason 触发的原因
     * @return true 能够暂停; false 不能暂停
     */
    private boolean isEnableRequestFocus(ChangeReason changeReason) {
        return isEnableRequestFocus(changeReason, mCurrentRadioInfo.getCurrentRadioMessage());
    }

    private boolean isEnableRequestFocus(ChangeReason changeReason, RadioMessage radioMessage) {
        Log.d(TAG, "isEnableRequestFocus: changeReason = " + changeReason + " radioMessage = " + radioMessage);
        if (changeReason.getPrority() > mCurrentRadioInfo.getPauseReason().getPrority() && (radioMessage.getRadioBand() == mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand())) {
            return false;
        }
        return requestFocus(radioMessage);
    }

    private boolean searchEnableRequestFocus(RadioMessage radioMessage) {
        Log.d(TAG, "searchEnableRequestFocus: radioMessage = " + radioMessage);
        return requestFocus(radioMessage);
    }

    /**
     * 搜索全部有效电台
     * 1. 记录当前播放的电台
     * 2. 跳转到FM的电台（这个过程不能申请音频焦点）
     * 3. 搜索,然后在回调里面搞事情
     */
    private void astAll() {
        Log.d(TAG, "astAll: ");
        //将当前播放的电台进行clone
        mCurrentRadioMessageBeforeAst = mCurrentRadioInfo.getCurrentRadioMessage().Clone();
        mCurrentRadioInfo.registerRadioStatusChangeListener(iRadioStatusChange);
        processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AST_ALL, mCurrentRadioInfo.getFMRadioMessage());
        processCommand(RadioAction.AST, ChangeReasonData.AST_ALL);

    }

    /**
     * 电台状态变化回调处理
     */
    private final IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {

        }


        @Override
        public void onPlayStatusChange(boolean isPlaying) {

        }

        /**
         * 回调里面要做的申请
         * 1. 如果FM搜索完毕后，打开AM，然后再次搜索
         * 2. 如果是AM搜索完毕，那就打开之前的记忆的电台
         * @param band
         */
        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged: band = " + band);
            if (band == RadioManager.BAND_FM) {
                processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AST_ALL, mCurrentRadioInfo.getAMRadioMessage());
                processCommand(RadioAction.AST, ChangeReasonData.AST_ALL);
            } else if (band == RadioManager.BAND_AM) {
                Log.d(TAG, "onAstListChanged: mCurrentRadioMessageBeforeAst = " + mCurrentRadioMessageBeforeAst);
                mCurrentRadioInfo.unregisterRadioStatusChangerListener(iRadioStatusChange);
                processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AST_ALL, mCurrentRadioMessageBeforeAst);
            }
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
    };

    /**
     * 请求焦点
     *
     * @return true 请求焦点成功 false 请求焦点失败
     */
    private boolean requestFocus(RadioMessage radioMessage) {
        int status = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        Log.d(TAG, "requestFocus: currentRadioMessage = " + radioMessage);
        if (radioMessage == null) {
            return false;
        }

        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    status = AudioFocusUtils.getInstance().requestFocus(AudioAttributes.SOURCE_AM, mAMOnAudioFocusChangeListener);
                    break;
                case RadioManager.BAND_FM:
                    status = AudioFocusUtils.getInstance().requestFocus(AudioAttributes.SOURCE_FM, mFMOnAudioFocusChangeListener);
                    break;
            }
        } else {
            status = AudioFocusUtils.getInstance().requestFocus(AudioAttributes.SOURCE_DAB, mDabOnAudioFocusChangeListener);
        }
        Log.d(TAG, "requestFocus: status = " + status);
        return status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 请求 Radio TTS 焦点
     *
     * @return true 请求焦点成功 false 请求焦点失败
     */
    private boolean requestFocusWithTTS(RadioMessage radioMessage) {
        int status = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        Log.d(TAG, "requestFocusWithTTS: currentRadioMessage = " + radioMessage);
        if (radioMessage == null) {
            return false;
        }

        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    break;
                case RadioManager.BAND_FM:
                    //后面要改成SOURCE_RDS_TTS，目前设置这个会导致没有声音
                    status = AudioFocusUtils.getInstance().requestFocusWithTTS(AudioAttributes.SOURCE_RDS_TTS, mFMTTSOnAudioFocusChangeListener);
                    break;
            }
        } else {
            //后面要改成SOURCE_DAB_TTS，目前设置这个会导致没有声音
            status = AudioFocusUtils.getInstance().requestFocusWithTTS(AudioAttributes.SOURCE_DAB_TTS, mDabTTSOnAudioFocusChangeListener);
        }
        Log.d(TAG, "requestFocusWithTTS: status = " + status);
        if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            hadTTS = true;
        }
        return status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    /**
     * 释放 Radio TTS 焦点
     *
     * @return true 释放焦点成功 false 释放焦点失败
     */
    private void releaseFocusWithTTS(RadioMessage radioMessage) {
        Log.d(TAG, "releaseFocusWithTTS: currentRadioMessage = " + radioMessage);
        if (radioMessage == null) {
            return ;
        }
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    break;
                case RadioManager.BAND_FM:
                    AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_RDS_TTS, mFMTTSOnAudioFocusChangeListener);
                    break;
            }
        } else {
             AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_DAB_TTS, mDabTTSOnAudioFocusChangeListener);
        }
    }

    /**
     * 进入STR的时候
     * @param radioMessage
     */
    public void releaseFocusWithSTR(RadioMessage radioMessage) {
        Log.d(TAG, "releaseFocusWithSTR: currentRadioMessage = " + radioMessage);
        if (radioMessage == null) {
            return ;
        }
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_AM, mAMOnAudioFocusChangeListener);
                    break;
                case RadioManager.BAND_FM:
                    AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_FM, mFMOnAudioFocusChangeListener);
                    break;
            }
        } else {
            AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_DAB, mDabOnAudioFocusChangeListener);
        }
    }



    private class FocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange: focusChange = " + focusChange);
            int sourceId = AudioFocusUtils.getInstance().getCurrentSourceId();
            Log.d(TAG, "onAudioFocusChange: currentSource = " + sourceId);
            boolean needStop = AudioAttributes.SOURCE_FM != sourceId
                    && AudioAttributes.SOURCE_AM != sourceId
                    && AudioAttributes.SOURCE_DAB != sourceId
                    && AudioAttributes.SOURCE_DAB_TTS != sourceId
                    && AudioAttributes.SOURCE_RDS_TTS != sourceId;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (needStop) {
                        resumeOnFocusGain = false;
                        processCommand(RadioAction.STOP_AST, ChangeReasonData.SOURCE);
                        processCommand(RadioAction.PAUSE, ChangeReasonData.SOURCE);
                        if (AudioAttributes.SOURCE_PHONE == sourceId
                            ||AudioAttributes.SOURCE_RING == sourceId
                            ||AudioAttributes.SOURCE_RVC == sourceId
                            ||AudioAttributes.SOURCE_CP_PHONE == sourceId
                            ||AudioAttributes.SOURCE_POWER == sourceId) {
                            notifyLossForTTS();
                        }
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (AudioAttributes.SOURCE_DAB_TTS != sourceId
                        && AudioAttributes.SOURCE_RDS_TTS != sourceId){
                        resumeOnFocusGain = true;
                        if (!getAstReason().equals(ChangeReasonData.VR_CONTROL)){
                            processCommand(RadioAction.STOP_AST, ChangeReasonData.SOURCE);
                        }
                        processCommand(RadioAction.PAUSE, ChangeReasonData.SOURCE);
                        if (AudioAttributes.SOURCE_PHONE == sourceId
                                ||AudioAttributes.SOURCE_RING == sourceId
                                ||AudioAttributes.SOURCE_RVC == sourceId
                                ||AudioAttributes.SOURCE_POWER == sourceId
                                ||AudioAttributes.SOURCE_CP_PHONE == sourceId
                                ||AudioAttributes.SOURCE_CP_RING == sourceId
                                ||AudioAttributes.SOURCE_MEDIA_MUTE == sourceId) {
                            notifyLossForTTS();
                        }
                    }else {
                        resumeOnFocusGain = false;//TTS不需要恢复播放，交由OPEN处理
                    }

                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    //如果是由于退出STR导致的焦点返回，则不需要播放，后置到STR的监听去处理
                    if (resumeOnFocusGain && !RadioControlAction.getInstance().isfromSTR() && (AudioAttributes.SOURCE_DAB_TTS != sourceId
                            && AudioAttributes.SOURCE_RDS_TTS != sourceId)) {
                        processCommand(RadioAction.PLAY, ChangeReasonData.SOURCE);
                    }
                    break;
            }
        }
    }

    /**
     * add by lzm 需要实现消息在子线程中实现，所以用Handler实现一个消息队列
     */
    private static class MessageData {
        RadioAction radioAction;
        ChangeReason changeReason;

        @Nullable
        public RadioMessage radioMessage;

        public Object object;

        MessageData(RadioAction radioAction, ChangeReason changeReason) {
            this.radioAction = radioAction;
            this.changeReason = changeReason;
        }

        MessageData(RadioAction radioAction, ChangeReason changeReason, RadioMessage radioMessage) {
            this.radioAction = radioAction;
            this.changeReason = changeReason;
            this.radioMessage = radioMessage;
        }

        MessageData(RadioAction radioAction, ChangeReason changeReason, Object object) {
            this.radioAction = radioAction;
            this.changeReason = changeReason;
            this.object = object;
        }
    }

    /**
     * 修改选中电台的收藏主题
     *
     * @param radioMessage 选择的电台
     */
    private void changeCollectRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "collectRadioMessage: radioMessage = " + radioMessage);
        if (radioMessage != null) {
            mCurrentRadioInfo.setCurrentCollectRadioMessage(radioMessage);
            RadioList.getInstance().updateCollectList(radioMessage);
        }
    }

    /**
     * 收藏选中的电台
     *
     * @param radioMessage 选中的电台
     */
    private void collectRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "collectRadioMessage: radioMessage = " + radioMessage);
        if (radioMessage != null) {
            mCurrentRadioInfo.setCurrentCollectRadioMessage(radioMessage);
            RadioList.getInstance().collectRadioMessage(radioMessage);
        }
    }

    /**
     * 取消收藏选中的电台
     *
     * @param radioMessage 选中的电台
     */
    private void cancelCollectRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "cancelCollectRadioMessage: radioMessage = " + radioMessage);
        if (radioMessage != null) {
            mCurrentRadioInfo.setCurrentCollectRadioMessage(radioMessage);
            RadioList.getInstance().cancelCollectRadioMessage(radioMessage);
        }
    }

    /**
     * 焦点丢失的时候，需要通知弹窗消失
     */
    public void notifyLossForTTS(){

        RDSAnnouncement rdsAnnouncement = new RDSAnnouncement();
        rdsAnnouncement.setAnnounceType(2);
        rdsAnnouncement.setStatus(0);
        mCurrentRadioInfo.onRDSAnnChange(rdsAnnouncement);

        DABAnnNotify dabAnnNotify = new DABAnnNotify();
        dabAnnNotify.setAnnounceType(0);
        dabAnnNotify.setStatus(0);
        mCurrentRadioInfo.setAnnNotifyStatus(dabAnnNotify);
    }

    /**
     * 进入的时候，需要通知弹窗消失
     */
    public void notifyLossForTTSWithSTR(){

        RDSAnnouncement rdsAnnouncement = new RDSAnnouncement();
        rdsAnnouncement.setAnnounceType(-1);
        rdsAnnouncement.setStatus(-1);
        mCurrentRadioInfo.onRDSAnnChange(rdsAnnouncement);

        DABAnnNotify dabAnnNotify = new DABAnnNotify();
        dabAnnNotify.setAnnounceType(-1);
        dabAnnNotify.setStatus(-1);
        mCurrentRadioInfo.setAnnNotifyStatus(dabAnnNotify);
    }

    private boolean isPhoneCallState = false;

    public boolean isPhoneCallState() {
        Log.d(TAG,"isPhoneCallState:"+isPhoneCallState);
        return isPhoneCallState;
    }

    public void setPhoneCallState(boolean phoneCallState) {
        Log.d(TAG,"setPhoneCallState:"+phoneCallState);
        isPhoneCallState = phoneCallState;
    }

    private boolean isShowingTTS = false;

    public boolean isShowingTTS() {
        Log.d(TAG,"isShowingTTS:"+isShowingTTS);
        return isShowingTTS;
    }

    public void setShowingTTS(boolean showingTTS) {
        isShowingTTS = showingTTS;
    }

    //是否申请过TTS且当前没有其它焦点
    private boolean hadTTS = false;
    public boolean checkHadRadioTTSWithSTR(){
        Log.d(TAG, "checkHadRadioTTSWithSTR: hadTTS = " + hadTTS);
        int currentSourceId =  AudioFocusUtils.getInstance().getCurrentSourceId();
        Log.d(TAG, "checkHadRadioTTSWithSTR: currentSourceId = " + currentSourceId);
        return hadTTS && (currentSourceId == -1);//这个条件满足，说明释放了Radio_TTS焦点之后，没有人持有焦点，导致通道还是在Tuner
    }
    //是否处于RDSTTS
    public boolean checkRDSTTSSourceWithSTR(){
        boolean isHasFocus = AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.RDS_TTS_SOURCE) == AudioManager.AUDIOFOCUS_GAIN;
        Log.d(TAG, "checkRDSTTSSourceWithSTR: isHasFocus = " + isHasFocus);
        return isHasFocus;
    }
    public void releaseRDSTTSFocusWithSTR(){
        Log.d(TAG, "releaseRDSTTSFocusWithSTR");
        AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_RDS_TTS, mFMTTSOnAudioFocusChangeListener);
    }
    //是否处于DABTTS
    public boolean checkDABTTSSourceWithSTR(){
        boolean isHasFocus = AudioFocusUtils.getInstance().checkAudioFocusStatus(AppBase.mContext, DsvAudioSDKConstants.DAB_TTS_SOURCE) == AudioManager.AUDIOFOCUS_GAIN;
        Log.d(TAG, "checkDABTTSSourceWithSTR: isHasFocus = " + isHasFocus);
        return isHasFocus;
    }

    public void releaseDABTTSFocusWithSTR(){
        Log.d(TAG, "releaseDABTTSFocusWithSTR");
        AudioFocusUtils.getInstance().releaseFocus(AudioAttributes.SOURCE_DAB_TTS, mFMTTSOnAudioFocusChangeListener);
    }

    private ChangeReason currentOpenReason;
    public ChangeReason getOpenReason(){
        return currentOpenReason;
    }

    private ChangeReason currentAstReason = ChangeReasonData.NA;
    public ChangeReason getAstReason(){
        return currentAstReason;
    }
}
