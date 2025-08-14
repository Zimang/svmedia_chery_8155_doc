package com.desaysv.moduleusbvideo.trigger;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.carstate.VDEventCarState;
import com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.control.MediaControlRegistrar;
import com.desaysv.libusbmedia.control.SVEnterStrControl;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.libusbmedia.interfaze.IRequestMediaPlayer;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoListSearchTrigger;
import com.desaysv.moduleusbvideo.mediasession.MediaSessionController;
import com.desaysv.moduleusbvideo.ui.BaseVideoPlayActivity;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.moduleusbvideo.util.LionPointSdkUtil;
import com.desaysv.moduleusbvideo.util.MediaStatusChangeListenerImpl;
import com.desaysv.moduleusbvideo.util.MediaVideoConstantUtils;
import com.desaysv.moduleusbvideo.vr.VideoVRControl;
import com.desaysv.svlibmediastore.query.VideoQuery;
import com.desaysv.svlibmediastore.receivers.MediaScanStateManager;

import java.util.Arrays;

/**
 * Created by uidp5370 on 2019-6-3.
 * USB视频的module启动器，在application启动的时候调用
 */
public class ModuleUSBVideoTrigger {
    private static final String TAG = "ModuleUSBVideoTrigger";

    private static final class InstanceHolder {
        static final ModuleUSBVideoTrigger instance = new ModuleUSBVideoTrigger();
    }

    public static ModuleUSBVideoTrigger getInstance() {
        return InstanceHolder.instance;
    }

    private ModuleUSBVideoTrigger() {

    }

    public BaseVideoPlayActivity baseVideoPlayActivity;

    /* USB1视频的控制获取器，将这个对象抽出，为了可以全局控制，其初始化是在界面初始化的，所以可能为空 */
    @Nullable
    public IGetControlTool getUSB1VideoControlTool;

    /* USB1视频的控制器 */
    @Nullable
    public IControlTool USB1VideoControlTool;

    /* USB1视频的状态获取器 */
    @Nullable
    public IStatusTool USB1VideoStatusTool;


    /* USB2视频的控制获取器，将这个对象抽出，为了可以全局控制，其初始化是在界面初始化的，所以可能为空 */
    @Nullable
    public IGetControlTool getUSB2VideoControlTool;

    /* USB2视频的控制器 */
    @Nullable
    public IControlTool USB2VideoControlTool;

    /*  USB2视频的状态获取器 */
    @Nullable
    public IStatusTool USB2VideoStatusTool;

    /**
     * 初始化逻辑，在application启动的时候就会调用，然后初始化列表获取
     */
    public void initialize(Context mContext) {
        Log.d(TAG, "initialize: Constant.isRtl: " + Constant.isRtl());
        // 获取挡位初始化
        CarInfoProxy.getInstance().init(mContext);

        CarConfigUtil.getDefault().init(mContext);
        Constant.initRunningSpeed();
        // 初始化数据埋点
        LionPointSdkUtil.getInstance().initialize(mContext);
        VDBus.getDefault().init(mContext);
        MediaVideoConstantUtils.getInstance().initialize(mContext);
        //数据-U盘扫描
        USBVideoListSearchTrigger.getInstance().initialize(mContext);
        //初始化数据扫描
        MediaScanStateManager.getInstance().init(mContext);
        // 是否网络安全认证版本
        //boolean needCyberSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity();
        //Log.i(TAG, "initialize: needCyberSecurity: " + needCyberSecurity);
        VideoQuery.getInstance().setDataType(true);

        // 用来保存媒体的播放状态的工具类
        MediaPlayStatusSaveUtils.getInstance().initialize(mContext);

        // 初始化VR 控制器，和主动反馈播放状态
        VideoVRControl.getInstance().initialize(mContext);
        bindVDS();

        //初始化STR监听器
        SVEnterStrControl.getInstance().init(AppBase.mContext);

        //绑定USB视频的mediaSession服务，确保服务起来;
        MediaSessionController mMediaSessionController = new MediaSessionController();
        mMediaSessionController.connect(mContext);
    }

    public void unInitialize() {
        Log.d(TAG, "unInitialize: ");
        LionPointSdkUtil.getInstance().unInitialize();
        unBindVDS();
    }

    public void initUSB1VideoControl(IRequestMediaPlayer requestVideoPlayer) {
        Log.i(TAG, "initUSB1VideoControl() called with: requestVideoPlayer = [" + requestVideoPlayer + "]");
        //初始化USB1的媒体控制器，将媒体播放器回调设置进去，用来实现媒体播放器的获取实例
        IGetControlTool getUSB1VideoControlTool = MediaControlRegistrar.getInstance().registeredMediaTool(MediaType.USB1_VIDEO, requestVideoPlayer);
        getUSB1VideoControlTool.registerMediaStatusChangeListener(MediaType.USB1_VIDEO + "_INITIALIZE", new MediaStatusChangeListenerImpl(MediaType.USB1_VIDEO));
        ModuleUSBVideoTrigger.getInstance().getUSB1VideoControlTool = getUSB1VideoControlTool;
        ModuleUSBVideoTrigger.getInstance().USB1VideoControlTool = getUSB1VideoControlTool.getControlTool();
        ModuleUSBVideoTrigger.getInstance().USB1VideoStatusTool = getUSB1VideoControlTool.getStatusTool();
    }

