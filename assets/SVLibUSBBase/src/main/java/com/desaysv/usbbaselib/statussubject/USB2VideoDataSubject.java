package com.desaysv.usbbaselib.statussubject;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.observer.Subject;

import java.util.Map;

/**
 *
 * @author lzm
 * @date 2019-6-10
 * 后去USB2视频的搜索状态，观察者模式
 */

public class USB2VideoDataSubject extends Subject {

    private static final String TAG = "USB2VideoDataSubject";

    private static USB2VideoDataSubject instance;

    public static USB2VideoDataSubject getInstance() {
        if (instance == null) {
            synchronized (USB2VideoDataSubject.class) {
                if (instance == null) {
                    instance = new USB2VideoDataSubject();
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
     * 不需要在这里初始化，因为应用启动的时候会主动去扫描一次数据
     */
    @Override
    public void initialize() {

    }

    private SearchType USB2VideoSearchType = SearchType.SEARCHING;

    /**
     * 获取USB2视频数据的扫描状态
     *
     * @return
     */
    public SearchType getUSB2VideoSearchType() {
        return USB2VideoSearchType;
    }

    /**
     * 设置USB2视频数据的扫描状态
     *
     * @param USB2VideoSearchType
     */
    public void setUSB2VideoSearchType(SearchType USB2VideoSearchType) {
        Log.d(TAG, "setUSB2VideoSearchType: USB2VideoSearchType = " + USB2VideoSearchType);
        this.USB2VideoSearchType = USB2VideoSearchType;
        notifyObserverUpdate();
    }
}
