package com.desaysv.libradio.bean;


import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.dab.DABBerMessage;
import com.desaysv.libradio.bean.dab.DABEPGScheduleList;
import com.desaysv.libradio.bean.dab.DABHeadlineMessage;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.rds.RDSAnnouncement;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IPlayStatusTool;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.libradio.utils.PowerStatusUtil;
import com.desaysv.libradio.utils.RadioConversionUtils;
import com.desaysv.libradio.utils.RadioMessageSaveUtils;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 这个类是一个数据类，里面加入了界面变化的回调，用实现数据驱动界面的逻辑
 *
 * @author uidp5370
 */
public class CurrentRadioInfo implements IStatusTool {

    private static final String TAG = "CurrentRadioInfo";

    private static CurrentRadioInfo instance;

    public static CurrentRadioInfo getInstance() {
        if (instance == null) {
            synchronized (CurrentRadioInfo.class) {
                if (instance == null) {
                    instance = new CurrentRadioInfo();
                }
            }
        }
        return instance;
    }

    private CurrentRadioInfo() {

    }

    /**
     * 用来和Action对接的接口
     */
    private IPlayStatusTool mPlayStatusTool;

    /**
     * 用来记录暂停的原因
     */
    private ChangeReason pauseReason = ChangeReasonData.NA;

    /**
     * 设置获取收音播放状态的接口
     *
     * @param iPlayStatusTool action里面设置的接口，会回调数据状态过来
     */
    public void setPlayStatusTool(IPlayStatusTool iPlayStatusTool) {
        //这个是action设置给info的，可以让info拿到系统的状态
        mPlayStatusTool = iPlayStatusTool;
    }

    /**
     * AM的收音信息，初始化的时候，需要从底层数据去拿
     */
    private final RadioMessage mAMRadioMessage = RadioMessageSaveUtils.getInstance().getAMRadioMessage();


    /**
     * FM的收音信息,初始化的时候，需要从底层数据去拿
     */
    private final RadioMessage mFMRadioMessage = RadioMessageSaveUtils.getInstance().getFMRadioMessage();

    /**
     * DAB的收音信息,初始化的时候，需要从底层数据去拿
     */
    private final RadioMessage mDABRadioMessage = RadioMessageSaveUtils.getInstance().getDABRadioMessage();

    /**
     * 当前播放的收音信息，这个对象不能重新赋值，只能使用cloneRadioMessage的方法更新数据,数据的初始化，必须从底层保存的数据里面获取，不然会出现异常
     */
    private final RadioMessage mCurrentRadioMessage = RadioMessageSaveUtils.getInstance().getCurrentRadioMessage();

    /**
     * 切换到DAB前的收音信息,初始化的时候，如果是DAB的源，需要用这个来初始化Tuner
     */
    private final RadioMessage mPreRadioMessage = RadioMessageSaveUtils.getInstance().getPreRadioMessage();


    /**
     * 切换到DAB/FM融合的收音信息,融合界面需要通过这个来获取上次播放的内容
     */
    private final RadioMessage mDABorFMRadioMessage = RadioMessageSaveUtils.getInstance().getDABorFMRadioMessage();


    /**
     * 当前 收藏/取消收藏 操作 对应的 RadioMessage对象
     * 默认为空，只有收藏操作变化时变更，不需要保存
     */
    private RadioMessage mCurrentCollectRadioMessage;

    @Override
    public RadioMessage getCurrentCollectRadioMessage() {
        return mCurrentCollectRadioMessage;
    }

    public void setCurrentCollectRadioMessage(RadioMessage mCurrentCollectRadioMessage) {
        this.mCurrentCollectRadioMessage = mCurrentCollectRadioMessage;
    }

    /**
     * 用来记录当前的电台的状态是否是已经打开了
     */
    private boolean isTuned = false;

    /**
     * 设置当前频点是否打开
     *
     * @param tuned true：已经打开 false：没有打开
     */
    public void setTuned(boolean tuned) {
        isTuned = tuned;
    }

    /**
     * 获取当前频点是否是打开
     *
     * @return isTuned true：打开 false：没有打开
     */
    @Override
    public boolean getTuned() {
        return isTuned;
    }

