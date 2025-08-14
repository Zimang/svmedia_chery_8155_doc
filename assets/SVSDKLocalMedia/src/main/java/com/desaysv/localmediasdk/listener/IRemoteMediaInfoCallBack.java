package com.desaysv.localmediasdk.listener;

import com.desaysv.localmediasdk.bean.RemoteBeanList;

/**
 * Created by LZM on 2020-3-18
 * Comment 数据变化的时候，SDK里面会触发这个回调，然后将数据变化回馈给SVMediaService里面注册了这个接口的对象
 */
public interface IRemoteMediaInfoCallBack {


    /**
     * 媒体数据发生改变，会触发这个回调
     *
     * @param source    数据变化是那个音源
     * @param InfoType  变化的数据是那种类型的数据
     * @param mediaInfo 数据本体
     */
    void onMediaInfoChange(String source, String InfoType, String mediaInfo);

    /**
     * 媒体的专辑图片发生改把的时候，会触发的数据变化
     *
     * @param source  对应的音源
     * @param picInfo 图片数据的json
     */
    void onMediaPicChange(String source, byte[] picInfo);

    /**
     * 媒体数据列表变化
     * @param source
     * @param type
     * @param remoteList
     */
    void onRemoteListChanged(String source,String type, RemoteBeanList remoteList);
}
