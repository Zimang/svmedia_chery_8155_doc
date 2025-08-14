package com.desaysv.localmediasdk.sdk.manager;

import android.content.Context;

import androidx.annotation.Nullable;

import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.localmediasdk.sdk.listener.IMediaInfoCallBack;
import com.desaysv.localmediasdk.sdk.bean.Constant;

/**
 * Created by LZM on 2020-5-2
 * Comment 透传使用的接口
 */
public interface ISDKMediaManager {

    /**
     * 初始化媒体外部的媒体控制器
     *
     * @param context 上下文
     */
    void initialize(Context context);


    /**
     * 进行播放
     *
     * @param source 需要控制的音源
     */
    void play(String source);

    /**
     * 进行暂停
     *
     * @param source 需要控制的音源
     */
    void pause(String source);

    /**
     * 进行播放或者暂停
     *
     * @param source 需要控制的音源
     */
    void playOrPause(String source);

    /**
     * 进行下一曲
     *
     * @param source 需要控制的音源
     */
    void next(String source);

    /**
     * 进行上一曲
     *
     * @param source 需要控制的音源
     */
    void pre(String source);

    /**
     * 设置跳转播放进度
     *
     * @param source source 控制的音源
     * @param time   time 设置的时间
     */
    void seekTo(String source, int time);

    /**
     * 进行快进
     *
     * @param source 需要控制的音源
     */
    void startFastForward(String source);

    /**
     * 停止快进
     *
     * @param source 需要控制的音源
     */
    void stopFastForward(String source);

    /**
     * 开始快退
     *
     * @param source 需要控制的音源
     */
    void startRewind(String source);

    /**
     * 停止快退
     *
     * @param source 需要控制的音源
     */
    void stopRewind(String source);

    /**
     * 收藏逻辑
     *
     * @param source 控制的音源
     */
    void collect(String source);


    /**
     * 切换循环模式
     *
     * @param source 控制的音源
     * @param typeInfo 切换的类型
     */
    void changeLoopType(String source, String typeInfo);

    /**
     * 根据选择的音源获取该音源的ID3信息
     *
     * @param source 需要控制的音源
     * @return ID3信息的json数据
     */
    @Nullable
    String getCurrentPlayInfo(String source);

    /**
     * 根据选择的音源获取该源的播放状态
     *
     * @param source 需要控制的音源
     * @return 播放状态的json数据
     */
    @Nullable
    String getCurrentPlayStatus(String source);

    /**
     * 根据选择的音源获取当前的播放时间
     *
     * @param source 需要控制的音源
     * @return 播放时间的json数据
     */
    @Nullable
    String getCurrentPlayTime(String source);

    /**
     * 获取设备连接装
     *
     * @param source source
     * @return 状态对应的json数据
     */
    String getDeviceConnectState(String source);

    /**
     * 根据选择的音源获取当前的专辑封面
     *
     * @param source 需要获取的音源
     * @return 图片数据的数组
     */
    @Nullable
    byte[] getAlbumPicData(String source);

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
     * 获取收藏状态
     *
     * @param source 需要获取的音源
     * @return 收音的收藏状态
     */
    String getCollectStatus(String source);


    /**
     * 获取循环模式
     *
     * @param source 需要获取的音源
     * @return USB音乐的循环状态
     */
    String getLoopType(String source);

    /**
     * 注册媒体变化的回调广播
     *
     * @param origin                    需要注册那个来源的数据变化
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    void registerMediaInfoCallback(String origin, IMediaInfoCallBack iRemountMediaInfoCallBack);

    /**
     * 注销媒体变化的回调广播
     *
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    void unRegisterMediaInfoCallback(String origin, IMediaInfoCallBack iRemountMediaInfoCallBack);


    /**
     * 启动对应的音源
     *
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     * @param openReason   启动的原因
     * @param flag         启动到某个界面的标志, {@link Constant.NavigationFlag}
     */
    void openSourceWithFlag(String source, boolean isForeground, String openReason, int flag);

    /**
     * 设置Band，预留出来处理切换Band但是不播放对应Band的情况
     *
     * @param band {@link Constant.RadioBand}
     */
    void setBand(String source, String band);

    /**
     * 设置某个指定的Media，并打开
     *
     * @param source
     * @param mediaInfoBean
     */
    void setMedia(String source, MediaInfoBean mediaInfoBean);

    /**
     * 启动搜索
     *
     * @param source
     */
    void startAst(String source);

    /**
     * 停止搜索
     *
     * @param source
     */
    void stopAst(String source);


    /**
     * 操作Media设置的接口
     *
     * @param source 选中的音源
     * @param type   例如 TA/SF等
     * @param value  设置的值，0 表示关闭，1表示打开，2以及后面的待定
     */
    void setMediaSettings(String source, String type, int value);

    /**
     * 根据选择的音源获取该源的搜索状态
     *
     * @param source 需要控制的音源
     * @return 搜索状态的json数据
     */
    @Nullable
    String getCurrentSearchStatus(String source);


    /**
     * 根据选择的音源获取频点值
     * dab也可以用这个返回电台名
     *
     * @param source 需要控制的音源
     * @return Radio频点值的json数据
     */
    @Nullable
    String getRadioFreq(String source);


    /**
     * 根据选择的音源获取该源的RDS/DAB设置状态
     *
     * @param source 需要控制的音源
     * @return RDS/DAB设置状态的json数据
     */
    @Nullable
    String getRadioStatus(String source);

    /**
     * 根据选择的音源和类型获取对应的列表
     *
     * @param source
     * @param type   {@link Constant.ListType}
     * @return
     */
    RemoteBeanList getRemoteList(String source, String type);
}
