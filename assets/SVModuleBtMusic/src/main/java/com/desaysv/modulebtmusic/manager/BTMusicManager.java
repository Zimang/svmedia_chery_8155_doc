package com.desaysv.modulebtmusic.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.bt.VDEventBT;
import com.desaysv.ivi.vdb.event.id.bt.VDValueBT;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTConnect;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTListProgress;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTMusicInfo;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTMusicPlay;
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTMusicProgress;
import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.bean.SVDataListBean;
import com.desaysv.modulebtmusic.bean.SVDevice;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.interfaces.listener.IBTMusicListener;
import com.desaysv.modulebtmusic.interfaces.listener.IMusicInfoDownloadListener;
import com.desaysv.modulebtmusic.interfaces.manager.IBTMusicManager;
import com.desaysv.modulebtmusic.service.BTMusicService;
import com.desaysv.modulebtmusic.utils.MultiTaskTimer;
import com.desaysv.modulebtmusic.utils.ObserverBuilder;
import com.desaysv.modulebtmusic.utils.ParseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BTMusicManager
 * 通用蓝牙音乐管理类
 */
public class BTMusicManager implements IBTMusicManager, MultiTaskTimer.ITimerTaskHandler {
    private static final String TAG = Constants.TAG + "BTMusicManager";
    private static volatile BTMusicManager mInstance;
    private Context mContext;

    private final ObserverBuilder<IBTMusicListener> mObserverBuilder = new ObserverBuilder<>();

    private MultiTaskTimer mMultiTaskTimer;
    private final ExecutorService mDataSingleThreadPool = Executors.newSingleThreadExecutor();
    private final ExecutorService mSingleThreadPool = Executors.newSingleThreadExecutor();
    private final ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(2);
    private static final int TIME_OUT = 3000;

    private static final List<SVMusicInfo> mMusicInfoList = new ArrayList<>();
    private static final SVDataListBean<SVMusicInfo> mMusicInfoListBean = new SVDataListBean<>();
    private SVMusicInfo mSVMusicInfo;

    private IMusicInfoDownloadListener mMusicInfoDownloadListener;

    private boolean mIsRePlayBtMusic = true;

    public static BTMusicManager getInstance() {
        if (mInstance == null) {
            synchronized (BTMusicManager.class) {
                if (mInstance == null) {
                    mInstance = new BTMusicManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void initialize(Context context) {
        Log.d(TAG, "initialize: ");
        mContext = context;
        context.startForegroundService(new Intent(mContext, BTMusicService.class));
        VDBus.getDefault().init(context);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.BT);
        VDBus.getDefault().registerVDBindListener(mVdBindListener);
        subscribeEvents();
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);//注册订阅事件

        mDataSingleThreadPool.execute(() -> {
            mSVMusicInfo = getRemoteMusicPlayInfo();
            updateMusicInfoList(loadMusicInfo());
            mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IBTMusicListener>) observer -> {
                observer.onMusicPlayInfoChanged(mSVMusicInfo);
            });
        });

        mMultiTaskTimer = new MultiTaskTimer(this);
        Log.d(TAG, "initialize: finish");
    }

