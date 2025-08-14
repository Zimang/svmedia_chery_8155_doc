package com.desaysv.moduleradio.utils;

import android.hardware.radio.RadioManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.localmediasdk.IAIDLMediaInfoCallback;
import com.desaysv.localmediasdk.bean.LocalMediaConstants;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.bean.RemoteBean;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.svlibmediaobserver.manager.MediaObserverManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by LZM on 2020-3-24
 * Comment 收音的媒体状态回调
 *
 * @author uidp5370
 */
public class RadioStatusUtils {

    private static final String TAG = "RadioStatusUtils";

    private static RadioStatusUtils instance;

    public static RadioStatusUtils getInstance() {
        if (instance == null) {
            synchronized (RadioStatusUtils.class) {
                if (instance == null) {
                    instance = new RadioStatusUtils();
                }
            }
        }
        return instance;
    }

    private RadioStatusUtils() {

    }

    /**
     * 初始化，并且注册数据变化回调
     */
    public void initialize() {
        Log.d(TAG, "initialize: ");
        ModuleRadioTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(mRadioStatusChange);
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
    }


    /**
     * 获取当前的播放信息,
     *
     * @return 当前播放信息的json数据
     */
    public String getCurrentPlayInfo(String source) {
        //TODO：这里可以根据项目的需求，判断当前需要返回怎么样的数据，现在是将收音和音乐区分开了两个服务
        Log.d(TAG, "getCurrentPlayInfo: source = " + source);
        RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
                radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage();
                break;
            case DsvAudioSDKConstants.AM_SOURCE:
                radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage();
                break;
            case DsvAudioSDKConstants.DAB_SOURCE:
                radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage();
                break;
        }
        return changeRadioMessageToJson(source, radioMessage);
    }

    /**
     * 获取当前的播放状态
     *
     * @return 当前播放状态的json数据
     */
    public String getCurrentPlayStatus(String source) {
        Log.d(TAG, "getCurrentPlayStatus: source = " + source);
        boolean isRadioPlay = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying();
        return changePlayStatusToJson(source, isRadioPlay);
    }


    /**
     * 获取当前的搜索状态
     *
     * @return 当前播放状态的json数据
     */
    public String getCurrentSearchStatus(String source) {
        Log.d(TAG, "getCurrentSearchStatus: source = " + source);
        boolean isRadioSearch = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSearching();
        return changeSearchStatusToJson(source, isRadioSearch);
    }


    /**
     * 获取当前源的频点信息
     *
     * @return 当前播放状态的json数据
     */
    public String getRadioFreq(String source){
        Log.d(TAG, "getRadioFreq: source = " + source);
        String freq = "";
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
                freq = String.valueOf(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage().getRadioFrequency());
                break;
            case DsvAudioSDKConstants.AM_SOURCE:
                freq = String.valueOf(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage().getRadioFrequency());
                break;
            case DsvAudioSDKConstants.DAB_SOURCE:
                freq = String.valueOf(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage().getDabMessage().getShortProgramStationName());
                break;
        }
        return changeRadioMessageToJson(source, freq);
    }


    /**
     * 根据选择的音源获取该源的RDS/DAB设置状态
     *
     * @return 当前播放状态的json数据
     */
    public String getRadioStatus(String source){
        Log.d(TAG, "getRadioStatus: source = " + source);
        //预留接口，todo
        String freq = "";
        return changeRadioMessageToJson(source, freq);
    }

    /**
     * 根据音源和类型返回对应列表
     * @param source
     * @param type
     * @return
     */
    public RemoteBeanList getRemoteList(String source, String type){
        Log.d(TAG, "getRemoteList: source = " + source + ", type = " + type);
        RemoteBeanList remoteBeanList = new RemoteBeanList();
        remoteBeanList.setSource(source);
        remoteBeanList.setType(type);
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
                switch (type){
                    case LocalMediaConstants.ListType.LIST_EFFECT:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getFMEffectRadioMessageList()));
                        break;
                    case LocalMediaConstants.ListType.LIST_COLLECT:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getFMCollectRadioMessageList()));
                        break;
                    case LocalMediaConstants.ListType.LIST_ALL:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getFMAllRadioMessageList()));
                        break;
                }
                break;
            case DsvAudioSDKConstants.AM_SOURCE:
                switch (type){
                    case LocalMediaConstants.ListType.LIST_EFFECT:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getAMEffectRadioMessageList()));
                        break;
                    case LocalMediaConstants.ListType.LIST_COLLECT:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getAMCollectRadioMessageList()));
                        break;
                    case LocalMediaConstants.ListType.LIST_ALL:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getAMAllRadioMessageList()));
                        break;
                }
                break;
            case DsvAudioSDKConstants.DAB_SOURCE:
                switch (type){
                    case LocalMediaConstants.ListType.LIST_EFFECT:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getDABEffectRadioMessageList()));
                        break;
                    case LocalMediaConstants.ListType.LIST_COLLECT:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getDABCollectRadioMessageList()));
                        break;
                    case LocalMediaConstants.ListType.LIST_ALL:
                        remoteBeanList.setRemoteBeanList(changeRadioListToRemoteBeanList(RadioList.getInstance().getDABAllRadioMessageList()));
                        break;
                }
                break;
        }
        return remoteBeanList;
    }

    /**
     * 将RadioList列表转成 RemoteBeanList对象
     * @param radioMessageList
     * @return
     */
    private RemoteBeanList changeRadioListToRemoteList(List<RadioMessage> radioMessageList){
        Log.d(TAG,"changeRadioListToRemoteList,radioMessageList:"+radioMessageList);
        RemoteBeanList remoteList = new RemoteBeanList();
        if (radioMessageList == null || radioMessageList.size() < 1){
            return  remoteList;
        }
        List<RemoteBean> remoteBeanList = new ArrayList<>();
        List<RadioMessage> tempList = new ArrayList<>();//用来避免RadioList获取到的列表出现变化导致的ConstuctorException问题
        tempList.addAll(radioMessageList);
        for (RadioMessage radioMessage : tempList){
            remoteBeanList.add(changeRadioMessageToRemoteBean(radioMessage));
        }
        remoteList.setRemoteBeanList(remoteBeanList);
        return remoteList;
    }


    /**
     * 将RadioList列表转成 RemoteBean的列表
     * @param radioMessageList
     * @return
     */
    private List<RemoteBean> changeRadioListToRemoteBeanList(List<RadioMessage> radioMessageList){
        Log.d(TAG,"changeRadioListToRemoteBeanList,radioMessageList:"+radioMessageList);
        synchronized (this) {
            if (radioMessageList == null) {
                return null;
            }
            List<RemoteBean> remoteBeanList = new ArrayList<>();
            List<RadioMessage> tempList = new ArrayList<>();//用来避免RadioList获取到的列表出现变化导致的ConstuctorException问题
            tempList.addAll(radioMessageList);
            for (RadioMessage radioMessage : tempList) {
                remoteBeanList.add(changeRadioMessageToRemoteBean(radioMessage));
            }
            return remoteBeanList;
        }
    }

    /**
     * 将RadioMessage转成 RemoteBean的数据结构
     * @param radioMessage
     * @return
     */
    private RemoteBean changeRadioMessageToRemoteBean(RadioMessage radioMessage ){
        synchronized (this) {
            RemoteBean remoteBean = new RemoteBean();

            remoteBean.setCollectStatus(radioMessage.isCollect());
            remoteBean.setFrequency(String.valueOf(radioMessage.getRadioFrequency()));
            if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
                remoteBean.setTitle(radioMessage.getDabMessage().getShortProgramStationName());
                remoteBean.setComponentId(String.valueOf(radioMessage.getDabMessage().getServiceComponentId()));
                remoteBean.setServiceId(String.valueOf(radioMessage.getDabMessage().getServiceId()));
                remoteBean.setEnsembleLabel(radioMessage.getDabMessage().getShortEnsembleLabel());
            }
            //todo 如果需要其它信息，可在此处添加
            return remoteBean;
        }
    }

    /**
     * 获取当前播放电台的收藏状态
     *
     * @return 收藏状态的json数据
     */
    public String getCurrentRadioMessageCollectStatus(String source) {
        boolean collectStatus = false;
        switch (source) {
            case DsvAudioSDKConstants.FM_SOURCE:
                collectStatus = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage().isCollect();
                break;
            case DsvAudioSDKConstants.AM_SOURCE:
                collectStatus = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage().isCollect();
                break;
            case DsvAudioSDKConstants.DAB_SOURCE:
                collectStatus = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getDABRadioMessage().isCollect();
                break;
            default:
                collectStatus = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage().isCollect();
        }
        Log.d(TAG, "getCurrentRadioMessageCollectStatus: source = " + source + " collectStatus = " + collectStatus);
        return changeCollectStatusToJson(source, collectStatus);
    }


    /**
     * AIDL的回调列表
     */
    private final RemoteCallbackList<IAIDLMediaInfoCallback> mCallbackList = new RemoteCallbackList<>();

    /**
     * 设置收音数据的变化回调
     *
     * @param packageName           包名
     * @param aidlMediaInfoCallback 回调
     */
    public void setRadioCallback(String packageName, IAIDLMediaInfoCallback aidlMediaInfoCallback) {
        Log.d(TAG, "setRadioCallback: packageName = " + packageName);
        mCallbackList.register(aidlMediaInfoCallback);
    }

    /**
     * 清除收音数据的变化回调
     *
     * @param packageName 包名
     */
    public void removeRadioCallback(String packageName, IAIDLMediaInfoCallback aidlMediaInfoCallback) {
        Log.d(TAG, "removeRadioCallback: packageName = " + packageName);
        mCallbackList.unregister(aidlMediaInfoCallback);
    }

    /**
     * 收音类型的回调
     */
    private IRadioStatusChange mRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSearching() || ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSeeking()){
                return;
            }
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            Log.d(TAG, "onCurrentRadioMessageChange: radioMessage = " + radioMessage);

            MediaObserverManager.getInstance().setMediaInfo(radioMessage.getRadioMessageSource(),changeRadioMessageToAppMedia(radioMessage.getRadioMessageSource(),radioMessage),false);

            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onCurrentRadioMessageChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.PLAY_INFO,
                                changeRadioMessageToJson(radioMessage.getRadioMessageSource(), radioMessage));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
            //因为Logo使用机制变更，需要同步更新一下logo
            onDABLogoChanged(new byte[]{});
        }


        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            boolean isRadioPlay = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying();
            if (isRadioPlay){
                MediaObserverManager.getInstance().setMediaInfo(radioMessage.getRadioMessageSource(),changeRadioMessageToAppMedia(radioMessage.getRadioMessageSource(),radioMessage),false);
            } else {
                boolean hasAudioFocus = RadioControlTool.getInstance().checkCurrentRadioSource();
                //焦点丢失时要清空缓存logo数据，不然从其他音源切过来的情况下，会导致mini播放器不更新logo
                if (!hasAudioFocus) {
                    tempLogoByte = null;
                }
            }
            MediaObserverManager.getInstance().setPlayStatus(radioMessage.getRadioMessageSource(),isRadioPlay);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onPlayStatusChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.PLAY_STATUS,
                                changePlayStatusToJson(radioMessage.getRadioMessageSource(), isRadioPlay));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onAstListChanged(int band) {

        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onSearchStatusChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.SEARCH_STATUS,
                                changeSearchStatusToJson(radioMessage.getRadioMessageSource(), isSearching));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onSeekStatusChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.SEEK_STATUS,
                                changeSearchStatusToJson(radioMessage.getRadioMessageSource(), isSeeking));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onRDSFlagInfoChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.SETTINGS_STATUS,
                                changeRDSToJson(radioMessage.getRadioMessageSource(), info));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }


        private byte[] tempLogoByte;

        @Override
        public void onDABLogoChanged(byte[] logoByte) {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (radioMessage.getDabMessage() == null){
                tempLogoByte = null;
                return;
            }
            //优先使用Sls
            logoByte = radioMessage.getDabMessage().getSlsDataList();
            //次级使用存储的Logo
            if (logoByte == null){
                logoByte = ListUtils.getOppositeDABLogo(radioMessage);
            }
            //最后使用当前获取到的Logo
            if (logoByte == null){
                logoByte = radioMessage.getDabMessage().getLogoDataList();
            }
            if (tempLogoByte != null && Arrays.equals(tempLogoByte, logoByte)){
                Log.d(TAG,"updateCurrentRadio，currentLogo is same");
                return;
            }
            tempLogoByte = logoByte;
            MediaObserverManager.getInstance().setAlbum(radioMessage.getRadioMessageSource(),logoByte);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onDABLogoChanged: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaPicChange(radioMessage.getRadioMessageSource(), logoByte);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }
    };



    /**
     * 收音列表变化时会触发的回调，收藏状态的变化反馈的是列表的变化，所以通过列表的变化来监听收藏状态的变化
     */
    IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange: ");
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (radioMessage == null){
                return;
            }
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onFMCollectListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.COLLECT_STATUS,
                                changeCollectStatusToJson(radioMessage));

                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.ListType.LIST_COLLECT,
                                changeRadioListToRemoteList(RadioList.getInstance().getFMCollectRadioMessageList()));

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange: ");
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (radioMessage == null){
                return;
            }
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onAMCollectListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.StatusAction.COLLECT_STATUS,
                                changeCollectStatusToJson(radioMessage));

                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(radioMessage.getRadioMessageSource(),
                                LocalMediaConstants.ListType.LIST_COLLECT,
                                changeRadioListToRemoteList(RadioList.getInstance().getAMCollectRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onDABCollectListChange() {
            RadioMessage radioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
            if (radioMessage == null){
                return;
            }
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onDABCollectListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.DAB_SOURCE,
                                LocalMediaConstants.StatusAction.COLLECT_STATUS,
                                changeCollectStatusToJson(radioMessage));

                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.DAB_SOURCE,
                                LocalMediaConstants.ListType.LIST_COLLECT,
                                changeRadioListToRemoteList(RadioList.getInstance().getDABCollectRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onFMEffectListChange() {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onFMEffectListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.FM_SOURCE,
                                LocalMediaConstants.ListType.LIST_EFFECT,
                                changeRadioListToRemoteList(RadioList.getInstance().getFMEffectRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onAMEffectListChange() {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onAMEffectListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.AM_SOURCE,
                                LocalMediaConstants.ListType.LIST_EFFECT,
                                changeRadioListToRemoteList(RadioList.getInstance().getAMEffectRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onDABEffectListChange() {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onDABEffectListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.DAB_SOURCE,
                                LocalMediaConstants.ListType.LIST_EFFECT,
                                changeRadioListToRemoteList(RadioList.getInstance().getDABEffectRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onFMAllListChange() {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onFMAllListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.FM_SOURCE,
                                LocalMediaConstants.ListType.LIST_ALL,
                                changeRadioListToRemoteList(RadioList.getInstance().getFMAllRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onAMAllListChange() {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onAMAllListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.AM_SOURCE,
                                LocalMediaConstants.ListType.LIST_ALL,
                                changeRadioListToRemoteList(RadioList.getInstance().getAMAllRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onDABAllListChange() {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "onDABAllListChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onRemoteListChanged(DsvAudioSDKConstants.DAB_SOURCE,
                                LocalMediaConstants.ListType.LIST_ALL,
                                changeRadioListToRemoteList(RadioList.getInstance().getDABAllRadioMessageList()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }

        }
    };

    /**
     * 将收音数据转化为json数据
     *
     * @param currentSource 当前的媒体源
     * @param radioMessage  收音数据
     * @return json数据
     */
    private String changeRadioMessageToJson(String currentSource, RadioMessage radioMessage) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource);
        if (radioMessage != null) {
            if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
                String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
                if (radioName != null && radioName.trim().length() > 1){
                    builder.setTitle(radioName);
                }else {
                    builder.setTitle("");
                }
                builder.setFrequency(String.valueOf(radioMessage.getRadioFrequency()));
                builder.setBand(radioMessage.getRadioBand());
            }else {
                builder.setTitle(radioMessage.getDabMessage().getShortProgramStationName());
                builder.setBand(Constant.RadioBand.BAND_DAB);
                builder.setFrequency("-1");
            }
            builder.setCollectStatus(radioMessage.isCollect());
        }
        MediaInfoBean mediaInfoBean = builder.created();

        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeRadioMessageToJson: data = " + data);
        return data;
    }


    /**
     * 将当前的播放状态设置为json数据
     *
     * @param currentSource 当前的音源
     * @param playStatus    当前的播放状态
     * @return json数据
     */
    private String changePlayStatusToJson(String currentSource, boolean playStatus) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource).setPlayStatus(playStatus);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changePlayStatusToJson: data = " + data);
        return data;
    }

    /**
     * 将当前电台的收藏状态设置为json数据
     * @return json数据
     */
    private String changeCollectStatusToJson(RadioMessage radioMessage) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(radioMessage.getRadioMessageSource()).setCollectStatus(radioMessage.isCollect());
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
            if (radioName != null && radioName.trim().length() > 1){
                builder.setTitle(radioName);
            }else {
                builder.setTitle("");
            }
            builder.setFrequency(String.valueOf(radioMessage.getRadioFrequency()));
            builder.setBand(radioMessage.getRadioBand());
        }else {
            builder.setTitle(radioMessage.getDabMessage().getShortProgramStationName());
            builder.setBand(Constant.RadioBand.BAND_DAB);
        }
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeCollectStatusToJson: data = " + data);
        return data;
    }



    /**
     * 将当前电台的收藏状态设置为json数据
     *
     * @param source        选择的音源
     * @param collectStatus 收藏状态
     * @return json数据
     */
    private String changeCollectStatusToJson(String source, boolean collectStatus) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(source).setCollectStatus(collectStatus);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeCollectStatusToJson: data = " + data);
        return data;
    }

    /**
     * 将当前音源转化为json
     *
     * @param source 当前音源
     * @return json数据
     */
    private String changeSourceToJson(String source) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(source);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeSourceToJson: data = " + data);
        return data;
    }

    /**
     * 将当前的搜索状态设置为json数据
     *
     * @param currentSource 当前的音源
     * @param searchStatus    当前的搜索状态
     * @return json数据
     */
    private String changeSearchStatusToJson(String currentSource, boolean searchStatus) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource).setSearchStatus(searchStatus);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeSearchStatusToJson: data = " + data);
        return data;
    }


    /**
     * 将收音数据转化为json数据
     *
     * @param currentSource 当前的媒体源
     * @param freq  收音数据频点值
     * @return json数据
     */
    private String changeRadioMessageToJson(String currentSource, String freq) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource);
        if (freq != null) {
            builder.setFrequency(freq);
        }
        MediaInfoBean mediaInfoBean = builder.created();

        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeRadioMessageToJson: data = " + data);
        return data;
    }


    /**
     * 将收音设置转化为json数据
     *
     * @param currentSource 当前的媒体源
     * @param rdsInfo  rds数据
     * @return json数据
     */
    private String changeRDSToJson(String currentSource, RDSFlagInfo rdsInfo) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource);
        if (rdsInfo != null) {
           //todo 暂时不清楚需要什么信息
        }
        MediaInfoBean mediaInfoBean = builder.created();

        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeRDSToJson: data = " + data);
        return data;
    }



    private com.desaysv.svlibmediaobserver.bean.MediaInfoBean changeRadioMessageToAppMedia(String currentSource, RadioMessage radioMessage) {
        com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder appBuilder = new com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder();
        appBuilder.setSource(currentSource);
        appBuilder.setPlaying(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying());
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
            appBuilder.setTitle(radioMessage.getDabMessage().getShortProgramStationName());
            appBuilder.setBytes(radioMessage.getDabMessage().getLogoDataList());
            appBuilder.setFreq(String.valueOf(radioMessage.getDabMessage().getFrequency()));
            appBuilder.setServiceID(radioMessage.getDabMessage().getServiceId());
            appBuilder.setServiceComID(radioMessage.getDabMessage().getServiceComponentId());
        }else {
            if (radioMessage.getRadioBand() == RadioManager.BAND_AM) {
                appBuilder.setTitle(AppBase.mContext.getResources().getString(R.string.radio_am)+" "+radioMessage.getCalculateFrequencyAndUnit());
            } else if (radioMessage.getRadioBand() == RadioManager.BAND_FM) {
                String rdsName = RadioCovertUtils.getOppositeRDSName(radioMessage);
                if (rdsName != null && rdsName.length() > 0) {
                    appBuilder.setTitle(rdsName);
                } else {
                    appBuilder.setTitle(AppBase.mContext.getResources().getString(R.string.radio_fm) + " " + radioMessage.getCalculateFrequencyAndUnit());
                }
            }
            appBuilder.setFreq(radioMessage.getCalculateFrequencyAndUnit());
        }
        return appBuilder.Build();
    }
}
