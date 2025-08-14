package com.desaysv.moduleusbmusic.businesslogic.listdata;

import android.content.ContentResolver;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.svlibmediastore.query.MusicQuery;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.datebase.DbUtils;
import com.desaysv.usbbaselib.datebase.MediaDatabaseKey;

import java.io.File;
import java.util.List;


/**
 * Created by uidp5370 on 2019-6-3.
 * 搜索相应的数据库的对外方法类
 */

public abstract class BaseSearchMusicData {

    private final String TAG = this.getClass().getSimpleName();

    private final ContentResolver mContentResolver;

    BaseSearchMusicData() {
        mContentResolver = AppBase.mContext.getContentResolver();
    }


    /**
     * 获取扫描的路径
     *
     * @return FILE_PATH_USB_A； FILE_PATH_USB_B；
     */
    protected abstract String getScanPath();

    /**
     * 将列表数据保存起来，并且触发数据状态的变化回调
     *
     * @param scanStatus USB数据的扫描状态
     * @param musicList  需要存储的音乐列表
     */
    protected abstract void addUSBMusicList(int scanStatus, List<FileMessage> musicList);


    /**
     * 获取持久化保存的媒体播放类
     *
     * @return 媒体播放类
     */
    protected abstract FileMessage getSaveFileMessage();

    /**
     * 获取USB1音乐的列表
     */
    public void getUSBMusicData(int scanStatus) {
        List<FileMessage> musicList;
        if (USBConstants.SupportProvider.IS_SUPPORT_ANDROID) {
            musicList = MusicQuery.getInstance().queryMediaList(getScanPath());
        } else {
            musicList = getMusicData();
        }
        Log.i(TAG, "getUSBMusicData: musicList = " + musicList.size() + " scanStatus = " + scanStatus + " musicList = " + musicList);
        //获取底层的列表，如果在的话，优先放到列表里面的第一位
        //原生扫描很快，无需作此动作
        changeMusicList(musicList);
        //将USB的列表数据，完全添加进去，并触发回调
        addUSBMusicList(scanStatus, musicList);
    }

    /**
     * 根据条件查询音乐的数据
     *
     * @return 数据列表
     */
    private List<FileMessage> getMusicData() {
        String sortOrder = MediaDatabaseKey.SORT_LETTERS_NAME + " ASC";
        String selection;
        String[] args;
        //这里得过滤逻辑是过滤加载完ID3信息得消息
        selection = MediaDatabaseKey.ROOT_PATH + "=?" + " and " + MediaDatabaseKey.IS_HAS_ID3 + "=?";
        args = new String[]{getScanPath(), "1"};
        return DbUtils.getMusicDataBaseData(mContentResolver, USBConstants.ProviderUrl.MUSIC_DATA_URL, sortOrder, selection, args);
    }

    /**
     * 检测记忆的文件是否存在
     *
     * @return true 存在 false 不存在
     */
    private boolean checkSaveFileExists(String path) {
        Log.i(TAG, "checkSaveFileExists: path = " + path);
        if (TextUtils.isEmpty(path)) {
            Log.i(TAG, "checkSaveFileE: save path is null");
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

    /**
     * 检测保存的文件路径是否在数据库中
     *
     * @return true 在； false 不在
     */
    private boolean checkSaveFileInDataBase(String path, List<FileMessage> musicList) {
        Log.d(TAG, "checkSaveFileInDataBase: path = " + path);
        for (FileMessage fileMessage : musicList) {
            if (path.equals(fileMessage.getPath())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取需要音源恢复的文件是不是在文件夹中，如果在的话，并且数据库还没有扫到它，就需要将它放在列表的第一位
     */
    protected void changeMusicList(List<FileMessage> musicList) {
        //获取底层保存的文件
        FileMessage fileMessage = getSaveFileMessage();
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
        //检测保存的文件是不是在已经扫描到的问题
        boolean isFileInDataBase = checkSaveFileInDataBase(path, musicList);
        Log.d(TAG, "changeMusicList: isFileInDataBase = " + isFileInDataBase);
        if (isFileInDataBase) {
            //文件已经在数据库中，所以不需要放在列表的第一位，直接return
            return;
        }
        Log.d(TAG, "changeMusicList: fileMessage = " + fileMessage);
        //将音源恢复的列表放在第一位
        musicList.add(0, fileMessage);
    }
}
