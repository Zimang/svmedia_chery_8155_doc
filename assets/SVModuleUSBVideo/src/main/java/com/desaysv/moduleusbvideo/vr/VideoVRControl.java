package com.desaysv.moduleusbvideo.vr;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRUpload;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IGetControlTool;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoDate;
import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.desaysv.moduleusbvideo.ui.BaseVideoPlayActivity;
import com.desaysv.moduleusbvideo.ui.USB1VideoPlayActivity;
import com.desaysv.moduleusbvideo.ui.USB2VideoPlayActivity;
import com.desaysv.moduleusbvideo.util.VideoStatusChangeUtil;
import com.desaysv.moduleusbvideo.util.listener.IVideoStatusChange;
import com.desaysv.svcommonutils.LanguageManager;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.google.gson.Gson;

import java.util.List;

public class VideoVRControl {
    private static final String TAG = "VideoVRControl";

    @SuppressLint("StaticFieldLeak")
    private static VideoVRControl videoVRControl;

    public static VideoVRControl getInstance() {
        if (null == videoVRControl) {
            videoVRControl = new VideoVRControl();
        }
        return videoVRControl;
    }

    private Context mContext;
    private Gson mGson;

    public void initialize(Context mContext) {
        Log.d(TAG, "initialize: ");
        mGson = new Gson();
        this.mContext = mContext;
        VideoStatusChangeUtil.getInstance().addVideoStatusChange(TAG, iVideoStatusChange);
    }

    private static final String VR_ACTION_KEY = "playVideo";
    private static final String VR_ACTION_KEY_MEDIA = "controlPlayState";

    private static final String VR_ACTION_KEY_COLLECT = "controlCollect";
    private static final String VR_ACTION_VALUE_COLLECT = "COLLECT";
    private static final String VR_ACTION_VALUE_CANCEL_COLLECT = "CANCEL_COLLECT";
    private static final String VR_ACTION_VALUE_COLLECT_OPEN = "OPEN";

    private static final String VR_ACTION_KEY_LIST = "controlPlayList";
    private static final String VR_ACTION_VALUE_LIST_OPEN = "OPEN";
    private static final String VR_ACTION_VALUE_LIST_CLOSE = "CLOSE";

    private static final String VR_ACTION_VALUE_PAUSE = "PAUSE";
    private static final String VR_ACTION_VALUE_PLAY = "PLAY";
    private static final String VR_ACTION_VALUE_NEXT = "NEXT";
    private static final String VR_ACTION_VALUE_PREVIOUS = "PREVIOUS";
    // 打开视频
    private static final String VR_ACTION_VALUE_ACTIVITY = "OPEN";
    // 重播
    private static final String VR_ACTION_VALUE_REPLAY = "REPEAT";

    //STATUS
    public static final String VIDEO_PLAY_STATUS_PLAYING = "playing";
    public static final String VIDEO_PLAY_STATUS_PAUSE = "paused";

    private static final String SV_VIDEO_APP_PACKAGE = "com.desaysv.videoapp";

    /**
     * 处理 VR 语音
     *
     * @param key   key
     * @param value value
     * @param id    id
     */
    public void onRequest(String key, String value, int id) {
        Log.d(TAG, "onRequest: VR: key: " + key + ", value: " + value + ", id: " + id);
        if (VDEventVR.VR_MEDIA == id) {
            // 通用语义，需要判断音源焦点再响应；
            String currentAudioSourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName(mContext);
            Log.i(TAG, "onRequest: currentAudioSourceName: " + currentAudioSourceName);
            if (!DsvAudioSDKConstants.USB0_VIDEO_SOURCE.equals(currentAudioSourceName)
                    && !DsvAudioSDKConstants.USB1_VIDEO_SOURCE.equals(currentAudioSourceName)) {
                Log.w(TAG, "onRequest: current source not video ; Do not execute !!");
                return;
            }
        }
        VRVideoActionBean valueObject = mGson.fromJson(value, VRVideoActionBean.class);
        if (null == valueObject) {
            Log.e(TAG, "onRequest: error valueObject is null");
            return;
        }
        VRVideoActionBean.SemanticBean semanticObject = valueObject.getSemantic();
        Log.d(TAG, "onRequest: semanticObject = " + semanticObject);
        if (null == semanticObject) {
            Log.e(TAG, "onRequest: error semanticObject is null");
            return;
        }
        String type = semanticObject.getType();
        String action = semanticObject.getAction();
        String resultValue = semanticObject.getValue();
        Log.d(TAG, "onRequest: key: " + key + " , type: " + type + ", action: " + action + ", resultValue: " + resultValue);
        switch (key) {
            case VR_ACTION_KEY:
            case VR_ACTION_KEY_MEDIA:
                Log.i(TAG, "onRequest: VR_ACTION_KEY or VR_ACTION_KEY_MEDIA");
                toMediaAction(action);
                break;
            case VR_ACTION_KEY_LIST:
                Log.i(TAG, "onRequest: VR_ACTION_KEY_LIST");
                toListAction(action);
                break;
            case VR_ACTION_KEY_COLLECT:
                Log.i(TAG, "onRequest: VR_ACTION_KEY_COLLECT");
                toCollectAction(action);
                break;
        }
    }

