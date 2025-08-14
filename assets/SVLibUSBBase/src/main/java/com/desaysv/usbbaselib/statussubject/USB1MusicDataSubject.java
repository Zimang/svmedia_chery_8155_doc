package com.desaysv.usbbaselib.statussubject;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.observer.Subject;

import java.util.Map;

/**
 *
 * @author lzm
 * @date 2019-6-10
 * 获取USB1音乐数据的搜索状态，观察者模式
 */

public class USB1MusicDataSubject extends Subject {

    private static final String TAG = "USB1MusicDataSubject";

    private static USB1MusicDataSubject instance;

    public static USB1MusicDataSubject getInstance() {
        if (instance == null) {
            synchronized (USB1MusicDataSubject.class) {
                if (instance == null) {
                    instance = new USB1MusicDataSubject();
                }
            }
        }
        return instance;
    }

    /**
     * 通知观察者USB1音乐数据刷新
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
     * 初始化，里面没有做逻辑，一般是要应用起来后自己去扫描一次数据，所以就不会再这里做了
     */
    @Override
    public void initialize() {

    }


    private SearchType USB1MusicSearchType = SearchType.SEARCHING;

    /**
     * 获取USB1音乐的搜索状态
     *
     * @return USB1MusicSearchType
     */
    public SearchType getUSB1MusicSearchType() {
        Log.d(TAG, "getUSB1MusicSearchType: USB1MusicSearchType = " + USB1MusicSearchType);
        return USB1MusicSearchType;
    }

    /**
     * 设置USB1音乐的搜索状态，然后在回调给所有的观察者
     *
     * @param USB1MusicSearchType 当前的状态
     */
    public void setUSB1MusicSearchType(SearchType USB1MusicSearchType) {
        Log.d(TAG, "setUSB1MusicSearchType: USB1MusicSearchType = " + USB1MusicSearchType);
        this.USB1MusicSearchType = USB1MusicSearchType;
        notifyObserverUpdate();
    }
}