    @Override
    public int getRssiValue() {
        return 0;
    }

    @Override
    public DABAnnSwitch getDABAnnSwitchStatus() {
        return mPlayStatusTool.getDABAnnSwitchStatus();
    }

    @Override
    public RDSSettingsSwitch getRDSSettingsSwitchStatus() {
        return mPlayStatusTool.getRDSSettingsSwitchStatus();
    }

    @Override
    public List<DABHeadlineMessage> getDABHeadlineMessage() {
        return null;
    }

    @Override
    public DABTime getDABTime() {
        return mPlayStatusTool.getDABTime();
    }

    @Override
    public DABEPGScheduleList getEPGList() {
        return mPlayStatusTool.getEPGList();
    }

    @Override
    public DABBerMessage getDABBerMessage() {
        return null;
    }

    /**
     * 设置当前播放的收音信息，这一个只有在收音回调的时候会触发，一个是频段改变，设置FM和AM，一个是频率改变，更新当前显示
     *
     * @param currentRadioMessage 当前播放的收音信息
     */
    public void setCurrentRadioMessage(RadioMessage currentRadioMessage) {
        Log.d(TAG, "setCurrentRadioMessage: currentRadioMessage = " + currentRadioMessage + " mCurrentRadioMessage = " + mCurrentRadioMessage + " isTuned = " + isTuned);
        //只有底层已经打开了，才能做持久化保存
        if (isTuned) {
            if (currentRadioMessage.getRadioType() == RadioMessage.DAB_TYPE
                    && mCurrentRadioMessage.getRadioType() == RadioMessage.FM_AM_TYPE){
                RadioMessageSaveUtils.getInstance().savePreRadioMessage(mCurrentRadioMessage);
            }
            RadioMessageSaveUtils.getInstance().saveRadioMessage(currentRadioMessage);
        }
        // 在这里判断逻辑，如果频率相同，则不做更新
//        if (mCurrentRadioMessage.getRadioFrequency() == currentRadioMessage.getRadioFrequency()) {
//            return;
//        }
        mCurrentRadioMessage.cloneRadioMessage(currentRadioMessage);
        //设置当前频率之后，还需要将FM和AM的频率设置一遍
        if (currentRadioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            int band = currentRadioMessage.getRadioBand();
            int frequency = currentRadioMessage.getRadioFrequency();
            switch (band) {
                case RadioManager.BAND_AM:
                case RadioManager.BAND_AM_HD:
                    getAMRadioMessage().setRadioFrequency(frequency);
                    break;
                case RadioManager.BAND_FM:
                case RadioManager.BAND_FM_HD:
                    getFMRadioMessage().setRadioFrequency(frequency);
                    getDABorFMRadioMessage().setRadioFrequency(frequency);
                    getDABorFMRadioMessage().setRadioType(RadioMessage.FM_AM_TYPE);
                    getDABorFMRadioMessage().setRadioBand(RadioManager.BAND_FM);
                    break;
            }
        } else {
            getDABRadioMessage().setDabMessage(currentRadioMessage.getDabMessage());
            getDABorFMRadioMessage().setDabMessage(currentRadioMessage.getDabMessage());
            getDABorFMRadioMessage().setRadioType(RadioMessage.DAB_TYPE);
            getDABorFMRadioMessage().setRadioBand(RadioMessage.DAB_BAND);
        }
        //通知上层，当前播放的电台数据发送了改变
        onCurrentRadioMessageChange();
    }


    /**
     * 是否在扫描中
     */
    private boolean isSearching = false;

    /**
     * 获取是否在搜索中
     *
     * @return true 搜索中; false 非搜索中
     */
    @Override
    public boolean isSearching() {
        return isSearching;
    }

    /**
     * 设置是否在搜索中,如果回调里面说isTuned是true，那就说明没有扫描了，就设置为false
     *
     * @param searching 当前是否是扫描状态
     */
    public void setSearching(boolean searching) {
        Log.d(TAG, "setSearching: isSearching = " + isSearching + " searching = " + searching);
        if (isSearching != searching) {
            isSearching = searching;
            onSearchStatusChange(isSearching);
        }
    }

