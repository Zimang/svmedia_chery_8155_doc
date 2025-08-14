package com.desaysv.svlibpicturebean.manager;

import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.usbbaselib.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * created by ZNB on 2022-08-05
 * CVBOX的相册列表数据管理器
 */
public class AlbumListManager {
    private static final String TAG ="AlbumListManager";

    private volatile static AlbumListManager mInstance;
    public static AlbumListManager getInstance() {
        synchronized (AlbumListManager.class) {
            if (mInstance == null) {
                mInstance = new AlbumListManager();
            }
            return mInstance;
        }
    }

    private List<Observer> albumObservers = new ArrayList<>();//观察者列表

    private CopyOnWriteArrayList<FileMessage> mAlbumList = new CopyOnWriteArrayList<>();//数据列表

    /**
     * 注册数据观察者
     * @param observer
     */
    public void attachObserver(Observer observer){
        albumObservers.add(observer);
    }

    /**
     * 注销数据观察者
     * @param observer
     */
    public void detachObserver(Observer observer){
        albumObservers.remove(observer);
    }

    /**
     * 通知观察者数据变化
     */
    private void notifyObserver(){
        for (Observer observer : albumObservers){
            observer.onUpdate();
        }
    }

    /**
     * 更新Album列表数据
     * @param albums
     */
    public void updateAlbumList(CopyOnWriteArrayList<FileMessage> albums){
        mAlbumList.clear();
        mAlbumList.addAll(albums);
        notifyObserver();
    }

    /**
     * 获取Album列表数据
     * @return
     */
    public CopyOnWriteArrayList<FileMessage> getAlbumList(){
        return mAlbumList;
    }
}
