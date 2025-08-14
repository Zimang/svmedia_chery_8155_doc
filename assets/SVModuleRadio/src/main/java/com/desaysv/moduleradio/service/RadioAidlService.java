package com.desaysv.moduleradio.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.localmediasdk.IAIDLMediaInfoCallback;
import com.desaysv.localmediasdk.IAIDLMediaManager;
import com.desaysv.localmediasdk.bean.LocalMediaConstants;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.ServiceUtils;
import com.desaysv.moduledab.service.DABPopupService;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.utils.ClickUtils;
import com.desaysv.moduleradio.utils.RadioControlUtils;
import com.desaysv.moduleradio.utils.RadioStatusUtils;
import com.desaysv.svlibtoast.ToastUtil;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;

/**
 * Created by LZM on 2020-3-18
 * Comment 收音的AIDL通讯服务
 * TODO：后续根据配置的module，选择AIDL服务
 */
public class RadioAidlService extends Service {

    private static final String TAG = "RadioAidlService";

    private RadioCollectFullHandler radioCollectFullHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        ServiceUtils.startForegroundNotification(this, "AIDLAudioFocusService", "AIDLAudioFocusService");
        radioCollectFullHandler = new RadioCollectFullHandler(this);
        RadioControlUtils.getInstance().setRadioCollectFullCallback(new RadioControlUtils.IRadioCollectFullCallback() {
            @Override
            public void onRadioCollectFull() {
                radioCollectFullHandler.sendEmptyMessage(RadioCollectFullHandler.MSG_FULLCOLLECT_TOAST);
            }

            @Override
            public void onRadioCancelCollect() {
                radioCollectFullHandler.sendEmptyMessage(RadioCollectFullHandler.MSG_CANCELCOLLECT_TOAST);
            }
        });
    }

    //外部点击收藏时，会toast提示，
    //如果此时界面没有起来，因为是子线程调用过来的，会导致无法toast
    //因此用回调的方式放到主线程
    private static class RadioCollectFullHandler extends Handler {
        public static final int MSG_FULLCOLLECT_TOAST = 0;
        public static final int MSG_CANCELCOLLECT_TOAST = 1;
        private WeakReference<RadioAidlService> weakReference;

        public RadioCollectFullHandler(RadioAidlService radioAidlService) {
            weakReference = new WeakReference<>(radioAidlService);
        }

        @Override
        public void handleMessage(@NonNull Message msg){
            switch (msg.what){
                case MSG_FULLCOLLECT_TOAST:
                    weakReference.get().showRadioCollectFullToast();
                    break;
                case MSG_CANCELCOLLECT_TOAST:
                    weakReference.get().showRadioCancelCollectToast();
                    break;
                default:
                    break;
            }
        }
    }

    public void showRadioCollectFullToast(){
        Log.d(TAG,"showRadioCollectFullToast");
        ToastUtil.showToast(AppBase.mContext, AppBase.mContext.getResources().getString(R.string.dab_collect_fully));
    }

    public void showRadioCancelCollectToast(){
        Log.d(TAG,"showRadioCollectFullToast");
        ToastUtil.showToast(AppBase.mContext, AppBase.mContext.getResources().getString(R.string.dab_cancel_collected));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    private IAIDLMediaManager.Stub iBinder = new IAIDLMediaManager.Stub() {

        /**
         * 远程发送控制逻辑会触发到这里
         * @param source 需要控制的音源
         * @param command 控制的命令
         * @throws RemoteException
         */
        @Override
        public void sendCommand(String source, String command, String info) throws RemoteException {
            Log.d(TAG, "sendCommand: source = " + source + " command = " + command + " info = " + info);
            switch (command) {
                case LocalMediaConstants.ControlAction.PLAY:
                    RadioControlUtils.getInstance().play(source);
                    break;
                case LocalMediaConstants.ControlAction.PAUSE:
                    RadioControlUtils.getInstance().pause(source);
                    break;
                case LocalMediaConstants.ControlAction.PLAY_OR_PAUSE:
                    RadioControlUtils.getInstance().playOrPause(source);
                    break;
                case LocalMediaConstants.ControlAction.NEXT:
                    if (!ClickUtils.isAllowClick()) {
                        return;
                    }
                    RadioControlUtils.getInstance().next(source);
                    break;
                case LocalMediaConstants.ControlAction.PRE:
                    if (!ClickUtils.isAllowClick()) {
                        return;
                    }
                    RadioControlUtils.getInstance().pre(source);
                    break;
                case LocalMediaConstants.ControlAction.CHANGE_COLLECT:
                    RadioControlUtils.getInstance().changeCollect(source);
                    break;
                case LocalMediaConstants.ControlAction.START_FAST_FORWARD:
                    RadioControlUtils.getInstance().stepForward(source);
                    break;
                case LocalMediaConstants.ControlAction.START_REWIND:
                    RadioControlUtils.getInstance().stepBackward(source);
                    break;
                case LocalMediaConstants.ControlAction.START_AST:
                    RadioControlUtils.getInstance().startAst(source);
                    break;
                case LocalMediaConstants.ControlAction.STOP_AST:
                    RadioControlUtils.getInstance().stopAst(source);
                    break;
                case LocalMediaConstants.ControlAction.OPEN_SOURCE:
                    Gson gson = new Gson();
                    MediaInfoBean mediaInfoBean = gson.fromJson(info, MediaInfoBean.class);
                    RadioControlUtils.getInstance().openSource(source, mediaInfoBean.isForeground(), mediaInfoBean.getFlag(),mediaInfoBean.getOpenReason());
                    break;
                case LocalMediaConstants.ControlAction.SET_MEDIA:
                    RadioControlUtils.getInstance().play(source, info);
                    break;
            }
        }

        /**
         * 获取AIDL选中的数据
         * @param source 对应的音源
         * @param type 需要获取的是那种类型的数据
         * @return 对应的json数据
         * @throws RemoteException 远程异常
         */
        @Override
        public String getSelectInfo(String source, String type) throws RemoteException {
            Log.d(TAG, "getSelectInfo: source = " + source + " type = " + type);
            String info = "";
            if (!DsvAudioSDKConstants.AM_SOURCE.equals(source) && !DsvAudioSDKConstants.FM_SOURCE.equals(source) && !DsvAudioSDKConstants.DAB_SOURCE.equals(source)
                    && !DsvAudioSDKConstants.LOCAL_RADIO_SOURCE.equals(source)) {
                Log.e(TAG, "getSelectInfo: source is not the radio source");
                return info;
            }
            switch (type) {
                case LocalMediaConstants.StatusAction.PLAY_INFO:
                    info = RadioStatusUtils.getInstance().getCurrentPlayInfo(source);
                    break;
                case LocalMediaConstants.StatusAction.PLAY_STATUS:
                    info = RadioStatusUtils.getInstance().getCurrentPlayStatus(source);
                    break;
                case LocalMediaConstants.StatusAction.COLLECT_STATUS:
                    info = RadioStatusUtils.getInstance().getCurrentRadioMessageCollectStatus(source);
                    break;
                case LocalMediaConstants.StatusAction.SEARCH_STATUS:
                    info = RadioStatusUtils.getInstance().getCurrentSearchStatus(source);
                    break;
                case LocalMediaConstants.StatusAction.RADIO_FREQ:
                    info = RadioStatusUtils.getInstance().getRadioFreq(source);
                    break;
            }
            Log.d(TAG, "getSelectInfo: info = " + info);
            return info;
        }

        /**
         * 获取图片数据，由于电台是不需要将图片数据外传的，所以返回一个空的byte数组
         * @param source 获取数据对应的音源
         * @return 空的byte数组
         * @throws RemoteException 远程异常
         */
        @Override
        public byte[] getCurrentPic(String source) throws RemoteException {
            return RadioControlUtils.getInstance().getCurrentPic(source);
        }

        /**
         * 注册媒体数据状态的变化回调
         * @param packageName 注册应用的包名
         * @param aidlMediaInfoCallback 回调
         * @throws RemoteException 远程服务的异常
         */
        @Override
        public void registerMediaInfoCallback(String packageName, IAIDLMediaInfoCallback aidlMediaInfoCallback) throws RemoteException {
            Log.d(TAG, "registerMediaInfoCallback: packageName = " + packageName);
            RadioStatusUtils.getInstance().setRadioCallback(packageName, aidlMediaInfoCallback);
        }

        /**
         * 注销媒体服务的数据状态变化的回调
         * @param packageName 注册应用的包名
         * @param aileMediaInfoCallBack 回调
         * @throws RemoteException 远程服务的异常
         */
        @Override
        public void unregisterMediaInfoCallback(String packageName, IAIDLMediaInfoCallback aileMediaInfoCallBack) throws RemoteException {
            Log.d(TAG, "unregisterMediaInfoCallback: packageName = " + packageName);
            RadioStatusUtils.getInstance().removeRadioCallback(packageName, aileMediaInfoCallBack);
        }

        @Override
        public RemoteBeanList getRemoteList(String source, String type) throws RemoteException {
            return RadioStatusUtils.getInstance().getRemoteList(source, type);
        }
    };

}
