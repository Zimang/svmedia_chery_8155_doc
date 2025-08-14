package com.desaysv.libradio.action;

import android.annotation.SuppressLint;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioMetadata;
import android.hardware.radio.RadioTuner;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.JsonNode;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioEvent;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.dab.DABBerMessage;
import com.desaysv.libradio.bean.dab.DABEPGScheduleList;
import com.desaysv.libradio.bean.dab.DABHeadlineMessage;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.rds.RDSAnnouncement;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSRadioName;
import com.desaysv.libradio.bean.rds.RDSRadioNameList;
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.libradio.bean.rds.RDSRadioTextTemp;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IPlayControl;
import com.desaysv.libradio.interfaze.IPlayStatusTool;
import com.desaysv.libradio.utils.ASCIITools;
import com.desaysv.libradio.utils.JsonUtils;
import com.desaysv.libradio.utils.PinyinComparator;
import com.desaysv.libradio.utils.RadioConversionUtils;
import com.desaysv.libradio.utils.RadioEventValueUtils;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LZM
 * @date 2019-7-9
 * Comment 收音的实现类，实现了收音的播放控制
 * 电台初始化流程：
 * 1、先获取RadioTuner，
 * 2、根据 onEventNotify: eventID = 7 返回，判别，HAL已经初始化完成；
 * 3、HAL初始化完成后， tuner一个电台到HAL；
 * 4、在onProgramInfoChanged 第一次isTuned，判别为初始化成功。
 */
public class RadioControlAction implements IPlayControl, IPlayStatusTool {
    private static final String TAG = "RadioControlAction";
    private static RadioControlAction instance;
    /**
     * 是否已经进行初始化电台
     * HAL 通知，RadioTuner初始化完成，
     */
    private boolean initRadio = false;
    /**
     * RadioTuner初始化完成后，需要tuner一次电台值到HAL，逻辑；
     * 等待，打开电台成功后，才算正确的打开电台操作
     */
    public boolean initSuccess = false;
    /**
     * 判断是否是第一次操作。
     */
    private boolean initFirst = true;

    private final Object mLock = new Object();
    private final Object mInitLock = new Object();
    private MyHandler mHandler;
    private static final int RE_INIT_TIME = 500;
    private static final int RE_INIT_RADIO_CONTROL = 1;
    private RadioTuner mRadioTuner;
    private RadioManager.FmBandConfig mFmConfig;
    private RadioManager.AmBandConfig mAmConfig;
    private final List<RadioManager.ModuleProperties> mModules = new ArrayList<>();
    //是否初始化完成的标志位
    private boolean isInit = false;
    private static final String BROADCAST_RADIO = "broadcastradio";
    private final CurrentRadioInfo mCurrentRadioInfo;
    private final static int ERROR_FREQUENCY = -1;
    private final static int ERROR_BAND = -1;
    private int currentRadioType = RadioMessage.FM_AM_TYPE;

    //当前seek类型
    private int seekType = -1;
    private final static int SEEK_FORWARD = 0;
    private final static int SEEK_BACKWARD = 1;

    /**
     * 判断是否处于搜索模式
     * 默认，没有打开自动搜索操作
     */
    private boolean isSearchMode = false;
    private final Object mSearchModeLock = new Object();


    /**
     * 判断是否处于组合搜索模式
     */
    private boolean isMultiSearchMode = false;
    private final Object mMultiSearchModeLock = new Object();

    /**
     * 判断是否处于组合搜索DAB
     */
    private boolean isMultiSearchDAB = false;

    /**
     * 判断是否打断组合搜索
     */
    private boolean isMultiSearchInterrupt = false;

    private boolean hasDAB = false;
    private boolean hasMulti = false;
    public static RadioControlAction getInstance() {
        if (instance == null) {
            synchronized (RadioControlAction.class) {
                if (instance == null) {
                    instance = new RadioControlAction();
                }
            }
        }
        return instance;
    }

    private RadioControlAction() {
        mCurrentRadioInfo = CurrentRadioInfo.getInstance();
        mCurrentRadioInfo.setPlayStatusTool(this);
        hasDAB = CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
        hasMulti = hasDAB;//CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }

    /**
     * 打开收音机
     *
     * @param radioMessage 需要打开的收音信息
     */
    @Override
    public void openRadio(RadioMessage radioMessage) {
        Log.d(TAG, "openRadio: radioMessage = " + radioMessage);
        //由于初始化在handler的进程里面，而打开收音这个动作是有等待动作的，打开动作不能做在handler，不然会出现死锁
        actionOpenRadio(radioMessage);
    }

    /**
     * 由于要加锁，所以放到另外一个线程里面去做动作执行
     *
     * @param radioMessage 需要打开的收音信息
     */
    private void actionOpenRadio(RadioMessage radioMessage) {
        Log.d(TAG, "actionOpenRadio: radioMessage = " + radioMessage + " mRadioTuner = " + mRadioTuner);
        waitRadioInitSuccess();
        Log.d(TAG, "actionOpenRadio: mRadioTuner = " + mRadioTuner + " isSearching = " + mCurrentRadioInfo.isSearching()
                + " isSeeking = " + mCurrentRadioInfo.isSeeking() + " initSuccess = " + initSuccess);
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "actionOpenRadio: mRadioTuner is null");
            return;
        }
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mCurrentRadioInfo.isSearching()) {
            //由于是会出现进程间的操作，所以这里不能用抛消息的方式去stop
            stopAst();
        }else if (mCurrentRadioInfo.isSeeking()){
            stopSeek();
        }
        //打开之后，不能设置当前频率，需要等回调成功之后才设置
        tuneAndSetConfiguration(radioMessage,false);

        RadioList.getInstance().updatePlayList(radioMessage);
        isMultiSearchDAB  = false;
    }

    /**
     * 向前搜台
     */
    @Override
    public void seekForward() {
        Log.d(TAG, "seekForward: mRadioTuner = " + mRadioTuner + " isSearching = " + mCurrentRadioInfo.isSearching()
                + " isSeeking = " + mCurrentRadioInfo.isSeeking() + " initSuccess = " + initSuccess);
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "seekForward: mRadioTuner is null");
            return;
        }
        if (mCurrentRadioInfo.isSearching()) {
            RadioControlTool.getInstance().processCommand(RadioAction.STOP_AST, ChangeReasonData.CONTROL_ACTION);
            return;
        }else if (mCurrentRadioInfo.isSeeking()){
            stopSeek();
            return;
        }
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
            if (isStepMode) {
                //如果当前是在步进模式的话，那就不是跳转，而是步进,然后这里是执行逻辑了，直接跑
                RadioControlTool.getInstance().processCommand(RadioAction.STEP_FORWARD, ChangeReasonData.STEP_MODE);
                //以及重置退出步进模式的时间
                resetQuitStepModeTime();
                return;
            }
            mRadioTuner.scan(RadioTuner.DIRECTION_UP, true);
        } else {
            DABControlAction.getInstance().seekForward();
        }
        mCurrentRadioInfo.setSeeking(true);
        seekType = SEEK_FORWARD;
    }

    /**
     * 向后搜台
     */
    @Override
    public void seekBackward() {
        Log.d(TAG, "seekBackward: mRadioTuner = " + mRadioTuner + " isSearching = " + mCurrentRadioInfo.isSearching()
                + " isSeeking = " + mCurrentRadioInfo.isSeeking() + " initSuccess = " + initSuccess);
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "seekBackward: mRadioTuner is null");
            return;
        }
        if (mCurrentRadioInfo.isSearching()) {
            RadioControlTool.getInstance().processCommand(RadioAction.STOP_AST, ChangeReasonData.CONTROL_ACTION);
            return;
        }else if (mCurrentRadioInfo.isSeeking()){
            stopSeek();
            return;
        }
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
            if (isStepMode) {
                //如果当前是在步进模式的话，那就不是跳转，而是步进,然后这里是执行逻辑了，直接跑
                RadioControlTool.getInstance().processCommand(RadioAction.STEP_BACKWARD, ChangeReasonData.STEP_MODE);
                //以及重置退出步进模式的时间
                resetQuitStepModeTime();
                return;
            }
            mRadioTuner.scan(RadioTuner.DIRECTION_DOWN, true);
        } else {
            DABControlAction.getInstance().seekBackward();
        }
        mCurrentRadioInfo.setSeeking(true);
        seekType = SEEK_BACKWARD;
    }

    /**
     * 向前跳一步
     * 注DAB没有步进的说法
     */
    @Override
    public void stepForward() {
        Log.d(TAG, "stepForward: mRadioTuner = " + mRadioTuner);
        if (mRadioTuner != null && !mCurrentRadioInfo.isSearching() && initSuccess) {
            if (currentRadioType == RadioMessage.FM_AM_TYPE){
                mRadioTuner.step(RadioTuner.DIRECTION_UP, true);
            }else {
                DABControlAction.getInstance().stepForward();
            }
        }
    }

    /**
     * 向后跳一步
     * 注DAB没有步进的说法
     */
    @Override
    public void stepBackward() {
        Log.d(TAG, "stepBackward:  mRadioTuner = " + mRadioTuner);
        if (mRadioTuner != null && !mCurrentRadioInfo.isSearching() && initSuccess) {
            if (currentRadioType == RadioMessage.FM_AM_TYPE){
                mRadioTuner.step(RadioTuner.DIRECTION_DOWN, true);
            }else {
                DABControlAction.getInstance().stepBackward();
            }
        }
    }

    /**
     * 播放
     */
    @Override
    public void play() {
        Log.d(TAG, "play: mRadioTuner = " + mRadioTuner + " isSearching = " + mCurrentRadioInfo.isSearching()
                + " isSeeking = " + mCurrentRadioInfo.isSeeking() + " initSuccess = " + initSuccess);
        // 阻塞等待 初始化完成
        waitRadioInitSuccess();
        Log.d(TAG, "play: run ?");
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "play: mRadioTuner is null");
            return;
        }
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mCurrentRadioInfo.isSearching()) {
            //由于是会出现进程间的操作，所以这里不能用抛消息的方式去stop
            stopAst();
        }

        currentRadioType = mCurrentRadioInfo.getCurrentRadioMessage().getRadioType();
        Log.d(TAG, "play: currentRadioType is:"+currentRadioType);
        mRadioTuner.setMute(false);
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
            if (forceSwitchSource) {
                switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
                forceSwitchSource = false;
            }
