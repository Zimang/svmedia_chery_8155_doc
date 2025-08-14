package com.desaysv.modulebtmusic.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRUpload;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.Constants;
import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.interfaces.listener.IBTMusicListener;
import com.desaysv.modulebtmusic.vr.BTMusicVRConstants;
import com.desaysv.modulebtmusic.vr.bean.BTMusicVRBean;
import com.desaysv.modulebtmusic.vr.bean.BTMusicVRPlayMusicBean;
import com.desaysv.modulebtmusic.vr.bean.BTMusicVRUploadBean;
import com.desaysv.svcommonutils.LanguageManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

/**
 * 语音事件适配器
 */
public class BTMusicVRAdapterManager {
    private static final String TAG = Constants.TAG + "BTMusicVRAdapterManager-0521";
    private static volatile BTMusicVRAdapterManager mInstance;
    private Context mContext;
    private Gson mGson;
    private SVMusicInfo mMusicInfo;
    private boolean isBTFragmentForeground;

    private static final String PACKAGE_NAME = "com.desaysv.svaudioapp";
    private static final String CLASS_NAME = "com.desaysv.svaudioapp.ui.MainActivity";

    //view: 跳转的目标界面
    private static final int INTENT_DEFAULT_VIEW = 1;//默认形式
    private static final int INTENT_MAIN_VIEW = 2;//媒体源的首页
    private static final int INTENT_PLAY_VIEW = 3;//媒体源的播放页

    private static final int UPLOAD_REASON_MUSIC_INFO = 1;
    private static final int UPLOAD_REASON_MUSIC_STATUS = 2;
    private static final int UPLOAD_REASON_ACTIVITY_STATUS = 3;

