package com.desaysv.mediasdk.manager;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;

import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.mediasdk.bean.Constant;
import com.desaysv.mediasdk.bean.MediaInfoBean;
import com.desaysv.mediasdk.listener.IMediaInfoCallBack;
import com.desaysv.mediasdk.remote.MediaRemoteControl;

/**
 * Created by LZM on 2020-5-1
 * Comment 这是一个媒体控制器，可以根据每一个音源进行控制自行控制，属于透传类型
 */
public class DsvSingleMediaManager implements ISingleMediaManager {

    private static final String TAG = "DsvSingleMediaManager";

    private static ISingleMediaManager instance;

    public static ISingleMediaManager getInstance() {
        if (instance == null) {
            synchronized (DsvSingleMediaManager.class) {
                if (instance == null) {
                    instance = new DsvSingleMediaManager();
                }
            }
        }
        return instance;
    }

    private DsvSingleMediaManager() {

    }


    /**
     * 初始化媒体外部的媒体控制器,必须使用Application的Context
     *
     * @param context 上下文
     */
    @Override
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        MediaRemoteControl.getInstance().initialize(context);
    }

    /**
     * 进行播放
     *
     * @param source 需要控制的音源
     */
    @Override
    public void play(String source) {
        Log.d(TAG, "play: source = " + source);
        MediaRemoteControl.getInstance().play(source);
    }

    /**
     * 进行暂停
     *
     * @param source 需要控制的音源
     */
    @Override
    public void pause(String source) {
        Log.d(TAG, "pause: source = " + source);
        MediaRemoteControl.getInstance().pause(source);
    }

    /**
     * 进行播放或者暂停
     *
     * @param source 需要控制的音源
     */
    @Override
    public void playOrPause(String source) {
        Log.d(TAG, "playOrPause: source = " + source);
        MediaRemoteControl.getInstance().playOrPause(source);
    }

    /**
     * 进行下一曲
     *
     * @param source 需要控制的音源
     */
    @Override
    public void next(String source) {
        Log.d(TAG, "next: source = " + source);
        MediaRemoteControl.getInstance().next(source);
    }

    /**
     * 进行上一曲
     *
     * @param source 需要控制的音源
     */
    @Override
    public void pre(String source) {
        Log.d(TAG, "pre: source = " + source);
        MediaRemoteControl.getInstance().pre(source);
    }

    /**
     * 进行快进
     *
     * @param source 需要控制的音源
     */
    @Override
    public void startFastForward(String source) {
        Log.d(TAG, "startFastForward: source = " + source);
        MediaRemoteControl.getInstance().startFastForward(source);
    }

    /**
     * 停止快进
     *
     * @param source 需要控制的音源
     */
    @Override
    public void stopFastForward(String source) {
        Log.d(TAG, "stopFastForward: source = " + source);
        MediaRemoteControl.getInstance().stopFastForward(source);
    }

    /**
     * 开始快退
     *
     * @param source 需要控制的音源
     */
    @Override
    public void startRewind(String source) {
        Log.d(TAG, "startRewind: source = " + source);
        MediaRemoteControl.getInstance().startRewind(source);
    }

    /**
     * 停止快退
     *
     * @param source 需要控制的音源
     */
    @Override
    public void stopRewind(String source) {
        Log.d(TAG, "stopRewind: source = " + source);
        MediaRemoteControl.getInstance().stopRewind(source);
    }

    /**
     * 收藏逻辑
     *
     * @param source 控制的音源
     */
    @Override
    public void collect(String source) {
        Log.d(TAG, "collect: source = " + source);
        MediaRemoteControl.getInstance().collect(source);
    }

    /**
     * 切换循环模式
     *
     * @param source 控制的音源
     */
    @Override
    public void changeLoopType(String source) {
        Log.d(TAG, "changeLoopType: source = " + source);
        MediaRemoteControl.getInstance().changeLoopType(source);
    }

    /**
     * 根据选择的音源获取该音源的ID3信息
     *
     * @param source 需要控制的音源
     * @return ID3信息的json数据
     */
    @Override
    @Nullable
    public String getCurrentPlayInfo(String source) {
        String playInfo = MediaRemoteControl.getInstance().getCurrentPlayInfo(source);
        Log.d(TAG, "getCurrentPlayInfo: source = " + source + " playInfo = " + playInfo);
        return playInfo;
    }

    /**
     * 根据选择的音源获取该源的播放状态
     *
     * @param source 需要控制的音源
     * @return 播放状态的json数据
     */
    @Override
    @Nullable
    public String getCurrentPlayStatus(String source) {
        String playStatus = MediaRemoteControl.getInstance().getCurrentPlayStatus(source);
        Log.d(TAG, "getCurrentPlayStatus: source = " + source + " playStatus = " + playStatus);
        return playStatus;
    }

    /**
     * 根据选择的音源获取当前的播放时间
     *
     * @param source 需要控制的音源
     * @return 播放时间的json数据
     */
    @Override
    @Nullable
    public String getCurrentPlayTime(String source) {
        String playTime = MediaRemoteControl.getInstance().getCurrentPlayTime(source);
        Log.d(TAG, "getCurrentPlayTime: source = " + source + " playTime = " + playTime);
        return playTime;
    }

    /**
     * 根据选择的音源获取当前的专辑封面
     *
     * @param source 需要获取的音源
     * @return 图片数据的数组
     */
    @Nullable
    @Override
    public byte[] getAlbumPicData(String source) {
        Log.d(TAG, "getAlbumPicData: source = " + source);
        return MediaRemoteControl.getInstance().getAlbumPicData(source);
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
        Log.d(TAG, "openSource: source = " + source + " isForeground = " + isForeground + " openReason = " + openReason);
        MediaRemoteControl.getInstance().openSource(context, source, isForeground, openReason);
    }

    /**
     * 获取收藏状态
     *
     * @param source 需要获取的音源
     * @return 收音的收藏状态
     */
    @Override
    public String getCollectStatus(String source) {
        String collectStatus = MediaRemoteControl.getInstance().getCollectStatus(source);
        Log.d(TAG, "getCollectStatus: source = " + source + " collectStatus = " + collectStatus);
        return collectStatus;
    }

    /**
     * 获取循环模式
     *
     * @param source 需要获取的音源
     * @return USB音乐的循环状态
     */
    @Override
    public String getLoopType(String source) {
        String loopType = MediaRemoteControl.getInstance().getLoopType(source);
        Log.d(TAG, "getCollectStatus: source = " + source + " loopType = " + loopType);
        return loopType;
    }

    /**
     * 注册媒体变化的回调广播，给需要单独使用的音源选择的权力，要那个回调，注册那个回调
     *
     * @param origin            选择的控制器
     * @param mediaInfoCallBack 数据变化的回调
     */
    @Override
    public void registerMediaInfoCallback(String origin, IMediaInfoCallBack mediaInfoCallBack) {
        Log.d(TAG, "registerMediaInfoCallback: ");
        if (mediaInfoCallBack != null) {
            MediaRemoteControl.getInstance().registerMediaInfoCallback(origin, mediaInfoCallBack);
        }
    }

    /**
     * 注销媒体变化的回调广播，给需要单独使用的音源选择的权力，要那个回调，注销那个回调
     *
     * @param origin            选择的控制器
     * @param mediaInfoCallBack 数据变化的回调
     */
    @Override
    public void unRegisterMediaInfoCallback(String origin, IMediaInfoCallBack mediaInfoCallBack) {
        Log.d(TAG, "unRegisterMediaInfoCallback: ");
        if (mediaInfoCallBack != null) {
            MediaRemoteControl.getInstance().unRegisterMediaInfoCallback(origin, mediaInfoCallBack);
        }
    }

    /**
     * 启动对应的音源
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     * @param openReason   启动的原因
     * @param flag         启动到某个界面的标志, {@link Constant.NavigationFlag}
     */
    @Override
    public void openSourceWithFlag(String source, boolean isForeground, String openReason, int flag) {
        Log.d(TAG, "openSource: source = " + source + " isForeground = " + isForeground + " openReason = " + openReason + " flag = " + flag);
        MediaRemoteControl.getInstance().openSourceWithFlag(source, isForeground, openReason,flag);
    }

    /**
     * 设置Band，预留出来处理切换Band但是不播放对应Band的情况
     * @param band
     */
    @Override
    public void setBand(String source, String band) {
        Log.d(TAG, "setBand,band: "+band + ",source:"+source);
        MediaRemoteControl.getInstance().setBand(source,band);
    }

    /**
     * 设置某个指定的Media，并打开
     * @param source
     * @param mediaInfoBean
     */
    @Override
    public void setMedia(String source, MediaInfoBean mediaInfoBean) {
        Log.d(TAG, "setMedia,source: "+source + ",mediaInfoBean"+mediaInfoBean);
        MediaRemoteControl.getInstance().setMedia(source, mediaInfoBean);
    }


    @Override
    public void startAst(String source) {
        Log.d(TAG, "startAst,source: "+source);
        MediaRemoteControl.getInstance().startAst(source);
    }

    @Override
    public void stopAst(String source) {
        Log.d(TAG, "stopAst,source: "+source);
        MediaRemoteControl.getInstance().stopAst(source);
    }

    @Override
    public void setMediaSettings(String source, String type, int value) {
        //预留接口，用来操作Radio等的设置项，目前是个空实现，待后续有需要再进行实现
        Log.d(TAG, "setMediaSettings,source: "+source + ", type: " + type + ", value: " + value);
    }

    @Nullable
    @Override
    public String getCurrentSearchStatus(String source) {
        String searchStatus = MediaRemoteControl.getInstance().getCurrentSearchStatus(source);
        Log.d(TAG, "getCurrentSearchStatus,searchStatus: "+searchStatus);
        return searchStatus;
    }

    @Nullable
    @Override
    public String getRadioFreq(String source) {
        String radioFreq = MediaRemoteControl.getInstance().getRadioFreq(source);
        Log.d(TAG, "getRadioFreq,searchStatus: "+radioFreq);
        return radioFreq;
    }

    @Nullable
    @Override
    public String getRadioStatus(String source) {
        //预留接口，todo
        return null;
    }

    @Override
    public RemoteBeanList getRemoteList(String source, String type) {
        RemoteBeanList remoteBeanList = MediaRemoteControl.getInstance().getRemoteList(source,type);
        Log.d(TAG, "getRemoteList,remoteBeanList: "+ remoteBeanList);
        return remoteBeanList;
    }
}
