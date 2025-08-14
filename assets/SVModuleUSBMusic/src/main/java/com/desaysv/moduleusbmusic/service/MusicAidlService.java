package com.desaysv.moduleusbmusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.localmediasdk.IAIDLMediaInfoCallback;
import com.desaysv.localmediasdk.IAIDLMediaManager;
import com.desaysv.localmediasdk.bean.LocalMediaConstants;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.localmediasdk.bean.StartSourceIntentBean;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.ServiceUtils;
import com.desaysv.moduleusbmusic.businesslogic.control.SourceResumeControl;
import com.desaysv.moduleusbmusic.utils.MusicControlUtils;
import com.desaysv.moduleusbmusic.utils.MusicStatusUtils;
import com.google.gson.Gson;

/**
 * Created by LZM on 2020-3-18
 * Comment 音乐控制的AIDL服务，用来控制音乐的上下曲，获取媒体状态和设置媒体状态回调
 * TODO：后续根据对接的module，配置对应的AIDL服务
 */
public class MusicAidlService extends Service {

    private static final String TAG = "MusicAidlService";

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceUtils.startForegroundNotification(this, "AIDLAudioFocusService", "AIDLAudioFocusService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    /**
     * IPC
     */
    private final IAIDLMediaManager.Stub iBinder = new IAIDLMediaManager.Stub() {
        /**
         * 发送控制命令过来
         * @param source 需要控制的音源
         * @param command 控制的命令
         * @throws RemoteException 远程异常
         */
        @Override
        public void sendCommand(String source, String command, String info) throws RemoteException {
            Log.d(TAG, "sendCommand: source = " + source + " command = " + command + " info = " + info);
            switch (command) {
                case LocalMediaConstants.ControlAction.PLAY:
                    MusicControlUtils.getInstance().play(source);
                    break;
                case LocalMediaConstants.ControlAction.PAUSE:
                    MusicControlUtils.getInstance().pause(source);
                    break;
                case LocalMediaConstants.ControlAction.PLAY_OR_PAUSE:
                    MusicControlUtils.getInstance().playOrPause(source);
                    break;
                case LocalMediaConstants.ControlAction.NEXT:
                    MusicControlUtils.getInstance().next(source);
                    break;
                case LocalMediaConstants.ControlAction.PRE:
                    MusicControlUtils.getInstance().pre(source);
                    break;
                case LocalMediaConstants.ControlAction.SEEK_TO:
                    MusicControlUtils.getInstance().seekTo(source, info);
                    break;
                case LocalMediaConstants.ControlAction.START_FAST_FORWARD:
                    MusicControlUtils.getInstance().startFastForward(source);
                    break;
                case LocalMediaConstants.ControlAction.STOP_FAST_FORWARD:
                    MusicControlUtils.getInstance().stopFastForward(source);
                    break;
                case LocalMediaConstants.ControlAction.START_REWIND:
                    MusicControlUtils.getInstance().startRewind(source);
                    break;
                case LocalMediaConstants.ControlAction.STOP_REWIND:
                    MusicControlUtils.getInstance().stopRewind(source);
                    break;
                case LocalMediaConstants.ControlAction.CHANGE_LOOP_TYPE:
                    MusicControlUtils.getInstance().changeLoopType(source, info);
                    break;
                case LocalMediaConstants.ControlAction.OPEN_SOURCE:
                    Gson gson = new Gson();
                    MediaInfoBean mediaInfoBean = gson.fromJson(info, MediaInfoBean.class);
                    String openReason = mediaInfoBean.getOpenReason();
                    if (StartSourceIntentBean.OPEN_REASON_BOOT_RESUME.equals(openReason)) {
                        SourceResumeControl.getInstance().openSource(source, mediaInfoBean.isForeground(), mediaInfoBean.getFlag(), ChangeReasonData.BOOT_RESUME);
                    } else if (StartSourceIntentBean.OPEN_REASON_MODE.equals(openReason)) {
                        SourceResumeControl.getInstance().openSource(source, mediaInfoBean.isForeground(), mediaInfoBean.getFlag(), ChangeReasonData.MODE_CONTROL);
                    } else {
                        SourceResumeControl.getInstance().openSource(source, mediaInfoBean.isForeground(), mediaInfoBean.getFlag(), ChangeReasonData.AIDL);
                    }
                    break;
            }
        }

        /**
         * 获取对应选种的信息
         * @param source 对应的音源
         * @param type 需要获取的数据
         * @return 对应信息的json数据
         * @throws RemoteException 远程服务异常
         */
        @Override
        public String getSelectInfo(String source, String type) throws RemoteException {
            Log.d(TAG, "getSelectInfo: source = " + source + " type = " + type);
            String info = "";
            switch (type) {
                case LocalMediaConstants.StatusAction.PLAY_INFO:
                    info = MusicStatusUtils.getInstance().getCurrentPlayInfo(source);
                    break;
                case LocalMediaConstants.StatusAction.PLAY_STATUS:
                    info = MusicStatusUtils.getInstance().getCurrentPlayStatus(source);
                    break;
                case LocalMediaConstants.StatusAction.PLAY_TIME:
                    info = MusicStatusUtils.getInstance().getCurrentPlayTime(source);
                    break;
                case LocalMediaConstants.StatusAction.LOOP_TYPE:
                    info = MusicStatusUtils.getInstance().getLoopType(source);
                    break;
                case LocalMediaConstants.StatusAction.DEVICE_STATUS:
                    info = MusicStatusUtils.getInstance().getDeviceConnectState(source);
                    break;
                case LocalMediaConstants.StatusAction.COLLECT_STATUS:
                    info = MusicStatusUtils.getInstance().getCollectStatus(source);
                    break;
            }
            return info;
        }

        /**
         * 获取图片数据
         * @param source 对应的音源
         * @return 图片的byte数据
         * @throws RemoteException 远程服务异常
         */
        @Override
        public byte[] getCurrentPic(String source) throws RemoteException {
            Log.d(TAG, "getCurrentPic: source = " + source);
            return MusicStatusUtils.getInstance().getPicData(source);
        }

        /**
         * 注册数据状态的变化回调
         * @param packageName 对应的包名
         * @param mediaInfoCallback 回调
         * @throws RemoteException 远程服务异常
         */
        @Override
        public void registerMediaInfoCallback(String packageName, IAIDLMediaInfoCallback mediaInfoCallback) throws RemoteException {
            Log.d(TAG, "registerMediaInfoCallback: packageName = " + packageName);
            MusicStatusUtils.getInstance().setMusicCallback(packageName, mediaInfoCallback);
        }

        /**
         * 注销数据状态的变化回调
         * @param packageName 对应的包名
         * @param aileMediaInfoCallBack 回调
         * @throws RemoteException 远程服务异常
         */
        @Override
        public void unregisterMediaInfoCallback(String packageName, IAIDLMediaInfoCallback aileMediaInfoCallBack) throws RemoteException {
            Log.d(TAG, "unregisterMediaInfoCallback: packageName = " + packageName);
            MusicStatusUtils.getInstance().removeMusicCallback(packageName, aileMediaInfoCallBack);
        }

        @Override
        public RemoteBeanList getRemoteList(String source, String type) throws RemoteException {
            return null;
        }
    };
}
