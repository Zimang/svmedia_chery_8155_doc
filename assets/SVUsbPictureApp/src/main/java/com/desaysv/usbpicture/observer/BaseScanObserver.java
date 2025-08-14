package com.desaysv.usbpicture.observer;

import android.util.Log;

import com.desaysv.usbbaselib.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by ZNB on 2022-02-24
 * 扫描模式的观察者
 * 通知扫描状态的变化
 */

public abstract class BaseScanObserver {
    protected final String TAG = this.getClass().getSimpleName();

    /*将一个被观察者对象和多个观察者对象绑定起来*/
    protected List<IScanObserver> observers = new ArrayList<>();
    //进行加锁处理
    protected ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    protected Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    protected Lock writeLock = mReentrantLock.writeLock();

    /**
     * 添加观察者
     *
     * @param observer 添加观察者的回调
     */
    public void attachObserver(IScanObserver observer) {
        Log.d(TAG, "attachObserver: observer = " + observer);
        writeLock.lock();
        try {
            if (observer != null) {
                observers.remove(observer);
                observers.add(observer);
            }
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * 注销观察者
     *
     * @param observer 添加观察者的回调
     */
    public void detachObserver(IScanObserver observer) {
        Log.d(TAG, "detachObserver: observer = " + observer);
        writeLock.lock();
        try {
            if (observer != null) {
                observers.remove(observer);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 通知观察者数据变化
     */
    public abstract void notifyObserverUpdate();

    /**
     * 获取当前观察者的数量，以区分是否进入了应用界面
     */
    public abstract int getObserverSize();

}
