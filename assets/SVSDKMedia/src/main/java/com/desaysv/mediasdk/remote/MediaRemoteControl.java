package com.desaysv.mediasdk.remote;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.mediasdk.bean.AudioSourceOriginBean;
import com.desaysv.mediasdk.bean.MediaInfoBean;
import com.desaysv.mediasdk.listener.IMediaControl;
import com.desaysv.mediasdk.listener.IMediaFunction;
import com.desaysv.mediasdk.listener.IMediaInfoCallBack;

import java.util.HashMap;

/**
 * Created by LZM on 2020-5-1
 * Comment 媒体控制器，将音源转化为对应的媒体控制器
 */
public class MediaRemoteControl implements IMediaControl {

    private static final String TAG = "MediaRemoteControl";

    private static IMediaControl instance;

    public static IMediaControl getInstance() {
        if (instance == null) {
            synchronized (MediaRemoteControl.class) {
                if (instance == null) {
                    instance = new MediaRemoteControl();
                }
            }
        }
        return instance;
    }

    //集合外部音源控制器的map
    private HashMap<String, IMediaFunction> mMediaFunctionMap = new HashMap<>();


    /**
     * 初始化逻辑，初始化的时候，需要将远程的媒体控制器设置进去
     *
     * @param context 上下文
     */
    @Override
    public void initialize(Context context) {
        RadioRemoteFunction.getInstance().initialize(context);
        USBMusicRemoteFunction.getInstance().initialize(context);
    }

    /**
     * 注册媒体变化的回调广播
     *
     * @param origin                    什么类型的控制器
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    @Override
    public void registerMediaInfoCallback(String origin, IMediaInfoCallBack iRemountMediaInfoCallBack) {
        mMediaFunctionMap.get(origin).registerMediaInfoCallback(iRemountMediaInfoCallBack);
    }

    /**
     * 注销媒体变化的回调
     *
     * @param origin                    什么类型的控制器
     * @param iRemountMediaInfoCallBack 数据变化的回调
     */
    @Override
    public void unRegisterMediaInfoCallback(String origin, IMediaInfoCallBack iRemountMediaInfoCallBack) {
        mMediaFunctionMap.get(origin).unRegisterMediaInfoCallback(iRemountMediaInfoCallBack);
    }

    /**
     * 注入逻辑控制器
     *
     * @param origin             什么类型的控制器
     * @param remoteMediaManager 逻辑控制器
     */
    @Override
    public void injectControl(String origin, IMediaFunction remoteMediaManager) {
        Log.d(TAG, "injectControl: origin = " + origin);
        mMediaFunctionMap.put(origin, remoteMediaManager);
    }

    /**
     * 进行播放
     *
     * @param source 控制的音源
     */
    @Override
    public void play(String source) {
        Log.d(TAG, "play: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "play: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).play(source);
    }

    /**
     * 进行暂停
     *
     * @param source 控制的音源
     */
    @Override
    public void pause(String source) {
        Log.d(TAG, "pause: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "pause: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).pause(source);
    }

    /**
     * 进行播放或者暂停
     *
     * @param source 控制的音源
     */
    @Override
    public void playOrPause(String source) {
        Log.d(TAG, "playOrPause: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "playOrPause: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).playOrPause(source);
    }

    /**
     * 进行下一曲
     *
     * @param source 控制的音源
     */
    @Override
    public void next(String source) {
        Log.d(TAG, "next: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "next: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).next(source);
    }

    /**
     * 进行上一曲
     *
     * @param source 控制的音源
     */
    @Override
    public void pre(String source) {
        Log.d(TAG, "pre: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "pre: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).pre(source);
    }

    /**
     * 进行快进
     *
     * @param source 控制的音源
     */
    @Override
    public void startFastForward(String source) {
        Log.d(TAG, "startFastForward: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "startFastForward: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).startFastForward(source);
    }

    /**
     * 停止快进
     *
     * @param source 控制的音源
     */
    @Override
    public void stopFastForward(String source) {
        Log.d(TAG, "stopFastForward: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "stopFastForward: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).stopFastForward(source);
    }

    /**
     * 开始快退
     *
     * @param source 控制的音源
     */
    @Override
    public void startRewind(String source) {
        Log.d(TAG, "startRewind: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "startRewind: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).startRewind(source);
    }

    /**
     * 停止快退
     *
     * @param source 控制的音源
     */
    @Override
    public void stopRewind(String source) {
        Log.d(TAG, "stopRewind: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "stopRewind: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).stopRewind(source);
    }

    /**
     * 收藏逻辑
     *
     * @param source 控制的音源
     */
    @Override
    public void collect(String source) {
        Log.d(TAG, "collect: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "collect: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).collect(source);
    }

    /**
     * 切换循环模式
     *
     * @param source 控制的音源
     */
    @Override
    public void changeLoopType(String source) {
        Log.d(TAG, "changeLoopType: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "changeLoopType: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).changeLoopType(source);
    }

