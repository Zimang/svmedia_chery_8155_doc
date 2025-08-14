package com.desaysv.localmediasdk.listener;

import com.desaysv.localmediasdk.IAIDLMediaManager;
import com.desaysv.localmediasdk.bean.RemoteBeanList;

/**
 * Created by LZM on 2020-3-18
 * Comment 媒体控制器的接口
 */
public interface IRemoteMediaManager {

    /**
     * 初始化逻辑，初始化的时候，需要将远程的媒体控制器设置进去
     *
     * @param iaidlMediaManager AIDL的绑定服务
     */
    void initialize(IAIDLMediaManager iaidlMediaManager);

    /**
     * 服务断开的时候，需要将媒体播放器销毁掉
     */
    void destroy();

    /**
     * 发送控制命令
     *
     * @param source    需要控制的音源
     * @param command   需要控制的动作，上一曲，下一曲等
     * @param mediaInfo 携带的信息
     */
    void sendCommand(String source, String command, String mediaInfo);

    /**
     * 获取选中音源选中的类型的信息
     *
     * @param source 需要获取数据的音源
     * @param type   需要获取那种类型的数据
     * @return 对于数据的json数据
     */
    String getSelectInfo(String source, String type);

    /**
     * 获取对于音源的图片数据
     *
     * @return byte[]
     */
    byte[] getPicData(String source);

    /**
     * 注册媒体变化的回调广播
     *
     * @param iRemoteMediaInfoCallBack 数据变化的回调
     */
    void registerMediaInfoCallback(IRemoteMediaInfoCallBack iRemoteMediaInfoCallBack);

    /**
     * 注销媒体变化的回调广播
     *
     * @param iRemoteMediaInfoCallBack 数据变化的回调
     */
    void unRegisterMediaInfoCallback(IRemoteMediaInfoCallBack iRemoteMediaInfoCallBack);

    /**
     * 获取是否初始化完成，如果没有初始化完成，调用方法会出现空指针异常
     *
     * @return true 初始化完成；false 初始化失败
     */
    boolean getInitSuccess();

    /**
     * 根据选择的音源和类型获取对应的列表
     *
     * @param source
     * @param type
     * @return
     */
    RemoteBeanList getRemoteList(String source, String type);
}
