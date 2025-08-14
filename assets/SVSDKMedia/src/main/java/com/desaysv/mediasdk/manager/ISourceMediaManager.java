package com.desaysv.mediasdk.manager;

import android.content.Context;
import androidx.annotation.Nullable;

import com.desaysv.mediasdk.listener.IMediaInfoCallBack;

/**
 * Created by LZM on 2020-5-2
 * Comment 经过音源过滤使用的接口
 */
public interface ISourceMediaManager {

    /**
     * 初始化媒体外部的媒体控制器
     *
     * @param context 上下文
     */
    void initialize(Context context);


    /**
     * 根据当前音源控制播放
     */
    void play();

    /**
     * 根据当前音源进行暂停
     */
    void pause();

    /**
     * 根据当前音源进行播放或者暂停
     */
    void playOrPause();

    /**
     * 根据当前音源进行下一曲
     */
    void next();

    /**
     * 根据当前音源进行上一曲
     */
    void pre();

    /**
     * 根据当前音源进行快进
     */
    void startFastForward();

    /**
     * 根据当前音源停止快进
     */
    void stopFastForward();

    /**
     * 根据当前音源开始快退
     */
    void startRewind();

    /**
     * 根据当前音源停止快退
     */
    void stopRewind();

    /**
     * 收藏逻辑
     */
    void collect();


    /**
     * 切换循环模式
     */
    void changeLoopType();

    /**
     * 根据当前音源获取该音源的ID3信息
     *
     * @return ID3信息的json数据
     */
    @Nullable
    String getCurrentPlayInfo();

    /**
     * 根据当前音源获取该源的播放状态
     *
     * @return 播放状态的json数据
     */
    @Nullable
    String getCurrentPlayStatus();

    /**
     * 根据当前音源获取当前的播放时间
     *
     * @return 播放时间的json数据
     */
    @Nullable
    String getCurrentPlayTime();


    /**
     * 根据当前的音源获取专辑封面
     *
     * @return 专辑封面数据
     */
    byte[] getCurrentAlbumPic();

    /**
     * 启动对应的音源
     *
     * @param context      上下文
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     * @param openReason   启动的原因
     */
    void openSource(Context context, String source, boolean isForeground, String openReason);

    /**
     * 获取收藏状态，只针对于收音
     *
     * @return 收音的收藏状态
     */
    String getCollectStatus();


    /**
     * 获取循环模式
     *
     * @return USB音乐的循环状态
     */
    String getLoopType();

    /**
     * 注册媒体变化的回调广播
     *
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    void registerCurrentMediaInfoCallback(IMediaInfoCallBack iRemountMediaInfoCallBack);

    /**
     * 注销媒体变化的回调广播
     *
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    void unRegisterCurrentMediaInfoCallback(IMediaInfoCallBack iRemountMediaInfoCallBack);

}
