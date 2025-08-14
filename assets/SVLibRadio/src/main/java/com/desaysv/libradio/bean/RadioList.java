package com.desaysv.libradio.bean;

import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.dab.DABSliderShow;
import com.desaysv.libradio.bean.rds.RDSRadioName;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.utils.RadioListSaveUtils;
import com.desaysv.libradio.utils.RadioMessageSaveUtils;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by LZM on 2019-8-7.
 * Comment 收音的列表子类，根据不同需求，可以在这里修改
 */
public class RadioList {

    private static final String TAG = "RadioList";

    /**
     * 收音数据的观察者模式
     */
    private final List<IRadioMessageListChange> mRadioListChangeCallbackListener = new CopyOnWriteArrayList<>();

    //进行加锁处理
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();

    //读锁，排斥写操作
    private final Lock readLock = mReentrantLock.readLock();

    //写锁，排斥读与写操作
    private final Lock writeLock = mReentrantLock.writeLock();

    private static final int ITEM_NO_IN_LIST = -1;

    /**
     * AM的全部列表，里面包含了有效的电台和收藏的电台
     */
    private final List<RadioMessage> AMAllRadioMessageList = new ArrayList<>();

    /**
     * AM的电台收藏列表
     */
    private final List<RadioMessage> AMCollectRadioMessageList = new CopyOnWriteArrayList<>();


    /**
     * AM的有效电台列表
     */
    private final List<RadioMessage> AMEffectRadioMessageList = new CopyOnWriteArrayList<>();

    /**
     * FM的电台列表，里面包括了收藏和有效列表
     */
    private final List<RadioMessage> FMAllRadioMessageList = new CopyOnWriteArrayList<>();


    /**
     * FM的电台收藏列表
     */
    private final List<RadioMessage> FMCollectRadioMessageList = new CopyOnWriteArrayList<>();

    /**
     * FM的有效电台列表
     */
    private final List<RadioMessage> FMEffectRadioMessageList = new CopyOnWriteArrayList<>();

    /**
     * DAB的电台列表，里面包括了收藏和有效列表
     */
    private final List<RadioMessage> DABAllRadioMessageList = new ArrayList<>();


    /**
     * DAB的电台收藏列表
     */
    private final List<RadioMessage> DABCollectRadioMessageList = new CopyOnWriteArrayList<>();

    /**
     * DAB的有效电台列表
     */
    private final List<RadioMessage> DABEffectRadioMessageList = new CopyOnWriteArrayList<>();

    /**
     * 全部的收藏列表
     */
    private final List<RadioMessage> AllCollectRadioMessageList = new ArrayList<>();


    /**
     * FM播放列表
     */
    private final List<RadioMessage> FMPlayRadioMessageList = new ArrayList<>();
    /**
     * AM播放列表
     */
    private final List<RadioMessage> AMPlayRadioMessageList = new ArrayList<>();
    /**
     * DAB播放列表
     */
    private final List<RadioMessage> DABPlayRadioMessageList = new ArrayList<>();


    /**
     * DAB/FM 组合列表
     */
    private final CopyOnWriteArrayList<RadioMessage> MultiRadioMessageList = new CopyOnWriteArrayList<>();


    /**
     * DAB/FM 组合收藏列表
     */
    private final List<RadioMessage> MultiCollectRadioMessageList = new ArrayList<>();

    /**
     * RDS名称列表
     */
    private final List<RDSRadioName> rdsRadioNameList = new ArrayList<>();

    /**
     * DAB logo 列表
     */
    private final CopyOnWriteArrayList<DABLogo> dabLogoList = new CopyOnWriteArrayList<>();

    /**
     * DAB SliderShow列表
     */
    private final ArrayList<DABSliderShow> dabSliderShowList = new ArrayList<>();



    private static RadioList instance;

    public static RadioList getInstance() {
        if (instance == null) {
            synchronized (RadioList.class) {
                if (instance == null) {
                    instance = new RadioList();
                }
            }
        }
        return instance;
    }

    /**
     * 获取AM电台的有效列表
     *
     * @return AMEffectRadioMessageList
     */
    public List<RadioMessage> getAMEffectRadioMessageList() {
        return AMEffectRadioMessageList;
    }

    /**
     * 获取AM电台的收藏列表
     *
     * @return FMCollectRadioMessageList
     */
    public List<RadioMessage> getAMCollectRadioMessageList() {
        return AMCollectRadioMessageList;
    }

    /**
     * 获取AM的全部音乐列表
     *
     * @return AMRadioMessageList
     */
    public List<RadioMessage> getAMAllRadioMessageList() {
        return AMAllRadioMessageList;
    }

    /**
     * 获取FM电台的有效列表
     *
     * @return FMEffectRadioMessageList
     */
    public List<RadioMessage> getFMEffectRadioMessageList() {
        return FMEffectRadioMessageList;
    }