    public static BTMusicVRAdapterManager getInstance() {
        if (mInstance == null) {
            synchronized (BTMusicVRAdapterManager.class) {
                if (mInstance == null) {
                    mInstance = new BTMusicVRAdapterManager();
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
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VR);
        VDBus.getDefault().registerVDBindListener(mVDBindListener);
        subscribeEvents();//订阅事件
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);//注册事件订阅监听
        mMusicInfo = BTMusicManager.getInstance().getMusicPlayInfo();
        BTMusicManager.getInstance().registerListener(mBtMusicListener, false);
        Log.d(TAG, "initialize: finish");
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "release: ");
        BTMusicManager.getInstance().unregisterListener(mBtMusicListener);
        mMusicInfo = null;
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener); //反注册事件订阅监听
        VDBus.getDefault().unregisterVDBindListener(mVDBindListener);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.VR);
        Log.d(TAG, "release: finish");
    }

    private void subscribeEvents() {
        VDBus.getDefault().addSubscribe(VDEventVR.VR_MUSIC);
        VDBus.getDefault().addSubscribe(VDEventVR.VR_MEDIA);
        VDBus.getDefault().subscribeCommit();
    }

    private VDBindListener mVDBindListener = new VDBindListener() {
        @Override
        public void onVDConnected(VDServiceDef.ServiceType serviceType) {
            if (serviceType == VDServiceDef.ServiceType.VR) {
                Log.d(TAG, "bindService success");
            }
        }

        @Override
        public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
            if (serviceType == VDServiceDef.ServiceType.VR) {
                Log.d(TAG, "bindService fail, bind again");
                VDBus.getDefault().bindService(VDServiceDef.ServiceType.VR);
            }
        }
    };

    private VDNotifyListener mVDNotifyListener = (vdEvent, threadType) -> {
        if (vdEvent == null) {
            Log.w(TAG, "onVDNotify: vdEvent is null");
            return;
        }
        if (threadType == VDThreadType.MAIN_THREAD) {
            Log.d(TAG, "vdEvent.getId() = " + vdEvent.getId());
            switch (vdEvent.getId()) {
                case VDEventVR.VR_MUSIC://音乐媒体状态
                case VDEventVR.VR_MEDIA:
                    VDVRPipeLine param = VDVRPipeLine.getValue(vdEvent);
                    String key = param.getKey();
                    String data = param.getValue();
                    handleVREvent(key, data);
                    break;
            }
        }
    };

    private void handleVREvent(String key, String data) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(data)) {
            Log.w(TAG, "handleVREvent: key and data is empty");
            return;
        }
        Log.d(TAG, "handleVREvent: key = " + key);
        switch (key) {
            case BTMusicVRConstants.Key.KEY_PLAY_MUSIC:
                handlePlayMusicEvent(data);
                break;
            case BTMusicVRConstants.Key.KEY_CONTROL_PLAY_STATE:
                handleControlPlatStateEvent(data);
                break;
            case BTMusicVRConstants.Key.KEY_CONTROL_COLLECT://收藏列表
                handleControlCollectEvent(data);
                break;
            case BTMusicVRConstants.Key.KEY_CONTROL_PLAY_LIST://播放列表
                handlePlayListEvent(data);
                break;
//            default:
//                Log.d(TAG, "handleVREvent: Not Support Default Response");
//                setVrResponse(mContext.getString(R.string.media_music_sourcemusic_source_unable));
//                break;
        }
    }

    private void handlePlayMusicEvent(String data) {
        BTMusicVRPlayMusicBean btMusicVRPlayMusicBean = parseJsonToPlayMusicBean(data);
        if (btMusicVRPlayMusicBean == null) {
            Log.w(TAG, "handlePlayMusicEvent: btMusicVRPlayMusicBean is null");
            return;
        }
        Log.d(TAG, "handlePlayMusicEvent: btMusicVRPlayMusicBean = " + btMusicVRPlayMusicBean.toString());
        if (!TextUtils.equals(btMusicVRPlayMusicBean.getSemantic().source, BTMusicVRConstants.PlayMusic.Source.SOURCE_BT)) {
            Log.w(TAG, "handlePlayMusicEvent: source is not bt");
            return;
        }
        if (!BTMusicManager.getInstance().isA2DPConnected()) {
            Log.w(TAG, "handlePlayMusicEvent: a2dp disconnected");
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_sourcemusic_source_unable));
            return;
        }
        if (TextUtils.equals(btMusicVRPlayMusicBean.getSemantic().action, BTMusicVRConstants.PlayMusic.Action.ACTION_OPEN)) {
            BTMusicManager.getInstance().play();
            lunchForegroundView(INTENT_PLAY_VIEW);
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_sourcemusic_source_play));
            BTMusicDataServiceManager.getInstance().trackEventPlay(BTMusicDataServiceManager.VALUE_OPER_VOICE);
        } else if (TextUtils.equals(btMusicVRPlayMusicBean.getSemantic().action, BTMusicVRConstants.PlayMusic.Action.ACTION_CLOSE)) {
            BTMusicManager.getInstance().pause();
            BTMusicDataServiceManager.getInstance().trackEventPause(BTMusicDataServiceManager.VALUE_OPER_VOICE);
        }
    }

    private void handleControlPlatStateEvent(String data) {
        BTMusicVRBean btMusicVRBean = parseJsonToBean(data);
        if (btMusicVRBean == null) {
            Log.w(TAG, "handleControlPlatStateEvent: btMusicVRBean is null");
            return;
        }
        Log.d(TAG, "handleControlPlatStateEvent: btMusicVRBean = " + btMusicVRBean.toString());
        if (!BTMusicAudioManager.getInstance().checkBTAudioFocusStatus()) {
            Log.w(TAG, "handleControlPlatStateEvent: Bluetooth music has no audio focus");
            return;
        }
        if (!BTMusicManager.getInstance().isA2DPConnected()) {
            Log.w(TAG, "handleControlPlatStateEvent: a2dp disconnected");
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_sourcemusic_source_unable));
            return;
        }
        if (TextUtils.isEmpty(btMusicVRBean.getSemantic().action)) {
            Log.w(TAG, "handleControlPlatStateEvent: action is empty");
            return;
        }
        switch (btMusicVRBean.semantic.action) {
            case BTMusicVRConstants.ControlPlayState.Action.ACTION_PAUSE:
                BTMusicManager.getInstance().pause();
                BTMusicDataServiceManager.getInstance().trackEventPause(BTMusicDataServiceManager.VALUE_OPER_VOICE);
                break;
            case BTMusicVRConstants.ControlPlayState.Action.ACTION_PLAY:
                BTMusicManager.getInstance().play();
                BTMusicDataServiceManager.getInstance().trackEventPlay(BTMusicDataServiceManager.VALUE_OPER_VOICE);
                setVrResponse(getStrFromLanguageMgr(R.string.bt_music_sourcemusic_source_play));
                break;
            case BTMusicVRConstants.ControlPlayState.Action.ACTION_NEXT:
                BTMusicManager.getInstance().skipToNext();
                BTMusicDataServiceManager.getInstance().trackEventNext(BTMusicDataServiceManager.VALUE_OPER_VOICE);
                break;
            case BTMusicVRConstants.ControlPlayState.Action.ACTION_PREVIOUS:
                BTMusicManager.getInstance().skipToPrevious();
                BTMusicDataServiceManager.getInstance().trackEventPrevious(BTMusicDataServiceManager.VALUE_OPER_VOICE);
                break;
        }
    }

    private void handleControlCollectEvent(String data) {
        BTMusicVRPlayMusicBean btMusicVRPlayMusicBean = parseJsonToPlayMusicBean(data);
        if (btMusicVRPlayMusicBean == null) {
            Log.w(TAG, "handleControlCollectEvent: btMusicVRPlayMusicBean is null");
            return;
        }
        if (TextUtils.equals(btMusicVRPlayMusicBean.getSemantic().source, BTMusicVRConstants.PlayMusic.Source.SOURCE_BT)) {
            Log.d(TAG, "handleControlCollectEvent: source = bt, respond Not Support");
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_collect_playing_nosupport));
            return;
        }
        if (!BTMusicAudioManager.getInstance().checkBTAudioFocusStatus()) {
            Log.w(TAG, "handleControlCollectEvent: Bluetooth music has no audio focus");
            return;
        }
        if (!BTMusicManager.getInstance().isA2DPConnected()) {
            Log.w(TAG, "handleControlCollectEvent: a2dp disconnected");
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_sourcemusic_source_unable));
            return;
        }
        Log.d(TAG, "handleControlCollectEvent: respond Not Support");
        setVrResponse(getStrFromLanguageMgr(R.string.bt_music_collect_playing_nosupport));
    }

    private void handlePlayListEvent(String data) {
        BTMusicVRPlayMusicBean btMusicVRPlayMusicBean = parseJsonToPlayMusicBean(data);
        if (btMusicVRPlayMusicBean == null) {
            Log.w(TAG, "handlePlayListEvent: btMusicVRPlayMusicBean is null");
            return;
        }
        if (TextUtils.equals(btMusicVRPlayMusicBean.getSemantic().source, BTMusicVRConstants.PlayMusic.Source.SOURCE_BT)) {
            Log.d(TAG, "handlePlayListEvent: source = bt, respond Not Support");
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_openplaylist_nosupport));
            return;
        }
        if (!BTMusicAudioManager.getInstance().checkBTAudioFocusStatus()) {
            Log.w(TAG, "handlePlayListEvent: Bluetooth music has no audio focus");
            return;
        }
        if (!BTMusicManager.getInstance().isA2DPConnected()) {
            Log.w(TAG, "handlePlayListEvent: a2dp disconnected");
            setVrResponse(getStrFromLanguageMgr(R.string.bt_music_sourcemusic_source_unable));
            return;
        }
        Log.d(TAG, "handlePlayListEvent: respond Not Support");
        setVrResponse(getStrFromLanguageMgr(R.string.bt_music_openplaylist_nosupport));
    }

    private BTMusicVRPlayMusicBean parseJsonToPlayMusicBean(String data) {
        if (TextUtils.isEmpty(data)) {
            Log.w(TAG, "parseJsonToPlayMusicBean: data is empty");
            return null;
        }
        if (mGson == null) {
            mGson = new Gson();
        }
        BTMusicVRPlayMusicBean btMusicVRPlayMusicBean = null;
        try {
            btMusicVRPlayMusicBean = mGson.fromJson(data, BTMusicVRPlayMusicBean.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if (btMusicVRPlayMusicBean == null || btMusicVRPlayMusicBean.getSemantic() == null) {
            Log.w(TAG, "parseJsonToPlayMusicBean: parse failed");
            return null;
        }
        Log.i(TAG, "parseJsonToPlayMusicBean: " + btMusicVRPlayMusicBean.toString());
        return btMusicVRPlayMusicBean;
    }

    private BTMusicVRBean parseJsonToBean(String data) {
        if (TextUtils.isEmpty(data)) {
            Log.w(TAG, "parseJsonToBean: data is empty");
            return null;
        }
        if (mGson == null) {
            mGson = new Gson();
        }
        BTMusicVRBean btMusicVRBean = null;
        try {
            btMusicVRBean = mGson.fromJson(data, BTMusicVRBean.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if (btMusicVRBean == null || btMusicVRBean.getSemantic() == null) {
            Log.w(TAG, "parseJsonToBean: parse failed");
            return null;
        }
        Log.i(TAG, "parseJsonToBean: " + btMusicVRBean.toString());
        return btMusicVRBean;
    }

    /**
     * 发送反馈语/TTS播报
     *
     * @param content
     */
    private void setVrResponse(String content) {
        if (TextUtils.isEmpty(content)) {
            Log.w(TAG, "setVrResponse: content is empty");
            return;
        }
        Log.i(TAG, "setVrResponse: content = " + content);
        VDVRPipeLine param = new VDVRPipeLine();
        param.setKey(VDValueVR.VRSemanticKey.VR_CONTROL_RESPONSE);
        param.setValue(content);
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_SUCCEED_AND_TTS); //根据执行结果反馈
        VDEvent event = VDVRPipeLine.createEvent(VDEventVR.VR_MUSIC, param);
        VDBus.getDefault().set(event);
    }

    private IBTMusicListener mBtMusicListener = new IBTMusicListener() {
        @Override
        public void onConnectionStateChanged(int profile, String address, int state) {
        }

        @Override
        public void onMusicPlayInfoChanged(SVMusicInfo musicInfo) {
            if ((mMusicInfo == null && musicInfo != null) || (mMusicInfo != null && musicInfo == null)) {
                mMusicInfo = musicInfo;
                uploadBtMusicData(mMusicInfo, UPLOAD_REASON_MUSIC_INFO);
                return;
            }
            if (mMusicInfo == null) {
                Log.w(TAG, "onMusicPlayInfoChanged: mMusicInfo is null");
                return;
            }
            Log.i(TAG, "onMusicPlayInfoChanged: musicInfo = " + musicInfo);
            if ((!TextUtils.equals(mMusicInfo.getAlbumName(), musicInfo.getAlbumName()))
                    || !TextUtils.equals(mMusicInfo.getArtistName(), musicInfo.getArtistName())
                    || mMusicInfo.getDuration() != musicInfo.getDuration()) {
                mMusicInfo = musicInfo;
                uploadBtMusicData(mMusicInfo, UPLOAD_REASON_MUSIC_INFO);
                BTMusicDataServiceManager.getInstance().trackEventProgramName(musicInfo.getMediaTitle());
                BTMusicDataServiceManager.getInstance().trackEventAuthor(musicInfo.getArtistName());
            }
        }

        @Override
        public void onMusicPlayStateChanged(int state) {
            if (mMusicInfo == null) {
                Log.i(TAG, "onMusicPlayStateChanged: mMusicInfo is null, upload");
                mMusicInfo = BTMusicManager.getInstance().getMusicPlayInfo();
                uploadBtMusicData(mMusicInfo, UPLOAD_REASON_MUSIC_STATUS);
            } else if (mMusicInfo.getPlayState() != state) {
                Log.i(TAG, "onMusicPlayStateChanged: play state changed, upload");
                mMusicInfo.setPlayState(state);
                uploadBtMusicData(mMusicInfo, UPLOAD_REASON_MUSIC_STATUS);
            }
        }

        @Override
        public void onMusicPlayProgressChanged(long progress, long duration) {
        }

        @Override
        public void onMusicPlayListChanged() {
        }
    };

    private void uploadBtMusicData(SVMusicInfo musicInfo, int uploadReason) {
        Log.i(TAG, "uploadBtMusicData: uploadReason = " + uploadReason);
        BTMusicVRUploadBean.data beanData = new BTMusicVRUploadBean.data();
        beanData.setActiveStatus(isBTFragmentForeground ? BTMusicVRConstants.VRUploadRadioValue.VR_RADIO_STATUE_FG : BTMusicVRConstants.VRUploadRadioValue.VR_RADIO_STATUE_BG);
        if (musicInfo == null) {
            Log.w(TAG, "uploadBtMusicData: musicInfo is null");
            beanData.setSceneStatus(BTMusicVRConstants.VRUploadRadioValue.VR_RADIO_STATUE_PAUSE);
        } else {
            BTMusicVRUploadBean.data.dataInfo beanDataInfo = new BTMusicVRUploadBean.data.dataInfo();
            beanDataInfo.setArtist(musicInfo.getArtistName());
            beanDataInfo.setSong(musicInfo.getMediaTitle());
            beanData.setSceneStatus(BTMusicManager.getInstance().isPlayingState(musicInfo.getPlayState()) ? BTMusicVRConstants.VRUploadRadioValue.VR_RADIO_STATUE_PLAY : BTMusicVRConstants.VRUploadRadioValue.VR_RADIO_STATUE_PAUSE);
            beanData.setDataInfo(beanDataInfo);
        }
        BTMusicVRUploadBean bean = new BTMusicVRUploadBean();
        bean.setData(beanData);
        Log.i(TAG, "uploadBtMusicData: bean = " + bean.toString());
        if (mGson == null) {
            mGson = new Gson();
        }
        String dataJson = mGson.toJson(bean);
        Log.i(TAG, "uploadBtMusicData: dataJson = " + dataJson);

        VDVRUpload param = new VDVRUpload();
        param.setPkgName(AppBase.mContext.getPackageName());
        switch (uploadReason) {
            case UPLOAD_REASON_MUSIC_INFO:
                param.setKey(VDValueVR.VRUploadMusicStatus.VR_MUSIC_STATUS_RESPONSE);
                break;
            case UPLOAD_REASON_MUSIC_STATUS:
            case UPLOAD_REASON_ACTIVITY_STATUS:
                param.setKey(VDValueVR.VRUploadMusicStatus.VR_MUSIC_PLAY_RESPONSE);
                break;
        }
        param.setKey(VDValueVR.VRUploadMusicStatus.VR_MUSIC_STATUS_RESPONSE);
        param.setData(dataJson);
        param.setResultCode(VDValueVR.VR_RESPONSE_STATUS.RESPONSE_CODE_NOTIFY);
        VDEvent event = VDVRUpload.createEvent(VDEventVR.VR_DATA_UPLOAD, param);
        VDBus.getDefault().set(event);
    }

    /**
     * 获取系统顶层界面的包名
     *
     * @return Audio应用是否前台
     */
    private boolean isActivityOnTop() {
        String topPackageName;
        ActivityManager activityManager = (ActivityManager) (AppBase.mContext.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager.getRunningTasks(1);
        ComponentName componentName = runningTaskInfo.get(0).topActivity;
        topPackageName = componentName.getPackageName();
        Log.d(TAG, "isActivityOnTop: topPackageName = " + topPackageName);
        return TextUtils.equals(PACKAGE_NAME, topPackageName);
    }

    /**
     * 设置蓝牙Fragment的前台情况
     * @param isForegound 是否在前台。 true--在前台  false--在后台
     */
    public void setBTFragmentForeground(boolean isForegound){
        isBTFragmentForeground = isForegound;
        Log.i(TAG, "setBTFragmentForeground: BTFragmentForeground changed, upload. isBTFragmentForeground = " + isForegound);
        mMusicInfo = BTMusicManager.getInstance().getMusicPlayInfo();
        uploadBtMusicData(mMusicInfo,UPLOAD_REASON_MUSIC_INFO);
    }


    /**
     * 拉起界面
     *
     * @param view
     */
    private void lunchForegroundView(int view) {
        Log.i(TAG, "lunchForegroundView: view = " + view);
        Intent startIntent = new Intent();
        startIntent.setClassName(PACKAGE_NAME, CLASS_NAME);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.putExtra(com.desaysv.mediacommonlib.bean.Constants.Source.SOURCE_KEY,
                com.desaysv.mediacommonlib.bean.Constants.Source.SOURCE_BT);
        if (view == INTENT_MAIN_VIEW) {//媒体源的首页
            startIntent.putExtra(com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.KEY,
                    com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.FLAG_MAIN);
        } else if (view == INTENT_PLAY_VIEW) {//媒体源的播放页
            startIntent.putExtra(com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.KEY,
                    com.desaysv.mediacommonlib.bean.Constants.NavigationFlag.FLAG_PLAY);
        }
        mContext.startActivity(startIntent);
        BTMusicDataServiceManager.getInstance().trackEventOpen(BTMusicDataServiceManager.VALUE_POWER_VOICE,
                BTMusicDataServiceManager.VALUE_OPER_VOICE);
    }

    private String getStrFromLanguageMgr(int resId) {
        if (mContext == null) {
            Log.w(TAG, "getStrFromLanguageMgr: mContext == null, return empty str");
            return "";
        }
        return LanguageManager.getInstance().getString(mContext, resId);
    }
}
