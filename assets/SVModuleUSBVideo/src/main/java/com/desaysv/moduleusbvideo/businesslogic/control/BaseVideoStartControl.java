package com.desaysv.moduleusbvideo.businesslogic.control;

import static com.desaysv.libusbmedia.control.MediaControlTool.NEED_TO_FIND_THE_SAVE_MEDIA;

import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.statussubject.SearchType;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by LZM on 2019-11-27
 * Comment 后台启动和界面启动的实现类，每个界面自己去实现
 */
public abstract class BaseVideoStartControl {

    private final String TAG = this.getClass().getSimpleName();

    /**
     * 获取设备的状态信息
     *
     * @return 设备的状态
     */
    protected abstract boolean getDeviceStatus();

    /**
     * 获取数据的扫描状态
     *
     * @return 数据的扫描状态
     */
    protected abstract SearchType getVideoSearchStatus();

    /**
     * 获取音乐的控制器
     *
     * @return 音乐的控制器
     */
    protected abstract IControlTool getVideoControl();

    /**
     * 获取音乐的列表
     *
     * @return 音乐的列表
     */
    protected abstract List<FileMessage> getVideoList();

    /**
     * 启动相应的界面
     */
    protected abstract void startView();

    /**
     * 判断能否打开，需要根据数据状态，根据界面显示状态，根据当前的音源状态
     * 能够打开的前提条件，1. 当前是有数据的情况下
     * 2. 只要界面在前台，不管音源是不是在自己手里，到可以打开，如果界面不在前台，打死都不能打开
     *
     * @return true：能够打开；false：不能打开
     */
    protected abstract boolean isEnableOpen();

    private Future<?> mMusicStartTask;

    private static final int WAIT_COUNT = 60000 / 500;

    private boolean isStop = false;

    /**
     * 启动视频播放，是否是在前台，如果是在界面里面调用，就是用false，如果不是的话，那就需要启动界面和启动音源了
     *
     * @param isForeground true： 前台； false：后台
     */
    public void startVideo(boolean isForeground) {
        if (isForeground) {
            startView();
        }
        openVideo();
    }

    /**
     * 音乐的逻辑复制过来的，后面可能用不到，不过不删掉，留在这里，看后面会不会用到
     */
    public void openVideo() {
        Log.d(TAG, "openVideo");
        if (mMusicStartTask != null && !mMusicStartTask.isDone()) {
            return;
        }
        isStop = false;
        Log.d(TAG, "openVideo: mMusicStartTask is can start");
        mMusicStartTask = MediaThreadPoolExecutorUtils.getInstance().submit(startOpenVideo);
    }

    /**
     *
     */
    private final Runnable startOpenVideo = new Runnable() {
        @Override
        public void run() {
            if (!getDeviceStatus()) {
                Log.d(TAG, "openVideo: no device");
                return;
            }
            Log.d(TAG, "openVideo: getUSBDeviceStatus is CONNECTED");
            int count = 0;
            Log.d(TAG, "openVideo: getVideoSearchType = " + getVideoSearchStatus());
            while ((getVideoSearchStatus() == SearchType.SEARCHING ||
                    getVideoSearchStatus() == SearchType.SEARCHING_HAVE_DATA) &&
                    count < WAIT_COUNT && !isStop) {
                Log.d(TAG, "openVideo: waiting getVideoSearchType = " + getVideoSearchStatus());
                try {
                    count++;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "openVideo:  getVideoList size = " + getVideoList().size());
            if (!isEnableOpen()) {
                Log.d(TAG, "openVideo: is can not open");
                return;
            }
            getVideoControl().setPlayList(getVideoList(), CurrentPlayListType.ALL);
            getVideoControl().processCommand(MediaAction.OPEN, ChangeReasonData.BOOT_RESUME, NEED_TO_FIND_THE_SAVE_MEDIA);
            Log.d(TAG, "openVideo: true");
        }
    };

}