    /**
     * 释放资源
     */
    public void release() {
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);//反注册事件订阅监听
        VDBus.getDefault().unregisterVDBindListener(mVdBindListener);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.BT);
        VDBus.getDefault().release();

        mSVMusicInfo = null;
        updateMusicInfoList(null);
        Log.d(TAG, "release: finish");
    }

    private VDBindListener mVdBindListener = new VDBindListener() {
        @Override
        public void onVDConnected(VDServiceDef.ServiceType serviceType) {
            if (serviceType == VDServiceDef.ServiceType.BT) {
                Log.i(TAG, "onVDConnected: bindService success");
            }
        }

        @Override
        public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
            if (serviceType == VDServiceDef.ServiceType.BT) {
                Log.i(TAG, "onVDDisconnected: bindService failed");
                VDBus.getDefault().bindService(VDServiceDef.ServiceType.BT);
            }
        }
    };

    private void subscribeEvents() {
        VDBus.getDefault().addSubscribe(VDEventBT.SETTING_CONNECT_A2DP);
        VDBus.getDefault().addSubscribe(VDEventBT.SETTING_CONNECT_AVRCP);
        VDBus.getDefault().addSubscribe(VDEventBT.MUSIC_INFO);
        VDBus.getDefault().addSubscribe(VDEventBT.MUSIC_PROGRESS);
        VDBus.getDefault().addSubscribe(VDEventBT.MUSIC_LIST);
        VDBus.getDefault().addSubscribe(VDEventBT.MUSIC_LIST_SYNC);
        VDBus.getDefault().subscribeCommit();
    }

    @Override
    public void registerListener(IBTMusicListener btMusicListener, boolean isRemote) {
        mObserverBuilder.registerObserver(btMusicListener);
    }

    @Override
    public void unregisterListener(IBTMusicListener btMusicListener) {
        mObserverBuilder.unregisterObserver(btMusicListener);
    }

    @Override
    public void registerAudioResumeService(String packageName, String className) {//do nothing
    }

    @Override
    public void unregisterAudioResumeService(String packageName, String className) {//do nothing
    }

    @Override
    public boolean isA2DPConnected() {
        int svConnectStatus = ParseUtils.getSVConnectStatus(BaseConstants.ProfileType.A2DP_SINK);
        return svConnectStatus == BaseConstants.ProfileConnectionState.STATE_CONNECTED;
    }

    @Override
    public boolean isAVRCPConnected() {
        int svConnectStatus = ParseUtils.getSVConnectStatus(BaseConstants.ProfileType.AVRCP_CONTROLLER);
        return svConnectStatus == BaseConstants.ProfileConnectionState.STATE_CONNECTED;
    }

    @Override
    public int getConnectionState(String address) {
        return ParseUtils.getSVConnectStatus(BaseConstants.ProfileType.A2DP_SINK);
    }

    @Override
    public String getConnectedAddress() {
        return ParseUtils.getConnectedAddress(BaseConstants.ProfileType.A2DP_SINK);
    }

    @Override
    public SVDevice getConnectedDevice() {
        return ParseUtils.getConnectedDevice(BaseConstants.ProfileType.A2DP_SINK);
    }

    @Override
    public List<SVDevice> getConnectedDeviceList() {
        return ParseUtils.getSVConnectedList(BaseConstants.ProfileType.A2DP_SINK);
    }

    @Override
    public SVMusicInfo getMusicPlayInfo() {//返回当前记录的SVMusicInfo，并非通过VDB拿远端数据
        return mSVMusicInfo;
    }

    @Override
    public SVMusicInfo getRemoteMusicPlayInfo() {//通过VDB拿远端数据
        VDEvent event = VDBus.getDefault().getOnce(VDEventBT.MUSIC_INFO);
        VDBTMusicInfo musicInfo = VDBTMusicInfo.getValue(event);
        if (musicInfo == null) {
            Log.w(TAG, "getRemoteMusicPlayInfo: musicInfo == null");
            return null;
        }
        return ParseUtils.parseVDBTMusicInfo(musicInfo);
    }

    @Override
    public List<SVMusicInfo> getMusicInfoList() {
        return mMusicInfoList;
    }

    @Override
    public SVDataListBean<SVMusicInfo> getMusicInfoListBean() {
        return mMusicInfoListBean;
    }

    @Override
    public boolean connect(String address) {//do nothing
        return false;
    }

    @Override
    public boolean disconnect(String address) {//do nothing
        return false;
    }

    @Override
    public void playFromMediaId(String mediaId, Bundle extras) {
        playFromPlaylist(mediaId, extras, VDValueBT.MusicPlayType.FROM_MEDIA_ID);
    }

    @Override
    public void playFromSearch(String query, Bundle extras) {
        playFromPlaylist(query, extras, VDValueBT.MusicPlayType.FROM_SEARCH);
    }

    @Override
    public void playFromUri(Uri uri, Bundle extras) {
        playFromPlaylist(uri, extras, VDValueBT.MusicPlayType.FROM_URI);
    }

    @Override
    public void play() {
        setPlayOperation(VDValueBT.MusicPlayCtrl.PLAY);
    }

    @Override
    public void pause() {
        setPlayOperation(VDValueBT.MusicPlayCtrl.PAUSE);
    }

    @Override
    public void stop() {
        setPlayOperation(VDValueBT.MusicPlayCtrl.STOP);
    }

    @Override
    public void skipToNext() {
        setPlayOperation(VDValueBT.MusicPlayCtrl.NEXT);
    }

    @Override
    public void skipToPrevious() {
        setPlayOperation(VDValueBT.MusicPlayCtrl.PREVIOUS);
    }

    @Override
    public void fastForward(boolean isFastForward) {
        if (isFastForward) {
            setPlayOperation(VDValueBT.MusicPlayCtrl.FORWARD_START);
        } else {
            setPlayOperation(VDValueBT.MusicPlayCtrl.FORWARD_END);
        }
    }

    @Override
    public void fastRewind(boolean isFastRewind) {
        if (isFastRewind) {
            setPlayOperation(VDValueBT.MusicPlayCtrl.REWIND_START);
        } else {
            setPlayOperation(VDValueBT.MusicPlayCtrl.REWIND_END);
        }
    }

    @Override
    public void requestBTMusicAudioFocus() {
        Log.d(TAG, "requestBTMusicAudioFocus: ");
        VDEvent event = new VDEvent(VDEventBT.MUSIC_AUDIO_FOCUS_REQUEST);
        VDBus.getDefault().set(event);
    }

    @Override
    public boolean syncPlayFromMediaId(String mediaId, Bundle extras, boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncPlayFromSearch(String query, Bundle extras, boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncPlayFromUri(Uri uri, Bundle extras, boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncPlay(boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncPause(boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncSkipToNext(boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncSkipToPrevious(boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncFastForward(boolean isFastForward, boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public boolean syncFastRewind(boolean isFastRewind, boolean isRequestAudioFocus) {//do nothing
        return false;
    }

    @Override
    public int syncRequestBTMusicAudioFocus() {//do nothing
        return 0;
    }

    @Override
    public void setA2DPMuteState(boolean isMute) {
        Log.d(TAG, "setA2DPMuteState: isMute = " + isMute);
        Bundle payload = new Bundle();
        payload.putInt(VDKey.STATUS, isMute ? VDValueBT.MusicMuteMode.MUTE : VDValueBT.MusicMuteMode.UNMUTE);
        VDEvent event = new VDEvent(VDEventBT.MUSIC_MUTE_MODE, payload);
        VDBus.getDefault().set(event);
    }

    @Override
    public boolean isA2DPMute() {
        VDEvent event = VDBus.getDefault().getOnce(VDEventBT.MUSIC_MUTE_MODE);
        if (event != null && event.getPayload() != null) { // 服务未绑定，会返回null
            int mode = event.getPayload().getInt(VDKey.STATUS);
            Log.d(TAG, "isA2DPMute: mode = " + mode);
            return mode == VDValueBT.MusicMuteMode.MUTE;
        }
        return false;
    }

    @Override
    public void loadMusicInfoList(IMusicInfoDownloadListener listener) {//同步蓝牙音乐播放列表数据，在MUSIC_LIST事件中回调数据上来
        if (listener != null) {
            mMusicInfoDownloadListener = listener;
        }
        VDEvent event = new VDEvent(VDEventBT.MUSIC_LIST_SYNC);
        VDBus.getDefault().set(event);
    }

    @Override
    public int getPlayerSettings() {
        VDEvent event = VDBus.getDefault().getOnce(VDEventBT.MUSIC_SETTINGS);
        return event.getPayload().getInt(VDKey.TYPE);
    }

    @Override
    public boolean setPlayerSettings(int type, int state) {
        Log.d(TAG, "setPlayerSettings: type = " + type + ", state = " + state);
        Bundle payload = new Bundle();
        payload.putInt(VDKey.TYPE, type);
        payload.putInt(VDKey.STATUS, state);
        VDEvent event = new VDEvent(VDEventBT.MUSIC_SETTINGS, payload);
        VDBus.getDefault().set(event);
        return true;
    }

    private void playFromPlaylist(Object obj, Bundle extras, int eventId) {
        Log.d(TAG, "playFromPlayList: eventId = " + eventId);
        VDBTMusicPlay param = new VDBTMusicPlay();
        param.putPlayCtrl(VDValueBT.MusicPlayCtrl.PLAY);
        param.putPlayType(eventId);
        switch (eventId) {
            case VDValueBT.MusicPlayType.FROM_MEDIA_ID:
                param.putMediaId((String) obj);
                break;
            case VDValueBT.MusicPlayType.FROM_SEARCH:
                param.putQuery((String) obj);
                break;
            case VDValueBT.MusicPlayType.FROM_URI:
                param.putUri((Uri) obj);
                break;
        }
        param.putExtras(extras);
        VDEvent event = VDBTMusicPlay.createEvent(VDEventBT.MUSIC_PLAY, param);
        VDBus.getDefault().set(event);
    }

    private void setPlayOperation(int eventId) {
        Log.d(TAG, "setPlayOperation: eventId = " + eventId);
        VDBTMusicPlay param = new VDBTMusicPlay();
        param.putPlayCtrl(eventId);
        param.putPlayType(VDValueBT.MusicPlayType.DEFAULT);
        VDEvent event = VDBTMusicPlay.createEvent(VDEventBT.MUSIC_PLAY, param);
        VDBus.getDefault().set(event);
    }

    private synchronized List<SVMusicInfo> loadMusicInfo() {
        Log.i(TAG, "loadMusicInfo");
        AtomicInteger count = new AtomicInteger();
        List<SVMusicInfo> list = new ArrayList<>();
        AtomicBoolean isException = new AtomicBoolean(false);
        AtomicBoolean isLoadFinish = new AtomicBoolean(false);
        IMusicInfoDownloadListener listener = (musicInfoList, index, total) -> {
            Log.d(TAG, "loadMusicInfo: musicInfoList.size=" + musicInfoList.size() + ",index=" + index + ",total=" + total);
            count.set(0);
            if (total > 0) {
                list.addAll(musicInfoList);
                if (index >= total - 1) {
                    isLoadFinish.set(true);
                }
            } else if (total < 0) {
                isException.set(true);
            } else {
                isLoadFinish.set(true);
            }
        };
        loadMusicInfoList(listener);
        int sleepTime = 50;
        int maxCount = TIME_OUT / sleepTime;
        while (!isLoadFinish.get()) {
            if (isException.get()) {
                Log.w(TAG, "loadMusicInfo: Data Exception!");
                return null;
            }
            if (count.getAndIncrement() > maxCount) {
                Log.w(TAG, "loadMusicInfo: TimeOut!");
                return null;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "loadMusicInfo: list.size=" + list.size());
        return list;
    }

    private void updateMusicInfoList(List<SVMusicInfo> list) {
        synchronized (mMusicInfoList) {
            mMusicInfoList.clear();
            if (list != null) {
                mMusicInfoList.addAll(list);
            }
        }
        synchronized (mMusicInfoListBean) {
            mMusicInfoListBean.setList(mMusicInfoList);
            mMusicInfoListBean.setSuccess(list == null ? false : true);
        }
    }

    private VDNotifyListener mVDNotifyListener = (vdEvent, threadType) -> {
        if (vdEvent == null) {
            Log.w(TAG, "onVDNotify: vdEvent is null");
            return;
        }
        if (threadType == VDThreadType.MAIN_THREAD) {
            switch (vdEvent.getId()) {
                case VDEventBT.SETTING_CONNECT_A2DP:
                    notifyConnectionStateChanged(vdEvent, BaseConstants.ProfileType.A2DP_SINK);
                    break;
                case VDEventBT.SETTING_CONNECT_AVRCP:
                    notifyConnectionStateChanged(vdEvent, BaseConstants.ProfileType.AVRCP_CONTROLLER);
                    break;
                case VDEventBT.MUSIC_INFO:
                    notifyMusicPlayInfoChanged(vdEvent);
                    break;
                case VDEventBT.MUSIC_PROGRESS:
                    notifyMusicPlayProgressChanged(vdEvent);
                    break;
                case VDEventBT.MUSIC_LIST:
                    notifyMusicPlayListUpdate(vdEvent);
                    break;
                case VDEventBT.MUSIC_LIST_SYNC:
                    notifyMusicPlayListChanged(vdEvent);
                    break;
            }
        }
    };

    private void notifyConnectionStateChanged(VDEvent event, int profileType) {
        VDBTConnect vdbtConnect = VDBTConnect.getValue(event);
        if (vdbtConnect == null) {
            Log.i(TAG, "notifyConnectionStateChanged: vdbtConnect == null");
            return;
        }
        int svConnectStatus = ParseUtils.parseVDProfileConnectionState(vdbtConnect.getConnectStatus());
        Log.d(TAG, "notifyConnectionStateChanged: svConnectStatus = " + svConnectStatus);
        mSingleThreadPool.execute(() -> {
            if (profileType == Constants.ProfileType.A2DP_SINK) {
                if (svConnectStatus == Constants.ProfileConnectionState.STATE_DISCONNECTED) {
                    mSVMusicInfo = new SVMusicInfo();
                    updateMusicInfoList(null);
                }
            }
            mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IBTMusicListener>) observer -> {
                observer.onConnectionStateChanged(profileType, vdbtConnect.getMac(), svConnectStatus);
            });
        });
    }

    private void notifyMusicPlayInfoChanged(VDEvent event) {
        VDBTMusicInfo vdbtMusicInfo = VDBTMusicInfo.getValue(event);
        if (vdbtMusicInfo == null) {
            Log.i(TAG, "notifyMusicPlayInfoChanged: vdbtMusicInfo == null");
            return;
        }
        SVMusicInfo svMusicInfo = ParseUtils.parseVDBTMusicInfo(vdbtMusicInfo);
        if (svMusicInfo == null) {
            Log.w(TAG, "notifyMusicPlayInfoChanged: svMusicInfo == null");
            return;
        }
        Log.d(TAG, "notifyMusicPlayInfoChanged: svMusicInfo: " + svMusicInfo.toString());
        mSingleThreadPool.execute(() -> {
            mSVMusicInfo = svMusicInfo;
            mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IBTMusicListener>) observer -> {
                observer.onMusicPlayInfoChanged(svMusicInfo);
            });
        });
        mSingleThreadPool.execute(() -> {
            if (mSVMusicInfo != null) {
                mSVMusicInfo.setPlayState(svMusicInfo.getPlayState());
            }
            mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IBTMusicListener>) observer -> {
                observer.onMusicPlayStateChanged(svMusicInfo.getPlayState());
            });
        });
    }

    private void notifyMusicPlayProgressChanged(VDEvent event) {
        VDBTMusicProgress vdbtMusicProgress = VDBTMusicProgress.getValue(event);
        if (vdbtMusicProgress == null) {
            Log.i(TAG, "notifyMusicPlayProgressChanged: vdbtMusicProgress == null");
            return;
        }
        Log.d(TAG, "notifyMusicPlayProgressChanged: Progress = " + vdbtMusicProgress.getPosition() + ", Duration = " + vdbtMusicProgress.getDuration());
        if (mSVMusicInfo != null) {
            mSVMusicInfo.setProgress(vdbtMusicProgress.getPosition());
            mSVMusicInfo.setDuration(vdbtMusicProgress.getDuration());
        }
        mFixedThreadPool.execute(() -> {
            mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IBTMusicListener>) observer -> {
                observer.onMusicPlayProgressChanged(vdbtMusicProgress.getPosition(), vdbtMusicProgress.getDuration());
            });
        });
    }

    private void notifyMusicPlayListUpdate(VDEvent event) {
        ArrayList<VDBTMusicInfo> list = VDBTMusicInfo.getList(event);
        VDBTListProgress progress = VDBTListProgress.getValue(event);
        if (list == null) {
            Log.w(TAG, "notifyMusicPlayListChanged: list == null");
            return;
        }
        if (progress == null) {
            Log.w(TAG, "notifyMusicPlayListChanged: progress == null");
            return;
        }
        Log.d(TAG, "notifyMusicPlayListChanged: list.size = " + list.size() + ", progress = " + progress.toString());
        ArrayList<SVMusicInfo> musicInfoList = new ArrayList<>();
        for (VDBTMusicInfo musicInfo : list) {
            SVMusicInfo svMusicInfo = ParseUtils.parseVDBTMusicInfo(musicInfo);
            musicInfoList.add(svMusicInfo);
        }
        if (mMusicInfoDownloadListener != null) {
            mMusicInfoDownloadListener.onDownloadProgress(musicInfoList, progress.getIndex(), progress.getTotal());
        }
    }

    private void notifyMusicPlayListChanged(VDEvent event) {
        mDataSingleThreadPool.execute(() -> {
            updateMusicInfoList(loadMusicInfo());
            mObserverBuilder.notifyObservers((ObserverBuilder.IListener<IBTMusicListener>) IBTMusicListener::onMusicPlayListChanged);
        });
    }

    public boolean setTimeTask(int taskId, int cyclicTime) {
        if (mMultiTaskTimer != null && mMultiTaskTimer.isTaskExist(taskId)) {
            Log.w(TAG, "taskId = " + taskId + ", anti shake");
            return false;
        }
        if (mMultiTaskTimer != null) {
            mMultiTaskTimer.setTimeTask(taskId, cyclicTime);
        }
        return true;
    }

    @Override
    public void onTimerTaskHandle(int taskId) {
        if (mMultiTaskTimer != null) {
            mMultiTaskTimer.cancelTimeTask(taskId);
        }
    }

    public void setReplayBtMusicFlag(boolean isReplayBtMusic) {
        mIsRePlayBtMusic = isReplayBtMusic;
    }

    public boolean getReplayBtMusicFlag() {
        Log.i(TAG, "getReplayBtMusicFlag: mIsRePlayBtMusic = " + mIsRePlayBtMusic);
        return mIsRePlayBtMusic;
    }

    public boolean isPlayingState(int playState) {
        return playState == Constants.PlayState.STATE_PLAYING
                || playState == Constants.PlayState.STATE_FAST_FORWARDING
                || playState == Constants.PlayState.STATE_BUFFERING;
    }
}