    /**
     * 根据选择的音源获取该音源的ID3信息
     *
     * @param source 需要获取的音源
     * @return ID3信息的json数据
     */
    @Override
    public String getCurrentPlayInfo(String source) {
        Log.d(TAG, "getCurrentPlayInfo: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCurrentPlayInfo: remote control is null");
            return null;
        }
        String playInfo = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getCurrentPlayInfo(source);
        Log.d(TAG, "getCurrentPlayInfo: playInfo = " + playInfo);
        return playInfo;
    }

    /**
     * 根据选择的音源获取该源的播放状态
     *
     * @param source 需要获取的音源
     * @return 播放状态的json数据
     */
    @Override
    public String getCurrentPlayStatus(String source) {
        Log.d(TAG, "getCurrentPlayStatus: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCurrentPlayStatus: remote control is null");
            return null;
        }
        String playStatus = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getCurrentPlayStatus(source);
        Log.d(TAG, "getCurrentPlayStatus: playStatus = " + playStatus);
        return playStatus;
    }

    /**
     * 根据选择的音源获取当前的播放时间
     *
     * @param source 需要获取的音源
     * @return 播放时间的json数据
     */
    @Override
    public String getCurrentPlayTime(String source) {
        Log.d(TAG, "getCurrentPlayTime: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCurrentPlayTime: remote control is null");
            return null;
        }
        String playTime = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getCurrentPlayTime(source);
        Log.d(TAG, "getCurrentPlayTime: playTime = " + playTime);
        return playTime;
    }

    /**
     * 获取专辑封面图片
     *
     * @return 专辑封面数据
     */
    @Override
    public byte[] getAlbumPicData(String source) {
        Log.d(TAG, "getCurrentPlayTime: source = " + source);
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCurrentPlayTime: remote control is null");
            return null;
        }
        byte[] picData = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getPicData(source);
        Log.d(TAG, "getCurrentPlayTime: playTime = " + picData);
        return picData;
    }

    /**
     * 启动对应的音源
     *
     * @param context      上下文
     * @param source       选择的音源
     * @param isForeground true：前台 false：后台
     */
    @Override
    public void openSource(Context context, String source, boolean isForeground, String openReason) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "openSource: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).openSource(context, source, isForeground, openReason);
    }

    /**
     * 获取收藏状态
     *
     * @param source 需要获取的音源
     * @return 收音的收藏状态
     */
    @Override
    public String getCollectStatus(String source) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCollectStatus: remote control is null");
            return null;
        }
        String collectStatus = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getCollectStatus(source);
        Log.d(TAG, "getCollectStatus: collectStatus = " + collectStatus);
        return collectStatus;
    }

    /**
     * 获取循环模式
     *
     * @param source 需要获取的音源
     * @return USB音乐的循环状态
     */
    @Override
    public String getLoopType(String source) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getLoopType: remote control is null");
            return null;
        }
        String loopType = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getLoopType(source);
        Log.d(TAG, "getLoopType: loopType = " + loopType);
        return loopType;
    }


    @Override
    public void openSourceWithFlag(String source, boolean isForeground, String openReason, int flag) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "openSourceWithFlag: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).openSourceWithFlag(source, isForeground, openReason,flag);
    }

    @Override
    public void setBand(String source,String band) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "setBand: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).setBand(source, band);
    }

    @Override
    public void setMedia(String source, MediaInfoBean mediaInfoBean) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "setMedia: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).setMedia(source, mediaInfoBean);
    }

    @Override
    public void startAst(String source) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "startAst: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).startAst(source);
    }

    @Override
    public void stopAst(String source) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "stopAst: remote control is null");
            return;
        }
        mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).stopAst(source);
    }

    @Override
    public void setMediaSettings(String source, String type, int value) {
        //预留接口
    }

    @Nullable
    @Override
    public String getCurrentSearchStatus(String source) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCurrentSearchStatus: remote control is null");
            return null;
        }
        String searchStatus = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getCurrentSearchStatus(source);
        Log.w(TAG, "getCurrentSearchStatus，searchStatus："+searchStatus);
        return searchStatus;
    }

    @Nullable
    @Override
    public String getRadioFreq(String source) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getCurrentSearchStatus: remote control is null");
            return null;
        }
        String radioFreq = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getRadioFreq(source);
        Log.w(TAG, "getRadioFreq，radioFreq："+radioFreq);
        return radioFreq;
    }

    @Nullable
    @Override
    public String getRadioStatus(String source) {
        return null;
    }

    @Override
    public RemoteBeanList getRemoteList(String source, String type) {
        if (mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)) == null) {
            Log.w(TAG, "getRemoteList: remote control is null");
            return null;
        }
        RemoteBeanList remoteBeanList = mMediaFunctionMap.get(AudioSourceOriginBean.transformSource(source)).getRemoteList(source,type);
        Log.w(TAG, "getRemoteList，remoteBeanList："+remoteBeanList);
        return remoteBeanList;
    }
}