    /**
     * 上下搜台中
     */
    private boolean isSeeking = false;

    /**
     * 获取当前是否在向上/向下搜台
     *
     * @return true：当前在上下台搜索， false：当前不在上下台搜索
     */
    @Override
    public boolean isSeeking() {
        return isSeeking;
    }

    /**
     * 记录是否正在上下搜台中
     *
     * @param seeking 上下搜台中
     */
    public void setSeeking(boolean seeking) {
        Log.d(TAG, "setSeeking: isSeeking = " + isSeeking + " seeking = " + seeking);
        if (this.isSeeking != seeking) {
            this.isSeeking = seeking;
            onSeekStatusChange(isSeeking);
        }
    }

    /**
     * 播放中的状态获取
     */
    boolean isPlaying = false;


    /**
     * 设置当前是否在播放状态
     * ·
     *
     * @param playing true 播放中; false 暂停
     */
    public void setPlaying(boolean playing) {
        Log.d(TAG, "setPlaying: isPlaying = " + isPlaying + " playing = " + playing);
        isPlaying = playing;
        savePlayingState(playing);
        onPlayStatusChange(isPlaying);
    }

    /**
     * 电源使用状态才记录播放状态,暂停
     * @param playing
     */
    private void savePlayingState(boolean playing) {
        if (playing) {
            SPUtlis.getInstance().saveRadioPlayPauseStatus(true);
        } else {//待机 STR SLEEP后的暂停不记录
            if (PowerStatusUtil.isPowerNotInStrOrSleep()) {
                SPUtlis.getInstance().saveRadioPlayPauseStatus(false);
            }
        }
    }

    /************************************************************状态获取********************************************/
    /**
     * 当前是否是播放状态
     *
     * @return true 播放; false 暂停
     */
    @Override
    public boolean isPlaying() {
        boolean isPlaying = mPlayStatusTool.isPlaying();
        Log.d(TAG, "isPlaying: isPlaying = " + isPlaying);
        boolean hasAudioFocus = RadioControlTool.getInstance().checkCurrentRadioSource();
        Log.d(TAG, "isPlaying: hasAudioFocus = " + hasAudioFocus);
        return isPlaying && hasAudioFocus;
    }

    /**
     * 获取AM的收音信息
     *
     * @return mAMRadioMessage
     */
    @Override
    public RadioMessage getAMRadioMessage() {
        Log.d(TAG, "getAMRadioMessage: mAMRadioMessage = " + mAMRadioMessage);
        return mAMRadioMessage;
    }

    /**
     * 获取FM的收音信息
     *
     * @return mFMRadioMessage
     */
    @Override
    public RadioMessage getFMRadioMessage() {
        Log.d(TAG, "getFMRadioMessage: mFMRadioMessage = " + mFMRadioMessage);
        return mFMRadioMessage;
    }

    @Override
    public RadioMessage getDABRadioMessage() {
        Log.d(TAG, "getDABRadioMessage: mDABRadioMessage = " + mDABRadioMessage);
        return mDABRadioMessage;
    }

    /**
     * 获取当前播放的收音信息
     *
     * @return mCurrentRadioMessage
     */
    @Override
    public RadioMessage getCurrentRadioMessage() {
        Log.d(TAG, "getCurrentRadioMessage: mCurrentRadioMessage = " + mCurrentRadioMessage);
        return mCurrentRadioMessage;
    }

    //初始化Tuner的时候，无法通过DAB来进行，需要记忆切换到DAB之前的是哪个FM/AM
    //这样才能打开
    @Override
    public RadioMessage getPreRadioMessage() {
        Log.d(TAG, "getPreRadioMessage: mPreRadioMessage = " + mPreRadioMessage);
        return mPreRadioMessage;
    }

    /**
     * DAB/FM融合需求，获取融合界面播放的收音信息
     * @return
     */
    @Override
    public RadioMessage getDABorFMRadioMessage() {
        Log.d(TAG, "getDABorFMRadioMessage: mDABorFMRadioMessage = " + mDABorFMRadioMessage);
        return mDABorFMRadioMessage;
    }

