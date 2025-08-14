package com.desaysv.svlibusbdialog.observer;



import com.desaysv.svlibusbdialog.iinterface.IQueryMusicStateChangedListener;
import com.desaysv.svlibusbdialog.iinterface.IQueryPictureStateChangedListener;
import com.desaysv.svlibusbdialog.iinterface.IQueryVideoStateChangedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询媒体库状态的观察者
 */
public class QueryStateObserver {

    private static final String TAG = "QueryStateObserver";
    private static QueryStateObserver mInstance;

    public static QueryStateObserver getInstance(){
        if (mInstance == null){
            synchronized (QueryStateObserver.class){
                if (mInstance == null){
                    mInstance = new QueryStateObserver();
                }
            }
        }
        return mInstance;
    }

    private Map<String, IQueryMusicStateChangedListener> listenerMusicMap = new HashMap<>();
    private Map<String, IQueryPictureStateChangedListener> listenerPictureMap = new HashMap<>();
    private Map<String, IQueryVideoStateChangedListener> listenerVideoMap = new HashMap<>();

    /**
     * 注册Music观察者
     * @param clkName
     */
    public void attachMusicObserver(String clkName, IQueryMusicStateChangedListener listener){
        if (!listenerMusicMap.containsKey(clkName)){
            listenerMusicMap.put(clkName,listener);
        }

    }

    /**
     * 注销观察者
     * @param clkName
     */
    public void detachMusicObserver(String clkName){
        listenerMusicMap.remove(clkName);
    }

    /**
     * 注册Picture观察者
     * @param clkName
     */
    public void attachPictureObserver(String clkName, IQueryPictureStateChangedListener listener){
        if (!listenerPictureMap.containsKey(clkName)){
            listenerPictureMap.put(clkName,listener);
        }

    }

    /**
     * 注销观察者
     * @param clkName
     */
    public void detachPictureObserver(String clkName){
        listenerPictureMap.remove(clkName);
    }

    /**
     * 注册Video观察者
     * @param clkName
     */
    public void attachVideoObserver(String clkName, IQueryVideoStateChangedListener listener){
        if (!listenerVideoMap.containsKey(clkName)){
            listenerVideoMap.put(clkName,listener);
        }

    }

    /**
     * 注销观察者
     * @param clkName
     */
    public void detachVideoObserver(String clkName){
        listenerVideoMap.remove(clkName);
    }


    /**
     * 通知Music观察者数据变化
     */
    public void notifyMusicObserver(){
        for (IQueryMusicStateChangedListener listener : listenerMusicMap.values()){
            listener.onQueryMusicStateUpdate();
        }
    }

    /**
     * 通知Picture观察者数据变化
     */
    public void notifyPictureObserver(){
        for (IQueryPictureStateChangedListener listener : listenerPictureMap.values()){
            listener.onQueryPictureStateUpdate();
        }
    }

    /**
     * 通知Video观察者数据变化
     */
    public void notifyVideoObserver(){
        for (IQueryVideoStateChangedListener listener : listenerVideoMap.values()){
            listener.onQueryVideoStateUpdate();
        }
    }

}
