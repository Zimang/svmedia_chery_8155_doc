// IMusicInfoCallback.aidl
package com.desaysv.localmediasdk;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
/**
 * created by lzm on 2020-03-18
 * purpose: 本地媒体的AIDL接口，用来控制和获取媒体状态的接口
 */
interface IAIDLMediaInfoCallback {

    //媒体的状态发送改变会触发回调，回调里面包括了音源和媒体信息的json
    oneway void onMediaInfoChange(String source, String InfoType,String mediaInfo);

    //如果媒体数据带有专辑封面的话，就会根据这个回调过来
    oneway void onMediaPicChange(String source,in byte[] picInfo);

    void onRemoteListChanged(String source,String type, in RemoteBeanList remoteList);
}