    /**
     * RadioManager.ProgramInfo
     * [0]频点；[1]信号强度；[2]信噪比；[3]多路径干扰；[4]频偏；[5]带宽；[6]调制度
     * @return getProgramInfo
     */
    @Override
    public ArrayList<Integer> getProgramInfo() {
        ArrayList<Integer> programInfo = mPlayStatusTool.getProgramInfo();
        Log.d(TAG, "getProgramInfo: programInfo = " + programInfo);
        if (null != programInfo) {
            RadioMessage currentRadioMessage = getCurrentRadioMessage();
            Log.d(TAG, "getProgramInfo: currentRadioMessage = " + currentRadioMessage);
            if (null == currentRadioMessage || RadioMessage.FM_AM_TYPE != currentRadioMessage.getRadioType()) {
                Log.d(TAG, "getProgramInfo: return no FM_AM_TYPE");
                return null;
            }
            int frequency = programInfo.get(0);
            programInfo.set(0, frequency);
            int band = RadioConversionUtils.frequencyGetBand(frequency);
            // 判断，当前电台 和 RadioManager.ProgramInfo 内容电台是否一致，不一致则，不需要更新
            if (band != currentRadioMessage.getRadioBand() || frequency != currentRadioMessage.getRadioFrequency()) {
                Log.d(TAG, "getProgramInfo: no current ");
                return null;
            }
        }
        return programInfo;
    }

    @Override
    public RadioMessage getCurrentRadioMessageWithEPG() {
        return mPlayStatusTool.getCurrentRadioMessageWithEPG();
    }

    /**
     * 获取搜索到的数据列表
     *
     * @param band 获取的列表的频段
     * @return radioMessageList
     */
    @Override
    public List<RadioMessage> getAstList(int band) {
        List<RadioMessage> radioMessageList = mPlayStatusTool.getAstList(band);
        Log.d(TAG, "getAstList: radioMessageList = " + radioMessageList);
        return radioMessageList;
    }

    /**
     * 获取暂停状态的原因
     *
     * @return pauseReason
     */
    public ChangeReason getPauseReason() {
        Log.d(TAG, "getPauseReason: ");
        return pauseReason;
    }

    /**
     * 设置设置暂停状态的原因
     *
     * @param pauseReason 原因
     */
    public void setPauseReason(ChangeReason pauseReason) {
        Log.d(TAG, "setPauseReason: pauseReason = " + pauseReason);
        this.pauseReason = pauseReason;
    }


    /***********************************************************状态变化回调******************************************/

    //进行加锁处理
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = mReentrantLock.writeLock();


    /**
     * 设置状态变化的回调
     */
    private List<IRadioStatusChange> mRadioStatusCallbackList = new ArrayList<>();

