// IMusicManager.aidl
package com.desaysv.localmediasdk;

import com.desaysv.localmediasdk.IAIDLMediaInfoCallback;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
/**
 * created by lzm on 2020-03-18
 * purpose: 本地媒体的AIDL接口，用来控制和获取媒体状态的接口
 */
interface IAIDLMediaManager {
    /**********************************************音乐的控制逻辑************************************************/
    //发送控制命令 source:需要控制的音源  commond：控制的命令，包括上下曲，播放暂停
    oneway void sendCommand(String source,String command,String mediaInfo);

    /*******************************************媒体状态的接口*******************************************/
    //获取媒体的播放信息 source:需要获取数据的音源 type:需要获取那个类型的音源信息
    String getSelectInfo(String source,String type);

    //获取当前播放媒体的专辑图片数据
    byte[] getCurrentPic(String source);

    //注册媒体状态的变化回调
    oneway void registerMediaInfoCallback(String className,IAIDLMediaInfoCallback aileMediaInfoCallBack);

    //注销媒体状态的变化回调
    oneway void unregisterMediaInfoCallback(String className,IAIDLMediaInfoCallback aileMediaInfoCallBack);

    RemoteBeanList getRemoteList(String source, String type);
}