//            ArrayList<Integer> int32Values = new ArrayList<>();
//            int32Values.add(0);//（1：启动mute  0 ：取消mute）
//            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
//            mCurrentRadioInfo.setPlaying(!mRadioTuner.getMute());
//            mCurrentRadioInfo.setPlaying(!getMute());
        } else {
            //初始时如果打开的是DAB，需要切一下，因为initRadio需要用到FM/AM来Tune，这个时候会切到FM_AM
            //从而导致DAB事件无效
            //多次切不会有影响，所以放到这里处理，后续可以考虑优化
            if (forceSwitchSource) {
                switchRadioSource(RadioEvent.RadioSourceType.DAB);
                forceSwitchSource = false;
            }
            //DABControlAction.getInstance().play();
        }
        ArrayList<Integer> int32Values = new ArrayList<>();
        int32Values.add(0);//（1：启动mute  0 ：取消mute）
        mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
        DABControlAction.getInstance().play();
        isMultiSearchDAB  = false;
        Log.d(TAG, "play: end");
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        Log.d(TAG, "pause: mRadioTuner = " + mRadioTuner + " isSearching = " + mCurrentRadioInfo.isSearching()
                + " isSeeking = " + mCurrentRadioInfo.isSeeking() + " initSuccess = " + initSuccess);
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "pause: mRadioTuner is null");
            return;
        }
        mRadioTuner.setMute(true);
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
//            ArrayList<Integer> int32Values = new ArrayList<>();
//            int32Values.add(1); //（1：启动mute  0 ：取消mute）
//            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
//            mCurrentRadioInfo.setPlaying(!mRadioTuner.getMute());
//            mCurrentRadioInfo.setPlaying(!getMute());
        } else {
            //DABControlAction.getInstance().pause();
        }
        ArrayList<Integer> int32Values = new ArrayList<>();
        int32Values.add(1); //（1：启动mute  0 ：取消mute）
        mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
        mCurrentRadioInfo.setPlaying(!mRadioTuner.getMute());
        DABControlAction.getInstance().pause();
        Log.d(TAG, "pause: end");
    }

    /**
     * 搜索有效电台
     */
    @Override
    public void ast() {
        Log.d(TAG, "ast: mRadioTuner = " + mRadioTuner + " isSearching = " + mCurrentRadioInfo.isSearching()
                + " isSeeking = " + mCurrentRadioInfo.isSeeking() + " initSuccess = " + initSuccess);
        waitRadioInitSuccess();
        Log.d(TAG, "ast: run ? ");
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "ast: mRadioTuner is null or not success");
            return;
        }
        Log.d(TAG, "ast: isSearchMode = " + isSearchMode);
        if (!isSearchMode) {
            if (mCurrentRadioInfo.isSearching()) {
                RadioControlTool.getInstance().processCommand(RadioAction.STOP_AST, ChangeReasonData.CONTROL_ACTION);
                return;
            }else if (mCurrentRadioInfo.isSeeking()){
                stopSeek();
                return;
            }
            mCurrentRadioInfo.setSearching(true);
        }
        mRadioTuner.setMute(true);
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
            //mRadioTuner.ast();
            ArrayList<Integer> int32Values = new ArrayList<>();
            int32Values.add(1);// 0/1 （1：启动 ； 0 ：取消） 开始AST
            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_AST_STATE_CTRL,
                    new RadioManager.RawValue(int32Values, null, null, null, null)));
        } else {
            DABControlAction.getInstance().ast();
        }
        Log.d(TAG, "ast: end");
    }

    /**
     * 停止搜索
     */
    @Override
    public void stopAst() {
        Log.d(TAG, "stopAst: mRadioTuner = " + mRadioTuner + " isSearchMode = " + isSearchMode + " isSearchSwitch = " + isSearchSwitch);
        if (mRadioTuner != null) {
            if (isMultiSearchMode){
                notifyMultiSearchModeLock();
                isMultiSearchMode = false;
                Log.d(TAG, "stopAst: stop MultiSearchMode");
            }
            // 当前处理自动搜索中
            if (isSearchMode) {
                Log.d(TAG, "stopAst: stop auto Search Mode");
                isSearchMode = false;
                if (!isSearchSwitch) {
                    // 当前执行了切换，释放锁定
                    isSearchSwitch = true;
                    notifySearchModeLock(false);
                    Log.d(TAG, "stopAst: Release the lock");
                }
            }

            if (mCurrentRadioInfo.isSearching()) {//搜索状态才去停止搜索，不然会出现一个问题：底层在处理停止搜索时，然后又发起一个打开电台的操作，此时概率性出现onProgramed不返回的情况
                //停止搜索的时候，也需要清空标志位
                Log.d(TAG, "stopAst: start");
                mCurrentRadioInfo.setSearching(false);
                mCurrentRadioInfo.setSeeking(false);
                if (hasMulti){
                    if (!isMultiSearchDAB) {
                        ArrayList<Integer> int32Values = new ArrayList<>();
                        int32Values.add(0);// 0/1 （1：启动 ； 0 ：取消）
                        mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_AST_STATE_CTRL,
                                new RadioManager.RawValue(int32Values, null, null, null, null)));
                    } else {
                        DABControlAction.getInstance().stopAst();
                    }
                }else {
                    if (currentRadioType == RadioMessage.FM_AM_TYPE) {
//                mRadioTuner.cancel();
                        ArrayList<Integer> int32Values = new ArrayList<>();
                        int32Values.add(0);// 0/1 （1：启动 ； 0 ：取消）
                        mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_AST_STATE_CTRL,
                                new RadioManager.RawValue(int32Values, null, null, null, null)));
                    } else {
                        DABControlAction.getInstance().stopAst();
                    }
                }
            }else if (mCurrentRadioInfo.isSeeking()){
                stopSeek();
            }
        }
        Log.d(TAG, "stopAst: end");
    }

    /**
     * 搜索指定电台类型
     *
     * @param radioMessage radioMessage
     */
    @Override
    public void specifiesAst(RadioMessage radioMessage) {
        Log.d(TAG, "specifiesAst: mRadioTuner = " + mRadioTuner + " radioMessage = " + radioMessage);
        waitRadioInitSuccess();
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "specifiesAst: return ");
            return;
        }
        if (null == radioMessage) {
            Log.e(TAG, "specifiesAst: return radioMessage is null");
            return;
        }

        // 静音
        Log.d(TAG, "specifiesAst: pause mute ");
        pause();

        // 0、先进行静音操作，
        // 1、先进行判断，是否需要切换频点
        // 2、如果需要切换频点， 则需要等待打开频点成功后
        // 3、搜索电台
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        // 启动自动搜索模式
        isSearchMode = true;
        mCurrentRadioInfo.setSearching(true);

        Log.d(TAG, "specifiesAst: isSearchMode = " + isSearchMode);
        RadioMessage currentRadioMessage = mCurrentRadioInfo.getCurrentRadioMessage();
        if (radioMessage.getRadioBand() != currentRadioMessage.getRadioBand() || radioMessage.getRadioType() != currentRadioMessage.getRadioType()) {
            Log.d(TAG, "specifiesAst: switch");
            // 需要切换到对应频点,然后
            mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
            tuneAndSetConfiguration(radioMessage,false);
            isSearchSwitch = false;
            waitSearchMode();
        }else {
            //切换band搜索时，需要切换一次通道，避免首次开机播放时，通道不正确
            if (currentRadioType == RadioMessage.FM_AM_TYPE){
                switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
            }else {
                switchRadioSource(RadioEvent.RadioSourceType.DAB);
            }
        }
        if (!isSearchMode) {
            Log.d(TAG, "specifiesAst: return current is not Search Mode");
            return;
        }

        // 等待解锁
        Log.d(TAG, "specifiesAst: to ast");
        ast();
        Log.d(TAG, "specifiesAst: end");
    }

    /**
     * 判断是否 执行切换
     * 1、如果处理切换中，则需要解锁后续的操作，执行搜素；
     * 默认不需要执行切换锁定
     */
    private boolean isSearchSwitch = true;

    /**
     *
     */
    private void waitSearchMode() {
        Log.d(TAG, "waitSearchMode: isSearchSwitch = " + isSearchSwitch);
        if (!isSearchSwitch) {
            synchronized (mSearchModeLock) {
                try {
                    mSearchModeLock.wait();
                    Log.d(TAG, "waitSearchMode: wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notifySearchModeLock(boolean forceNotify) {
        Log.d(TAG, "notifySearchModeLock: isSearchSwitch = " + isSearchSwitch);
        if (isSearchSwitch) {
            synchronized (mSearchModeLock) {
                mSearchModeLock.notifyAll();
                Log.d(TAG, "notifySearchModeLock: notifyAll");
            }
        }
    }


    private void waitMultiSearchMode() {
        Log.d(TAG, "waitMultiSearchMode: isMultiSearchMode = " + isMultiSearchMode);
        if (isMultiSearchMode) {
            synchronized (mMultiSearchModeLock) {
                try {
                    mMultiSearchModeLock.wait();
                    Log.d(TAG, "waitMultiSearchMode: wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setIsMultiSearchInterrupt(boolean interrupt){
        Log.d(TAG,"setIsMultiSearchInterrupt:"+interrupt);
        isMultiSearchInterrupt = interrupt;
    }

    public void notifyMultiSearchModeLock() {
        Log.d(TAG, "notifyMultiSearchModeLock: isMultiSearchMode = " + isMultiSearchMode);
        if (isMultiSearchMode) {
            synchronized (mMultiSearchModeLock) {
                mMultiSearchModeLock.notifyAll();
                Log.d(TAG, "notifyMultiSearchModeLock: notifyAll");
            }
        }
    }



    /**
     * 给inform类，判断当前是否是播放状态 长城的状态是反的，所以这里先用反的
     *
     * @return true：播放 false：暂停
     */
    @Override
    public boolean isPlaying() {
        boolean isPlaying = false;
        if (mRadioTuner != null) {
            if (currentRadioType == RadioMessage.FM_AM_TYPE) {
                isPlaying = !mRadioTuner.getMute();
//                isPlaying = !getMute();
            } else {
//                isPlaying = DABControlAction.getInstance().isPlaying();
                isPlaying = !mRadioTuner.getMute();
            }
        }
        Log.d(TAG, "isPlaying: isPlaying = " + isPlaying);
        return isPlaying;
    }

    /**
     * get 当前Mute 状态
     * 使用get 方法获取mute 状态，会不及时，导致状态判断出错；默认使用RadioTuner直接获取mute状态
     * 2022-11-04
     * 设置完成播放状态后，立刻去获取，获取的内容还是上次的。
     * @return true mute; false UnMute
     */
    private boolean getMute() {
        if (null == mRadioTuner) {
            Log.d(TAG, "getMute: is null");
            return false;
        }
        boolean mute;
        // 使用get 方法获取mute 状态，会不及时，导致状态判断出错；默认使用RadioTuner直接获取mute状态
        RadioManager.RadioEventValue radioEventValue = mRadioTuner.get(RadioEventValueUtils.generateEventValue(RadioManager.EVENT_MUTE_STATE));
        Log.d(TAG, "getMute: radioEventValue = " + radioEventValue);
        if (null == radioEventValue) {
            return false;
        }
        RadioManager.RawValue rawValue = radioEventValue.getRawValue();
        Log.d(TAG, "getMute: rawValue = " + rawValue);
        if (null == rawValue) {
            return false;
        }
        ArrayList<Integer> int32Values = rawValue.getInt32Values();
        Log.d(TAG, "getMute: int32Values = " + int32Values);
        if (null == int32Values || int32Values.isEmpty()) {
            return false;
        }
        Integer value = int32Values.get(0); // )0 /1；（1：启动mute  0 ：取消mute）
        mute = (null != value && value == 1);
        Log.d(TAG, "getMute:  mute = " + mute);
        return mute;
    }

    @Override
    public void setDABAnnStatus(DABAnnSwitch dabAnnSwitch) {
        Log.d(TAG, "setDABAnnStatus: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return;
        }
        DABControlAction.getInstance().setDABAnnStatus(dabAnnSwitch);
    }

    @Override
    public void setRDSSettingsStatus(RDSSettingsSwitch rdsSettingsStatus) {
        Log.d(TAG, "setRDSSettingsStatus: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return;
        }
        //RDS状态
        String settings = JsonUtils.generateJson(rdsSettingsStatus);
        if (rdsSettingsStatus.getRds() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_OP_SET_RDS_ON_OFF, settings));
        }
        if (rdsSettingsStatus.getAf() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_OP_SET_AF_ON_OFF, settings));
        }
        if (rdsSettingsStatus.getEon() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_OP_SET_EON_ON_OFF, settings));
        }
        if (rdsSettingsStatus.getPty() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_OP_SET_PTY_ON_OFF, settings));
        }
        if (rdsSettingsStatus.getReg() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_OP_SET_REG_ON_OFF, settings));
        }
        if (rdsSettingsStatus.getTa() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_OP_SET_TA_ON_OFF, settings));
        }

        mCurrentRadioInfo.onRDSSettingsStatusChange(rdsSettingsStatus);
    }

    @Override
    public DABAnnSwitch getDABAnnSwitchStatus() {
        Log.d(TAG, "getDABAnnSwitchStatus: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        return DABControlAction.getInstance().getDABAnnSwitchStatus();
    }

    @Override
    public RDSSettingsSwitch getRDSSettingsSwitchStatus() {
        Log.d(TAG, "getRDSSettingsSwitchStatus: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        RadioManager.RadioEventValue radioEventValue = mRadioTuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_RDS_SETTING_OPTION));
        String radioJson = getRadioJson(radioEventValue);
        Log.d(TAG, "getRDSSettingsSwitchStatus: radioJson = " + radioJson);
        return JsonUtils.generateObject(radioJson, RDSSettingsSwitch.class);
    }

    @Override
    public List<DABHeadlineMessage> getDABHeadlineMessage() {
        Log.d(TAG, "getDABHeadlineMessage: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        return DABControlAction.getInstance().getDABHeadlineMessage();
    }

    @Override
    public DABTime getDABTime() {
        Log.d(TAG, "getDABTime: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        return DABControlAction.getInstance().getDABTime();
    }

    @Override
    public DABBerMessage getDABBerMessage() {
        Log.d(TAG, "getDABBerMessage: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        return DABControlAction.getInstance().getDABBerMessage();
    }

    @Override
    public DABEPGScheduleList getEPGList() {
        Log.d(TAG, "getEPGList: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        return DABControlAction.getInstance().getEPGList();
    }

    /**
     * 获取搜索到的list
     *
     * @param band 获取的频段
     * @return radioMessageList
     */
    @Override
    public List<RadioMessage> getAstList(int band) {
        ArrayList<Integer> list = null;
        if (mRadioTuner != null) {
            if (currentRadioType == RadioMessage.FM_AM_TYPE) {
                //EVENT_AST_INFO 获取数据
                //获取相应的band PS:这里有问题，不能通过get方式获取这个EVENT_AST_INFO，不过目前没有直接获取的地方，先忽略
                RadioManager.RadioEventValue radioEventValue = mRadioTuner.get(new RadioManager.RadioEventValue(System.nanoTime(),
                        RadioManager.EVENT_AST_INFO, new RadioManager.RawValue(null, null, null, null, null)));
                list = radioEventValue.getRawValue().getInt32Values();
            } else {
                return DABControlAction.getInstance().getAstList(0);
            }
        }
        return getAstList(band, list);
    }

    @Override
    public int getRssiValue() {
        Log.d(TAG, "getRssiValue: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return 0;
        }
        return DABControlAction.getInstance().getRssiValue();
    }

    /**
     * @param band band
     * @param list list
     * @return List<RadioMessage>
     */
    private List<RadioMessage> getAstList(int band, List<Integer> list) {
        List<RadioMessage> radioMessageList = new ArrayList<>();
        Log.d(TAG, "getAstList: astList = " + list);
        // 由于SH平台这个接口会返回null，所以这里需要加入判空逻辑
        if (list == null) {
            return radioMessageList;
        }
        for (int value : list) {
            if (value != 0) {
                radioMessageList.add(new RadioMessage(band, value));
            }
        }
        Log.d(TAG, "getAstList: radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 启动一个软引用的子线程
     */
    private static class MyHandler extends Handler {
        WeakReference<RadioControlAction> weakReference;

        MyHandler(RadioControlAction radioControlAction, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(radioControlAction);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final RadioControlAction radioControlAction = weakReference.get();
            if (msg.what == RE_INIT_RADIO_CONTROL) {
                radioControlAction.initRadio();
            }else if (msg.what == MSG_QUIT_STEP_MODE){
                Log.d(TAG, "handleMessage: MSG_QUIT_STEP_MODE = " + MSG_QUIT_STEP_MODE);
                //退出步进模式
                radioControlAction.setStepMode(false);
            }
        }
    }

    private boolean fromSTR = false;
    //是否需要强制切通道
    private boolean forceSwitchSource = false;

    public void resetInit(){
        isInit = false;
        initSuccess = false;
        initFirst = true;
        fromSTR = true;
        //通知底层进入STR
        mRadioTuner.set(RadioEventValueUtils.generateEventValue(0x81C));
    }

    public boolean isfromSTR() {
        Log.d(TAG,"isfromSTR: "+fromSTR);
        return fromSTR;
    }

    public void setfromSTR(boolean test) {
        this.fromSTR = test;
        forceSwitchSource = true;//休眠唤醒的情况下，需要强制切一次通道
    }

    /**
     * 初始化
     */
    @SuppressLint("WrongConstant")
    public synchronized void initialize() {
        Log.d(TAG, "initialize: isInit = " + isInit);
        //如果已经初始化了，就不用再次初始化，避免重复初始化，导致了底层芯片的异常
        if (isInit) {
            return;
        }
        isInit = true;
        initFirst = true;
        HandlerThread mHandlerThread = new HandlerThread("HandlerThread-For-RadioAction");
        mHandlerThread.start();
        mHandler = new MyHandler(this, mHandlerThread.getLooper());
        mHandler.sendEmptyMessage(RE_INIT_RADIO_CONTROL);
        Log.d(TAG, "initialize: initSuccess = " + initSuccess);
    }

    /**
     * 通知底层打开收音的某个频点和频段
     *
     * @param radioMessage 需要打开的收音信息
     */
    private void tuneAndSetConfiguration(RadioMessage radioMessage, boolean isInit) {
        Log.d(TAG, "tuneAndSetConfiguration: radioMessage = " + radioMessage + " mRadioTuner = " + mRadioTuner +",isInit="+isInit);
        if (isInit){//初始化的时候需要通过fm/am打开
            switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
            mRadioTuner.setConfiguration(getRadioConfig(radioMessage.getRadioBand()));
            mRadioTuner.tune(radioMessage.getRadioFrequency(), 0);
        }else {
            currentRadioType = radioMessage.getRadioType();
            if (currentRadioType == RadioMessage.FM_AM_TYPE) {
                switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
                mRadioTuner.setConfiguration(getRadioConfig(radioMessage.getRadioBand()));
                mRadioTuner.tune(radioMessage.getRadioFrequency(), 0);
            } else {
                switchRadioSource(RadioEvent.RadioSourceType.DAB);
                DABControlAction.getInstance().openRadio(radioMessage);
            }
        }
    }

    /**
     * 初始化底层收音
     */
    @SuppressLint("WrongConstant")
    private void initRadio() {
        initRadio = false;
        initSuccess = false;
        RadioManager mRadioManager = (RadioManager) AppBase.mContext.getSystemService(BROADCAST_RADIO);
        Log.d(TAG, "initRadio: mRadioManager " + mRadioManager);
        if (mRadioManager == null) {
            reInitRadioControl();
            return;
        }
        mModules.clear();
        int status = mRadioManager.listModules(mModules);
        Log.d(TAG, "initRadio: status = " + status);
        if (status != RadioManager.STATUS_OK) {
            reInitRadioControl();
            return;
        }
        Log.d(TAG, "initRadio: mModules = " + mModules.size());
        if (mModules.size() == 0) {
            reInitRadioControl();
            return;
        }
        for (RadioManager.BandDescriptor band : mModules.get(0).getBands()) {
            Log.d(TAG, "initRadio: band = " + band);
            if (band.isFmBand()) {
                mFmConfig = new RadioManager.FmBandConfig.Builder((RadioManager.FmBandDescriptor) band).setStereo(true).build();
            }
            if (band.isAmBand()) {
                mAmConfig = new RadioManager.AmBandConfig.Builder((RadioManager.AmBandDescriptor) band).setStereo(true).build();
            }
        }

        Log.d(TAG, "initRadio: mFmConfig = " + mFmConfig + " mAmConfig = " + mAmConfig);
        if (mFmConfig == null && mAmConfig == null) {
            reInitRadioControl();
            return;
        }
        //初始化电台，用来获取当前底层记忆的电台
        RadioMessage radioMessage = mCurrentRadioInfo.getCurrentRadioMessage();
        //如果是DAB的话，需要通过之前的FM或者AM来打开
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
            radioMessage = mCurrentRadioInfo.getPreRadioMessage();
            currentRadioType = RadioMessage.DAB_TYPE;
            Log.d(TAG, "initRadio: currentRadioType is dab");
        }
        Log.d(TAG, "initRadio: radioMessage = " + radioMessage);
        mRadioTuner = mRadioManager.openTuner(mModules.get(0).getId(), getRadioConfig(radioMessage.getRadioBand()), true, new RadioCallback(), null /* handler */);
        Log.d(TAG, "initRadio: mRadioTuner = " + mRadioTuner);
        if (mRadioTuner == null) {
            reInitRadioControl();
            return;
        }
        // 等待初始化完成、等待；初始化完成后回调 onEventNotify: eventID = 7 ，
        waitRadioInit();
        //电台重新初始化了,mute值每次init都是初始值false,这里要把mute设置为ture不然会卡到显示播放中，实际没有播放的问题
        mRadioTuner.setMute(true);
        //初始化时赋值给DAB控制器
        DABControlAction.getInstance().setTuner(mRadioTuner);
        //初始化时检查是否改变了收音区域配置
        RadioConfig.getInstance().checkAndSetPreRegion();
        //初始化完成之后，就需要打开一次频率，这个是底层给的逻辑, 只有第一次电台打开后，才能判别为初始化成功，就绪中。
        tuneAndSetConfiguration(radioMessage,true);
        Log.d(TAG, "initRadio: end initSuccess = " + initSuccess);
    }

    /**
     * 由于收音有可能初始化失败，所以加入一个重新初始化的逻辑
     */
    private void reInitRadioControl() {
        Log.d(TAG, "reInitRadioControl: ");
        mHandler.removeMessages(RE_INIT_RADIO_CONTROL);
        mHandler.sendEmptyMessageDelayed(RE_INIT_RADIO_CONTROL, RE_INIT_TIME);
    }

    /**
     * 关闭
     */
    private void closeRadio() {
        Log.d(TAG, "closeRadio: ");
        if (null != mRadioTuner) {
            mRadioTuner.close();
        }
        mRadioTuner = null;
        initSuccess = false;
        DABControlAction.getInstance().setTuner(null);
    }

    /**
     * 设置电台区域类型
     * 当前没有确定具体的数值 2022-11-04
     */
    public void setRadioType(int radioTypeRegion) {
        Log.d(TAG, "setRadioType: mRadioTuner = " + mRadioTuner + " radioTypeRegion = " + radioTypeRegion + " initSuccess = " + initSuccess);
        if (mRadioTuner == null) {
            Log.e(TAG, "setRadioType: mRadioTuner is null");
            return;
        }
        /*if (currentRadioType == RadioMessage.FM_AM_TYPE)*/ {
            Log.d(TAG, "setRadioType: ");
            ArrayList<Integer> int32Values = new ArrayList<>();
            int32Values.add(radioTypeRegion);// 后面补充亚洲/欧洲等区域码
            //EVENT_REGION_TYPE
            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_REGION_TYPE,
                    new RadioManager.RawValue(int32Values, null, null, null, null)));
        }
        Log.d(TAG, "setRadioType: end");
    }

    /**
     * 主动获取 FM/AM 电台下的信息
     * [0]频点；[1]信号强度；[2]信噪比；[3]多路径干扰；[4]频偏；[5]带宽；[6]调制度
     */
    @Override
    public ArrayList<Integer> getProgramInfo() {
        Log.d(TAG, "getProgramInfo: mRadioTuner = " + mRadioTuner + " initSuccess = " + initSuccess);
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "getProgramInfo: mRadioTuner is null");
            return null;
        }
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
            Log.d(TAG, "getProgramInfo: ");
            RadioManager.RadioEventValue radioEventValue = mRadioTuner.get(new RadioManager.RadioEventValue(System.nanoTime(),
                    RadioEvent.EventID.EVE_PROGRAM_INFO, new RadioManager.RawValue(null, null, null, null, null)));
            Log.d(TAG, "getProgramInfo: radioEventValue = " + radioEventValue);
            if (null != radioEventValue) {
                RadioManager.RawValue rawValue = radioEventValue.getRawValue();
                Log.d(TAG, "getProgramInfo: rawValue = " + rawValue);
                if (null != rawValue) {
                    ArrayList<Integer> int32Values = rawValue.getInt32Values();
                    Log.d(TAG, "getProgramInfo: int32Values = " + int32Values);
                    return int32Values;
                }
            }
        }
        Log.d(TAG, "getProgramInfo: end");
        return null;
    }


    /**
     * 当前是否是步进模式
     */
    private boolean isStepMode = false;

    /**
     * 当前是否是快速往前步进的模式
     */
    private boolean isFastStepNext = false;

    /**
     * 当前是否是快速往后步进的模式
     */
    private boolean isFastStepPre = false;

    /**
     * 快速步进的runnable
     */
    private final FastStepRunnable fastStepRunnable = new FastStepRunnable();

    /**
     * 长按的话，每200ms步进一次
     */
    private static final int FAST_STEP_TIME = 100;

    /**
     * 退出步进模式的消息
     */
    private static final int MSG_QUIT_STEP_MODE = 2;

    /**
     * 退出步进模式的时间，目前确认是5s
     */
    private static final int QUIT_STEP_MODE_TIME = 5000;

    @Override
    public void startStepMode() {
        Log.d(TAG, "startStepMode: ");
        setStepMode(true);
    }

    @Override
    public void stopStepMode() {
        Log.d(TAG, "stopStepMode: ");
        setStepMode(false);
    }

    @Override
    public void startFastPreStep() {
        Log.d(TAG, "startFastPreStep: ");
        isFastStepPre = true;
        //开始快退的时候，要先静音
        RadioControlTool.getInstance().processCommand(RadioAction.PAUSE, ChangeReasonData.START_FAST_STEP);
        mHandler.removeMessages(0);
        mHandler.post(fastStepRunnable);
    }

    @Override
    public void stopFastPreStep() {
        //停止快退的时候，要将原来的静音取消掉
        RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.STOP_FAST_STEP);
        Log.d(TAG, "stopFastPreStep: ");
        isFastStepPre = false;
    }

    @Override
    public void startFastNextStep() {
        Log.d(TAG, "startFastNextStep: ");
        isFastStepNext = true;
        //开始快进的时候，要先静音
        RadioControlTool.getInstance().processCommand(RadioAction.PAUSE, ChangeReasonData.START_FAST_STEP);
        mHandler.removeMessages(0);
        mHandler.post(fastStepRunnable);
    }

    @Override
    public void stopFastNextStep() {
        //停止快进的时候，要将原来的静音取消掉
        RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.STOP_FAST_STEP);
        Log.d(TAG, "stopFastNextStep: ");
        isFastStepNext = false;
    }

    @Override
    public void stopSeek() {
        Log.d(TAG, "stopSeek begin");
        if (mRadioTuner == null || !initSuccess || seekType == -1) {
            Log.e(TAG, "stopSeek: mRadioTuner is null");
            return;
        }
        if (currentRadioType == RadioMessage.FM_AM_TYPE) {
            if (seekType == SEEK_FORWARD){
                Log.d(TAG, "stopSeek DIRECTION_UP");
                mRadioTuner.scan(RadioTuner.DIRECTION_UP, true);
            }else {
                Log.d(TAG, "stopSeek DIRECTION_DOWN");
                mRadioTuner.scan(RadioTuner.DIRECTION_DOWN, true);
            }
        }
        Log.d(TAG, "stopSeek end");
        mCurrentRadioInfo.setSeeking(false);
        seekType = -1;
    }


    @Override
    public void multiAst() {
        waitRadioInitSuccess();
        Log.d(TAG, "multiAst: run ? ");
        //通用逻辑，如果当前搜索状态，就将搜索逻辑给去掉
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "multiAst: mRadioTuner is null or not success");
            return;
        }
        if (mCurrentRadioInfo.isSearching()) {
            RadioControlTool.getInstance().processCommand(RadioAction.STOP_AST, ChangeReasonData.CONTROL_ACTION);
            return;
        }

        //DAB/FM都要静音，pause()方法只静音一个，所以要抽取出来直接用
        mRadioTuner.setMute(true);
        ArrayList<Integer> int32MuteValues = new ArrayList<>();
        int32MuteValues.add(1); //（1：启动mute  0 ：取消mute）
        mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32MuteValues, null, null, null, null)));
        mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_MUTE_UNMUTE, JsonUtils.generateSingleNodeJson(JsonNode.MUTE_UNMUTE_NODE, RadioEvent.MuteStatus.MUTE)));
        mCurrentRadioInfo.setPlaying(!mRadioTuner.getMute());

        isMultiSearchDAB = false;
        isMultiSearchMode = true;
        mCurrentRadioInfo.setSearching(true);
        setIsMultiSearchInterrupt(false);
        //switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
        //先打开到FM，避免出现在AM的时候执行搜索，导致搜索的是AM
        mRadioTuner.setConfiguration(getRadioConfig(CurrentRadioInfo.getInstance().getFMRadioMessage().getRadioBand()));
        mRadioTuner.tune(CurrentRadioInfo.getInstance().getFMRadioMessage().getRadioFrequency(), 0);
        ArrayList<Integer> int32Values = new ArrayList<>();
        int32Values.add(1);// 0/1 （1：启动 ； 0 ：取消） 开始AST
        mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_AST_STATE_CTRL,
                new RadioManager.RawValue(int32Values, null, null, null, null)));
        waitMultiSearchMode();
        if (isMultiSearchMode && !isMultiSearchInterrupt) {//如果搜索被打断，这个标志会被重置
            //switchRadioSource(RadioEvent.RadioSourceType.DAB);
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SET_DAB_BAND));
            mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SCAN));
            isMultiSearchDAB = true;
            isMultiSearchMode = false;
            DABControlAction.getInstance().multiAst();
        }
    }

    @Override
    public void multiSeekForward() {
        next();
    }

    @Override
    public void multiSeekBackward() {
        pre();
    }

    @Override
    public void multiStepForward() {
        next();
    }

    @Override
    public void multiStepBackward() {
        pre();
    }

    @Override
    public void openTTSRadio(RadioMessage radioMessage) {
        Log.d(TAG,"openTTSRadio,radioMessage:"+radioMessage);
        stopAst();
        mRadioTuner.setMute(false);
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
            switchRadioSource(RadioEvent.RadioSourceType.DAB);
            DABControlAction.getInstance().openTTSRadio(radioMessage);
        }else {
            switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
            ArrayList<Integer> int32Values = new ArrayList<>();
            int32Values.add(0);//（1：启动mute  0 ：取消mute）
            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
            mCurrentRadioInfo.setPlaying(!mRadioTuner.getMute());
//            mRadioTuner.setConfiguration(getRadioConfig(radioMessage.getRadioBand()));
//            mRadioTuner.tune(radioMessage.getRadioFrequency(), 0);
        }
    }


    /**
     * 获取当前电台，在有效电台的位置
     *
     * @return position
     */
    private int getCurrentRadioMessagePosition() {
        int position = -1;
        RadioMessage currentMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
        Log.d(TAG, "getCurrentRadioDabMessagePosition: currentMessage = " + currentMessage);
        List<RadioMessage> dabMessages = currentMessage.getRadioBand() == RadioManager.BAND_AM ? RadioList.getInstance().getAMEffectRadioMessageList() : RadioList.getInstance().getMultiRadioMessageList();
        for (int i = 0; i < dabMessages.size(); i++) {
            RadioMessage radioDabMessageData = dabMessages.get(i);
            if (currentMessage.getRadioType() == RadioMessage.DAB_TYPE){
                if (radioDabMessageData.getRadioType() == RadioMessage.DAB_TYPE) {
                    if (currentMessage.getDabMessage().getFrequency() == radioDabMessageData.getDabMessage().getFrequency()
                            &&currentMessage.getDabMessage().getServiceId() == radioDabMessageData.getDabMessage().getServiceId()
                            && currentMessage.getDabMessage().getServiceComponentId() == radioDabMessageData.getDabMessage().getServiceComponentId()) {
                        position = i;
                        break;
                    }
                }
            }else {
                if (currentMessage.getRadioFrequency() == radioDabMessageData.getRadioFrequency()) {
                    position = i;
                    break;
                }
            }
        }
        Log.d(TAG, "getCurrentRadioDabMessagePosition: position = " + position);
        return position;
    }

    public  String getOppositeRDSName(RadioMessage radioMessage){
        for (RDSRadioName rdsRadioName : RadioList.getInstance().getRdsRadioNameList()){
            if (radioMessage.getRadioFrequency() == rdsRadioName.getFrequency()){
                Log.d(TAG,"getOppositeRDSName has one: "+radioMessage.getRadioFrequency() + " freq");
                Log.d(TAG,"getOppositeRDSName has one: "+rdsRadioName.getProgramStationName() + " NAME");
                return rdsRadioName.getProgramStationName();
            }
        }
        Log.d(TAG,"getOppositeRDSName has none");
        return null;
    }
    public  List<RadioMessage> sortWithName(List<RadioMessage> currentList){
        List<RadioMessage> tempList = new ArrayList<>();
        for (RadioMessage radioMessage : currentList) {
            if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
                String radioName = getOppositeRDSName(radioMessage);
                if (radioName == null || radioName.trim().length() < 1){
                    radioName = "FM " + (radioMessage.getRadioFrequency() / 1000.0);
                }
                radioMessage.setSortName(radioName);
            }else {
                radioMessage.setSortName(radioMessage.getDabMessage().getProgramStationName());
            }
            tempList.add(radioMessage);
        }
        // 当前目录文件夹的排序
        Collections.sort(tempList, new Comparator<RadioMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(RadioMessage o1, RadioMessage o2) {
                if (o1.getSortName() != null && o2.getSortName() != null && o1.getSortName().startsWith("FM")
                        && o2.getSortName().startsWith("FM")){
                    return o1.getRadioFrequency() > o2.getRadioFrequency() ? 1 : -1;
                }
                return (o1.getSortName() == null || o2.getSortName() == null) ? 0 : comparator.compare(o1.getSortName(), o2.getSortName());
            }
        });

        return tempList;
    }


    /**
     * 获取当前电台，在有效电台的位置
     *
     * @return position
     */
    private int getCurrentRadioMessagePositionWithList(List<RadioMessage> radioMessageList,RadioMessage currentMessage) {
        int position = -1;
        for (int i = 0; i < radioMessageList.size(); i++) {
            RadioMessage radioDabMessageData = radioMessageList.get(i);
            if (currentMessage.getRadioType() == RadioMessage.DAB_TYPE){
                if (radioDabMessageData.getRadioType() == RadioMessage.DAB_TYPE) {
                    if (currentMessage.getDabMessage().getFrequency() == radioDabMessageData.getDabMessage().getFrequency()
                            && currentMessage.getDabMessage().getServiceId() == radioDabMessageData.getDabMessage().getServiceId()
                            && currentMessage.getDabMessage().getServiceComponentId() == radioDabMessageData.getDabMessage().getServiceComponentId()) {
                        position = i;
                        break;
                    }
                }
            }else {
                if (currentMessage.getRadioFrequency() == radioDabMessageData.getRadioFrequency()) {
                    position = i;
                    break;
                }
            }
        }
        Log.d(TAG, "getCurrentRadioMessagePositionWithList: position = " + position);
        return position;
    }

    /**
     * 上一个
     */
    private void pre() {
        RadioMessage currentMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
        Log.d(TAG, "pre: currentMessage = " + currentMessage);
        List<RadioMessage> radioMessageList;
        if (currentMessage.getRadioBand() == RadioManager.BAND_AM){
            if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                radioMessageList = RadioList.getInstance().getAllCollectRadioMessageList();
            } else {
                radioMessageList = RadioList.getInstance().getAMEffectRadioMessageList();
            }
        }else {
            if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                radioMessageList = RadioList.getInstance().getAllCollectRadioMessageList();
            } else {
                radioMessageList = RadioList.getInstance().getMultiRadioMessageList();
                //先排序
                radioMessageList = sortWithName(radioMessageList);
            }
        }
        int position = getCurrentRadioMessagePositionWithList(radioMessageList,currentMessage);

        if (radioMessageList.isEmpty()) {
            Log.d(TAG, "pre: getEffectRadioMessageList isEmpty");
            return;
        }
        if (position == -1) {
            position = 0;
        } else {
            position = position - 1;
        }
        if (position < 0) {
            position = radioMessageList.size() - 1;//测试弄错了，要做循环
        }
        Log.d(TAG, "pre: position = " + position);
        RadioMessage dabMessageData = radioMessageList.get(position);
        if (null == dabMessageData) {
            Log.d(TAG, "pre: ");
            return;
        }
        Log.d(TAG, "pre: dabMessageData = " + dabMessageData);
        RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.PRE,dabMessageData);
    }


    /**
     * 下一个
     */
    private void next() {
        RadioMessage currentMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
        Log.d(TAG, "next: currentMessage = " + currentMessage);
        List<RadioMessage> radioMessageList;
        if (currentMessage.getRadioBand() == RadioManager.BAND_AM){
            if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                radioMessageList = RadioList.getInstance().getAllCollectRadioMessageList();
            } else {
                radioMessageList = RadioList.getInstance().getAMEffectRadioMessageList();
            }
        }else {
            if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                radioMessageList = RadioList.getInstance().getAllCollectRadioMessageList();
            } else {
                radioMessageList = RadioList.getInstance().getMultiRadioMessageList();
                //先排序
                radioMessageList = sortWithName(radioMessageList);
            }
        }
        int position = getCurrentRadioMessagePositionWithList(radioMessageList,currentMessage);
         if (radioMessageList.isEmpty()) {
            Log.d(TAG, "next: getEffectRadioMessageList isEmpty");
            return;
        }
        if (position == -1) {
            position = 0;
        } else {
            position = position + 1;
        }
        if (position > radioMessageList.size() - 1) {
            position = 0;//测试弄错了，要做循环
        }
        Log.d(TAG, "next: position = " + position);
        RadioMessage dabMessageData = radioMessageList.get(position);
        if (null == dabMessageData) {
            Log.d(TAG, "next: return dabMessageData is null ");
            return;
        }
        Log.d(TAG, "next: dabMessageData = " + dabMessageData);
        RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.NEXT,dabMessageData);
    }
    /**
     * 设置步进模式
     *
     * @param isStepMode true：步进模式 false：不是步进模式
     */
    private void setStepMode(boolean isStepMode) {
        Log.d(TAG, "setStepMode: isStepMode = " + isStepMode);
        this.isStepMode = isStepMode;
        if (isStepMode) {
            resetQuitStepModeTime();
        }
    }

    /**
     * 重置退出步进模式的时间
     * 在进入步进模式，以及在步进模式的时候，跳转上下电台的时候，会触发
     */
    private void resetQuitStepModeTime() {
        Log.d(TAG, "resetQuitStepModeTime: isStepMode = " + isStepMode);
        if (isStepMode) {
            mHandler.removeMessages(MSG_QUIT_STEP_MODE);
            mHandler.sendEmptyMessageDelayed(MSG_QUIT_STEP_MODE, QUIT_STEP_MODE_TIME);
        }
    }


    /**
     * 一个用来实现快速布局的runnable
     */
    private class FastStepRunnable implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "FastStepRunnable run: isFastStepNext = " + isFastStepNext + " isFastStepPre = " + isFastStepPre);
            if (!isFastStepNext && !isFastStepPre) {
                return;
            }
            //如果当前是在快进暂停
            if (isFastStepNext) {
                RadioControlTool.getInstance().processCommand(RadioAction.STEP_FORWARD, ChangeReasonData.AUTO_STEP);
            } else if (isFastStepPre) {
                RadioControlTool.getInstance().processCommand(RadioAction.STEP_BACKWARD, ChangeReasonData.AUTO_STEP);
            }
            mHandler.removeMessages(0);
            mHandler.postDelayed(fastStepRunnable, FAST_STEP_TIME);

        }
    }





    /**
     * 收音底层的回调
     */
    private class RadioCallback extends RadioTuner.Callback {

        /**
         * 底层出现了异常
         *
         * @param status 未知
         */
        @Override
        public void onError(int status) {
            Log.d(TAG, "onError: status = " + status);
        }

        /**
         * 收音的频段发送了改变
         *
         * @param config 当前的频段
         */
        @Override
        public void onConfigurationChanged(RadioManager.BandConfig config) {
            // 当前，频段切换，已弃用，主要在onProgramInfoChanged 进行频段和频点的切换设置
//            Log.d(TAG, "onConfigurationChanged: config = " + config);
//            int band = config.getType();
            //当前的频段发送了改变，需要更新当前的频率和显示频率
            //这里不能设置当前播放的频率，等到后面频率变化再一起设置，不然会出现闪动的问题
//            setCurrentRadioBand(band);

            //频率切换的时候，这个时候搜索状态时停止的，也要清除
//            mCurrentRadioInfo.setSearching(false);
//            mCurrentRadioInfo.setSeeking(false);
        }

        /**
         * 收音的频率发送了改变
         *
         * @param info 改变的频率
         */
        @Override
        public void onProgramInfoChanged(RadioManager.ProgramInfo info) {
            Log.d(TAG, "onProgramInfoChanged: info = " + info);
            if (info != null) {
                int frequency = (int) info.getSelector().getPrimaryId().getValue();
                Log.d(TAG, "onProgramInfoChanged: isTuned = " + info.isTuned() + " frequency = " + frequency);
                //底层的枚举定义了1和2，所以上层使用的时候需要减1
                int band = getRadioBandFromFrequency(frequency);
                Log.d(TAG, "onProgramInfoChanged: band = " + band + " currentRadioType = " + currentRadioType);
                //底层时序问题，会导致偶现切到FM/AM时，此处返回上一个AM/FM的情况
                //例如AM在搜索，切到FM，此时需要通知底层停止搜索，但是底层停止搜索是个耗时异步操作，
                //如果此时快速在FM/AM之间切换，就会导致此处返回了FM，界面切FM时判断是相同内容，执行的play操作，从而导致显示和播放的不一致问题
                //此处应该判断是TAB之间快速切换时才做这个处理
                if (currentRadioType != RadioMessage.DAB_TYPE && band != mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() && ChangeReasonData.UI_START == RadioControlTool.getInstance().getOpenReason() && !isSearchMode){
                    Log.d(TAG,"onProgramInfoChanged band not same as current,return");
                    return;
                }

                //设置Tuned的状态
                mCurrentRadioInfo.setTuned(info.isTuned());
                //频率发送了改变，设置频率
                if (currentRadioType == RadioMessage.DAB_TYPE) {
                    switch (band) {
                        case RadioManager.BAND_AM:
                        case RadioManager.BAND_AM_HD:
                            mCurrentRadioInfo.getAMRadioMessage().setRadioFrequency(frequency);
                            break;
                        case RadioManager.BAND_FM:
                        case RadioManager.BAND_FM_HD:
                            mCurrentRadioInfo.getFMRadioMessage().setRadioFrequency(frequency);
                            break;
                    }
                    if (isSearchMode) {
                        Log.d(TAG, "onProgramInfoChanged: currentRadioType is DAB.");
                        return;
                    }
                }
                //频率发送了改变，设置频率
                RadioMessage radioMessage = new RadioMessage(band, frequency);
                radioMessage.setTP(info.isTrafficProgram());
                radioMessage.setST(info.isStereo());
                //如果已经搜索完成，那就要调用一下播放，将当前显示的频率同步给当前播放的频率
                if (info.isTuned()) {
                    if (initFirst) {
                        // 初始化完成后，需要tune打开一次电台，根据这次反馈，在进行通知初始化完成
                        if (mCurrentRadioInfo.getCurrentRadioMessage().getRadioType() != RadioMessage.DAB_TYPE) {
                            mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
                        }
                        initFirst = false;
                        initSuccess = true;
                        notifyLock();
                        Log.w(TAG, "onProgramInfoChanged: first , no play ");
                        return;
                    } else if (isSearchMode) {
                        // 指定频段搜索
                        if (!isSearchSwitch) {
                            // 当前处于自动搜索-切换频段中
                            if (currentRadioType != RadioMessage.DAB_TYPE) {
                                mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
                            }
                            isSearchSwitch = true;
                            notifySearchModeLock(false);
                            Log.w(TAG, "onProgramInfoChanged: stop auto search switch");
                            return;
                        } else {
                            // 当前，自动搜素中，并且不处于切换频段，则关闭搜索模式
                            isSearchMode = false;
                            Log.w(TAG, "onProgramInfoChanged: stop auto Search Mode");
                        }
                    }
                    //这里需要加入消抖，不然底层会出现频率设置抖动，500ms这个可能需要动态调整
                    //底层打开之后，需要播放一次
                    if (!isMultiSearchMode) {//组合搜索不需要播放
                        mCurrentRadioInfo.setSearching(false);
                        mCurrentRadioInfo.setSeeking(false);
                        if (ChangeReasonData.RADIO_TTS == RadioControlTool.getInstance().getOpenReason()){
                            RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.RADIO_TTS);
                        }else if (ChangeReasonData.TTS_RESUME_PAUSE != RadioControlTool.getInstance().getOpenReason() && ChangeReasonData.AIDL != RadioControlTool.getInstance().getAstReason() && ChangeReasonData.BOOT_RESUME_NOT_PLAY != RadioControlTool.getInstance().getOpenReason()){
                        	RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.AST_CHANGE_PLAY);
                    	}
                    }
                }
                if (currentRadioType != RadioMessage.DAB_TYPE && ChangeReasonData.RADIO_TTS != RadioControlTool.getInstance().getOpenReason()) {
                    mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
                }
            }
        }

        /**
         * @deprecated
         */
        @Override
        @Deprecated
        public void onMetadataChanged(RadioMetadata metadata) {
//            Log.d(TAG, "onMetadataChanged: metadata = " + metadata);
        }

        /**
         * 有人抢了收音的控制权限
         *
         * @param control true 还是自己的，false 被抢了
         */
        @Override
        public void onControlChanged(boolean control) {
            Log.d(TAG, "onControlChanged: control = " + control);
            if (!control) {
                Log.d(TAG, "onControlChanged: mRadioTuner = null");
                closeRadio();
            }
        }

        @Override
        public void onTuneFailed(int result, ProgramSelector selector) {
            super.onTuneFailed(result, selector);
            Log.d(TAG, "onTuneFailed: result = " + result + " selector = " + selector);
        }

        @Override
        public void onEventNotify(RadioManager.RadioEventValue radioEventValue) {
            super.onEventNotify(radioEventValue);
            int eventID = radioEventValue.getEventID();
            Log.d(TAG, "onEventNotify: eventID = " + eventID);
            if ((7 != eventID && 2075 != eventID) && !initSuccess){//进入STR之后，底层需要过几秒才能真正断掉，在这个时间内仍旧会收到底层上报的消息，会导致进入STR了，上层还是会申请焦点，导致杂音临时方案失效
                Log.d(TAG, "onEventNotify: initSuccess false");
                return;
            }
            if (RadioManager.EVENT_REGION_TYPE == eventID) {
                // 电台区域 类型变化
                RadioList.getInstance().clearAllList();
                return;
            } else if (RadioManager.EVENT_AST_INFO == eventID) {
                RadioManager.RawValue rawValue = radioEventValue.getRawValue();
                ArrayList<Byte> byteValues = rawValue.getByteValues();
                int bandId = byteValues.get(0);
                ArrayList<Integer> list = rawValue.getInt32Values();
                Log.d(TAG, "onEventNotify: EVENT_AST_INFO bandId = " + bandId + " list = " + list);
                RadioList.getInstance().updateEffectRadioMessageList(bandId, getAstList(bandId, list));
                mCurrentRadioInfo.onAstListChanged(bandId);
                if (isMultiSearchMode){
                    Log.w(TAG, "onProgramInfoChanged: isMultiSearchMode");
                    notifyMultiSearchModeLock();
                }
                return;
            }/* else if (6 == eventID) {
                //  该eventid 已在主动获取ProgramInfo
                return;
            }*/ else if (7 == eventID) {
                // 初始化成功 RadioManager 未定义
                // 其他方法的执行需要等解锁;这个就是初始化完成的回调了
                if (!hasDAB) {//如果有DAB的话，等待另一条初始化完成的回调，因为DAB的初始化会比较慢
                    initRadio = true;
                    notifyInitLock();
                    mCurrentRadioInfo.onInitSuccessCallback();
                    Log.d(TAG, "onEventNotify: initRadio: initSuccess = " + initSuccess);
                    return;
                }
            }else if (2075 == eventID) {
                // 初始化成功 RadioManager 未定义
                // 其他方法的执行需要等解锁;这个就是初始化完成的回调了
                initRadio = true;
                notifyInitLock();
                mCurrentRadioInfo.onInitSuccessCallback();
                Log.d(TAG, "onEventNotify: initDAB: initSuccess = "+initSuccess);
                return;
            } else if (RadioEvent.EventID.EVT_RDS_STATUS_FLAG_INFO == eventID) {
                String radioJson = getRadioJson(radioEventValue);
                Log.d(TAG, "onEventNotify: EVT_RDS_STATUS_FLAG_INFO radioJson = " + radioJson);
                RDSFlagInfo rdsFlagInfo = JsonUtils.generateObject(radioJson, RDSFlagInfo.class);
                mCurrentRadioInfo.onRDSFlagInfoChange(rdsFlagInfo);
                return;
            } else if (RadioEvent.EventID.EVT_RDS_CURRENT_STATION_INFO == eventID) {
                Log.d(TAG, "onEventNotify: update RDS STATION INFO");
                String radioJson = getRadioJson(radioEventValue);
                RDSRadioTextTemp rdsRadioTextTemp = JsonUtils.generateObject(radioJson, RDSRadioTextTemp.class);

                if (mCurrentRadioInfo.getCurrentRadioMessage().getRadioBand() != RadioManager.BAND_FM
                    || mCurrentRadioInfo.getCurrentRadioMessage().getRadioFrequency() != rdsRadioTextTemp.getFrequency()){
                    Log.d(TAG, "onEventNotify update RDS,current band not FM or Fre not same, return!");
                    return;
                }

                RDSRadioText rdsRadioText = new RDSRadioText();
                rdsRadioText.setRadioText(Html.fromHtml(ASCIITools.hex2Html(rdsRadioTextTemp.getRadioText())).toString());
                rdsRadioText.setProgramStationName(Html.fromHtml(ASCIITools.hex2Html(rdsRadioTextTemp.getProgramStationName())).toString());
                rdsRadioText.setProgramType(rdsRadioTextTemp.getProgramType());
                RadioMessage radioMessage = new RadioMessage(RadioManager.BAND_FM, rdsRadioTextTemp.getFrequency());
                mCurrentRadioInfo.getCurrentRadioMessage().setRdsRadioText(rdsRadioText);
                radioMessage.cloneRadioMessage(mCurrentRadioInfo.getCurrentRadioMessage());
                mCurrentRadioInfo.setCurrentRadioMessage(radioMessage);
                return;
            }else if (RadioEvent.EventID.EVT_RDS_ANNOUNCEMENT_NOTIFY == eventID){
                String radioJson = getRadioJson(radioEventValue);
                Log.d(TAG, "onEventNotify: update RDS_ANNOUNCEMENT,radioJson："+radioJson);
                RDSAnnouncement rdsAnnouncement = JsonUtils.generateObject(radioJson, RDSAnnouncement.class);
                mCurrentRadioInfo.onRDSAnnChange(rdsAnnouncement);
                return;
            } else if (RadioEvent.EventID.EVT_RDS_PSN_LIST == eventID){
                String radioJson = getRadioJson(radioEventValue);
                Log.d(TAG, "onEventNotify: EVT_RDS_PSN_LIST,radioJson："+radioJson);
                if (!TextUtils.isEmpty(radioJson)) {
                    List<RDSRadioName> rdsRadioNameList = JsonUtils.generateObject(radioJson, RDSRadioNameList.class).getFmStationList();
                    List<RDSRadioName> rdsRadioNameListFinal = new ArrayList<>();
                    if (rdsRadioNameList != null){
                        for (RDSRadioName rdsRadioName : rdsRadioNameList){
                            String psn = Html.fromHtml(ASCIITools.hex2Html(rdsRadioName.getPsn())).toString();
                            rdsRadioName.setProgramStationName(psn);
                            Log.d(TAG,"psn:"+ psn);
                            rdsRadioNameListFinal.add(rdsRadioName);
                        }
                        RadioList.getInstance().updateRDSNameList(rdsRadioNameListFinal);
                    }
                }
                return;
            }
            //释放锁的操作需要放到前面，避免卡到切Band的时序，导致锁不会释放
            if (RadioEvent.EventID.EVT_DAB_PLAY_STATUS == eventID){
                if (isSearchMode) {
                    // 指定频段搜索
                    if (!isSearchSwitch) {
                        // 当前处于自动搜索-切换频段中
                        isSearchSwitch = true;
                        notifySearchModeLock(false);
                        Log.w(TAG, "onEventNotify: stop auto search switch");
                    } else {
                        // 当前，自动搜素中，并且不处于切换频段，则关闭搜索模式
                        isSearchMode = false;
                        Log.w(TAG, "onEventNotify: stop auto Search Mode");
                    }
                }
            }else if (RadioEvent.EventID.EVT_DAB_SEEK_SCACN_STATUS == eventID){
                if (CurrentRadioInfo.getInstance().isSearching()){
                    if (isSearchMode) {
                        // 指定频段搜索
                        if (!isSearchSwitch) {
                            // 当前处于自动搜索-切换频段中
                            isSearchSwitch = true;
                            notifySearchModeLock(false);
                            Log.w(TAG, "onEventNotify EVT_DAB_SEEK_SCACN_STATUS: stop auto search switch");
                        } else {
                            // 当前，自动搜素中，并且不处于切换频段，则关闭搜索模式
                            isSearchMode = false;
                            Log.w(TAG, "onEventNotify EVT_DAB_SEEK_SCACN_STATUS: stop auto Search Mode");
                        }
                    }
                }
            }
                //DAB处理
            if (!hasDAB){
                Log.d(TAG,"has no dab carconfig,but tuner callback dab info");
                return;
            }
            if (mCurrentRadioInfo.getCurrentRadioMessage().getRadioType() != RadioMessage.DAB_TYPE){
                if (RadioEvent.EventID.EVT_DAB_DATA_TIME_INFO == eventID || RadioEvent.EventID.EVT_DAB_ANNOUNCEMENT_NOTIFY == eventID || hasMulti){
                    Log.d(TAG, "onEventNotify to DAB on not dab,but event_dab_time or event_dab_ann");
                }else {
                    Log.d(TAG, "onEventNotify to DAB,but current is not dab");
                    return;
                }
            }
            DABControlAction.getInstance().onEventNotify(radioEventValue);
        }
    }

    /**
     * 初始化时使用，获取radio config
     *
     * @param radioBand 需要获取的频段
     * @return mFmConfig|mAmConfig
     */
    private RadioManager.BandConfig getRadioConfig(int radioBand) {
        Log.d(TAG, "getRadioConfig: radioBand = " + radioBand);
        switch (radioBand) {
            case RadioManager.BAND_AM:
            case RadioManager.BAND_AM_HD:
                return mAmConfig;
            case RadioManager.BAND_FM:
            case RadioManager.BAND_FM_HD:
                return mFmConfig;
            default:
                return null;
        }
    }

    /**
     * 用于电台初始化阻塞
     */
    private void waitRadioInit() {
        Log.d(TAG, "waitRadioInit: 0 init = " + initRadio);
        if (!initRadio) {
            synchronized (mInitLock) {
                try {
                    mInitLock.wait();
                    Log.d(TAG, "waitRadioInit: 0 wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notifyInitLock() {
        Log.d(TAG, "notifyInitLock: 0 init = " + initRadio);
        if (initRadio) {
            synchronized (mInitLock) {
                mInitLock.notifyAll();
                Log.d(TAG, "notifyInitLock: 0 notifyAll");
            }
        }
    }

    /**
     * 用于电台操作，阻塞
     */
    public void waitRadioInitSuccess() {
        Log.d(TAG, "waitRadioInitSuccess: 1 initSuccess = " + initSuccess);
        if (!initSuccess) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                    Log.d(TAG, "waitRadioInitSuccess: 1 wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notifyLock() {
        Log.d(TAG, "notifyLock: 1 initSuccess = " + initSuccess);
        if (initSuccess) {
            synchronized (mLock) {
                mLock.notifyAll();
                Log.d(TAG, "notifyLock: 1 notifyAll");
            }
        }
    }

    /**
     * 当出现异常情况导底层没有返回onProgramInfoChanged时，进而导致应用一直在等锁时，考虑在用户主动操作的时候，主动进行解锁操作，避免应用一直无法使用的情况出现-补丁
     */
    public void forceNotifyLock() {
        Log.d(TAG, "forceNotifyLock: isInit = " + isInit + " initFirst = " + initFirst + " initSuccess = " + initSuccess + " initRadio = " + initRadio);
        if (isInit && initFirst && !initSuccess && initRadio){
            initFirst = false;
            initSuccess = true;
            mLock.notifyAll();
            Log.w(TAG, "forceNotifyLock: mLock.notifyAll");
        }
    }


    /**
     * 从底层获取当前的收音频段，有问题，所以弃用，底层没办法修复的获取后导致底层崩溃的问题，所以没办法
     *
     * @return band 收音频段
     */
    private int getCurrentBand() {
        int band = ERROR_BAND;
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "getCurrentBand: mRadioTuner is null");
            return band;
        }
        RadioManager.BandConfig[] bandConfigs = new RadioManager.BandConfig[1];
        int status = mRadioTuner.getConfiguration(bandConfigs);
        if (status == RadioManager.STATUS_OK && bandConfigs[0] != null) {
            band = bandConfigs[0].getType();
        }
        Log.d(TAG, "getCurrentBand: band = " + band);
        return band;
    }

    /**
     * 从底层获取当前的频率，有问题，所以弃用，底层没办法修复的获取后导致底层崩溃的问题，所以没办法
     *
     * @return frequency：当前的收音频率
     */
    private int getCurrentFrequency() {
        int frequency = ERROR_FREQUENCY;
        if (mRadioTuner == null || !initSuccess) {
            Log.e(TAG, "getCurrentFrequency: mRadioTuner is null");
            return frequency;
        }
        RadioManager.ProgramInfo[] programInfo = new RadioManager.ProgramInfo[1];
        int status = mRadioTuner.getProgramInformation(programInfo);
        if (status == RadioManager.STATUS_OK && programInfo[0] != null) {
            frequency = programInfo[0].getChannel();
        }
        Log.d(TAG, "getCurrentFrequency: frequency = " + frequency);
//        setFrequency(frequency);
        return frequency;
    }

    /**
     * 根据频率获取当前的频段
     *
     * @return AM或者FM的频段
     */
    private int getRadioBandFromFrequency(int frequency) {
        return RadioConversionUtils.frequencyGetBand(frequency);
    }

    /**
     * 切换电台音源，DAB和FM/AM需重新切换
     */
    private void switchRadioSource(int source) {
        // 设置radio音源通道
        Map<String, Integer> map = new HashMap<>();
        map.put(JsonNode.RADIO_SOURCE_NODE, source);
        String json = JsonUtils.generateJson(map);
        Log.d(TAG, "switchRadioSource currentRadioType = " + currentRadioType + " json = " + json);
        mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_SOURCE_SET_SWITCH, json));
    }

    /**
     * 通过RadioEventValue提取Json数据方法块
     *
     * @param radioEventValue RadioManager.RadioEventValue
     * @return String
     */
    private String getRadioJson(RadioManager.RadioEventValue radioEventValue) {
        if (radioEventValue == null) {
            Log.d(TAG, "getRadioJson: radioEventValue is null !");
            return null;
        }
        RadioManager.RawValue rawValue = radioEventValue.getRawValue();
        if (rawValue == null) {
            Log.d(TAG, "getRadioJson: rawValue is null !");
            return null;
        }
        return rawValue.getStringValue();
    }


    @Override
    public RadioMessage getCurrentRadioMessageWithEPG() {
        DABMessage dabMessage = DABControlAction.getInstance().getCurrentDAB();
        Log.d(TAG,"getCurrentRadioMessageWithEPG: "+dabMessage);
        if (dabMessage == null){
            return null;
        }
        RadioMessage dabRadioMessage = CurrentRadioInfo.getInstance().getDABRadioMessage();
        if (!(dabMessage.getFrequency() == dabRadioMessage.getDabMessage().getFrequency()
                && dabMessage.getServiceId() == dabRadioMessage.getDabMessage().getServiceId()
                && dabMessage.getServiceComponentId() == dabRadioMessage.getDabMessage().getServiceComponentId())){
            CurrentRadioInfo.getInstance().clearLogo();
        }
        dabRadioMessage.setDabMessage(dabMessage);
        CurrentRadioInfo.getInstance().setCurrentRadioMessage(dabRadioMessage);
        return dabRadioMessage;
    }

    @Override
    public RadioParameter getRadioParameter() {
        Log.d(TAG, "getRadioParameter: mRadioTuner = " + mRadioTuner + "  initSuccess = " + initSuccess);
        if (null == mRadioTuner || !initSuccess) {
            return null;
        }
        RadioManager.RadioEventValue radioEventValue = mRadioTuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_FM_OP_SIGNAL_QUALITY_CONDITION));
        String radioJson = getRadioJson(radioEventValue);
        Log.d(TAG, "getRadioParameter: radioJson = " + radioJson);
        return JsonUtils.generateObject(radioJson, RadioParameter.class);
    }

    @Override
    public void setQualityCondition(RadioParameter radioParameter) {
        String settings = JsonUtils.generateJson(radioParameter);
        Log.d(TAG, "setQualityCondition: settings = " + settings);
        mRadioTuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_FM_OP_SIGNAL_QUALITY_CONDITION, settings));
    }

    @Override
    public void playTTS() {
        int currentSourceId = AudioFocusUtils.getInstance().getCurrentSourceId();
        Log.d(TAG, "playTTS,:" +currentSourceId);
        mRadioTuner.setMute(false);
        if (currentSourceId == AudioAttributes.SOURCE_RDS_TTS) {
            switchRadioSource(RadioEvent.RadioSourceType.FM_AM);
            ArrayList<Integer> int32Values = new ArrayList<>();
            int32Values.add(0);
            mRadioTuner.set(new RadioManager.RadioEventValue(System.nanoTime(), RadioManager.EVENT_MUTE_STATE, new RadioManager.RawValue(int32Values, null, null, null, null)));
            mCurrentRadioInfo.setPlaying(!mRadioTuner.getMute());
        } else if (currentSourceId == AudioAttributes.SOURCE_DAB_TTS) {
            switchRadioSource(RadioEvent.RadioSourceType.DAB);
            DABControlAction.getInstance().play();
        }else {
            play();
        }
    }

}
