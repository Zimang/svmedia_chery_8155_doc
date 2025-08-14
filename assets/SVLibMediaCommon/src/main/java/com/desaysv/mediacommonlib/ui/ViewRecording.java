package com.desaysv.mediacommonlib.ui;

import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LZM on 2020-8-29
 * Comment 用来记录界面前后台的类，如果有问题，可以修改为记录所有界面的生命周期
 *
 * @author uidp5370
 */
public class ViewRecording {

    private static final String TAG = "ViewRecording";

    private static ViewRecording instance;

    public static ViewRecording getInstance() {
        if (instance == null) {
            synchronized (ViewRecording.class) {
                if (instance == null) {
                    instance = new ViewRecording();
                }
            }
        }
        return instance;
    }

    private ViewRecording() {

    }

    /**
     * 数据前后台数据变化的回调列表
     */
    private List<ViewRecordingListener> mViewRecordingListenerList = new ArrayList<>();

    /**
     * 注册界面前后台变化时触发的回调
     *
     * @param viewRecordingListener 界面前后台变化回调
     */
    public void registerViewRecordingListener(ViewRecordingListener viewRecordingListener) {
        if (viewRecordingListener != null) {
            mViewRecordingListenerList.add(viewRecordingListener);
        }
    }

    /**
     * 注销界面前后台变化时触发的回调
     *
     * @param viewRecordingListener 界面前后台变化回调
     */
    public void unRegisterViewRecordingListener(ViewRecordingListener viewRecordingListener) {
        if (viewRecordingListener != null) {
            mViewRecordingListenerList.remove(viewRecordingListener);
        }
    }


    /**
     * 这是一个HashMap，用来记录界面名称，与前后台的关系
     */
    private HashMap<String, Boolean> mViewRecording = new HashMap<>();

    /**
     * 设置界面是否是前后台，这里要实现观察者模式
     *
     * @param viewName 界面的名称。全类名，包括包名
     * @param isFg     true 在前台 false 在后台
     */
    void setViewIsFg(String viewName, boolean isFg) {
        Log.d(TAG, "setViewIsFg: viewName = " + viewName + " isFg = " + isFg);
        mViewRecording.put(viewName, isFg);
        for (ViewRecordingListener viewRecordingListener : mViewRecordingListenerList) {
            viewRecordingListener.onViewRecordingChange(viewName, mViewRecording);
        }
    }


    /**
     * 获取对应界面是否是前台界面
     *
     * @param viewName 界面的全名称
     * @return true：界面在前台 false：界面在后台
     */
    public boolean getViewFgStatus(String viewName) {
        Boolean isFg = mViewRecording.get(viewName);
        if (isFg == null) {
            isFg = false;
        }
        Log.d(TAG, "getViewFgStatus: viewName = " + viewName + " isFg = " + isFg);
        return (boolean) isFg;
    }

    /**
     * 获取记忆界面前后台状态的HashMap
     *
     * @return mViewRecording
     */
    public HashMap<String, Boolean> getViewRecording() {
        return mViewRecording;
    }


    /**
     * 界面前后台HashMap变化的回调
     */
    public interface ViewRecordingListener {

        /**
         * 界面记录map数据变化的时候，触发的回调
         *
         * @param viewName      那个界面发生了变化
         * @param viewRecording 记录界面前后台的HashMap
         */
        void onViewRecordingChange(String viewName, HashMap<String, Boolean> viewRecording);

    }

}