    /**
     * 获取FM电台的收藏列表
     *
     * @return FMCollectRadioMessageList
     */
    public List<RadioMessage> getFMCollectRadioMessageList() {
        return FMCollectRadioMessageList;
    }

    /**
     * 获取FM的全部音乐列表
     *
     * @return FMRadioMessageList
     */
    public List<RadioMessage> getFMAllRadioMessageList() {
        return FMAllRadioMessageList;
    }

    /**
     * 获取DAB电台的有效列表
     *
     * @return DABEffectRadioMessageList
     */
    public List<RadioMessage> getDABEffectRadioMessageList() {
        return DABEffectRadioMessageList;
    }

    /**
     * 获取DAB电台的收藏列表
     *
     * @return DABCollectRadioMessageList
     */
    public List<RadioMessage> getDABCollectRadioMessageList() {
        return DABCollectRadioMessageList;
    }

    /**
     * 获取`DAB`的全部音乐列表
     *
     * @return DABRadioMessageList
     */
    public List<RadioMessage> getDABAllRadioMessageList() {
        return DABAllRadioMessageList;
    }

    public List<RadioMessage> getAllCollectRadioMessageList() {
        return AllCollectRadioMessageList;
    }

    public List<RadioMessage> getCurrentCollectRadioMessageList() {
        if (CurrentRadioInfo.getInstance().getCurrentRadioMessage().getRadioBand() ==RadioManager.BAND_AM) {
            return AMCollectRadioMessageList;
        } else {
            return MultiCollectRadioMessageList;
        }
    }

    /**
     * 获取FM播放列表
     *
     * @return FMPlayRadioMessageList
     */
    public List<RadioMessage> getFMPlayRadioMessageList() {
        return FMPlayRadioMessageList;
    }

    /**
     * 获取AM播放列表
     *
     * @return AMPlayRadioMessageList
     */
    public List<RadioMessage> getAMPlayRadioMessageList() {
        return AMPlayRadioMessageList;
    }

    /**
     * 获取DAB播放列表
     *
     * @return DABPlayRadioMessageList
     */
    public List<RadioMessage> getDABPlayRadioMessageList() {
        return DABPlayRadioMessageList;
    }


    /**
     * 获取DAB/FM 组合列表
     *
     * @return MultiRadioMessageList
     */
    public List<RadioMessage> getMultiRadioMessageList() {
        MultiRadioMessageList.clear();
        MultiRadioMessageList.addAll(getFMEffectRadioMessageList());
        MultiRadioMessageList.addAll(getDABEffectRadioMessageList());
        return MultiRadioMessageList;
    }


    /**
     * 获取DAB/FM 组合收藏列表
     *
     * @return MultiCollectRadioMessageList
     */
    public List<RadioMessage> getMultiCollectRadioMessageList() {
        return MultiCollectRadioMessageList;
    }

    /**
     * 列表的初始化逻辑，只需要初始化一次就好
     * TODO: 获取数据时
     */
    public void initialize() {
        //从数据库获取保存的收音列表，是要做在子线程里面的
        MediaThreadPoolExecutorUtils.getInstance().submit(() -> {
            //AM
            AMCollectRadioMessageList.clear();
            AMCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getAMCollectList());
            AMEffectRadioMessageList.clear();
            AMEffectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getAMEffectList());
            //FM
            FMCollectRadioMessageList.clear();
            FMCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getFMCollectList());
            FMEffectRadioMessageList.clear();
            FMEffectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getFMEffectList());
            //DAB
            DABCollectRadioMessageList.clear();
            DABCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getDABCollectList());
            DABEffectRadioMessageList.clear();
            DABEffectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getDABEffectList());
            //ALL
            AllCollectRadioMessageList.clear();
            AllCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getAllCollectList());
            //DAB/FM 组合列表
            MultiRadioMessageList.clear();
            MultiRadioMessageList.addAll(RadioListSaveUtils.getInstance().getFMEffectList());
            MultiRadioMessageList.addAll(RadioListSaveUtils.getInstance().getDABEffectList());


            //DAB/FM 组合收藏列表
            MultiCollectRadioMessageList.clear();
