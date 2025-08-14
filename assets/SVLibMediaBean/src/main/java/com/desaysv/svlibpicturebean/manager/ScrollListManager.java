package com.desaysv.svlibpicturebean.manager;

import android.util.Log;

import java.util.HashMap;

/**
 * 用来记忆每个页面的滚动位置
 */
public class ScrollListManager {
    private static final String TAG = "ScrollListManager";
    private static ScrollListManager mInstance;
    public static ScrollListManager getInstance() {
        synchronized (ScrollListManager.class) {
            if (mInstance == null) {
                mInstance = new ScrollListManager();
            }
            return mInstance;
        }
    }

    /**
     * 用来保持上一个目录的滚动距离
     */
    private HashMap<String, Integer> scrollOffsetList = new HashMap<String, Integer>();

    /**
     * 清空保存的数据
     */
    public void cleanOffsetList(){
        Log.d(TAG,"cleanOffsetList");
        scrollOffsetList.clear();
    }

    /**
     * 保存当前滚动距离
     * @param path 滚动时所处的路径，在当前实际是点击文件夹时，文件夹根目录的路径
     * @param offset 滚动的距离
     */
    public void saveCurrentOffset(String path, int offset){
        Log.d(TAG,"saveCurrentOffset,path: "+path+",offset: "+offset);
        scrollOffsetList.put(path,offset);
    }

    /**
     * 获取需要滚动的距离
     * @param path
     * @return
     */
    public int getOffset(String path){
        Log.d(TAG,"getOffset,path: "+path);
        int offset = scrollOffsetList.get(path);
        Log.d(TAG,"getOffset,offset: "+offset);
        return offset;
    }

}