    public void initUSB2VideoControl(IRequestMediaPlayer requestVideoPlayer) {
        Log.i(TAG, "initUSB2VideoControl() called with: requestVideoPlayer = [" + requestVideoPlayer + "]");
        //初始化USB2的媒体控制器，将媒体播放器回调设置进去，用来实现媒体播放器的获取实例
        IGetControlTool getUSB2VideoControlTool = MediaControlRegistrar.getInstance().registeredMediaTool(MediaType.USB2_VIDEO, requestVideoPlayer);
        getUSB2VideoControlTool.registerMediaStatusChangeListener(MediaType.USB2_VIDEO + "_INITIALIZE", new MediaStatusChangeListenerImpl(MediaType.USB2_VIDEO));
        ModuleUSBVideoTrigger.getInstance().getUSB2VideoControlTool = getUSB2VideoControlTool;
        ModuleUSBVideoTrigger.getInstance().USB2VideoControlTool = getUSB2VideoControlTool.getControlTool();
        ModuleUSBVideoTrigger.getInstance().USB2VideoStatusTool = getUSB2VideoControlTool.getStatusTool();
    }

    private void bindVDS() {
        Log.d(TAG, "bindVDS: ");
        VDBus.getDefault().registerVDBindListener(vdBindListener);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.CAR_STATE);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VR);
    }

    private void unBindVDS() {
        Log.d(TAG, "unBindVDS: start");
        VDBus.getDefault().removeSubscribe(VDEventCarState.POWER_STATUS);
        VDBus.getDefault().removeSubscribe(VDEventVR.VR_VIDEO);
        VDBus.getDefault().removeSubscribe(VDEventVR.VR_MEDIA);
        VDBus.getDefault().subscribeCommit();
        VDBus.getDefault().unregisterVDNotifyListener(vdNotifyListener);

        VDBus.getDefault().unregisterVDBindListener(vdBindListener);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.CAR_STATE);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.VR);
        Log.d(TAG, "unBindVDS: end");
    }

    private final VDBindListener vdBindListener = new VDBindListener() {
        @Override
        public void onVDConnected(VDServiceDef.ServiceType serviceType) {
            Log.d(TAG, "onVDConnected: serviceType " + serviceType);
            //只有绑定上了对应模块的VDS，才能进行订阅
            if (serviceType == VDServiceDef.ServiceType.CAR_STATE) {
                VDBus.getDefault().addSubscribe(VDEventCarState.POWER_STATUS);

                VDBus.getDefault().registerVDNotifyListener(vdNotifyListener);
                VDBus.getDefault().subscribeCommit();
            } else if (serviceType == VDServiceDef.ServiceType.VR) {
                VDBus.getDefault().addSubscribe(VDEventVR.VR_VIDEO, VDThreadType.MAIN_THREAD);
                VDBus.getDefault().addSubscribe(VDEventVR.VR_MEDIA, VDThreadType.MAIN_THREAD);
                VDBus.getDefault().registerVDNotifyListener(vdNotifyListener);
                VDBus.getDefault().subscribeCommit(); // 提交订阅
            }
        }

        @Override
        public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
            Log.d(TAG, "onVDDisconnected: " + serviceType);
        }
    };

    private final VDNotifyListener vdNotifyListener = new VDNotifyListener() {
        @Override
        public void onVDNotify(VDEvent vdEvent, int threadType) {
//            Log.d(TAG, "onVDNotify: vdEvent = " + vdEvent + " threadType = " + threadType);
            if (vdEvent == null || vdEvent.getPayload() == null) {
                // 服务未绑定，会返回null
                return;
            }
            int id = vdEvent.getId();
            Bundle payload = vdEvent.getPayload();
            if (id == VDEventCarState.POWER_STATUS) {
                // ACC 状态
                int powerStatus = payload.getInt(VDKey.STATUS);
                Log.d(TAG, "powerStatus:" + powerStatus);
            } else if (id == VDEventVR.VR_VIDEO || id == VDEventVR.VR_MEDIA) {
                // VR
                VDVRPipeLine param = VDVRPipeLine.getValue(vdEvent);
                // 内部Json语义表定义的Key值(参考: https://docs.qq.com/sheet/DZVBUYlBFUERNTG5q?tab=o9ktwo)
                String key = param.getKey();
                // 内部Json语义表定义的Json数据, 例: {"action":"OPEN","position":"F","type":"","value":""}
                String data = param.getValue();
                Log.d(TAG, "VR: key: " + key + ", data: " + data + ", id: " + id);
                //调用VR 控制器，反馈状态
                VideoVRControl.getInstance().onRequest(key, data, id);
            }
        }
    };

    /**
     * 示例:
     * 获取背光
     *
     * @return {@link VDEventCarState} 详见vdbus-javadoc
     */
    public int getScreenBacklight() {
        VDEvent event = VDBus.getDefault().getOnce(VDEventCarState.SCREEN_BACKLIGHT);
        if (event != null && event.getPayload() != null) {
            Log.d(TAG, "getScreenBacklight: " + event.getPayload());
            int[] data = event.getPayload().getIntArray(VDKey.DATA);
            Log.d(TAG, "getScreenBacklight: = " + Arrays.toString(data));
            if (data != null && data.length == 5 && data[4] == VDValueCarState.DisplayID.MASTER_SCREEN) {
                int value = 0;
                if (data[1] == 0) {
                    value = data[2];
                } else if (data[1] == 1) {
                    value = data[3];
                }
                return value;
            }
        }
        return -1;
    }
}