//            MultiCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getAMCollectList());
            MultiCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getFMCollectList());
            MultiCollectRadioMessageList.addAll(RadioListSaveUtils.getInstance().getDABCollectList());

            updateAMAllList();
            updateFMAllList();
            updateDABAllList();
            //EPG订阅数据
            subscribeEPGList.clear();
            subscribeEPGList.addAll(RadioListSaveUtils.getInstance().getSubscribeEPGList());
            FMPlayRadioMessageList.clear();
            FMPlayRadioMessageList.addAll(RadioListSaveUtils.getInstance().getFMPlayList());
            AMPlayRadioMessageList.clear();
            AMPlayRadioMessageList.addAll(RadioListSaveUtils.getInstance().getAMPlayList());
            DABPlayRadioMessageList.clear();
            AMPlayRadioMessageList.addAll(RadioListSaveUtils.getInstance().getDABPlayList());

            //搜索历史
            searchHistoryList.addAll(RadioListSaveUtils.getInstance().getRadioSearchHistoryList());

            rdsRadioNameList.addAll(RadioListSaveUtils.getInstance().getRDSNameList());
            dabLogoList.addAll(RadioListSaveUtils.getInstance().getDABLogoList());
        });
    }

    /**
     * 更新有效的电台列表
     *
     * @param band                   更新的收音频段
     * @param effectRadioMessageList 需要更新的列表
     */
    public void updateEffectRadioMessageList(int band, List<RadioMessage> effectRadioMessageList) {
        if (effectRadioMessageList != null && !effectRadioMessageList.isEmpty()){
            Log.d(TAG, "updateEffectRadioMessageList: band = " + band + " size = " + effectRadioMessageList.size());
        }else {
            Log.d(TAG, "updateEffectRadioMessageList is empty");
//            return;
        }
        switch (band) {
            case RadioManager.BAND_AM:
                updateAMEffectRadioMessageList(effectRadioMessageList);
                break;
            case RadioManager.BAND_FM:
                updateFMEffectRadioMessageList(effectRadioMessageList);
                break;
            case RadioMessage.DAB_BAND:
                updateDABEffectRadioMessageList(effectRadioMessageList);
                break;
        }
    }

    /**
     * 更新收藏列表
     *
     * @param radioMessage 添加收藏的收音信息
     */
    public void updateCollectList(RadioMessage radioMessage) {
        Log.d(TAG, "updateCollectList: radioMessage = " + radioMessage);
        if (radioMessage.getRadioFrequency() == -1 && radioMessage.getRadioType() != RadioMessage.DAB_TYPE) {
            return;
        }
        RadioMessage radioMessage1 = radioMessage.Clone();
        updateAllCollectRadioMessageList(radioMessage1);
        if (radioMessage1.getRadioType() == RadioMessage.DAB_TYPE) {
            updateDABCollectRadioMessageList(radioMessage1);
            return;
        }
        switch (radioMessage1.getRadioBand()) {
            case RadioManager.BAND_AM:
                updateAMCollectRadioMessageList(radioMessage1);
                break;
            case RadioManager.BAND_FM:
                updateFMCollectRadioMessageList(radioMessage1);
                break;
        }
    }

    /**
     * 收藏电台
     *
     * @param radioMessage 电台信息
     */
    public void collectRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "collectRadioMessage: radioMessage = " + radioMessage);
        if (radioMessage.getRadioFrequency() == -1 && radioMessage.getRadioType() != RadioMessage.DAB_TYPE) {
            return;
        }
        //先判断是否是已经收藏了
        int position = checkRadioMessageInList(AllCollectRadioMessageList, radioMessage);
        //如果不在全部收藏列表里面，那就说明没有收藏过，需要执行收藏逻辑
        if (position == ITEM_NO_IN_LIST) {
            //那就执行收藏的逻辑
            updateCollectList(radioMessage);
        }
    }

    /**
     * 取消收藏电台
     *
     * @param radioMessage 电台信息
     */
    public void cancelCollectRadioMessage(RadioMessage radioMessage) {
        Log.d(TAG, "cancelCollectRadioMessage: radioMessage = " + radioMessage);
        if (radioMessage.getRadioFrequency() == -1 && radioMessage.getRadioType() != RadioMessage.DAB_TYPE) {
            return;
        }
        //先判断是否是已经收藏了
        int position = checkRadioMessageInList(AllCollectRadioMessageList, radioMessage);
        //如果在收藏列表里面，那就说明需要执行取消收藏的逻辑
        if (position != ITEM_NO_IN_LIST) {
            //那就执行收藏的逻辑
            updateCollectList(radioMessage);
        }
    }

    /**
     * 更新AM的收音列表信息
     */
    private void updateAMAllList() {
        AMAllRadioMessageList.clear();
        AMAllRadioMessageList.addAll(AMCollectRadioMessageList);
        AMAllRadioMessageList.addAll(AMCollectRadioMessageList.size(), AMEffectRadioMessageList);
        deleteSameItem(AMAllRadioMessageList);
        notifyAMAllListChange();
    }

    /**
     * 更新FM的收音列表信息
     */
    private void updateFMAllList() {
        FMAllRadioMessageList.clear();
        FMAllRadioMessageList.addAll(FMCollectRadioMessageList);
        FMAllRadioMessageList.addAll(FMCollectRadioMessageList.size(), FMEffectRadioMessageList);
        deleteSameItem(FMAllRadioMessageList);
        notifyFMAllListChange();
    }

    /**
     * 更新DAB的收音列表信息
     */
    private void updateDABAllList() {
        DABAllRadioMessageList.clear();
        DABAllRadioMessageList.addAll(DABCollectRadioMessageList);
        DABAllRadioMessageList.addAll(DABCollectRadioMessageList.size(), DABEffectRadioMessageList);
        deleteSameItem(DABAllRadioMessageList);
        notifyDABAllListChange();
    }

    /**
     * 更新全部的收藏列表
     *
     * @param radioMessage 更新的列表
     */
    private void updateAllCollectRadioMessageList(RadioMessage radioMessage) {
        int position = checkRadioMessageInList(AllCollectRadioMessageList, radioMessage);
        Log.d(TAG, "updateAllCollectRadioMessageList: radioMessage = " + radioMessage + " position = " + position);
        if (position == -1) {
            AllCollectRadioMessageList.add(0, radioMessage);
        } else {
            AllCollectRadioMessageList.remove(position);
        }
        RadioListSaveUtils.getInstance().saveAllCollectList(AllCollectRadioMessageList);
    }

    /**
     * 更新AM的收藏列表
     *
     * @param radioMessage 更新的列表
     */
    private void updateAMCollectRadioMessageList(RadioMessage radioMessage) {
        int position = checkRadioMessageInList(AMCollectRadioMessageList, radioMessage);
        Log.d(TAG, "updateAMCollectRadioMessageList: radioMessage = " + radioMessage + " position = " + position);
        if (position == -1) {
            AMCollectRadioMessageList.add(0, radioMessage);
        } else {
            AMCollectRadioMessageList.remove(position);
        }
        RadioListSaveUtils.getInstance().saveAMCollectList(AMCollectRadioMessageList);
        MultiCollectRadioMessageList.clear();
        //MultiCollectRadioMessageList.addAll(AMCollectRadioMessageList);
        MultiCollectRadioMessageList.addAll(FMCollectRadioMessageList);
        MultiCollectRadioMessageList.addAll(DABCollectRadioMessageList);
        notifyAMCollectListChange();
        updateAMAllList();
    }

    /**
     * 更新FM的收藏列表
     *
     * @param radioMessage 需要更新的列表
     */
    private void updateFMCollectRadioMessageList(RadioMessage radioMessage) {
        int position = checkRadioMessageInList(FMCollectRadioMessageList, radioMessage);
        Log.d(TAG, "updateFMCollectRadioMessageList: radioMessage = " + radioMessage + " position = " + position);
        if (position == -1) {
            FMCollectRadioMessageList.add(0, radioMessage);
        } else {
            FMCollectRadioMessageList.remove(position);
        }
        RadioListSaveUtils.getInstance().saveFMCollectList(FMCollectRadioMessageList);
        MultiCollectRadioMessageList.clear();
        //MultiCollectRadioMessageList.addAll(AMCollectRadioMessageList);
        MultiCollectRadioMessageList.addAll(FMCollectRadioMessageList);
        MultiCollectRadioMessageList.addAll(DABCollectRadioMessageList);
        notifyFMCollectListChange();
        updateFMAllList();
    }

    /**
     * 更新DAB的收藏列表
     *
     * @param radioMessage 需要更新的列表
     */
    private void updateDABCollectRadioMessageList(RadioMessage radioMessage) {
        int position = checkRadioMessageInList(DABCollectRadioMessageList, radioMessage);
        Log.d(TAG, "updateDABCollectRadioMessageList: radioMessage = " + radioMessage + " position = " + position);
        if (position == -1) {
            DABCollectRadioMessageList.add(0, radioMessage);
        } else {
            DABCollectRadioMessageList.remove(position);
        }
        RadioListSaveUtils.getInstance().saveDABCollectList(DABCollectRadioMessageList);
        MultiCollectRadioMessageList.clear();
        //MultiCollectRadioMessageList.addAll(AMCollectRadioMessageList);
        MultiCollectRadioMessageList.addAll(FMCollectRadioMessageList);
        MultiCollectRadioMessageList.addAll(DABCollectRadioMessageList);
        notifyDABCollectListChange();
        updateDABAllList();
    }

    /**
     * 更新AM的有效电台列表
     *
     * @param effectRadioMessageList 需要更新的列表
     */
    private void updateAMEffectRadioMessageList(List<RadioMessage> effectRadioMessageList) {
        AMEffectRadioMessageList.clear();
        AMEffectRadioMessageList.addAll(effectRadioMessageList);
        RadioListSaveUtils.getInstance().saveAMEffectList(AMEffectRadioMessageList);

        //有效列表更新时，需要重置播放列表
        AMPlayRadioMessageList.clear();
        AMPlayRadioMessageList.addAll(effectRadioMessageList);
        RadioListSaveUtils.getInstance().saveAMPlayList(AMPlayRadioMessageList);

        notifyAMEffectListChange();
        updateAMAllList();
    }

    /**
     * 更新FM的有效电台列表
     *
     * @param effectRadioMessageList 需要更新的列表
     */
    private void updateFMEffectRadioMessageList(List<RadioMessage> effectRadioMessageList) {
        FMEffectRadioMessageList.clear();
        FMEffectRadioMessageList.addAll(effectRadioMessageList);
        RadioListSaveUtils.getInstance().saveFMEffectList(FMEffectRadioMessageList);

        MultiRadioMessageList.clear();
        MultiRadioMessageList.addAll(FMEffectRadioMessageList);
        MultiRadioMessageList.addAll(DABEffectRadioMessageList);

        //有效列表更新时，需要重置播放列表
        FMPlayRadioMessageList.clear();
        FMPlayRadioMessageList.addAll(effectRadioMessageList);
        RadioListSaveUtils.getInstance().saveFMPlayList(FMPlayRadioMessageList);

        notifyFMEffectListChange();
        updateFMAllList();
    }

    /**
     * 更新DAB的有效电台列表
     *
     * @param effectRadioMessageList 需要更新的列表
     */
    private void updateDABEffectRadioMessageList(List<RadioMessage> effectRadioMessageList) {
        DABEffectRadioMessageList.clear();
        DABEffectRadioMessageList.addAll(effectRadioMessageList);
        RadioListSaveUtils.getInstance().saveDABEffectList(DABEffectRadioMessageList);

        MultiRadioMessageList.clear();
        MultiRadioMessageList.addAll(FMEffectRadioMessageList);
        MultiRadioMessageList.addAll(DABEffectRadioMessageList);

        //有效列表更新时，需要重置播放列表
        DABPlayRadioMessageList.clear();
        DABPlayRadioMessageList.addAll(effectRadioMessageList);
        RadioListSaveUtils.getInstance().saveDABPlayList(DABPlayRadioMessageList);

        notifyDABEffectListChange();
        updateDABAllList();
    }

    /**
     * 根据当前播放内容，更新当前播放列表
     * @param radioMessage
     */
    public void updatePlayList(RadioMessage radioMessage){
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
            updateDABPlayList(radioMessage);
            return;
        }
        switch (radioMessage.getRadioBand()) {
            case RadioManager.BAND_AM:
                updateAMPlayList(radioMessage);
                break;
            case RadioManager.BAND_FM:
                updateFMPlayList(radioMessage);
                break;
        }
    }

    /**
     * 根据当前播放内容，更新当前播放列表
     * @param radioMessage
     */
    private void updateFMPlayList(RadioMessage radioMessage){
        if (FMEffectRadioMessageList.size() < 1){
            Log.d(TAG, "updateFMPlayList at no effect list,return");
            return;
        }
        if (checkRadioMessageInList(FMPlayRadioMessageList,radioMessage) == ITEM_NO_IN_LIST){
            Log.d(TAG, "updateFMPlayList: radioMessage = " + radioMessage);
            FMPlayRadioMessageList.add(0,radioMessage);
            RadioListSaveUtils.getInstance().saveFMPlayList(FMPlayRadioMessageList);
            notifyFMPlayListChange();
        }
    }

    /**
     * 根据当前播放内容，更新当前播放列表
     * @param radioMessage
     */
    private void updateAMPlayList(RadioMessage radioMessage){
        if (AMEffectRadioMessageList.size() < 1){
            Log.d(TAG, "updateAMPlayList at no effect list,return");
            return;
        }
        if (checkRadioMessageInList(AMPlayRadioMessageList,radioMessage) == ITEM_NO_IN_LIST){
            Log.d(TAG, "updateAMPlayList: radioMessage = " + radioMessage);
            AMPlayRadioMessageList.add(0,radioMessage);
            RadioListSaveUtils.getInstance().saveAMPlayList(AMPlayRadioMessageList);
            notifyAMPlayListChange();
        }
    }

    /**
     * 根据当前播放内容，更新当前播放列表
     * @param radioMessage
     */
    private void updateDABPlayList(RadioMessage radioMessage){
        if (DABEffectRadioMessageList.size() < 1){
            Log.d(TAG, "updateDABPlayList at no effect list,return");
            return;
        }
        if (checkRadioMessageInList(DABPlayRadioMessageList,radioMessage) == ITEM_NO_IN_LIST){
            Log.d(TAG, "updateDABPlayList: radioMessage = " + radioMessage);
            DABPlayRadioMessageList.add(0,radioMessage);
            RadioListSaveUtils.getInstance().saveDABPlayList(DABPlayRadioMessageList);
            notifyDABPlayListChange();
        }
    }


    /**
     * 通知播放列表发生改变
     */
    private void notifyFMPlayListChange() {
        Log.d(TAG, "notifyFMPlayListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onFMPlayListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知播放列表发生改变
     */
    private void notifyAMPlayListChange() {
        Log.d(TAG, "notifyAMPlayListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onAMPlayListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知播放列表发生改变
     */
    private void notifyDABPlayListChange() {
        Log.d(TAG, "notifyDABPlayListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onDABPlayListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 注册观察者模式
     *
     * @param radioMessageListChange 观察者的回调
     */
    public void registerRadioListListener(IRadioMessageListChange radioMessageListChange) {
        Log.d(TAG, "registerRadioListListener: radioMessageListChange = " + radioMessageListChange);
        writeLock.lock();
        try {
            if (radioMessageListChange != null) {
                mRadioListChangeCallbackListener.remove(radioMessageListChange);
                mRadioListChangeCallbackListener.add(radioMessageListChange);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 注销观察者
     *
     * @param radioMessageListChange 观察者的回调
     */
    public void unRegisterRadioListListener(IRadioMessageListChange radioMessageListChange) {
        Log.d(TAG, "unRegisterRadioListListener: radioMessageListChange = " + radioMessageListChange);
        writeLock.lock();
        try {
            if (radioMessageListChange != null) {
                mRadioListChangeCallbackListener.remove(radioMessageListChange);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 通知AM有效列表发生改变
     */
    private void notifyAMEffectListChange() {
        Log.d(TAG, "notifyAMEffectListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onAMEffectListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知AM收藏列表发生改变
     */
    private void notifyAMCollectListChange() {
        Log.d(TAG, "notifyAMCollectListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onAMCollectListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知AM全部列表发生改变
     */
    private void notifyAMAllListChange() {
        Log.d(TAG, "notifyAMAllListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onAMAllListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知FM有效列表发生改变
     */
    private void notifyFMEffectListChange() {
        Log.d(TAG, "notifyFMEffectListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onFMEffectListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知FM收藏列表发生改变
     */
    private void notifyFMCollectListChange() {
        Log.d(TAG, "notifyFMCollectListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onFMCollectListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知FM全部列表发生改变
     */
    private void notifyFMAllListChange() {
        Log.d(TAG, "notifyFMAllListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onFMAllListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知DAB有效列表发生改变
     */
    private void notifyDABEffectListChange() {
        Log.d(TAG, "notifyDABEffectListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onDABEffectListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知DAB收藏列表发生改变
     */
    private void notifyDABCollectListChange() {
        Log.d(TAG, "notifyDABCollectListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onDABCollectListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通知DAB全部列表发生改变
     */
    private void notifyDABAllListChange() {
        Log.d(TAG, "notifyDABAllListChange: observers size = " + mRadioListChangeCallbackListener);
        readLock.lock();
        try {
            for (IRadioMessageListChange radioMessageListChange : mRadioListChangeCallbackListener) {
                radioMessageListChange.onDABAllListChange();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 检测当前的收音信息是否在列表里面
     *
     * @param radioMessageList 检测的列表
     * @param radioMessage     检测的收音信息
     * @return position 在列表中的位置 ITEM_NO_IN_LIST为不在列表中
     */
    private int checkRadioMessageInList(List<RadioMessage> radioMessageList, RadioMessage radioMessage) {
        Log.d(TAG, "checkRadioMessageInList: radioMessageList = " + radioMessageList);
        Log.d(TAG, "checkRadioMessageInList: radioMessage = " + radioMessage);
        int position = ITEM_NO_IN_LIST;
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
            for (int i = 0; i < radioMessageList.size(); i++) {
                DABMessage dabMessageI = radioMessageList.get(i).getDabMessage();
                DABMessage dabMessageJ = radioMessage.getDabMessage();
                if (dabMessageI != null && dabMessageI.getFrequency() == dabMessageJ.getFrequency()
                        && dabMessageI.getServiceId() == dabMessageJ.getServiceId()
                        && dabMessageI.getServiceComponentId() == dabMessageJ.getServiceComponentId()) {
                    position = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < radioMessageList.size(); i++) {
                if (radioMessageList.get(i).getRadioFrequency() == radioMessage.getRadioFrequency()) {
                    position = i;
                    break;
                }
            }
        }
        Log.d(TAG, "checkRadioMessageInList: position = " + position);
        return position;
    }

    /**
     * 删除列表中的相投项
     *
     * @param radioMessageList 需要去除相同项的列表
     */
    private void deleteSameItem(List<RadioMessage> radioMessageList) {
        Log.d(TAG, "deleteSameItem: radioMessageList = " + radioMessageList);
        if (radioMessageList.isEmpty()) {
            return;
        }
        if (radioMessageList.get(0).getRadioType() == RadioMessage.FM_AM_TYPE) {
            for (int i = 0; i < radioMessageList.size(); i++) {
                for (int j = i + 1; j < radioMessageList.size(); j++) {
                    if (radioMessageList.get(i).getRadioFrequency() == radioMessageList.get(j).getRadioFrequency()) {
                        radioMessageList.remove(j);
                        break;
                    }
                }
            }
        } else {
            //DAB需具备频点、ServiceId和ServiceComponentId才能算是相同电台
            for (int i = 0; i < radioMessageList.size(); i++) {
                for (int j = i + 1; j < radioMessageList.size(); j++) {
                    DABMessage dabMessageI = radioMessageList.get(i).getDabMessage();
                    DABMessage dabMessageJ = radioMessageList.get(j).getDabMessage();
                    if (dabMessageI.getFrequency() == dabMessageJ.getFrequency()
                            && dabMessageI.getServiceId() == dabMessageJ.getServiceId()
                            && dabMessageI.getServiceComponentId() == dabMessageJ.getServiceComponentId()) {
                        radioMessageList.remove(j);
                        break;
                    }
                }
            }
        }
        Log.d(TAG, "deleteSameItem: radioMessageList = " + radioMessageList + " size = " + radioMessageList.size());
    }

    /**
     * 检测当前的收音信息是否是已经收藏的,公用的方法
     *
     * @param radioMessage 检测的收音信息
     * @return isCollect true：已经收藏；flase：没有收藏
     */
    boolean checkIsCollect(RadioMessage radioMessage) {
        boolean isCollect = false;
        //如果是DAB，则其它方式
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
            isCollect = checkRadioMessageInList(DABCollectRadioMessageList, radioMessage) != ITEM_NO_IN_LIST;
        } else {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    isCollect = checkRadioMessageInList(AMCollectRadioMessageList, radioMessage) != ITEM_NO_IN_LIST;
                    break;
                case RadioManager.BAND_FM:
                    isCollect = checkRadioMessageInList(FMCollectRadioMessageList, radioMessage) != ITEM_NO_IN_LIST;
                    break;
            }
        }
        Log.d(TAG, "checkIsCollect: isCollect = " + isCollect + " radioType = " + radioMessage.getRadioType());
        return isCollect;
    }

    /**
     * EPG预约列表
     */
    private final List<DABEPGSchedule> subscribeEPGList = new ArrayList<>();


    public List<DABEPGSchedule> getEPGSubscribeList(){
        return subscribeEPGList;
    }

    /**
     * 更新EPG预约列表
     * @param dabepgSchedule
     */
    public void updateEPGSubscribeList(DABEPGSchedule dabepgSchedule){
        int checkInList = checkInEPGSubscribeList(dabepgSchedule);
        Log.d(TAG,"updateEPGSubscribeList,checkInList："+checkInList);
        if (checkInList == ITEM_NO_IN_LIST){
            subscribeEPGList.add(dabepgSchedule);
        }else {
            subscribeEPGList.remove(dabepgSchedule);
        }
        RadioListSaveUtils.getInstance().saveSubscribe(dabepgSchedule,checkInList == ITEM_NO_IN_LIST);
    }

    /**
     * 判断当前订阅的数据是否已经存在
     * @param dabepgSchedule
     * @return
     */
    public int checkInEPGSubscribeList(DABEPGSchedule dabepgSchedule){
        int position = ITEM_NO_IN_LIST;
        for (int i = 0; i < subscribeEPGList.size(); i++){
            if (dabepgSchedule.equals(subscribeEPGList.get(i))){
                position = i;
            }
        }
        Log.d(TAG,"checkInEPGSubscribeList,position:"+position);
        return position;
    }


    /**
     * 这个是收音区域变化后，需要清除所有的收音列表 并重置 FM/AM 初始值
     */
    public void clearAllList() {
        Log.d(TAG, "clearAllList: ");
        RadioMessageSaveUtils.getInstance().saveRadioMessage(new RadioMessage(RadioManager.BAND_FM, RadioConfig.FM_MIN));
        RadioMessageSaveUtils.getInstance().saveRadioMessage(new RadioMessage(RadioManager.BAND_AM, RadioConfig.AM_MIN));
        MediaThreadPoolExecutorUtils.getInstance().submit(() -> {
            FMEffectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveFMEffectList(FMEffectRadioMessageList);
            notifyFMEffectListChange();
            AMEffectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveAMEffectList(AMEffectRadioMessageList);
            notifyAMEffectListChange();
            FMCollectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveFMCollectList(FMCollectRadioMessageList);
            notifyFMCollectListChange();
            AMCollectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveAMCollectList(AMCollectRadioMessageList);
            notifyAMCollectListChange();

            DABEffectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveDABEffectList(DABEffectRadioMessageList);
            DABCollectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveDABCollectList(DABCollectRadioMessageList);

            AllCollectRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveAllCollectList(AllCollectRadioMessageList);

            FMPlayRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveFMPlayList(FMPlayRadioMessageList);

            AMPlayRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveAMPlayList(AMPlayRadioMessageList);

            DABPlayRadioMessageList.clear();
            RadioListSaveUtils.getInstance().saveDABPlayList(DABPlayRadioMessageList);
            Log.d(TAG, "clearAllList: end");
        });
    }

    /**
     * 根据最新系统需求，收藏总数变更为 FM+AM 不能超过30个
     * @return
     */
    public boolean isFMAMFullCollect(){
        return FMCollectRadioMessageList.size() + AMCollectRadioMessageList.size() > 29;
    }


    /**
     * 搜索历史列表
     */
    private  List<String> searchHistoryList = new ArrayList<>();
    public List<String> getSearchHistoryList() {
        return searchHistoryList;
    }

    public void updateSearchHistoryList(String searchName) {
        if (searchHistoryList.contains(searchName)){
            searchHistoryList.remove(searchName);
        }
        searchHistoryList.add(0,searchName);
        if (searchHistoryList.size() > 20){//超出范围，移除最老的那条数据
            searchHistoryList.remove(searchHistoryList.size() - 1);
        }
        RadioListSaveUtils.getInstance().saveRadioSearchHistory(searchHistoryList);
    }

    public void deleteSearchHistoryList(){
        searchHistoryList.clear();
        RadioListSaveUtils.getInstance().deleteRadioSearchHistory();
    }

    public List<RDSRadioName> getRdsRadioNameList() {
        return rdsRadioNameList;
    }

    public List<DABLogo> getDabLogoList() {
        return dabLogoList;
    }

    public void updateRDSNameList(List<RDSRadioName> updateList){
        Log.d(TAG,"updateRDSNameList:"+updateList);
        rdsRadioNameList.clear();
        rdsRadioNameList.addAll(updateList);
        RadioListSaveUtils.getInstance().updateRdsNameList(rdsRadioNameList);
        notifyFMEffectListChange();
    }

    public void updDABLogoList(List<DABLogo> updateList){
        Log.d(TAG,"updDABLogoList:"+updateList);

        Stream<DABLogo> stream1 = updateList.stream();
        List<DABLogo> tempList = new ArrayList<>(dabLogoList);
        Stream<DABLogo> stream2 = tempList.stream();
        dabLogoList.clear();
        dabLogoList.addAll(Stream.concat(stream1, stream2).
                distinct().collect(Collectors.toList()));
        notifyDABEffectListChange();
        //数据库耗时操作，要放到子线程来处理
        new Thread(){
            @Override
            public void run() {
                RadioListSaveUtils.getInstance().updateDABLogoList(dabLogoList);
            }
        }.start();
    }

    /**
     * 底层回调后将SliderShow放入内存，有的话一直显示不置空
     * @param dabMessage
     */
    public void updateDABSliderShow(DABMessage dabMessage){
        Log.d(TAG,"updSliderShowList:" + dabMessage);
        if(dabMessage.getSlsLen() == 0 || dabMessage.getSlsDataList() == null || dabMessage.getSlsDataList().length == 0){
            return;
        }
        boolean hasThisDabData = false;
        for(int i = 0; i < dabSliderShowList.size(); i++){
            if(dabSliderShowList.get(i).getFrequency() == dabMessage.getFrequency()
                    && dabSliderShowList.get(i).getServiceId() == dabMessage.getServiceId()
                    &&dabSliderShowList.get(i).getServiceComponentId() == dabMessage.getServiceComponentId()) {
                dabSliderShowList.get(i).setSlsLen(dabMessage.getSlsLen());
                dabSliderShowList.get(i).setSlsDataList(dabMessage.getSlsDataList());
                hasThisDabData = true;
                break;
            }
        }
        if (!hasThisDabData) {
            DABSliderShow dabSliderShow = new DABSliderShow();
            dabSliderShow.setFrequency(dabMessage.getFrequency());
            dabSliderShow.setServiceId(dabMessage.getServiceId());
            dabSliderShow.setServiceComponentId(dabMessage.getServiceComponentId());
            dabSliderShow.setSlsLen(dabMessage.getSlsLen());
            dabSliderShow.setSlsDataList(dabMessage.getSlsDataList());
            dabSliderShowList.add(dabSliderShow);
        }
    }

    public DABSliderShow getDabSlideShow(DABMessage dabMessage) {
        if (dabSliderShowList.isEmpty()) return null;
        int index = -1;
        for(int i = 0;i < dabSliderShowList.size(); i++){
            if(dabSliderShowList.get(i).getFrequency() == dabMessage.getFrequency()
                    && dabSliderShowList.get(i).getServiceId() == dabMessage.getServiceId()
                    &&dabSliderShowList.get(i).getServiceComponentId() == dabMessage.getServiceComponentId()) {
                index = i;
                break;
            }
        }
        return index == -1 ? null : dabSliderShowList.get(index);
    }

}
