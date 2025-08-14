package com.desaysv.modulebtmusic.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.interfaces.listener.IBTMusicListener;
import com.desaysv.modulebtmusic.manager.BTMusicDataServiceManager;
import com.desaysv.modulebtmusic.manager.BTMusicManager;

import java.io.Serializable;
import java.util.List;

public abstract class BTMusicBaseFragment extends BaseFragment implements IBTMusicListener {
    private static final String TAG = "BTMusicBaseFragment";
    private static final String BUNDLE_SERIALIZABLE = "BUNDLE_SERIALIZABLE";
    private static final String BUNDLE_STRING = "BUNDLE_STRING";
    private static final String BUNDLE_INT = "BUNDLE_INT";
    private static final int MSG_UPDATE_A2DP_CONNECTION_STATE = 1;
    private static final int MSG_UPDATE_AVRCP_CONNECTION_STATE = 2;
    private static final int MSG_UPDATE_MUSIC_PLAY_INFO = 3;
    private static final int MSG_UPDATE_MUSIC_PLAY_STATE = 4;
    private static final int MSG_UPDATE_MUSIC_PLAY_PROGRESS = 5;
    private static final int MSG_UPDATE_MUSIC_PLAY_LIST = 6;

    private static boolean isForeground = false;

    protected abstract void updateA2DPConnectionState(String address, int state);

    protected abstract void updateAVRCPConnectionState(String address, int state);

    protected abstract void updateMusicPlayInfo(SVMusicInfo musicInfo);

    protected abstract void updateMusicPlayState(int state);

    protected abstract void updateMusicPlayProgress(long progress, long duration);

    protected abstract void updateMusicPlayList(List<SVMusicInfo> list);

    public static boolean isForeground() {
        return isForeground;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        initManager();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
        BTMusicDataServiceManager.getInstance().trackEventOpen(BTMusicDataServiceManager.VALUE_POWER_CLICK,
                BTMusicDataServiceManager.VALUE_OPER_CLICK);
    }

    @Override
    public void onPause() {
        super.onPause();
        isForeground = false;
        BTMusicDataServiceManager.getInstance().trackEventClose(BTMusicDataServiceManager.VALUE_POWER_CLICK, BTMusicDataServiceManager.VALUE_OPER_CLICK);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        releaseManager();
        mMainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onConnectionStateChanged(int profile, String address, int state) {
        Log.i(TAG, "onConnectionStateChanged: profile=" + profile + ", address=" + address + ", state=" + state);
        Message msg = Message.obtain();
        if (profile == Constants.ProfileType.A2DP_SINK) {
            msg.what = MSG_UPDATE_A2DP_CONNECTION_STATE;
        } else if (profile == Constants.ProfileType.AVRCP_CONTROLLER) {
            msg.what = MSG_UPDATE_AVRCP_CONNECTION_STATE;
        } else {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_STRING, address);
        bundle.putInt(BUNDLE_INT, state);
        msg.setData(bundle);
        mMainHandler.sendMessage(msg);
    }

    @Override
    public void onMusicPlayInfoChanged(SVMusicInfo musicInfo) {
        Log.i(TAG, "onMusicPlayInfoChanged: getMediaTitle=" + (musicInfo == null ? "null!" : musicInfo.getMediaTitle()));
        if (musicInfo == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_SERIALIZABLE, musicInfo);
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_MUSIC_PLAY_INFO;
        msg.setData(bundle);
        mMainHandler.sendMessage(msg);
    }

    @Override
    public void onMusicPlayStateChanged(int state) {
        Log.i(TAG, "onMusicPlayStateChanged: state=" + state);
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_INT, state);
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_MUSIC_PLAY_STATE;
        msg.setData(bundle);
        mMainHandler.sendMessage(msg);
    }

