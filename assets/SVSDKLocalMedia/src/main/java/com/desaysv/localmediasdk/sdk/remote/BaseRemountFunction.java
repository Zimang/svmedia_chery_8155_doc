package com.desaysv.localmediasdk.sdk.remote;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.localmediasdk.bean.LocalMediaConstants;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.localmediasdk.listener.IRemoteMediaInfoCallBack;
import com.desaysv.localmediasdk.listener.IServiceConnectCallback;
import com.desaysv.localmediasdk.remote.BaseRemoteManager;
import com.desaysv.localmediasdk.sdk.listener.IMediaFunction;
import com.desaysv.localmediasdk.sdk.listener.IMediaInfoCallBack;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2020-7-15
 * Comment 远程控制器的基础类
 */
public abstract class BaseRemountFunction implements IMediaFunction {

    private final String TAG = this.getClass().getSimpleName();


    private List<IMediaInfoCallBack> mMediaInfoCallBacks = new ArrayList<>();

    abstract void initialize(Context context, IServiceConnectCallback serviceConnectCallback);

    /**
     * 获取远程的控制器
     *
     * @return RemoteManager 媒体的远程控制器
     */
    abstract BaseRemoteManager getRemoteManager();

    /**
     * 打开对于的媒体源
     *
     * @param context        上下文
     * @param source         对应的音源
     * @param isForeground   是否是前后台
     * @param isRequestFocus 是否请求音频焦点
     * @param openReason     启动的原因
     */
    abstract void openRemoteSource(Context context, String source, boolean isForeground, boolean isRequestFocus, String openReason);

    /**
     * 获取对应的设备音源，设置进入音源统筹类
     *
     * @return 当前对象对应的来源
     */
    abstract String getOrigin();


    /**
     * 当前对应的音源是否是有效的，如果无效的话，就不用绑定服务，也不用启动了
     *
     * @return true 有效； false 无效
     */
    abstract boolean isEffectSource();

