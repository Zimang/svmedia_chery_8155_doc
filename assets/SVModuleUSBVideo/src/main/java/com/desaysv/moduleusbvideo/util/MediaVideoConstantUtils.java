package com.desaysv.moduleusbvideo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.google.gson.Gson;


/**
 * Created by uidp5370 on 2019-3-7.
 * 用来保存媒体的播放状态的工具类，使用SharedPreferences
 */

public class MediaVideoConstantUtils {
    private final String TAG = this.getClass().getSimpleName();
    private SharedPreferences spMedia;
    private SharedPreferences.Editor edMedia;

    private static final class InstanceHolder {
        static final MediaVideoConstantUtils instance = new MediaVideoConstantUtils();
    }

    public static MediaVideoConstantUtils getInstance() {
        return InstanceHolder.instance;
    }

    private Gson mGson;

    /**
     * 初始化，初始化SharedPreferences的edit
     * 需跨进程使用
     *
     * @param context AppBase.mContext
     */
    @SuppressLint({"CommitPrefEdits", "WorldWriteableFiles"})
    public void initialize(Context context) {
        spMedia = context.getSharedPreferences("SP_MEDIA_VIDEO", Context.MODE_PRIVATE);
        edMedia = spMedia.edit();
        mGson = new Gson();
    }

    /**
     * 保存 屏幕亮度
     *
     * @param screenBacklight 屏幕亮度
     */
    public void saveScreenBacklight(int screenBacklight) {
        Log.d(TAG, "saveScreenBacklight: screenBacklight = " + screenBacklight);
        if (edMedia != null) {
            edMedia.putInt("MEDIA_SCREENBACKLIGHT", screenBacklight);
            edMedia.apply();
        }
    }

    /**
     * 获取 屏幕亮度
     *
     * @return int 屏幕亮度
     */
    public int getScreenBacklight() {
        int screenBacklight = getSystemScreenBacklight();
        if (spMedia != null) {
            screenBacklight = spMedia.getInt("MEDIA_SCREENBACKLIGHT", screenBacklight);
        }
        Log.d(TAG, "getScreenBacklight: screenBacklight = " + screenBacklight);
        return screenBacklight;
    }

    /**
     * 获取系统当前亮度
     *
     * @return screenBacklight
     */
    private int getSystemScreenBacklight() {
        int screenBacklight = ModuleUSBVideoTrigger.getInstance().getScreenBacklight(); // 系统值
        if (-1 == screenBacklight) {
            screenBacklight = Constant.VIDEO_PLAY_ALPHA_DEFAULT;
        } else {
            screenBacklight = (screenBacklight + 1) * 10;
        }
        return screenBacklight;
    }

    /**
     * 存储对象到本地
     *
     * @param key    key
     * @param object object
     * @throws Exception 把相关的异常抛到上层
     */
    public void saveObject(String key, Object object) {
        edMedia.putString(key, mGson.toJson(object)).apply();
        /*if (object instanceof Serializable) {
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
        }*/
    }

    /**
     * 获取存储的对象
     *
     * @param key key
     * @return Object
     */
    @Nullable
    private <T> T getObject(String key, Class<T> classOfT) {
        String temp = spMedia.getString(key, null);
        if (null == temp) {
            return null;
        }
        Log.d(TAG, "getObject: key = " + key + "  temp = " + temp);
        return mGson.fromJson(temp, classOfT);
        /*Object readObject = null;
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
        return readObject;*/
    }

}