    @Override
    public void onMusicPlayProgressChanged(long progress, long duration) {
        Log.i(TAG, "onMusicPlayProgressChanged: progress=" + progress + ",duration=" + duration);
        SVMusicInfo musicInfo = new SVMusicInfo();
        musicInfo.setProgress(progress);
        musicInfo.setDuration(duration);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_SERIALIZABLE, musicInfo);
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_MUSIC_PLAY_PROGRESS;
        msg.setData(bundle);
        mMainHandler.sendMessage(msg);
    }

    @Override
    public void onMusicPlayListChanged() {
        List<SVMusicInfo> list = BTMusicManager.getInstance().getMusicInfoList();
        Log.d(TAG, "onMusicPlayListChanged: list.size=" + (list == null ? "null!" : list.size()));
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_SERIALIZABLE, (Serializable) list);
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_MUSIC_PLAY_LIST;
        msg.setData(bundle);
        mMainHandler.sendMessage(msg);
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case MSG_UPDATE_A2DP_CONNECTION_STATE:
                    String address = bundle.getString(BUNDLE_STRING);
                    int state = bundle.getInt(BUNDLE_INT);
                    updateA2DPConnectionState(address, state);
                    break;
                case MSG_UPDATE_AVRCP_CONNECTION_STATE:
                    address = bundle.getString(BUNDLE_STRING);
                    state = bundle.getInt(BUNDLE_INT);
                    updateAVRCPConnectionState(address, state);
                    break;
                case MSG_UPDATE_MUSIC_PLAY_INFO:
                    SVMusicInfo musicInfo = (SVMusicInfo) msg.getData().getSerializable(BUNDLE_SERIALIZABLE);
                    updateMusicPlayInfo(musicInfo);
                    break;
                case MSG_UPDATE_MUSIC_PLAY_STATE:
                    state = bundle.getInt(BUNDLE_INT);
                    updateMusicPlayState(state);
                    break;
                case MSG_UPDATE_MUSIC_PLAY_PROGRESS:
                    musicInfo = (SVMusicInfo) msg.getData().getSerializable(BUNDLE_SERIALIZABLE);
                    if (musicInfo != null) {
                        updateMusicPlayProgress(musicInfo.getProgress(), musicInfo.getDuration());
                    }
                    break;
                case MSG_UPDATE_MUSIC_PLAY_LIST:
                    List<SVMusicInfo> list = (List<SVMusicInfo>) msg.getData().getSerializable(BUNDLE_SERIALIZABLE);
                    updateMusicPlayList(list);
                    break;
            }
        }
    };

    protected boolean isA2DPConnected() {
        boolean isConnected = BTMusicManager.getInstance().isA2DPConnected();
        Log.d(TAG, "isA2DPConnected: isConnected = " + isConnected);
        return isConnected;
    }

    protected void switchPlayState() {
        Log.d(TAG, "switchPlayState");
        SVMusicInfo musicInfo = BTMusicManager.getInstance().getMusicPlayInfo();
        if (musicInfo == null) {
            Log.i(TAG, "switchPlayState: musicInfo == null, play");
            BTMusicManager.getInstance().play();
            BTMusicDataServiceManager.getInstance().trackEventPlay(BTMusicDataServiceManager.VALUE_OPER_CLICK);
        } else if (BTMusicManager.getInstance().isPlayingState(musicInfo.getPlayState())) {
            Log.i(TAG, "switchPlayState: switch to pause");
            BTMusicManager.getInstance().pause();
            BTMusicDataServiceManager.getInstance().trackEventPause(BTMusicDataServiceManager.VALUE_OPER_CLICK);
        } else {
            Log.i(TAG, "switchPlayState: switch to play");
            BTMusicManager.getInstance().play();
            BTMusicDataServiceManager.getInstance().trackEventPlay(BTMusicDataServiceManager.VALUE_OPER_CLICK);
        }
    }

    protected void skipToNext() {
        Log.d(TAG, "skipToNext");
        BTMusicManager.getInstance().skipToNext();
        BTMusicDataServiceManager.getInstance().trackEventNext(BTMusicDataServiceManager.VALUE_OPER_CLICK);
    }

    protected void skipToPrevious() {
        Log.d(TAG, "skipToPrevious");
        BTMusicManager.getInstance().skipToPrevious();
        BTMusicDataServiceManager.getInstance().trackEventPrevious(BTMusicDataServiceManager.VALUE_OPER_CLICK);
    }

    protected void requestBTMusicAudioFocus() {
        Log.d(TAG, "requestBTMusicAudioFocus");
        BTMusicManager.getInstance().requestBTMusicAudioFocus();
    }

    private void initManager() {
        Log.d(TAG, "releaseManager");
        BTMusicManager.getInstance().registerListener(this, false);
    }

    private void releaseManager() {
        Log.d(TAG, "releaseManager");
        BTMusicManager.getInstance().unregisterListener(this);
    }
}