    /**
     * 初始化逻辑，初始化的时候，需要将远程的媒体控制器设置进去
     *
     * @param context 上下文
     */
    @Override
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        if (!isEffectSource()) {
            Log.w(TAG, "initialize: source is no effect");
            return;
        }
        initialize(context, serviceConnectCallback);
        MediaRemoteControl.getInstance().injectControl(getOrigin(), this);
    }

    /**
     * 进行播放
     */
    @Override
    public void play(String source) {
        Log.d(TAG, "play: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.PLAY, "");
        }
    }

    /**
     * 进行暂停
     */
    @Override
    public void pause(String source) {
        Log.d(TAG, "pause: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.PAUSE, "");
        }
    }

    /**
     * 进行播放或者暂停
     */
    @Override
    public void playOrPause(String source) {
        Log.d(TAG, "playOrPause: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.PLAY_OR_PAUSE, "");
        }
    }

    /**
     * 进行下一曲
     */
    @Override
    public void next(String source) {
        Log.d(TAG, "next: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.NEXT, "");
        }
    }

    /**
     * 进行上一曲
     */
    @Override
    public void pre(String source) {
        Log.d(TAG, "pre: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.PRE, "");
        }
    }

    @Override
    public void seekTo(String source, int time) {
        Log.d(TAG, "seekTo: source = " + source + " time = " + time);
        if (getRemoteManager().getInitSuccess()) {
            Gson gson = new Gson();
            MediaInfoBean.Builder info = new MediaInfoBean.Builder().setSource(source).setSeekToTime(time);
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.SEEK_TO, gson.toJson(info));
        }
    }

    /**
     * 进行快进
     */
    @Override
    public void startFastForward(String source) {
        Log.d(TAG, "startFastForward: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.START_FAST_FORWARD, "");
        }
    }

    /**
     * 停止快进
     */
    @Override
    public void stopFastForward(String source) {
        Log.d(TAG, "stopFastForward: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.STOP_FAST_FORWARD, "");
        }
    }

    /**
     * 开始快退
     */
    @Override
    public void startRewind(String source) {
        Log.d(TAG, "startRewind: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.START_REWIND, "");
        }
    }

    /**
     * 停止快退
     */
    @Override
    public void stopRewind(String source) {
        Log.d(TAG, "stopRewind: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.STOP_REWIND, "");
        }
    }

    /**
     * 收藏逻辑，专门给收音使用的
     *
     * @param source 控制的音源
     */
    @Override
    public void collect(String source) {
        Log.d(TAG, "collect: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.CHANGE_COLLECT, "");
        }
    }

    /**
     * 切换循环模式，专门给USB音乐使用的
     *
     * @param source   控制的音源
     * @param typeInfo 切换的类型
     */
    @Override
    public void changeLoopType(String source, String typeInfo) {
        Log.d(TAG, "collect: source = " + source + " typeInfo = " + typeInfo);
        if (getRemoteManager().getInitSuccess()) {
            MediaInfoBean created = new MediaInfoBean.Builder().setSource(source).setLoopType(typeInfo).created();
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.CHANGE_LOOP_TYPE, new Gson().toJson(created));
        }
    }

    /**
     * 根据选择的音源获取该音源的ID3信息
     *
     * @return ID3信息的json数据
     */
    @Override
    public String getCurrentPlayInfo(String source) {
        Log.d(TAG, "getCurrentPlayInfo: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String playInfo = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.PLAY_INFO);
            Log.d(TAG, "getCurrentPlayInfo: playInfo = " + playInfo);
            return playInfo;
        }
        return null;
    }

    /**
     * 根据选择的音源获取该源的播放状态
     *
     * @return 播放状态的json数据
     */
    @Override
    public String getCurrentPlayStatus(String source) {
        Log.d(TAG, "getCurrentPlayStatus: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String playStatus = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.PLAY_STATUS);
            Log.d(TAG, "getCurrentPlayStatus: playStatus = " + playStatus);
            return playStatus;
        }
        return null;
    }

    /**
     * 根据选择的音源获取当前的播放时间
     *
     * @return 播放时间的json数据
     */
    @Override
    public String getCurrentPlayTime(String source) {
        Log.d(TAG, "getCurrentPlayTime: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String playTime = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.PLAY_TIME);
            Log.d(TAG, "getCurrentPlayTime: playTime = " + playTime);
            return playTime;
        }
        return null;
    }

    @Override
    public String getDeviceConnectState(String source) {
        Log.d(TAG, "getDeviceConnectState: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String connectState = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.DEVICE_STATUS);
            Log.d(TAG, "getDeviceConnectState: connectState = " + connectState);
            return connectState;
        }
        return null;
    }

    /**
     * 获取专辑的数据
     *
     * @return 专辑数据
     */
    @Nullable
    @Override
    public byte[] getPicData(String source) {
        Log.d(TAG, "getPicData: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            return getRemoteManager().getPicData(source);
        }
        return null;
    }

    /**
     * 获取收藏状态，只针对于收音
     *
     * @param source 需要获取的音源
     * @return 收音的收藏状态
     */
    @Override
    public String getCollectStatus(String source) {
        Log.d(TAG, "getCollectStatus: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String collectStatus = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.COLLECT_STATUS);
            Log.d(TAG, "getCollectStatus: collectStatus = " + collectStatus);
            return collectStatus;
        }
        return null;
    }

    /**
     * 获取循环模式，只针对于USB音乐
     *
     * @param source 需要获取的音源
     * @return USB音乐的循环状态
     */
    @Override
    public String getLoopType(String source) {
        Log.d(TAG, "getLoopType: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String loopType = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.LOOP_TYPE);
            Log.d(TAG, "getLoopType: loopType = " + loopType);
            return loopType;
        }
        return null;
    }

    /**
     * 启动对应的音源
     *
     * @param context      上下文
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     * @param openReason   启动的原因
     */
    @Override
    public void openSource(Context context, String source, boolean isForeground, String openReason) {
        openRemoteSource(context, source, isForeground, true, openReason);
    }


    @Override
    public void openSourceWithFlag(String source, boolean isForeground, String openReason, int flag) {
        if (getRemoteManager().getInitSuccess()) {
            Gson gson = new Gson();
            String data = gson.toJson(new MediaInfoBean.Builder().setSource(source).setForeground(isForeground).setOpenReason(openReason).setFlag(flag).created());
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.OPEN_SOURCE, data);
        }
    }

    @Override
    public void setBand(String source, String band) {
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.SET_BAND, band);
        }
    }

    @Override
    public void setMedia(String source, MediaInfoBean mediaInfoBean) {
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.SET_MEDIA, new Gson().toJson(mediaInfoBean));
        }
    }

    @Override
    public void startAst(String source) {
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.START_AST, "");
        }
    }

    @Override
    public void stopAst(String source) {
        if (getRemoteManager().getInitSuccess()) {
            getRemoteManager().sendCommand(source, LocalMediaConstants.ControlAction.STOP_AST, "");
        }
    }


    @Override
    public void setMediaSettings(String source, String type, int value) {

    }

    @Nullable
    @Override
    public String getCurrentSearchStatus(String source) {
        Log.d(TAG, "getCurrentSearchStatus" +
                "    public String getCurrentSearchStatus: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String searchStatus = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.SEARCH_STATUS);
            Log.d(TAG, "getCurrentSearchStatus" +
                    "    public String getCurrentSearchStatus: searchStatus = " + searchStatus);
            return searchStatus;
        }
        return null;
    }

    @Nullable
    @Override
    public String getRadioFreq(String source) {
        Log.d(TAG, "getRadioFreq: source = " + source);
        if (getRemoteManager().getInitSuccess()) {
            String radioFreq = getRemoteManager().getSelectInfo(source, LocalMediaConstants.StatusAction.RADIO_FREQ);
            Log.d(TAG, "getRadioFreq: radioFreq = " + radioFreq);
            return radioFreq;
        }
        return null;
    }

    @Nullable
    @Override
    public String getRadioStatus(String source) {
        Log.d(TAG, "getRadioStatus: source = " + source);
        //todo
        return null;
    }

    @Override
    public RemoteBeanList getRemoteList(String source, String type) {
        Log.d(TAG, "getRemoteList: source = " + source + ", type = " + type);
        if (getRemoteManager().getInitSuccess()) {
            RemoteBeanList remoteBeanList = getRemoteManager().getRemoteList(source, type);
            Log.d(TAG, "getRemoteList: remoteBeanList = " + remoteBeanList);
            return remoteBeanList;
        }
        return null;
    }

    /**
     * 注册媒体变化的回调广播
     *
     * @param mediaInfoCallBack 数据变化的回调
     */
    @Override
    public void registerMediaInfoCallback(IMediaInfoCallBack mediaInfoCallBack) {
        if (mediaInfoCallBack != null) {
            mMediaInfoCallBacks.remove(mediaInfoCallBack);
            mMediaInfoCallBacks.add(mediaInfoCallBack);
        }
        registerRemoteCallBack();
    }

    /**
     * 注销媒体变化的回调广播
     */
    @Override
    public void unRegisterMediaInfoCallback(IMediaInfoCallBack mediaInfoCallBack) {
        if (mediaInfoCallBack != null) {
            mMediaInfoCallBacks.remove(mediaInfoCallBack);
        }
        if (mMediaInfoCallBacks.isEmpty()) {
            unRegisterRemoteCallBack();
        }
    }

    /**
     * 是否是已经注册远程的回调
     */
    private boolean isRegisterRemoteCallBack = false;


    /**
     * 回调是否是已经连接了的
     */
    private boolean isServiceConnect = false;

    /**
     * 注册远程的回调
     * 服务的注册需要3个条件成立
     * 1. 服务绑定成功
     * 2. 注册的callback数量大于1
     * 3. 第一次注册AIDL回调
     * <p>
     * 调用的时机有两个
     * 1. 服务端连接成功的时候
     * 2. 外面注册回调的时候，如果回调列表不为空
     */
    private void registerRemoteCallBack() {
        boolean isEmpty = mMediaInfoCallBacks.isEmpty();
        Log.d(TAG, "registerRemoteCallBack: isEmpty = " + isEmpty + " isServiceConnect = " + isServiceConnect + " isRegisterRemoteCallBack = " + isRegisterRemoteCallBack);
        if (!isEmpty && isServiceConnect && !isRegisterRemoteCallBack) {
            getRemoteManager().registerMediaInfoCallback(remoteMediaInfoCallBack);
            //回调注册过了，就需要设置为true
            isRegisterRemoteCallBack = true;
        }
    }

    /**
     * 注销远程的回调
     * 直接注销就行了,注销的时机有两个
     * 1. 服务断开的时候注销
     * 2. 媒体回调为空的时候，需要注销
     */
    private void unRegisterRemoteCallBack() {
        Log.d(TAG, "unRegisterRemoteCallBack: ");
        getRemoteManager().unRegisterMediaInfoCallback(remoteMediaInfoCallBack);
        isRegisterRemoteCallBack = false;
    }

    /**
     * 服务绑定回调
     */
    private IServiceConnectCallback serviceConnectCallback = new IServiceConnectCallback() {
        @Override
        public void onServiceConnect() {
            Log.d(TAG, "onServiceConnect: ");
            isServiceConnect = true;
            for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                mediaInfoCallBack.onServiceConnect(getOrigin());
            }
            registerRemoteCallBack();
        }

        @Override
        public void onServiceDisConnect() {
            Log.d(TAG, "onServiceDisConnect: ");
            isServiceConnect = false;
            for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                mediaInfoCallBack.onServiceDisConnect(getOrigin());
            }
            unRegisterRemoteCallBack();
        }
    };


    /**
     * 来着收音SDK的数据变化回调
     */
    private IRemoteMediaInfoCallBack remoteMediaInfoCallBack = new IRemoteMediaInfoCallBack() {

        /**
         * 媒体数据发生改变，会触发这个回调
         *
         * @param source    数据变化是那个音源
         * @param InfoType  变化的数据是那种类型的数据
         * @param info 数据本体
         */
        @Override
        public void onMediaInfoChange(String source, String InfoType, String info) {
            Log.d(TAG, "onMediaInfoChange: source = " + source + " InfoType = " + InfoType + " info = " + info);
            Log.d(TAG, "onMediaInfoChange: mMediaInfoCallBacks = " + mMediaInfoCallBacks.size());
            switch (InfoType) {
                case LocalMediaConstants.StatusAction.PLAY_STATUS:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaPlayStatusChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.PLAY_INFO:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaInfoChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.PLAY_TIME:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaPlayTimeChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.LOOP_TYPE:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMusicLoopTypeChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.COLLECT_STATUS:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onRadioCollectStatusChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.SEARCH_STATUS:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaSearchStatusChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.SEEK_STATUS:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaSeekStatusChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.SETTINGS_STATUS:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaSettingsChange(source, info);
                    }
                    break;
                case LocalMediaConstants.StatusAction.DEVICE_STATUS:
                    for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                        mediaInfoCallBack.onMediaDeviceStatusChange(source, info);
                    }
                    break;
            }
        }

        /**
         * 媒体的专辑图片发生改把的时候，会触发的数据变化
         *
         * @param source  对应的音源
         * @param picInfo 图片数据的json
         */
        @Override
        public void onMediaPicChange(String source, byte[] picInfo) {
            Log.d(TAG, "onMediaPicChange: source = " + source);
            for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                mediaInfoCallBack.onMediaPicChange(source, picInfo);
            }
        }

        @Override
        public void onRemoteListChanged(String source, String type, RemoteBeanList remoteList) {
            Log.d(TAG, "onRemoteListChanged: source = " + source + ", type = " + type);
            remoteList.setSource(source);
            remoteList.setType(type);
            for (IMediaInfoCallBack mediaInfoCallBack : mMediaInfoCallBacks) {
                mediaInfoCallBack.onRemoteListChanged(source, type, remoteList);
            }
        }
    };
}
