package com.desaysv.moduleusbmusic.utils;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.localmediasdk.IAIDLMediaInfoCallback;
import com.desaysv.localmediasdk.bean.LocalMediaConstants;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.sdk.bean.Constant;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.businesslogic.control.CopyDeleteControl;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.vr.MusicVRValue;
import com.desaysv.moduleusbmusic.vr.MusicVrManager;
import com.desaysv.svlibmediaobserver.manager.MediaObserverManager;
import com.desaysv.svlibmediastore.dao.RecentlyMusicDao;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.google.gson.Gson;

import java.io.File;


/**
 * Created by LZM on 2020-3-24
 * Comment 音乐的媒体状态回调
 *
 * @author uidp5370
 */
public class MusicStatusUtils {
    private static final String TAG = "MusicStatusUtils";
    private static MusicStatusUtils instance;
    private Handler handler;
    //更新最新播放的条目
    private static final int UPDATE_RECENT_ITEM = 0x01;
    //更新最新播放的需要的时间
    private static final int UPDATE_RECENT_TIME = 15 * 1000;
    //专辑图的URI解析
    private final Uri ART_URI = Uri.parse("content://media/external/audio/albumart");

    public static MusicStatusUtils getInstance() {
        if (instance == null) {
            synchronized (MusicStatusUtils.class) {
                if (instance == null) {
                    instance = new MusicStatusUtils();
                }
            }
        }
        return instance;
    }

    private MusicStatusUtils() {

    }

