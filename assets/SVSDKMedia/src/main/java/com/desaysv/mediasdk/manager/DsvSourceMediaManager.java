package com.desaysv.mediasdk.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.mediasdk.bean.AudioSourceOriginBean;
import com.desaysv.mediasdk.listener.IMediaInfoCallBack;
import com.desaysv.mediasdk.remote.MediaRemoteControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2020-5-2
 * Comment
 */
public class DsvSourceMediaManager implements ISourceMediaManager {

    private static final String TAG = "DsvSourceMediaManager";

    @SuppressLint("StaticFieldLeak")
    private static ISourceMediaManager instance;

    public static ISourceMediaManager getInstance() {
        if (instance == null) {
            synchronized (DsvSourceMediaManager.class) {
                if (instance == null) {
                    instance = new DsvSourceMediaManager();
                }
            }
        }
        return instance;
    }

    private Context mContext;

    private DsvSourceMediaManager() {

    }

    /**
     * 媒体数据的变化回调
     * 这里的回调是全量注册，但是加了过滤逻辑，只有当前音源的才会回调
     */
    private List<IMediaInfoCallBack> mCurrentMediaInfoCallBacks = new ArrayList<>();

    /**
     * 初始化媒体外部的媒体控制器,必须使用Application的Context
     *
     * @param context 上下文
     */
    @Override
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        mContext = context;
        MediaRemoteControl.getInstance().initialize(context);
    }

    /**
     * 根据当前音源控制播放
     */
    @Override
    public void play() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "play: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().play(currentSource);
    }

    /**
     * 根据当前音源进行暂停
     */
    @Override
    public void pause() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "pause: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().pause(currentSource);
    }

    /**
     * 根据当前音源进行播放或者暂停
     */
    @Override
    public void playOrPause() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "playOrPause: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().playOrPause(currentSource);
    }

    /**
     * 根据当前音源进行下一曲
     */
    @Override
    public void next() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "next: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().next(currentSource);
    }

    /**
     * 根据当前音源进行上一曲
     */
    @Override
    public void pre() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "pre: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().pre(currentSource);
    }

    /**
     * 根据当前音源进行快进
     */
    @Override
    public void startFastForward() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "startFastForward: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().startFastForward(currentSource);
    }

    /**
     * 根据当前音源停止快进
     */
    @Override
    public void stopFastForward() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "stopFastForward: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().stopFastForward(currentSource);
    }

    /**
     * 根据当前音源开始快退
     */
    @Override
    public void startRewind() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "startRewind: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().startRewind(currentSource);
    }

    /**
     * 根据当前音源停止快退
     */
    @Override
    public void stopRewind() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "stopRewind: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().stopRewind(currentSource);
    }

    /**
     * 收藏逻辑
     */
    @Override
    public void collect() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "collect: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().collect(currentSource);
    }

    /**
     * 切换循环模式
     */
    @Override
    public void changeLoopType() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "changeLoopType: currentSource = " + currentSource);
        MediaRemoteControl.getInstance().changeLoopType(currentSource);
    }

    /**
     * 根据当前音源获取该音源的ID3信息
     *
     * @return ID3信息的json数据
     */
    @Nullable
    @Override
    public String getCurrentPlayInfo() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "getCurrentPlayInfo: currentSource = " + currentSource);
        return MediaRemoteControl.getInstance().getCurrentPlayInfo(currentSource);
    }

    /**
     * 根据当前音源获取该源的播放状态
     *
     * @return 播放状态的json数据
     */
    @Nullable
    @Override
    public String getCurrentPlayStatus() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "getCurrentPlayStatus: currentSource = " + currentSource);
        return MediaRemoteControl.getInstance().getCurrentPlayStatus(currentSource);

    }

    /**
     * 根据当前音源获取当前的播放时间
     *
     * @return 播放时间的json数据
     */
    @Nullable
    @Override
    public String getCurrentPlayTime() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "getCurrentPlayTime: currentSource = " + currentSource);
        return MediaRemoteControl.getInstance().getCurrentPlayTime(currentSource);
    }

    /**
     * 根据当前的音源获取专辑封面
     *
     * @return 专辑封面数据
     */
    @Override
    public byte[] getCurrentAlbumPic() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "getCurrentPlayTime: currentSource = " + currentSource);
        return MediaRemoteControl.getInstance().getAlbumPicData(currentSource);
    }

    /**
     * 启动对应的音源
     *
     * @param context      上下文
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     * @param openReason   启动的原因
     */
    @Override
    public void openSource(Context context, String source, boolean isForeground, String openReason) {
        Log.d(TAG, "openSource: source = " + source + " isForeground = " + isForeground);
        MediaRemoteControl.getInstance().openSource(context, source, isForeground, openReason);
    }

    /**
     * 获取收藏状态，只针对于收音
     *
     * @return 收音的收藏状态
     */
    @Override
    public String getCollectStatus() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "getCollectStatus: currentSource = " + currentSource);
        return MediaRemoteControl.getInstance().getCollectStatus(currentSource);
    }

    /**
     * 获取循环模式
     *
     * @return USB音乐的循环状态
     */
    @Override
    public String getLoopType() {
        String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
        Log.d(TAG, "getLoopType: currentSource = " + currentSource);
        return MediaRemoteControl.getInstance().getLoopType(currentSource);
    }

    /**
     * 注册当前音频焦点数据的变化回调
     *
     * @param iMediaInfoCallBack 数据变化的回调
     */
    @Override
    public void registerCurrentMediaInfoCallback(IMediaInfoCallBack iMediaInfoCallBack) {
        Log.d(TAG, "registerMediaInfoCallback: ");
        if (iMediaInfoCallBack != null) {
            mCurrentMediaInfoCallBacks.remove(iMediaInfoCallBack);
            mCurrentMediaInfoCallBacks.add(iMediaInfoCallBack);
        }
        registerAllMediaInfoCallBack();
    }

    /**
     * 注销媒体变化的回调广播
     *
     * @param iMediaInfoCallBack 数据变化的回调
     */
    @Override
    public void unRegisterCurrentMediaInfoCallback(IMediaInfoCallBack iMediaInfoCallBack) {
        Log.d(TAG, "unRegisterMediaInfoCallback: ");
        if (iMediaInfoCallBack != null) {
            mCurrentMediaInfoCallBacks.remove(iMediaInfoCallBack);
        }
        unRegisterAllMediaInfoCallBack();
    }


    /**
     * 注册全部的媒体数据类型的变化回调
     * 这个方法的触发条件
     * 只要监听当前音源的数据变化的回调一旦不为空，就要注册
     */
    private void registerAllMediaInfoCallBack() {
        boolean isEmpty = mCurrentMediaInfoCallBacks.isEmpty();
        Log.d(TAG, "registerAllMediaInfoCallBack: isEmpty = " + isEmpty);
        if (!isEmpty) {
            //注册音乐部分的数据回调
            MediaRemoteControl.getInstance().registerMediaInfoCallback(AudioSourceOriginBean.MUSIC_ORIGIN, mediaInfoCallBack);
            //注册电台部分的数据回调
            MediaRemoteControl.getInstance().registerMediaInfoCallback(AudioSourceOriginBean.RADIO_ORIGIN, mediaInfoCallBack);
        }
    }

    /**
     * 注销全部的媒体数据类型的变化回调
     * 这个方法只要一旦监听当前媒体数据变化的回调一旦为空，就需要注销
     */
    private void unRegisterAllMediaInfoCallBack() {
        boolean isEmpty = mCurrentMediaInfoCallBacks.isEmpty();
        Log.d(TAG, "unRegisterAllMediaInfoCallBack: isEmpty = " + isEmpty);
        if (isEmpty) {
            //注销音乐部分的数据回调
            MediaRemoteControl.getInstance().unRegisterMediaInfoCallback(AudioSourceOriginBean.MUSIC_ORIGIN, mediaInfoCallBack);
            //注销电台播放的数据回调
            MediaRemoteControl.getInstance().unRegisterMediaInfoCallback(AudioSourceOriginBean.RADIO_ORIGIN, mediaInfoCallBack);
        }
    }


    /**
     * 媒体状态变化的数据回调
     */
    private IMediaInfoCallBack mediaInfoCallBack = new IMediaInfoCallBack() {
        @Override
        public void onMediaInfoChange(String source, String mediaInfo) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onMediaInfoChange: source = " + source + " currentSource = " + currentSource + " callback size = " + mCurrentMediaInfoCallBacks.size());
            Log.d(TAG, "onMediaInfoChange: mediaInfo = " + mediaInfo);
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onMediaInfoChange(source, mediaInfo);
            }
        }

        /**
         * 专辑图片发生改变的时候触发的回调
         *
         * @param source  变化的音源
         * @param picInfo 变化的图片数据
         */
        @Override
        public void onMediaPicChange(String source, byte[] picInfo) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onMediaPicChange: source = " + source + " callback size = " + mCurrentMediaInfoCallBacks.size());
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onMediaPicChange(source, picInfo);
            }
        }

        @Override
        public void onMediaPlayStatusChange(String source, String playInfo) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onMediaPlayStatusChange: source = " + source + " callback size = " + mCurrentMediaInfoCallBacks.size());
            Log.d(TAG, "onMediaPlayStatusChange: playInfo = " + playInfo);
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onMediaPlayStatusChange(source, playInfo);
            }
        }

        @Override
        public void onMediaPlayTimeChange(String source, String timeInfo) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onMediaPlayTimeChange: source = " + source + " callback size = " + mCurrentMediaInfoCallBacks.size());
            Log.d(TAG, "onMediaPlayTimeChange: timeInfo = " + timeInfo);
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onMediaPlayTimeChange(source, timeInfo);
            }
        }

        /**
         * 电台的收藏状态发送改变会触发的回调
         *
         * @param source      变化的音源
         * @param collectInfo 变化消息的json数据
         */
        @Override
        public void onRadioCollectStatusChange(String source, String collectInfo) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onRadioCollectStatusChange: source = " + source + " callback size = " + mCurrentMediaInfoCallBacks.size());
            Log.d(TAG, "onRadioCollectStatusChange: collectInfo = " + collectInfo);
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onRadioCollectStatusChange(source, collectInfo);
            }
        }

        /**
         * USB音乐的循环模式发生改变触发的回调
         *
         * @param source   变化的音源
         * @param loopInfo 变化的循环模式
         */
        @Override
        public void onMusicLoopTypeChange(String source, String loopInfo) {
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onUSBMusicLoopTypeChange: source = " + source + " callback size = " + mCurrentMediaInfoCallBacks.size());
            Log.d(TAG, "onUSBMusicLoopTypeChange: loopInfo = " + loopInfo);
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onMusicLoopTypeChange(source, loopInfo);
            }
        }

        /**
         * AIDL服务连接成功之后，需要将当前播放的信息全部更新
         *
         * @param source 数据变化的音源
         */
        @Override
        public void onServiceConnect(String source) {
            Log.d(TAG, "onServiceConnectInfoInitialize:  callback size = " + mCurrentMediaInfoCallBacks.size());
            String currentSource = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(mContext);
            Log.d(TAG, "onServiceConnectInfoInitialize: source = " + source + " currentSource =  " + currentSource);
            //如果当前音源不是回调的音源，就进行过滤
            if (!currentSource.equals(source)) {
                return;
            }
            for (IMediaInfoCallBack iMediaInfoCallBack : mCurrentMediaInfoCallBacks) {
                iMediaInfoCallBack.onServiceConnect(source);
            }
        }


        @Override
        public void onMediaSearchStatusChange(String source, String searchInfo) {

        }

        @Override
        public void onMediaSeekStatusChange(String source, String seekInfo) {

        }

        @Override
        public void onMediaSettingsChange(String source, String settingsInfo) {

        }

        @Override
        public void onRemoteListChanged(String source, String type, RemoteBeanList remoteList) {

        }
    };

}
