package com.desaysv.usbbaselib.statussubject;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.observer.Subject;

import java.util.Map;

/**
 *
 * @author lzm
 * @date 2019-6-10
 * 获取USB1视频的搜索状态，观察者模式
 */

public class USB1VideoDataSubject extends Subject {

    private static final String TAG = "USB1VideoDataSubject";

    private static USB1VideoDataSubject instance;

    public static USB1VideoDataSubject getInstance() {
        if (instance == null) {
            synchronized (USB1VideoDataSubject.class) {
                if (instance == null) {
                    instance = new USB1VideoDataSubject();
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
     * 初始化，这里不需要做，因为应用起来后需要主动去扫描一次数据
     */
    @Override
    public void initialize() {

    }

    private SearchType USB1VideoSearchType = SearchType.SEARCHING;

    /**
     * 获取USB1视频数据的搜索状态
     *
     * @return USB1VideoSearchType
     */
    public SearchType getUSB1VideoSearchType() {
        return USB1VideoSearchType;
    }

    /**
     * 设置USB1视频数据的搜索状态
     *
     * @param USB1VideoSearchType
     */
    public void setUSB1VideoSearchType(SearchType USB1VideoSearchType) {
        Log.d(TAG, "setUSB1VideoSearchType: USB1VideoSearchType = " + USB1VideoSearchType);
        this.USB1VideoSearchType = USB1VideoSearchType;
        notifyObserverUpdate();
    }
}
