package com.desaysv.modulebtmusic.utils;

import android.util.Log;

import com.desaysv.modulebtmusic.Constants;

import java.util.ArrayList;

/**
 * 通用回调工具类
 *
 * @param <T>
 */
public class ObserverBuilder<T> {
    private static final String TAG = Constants.TAG + "Observer";
    private final ArrayList<T> mObserverList = new ArrayList<>();

    public void registerObserver(T observer) {
        if (observer == null) {
            Log.w(TAG, "registerObserver: observer == null");
            return;
        }
        synchronized (mObserverList) {
            boolean isAddSuccess;
            String className = observer.getClass().getName();
            if (isHashCodeException(observer)) {
                isAddSuccess = mObserverList.add(observer);
                Log.i(TAG, "registerObserver: Remote! isAddSuccess=" + isAddSuccess + ",className=" + className);
            } else if (!mObserverList.contains(observer)) {
                isAddSuccess = mObserverList.add(observer);
                Log.i(TAG, "registerObserver: Local! isAddSuccess=" + isAddSuccess + ",className=" + className);
            } else {
                Log.i(TAG, "registerObserver: Existed! className=" + className);
            }
        }
    }

    public void unregisterObserver(T observer) {
        if (observer == null) {
            Log.w(TAG, "unregisterObserver: observer == null");
            return;
        }
        if (isHashCodeException(observer))
            return;
        synchronized (mObserverList) {
            boolean isRemoveSuccess = mObserverList.remove(observer);
            Log.i(TAG, "unregisterObserver: isRemoveSuccess=" + isRemoveSuccess
                    + ",className=" + observer.getClass().getName());
        }
    }

    public void clearObservers() {
        synchronized (mObserverList) {
            mObserverList.clear();
        }
    }

    public void notifyObservers(IListener listener) {
        if (listener == null)
            return;
        ArrayList<T> tempObserverList = new ArrayList<>(mObserverList);
        for (int i = 0; i < tempObserverList.size(); i++) {
            T observer = tempObserverList.get(i);
            if (observer == null) {
                continue;
            }
            try {
                listener.onNotification(observer);
            } catch (Exception e) {
                Log.e(TAG, "notifyObservers: Exception");
                e.printStackTrace();
            }
        }
    }

    private boolean isHashCodeException(T observer) {
        boolean isException = false;
        try {
            observer.hashCode();
        } catch (Exception e) {
            StackTraceElement[] stackTraceElement = e.getStackTrace();
            for (StackTraceElement element : stackTraceElement) {
                Log.i(TAG, "isHashCodeException: " + element);
            }
            isException = true;
        }
        return isException;
    }

    public interface IListener<T> {
        void onNotification(T observer);
    }
}