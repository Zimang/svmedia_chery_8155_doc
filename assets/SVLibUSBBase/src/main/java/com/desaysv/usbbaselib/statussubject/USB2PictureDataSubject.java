package com.desaysv.usbbaselib.statussubject;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.observer.Subject;

import java.util.Map;

/**
 *
 * @author lzm
 * @date 2019-6-10
 * 获取USB2图片的搜索状态，观察者模式
 */

public class USB2PictureDataSubject extends Subject {

    private static final String TAG = "USB2PictureDataSubject";

    private static USB2PictureDataSubject instance;

    public static USB2PictureDataSubject getInstance() {
        if (instance == null) {
            synchronized (USB2PictureDataSubject.class) {
                if (instance == null) {
                    instance = new USB2PictureDataSubject();
                }
            }
        }
        return instance;
    }

    /**
     * 通知观察者数据变化
     */
    @Override
    public void notifyObserverUpdate() {
        readLock.lock();
        try {
            for (Map.Entry<String, Observer> entry : observers.entrySet()) {
                Log.d(TAG, "notifyObserverUpdate: className = " + entry.getKey());
                entry.getValue().onUpdate();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 这里面不需要做逻辑，应用启动的时候需要主动去扫描一下数据
     */
    @Override
    public void initialize() {

    }


    private SearchType USB2PictureSearchType = SearchType.SEARCHING;

    /**
     * 获取USB2图片数据的搜索状态
     *
     * @return USB2PictureSearchType
     */
    public SearchType getUSB2PictureSearchType() {
        return USB2PictureSearchType;
    }

    /**
     * 设置USB2图片的搜索状态
     *
     * @param USB2PictureSearchType NO_DATA,            //无数据
     *                              SEARCHING,          //搜索中
     *                              SEARCH_COMPLETE     //搜索完成，并且有数据
     */
    public void setUSB2PictureSearchType(SearchType USB2PictureSearchType) {
        Log.d(TAG, "setUSB2PictureSearchType: USB2PictureSearchType = " + USB2PictureSearchType);
        this.USB2PictureSearchType = USB2PictureSearchType;
        notifyObserverUpdate();
    }
}