    /**
     * 操作打开视频、播放暂停、上下曲等
     *
     * @param action action
     */
    private void toMediaAction(String action) {
        Log.i(TAG, "toAction: action: " + action);
        IGetControlTool iGetControlTool = isOnVideoPlayActivity(mContext);
        if (VR_ACTION_VALUE_ACTIVITY.equals(action)) {
            int resId = R.string.media_usbvideo_openusbvideo_playing;
            List<FileMessage> usb1VideoAllList = USBVideoDate.getInstance().getUSB1VideoAllList();
            List<FileMessage> usb2VideoAllList = USBVideoDate.getInstance().getUSB2VideoAllList();
            if (usb1VideoAllList.isEmpty() && usb2VideoAllList.isEmpty()) {
                Log.i(TAG, "toAction: not video data");
                // 当前没有可播放的视频
                resId = R.string.media_usbvideo_openusbvideo_empty;
            } else if (null == iGetControlTool && !SV_VIDEO_APP_PACKAGE.equals(getTopActivityPackageName())) {
                Log.i(TAG, "toAction: to video play ");
                // 跳转到视频
                resId = R.string.media_usbvideo_openusbvideo_play;
                startActivity();
            } else if (null != iGetControlTool) {
                boolean playing = iGetControlTool.getStatusTool().isPlaying();
                if (playing) {
                    // 当前正在播放
                    Log.i(TAG, "toAction: current is playing ");
                    resId = R.string.media_usbvideo_openusbvideo_playing;
                }
            } else {
                Log.d(TAG, "toAction: current is video list page ; open ");
                resId = R.string.media_usbvideo_openusbvideo_play;
                startPlayActivity(usb1VideoAllList.get(0).getPath());
            }
            sendResult(resId);
            return;
        }
        if (null == iGetControlTool) {
            Log.e(TAG, "toAction: controlTool is null");
            //播放，不在USB视频界面上，回复语
            if (VR_ACTION_VALUE_PLAY.equals(action)) {
//                sendResult(R.string.media_usbvideo_continueplaying_empty);
                List<FileMessage> usb1VideoAllList = USBVideoDate.getInstance().getUSB1VideoAllList();
                List<FileMessage> usb2VideoAllList = USBVideoDate.getInstance().getUSB2VideoAllList();
                if (usb1VideoAllList.isEmpty() && usb2VideoAllList.isEmpty()) {
                    Log.i(TAG, "toAction: PLAY not video data");
                    // 当前没有可播放的视频
                    sendResult(R.string.media_usbvideo_openusbvideo_empty);
                } else {
                    Log.i(TAG, "toAction: PLAY to video playPage");
                    //视频未打开，有视频可播放，播放默认视频
                    startPlayActivity(usb1VideoAllList.get(0).getPath());
                }
            } else if (VR_ACTION_VALUE_REPLAY.equals(action)) {
                sendResult(R.string.media_video_replay_noplaying);
            }
            return;
        }
        Log.d(TAG, "toAction: to action: " + action);
        // 播放、暂停、上下曲等操作，只有在USB视频播放界面才进行生效；
        switch (action) {
            case VR_ACTION_VALUE_PAUSE:
                // 暂停
                iGetControlTool.getControlTool().processCommand(MediaAction.PAUSE, ChangeReasonData.VR_CONTROL);
                break;
            case VR_ACTION_VALUE_PLAY:
                // 播放
                iGetControlTool.getControlTool().processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.VR_CONTROL);
                break;
            case VR_ACTION_VALUE_NEXT:
                // 下一曲
                iGetControlTool.getControlTool().processCommand(MediaAction.NEXT, ChangeReasonData.VR_CONTROL);
                break;
            case VR_ACTION_VALUE_PREVIOUS:
                // 上一曲
                iGetControlTool.getControlTool().processCommand(MediaAction.PRE, ChangeReasonData.VR_CONTROL);
                break;
            case VR_ACTION_VALUE_REPLAY:
                // 重新播放
                boolean playing = iGetControlTool.getStatusTool().isPlaying();
                int resId;
                if (playing) {
                    resId = R.string.media_video_replay_playing;
                    iGetControlTool.getControlTool().processCommand(MediaAction.SEEKTO, ChangeReasonData.VR_CONTROL, 0);
                } else {
                    resId = R.string.media_video_replay_noplaying;
                }
                sendResult(resId);
                break;
        }
    }

    /**
     * 在播放界面，打开关闭播放列表
     *
     * @param action action
     */
    private void toListAction(String action) {
        Log.i(TAG, "toListAction: action: " + action);
        switch (action) {
            case VR_ACTION_VALUE_LIST_OPEN:
                BaseVideoPlayActivity activityOpen = ModuleUSBVideoTrigger.getInstance().baseVideoPlayActivity;
                if (null != activityOpen) {
                    activityOpen.showPlayList();
                    sendResult(R.string.general_openplaylist_open);
                } else {
                    sendResult(R.string.general_openplaylist_nosupport);
                }
                break;
            case VR_ACTION_VALUE_LIST_CLOSE:
                BaseVideoPlayActivity activityClose = ModuleUSBVideoTrigger.getInstance().baseVideoPlayActivity;
                if (null != activityClose) {
                    activityClose.dismissPlayList();
                    sendResult(R.string.general_closeplaylist_close);
                } else {
                    sendResult(R.string.general_closeplaylist_nosupport);
                }
                break;
        }
    }

    /**
     * usb视频，没有收藏相关操作指令
     *
     * @param action action
     */
    private void toCollectAction(String action) {
        Log.i(TAG, "toCollectAction: action: " + action);
        switch (action) {
            case VR_ACTION_VALUE_COLLECT:
            case VR_ACTION_VALUE_CANCEL_COLLECT:
                sendResult(R.string.general_uncollect_playing_nosupport);
                break;
            case VR_ACTION_VALUE_COLLECT_OPEN:
                sendResult(R.string.general_opencollect_nosupport);
                break;
        }
    }

    /**
     * 启动应用
     */
    private void startActivity() {
        Log.d(TAG, "startActivity: ");
        PackageManager packageManager = mContext.getPackageManager();
        Intent it = packageManager.getLaunchIntentForPackage(SV_VIDEO_APP_PACKAGE);
        if (null == it) {
            Log.e(TAG, "startActivity: intent is null for " + SV_VIDEO_APP_PACKAGE);
            return;
        }
        mContext.startActivity(it);
    }

    /**
     * 打开视频播放
     */
    private void startPlayActivity(String filePath) {
        Log.d(TAG, "startPlayActivity: ");
        PackageManager packageManager = mContext.getPackageManager();
        Intent it = packageManager.getLaunchIntentForPackage(SV_VIDEO_APP_PACKAGE);
        if (null == it) {
            Log.e(TAG, "startActivity: intent is null for " + SV_VIDEO_APP_PACKAGE);
            return;
        }
        it.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_PLAY);
        it.putExtra(Constants.FileMessageData.PATH, filePath);
        mContext.startActivity(it);
    }

    /**
     * 发送控制反馈
     */
    private void sendResult(int resId) {
        // TTS播报（set）
        VDVRPipeLine param = new VDVRPipeLine();
        param.setPkgName(mContext.getPackageName());
        param.setKey(VDValueVR.VRSemanticKey.VR_CONTROL_RESPONSE);
        String ttsString = getRightLanguageTTSString(mContext, resId);
        param.setValue(ttsString);
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_SUCCEED_AND_TTS); //根据执行结果反馈
        VDEvent event = VDVRPipeLine.createEvent(VDEventVR.VR_VIDEO, param);
        VDBus.getDefault().set(event);
        Log.d(TAG, "sendResult: end VDVRPipeLine " + param);
    }

    private String getRightLanguageTTSString(Context context, int resId) {
        Log.d(TAG, "getRightLanguageTTSString:");
        return LanguageManager.getInstance().getString(context, resId);
    }

    /**
     * 主动通知USB视频，当前播放状态
     *
     * @param status status
     */
    public void notifyCurrentVideo(boolean status) {
        Log.d(TAG, "notifyCurrentVideo: " + status);
        VRVideoResultDetails.Data data = new VRVideoResultDetails.Data();
        data.setActiveStatus(getActivityStatus());
        data.setSceneStatus(status ? VIDEO_PLAY_STATUS_PLAYING : VIDEO_PLAY_STATUS_PAUSE);
        VRVideoResultDetails responseBean = new VRVideoResultDetails();
        responseBean.setData(data);
        String uploadData = mGson.toJson(responseBean);


        VDVRUpload vdvrUpload = new VDVRUpload();
        vdvrUpload.setKey(VDValueVR.VRUploadVideoStatus.VR_PLAY_VIDEO_RESPONSE);
        vdvrUpload.setData(uploadData); // 内容
        vdvrUpload.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_NOTIFY); //数据上报
        vdvrUpload.setPkgName(SV_VIDEO_APP_PACKAGE);// 包名
        Log.d(TAG, "handleRequestStatus,uploadData:" + uploadData);
        VDEvent event = VDVRUpload.createEvent(VDEventVR.VR_DATA_UPLOAD, vdvrUpload);
        VDBus.getDefault().set(event);
    }

    /**
     * 主动通知USB视频，当前播放状态
     *
     */
    public void notifyCurrentVideo() {
        notifyCurrentVideo(getPlayState());
    }

    /**
     * 获得视频播放状态
     * @return
     */
    private boolean getPlayState() {
        IGetControlTool iGetControlTool = ModuleUSBVideoTrigger.getInstance().getUSB1VideoControlTool;
        if(iGetControlTool != null){
            IStatusTool statusTool = iGetControlTool.getStatusTool();
            if(statusTool != null){
                return statusTool.isPlaying();
            }
        }
        return false;
    }


    /**
     * 获取当前视频控制器
     * 根据当前界面是否在USB1或USB2播放界面，如果不在，则返回NULL
     *
     * @param context context
     * @return IControlTool
     */
    private IGetControlTool isOnVideoPlayActivity(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (null == runningTaskInfos || runningTaskInfos.isEmpty()) {
            Log.e(TAG, "isOnVideoPlayActivity: running taskInfos is null ");
            return null;
        }
        ComponentName topActivity = runningTaskInfos.get(0).topActivity;
        String className = topActivity.getClassName();
        Log.i(TAG, "isOnVideoPlayActivity: " + className);
        if (USB1VideoPlayActivity.class.getName().equals(className)) {
            return ModuleUSBVideoTrigger.getInstance().getUSB1VideoControlTool;
        } else if (USB2VideoPlayActivity.class.getName().equals(className)) {
            return ModuleUSBVideoTrigger.getInstance().getUSB2VideoControlTool;
        } else {
            Log.e(TAG, "isOnVideoPlayActivity: current top activity not Video Play Page");
            return null;
        }
    }

    /**
     * get the top package
     *
     * @return topPackageName
     */
    public String getTopActivityPackageName() {
        String topPackageName;
        ActivityManager activityManager = (ActivityManager) (mContext.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        ComponentName componentName = runningTaskInfos.get(0).topActivity;
        Log.d(TAG, "current top class name: " + componentName.getPackageName());
        topPackageName = componentName.getPackageName();
        return topPackageName;
    }

    /**
     * check is foreground or background
     */
    public String getActivityStatus() {
        return SV_VIDEO_APP_PACKAGE.equals(getTopActivityPackageName()) ? "fg" : "bg";
    }

    IVideoStatusChange iVideoStatusChange = new IVideoStatusChange() {
        @Override
        public void onVideoPlayStatus(MediaType mediaType, boolean isPlaying) {
            notifyCurrentVideo(isPlaying);
        }
    };
}
