package com.desaysv.usbbaselib.statussubject;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbbaselib.observer.Subject;

import java.util.Map;

/**
 * @author lzm
 * @date 2019-6-10
 * 获取USB1图片的搜索状态，观察者模式
 */

public class USB1PictureDataSubject extends Subject {

    private static final String TAG = "USB1PictureDataSubject";

    private static USB1PictureDataSubject instance;

    public static USB1PictureDataSubject getInstance() {
        if (instance == null) {
            synchronized (USB1PictureDataSubject.class) {
                if (instance == null) {
                    instance = new USB1PictureDataSubject();
                }
            }
        }
        return instance;
    }

    /**
     * 通知USB1图片数据发送改变
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
     * 初始化，这里不用做逻辑，应用起来的时候需要主动的获取一下图片数据
     */
    @Override
    public void initialize() {

    }

    private SearchType USB1PictureSearchType = SearchType.SEARCHING;

    /**
     * 获取USB1图片数据
     *
     * @return USB1PictureSearchType
     */
    public SearchType getUSB1PictureSearchType() {
        return USB1PictureSearchType;
    }

    /**
     * 设置USB1图片数据状态，并通知给观察者
     *
     * @param USB1PictureSearchType USB1图片状态
     */
    public void setUSB1PictureSearchType(SearchType USB1PictureSearchType) {
        Log.d(TAG, "setUSB1PictureSearchType: USB1PictureSearchType = " + USB1PictureSearchType);
        this.USB1PictureSearchType = USB1PictureSearchType;
        notifyObserverUpdate();
    }
}
