package com.desaysv.libusbmedia.control;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.audiosdk.manager.audiofocusmanager.DsvAudioFocusManager;
import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libusbmedia.action.MediaControlAction;
import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IPlayControl;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReason;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;

import java.io.File;
import java.util.List;
import java.util.Random;


/**
 * Created by uidp5370 on 2019-6-12.
 * 媒体控制器，里面包含了媒体控制的业务逻辑
 */

public class MediaControlTool implements IControlTool {

    private static final String TAG = "MediaControlTool";

    private static MediaControlTool USB1MusicControlTool;
    private static MediaControlTool USB2MusicControlTool;
    private static MediaControlTool USB1VideoControlTool;
    private static MediaControlTool USB2VideoControlTool;
    private static MediaControlTool LocalMusicControlTool;
    private static MediaControlTool RecentMusicControlTool;
    private final MediaType mCurrentMediaType;
    private String mCurrentSourceType = "null";
    private final Random random;
    private final CurrentPlayInfo mCurrentPlayInfor;
    private final IPlayControl mMusicPlayControl;
    private final Handler mHandler;

    /**
     * 根据mediaType获取各个音源的控制单例
     *
     * @param mediaType USB1_MUSIC,USB2_MUSIC,USB1_VIDEO,USB2_VIDEO,
     * @return 媒体控制器的单例
     */
    public static MediaControlTool getInstance(MediaType mediaType) {
        Log.d(TAG, "getInstance: mediaType = " + mediaType);
        if (mediaType == MediaType.USB1_MUSIC) {
            if (USB1MusicControlTool == null) {
                synchronized (MediaControlTool.class) {
                    if (USB1MusicControlTool == null) {
                        USB1MusicControlTool = new MediaControlTool(mediaType);
                    }
                }
            }
            return USB1MusicControlTool;
        } else if (mediaType == MediaType.USB2_MUSIC) {
            if (USB2MusicControlTool == null) {
                synchronized (MediaControlTool.class) {
                    if (USB2MusicControlTool == null) {
                        USB2MusicControlTool = new MediaControlTool(mediaType);
                    }
                }
            }
            return USB2MusicControlTool;
        } else if (mediaType == MediaType.USB1_VIDEO) {
            if (USB1VideoControlTool == null) {
                synchronized (MediaControlTool.class) {
                    if (USB1VideoControlTool == null) {
                        USB1VideoControlTool = new MediaControlTool(mediaType);
                    }
                }
            }
            return USB1VideoControlTool;
        } else if (mediaType == MediaType.USB2_VIDEO) {
            if (USB2VideoControlTool == null) {
                synchronized (MediaControlTool.class) {
                    if (USB2VideoControlTool == null) {
                        USB2VideoControlTool = new MediaControlTool(mediaType);
                    }
                }
            }
            return USB2VideoControlTool;
        } else if (mediaType == MediaType.LOCAL_MUSIC) {
            if (LocalMusicControlTool == null) {
                synchronized (MediaControlTool.class) {
                    if (LocalMusicControlTool == null) {
                        LocalMusicControlTool = new MediaControlTool(mediaType);
                    }
                }
            }
            return LocalMusicControlTool;
        } else if (mediaType == MediaType.RECENT_MUSIC) {
            if (RecentMusicControlTool == null) {
                synchronized (MediaControlTool.class) {
                    if (RecentMusicControlTool == null) {
                        RecentMusicControlTool = new MediaControlTool(mediaType);
                    }
                }
            }
            return RecentMusicControlTool;
        }
        Log.e(TAG, "getInstance: with error parameter");
        return null;
    }

