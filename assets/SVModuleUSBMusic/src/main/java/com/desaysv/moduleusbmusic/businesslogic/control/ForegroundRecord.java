package com.desaysv.moduleusbmusic.businesslogic.control;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2020-6-8
 * Comment 当前是那个界面的前台的记录， 这里可以实现观察者模式，用来记录界面是否前后台
 */
public class ForegroundRecord {

    private static final String TAG = "ForegroundRecord";

    private static ForegroundRecord instance;

    public static ForegroundRecord getInstance() {
        if (instance == null) {
            synchronized (ForegroundRecord.class) {
                if (instance == null) {
                    instance = new ForegroundRecord();
                }
            }
        }
        return instance;
    }

    //用来记忆USB1音乐播放界面是否在前台的全局变量
    private boolean isUSB1MusicPlayViewIsForeground = false;

    //用来记忆USB2音乐播放界面是否在前台的全局变量
    private boolean isUSB2MusicPlayViewIsForeground = false;

    public boolean isUSB1MusicPlayViewIsForeground() {
        Log.d(TAG, "isUSB1MusicPlayViewIsForeground: = " + isUSB1MusicPlayViewIsForeground);
        return isUSB1MusicPlayViewIsForeground;
    }

    /**
     * 设置USB1音乐界面是否在前后台，这里需要注意一点，就是界面如果不是单例的话，可能会多次走生命周期，所以需要考虑如何
     * 设置才不会让标志位出现异常
     *
     * @param USB1MusicPlayViewIsForeground 前后台
     */
    public void setUSB1MusicPlayViewIsForeground(boolean USB1MusicPlayViewIsForeground) {
        Log.d(TAG, "setUSB1MusicPlayViewIsForeground: USB1MusicPlayViewIsForeground = " + USB1MusicPlayViewIsForeground);
        isUSB1MusicPlayViewIsForeground = USB1MusicPlayViewIsForeground;
        for (OnMusicPlayViewChangeListener onMusicPlayViewChangeListener : musicPlayViewChangeListenerList) {
            onMusicPlayViewChangeListener.musicPlayViewChangeListener();
        }
    }

    public boolean isUSB2MusicPlayViewIsForeground() {
        Log.d(TAG, "isUSB2MusicPlayViewIsForeground: isUSB2MusicPlayViewIsForeground = " + isUSB2MusicPlayViewIsForeground);
        return isUSB2MusicPlayViewIsForeground;
    }

    /**
     * 设置USB2音乐界面是否在前后台，这里需要注意一点，就是界面如果不是单例的话，可能会多次走生命周期，所以需要考虑如何
     * 设置才不会让标志位出现异常
     *
     * @param USB2MusicPlayViewIsForeground 前后台
     */
    public void setUSB2MusicPlayViewIsForeground(boolean USB2MusicPlayViewIsForeground) {
        Log.d(TAG, "setUSB2MusicPlayViewIsForeground: USB2MusicPlayViewIsForeground = " + USB2MusicPlayViewIsForeground);
        isUSB2MusicPlayViewIsForeground = USB2MusicPlayViewIsForeground;
        for (OnMusicPlayViewChangeListener onMusicPlayViewChangeListener : musicPlayViewChangeListenerList) {
            onMusicPlayViewChangeListener.musicPlayViewChangeListener();
        }
    }

    /**
     * 音乐播放界面数据变化的回调
     */
    private List<OnMusicPlayViewChangeListener> musicPlayViewChangeListenerList = new ArrayList<>();

    public void setMusicPlayViewChangeListener(OnMusicPlayViewChangeListener
                                                       onMusicPlayViewChangeListener) {
        Log.d(TAG, "setMusicPlayViewChangeListener: onMusicPlayViewChangeListener = " + onMusicPlayViewChangeListener);
        if (musicPlayViewChangeListenerList != null) {
            musicPlayViewChangeListenerList.add(onMusicPlayViewChangeListener);
        }
    }

    public void removeMusicPlayViewChangeListener(OnMusicPlayViewChangeListener
                                                          onMusicPlayViewChangeListener) {
        Log.d(TAG, "removeMusicPlayViewChangeListener: onMusicPlayViewChangeListener = " + onMusicPlayViewChangeListener);
        if (musicPlayViewChangeListenerList != null) {
            musicPlayViewChangeListenerList.remove(onMusicPlayViewChangeListener);
        }
    }

    public interface OnMusicPlayViewChangeListener {
        void musicPlayViewChangeListener();
    }
}