    /**
     * 初始化，并且注册数据变化回调
     */
    public void initialize() {
        Log.d(TAG, "initialize: ");
        initHandler();
        ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.
                registerMediaStatusChangeListener(TAG, mLocalMediaStatusChange);
        ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.
                registerMediaStatusChangeListener(TAG, mUSB1MediaStatusChange);
        ModuleUSBMusicTrigger.getInstance().getUSB2MusicControlTool.
                registerMediaStatusChangeListener(TAG, mUSB2MediaStatusChange);
        ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.
                registerMediaStatusChangeListener(TAG, mRecentMediaStatusChange);
        //updateRecentlyMusic();
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == UPDATE_RECENT_ITEM && msg.obj instanceof FileMessage) {
                    RecentlyMusicDao.getInstance().update((FileMessage) msg.obj);
                }
            }
        };
    }

    /**
     * 更新下当前播放的列表
     */
    private void updateRecentlyMusic() {
        FileMessage currentPlayItem = ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem();
        if (!TextUtils.isEmpty(currentPlayItem.getPath())) {
            RecentlyMusicDao.getInstance().update(currentPlayItem);
        }
        //RecentlyMusicDao.getInstance().update(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem());
    }


    /**
     * 获取当前的播放信息,
     *
     * @return 当前播放信息的json数据
     */
    public String getCurrentPlayInfo(String source) {
        //TODO：这里可以根据项目的需求，判断当前需要返回怎么样的数据，现在是将收音和音乐区分开了两个服务
        Log.d(TAG, "getCurrentPlayInfo: source = " + source);
        if (isRecentMediaType()) {
            return changeFileMessageToJson(source, ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem());
        }
        return changeFileMessageToJson(source, getIStatusTool(source).getCurrentPlayItem());
    }

    /**
     * 获取对应source正在播放的曲目
     *
     * @param source source
     * @return FileMessage
     */
    private IStatusTool getIStatusTool(String source) {
        IStatusTool tool = ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool;
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                tool = ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool;
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                tool = ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool;
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                tool = ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool;
                break;
        }
        return tool;
    }

    /**
     * 获取当前的播放状态
     *
     * @return 当前播放状态的json数据
     */
    public String getCurrentPlayStatus(String source) {
        Log.d(TAG, "getCurrentPlayStatus: source = " + source);
        if (isRecentMediaType()) {
            return changePlayStatusToJson(source, ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.isPlaying());
        }
        return changePlayStatusToJson(source, getIStatusTool(source).isPlaying());
    }

    /**
     * 获取当前播放时间
     *
     * @return 当前播放时间的json数据
     */
    public String getCurrentPlayTime(String source) {
        Log.d(TAG, "getCurrentPlayTime: source = " + source);
        if (isRecentMediaType()) {
            return changePlayTimeToJson(source
                    , ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getDuration()
                    , ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayTime());
        }
        IStatusTool iStatusTool = getIStatusTool(source);
        return changePlayTimeToJson(source, iStatusTool.getDuration(), iStatusTool.getCurrentPlayTime());
    }


    /**
     * 获取当前的循环模式
     *
     * @param source 音源
     * @return 循环类型
     */
    public String getLoopType(String source) {
        Log.d(TAG, "getLoopType: source = " + source);
        if (isRecentMediaType()) {
            return changeLoopTypeToJson(source, ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getLoopType());
        }
        return changeLoopTypeToJson(source, getIStatusTool(source).getLoopType());
    }

    /**
     * 获取当前的设备连接状态
     *
     * @param source source
     * @return String
     */
    public String getDeviceConnectState(String source) {
        Log.d(TAG, "getDeviceConnectState: source = " + source);
        String connectState = "";
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                connectState = Constant.DeviceState.CONNECTED;
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(new File(USBConstants.USBPath.USB0_PATH)))) {
                    connectState = Constant.DeviceState.CONNECTED;
                } else {
                    connectState = Constant.DeviceState.DISCONNECTED;
                }
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(new File(USBConstants.USBPath.USB1_PATH)))) {
                    connectState = Constant.DeviceState.CONNECTED;
                } else {
                    connectState = Constant.DeviceState.DISCONNECTED;
                }
                break;
        }
        return changeDeviceStateToJson(source, connectState);
    }

    /**
     * 通知连接状态变化
     *
     * @param source       source
     * @param connectState {@link Constant.DeviceState}
     */
    public void notifyDeviceState(String source, String connectState) {
        Log.d(TAG, "notifyDeviceState: source = " + " connectState = " + connectState);
        synchronized (mCallbackList) {
            int callbackCount = mCallbackList.beginBroadcast();
            Log.d(TAG, "notifyDeviceState: callbackCount = " + callbackCount);
            while (callbackCount > 0) {
                callbackCount--;
                try {
                    mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(source,
                            LocalMediaConstants.StatusAction.DEVICE_STATUS,
                            changeDeviceStateToJson(source, connectState));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mCallbackList.finishBroadcast();
        }
    }

    /**
     * 获取当前的专辑图片
     *
     * @return byte[] 专辑数组
     */
    public byte[] getPicData(String source) {
        Log.d(TAG, "getPicData: source = " + source);
        if (isRecentMediaType()) {
            return ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCompressionAlbumPic();
        }
        return getIStatusTool(source).getCompressionAlbumPic();
    }

    /**
     * 当前的控制器是否是最近播放控制器
     *
     * @return T 是 F 否
     */
    private boolean isRecentMediaType() {
        int mediaType = MusicSetting.getInstance().getInt(SourceTypeUtils.MEDIA_TYPE, MediaType.USB1_MUSIC.ordinal());
        Log.d(TAG, "isRecentMediaType: mediaType = " + mediaType);
        return MediaType.RECENT_MUSIC.ordinal() == mediaType;
    }

    private final RemoteCallbackList<IAIDLMediaInfoCallback> mCallbackList = new RemoteCallbackList<>();

    /**
     * 设置音乐数据的变化回调
     *
     * @param packageName           包名
     * @param aidlMediaInfoCallback 回调
     */
    public void setMusicCallback(String packageName, IAIDLMediaInfoCallback aidlMediaInfoCallback) {
        Log.d(TAG, "setMusicCallback: packageName = " + packageName);
        mCallbackList.register(aidlMediaInfoCallback);
    }

    /**
     * 清除音乐数据的变化回调
     *
     * @param packageName 包名
     */
    public void removeMusicCallback(String packageName, IAIDLMediaInfoCallback aidlMediaInfoCallback) {
        Log.d(TAG, "removeMusicCallback: packageName = " + packageName);
        mCallbackList.unregister(aidlMediaInfoCallback);
    }

    /**
     * 本地音乐状态的数据回调
     */
    private final IMediaStatusChange mLocalMediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            FileMessage fileMessage = ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem();
            MediaObserverManager.getInstance().setMediaInfo(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, changeFileMessageToAppMedia(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, fileMessage), false);
            //设置当前播放的媒体信息
            //这里是跟随整个声明周期的，添加一下,根据需求是15s后添加
            updateRecentlyItem(fileMessage);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mLocalMediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount + " fileMessage = " + fileMessage);
                MusicVrManager.getInstance().getResponse().uploadInfo(fileMessage);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_INFO,
                                changeFileMessageToJson(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, fileMessage));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mLocalMediaStatusChange onPlayStatusChange: callbackCount = " + callbackCount);
                MusicVrManager.getInstance().getResponse().uploadPlayState(MusicVRValue.Source.LOCAL, isPlaying);
                if (isPlaying) {
                    MediaObserverManager.getInstance().setMediaInfo(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, changeFileMessageToAppMedia(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()), false);
                }
                MediaObserverManager.getInstance().setPlayStatus(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,  ModuleUSBMusicTrigger.getInstance().isPlaying()||isPlaying);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_STATUS,
                                changePlayStatusToJson(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, isPlaying));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                //Log.d(TAG, "mLocalMediaStatusChange onPlayTimeChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_TIME,
                                changePlayTimeToJson(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, duration, currentPlayTime));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {

        }

        /**
         * 媒体的专辑图片数据发生改变的时候
         */
        @Override
        public void onAlbumPicDataChange() {
            byte[] picData = ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCompressionAlbumPic();
            MediaObserverManager.getInstance().setAlbum(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, picData);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mLocalMediaStatusChange onAlbumPicDataChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaPicChange(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, picData);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLoopTypeChange() {
            String loopType = ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getLoopType();
            Log.d(TAG, "mLocalMediaStatusChange onLoopTypeChange: loopType = " + loopType);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mLocalMediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.LOOP_TYPE,
                                changeLoopTypeToJson(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, loopType));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLyricsChange() {

        }

        @Override
        public void onPlayListChange() {

        }
    };

    /**
     * USB1音乐状态的数据回调
     */
    private final IMediaStatusChange mUSB1MediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            FileMessage fileMessage = ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem();
            MediaObserverManager.getInstance().setMediaInfo(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, changeFileMessageToAppMedia(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, fileMessage), false);
            //设置当前播放的媒体信息
            //这里是跟随整个声明周期的，添加一下
            updateRecentlyItem(fileMessage);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB1MediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount + " fileMessage = " + fileMessage);
                MusicVrManager.getInstance().getResponse().uploadInfo(fileMessage);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_INFO,
                                changeFileMessageToJson(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, fileMessage));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB1MediaStatusChange onPlayStatusChange: callbackCount = " + callbackCount);
                MusicVrManager.getInstance().getResponse().uploadPlayState(MusicVRValue.Source.USB, isPlaying);
                if (isPlaying) {
                    MediaObserverManager.getInstance().setMediaInfo(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, changeFileMessageToAppMedia(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()), false);
                }
                MediaObserverManager.getInstance().setPlayStatus(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, ModuleUSBMusicTrigger.getInstance().isPlaying()||isPlaying);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_STATUS,
                                changePlayStatusToJson(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, isPlaying));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                //Log.d(TAG, "mUSB1MediaStatusChange onPlayTimeChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_TIME,
                                changePlayTimeToJson(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, duration, currentPlayTime));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {

        }

        /**
         * 媒体的专辑图片数据发生改变的时候
         */
        @Override
        public void onAlbumPicDataChange() {
            byte[] picData = ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCompressionAlbumPic();
            MediaObserverManager.getInstance().setAlbum(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, picData);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB1MediaStatusChange onAlbumPicDataChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaPicChange(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, picData);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLoopTypeChange() {
            String loopType = ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getLoopType();
            Log.d(TAG, "mUSB1MediaStatusChange onLoopTypeChange: loopType = " + loopType);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB1MediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.LOOP_TYPE,
                                changeLoopTypeToJson(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, loopType));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLyricsChange() {

        }

        @Override
        public void onPlayListChange() {

        }
    };

    /**
     * USB2音乐的数据回调
     */
    private final IMediaStatusChange mUSB2MediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            FileMessage fileMessage = ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem();
            //设置当前播放的媒体信息
            updateRecentlyItem(fileMessage);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB2MediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount + " fileMessage = " + fileMessage);
                MusicVrManager.getInstance().getResponse().uploadInfo(fileMessage);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_INFO,
                                changeFileMessageToJson(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, fileMessage));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB2MediaStatusChange onPlayStatusChange: callbackCount = " + callbackCount);
                MusicVrManager.getInstance().getResponse().uploadPlayState(MusicVRValue.Source.USB1, isPlaying);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_STATUS,
                                changePlayStatusToJson(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, isPlaying));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                //Log.d(TAG, "mUSB2MediaStatusChange onPlayTimeChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.PLAY_TIME,
                                changePlayTimeToJson(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, duration, currentPlayTime));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {

        }

        /**
         * 媒体的专辑图片数据发生改变的时候
         */
        @Override
        public void onAlbumPicDataChange() {
            byte[] picData = ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCompressionAlbumPic();
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB2MediaStatusChange onAlbumPicDataChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaPicChange(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, picData);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLoopTypeChange() {
            String loopType = ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getLoopType();
            Log.d(TAG, "mUSB2MediaStatusChange onLoopTypeChange: loopType = " + loopType);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mUSB2MediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
                                LocalMediaConstants.StatusAction.LOOP_TYPE,
                                changeLoopTypeToJson(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, loopType));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLyricsChange() {

        }

        @Override
        public void onPlayListChange() {

        }
    };

    /**
     * 最近播放音乐的数据回调
     */
    private final IMediaStatusChange mRecentMediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            FileMessage fileMessage = ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem();
            //设置当前播放的媒体信息
            updateRecentlyItem(fileMessage);
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                String source = getRecentToolPlaySourceName(fileMessage);
                Log.d(TAG, "mRecentMediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount + " source = " + source + " fileMessage = " + fileMessage);
                MediaObserverManager.getInstance().setMediaInfo(source, changeFileMessageToAppMedia(source, fileMessage), true);
                MusicVrManager.getInstance().getResponse().uploadInfo(fileMessage);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(source,
                                LocalMediaConstants.StatusAction.PLAY_INFO,
                                changeFileMessageToJson(source, fileMessage));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                String source = getRecentToolPlaySourceName(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem());
                Log.d(TAG, "mRecentMediaStatusChange onPlayStatusChange: callbackCount = " + callbackCount + " source = " + source);
                MediaObserverManager.getInstance().setPlayStatus(source, isPlaying);
                MusicVrManager.getInstance().getResponse().uploadPlayState(MusicVRValue.Source.ALL, isPlaying);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(source,
                                LocalMediaConstants.StatusAction.PLAY_STATUS,
                                changePlayStatusToJson(source, isPlaying));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
//                Log.d(TAG, "mRecentMediaStatusChange onPlayTimeChange: callbackCount = " + callbackCount);
                String source = getRecentToolPlaySourceName(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem());
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(source,
                                LocalMediaConstants.StatusAction.PLAY_TIME,
                                changePlayTimeToJson(source, duration, currentPlayTime));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {

        }

        /**
         * 媒体的专辑图片数据发生改变的时候
         */
        @Override
        public void onAlbumPicDataChange() {
            synchronized (mCallbackList) {
                byte[] picData = ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCompressionAlbumPic();
                String source = getRecentToolPlaySourceName(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem());
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mRecentMediaStatusChange onAlbumPicDataChange: callbackCount = " + callbackCount + " source = " + source);
                MediaObserverManager.getInstance().setAlbum(source, picData);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaPicChange(source, picData);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLoopTypeChange() {
            String loopType = ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getLoopType();
            String source = getRecentToolPlaySourceName(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem());
            synchronized (mCallbackList) {
                int callbackCount = mCallbackList.beginBroadcast();
                Log.d(TAG, "mRecentMediaStatusChange onMediaInfoChange: callbackCount = " + callbackCount + " loopType = " + loopType + " source = " + source);
                while (callbackCount > 0) {
                    callbackCount--;
                    try {
                        mCallbackList.getBroadcastItem(callbackCount).onMediaInfoChange(source,
                                LocalMediaConstants.StatusAction.LOOP_TYPE,
                                changeLoopTypeToJson(source, loopType));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mCallbackList.finishBroadcast();
            }
        }

        @Override
        public void onLyricsChange() {

        }

        @Override
        public void onPlayListChange() {

        }
    };

    /**
     * 更新播放条目的方法
     *
     * @param fileMessage fileMessage
     */
    private void updateRecentlyItem(FileMessage fileMessage) {
        handler.removeMessages(UPDATE_RECENT_ITEM);
        Message obtain = Message.obtain();
        obtain.what = UPDATE_RECENT_ITEM;
        obtain.obj = fileMessage;
        handler.sendMessageDelayed(obtain, UPDATE_RECENT_TIME);
    }

    /**
     * 获取最近播放列表播放器条目所属音源情况
     *
     * @param fileMessage 当前播放曲目
     * @return String
     */
    private String getRecentToolPlaySourceName(FileMessage fileMessage) {
        String path = fileMessage.getPath();
        if (path.startsWith(USBConstants.USBPath.USB0_PATH)) {
            return DsvAudioSDKConstants.USB0_MUSIC_SOURCE;
        } else if (path.startsWith(USBConstants.USBPath.USB1_PATH)) {
            return DsvAudioSDKConstants.USB1_MUSIC_SOURCE;
        } else if (path.startsWith(USBConstants.USBPath.LOCAL_PATH)) {
            return DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE;
        }
        return "";
    }

    /**
     * 获取当前音源的播放状态
     *
     * @param source source
     * @return String
     */
    public String getCollectStatus(String source) {
        return getCurrentPlayInfo(source);
    }

    /**
     * 将USB的媒体数据转化为json
     *
     * @param currentSource 当前的音频焦点
     * @param fileMessage   USB的文件信息
     * @return json数据
     */
    private String changeFileMessageToJson(String currentSource, FileMessage fileMessage) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource);
        if (fileMessage != null) {
            //这里需要把空字符名称转换下默认占位字符分发出去
            builder.setAlbumId(fileMessage.getAlbumId())
                    .setPath(fileMessage.getPath())
                    .setTitle(TextUtils.isEmpty(fileMessage.getName()) ? AppBase.mContext.getString(R.string.usb_music_unknown_song_name) : fileMessage.getName())
                    .setArtist(TextUtils.isEmpty(fileMessage.getAuthor()) ? AppBase.mContext.getString(R.string.usb_music_unknown_singer) : fileMessage.getAuthor())
                    .setAlbum(fileMessage.getAlbum())
                    .setCollectStatus(CopyDeleteControl.getInstance().getCopyControl().isCopied(fileMessage));
        }
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeFileMessageToJson: data = " + data);
        return data;
    }

    /**
     * 将当前的播放状态设置为json数据
     *
     * @param currentSource 当前的音源
     * @param playStatus    当前的播放状态
     * @return json数据
     */
    private String changePlayStatusToJson(String currentSource, boolean playStatus) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource).setPlayStatus(playStatus);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changePlayStatusToJson: data = " + data);
        return data;
    }


    /**
     * 将当前的播放时间转化为json数据
     *
     * @param currentSource   当前的音源
     * @param duration        当前的播放总时长
     * @param currentPlayTime 当前的播放时间
     * @return json数据
     */
    private String changePlayTimeToJson(String currentSource, int duration, int currentPlayTime) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(currentSource)
                .setDuration(duration).setCurrentPlayTime(currentPlayTime);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changePlayTimeToJson: data = " + data);
        return data;
    }

    /**
     * 将循环模式转化为json数据
     *
     * @param source   当前的音源
     * @param loopType 当前的循环模式
     * @return 循环模式的json数据
     */
    private String changeLoopTypeToJson(String source, String loopType) {
        Log.d(TAG, "changeLoopTypeToJson: loopType = " + loopType);
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(source).setLoopType(loopType);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeLoopTypeToJson: data = " + data);
        return data;
    }

    /**
     * 将循环模式转化为json数据
     *
     * @param source       当前的音源
     * @param connectState 当前的设备状态
     * @return 循环模式的json数据
     */
    private String changeDeviceStateToJson(String source, String connectState) {
        Log.d(TAG, "changeDeviceStateToJson: connectState = " + connectState);
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(source).setConnectState(connectState);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeDeviceStateToJson: data = " + data);
        return data;
    }

    /**
     * 将当前音源转化为json
     *
     * @param source 当前音源
     * @return json数据
     */
    private String changeSourceToJson(String source) {
        MediaInfoBean.Builder builder = new MediaInfoBean.Builder().setSource(source);
        MediaInfoBean mediaInfoBean = builder.created();
        Gson gson = new Gson();
        String data = gson.toJson(mediaInfoBean);
        Log.d(TAG, "changeSourceToJson: data = " + data);
        return data;
    }

    private com.desaysv.svlibmediaobserver.bean.MediaInfoBean changeFileMessageToAppMedia(String currentSource, FileMessage fileMessage) {
        com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder appBuilder = new com.desaysv.svlibmediaobserver.bean.MediaInfoBean.Builder();
        appBuilder.setSource(currentSource);
        appBuilder.setTitle(fileMessage.getName());
        appBuilder.setAlbumUri(ContentUris.withAppendedId(ART_URI, fileMessage.getAlbumId()).toString());
        appBuilder.setPath(fileMessage.getPath());
        return appBuilder.Build();
    }
}
