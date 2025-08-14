package com.desaysv.moduleusbmusic.businesslogic.control;

import android.util.Log;

import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.usbbaselib.bean.CurrentPlayListType;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.statussubject.SearchType;

import java.util.List;
import java.util.concurrent.Future;

import static com.desaysv.libusbmedia.control.MediaControlTool.NEED_TO_FIND_THE_SAVE_MEDIA;

/**
 * Created by LZM on 2019-11-27
 * Comment 基础的逻辑恢复类
 */
public abstract class BaseMusicStartControl {

    private final String TAG = getClass().getSimpleName();

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
    protected abstract SearchType getMusicSearchStatus();

    /**
     * 获取音乐的控制器
     *
     * @return 音乐的控制器
     */
    protected abstract IControlTool getMusicControl();

    /**
     * 获取音乐的列表
     *
     * @return 音乐的列表
     */
    protected abstract List<FileMessage> getMusicList();

    /**
     * 启动相应的界面
     */
    protected abstract void startView();

    /**
     * 判断能否打开，需要根据数据状态，根据界面显示状态，根据当前的音源状态
     * 能够打开的前提条件，1. 当前是有数据的情况下
     * 2. 当前的音源是空的，不管前后台，都是能够打开；
     * 3. 当前在前台，不管音源是不是空的，都是能够打开的；
     *
     * @return true：能够打开；false：不能打开
     */
    protected abstract boolean isEnableOpen();

    private Future<?> mMusicStartTask;

    private static final int WAIT_COUNT = 60000 / 500;

    private boolean isStop = false;


    public void startMusic(boolean isForeground) {
        Log.d(TAG, "startMusic: ");
        if (isForeground){
            startView();
        }
        //不管前后台，都加入启动的逻辑
        openMusic();
    }

    /**
     * 打开源，在只是一个播放动作，不过把数据扫描状态给归纳在里面，所以写了一个工具类来实现
     */
    private void openMusic() {
        Log.d(TAG, "openMusic: ");
        if (mMusicStartTask != null && !mMusicStartTask.isDone()) {
            return;
        }
        isStop = false;
        Log.d(TAG, "openMusic: mMusicStartTask is can start");
        mMusicStartTask = MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                //如果设备处于连接状态，才有恢复播放的可能性，才有进行下去的意义
                if (getDeviceStatus()) {
                    Log.d(TAG, "openMusic: getUSBDeviceStatus is CONNECTED");
                    int count = 0;
                    Log.d(TAG, "openMusic: getUSBMusicSearchType = " + getMusicSearchStatus());
                    //由于现在列表做了骚操作，所以一旦列表不为空，就可以直接恢复播放了
                    Log.d(TAG, "run: isEmpty = " + getMusicList().isEmpty());
                    while (getMusicList().isEmpty() && count < WAIT_COUNT && !isStop) {
                        Log.d(TAG, "openMusic: waiting getMusicSearchStatus = " + getMusicSearchStatus());
                        if (getMusicSearchStatus() == SearchType.NO_DATA || !getDeviceStatus() ) {
                            //如果列表为空的原因是由于U盘里面没有数据，那就直接return，不需要恢复播放了
                            //如果设备断开了，那也不需要恢复播放了，直接return
                            Log.d(TAG, "openMusic: getMusicSearchStatus is NO_DATA");
                            return;
                        }
                        try {
                            count++;
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "openMusic: getMusicSearchStatus = " + getMusicSearchStatus());
                    Log.d(TAG, "openMusic: musicList size = " + getMusicList().size());
                    //扫描结束之后，需要获取当前的状态来进行状态的恢复
                    if (isEnableOpen()) {
                        getMusicControl().setPlayList(getMusicList(), CurrentPlayListType.ALL);
                        getMusicControl().processCommand(MediaAction.OPEN, ChangeReasonData.AUTO_PLAY, NEED_TO_FIND_THE_SAVE_MEDIA);
                        Log.d(TAG, "openMusic: true");
                    } else {
                        Log.d(TAG, "openMusic: is not allow open");
                    }
                } else {
                    Log.d(TAG, "openMusic: getUSBDeviceStatus is DISCONNECTED");
                }
            }
        });
    }


}