    public void registerRadioStatusChangeListener(IRadioStatusChange radioStatusChange) {
        Log.d(TAG, "registerRadioStatusChangeListener: radioStatusChange = " + radioStatusChange);
        writeLock.lock();
        try {
            if (radioStatusChange != null) {
                mRadioStatusCallbackList.remove(radioStatusChange);
                mRadioStatusCallbackList.add(radioStatusChange);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void unregisterRadioStatusChangerListener(IRadioStatusChange radioStatusChange) {
        Log.d(TAG, "unregisterRadioStatusChangerListener: radioStatusChange = " + radioStatusChange);
        writeLock.lock();
        try {
            if (radioStatusChange != null) {
                mRadioStatusCallbackList.remove(radioStatusChange);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 搜索的列表状态回调
     *
     * @param bandId 当前变化的是那个频段的列表
     */
    public void onAstListChanged(int bandId) {
        Log.d(TAG, "onAstListChanged: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size());
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onAstListChanged(bandId);
            }
        } finally {
            readLock.unlock();
        }

    }

    /**
     * 搜索的列表状态回调
     */
    public void onInitSuccessCallback() {
        Log.d(TAG, "onInitSuccessCallback: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size());
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onInitSuccessCallback();
            }
        } finally {
            readLock.unlock();
        }

    }


    /**
     * 收音播放状态发生改变的时候触发的回调
     *
     * @param isPlaying true：播放 false:暂停
     */
    private void onPlayStatusChange(boolean isPlaying) {
        Log.d(TAG, "onPlayStatusChange: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size()
                + " isPlaying = " + isPlaying);
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onPlayStatusChange(isPlaying);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 收音上下搜台的状态发生改变的时候，触发的回调
     *
     * @param isSeeking true：当前在上下搜台中 false：当前不在上下搜台中
     */
    private void onSeekStatusChange(boolean isSeeking) {
        Log.d(TAG, "onSeekStatusChange: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size()
                + " isSeeking = " + isSeeking);
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onSeekStatusChange(isSeeking);
            }
        } finally {
            readLock.unlock();
        }
    }


    /**
     * 收音搜索电台时，触发的回调
     *
     * @param isSearching true：当前在搜索电台 false：当前不在搜索电台
     */
    private void onSearchStatusChange(boolean isSearching) {
        Log.d(TAG, "onSearchStatusChange: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size());
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onSearchStatusChange(isSearching);
            }
        } finally {
            readLock.unlock();
        }
    }

    private void onCurrentRadioMessageChange() {
        Log.d(TAG, "onCurrentRadioMessageChange: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size());
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onCurrentRadioMessageChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知界面变化
     *
     * @param annNotify annNotify
     */
    public void setAnnNotifyStatus(DABAnnNotify annNotify) {
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onAnnNotify(annNotify);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知界面变化
     *
     * @param info RDSFlagInfo
     */
    public void onRDSFlagInfoChange(RDSFlagInfo info) {
        rdsFlagInfo = info;
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onRDSFlagInfoChange(info);
            }
        } finally {
            readLock.unlock();
        }
    }


    /**
     * 通知界面变化
     *
     * @param rdsAnnouncement  公告的内容，例如TA
     */
    public void onRDSAnnChange(RDSAnnouncement rdsAnnouncement) {
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onRDSAnnNotify(rdsAnnouncement);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @param dabTime  dab时间变化
     */
    public void onDABTimeChange(DABTime dabTime) {
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onDABTimeNotify(dabTime);
            }
        } finally {
            readLock.unlock();
        }
    }


    /**
     * @param signalValue  dab信号标志，1 是 Low，0 是 Good
     */
    public void onDABSignalChanged(int signalValue) {
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onDABSignalChanged(signalValue);
            }
        } finally {
            readLock.unlock();
        }
    }


    private RDSFlagInfo rdsFlagInfo;

    /**
     * 获取当前的RDS状态标志//TP/AF
     * @return
     */
    @Override
    public RDSFlagInfo getCurrentRDSFlagInfo(){
        return rdsFlagInfo;
    }

    /**
     * RDS设置状态的回调
     *
     * @param rdsSettingsSwitch 当前设置的RDS状态
     */
    public void onRDSSettingsStatusChange(RDSSettingsSwitch rdsSettingsSwitch) {
        Log.d(TAG, "onRDSSettingsStatusChange: mRadioStatusCallbackList size = " + mRadioStatusCallbackList.size());
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onRDSSettingsStatus(rdsSettingsSwitch);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 清除Logo数据
     */
    public void clearLogo(){
        Log.d(TAG,"clearLogo");
        setCurrentDABLogo(0,new byte[]{});
    }

    /**
     * DAB logo 消息变化
     * @param logoLen logo数据长度
     * @param logoByte logo数据
     */
    public void setCurrentDABLogo(int logoLen, byte[] logoByte){
        Log.d(TAG,"setCurrentDABLogo");
        onDABLogoChange(logoByte);
    }
    /**
     * @param logoByte  dab logo变化
     */
    public void onDABLogoChange(byte[] logoByte) {
        Log.d(TAG,"onDABLogoChange");
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onDABLogoChanged(logoByte);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void onRadioRegionChanged() {
        Log.d(TAG,"onRadioRegionChanged");
        readLock.lock();
        try {
            for (IRadioStatusChange radioStatusChange : mRadioStatusCallbackList) {
                radioStatusChange.onRadioRegionChanged();
            }
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public RadioParameter getRadioParameter() {
        return mPlayStatusTool.getRadioParameter();
    }
}
