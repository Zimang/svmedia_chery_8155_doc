package com.desaysv.svlibmediaobserver.manager;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.svlibmediaobserver.bean.MediaInfoBean;
import com.desaysv.svlibmediaobserver.iiterface.IMediaObserver;

import java.util.HashMap;

/**
 * created by znb on 2023-06-15,用于观察媒体数据变化，以实现主界面实时显示各个媒体模块播放信息的功能
 */
public class MediaObserverManager {

    private static final String TAG = "MediaObserverManager";
    private static MediaObserverManager mInstance;

    public static MediaObserverManager getInstance() {
        synchronized (MediaObserverManager.class) {
            if (mInstance == null) {
                mInstance = new MediaObserverManager();
            }
            return mInstance;

        }
    }


    private MediaInfoBean currentMediaInfo;


    private HashMap<String, IMediaObserver> observerHashMap = new HashMap<>();

    private Context context;

    public void init(Context context) {
        this.context = context;
    }


    /**
     * 注册观察者
     *
     * @param observer
     */
    public void addObserver(String name, IMediaObserver observer) {
        Log.d(TAG, "addObserver:" + name);
        observerHashMap.put(name, observer);
    }


    /**
     * 移除观察者
     *
     * @param name
     */
    public void removeObserver(String name) {
        Log.d(TAG, "removeObserver:" + name);
        observerHashMap.remove(name);
    }

    /**
     * 各个模块在Fragment界面切换时设置
     *
     * @param flag，{@link com.desaysv.svlibmediaobserver.bean.AppConstants.Page}
     */
    public void setPageChanged(int flag) {
        Log.d(TAG, "setPageChanged:" + flag);
        for (IMediaObserver observer : observerHashMap.values()) {
            observer.onPageChanged(flag);
        }
    }

    /**
     * 各个模块在播放状态变化时设置
     *
     * @param source
     * @param isPlaying
     */
    public void setPlayStatus(String source, boolean isPlaying) {
        Log.d(TAG, "setPlayStatus，source：" + source + ",isPlaying:" + isPlaying);
        if (currentMediaInfo != null && source.equals(currentMediaInfo.getSource())) {
            currentMediaInfo.setPlaying(isPlaying);
            for (IMediaObserver observer : observerHashMap.values()) {
                observer.onPlayStatusChanged(source, isPlaying);
            }
        }
    }

    /**
     * 各个模块在专辑图变化时设置
     *
     * @param source
     * @param uri
     */
    public void setAlbum(String source, String uri) {
        Log.d(TAG, "setAlbum，source：" + source + ",uri:" + uri);
        if (currentMediaInfo != null && source.equals(currentMediaInfo.getSource())) {
            currentMediaInfo.setAlbumUri(uri);
            for (IMediaObserver observer : observerHashMap.values()) {
                observer.onAlbumChanged(source, uri);
            }
        }
    }

    /**
     * 各个模块在专辑图变化时设置
     *
     * @param source
     * @param bytes
     */
    public void setAlbum(String source, byte[] bytes) {
        Log.d(TAG, "setAlbum，source：" + source + ",bytes:" + bytes);
        if (currentMediaInfo != null && source.equals(currentMediaInfo.getSource())) {
            currentMediaInfo.setBytes(bytes);
            for (IMediaObserver observer : observerHashMap.values()) {
                observer.onAlbumChanged(source, bytes);
            }
        }
    }


    /**
     * 各个模块在播放内容变化时设置
     *
     * @param source
     * @param mediaInfoBean
     * @param isRecent      是否最近播放，是的话直接更新，因为最近播放是先更新再申请的焦点
     */
    public void setMediaInfo(String source, MediaInfoBean mediaInfoBean, boolean isRecent) {
        //拥有焦点的才处理
        if (source == null || source.length() < 1) {
            Log.d(TAG, "setMediaInfo，source is null,return!!!");
            return;
        }
        int currentSourceStatus = AudioFocusUtils.getInstance().checkAudioFocusStatus(context, source);
        Log.d(TAG, "setMediaInfo，currentSourceStatus：" + currentSourceStatus + " isRecent = " + isRecent);
        if (currentSourceStatus == AudioManager.AUDIOFOCUS_GAIN
                || currentSourceStatus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                || currentSourceStatus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || isRecent
                || currentMediaInfo != null && TextUtils.equals(currentMediaInfo.getSource(), "bt_music")) {//迷你播放器最新数据是蓝牙音乐时也放行。避免蓝牙音乐在丢焦点后断开连接无法清空迷你播放器。
            Log.d(TAG, "setMediaInfo，source：" + source + ",mediaInfoBean:" + mediaInfoBean + ",isRecent:" + isRecent);
            Log.d(TAG, "setMediaInfo，currentMediaInfo：" + currentMediaInfo);
            if (!mediaInfoBean.equals(currentMediaInfo)) {//相同的不需要更新
                //不要直接赋值，会导致原来设置的播放状态和当前状态不一致
                if (!TextUtils.equals("bt_music", source)) {
                    //蓝牙需要等待音乐ID3上报，需立即更新，不适用此逻辑
                    mediaInfoBean.setPlaying(currentMediaInfo != null && currentMediaInfo.getPlaying());
                }
                currentMediaInfo = mediaInfoBean;
                Log.d(TAG, "setMediaInfo end");
                for (IMediaObserver observer : observerHashMap.values()) {
                    observer.onMediaInfoChanged(source, mediaInfoBean);
                }
            }
        }
    }

    /**
     * 获取当前播放的内容
     * 主要用于界面首次起来时，获取一下后台播放的内容，避免首次起来没有内容显示
     *
     * @return
     */
    public MediaInfoBean getCurrentMediaInfo() {
        return currentMediaInfo;
    }
}
