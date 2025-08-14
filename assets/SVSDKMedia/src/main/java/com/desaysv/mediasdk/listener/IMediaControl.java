package com.desaysv.mediasdk.listener;

import android.content.Context;

import androidx.annotation.Nullable;

import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.mediasdk.bean.Constant;
import com.desaysv.mediasdk.bean.MediaInfoBean;


/**
 * Created by LZM on 2020-3-18
 * Comment 全部媒体控制器的入口
 */
public interface IMediaControl {


    /**
     * 初始化逻辑，初始化的时候，需要将远程的媒体控制器设置进去
     *
     * @param context 上下文
     */
    void initialize(Context context);

    /**
     * 注册媒体变化的回调广播
     *
     * @param origin                    什么类型的控制器
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    void registerMediaInfoCallback(String origin, IMediaInfoCallBack iRemountMediaInfoCallBack);

    /**
     * 注销媒体变化的回调
     *
     * @param origin                    什么类型的控制器
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    void unRegisterMediaInfoCallback(String origin, IMediaInfoCallBack iRemountMediaInfoCallBack);

    /**
     * 注入逻辑控制器
     *
     * @param origin             什么类型的控制器
     * @param remoteMediaManager 逻辑控制器
     */
    void injectControl(String origin, IMediaFunction remoteMediaManager);

    /**
     * 进行播放
     */
    void play(String source);

    /**
     * 进行暂停
     */
    void pause(String source);

    /**
     * 进行播放或者暂停
     */
    void playOrPause(String source);

    /**
     * 进行下一曲
     */
    void next(String source);

    /**
     * 进行上一曲
     */
    void pre(String source);

    /**
     * 进行快进
     */
    void startFastForward(String source);

    /**
     * 停止快进
     */
    void stopFastForward(String source);

    /**
     * 开始快退
     */
    void startRewind(String source);

    /**
     * 收藏逻辑，专门给收音使用的
     *
     * @param source 控制的音源
     */
    void collect(String source);


    /**
     * 切换循环模式，专门给USB音乐使用的
     *
     * @param source 控制的音源
     */
    void changeLoopType(String source);

    /**
     * 停止快退
     */
    void stopRewind(String source);

    /**
     * 根据选择的音源获取该音源的ID3信息
     *
     * @return ID3信息的json数据
     */
    String getCurrentPlayInfo(String source);

    /**
     * 根据选择的音源获取该源的播放状态
     *
     * @return 播放状态的json数据
     */
    String getCurrentPlayStatus(String source);

    /**
     * 根据选择的音源获取当前的播放时间
     *
     * @return 播放时间的json数据
     */
    String getCurrentPlayTime(String source);


    /**
     * 获取专辑封面的数据，本地音乐使用
     *
     * @param source 专辑封面的数据
     * @return 专辑封面的图片数据
     */
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
     * 获取收藏状态，只针对于收音
     *
     * @param source 需要获取的音源
     * @return 收音的收藏状态
     */
    String getCollectStatus(String source);


    /**
     * 获取循环模式，只针对于USB音乐
     *
     * @param source 需要获取的音源
     * @return USB音乐的循环状态
     */
    String getLoopType(String source);

    /**
     * 启动对应的音源
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     * @param openReason   启动的原因
     * @param flag         启动到某个界面的标志, {@link Constant.NavigationFlag}
     */
    void openSourceWithFlag(String source, boolean isForeground, String openReason ,int flag);

    /**
     * 设置Band，预留出来处理切换Band但是不播放对应Band的情况
     * @param band {@link Constant.RadioBand}
     */
    void setBand(String source, String band);

    /**
     * 设置某个指定的Media，并打开
     * @param source
     * @param mediaInfoBean
     */
    void setMedia(String source, MediaInfoBean mediaInfoBean);

    /**
     * 启动搜索
     * @param source
     */
    void startAst(String source);

    /**
     * 停止搜索
     * @param source
     */
    void stopAst(String source);

    /**
     * 操作Media设置的接口
     * @param source 选中的音源
     * @param type 例如 TA/SF等
     * @param value 设置的值，0 表示关闭，1表示打开，2以及后面的待定
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
     * @param source
     * @param type {@link Constant.ListType}
     * @return
     */
    RemoteBeanList getRemoteList(String source, String type);
}
