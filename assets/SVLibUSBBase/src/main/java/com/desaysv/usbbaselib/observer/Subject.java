package com.desaysv.usbbaselib.observer;

import android.util.ArrayMap;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author uidp5370
 * @date 2019-6-3
 * 观察者模式的父类
 */

public abstract class Subject {

    private final String TAG = this.getClass().getSimpleName();

    //进行加锁处理
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    protected final Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = mReentrantLock.writeLock();
    /*将一个被观察者对象和多个观察者对象绑定起来*/
    protected ArrayMap<String, Observer> observers = new ArrayMap<>();

    /**
     * 添加观察者
     *
     * @param observer 添加观察者的回调
     */
    public void attachObserver(String className, Observer observer) {
        Log.d(TAG, "attachObserver: className = " + className + " observer = " + observer);
        writeLock.lock();
        try {
            observers.put(className, observer);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 注销观察者
     *
     * @param observer 添加观察者的回调
     */
    public void detachObserver(String className) {
        writeLock.lock();
        try {
            observers.remove(className);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 通知观察者数据变化
     */
    public abstract void notifyObserverUpdate();

    /**
     * 初始化的逻辑
     */
    public abstract void initialize();


}
