package com.desaysv.usbbaselib.statussubject;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.observer.Subject;

import java.util.Map;

/**
 *
 * @author lzm
 * @date 2019-6-10
 * 获取USB2音乐的搜索状态，观察者模式
 */

public class USB2MusicDataSubject extends Subject {

    private static final String TAG = "USB2MusicDataSubject";

    private static USB2MusicDataSubject instance;

    public static USB2MusicDataSubject getInstance() {
        if (instance == null) {
            synchronized (USB2MusicDataSubject.class) {
                if (instance == null) {
                    instance = new USB2MusicDataSubject();
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
     * 这里面不需要做逻辑，后面应用起来后要主动去扫描一次数据
     */
    @Override
    public void initialize() {

    }

    private SearchType USB2MusicSearchType = SearchType.SEARCHING;

    /**
     * 获取USB2音乐的列表的搜索状态
     *
     * @return USB2MusicSearchType
     */
    public SearchType getUSB2MusicSearchType() {
        return USB2MusicSearchType;
    }

    /**
     * 设置USB2音乐列表的搜索状态
     *
     * @param USB2MusicSearchType NO_DATA,            //无数据
     *                            SEARCHING,          //搜索中
     *                            SEARCH_COMPLETE     //搜索完成，并且有数据
     */
    public void setUSB2MusicSearchType(SearchType USB2MusicSearchType) {
        Log.d(TAG, "setUSB2MusicSearchType: USB2MusicSearchType = " + USB2MusicSearchType);
        this.USB2MusicSearchType = USB2MusicSearchType;
        notifyObserverUpdate();
    }
}