    /**
     * 构造函数
     *
     * @param mediaType USB1_MUSIC,USB2_MUSIC,USB1_VIDEO,USB2_VIDEO,
     */
    private MediaControlTool(MediaType mediaType) {
        Log.d(TAG, "MediaControlTool: ");
        mCurrentPlayInfor = CurrentPlayInfo.getInstance(mediaType);
        mMusicPlayControl = MediaControlAction.getInstance(mediaType);
        mCurrentMediaType = mediaType;
        random = new Random();
        setCurrentSourceType();
        HandlerThread thread = new HandlerThread(mCurrentMediaType + "");
        thread.start();
        //FIXME:钟大师说，原来操作是在子线程的，而获取是在主线程的，两个线程操作MediaProvider，会出现线程死锁，所以只能把操作修改在主线程里面了
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dealMessage(msg);
            }
        };

    }


    /**
     * 初始化音频焦点的属性
     */
    private void setCurrentSourceType() {
        switch (mCurrentMediaType) {
            case USB1_MUSIC:
                mCurrentSourceType = DsvAudioSDKConstants.USB0_MUSIC_SOURCE;
                break;
            case USB2_MUSIC:
                mCurrentSourceType = DsvAudioSDKConstants.USB1_MUSIC_SOURCE;
                break;
            case USB1_VIDEO:
                mCurrentSourceType = DsvAudioSDKConstants.USB0_VIDEO_SOURCE;
                break;
            case USB2_VIDEO:
                mCurrentSourceType = DsvAudioSDKConstants.USB1_VIDEO_SOURCE;
                break;
            case LOCAL_MUSIC:
                mCurrentSourceType = DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE;
                break;
            case RECENT_MUSIC:
                mCurrentSourceType = MediaType.RECENT_MUSIC.name();
                break;
        }
    }

    private static final int DEFAULT_DATA = -100;
    private static final int DEFAULT_MESSAGE_WHAT = 1;

    /**
     * 命令集成类
     */
    private static class MessageData {

        MediaAction mediaAction;
        ChangeReason changeReason;
        int data = DEFAULT_DATA;

        MessageData(MediaAction mediaAction, ChangeReason changeReason) {
            this.mediaAction = mediaAction;
            this.changeReason = changeReason;
        }

        MessageData(MediaAction mediaAction, ChangeReason changeReason, int data) {
            this.mediaAction = mediaAction;
            this.changeReason = changeReason;
            this.data = data;
        }
    }

    /**
     * 媒体控制的入口，接口对外
     *
     * @param mediaAction  触发媒体控制的动作
     * @param changeReason 触发媒体控制的原因
     */
    @Override
    public void processCommand(MediaAction mediaAction, ChangeReason changeReason) {
        Log.d(TAG, "processCommand: mediaAction = " + mediaAction + " changeReason = " + changeReason);
        mHandler.sendMessage(mHandler.obtainMessage(DEFAULT_MESSAGE_WHAT, new MessageData(mediaAction, changeReason)));
    }

    /**
     * 触发媒体控制的入口，接口对外
     *
     * @param mediaAction  触发媒体控制的动作 OPEN;SEEKTO;
     * @param changeReason 触发媒体控制的原因
     * @param data         媒体控制的数据
     */
    @Override
    public void processCommand(MediaAction mediaAction, ChangeReason changeReason, int data) {
        Log.d(TAG, "processCommand: mediaAction = " + mediaAction + " mCurrentMediaType = " + mCurrentMediaType + " changeReason = " + changeReason + " data = " + data);
        mHandler.sendMessage(mHandler.obtainMessage(DEFAULT_MESSAGE_WHAT, new MessageData(mediaAction, changeReason, data)));
    }

    private void dealMessage(Message message) {
        MessageData messageData = (MessageData) message.obj;
        MediaAction mediaAction = messageData.mediaAction;
        ChangeReason changeReason = messageData.changeReason;
        int data = messageData.data;
        Log.d(TAG, "dealMessage: mediaAction = " + mediaAction + " changeReason = " + changeReason + " data = " + data);
        switch (mediaAction) {
            case OPEN:
                //这里是没有判断changeReason里面的优先级，判断的是路径以及列表的有效性
                mCurrentPlayInfor.setOpenReason(changeReason);
                resetPlayStatusByReason(changeReason);
                Uri path = isEnableOpen(data, changeReason);
                if (path != null) {
                    mMusicPlayControl.openMedia(path);
                }
                //这里是判断标志位是否能清除的逻辑,如果优先级和停止，或者暂停的优先级一样，或者高于，则可以将优先级清除掉
                if (changeReason.getPrority() <= mCurrentPlayInfor.getPauseReason().getPrority()) {
                    mCurrentPlayInfor.setPauseReason(ChangeReasonData.NA);
                }
                break;
            case SEEKTO:
                int position = checkSeekToTime(data, changeReason);
                if (position == PLAY_NEXT_MEDIA_ITEM) {
                    processCommand(MediaAction.NEXT, ChangeReasonData.SEEK_TO);
                }
                //如果MediaPlay没有准备好，哪后面的跳转逻辑就不需要实现了
                if (position == NOT_EFFECT_SEEK) {
                    Log.e(TAG, "dealMessage: position = NOT_EFFECT_SEEK");
                    return;
                }
                mMusicPlayControl.seekTo(position);
                if (changeReason != ChangeReasonData.CLICK_STOP) {
                    processCommand(MediaAction.START, changeReason);
                }
                break;
            case PLAY_OR_PAUSE:
                if (!mCurrentPlayInfor.isPlaying()) {
                    processCommand(MediaAction.START, changeReason);
                } else {
                    processCommand(MediaAction.PAUSE, changeReason);
                }
                break;
            case PRE:
                resetPlayStatusByReason(changeReason);
                pre(changeReason);
                break;
            case NEXT:
                resetPlayStatusByReason(changeReason);
                if (!needAudioFocusTrigger(changeReason)) {
                    next(changeReason);
                }
                break;
            case STOP:
                if (isEnableStop(changeReason)) {
                    mMusicPlayControl.stop();
                    //这里是判断标志位是否能清除的逻辑,如果优先级和停止，或者暂停的优先级一样，或者高于，则可以将优先级清除掉
                    if (changeReason.getPrority() <= mCurrentPlayInfor.getPauseReason().getPrority()) {
                        mCurrentPlayInfor.setPauseReason(changeReason);
                    }
                }
                break;
            case RELEASE:
                if (isEnableRelease(changeReason)) {
                    abandonFocus();
                    mMusicPlayControl.release();
                }
                break;
            case PAUSE:
                mMusicPlayControl.pause();
                savePauseStatus(changeReason);
                if (isEnablePause(changeReason)) {
                    mCurrentPlayInfor.setPauseReason(changeReason);
                }
                break;
            case START:
                if (isEnableStart(changeReason)) {
                    mMusicPlayControl.start();
                    //mCurrentPlayInfor.setPauseReason(ChangeReasonData.NA);
                    MediaPlayStatusSaveUtils.getInstance().saveMediaPlayPauseStatus(getAudioType(), true);
                }
                break;
            case REWIND:
            case REWIND_STOP:
            case FAST_FORWARD:
            case FAST_FORWARD_STOP:
                mMusicPlayControl.autoSeek(mediaAction);
                break;
            case CHANGE_LOOP_TYPE:
                switchLoopType();
                break;
            case SINGLE:
                switchLoopType(USBConstants.LoopType.SINGLE);
                break;
            case RANDOM:
                switchLoopType(USBConstants.LoopType.RANDOM);
                break;
            case CYCLE:
                switchLoopType(USBConstants.LoopType.CYCLE);
                break;
        }
    }

    /**
     * 根据状态判断是否需要重置播放状态
     * 在点击上下曲等，点击条目播放等，解除暂停标志位
     * 避免无法
     *
     * @param changeReason changeReason
     */
    private void resetPlayStatusByReason(ChangeReason changeReason) {
        Log.d(TAG, "resetPlayStatusByReason: changeReason = " + changeReason);
        if (ChangeReasonData.AIDL.equals(changeReason)
                || ChangeReasonData.CLICK.equals(changeReason)) {
            //注意、需要区分视频和音乐
            MediaPlayStatusSaveUtils.getInstance().saveMediaPlayPauseStatus(getAudioType(), true);
        }
    }

    /**
     * 根据暂停态是否主动来进行保存状态
     *
     * @param changeReason changeReason
     */
    private void savePauseStatus(ChangeReason changeReason) {
        if (ChangeReasonData.AIDL.equals(changeReason)
                || ChangeReasonData.CLICK.equals(changeReason)
                || ChangeReasonData.LONG_LOSS_SOURCE.equals(changeReason)) {
            MediaPlayStatusSaveUtils.getInstance().saveMediaPlayPauseStatus(getAudioType(), false);
        }
    }

    /**
     * 设置播放列表，接口对外
     *
     * @param playList            当前的播放列表
     * @param currentPlayListType 当前播放列表的类型
     */
    @Override
    public void setPlayList(List<FileMessage> playList, CurrentPlayListType currentPlayListType) {
        mCurrentPlayInfor.setCurrentPlayList(playList);
        mCurrentPlayInfor.setCurrentPlayListType(currentPlayListType);
    }


    /**
     * 上一曲的逻辑实现
     *
     * @param changeReason 执行上一曲的原因
     */
    private void pre(ChangeReason changeReason) {
        int position = getPrePosition(changeReason);
        Log.d(TAG, "pre: position = " + position);
        if (position != -1) {
            processCommand(MediaAction.OPEN, ChangeReasonData.PRE, position);
        }
    }

    /**
     * 获取需要跳转到上一曲的什么位置
     *
     * @param changeReason 执行的原因
     * @return position 需要播放的位置
     */
    private int getPrePosition(ChangeReason changeReason) {
        int position;
        Log.d(TAG, "getPrePosition:  changeReason = " + changeReason);
        List<FileMessage> mCurrentPlayList = mCurrentPlayInfor.getCurrentPlayList();
        if (mCurrentPlayList == null || mCurrentPlayList.isEmpty()) {
            Log.w(TAG, "getPrePosition: List fail");
            processCommand(MediaAction.STOP, ChangeReasonData.LIST_FAIL);
            return -1;
        }
        String loopType = mCurrentPlayInfor.getLoopType();
        position = mCurrentPlayInfor.getCurrentPlayPosition() - 1;
        //奇瑞需求随机模式的上一曲是返回列表的上一首BUG20240319_11595
        /*if (loopType.equals(USBConstants.LoopType.RANDOM)) {
            //还原position
            position = position + 1;
            for (int i = 0; i < 5; i++) {
                int randomPosition = random.nextInt(mCurrentPlayList.size());
                Log.d(TAG, "getPrePosition: randomPosition = " + randomPosition + " position = " + position);
                if (randomPosition != position) {
                    position = randomPosition;
                    break;
                }
            }
        }*/
        Log.i(TAG, "getPrePosition: position = " + position + " size = " + mCurrentPlayList.size() + " loopType = " + loopType);
        if (position < 0) {
            position = mCurrentPlayList.size() - 1;
        }
        Log.d(TAG, "getPrePosition: position = " + position);
        return position;
    }

    /**
     * 执行下一曲的动作
     *
     * @param changeReason 执行下一曲的原因
     */
    private void next(ChangeReason changeReason) {
        int position = getNextPosition(changeReason);
        Log.d(TAG, "next: position = " + position);
        if (position != -1) {
            processCommand(MediaAction.OPEN, ChangeReasonData.NEXT, position);
        }
    }

    /**
     * 获取需要播放下一曲的位置
     *
     * @param changeReason 执行下一曲的原因
     * @return position 需要播放的位置
     */
    private int getNextPosition(ChangeReason changeReason) {
        int position;
        List<FileMessage> mCurrentPlayList = mCurrentPlayInfor.getCurrentPlayList();
        if (mCurrentPlayList == null || mCurrentPlayList.isEmpty()) {
            Log.w(TAG, "getNextPosition: List fail");
            processCommand(MediaAction.STOP, ChangeReasonData.LIST_FAIL);
            return -1;
        }
        //FIXME:如果异常歌曲在最后一首，就不切换下一曲了
        if (changeReason == ChangeReasonData.ON_ERROR) {
            if (mCurrentPlayInfor.getCurrentPlayPosition() == (mCurrentPlayList.size() - 1)) {
                Log.w(TAG, "getNextPosition: the error item is in the last");
                return -1;
            }
        }
        String loopType = mCurrentPlayInfor.getLoopType();
        Log.d(TAG, "getNextPosition: changeReason = " + changeReason + " loopType = " + loopType);
        position = mCurrentPlayInfor.getCurrentPlayPosition() + 1;
        if (loopType.equals(USBConstants.LoopType.RANDOM)) {
            //还原position
            position = position - 1;
            for (int i = 0; i < 5; i++) {
                int randomPosition = random.nextInt(mCurrentPlayList.size());
                Log.d(TAG, "getNextPosition: randomPosition = " + randomPosition + " position = " + position);
                if (randomPosition != position) {
                    position = randomPosition;
                    break;
                }
            }
        }
        if (changeReason.equals(ChangeReasonData.ON_COMPLETE)) {
            if (loopType.equals(USBConstants.LoopType.SINGLE)) {
                position = mCurrentPlayInfor.getCurrentPlayPosition();
            } else if (loopType.equals(USBConstants.LoopType.NULL)) {
                return -1;
            }
        }
        if (position > mCurrentPlayList.size() - 1) {
            position = 0;
        }
        return position;
    }

    /**
     * 是否能够暂停
     *
     * @param changeReason 暂停的原因
     * @return true：能够暂停，false：不能暂停
     */
    private boolean isEnablePause(ChangeReason changeReason) {
        Log.d(TAG, "isEnablePause: changeReason = " + changeReason);
        return changeReason.getPrority() <= mCurrentPlayInfor.getPauseReason().getPrority();
    }

    /**
     * 是否能够播放
     *
     * @param changeReason 播放的原因,还有一个优先级判断逻辑，如果是由于高优先级导致的暂停，只能由高优先级的start打断
     * @return true：能够播放，false：不能播放
     */
    private boolean isEnableStart(ChangeReason changeReason) {
        Log.d(TAG, "isEnableStart: changeReason = " + changeReason);
        if (ChangeReasonData.BOOT_RESUME.equals(mCurrentPlayInfor.getOpenReason())
                && (ChangeReasonData.ON_PREPARE.equals(changeReason)
                || ChangeReasonData.CLICK_SAME_ITEM.equals(changeReason)
                || ChangeReasonData.BOOT_RESUME.equals(changeReason)
                || ChangeReasonData.SOURCE.equals(changeReason))
                && !MediaPlayStatusSaveUtils.getInstance().getMediaPlayPauseStatus(getAudioType())) {
            //如果当前的open时候是音源恢复的，且当前是解码准备完毕才START的需判断当前暂停条件
            Log.i(TAG, "isEnableStart: last boot is pause status ！！！");
            //return false;
        }
        if (changeReason.getPrority() > mCurrentPlayInfor.getPauseReason().getPrority()) {
            return false;
        }
        if (ChangeReasonData.FAST_FORWARD.equals(changeReason)) {
            setPlaySpeed(MediaControlAction.DOUBLE_SPEED);
            if (!mCurrentPlayInfor.isPlaying()) {
                return false;
            }
        }
        if (ChangeReasonData.FAST_FORWARD_CANCAL.equals(changeReason)) {
            setPlaySpeed(MediaControlAction.DEFAULT_SPEED);
            if (!mCurrentPlayInfor.isPlaying()) {
                return false;
            }
        }
        mCurrentPlayInfor.setPauseReason(ChangeReasonData.NA);
        return requestFocus();
    }

    /**
     * 判断是否能停止播放,同时清楚记忆的播放状态
     *
     * @param changeReason 停止的原因
     * @return true：能够停止 false：不能停止
     */
    private boolean isEnableStop(ChangeReason changeReason) {
        Log.d(TAG, "isEnableStop: changeReason = " + changeReason);
        mCurrentPlayInfor.setCurrentPlayItem(new FileMessage());
        mCurrentPlayInfor.setCurrentPlayPosition(0);
        return true;
    }

    /**
     * 是否能够释放
     *
     * @param changeReason 释放的原因
     * @return true：能够释放，false：不能释放
     */
    private boolean isEnableRelease(ChangeReason changeReason) {
        Log.d(TAG, "isEnableRelease: changeReason = " + changeReason + " mCurrentMediaType = " + mCurrentMediaType);
        return true;
    }

    public static final int NEED_TO_FIND_THE_SAVE_MEDIA = -1;

    /**
     * 检测设备状态
     *
     * @return true 有设备状态 false 无设备状态
     */
    private boolean usbDeviceConnected() {
        Log.d(TAG, "getDeviceStatus: mCurrentMediaType = " + mCurrentMediaType);
        if (mCurrentMediaType == MediaType.USB1_MUSIC || mCurrentMediaType == MediaType.USB1_VIDEO) {
            return DeviceStatusBean.getInstance().isUSB1Connect();
        } else if (mCurrentMediaType == MediaType.USB2_MUSIC || mCurrentMediaType == MediaType.USB2_VIDEO) {
            return DeviceStatusBean.getInstance().isUSB2Connect();
        }
        return false;
    }

    /**
     * 逻辑判断，是否能够打开
     *
     * @param position 需要打开的文件在列表中的位置
     * @return path null为不能打开，其他为可以打开
     */
    private Uri isEnableOpen(int position, ChangeReason changeReason) {
        Uri path;
        Log.d(TAG, "isEnableOpen: position = " + position + " changeReason = " + changeReason);
        //if (!requestFocus()) {
        if (needAudioFocusTrigger(changeReason)) {
            return null;
        }
        //add by lzm 由于语音打开的逻辑，所以音源申请不一定会成功，所以音频焦点不能作为open的频段值，而是要作为start的判断值
        if (mCurrentMediaType == MediaType.RECENT_MUSIC) {
            //最近播放需先有路径，才能申请对应的焦点
            path = findOpenPathByPosition(position, changeReason);
            requestFocus();
        } else {
            requestFocus();
            path = findOpenPathByPosition(position, changeReason);
        }
        return path;
    }

    /**
     * 用于调整原来的时序
     *
     * @param position     position
     * @param changeReason changeReason
     * @return Uri
     */
    private Uri findOpenPathByPosition(int position, ChangeReason changeReason) {
        Uri path = null;
        if (mCurrentPlayInfor.getCurrentPlayList() == null ||
                mCurrentPlayInfor.getCurrentPlayList().isEmpty()) {
            Log.e(TAG, "isEnableOpen: getCurrentPlayList isEmpty");
            return null;
        }
        if (position == NEED_TO_FIND_THE_SAVE_MEDIA) {
            position = findSaveMediaPosition();
        }
        if (position > mCurrentPlayInfor.getCurrentPlayList().size()) {
            //列表数据异常
            Log.e(TAG, "isEnableOpen: error position = " + position + " getCurrentPlayList = "
                    + mCurrentPlayInfor.getCurrentPlayList().size());
            return null;
        }
        FileMessage fileMessage = mCurrentPlayInfor.getCurrentPlayList().get(position);
        if (fileMessage == null) {
            Log.e(TAG, "isEnableOpen: fileMessage is null");
            return null;
        }
        File musicFile = new File(fileMessage.getPath());
        //add by lzm 如果切换的是相同歌曲，但是不同列表，也需要跟新列表位置
        mCurrentPlayInfor.setCurrentPlayPosition(position);
        if (isClickSameMusic(fileMessage, changeReason)) {
            Log.d(TAG, "isEnableOpen: isClickSameMusic");
            mCurrentPlayInfor.setCurrentPlayItem(fileMessage);
            return null;
        }
        mCurrentPlayInfor.setCurrentPlayItem(fileMessage);
        path = Uri.fromFile(musicFile);
        return path;
    }

    /**
     * 处理是否需要没有焦点时触发的逻辑
     *
     * @return boolean
     * 当前ON_COMPLETE或者ON_ERROR触发，且焦点不存在返回true，即此种情况需额外处理
     * 其他情况存在返回false，此种情况保持原先的逻辑
     */
    private boolean needAudioFocusTrigger(ChangeReason changeReason) {
        //针对的仅是ON_COMPLETE或者ON_ERROR触发两种状态
        if (ChangeReasonData.ON_COMPLETE.equals(changeReason) || ChangeReasonData.ON_ERROR.equals(changeReason)) {
            //add by lzm 由于语音打开的逻辑，所以音源申请不一定会成功，所以音频焦点不能作为open的频段值，而是要作为statr的判断值
            String sourceName = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceName(AppBase.mContext);
            Log.d(TAG, "needAudioFocusTrigger: sourceName = " + sourceName + " mCurrentMediaType = " + mCurrentMediaType);
            //当前是最近播放音乐的话不处理
            if (mCurrentMediaType == MediaType.RECENT_MUSIC) {
                if (DsvAudioSDKConstants.USB0_MUSIC_SOURCE.equals(sourceName)) {
                    return !MediaPlayStatusSaveUtils.getInstance().getMediaPlayPauseStatus(getAudioType());
                } else if (DsvAudioSDKConstants.USB1_MUSIC_SOURCE.equals(sourceName)) {
                    return !MediaPlayStatusSaveUtils.getInstance().getMediaPlayPauseStatus(getAudioType());
                } else if (DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE.equals(sourceName)) {
                    return !MediaPlayStatusSaveUtils.getInstance().getMediaPlayPauseStatus(getAudioType());
                } else {
                    return true;
                }
            }
            //如果是本地音乐的话
            if (mCurrentMediaType == MediaType.LOCAL_MUSIC) {
                return !MediaPlayStatusSaveUtils.getInstance().getMediaPlayPauseStatus(getAudioType());
            }
            //其它带视频的项目需要补充视频的源，广汽暂时不需要
            if (!getAudioType().equals(sourceName)) {
                return true;
            } else {
                //焦点存在，但是只有设备状态还在且不暂停的情况下才能去做媒体服务异常的恢复逻辑
                //这里还考量USB是否移除掉了
                // TODO: 2022-11-4 当前还添加了收藏到本地的逻辑策略，USB不存在的情况也可以播放
                return !MediaPlayStatusSaveUtils.getInstance().getMediaPlayPauseStatus(getAudioType())
                        || !usbDeviceConnected();
            }
        }
        return false;
    }

    /**
     * 判断是否打开的是相同的的文件，如果是的话，那就是用播放动作
     *
     * @param fileMessage 当前打开的文件
     * @return boolean true：是 false：否
     */
    private boolean isClickSameMusic(FileMessage fileMessage, ChangeReason changeReason) {
        //如果是START时，由于没有prepare或者由于mediaPlayer为空的情况下重新打开则认为不是相同音乐
        if (ChangeReasonData.UN_PREPARE.equals(changeReason)) {
            return false;
        }
        FileMessage currentPlayFile = mCurrentPlayInfor.getCurrentPlayItem();
        if (currentPlayFile != null) {
            if (fileMessage.getPath().equals(currentPlayFile.getPath())) {
                //add by lzm 需要做特殊逻辑，如果列表里面只有一首歌曲，则需要重新开始播放，不要列表里面只有一首歌曲，点击上下去无效
                if (mCurrentPlayInfor.getCurrentPlayList() == null) {
                    Log.w(TAG, "isClickSameMusic: mCurrentPlayList is null");
                    return true;
                }
                Log.d(TAG, "isClickSameMusic: mCurrentPlayList.size() = " + mCurrentPlayInfor.getCurrentPlayList().size());
                Log.d(TAG, "isClickSameMusic: ChangeReasonData = changeReason = " + changeReason);
                //add by lzm，列表只有一首歌的时候，上一曲，下一曲，要归零播放，自动播放的情况下要记忆播放时间
                if (mCurrentPlayInfor.getCurrentPlayList().size() == 1 &&
                        (changeReason == ChangeReasonData.NEXT || changeReason == ChangeReasonData.PRE)) {
                    processCommand(MediaAction.SEEKTO, ChangeReasonData.CLICK_SAME_ITEM, 0);
                } else {
                    processCommand(MediaAction.START, changeReason);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 如果需要寻找之前保存的位置，则需要从SharedPreferences里面去找保存的文件位置
     *
     * @return position 是否能找到，如果找不到就返回0，找得到就返回相应的位置
     */
    private int findSaveMediaPosition() {
        int position = 0;
        String savePath = MediaPlayStatusSaveUtils.getInstance().getMediaPlayPath(mCurrentMediaType);
        for (int i = 0; i < mCurrentPlayInfor.getCurrentPlayList().size(); i++) {
            if (savePath.contains(mCurrentPlayInfor.getCurrentPlayList().get(i).getPath())) {
                position = i;
                break;
            }
        }
        Log.d(TAG, "findSaveMediaPosition: savePath = " + savePath + " position = " + position);
        return position;
    }

    private final int PLAY_NEXT_MEDIA_ITEM = -1;

    private final int NOT_EFFECT_SEEK = -2;

    /**
     * 选择需要跳转的位置，这个是实现断点续播的功能
     *
     * @param position     当前要跳转的位置
     * @param changeReason 执行跳转的原因
     * @return position 这个position是经过处理，如果需要断点续播，就会返回需要恢复播放的位置
     */
    private int checkSeekToTime(int position, ChangeReason changeReason) {
        int totalPlayTime = mCurrentPlayInfor.getTotalPlayTime();
        Log.d(TAG, "checkSeekToTime: position = " + position + " totalPlayTime = " + totalPlayTime + " changeReason = " + changeReason);
        if (changeReason.equals(ChangeReasonData.ON_PREPARE)) {
            position = getSavePlayTime(position);
        }
        //如果totalPlayTime为0说明，mediaPlay还没有准备好，这个时候不需要做跳转逻辑
        if (totalPlayTime == 0) {
            return NOT_EFFECT_SEEK;
        }
        if (position > totalPlayTime) {
            return PLAY_NEXT_MEDIA_ITEM;
        } else if (position < 0) {
            return 0;
        }
        return position;
    }

    /**
     * 获取保存的播放时间
     *
     * @param position 当前的播放时间
     * @return position 如果有保存就返回保存的播放时间，如果没有保存，就返回原来的position
     */
    private int getSavePlayTime(int position) {
        //获取保存这个播放时间对于的媒体路径
        String timePath = MediaPlayStatusSaveUtils.getInstance().getMediaTimePath(mCurrentMediaType);
        if (timePath.contains(mCurrentPlayInfor.getCurrentPlayItem().getPath())) {
            position = MediaPlayStatusSaveUtils.getInstance().getMediaPlayTime(mCurrentMediaType);
        }
        Log.d(TAG, "getSavePlayTime: position = " + position + " savePath = " + timePath + " getPath = " + mCurrentPlayInfor.getCurrentPlayItem().getPath());
        return position;
    }

    private int mAudioFocus = -1;

    /**
     * 焦点申请的逻辑
     *
     * @return true：焦点申请成功；false：焦点申请失败
     */
    private boolean requestFocus() {
        Log.d(TAG, "requestFocus: mCurrentSourceType = " + mCurrentSourceType);
        //add by lz 变更，不再通过跨进程读取
        if (MediaType.RECENT_MUSIC == mCurrentMediaType) {
            //如果是最近音乐，则焦点申请需要查看实际播放的内容来申请本地还是USB
            FileMessage playItem = mCurrentPlayInfor.getCurrentPlayItem();
            String audioType = DsvAudioSDKConstants.USB0_MUSIC_SOURCE;
            if (playItem.getPath().startsWith(USBConstants.USBPath.LOCAL_PATH)) {
                audioType = DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE;
            } else if (playItem.getPath().startsWith(USBConstants.USBPath.USB0_PATH)) {
                audioType = DsvAudioSDKConstants.USB0_MUSIC_SOURCE;
            } else if (playItem.getPath().startsWith(USBConstants.USBPath.USB1_PATH)) {
                audioType = DsvAudioSDKConstants.USB1_MUSIC_SOURCE;
            }
            String sourceName = AudioFocusUtils.getInstance().getCurrentAudioSourceName(AppBase.mContext);
            Log.d(TAG, "requestFocus: sourceName = " + sourceName + " audioType = " + audioType);
            mAudioFocus = AudioFocusUtils.getInstance().requestFocus(AudioFocusUtils.getInstance().audioTypeToSourceID(audioType), mediaFocusChangerListener);
        } else {
            //取前先判断，避免重复申请
            if (mAudioFocus == AudioManager.AUDIOFOCUS_GAIN) {
                Log.d(TAG, "requestFocus: focus already GAIN!");
                return true;
            }
            mAudioFocus = AudioFocusUtils.getInstance().requestFocus(AudioFocusUtils.getInstance().audioTypeToSourceID(getAudioType()), mediaFocusChangerListener);
        }
        Log.d(TAG, "requestFocus: mAudioFocus = " + mAudioFocus);
        return mAudioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 释放焦点
     */
    private void abandonFocus() {
        Log.d(TAG, "abandonFocus: mCurrentMediaType = " + mCurrentMediaType);
        //AudioFocusManager.getInstance().abandonAudioFocus(mediaFocusChangerListener);
        //add by lzm 摈弃原来的音频焦点释放逻辑
        mAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
        DsvAudioFocusManager.getInstance().abandonAudioFocus(mediaFocusChangerListener);
    }

    private final MediaFocusChangerListener mediaFocusChangerListener = new MediaFocusChangerListener();

    /**
     * 焦点变化通知
     */
    private class MediaFocusChangerListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange: mCurrentMediaType = " + mCurrentMediaType);
            Log.d(TAG, "onAudioFocusChange:  focusChange = " + focusChange);
            mAudioFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    processCommand(MediaAction.PAUSE, ChangeReasonData.LONG_LOSS_SOURCE);
                    setPlaySpeed(MediaControlAction.DEFAULT_SPEED);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    //LOSS修改为PAUSE是由于有一些音源乱来，不是媒体类型，却申请了媒体的音频焦点，然后后面给回来了，就不会播放，这个应该是不能改为STOP的
                    //add by lzm 将恢复修改为open，然后这里就可以用stop了
                    processCommand(MediaAction.PAUSE, ChangeReasonData.SOURCE);
                    setPlaySpeed(MediaControlAction.DEFAULT_SPEED);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    //add by lzm 音源恢复只能用start
                    processCommand(MediaAction.START, ChangeReasonData.SOURCE);
                    break;
            }
        }
    }

    /**
     * 切换循模式
     */
    private void switchLoopType() {
        Log.d(TAG, "switchLoopType: ");
        switch (mCurrentPlayInfor.getLoopType()) {
            case USBConstants.LoopType.CYCLE:
                mCurrentPlayInfor.setCurrentLoopMode(USBConstants.LoopType.SINGLE);
                break;
            case USBConstants.LoopType.RANDOM:
                mCurrentPlayInfor.setCurrentLoopMode(USBConstants.LoopType.CYCLE);
                break;
            case USBConstants.LoopType.SINGLE:
                mCurrentPlayInfor.setCurrentLoopMode(USBConstants.LoopType.RANDOM);
                break;
        }
    }


    /**
     * 切换到指定的循环模式
     *
     * @param loopType 指定的循环模式
     */
    private void switchLoopType(String loopType) {
        Log.d(TAG, "switchLoopType: loopType = " + loopType);
        mCurrentPlayInfor.setCurrentLoopMode(loopType);
    }

    /**
     * 设置播放速率的接口
     *
     * @param speed speed
     */
    public void setPlaySpeed(float speed) {
        mMusicPlayControl.setPlaySpeed(speed);
    }

    /**
     * 根据当前媒体类型获取对应的焦点type
     *
     * @return String
     */
    private String getAudioType() {
        Log.d(TAG, "getAudioType: null ,mCurrentMediaType = " + mCurrentSourceType);
        return mCurrentSourceType;
    }
}
