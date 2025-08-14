package com.desaysv.moduleusbmusic.utils;

import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.utils.SourceTypeUtils;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.control.MediaControlTool;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.localmediasdk.bean.MediaInfoBean;
import com.desaysv.localmediasdk.bean.PackageConfig;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.utils.MusicSetting;
import com.desaysv.moduleusbmusic.businesslogic.listdata.USBMusicDate;
import com.desaysv.moduleusbmusic.dataPoint.ContentData;
import com.desaysv.moduleusbmusic.dataPoint.LocalMusicPoint;
import com.desaysv.moduleusbmusic.dataPoint.PointValue;
import com.desaysv.moduleusbmusic.dataPoint.UsbMusicPoint;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;
import java.util.Objects;


/**
 * Created by LZM on 2020-3-23
 * Comment 对外实现的媒体控制器，根据当前的音源，判断需要调用什么控制器
 */
public class MusicControlUtils {

    private static final String TAG = "MusicControlUtils";

    private static MusicControlUtils instance;

    public static MusicControlUtils getInstance() {
        if (instance == null) {
            synchronized (MusicControlUtils.class) {
                if (instance == null) {
                    instance = new MusicControlUtils();
                }
            }
        }
        return instance;
    }

    private MusicControlUtils() {

    }

    /**
     * 播放
     */
    public void play(String source) {
        Log.d(TAG, "play: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.START, ChangeReasonData.AIDL);
            UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.START, ChangeReasonData.AIDL);
                LocalMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.START, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.START, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                break;
        }
    }

    /**
     * 暂停
     */
    public void pause(String source) {
        Log.d(TAG, "pause: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.PAUSE, ChangeReasonData.AIDL);
            UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.PAUSE, ChangeReasonData.AIDL);
                LocalMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.PAUSE, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.PAUSE, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                break;
        }
    }

    /**
     * 播放暂停
     */
    public void playOrPause(String source) {
        Log.d(TAG, "playOrPause: source = " + source);
        if (isRecentMediaType()) {
            if (ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getStatusTool().isPlaying()) {
                UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            } else {
                UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            }
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.AIDL);
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().isPlaying()) {
                    LocalMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                } else {
                    LocalMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                }
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.AIDL);
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().isPlaying()) {
                    UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                } else {
                    UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                }
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.AIDL);
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                if (ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getStatusTool().isPlaying()) {
                    UsbMusicPoint.getInstance().pause(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                } else {
                    UsbMusicPoint.getInstance().play(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                }
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.PLAY_OR_PAUSE, ChangeReasonData.AIDL);
                break;
        }
    }

    /**
     * 下一曲
     */
    public void next(String source) {
        Log.d(TAG, "next: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.NEXT, ChangeReasonData.AIDL);
            UsbMusicPoint.getInstance().next(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.NEXT, ChangeReasonData.AIDL);
                LocalMusicPoint.getInstance().next(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.NEXT, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().next(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.NEXT, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().next(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                break;
        }
    }

    /**
     * 上一曲
     */
    public void pre(String source) {
        Log.d(TAG, "pre: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.PRE, ChangeReasonData.AIDL);
            UsbMusicPoint.getInstance().pre(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.PRE, ChangeReasonData.AIDL);
                LocalMusicPoint.getInstance().pre(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.PRE, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().pre(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.PRE, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().pre(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                break;
        }
    }

    /**
     * 上一曲
     */
    public void seekTo(String source, String info) {
        Gson gson = new Gson();
        MediaInfoBean mediaInfoBean = gson.fromJson(info, MediaInfoBean.class);
        Log.d(TAG, "seekTo: source = " + source + " time = " + mediaInfoBean.getSeekToTime());
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.SEEKTO, ChangeReasonData.AIDL, mediaInfoBean.getSeekToTime());
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.SEEKTO, ChangeReasonData.AIDL, mediaInfoBean.getSeekToTime());
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.SEEKTO, ChangeReasonData.AIDL, mediaInfoBean.getSeekToTime());
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.SEEKTO, ChangeReasonData.AIDL, mediaInfoBean.getSeekToTime());
                break;
        }
    }

    /**
     * 开始快进
     */
    public void startFastForward(String source) {
        Log.d(TAG, "startFastForward: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.FAST_FORWARD, ChangeReasonData.AIDL);
            UsbMusicPoint.getInstance().seekForward(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.FAST_FORWARD, ChangeReasonData.AIDL);
                LocalMusicPoint.getInstance().seekForward(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.FAST_FORWARD, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().seekForward(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.FAST_FORWARD, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().seekForward(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                break;
        }
    }

    /**
     * 开始快退
     */
    public void stopFastForward(String source) {
        Log.d(TAG, "stopFastForward: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.FAST_FORWARD_STOP, ChangeReasonData.AIDL);
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.FAST_FORWARD_STOP, ChangeReasonData.AIDL);
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.FAST_FORWARD_STOP, ChangeReasonData.AIDL);
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.FAST_FORWARD_STOP, ChangeReasonData.AIDL);
                break;
        }
    }

    /**
     * 开始快退
     */
    public void startRewind(String source) {
        Log.d(TAG, "startRewind: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.REWIND, ChangeReasonData.AIDL);
            UsbMusicPoint.getInstance().seekBackward(getContentData(ModuleUSBMusicTrigger.getInstance().RecentMusicStatusTool.getCurrentPlayItem()));
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.REWIND, ChangeReasonData.AIDL);
                LocalMusicPoint.getInstance().seekBackward(getContentData(ModuleUSBMusicTrigger.getInstance().LocalMusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.REWIND, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().seekBackward(getContentData(ModuleUSBMusicTrigger.getInstance().USB1MusicStatusTool.getCurrentPlayItem()));
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.REWIND, ChangeReasonData.AIDL);
                UsbMusicPoint.getInstance().seekBackward(getContentData(ModuleUSBMusicTrigger.getInstance().USB2MusicStatusTool.getCurrentPlayItem()));
                break;
        }
    }

    /**
     * 停止快退
     */
    public void stopRewind(String source) {
        Log.d(TAG, "stopRewind: source = " + source);
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(MediaAction.REWIND_STOP, ChangeReasonData.AIDL);
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(MediaAction.REWIND_STOP, ChangeReasonData.AIDL);
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(MediaAction.REWIND_STOP, ChangeReasonData.AIDL);
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(MediaAction.REWIND_STOP, ChangeReasonData.AIDL);
                break;
        }
    }

    /**
     * 切换循环模式
     *
     * @param source 需要切换循环模式的音源
     */
    public void changeLoopType(String source, String info) {
        Log.d(TAG, "changeLoopType: source = " + source + " info = " + info);
        MediaInfoBean mediaInfoBean = new Gson().fromJson(info, MediaInfoBean.class);
        MediaAction action;
        if (TextUtils.isEmpty(mediaInfoBean.getLoopType())) {
            action = MediaAction.CHANGE_LOOP_TYPE;
        } else {
            switch (mediaInfoBean.getLoopType()) {
                case USBConstants.LoopType.CYCLE:
                    action = MediaAction.CYCLE;
                    break;
                case USBConstants.LoopType.RANDOM:
                    action = MediaAction.RANDOM;
                    break;
                case USBConstants.LoopType.SINGLE:
                    action = MediaAction.SINGLE;
                    break;
                default:
                    action = MediaAction.CHANGE_LOOP_TYPE;
                    break;
            }
        }
        if (isRecentMediaType()) {
            ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool.processCommand(action, ChangeReasonData.AIDL);
            usbPointLoopType(ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getStatusTool().getLoopType());
            return;
        }
        switch (source) {
            case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool.processCommand(action, ChangeReasonData.AIDL);
                localPointLoopType(ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getLoopType());
                break;
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool.processCommand(action, ChangeReasonData.AIDL);
                usbPointLoopType(ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getLoopType());
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool.processCommand(action, ChangeReasonData.AIDL);
                usbPointLoopType(ModuleUSBMusicTrigger.getInstance().getUSB2MusicControlTool.getStatusTool().getLoopType());
                break;
        }
    }

    /**
     * 本地改变循环类型通知埋点
     *
     * @param type type
     */
    private void localPointLoopType(String type) {
        switch (type) {
            case USBConstants.LoopType.CYCLE:
                LocalMusicPoint.getInstance().cycleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                break;
            case USBConstants.LoopType.RANDOM:
                LocalMusicPoint.getInstance().randomMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                break;
            case USBConstants.LoopType.SINGLE:
                LocalMusicPoint.getInstance().singleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                break;
        }
    }

    /**
     * USB改变循环类型通知埋点
     *
     * @param type type
     */
    private void usbPointLoopType(String type) {
        switch (type) {
            case USBConstants.LoopType.CYCLE:
                UsbMusicPoint.getInstance().cycleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                break;
            case USBConstants.LoopType.RANDOM:
                UsbMusicPoint.getInstance().randomMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                break;
            case USBConstants.LoopType.SINGLE:
                UsbMusicPoint.getInstance().singleMode(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
                break;
        }
    }

    /**
     * 启动音源和界面
     */
    public void openSource(String source, boolean isForeground, int flag, ChangeReason changeReason) {
        Log.d(TAG, "openSource: source = " + source + " isForeground = " + isForeground + " flag = " + flag + " changeReason = " + changeReason);
        IControlTool controlTool = null;
        // by LYM 如果当前是 最近播放，并且打开方式 不是  MODE, 则 继续播放-> 最近播放
        // 最近播放列表不加入MODE切换逻辑；
        if (isRecentMediaType() && !Objects.equals(changeReason.getReason(), ChangeReasonData.MODE_CONTROL.getReason())) {
            Log.i(TAG, "openSource: to recent play");
            controlTool = ModuleUSBMusicTrigger.getInstance().RecentMusicControlTool;
            //如果没有数据则先设置播放数据
            if (ModuleUSBMusicTrigger.getInstance().getRecentMusicControlTool.getStatusTool().getPlayList() == null) {
                controlTool.setPlayList(USBMusicDate.getInstance().getRecentMusicAllList(), CurrentPlayListType.ALL);
            }
        } else {
            switch (source) {
                case DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE:
                    controlTool = ModuleUSBMusicTrigger.getInstance().LocalMusicControlTool;
                    //如果没有数据则先设置播放数据
                    if (ModuleUSBMusicTrigger.getInstance().getLocalMusicControlTool.getStatusTool().getPlayList() == null) {
                        List<FileMessage> allList = USBMusicDate.getInstance().getLocalMusicAllList();
                        //只有列表没有时才检测
                        if (allList.isEmpty()) {
                            changeMusicList(allList, MediaType.LOCAL_MUSIC);
                        }
                        controlTool.setPlayList(allList, CurrentPlayListType.ALL);
                    }
                    break;
                case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                    controlTool = ModuleUSBMusicTrigger.getInstance().USB1MusicControlTool;
                    if (ModuleUSBMusicTrigger.getInstance().getUSB1MusicControlTool.getStatusTool().getPlayList() == null) {
                        List<FileMessage> allList = USBMusicDate.getInstance().getUSB1MusicAllList();
                        //只有列表没有时才检测
                        if (allList.isEmpty()) {
                            //非基本变量是传地址，不要修改原来的值
                            changeMusicList(allList, MediaType.USB1_MUSIC);
                        }
                        controlTool.setPlayList(allList, CurrentPlayListType.ALL);
                    }
                    break;
                case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                    controlTool = ModuleUSBMusicTrigger.getInstance().USB2MusicControlTool;
                    if (ModuleUSBMusicTrigger.getInstance().getUSB2MusicControlTool.getStatusTool().getPlayList() == null) {
                        List<FileMessage> allList = USBMusicDate.getInstance().getUSB2MusicAllList();
                        //只有列表没有时才检测
                        if (allList.isEmpty()) {
                            changeMusicList(allList, MediaType.USB2_MUSIC);
                        }
                        controlTool.setPlayList(allList, CurrentPlayListType.ALL);
                    }
                    break;
            }
        }
        if (controlTool == null) {
            Log.w(TAG, "openSource: source = " + source + " is no music source");
            return;
        }
        controlTool.processCommand(MediaAction.OPEN, changeReason, MediaControlTool.NEED_TO_FIND_THE_SAVE_MEDIA);
        if (DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(source)) {
            LocalMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
        } else {
            UsbMusicPoint.getInstance().open(new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click));
        }
        //此时需启动界面
        if (isForeground) {
            Intent intent = new Intent();
            intent.setClassName(PackageConfig.MUSIC_APP_PACKAGE, "com.desaysv.svaudioapp.ui.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.Source.SOURCE_KEY, source);
            intent.putExtra(Constants.NavigationFlag.KEY, flag);
            AppBase.mContext.startActivity(intent);
        }
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

    /**
     * 封装曲目信息
     *
     * @param currentPlayItem currentPlayItem
     * @return ContentData[]
     */
    private ContentData[] getContentData(FileMessage currentPlayItem) {
        ContentData[] contentData = new ContentData[4];
        contentData[0] = new ContentData(PointValue.Field.OperStyle, PointValue.OperStyleValue.Click);
        contentData[1] = new ContentData(PointValue.Field.ProgramName, currentPlayItem.getName());
        contentData[2] = new ContentData(PointValue.Field.Author, currentPlayItem.getAuthor());
        contentData[3] = new ContentData(PointValue.Field.Album, currentPlayItem.getAlbum());
        return contentData;
    }

    /**
     * 获取需要音源恢复的文件是不是在文件夹中，如果在的话，并且数据库还没有扫到它，就需要将它放在列表的第一位
     */
    public static void changeMusicList(List<FileMessage> musicList, MediaType mediaType) {
        //获取底层保存的文件
        FileMessage fileMessage = MediaPlayStatusSaveUtils.getInstance().getMediaFileMessage(mediaType);
        Log.d(TAG, "changeMusicList: fileMessage = " + fileMessage);
        if (fileMessage == null) {
            //获取到底层记忆的播放文件是空的,直接return
            Log.d(TAG, "changeMusicList: fileMessage is null");
            return;
        }
        String path = fileMessage.getPath();
        //这里有判断路径是否正确，如果不正确，后面也跑不下去
        boolean isFileExists = checkSaveFileExists(path);
        Log.d(TAG, "changeMusicList: isFileExists = " + isFileExists);
        if (!isFileExists) {
            //文件不在，直接return
            return;
        }
        Log.d(TAG, "changeMusicList: fileMessage = " + fileMessage);
        //将音源恢复的列表放在第一位
        musicList.add(0, fileMessage);
    }

    /**
     * 检测记忆的文件是否存在
     *
     * @return true 存在 false 不存在
     */
    public static boolean checkSaveFileExists(String path) {
        Log.i(TAG, "checkSaveFileExists: path = " + path);
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        boolean exists = file.exists();
        if (!exists) {
            Log.i(TAG, "checkSaveFileExists: file is no exists");
            return false;
        }
        String storageState = Environment.getExternalStorageState(file);
        Log.i(TAG, "checkSaveFileExists: storageState = " + storageState);
        //这里有个问题，当U盘移除在EJECT但是不在UNMOUNTED时，exists返回真,所以必须校验当前路径是挂载状态
        return Environment.MEDIA_MOUNTED.equals(storageState);
    }
}
