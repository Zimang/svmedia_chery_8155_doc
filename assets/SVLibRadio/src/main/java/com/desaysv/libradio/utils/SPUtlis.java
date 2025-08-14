package com.desaysv.libradio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SPUtlis {
    private static SPUtlis instance;
    private static final String TAG = "SPUtlis";
    private static final String RADIO_PLAY_STATUS_KEY = "RADIO_PLAY_STATUS";
    private static final String SHOW_COLLECT_LIST_MODE_KEY = "SHOW_COLLECT_LIST_MODE_KEY";
    public static SPUtlis getInstance() {
        if (instance == null) {
            synchronized (SPUtlis.class) {
                if (instance == null) {
                    instance = new SPUtlis();
                }
            }
        }
        return instance;
    }
    private SharedPreferences spMedia;
    public  SharedPreferences.Editor edMedia;
    private Context mContext;

    public void initialize(Context context) {
        mContext = context;
        spMedia = mContext.getSharedPreferences("collectMode", Context.MODE_PRIVATE);
        edMedia = spMedia.edit();
    }

    public void Clear(){
        Log.d(TAG, "Clear");
       edMedia.clear();
       edMedia.commit();
    }

    /**
     * 电台播放状态持久化
     *
     * @param isPlay    isPlay
     */
    public void saveRadioPlayPauseStatus(boolean isPlay) {
        Log.d(TAG, "saveRadioPlayPauseStatus: isPlay = " + isPlay);
        if (edMedia != null) {
            edMedia.putBoolean(RADIO_PLAY_STATUS_KEY, isPlay);
            edMedia.apply();
        }
    }

    /**
     * 获取电台暂停播放持久化的状态
     *
     * @return PlayStatus
     */
    public boolean getRadioPlayPauseStatus() {
        Log.d(TAG, "getRadioPlayPauseStatus: spMedia != null = " + (spMedia != null));
        if (spMedia != null) {
            boolean isPlaying = spMedia.getBoolean(RADIO_PLAY_STATUS_KEY, false);
            Log.d(TAG, "getRadioPlayPauseStatus: isPlaying = " + isPlaying);
            return isPlaying;
        }
        return false;
    }

    /**
     * 从收藏列表进入，播放列表显示收藏列表
     * @param isShowCollectListMode
     */
    public void saveShowCollectListMode(boolean isShowCollectListMode) {
        Log.d(TAG, "saveShowCollectListMode:  "+ isShowCollectListMode);
        edMedia.putBoolean(SHOW_COLLECT_LIST_MODE_KEY, isShowCollectListMode);
        edMedia.apply();
    }

    public boolean getIsShowCollectListMode() {
        boolean isShowCollectListMode = false;
        if (spMedia != null) {
            isShowCollectListMode = spMedia.getBoolean(SHOW_COLLECT_LIST_MODE_KEY, false);
        }
        Log.d(TAG, "getIsShowCollectListMode: " + isShowCollectListMode);
        return isShowCollectListMode;
    }
}
