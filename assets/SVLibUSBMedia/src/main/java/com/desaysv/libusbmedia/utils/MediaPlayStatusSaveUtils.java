package com.desaysv.libusbmedia.utils;

import org.jetbrains.annotations.Nullable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Created by uidp5370 on 2019-3-7.
 * 用来保存媒体的播放状态的工具类，使用SharedPreferences
 */

public class MediaPlayStatusSaveUtils {
    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private SharedPreferences spMedia;
    private SharedPreferences.Editor edMedia;
    private static MediaPlayStatusSaveUtils instance;

    public static MediaPlayStatusSaveUtils getInstance() {
        if (instance == null) {
            synchronized (MediaPlayStatusSaveUtils.class) {
                if (instance == null) {
                    instance = new MediaPlayStatusSaveUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化，初始化SharedPreferences的edit
     * 需跨进程使用
     *
     * @param context AppBase.mContext
     */
    @SuppressLint({"CommitPrefEdits", "WorldWriteableFiles"})
    public void initialize(Context context) {
        mContext = context;
        spMedia = mContext.getSharedPreferences("spMedia", Context.MODE_PRIVATE);
        edMedia = spMedia.edit();
    }

    /**
     * 存放 上次播放的路径
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @param path      保存的路径
     */
    public void saveLastMediaPlayPath(MediaType mediaType, String path) {
        Log.d(TAG, "saveLastMediaPlayPath: path = " + path + " mediaType = " + mediaType);
        if (edMedia != null) {
            edMedia.putString(mediaType + "_LAST_PATH", path);
            edMedia.apply();
        }
    }


    /**
     * 获取 上次播放的路径
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @return String 获取到的路径
     */
    public String getLastMediaPlayPath(MediaType mediaType) {
        String path = "";
        if (spMedia != null) {
            path = spMedia.getString(mediaType + "_LAST_PATH", "");
        }
        Log.d(TAG, "getLastMediaPlayPath: path = " + path);
        return path;
    }


    /**
     * 存放需要恢复的媒体路径
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @param path      保存的路径
     */
    public void saveMediaPlayPath(MediaType mediaType, String path) {
        Log.d(TAG, "SaveUsbMusicPlayPath: path = " + path + " mediaType = " + mediaType);
        if (edMedia != null) {
            edMedia.putString(mediaType + "_PATH", path);
            edMedia.apply();
        }
    }


    /**
     * 获取需要恢复的媒体路径
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @return String 获取到的路径
     */
    public String getMediaPlayPath(MediaType mediaType) {
        String path = "";
        if (spMedia != null) {
            path = spMedia.getString(mediaType + "_PATH", "");
        }
        Log.d(TAG, "getUsbMusicPlayPath: path = " + path);
        return path;
    }

    /**
     * 保存当前播放媒体的时间
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @param time      需要保存的时间
     * @param path      这个保存的时间的路径
     */
    public void saveMediaPlayTime(MediaType mediaType, int time, String path) {
        Log.d(TAG, "saveMediaPlayTime: mediaType = " + mediaType + " time = " + time);
        if (edMedia != null) {
            edMedia.putInt(mediaType + "_TIME", time);
            edMedia.putString(mediaType + "_TIME_PATH", path);
            edMedia.apply();
        }
    }


    /**
     * 获取保存的时间
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @return int 保存的时间
     */
    public int getMediaPlayTime(MediaType mediaType) {
        int time = -1;
        if (spMedia != null) {
            time = spMedia.getInt(mediaType + "_TIME", -1);
        }
        Log.d(TAG, "getMediaPlayTime: mediaType = " + mediaType + " time = " + time);
        return time;
    }


    /**
     * 获取保存时间对于的路径
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @return string 保存的时间的媒体路径
     */
    public String getMediaTimePath(MediaType mediaType) {
        String timePath = "";
        if (spMedia != null) {
            timePath = spMedia.getString(mediaType + "_TIME_PATH", "");
        }
        Log.d(TAG, "getMediaPlayTime: mediaType = " + mediaType + " timePath = " + timePath);
        return timePath;
    }

    /**
     * 保存循环模式
     *
     * @param loopType String 循环模式
     */
    public void saveLoopType(String loopType) {
        Log.d(TAG, "saveLoopType: loopType = " + loopType);
        if (edMedia != null) {
            edMedia.putString("MUSIC_LOOPTYPE", "" + loopType);
            edMedia.apply();
        }
    }

    /**
     * 获取循环模式
     *
     * @return String循环模式
     */
    public String getLoopType() {
        String loopType = "CYCLE";
        if (spMedia != null) {
            loopType = spMedia.getString("MUSIC_LOOPTYPE", "CYCLE");
        }
        Log.d(TAG, "getLoopType: loopType = " + loopType);
        return loopType;
    }


    /**
     * 将当前播放的FileMessage信息进行持久化保存
     *
     * @param mediaType   USB1_MUSIC,     //USB1音乐
     *                    USB2_MUSIC,     //USB2音乐
     *                    USB1_VIDEO,     //USB1视频
     *                    USB2_VIDEO,     //USB2视频
     * @param fileMessage 媒体文件对象
     */
    public void saveMediaFileMessage(MediaType mediaType, FileMessage fileMessage) {
        Log.d(TAG, "saveMediaFileMessage: mediaType = " + mediaType + " fileMessage = " + fileMessage);
        //null或者null路径不进行更新，保留上一次的信息
        if (fileMessage == null || fileMessage.getPath().isEmpty()) {
            return;
        }
        try {
            saveObject(mediaType + "_PLAY_FILE_MESSAGE", fileMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取持久化保存的媒体文件
     *
     * @param mediaType USB1_MUSIC,     //USB1音乐
     *                  USB2_MUSIC,     //USB2音乐
     *                  USB1_VIDEO,     //USB1视频
     *                  USB2_VIDEO,     //USB2视频
     * @return 媒体文件信息
     */
    @Nullable
    public FileMessage getMediaFileMessage(MediaType mediaType) {
        FileMessage fileMessage = (FileMessage) getObject(mediaType + "_PLAY_FILE_MESSAGE");
        Log.d(TAG, "getMediaFileMessage: mediaType = " + mediaType + " fileMessage = " + fileMessage);
        return fileMessage;
    }

    /**
     * 存储对象到本地
     *
     * @param key    key
     * @param object object
     * @throws Exception 把相关的异常抛到上层
     */
    public void saveObject(String key, Object object) throws Exception {
        if (object instanceof Serializable) {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(object);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                if (edMedia != null) {
                    edMedia.putString(key, temp).apply();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (baos != null) {
                        baos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new Exception("Object must implements Serializable");
        }
    }

    /**
     * 获取存储的对象
     *
     * @param key key
     * @return Object
     */
    @Nullable
    private Object getObject(String key) {
        Object readObject = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        String temp = "";
        try {
            if (spMedia != null) {
                temp = spMedia.getString(key, "");
            }
            if (TextUtils.isEmpty(temp)) {
                Log.d(TAG, "getObject: temp is empty，so return null.");
                return null;
            }
            bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
            ois = new ObjectInputStream(bais);
            readObject = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return readObject;
    }

    /**
     * 媒体暂停播放状态持久化
     *
     * @param audioType audioType
     * @param isPlay    isPlay
     */
    public void saveMediaPlayPauseStatus(String audioType, boolean isPlay) {
        Log.d(TAG, "saveMediaPlayPauseStatus: audioType = " + audioType + " isPlay = " + isPlay);
        if (edMedia != null) {
            edMedia.putBoolean(audioType + "_PLAY_STATUS", isPlay);
            edMedia.apply();
        }
    }

    /**
     * 获取媒体暂停播放持久化的状态
     *
     * @param audioType audioType
     * @return PlayStatus
     */
    public boolean getMediaPlayPauseStatus(String audioType) {
        Log.d(TAG, "getMediaPlayPauseStatus: audioType = " + audioType);
        if (spMedia != null) {
            return spMedia.getBoolean(audioType + "_PLAY_STATUS", true);
        }
        return true;
    }
}
