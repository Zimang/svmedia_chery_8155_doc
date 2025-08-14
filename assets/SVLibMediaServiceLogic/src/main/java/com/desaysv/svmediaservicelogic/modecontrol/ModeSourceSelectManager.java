package com.desaysv.svmediaservicelogic.modecontrol;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.bean.DsvAudioSDKConstants;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.manager.SourceStatusManager;
import com.desaysv.mediacommonlib.base.AppBase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by LZM on 2020-3-6
 * Comment 获取当前需要切换的音源，不同项目会有不同的业务逻辑需求
 *
 * @author uidp5370
 */
public class ModeSourceSelectManager {

    private static final String TAG = "ModeSourceSelectManager";


    /**
     * 本地音乐的包名
     */
    private static final String LOCAL_MUSIC_PACKAGE_NAME = "com.desaysv.mediaapp";

    /**
     * 本地电台的包名
     */
    private static final String RADIO_PACKAGE_NAME = "com.desaysv.mediaapp";


    /**
     * 音源和界面之间的关系的hashMap
     */
    private HashMap<String, String> mSourcePackageHasMap = new HashMap<String, String>() {
        {
            put(DsvAudioSDKConstants.FM_SOURCE, RADIO_PACKAGE_NAME);
            put(DsvAudioSDKConstants.AM_SOURCE, RADIO_PACKAGE_NAME);
            put(DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE, LOCAL_MUSIC_PACKAGE_NAME);
            put(DsvAudioSDKConstants.USB0_MUSIC_SOURCE, LOCAL_MUSIC_PACKAGE_NAME);
            put(DsvAudioSDKConstants.USB1_MUSIC_SOURCE, LOCAL_MUSIC_PACKAGE_NAME);
        }
    };


    /**
     * mode切换的循环列表
     */
    private static final String[] MODE_SOURCES = {
            DsvAudioSDKConstants.FM_SOURCE,
            DsvAudioSDKConstants.AM_SOURCE,
            DsvAudioSDKConstants.LOCAL_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB0_MUSIC_SOURCE,
            DsvAudioSDKConstants.USB1_MUSIC_SOURCE,
    };


    private static final List<String> MODE_SOURCES_LIST = Arrays.asList(MODE_SOURCES);


    private static ModeSourceSelectManager instance;

    public static ModeSourceSelectManager getInstance() {
        if (instance == null) {
            synchronized (ModeSourceSelectManager.class) {
                if (instance == null) {
                    instance = new ModeSourceSelectManager();
                }
            }
        }
        return instance;
    }

    private ModeSourceSelectManager() {

    }

    /**
     * 按mode按键的时候，获取下一个有效的音源
     *
     * @param currentModeSource 当前mode的音源
     * @return String 下一个有效的音源
     */
    public String getNextEffectSource(String currentModeSource) {
        Log.d(TAG, "getNextEffectSource: currentModeSource = " + currentModeSource);
        //判断当前音源是不是在前台
        boolean isForeground = checkIsSourceInForeground(currentModeSource);
        Log.d(TAG, "getNextEffectSource: isForeground = " + isForeground);
        String NextEffectSource = currentModeSource;
        if (isForeground) {
            //如果在前台，那就切换下一个音源
            NextEffectSource = getEffectSource(getNextSource(currentModeSource));
        } else if (!checkSourceEffect(currentModeSource)) {
            Log.d(TAG, "getNextEffectSource: background source not effect");
            //如果音源在后台，但是这个音源无效，还是要切换下一个音源的，就好像拔掉U盘，回到主界面，按mode
            NextEffectSource = getEffectSource(getNextSource(currentModeSource));
        }
        Log.d(TAG, "getNextEffectSource: NextEffectSource = " + NextEffectSource);
        return NextEffectSource;
    }


    /**
     * 获取mode按键下一个有效的音源
     *
     * @param source 当前的音源
     * @return String mode列表中的下一个音源
     */
    private String getNextSource(String source) {
        int nextIndex;
        int index = MODE_SOURCES_LIST.indexOf(source);
        if (index < (MODE_SOURCES_LIST.size() - 1)) {
            nextIndex = index + 1;
        } else {
            nextIndex = 0;
        }
        String nextSource = MODE_SOURCES_LIST.get(nextIndex);
        Log.d(TAG, "getNextSource: source = " + source + " nextSource = " + nextSource);
        return nextSource;
    }

    /**
     * 检测音源是否有效
     *
     * @param source 检测的音源
     * @return 有效的音源
     */
    private String getEffectSource(String source) {
        String effectSource = source;
        boolean isSourceEffect = checkSourceEffect(source);
        Log.d(TAG, "checkSourceEffect: source = " + source + " isSourceEffect = " + isSourceEffect);
        if (!isSourceEffect) {
            //如果检测无效，则需要检测下一个音源是否有效，并递归回来
            effectSource = getEffectSource(getNextSource(source));
        }
        Log.d(TAG, "getEffectSource: effectSource = " + effectSource);
        return effectSource;
    }


    /**
     * 检测音源是否有效
     *
     * @param source 检测的音源
     * @return true：有效 false：无效
     */
    private boolean checkSourceEffect(String source) {
        boolean isSourceEffect;
        switch (source) {
            case DsvAudioSDKConstants.USB0_MUSIC_SOURCE:
                isSourceEffect = SourceStatusManager.getInstance().getDeviceSourceEffect(
                        DeviceConstants.DevicePath.USB0_PATH);
                break;
            case DsvAudioSDKConstants.USB1_MUSIC_SOURCE:
                isSourceEffect = SourceStatusManager.getInstance().getDeviceSourceEffect(
                        DeviceConstants.DevicePath.USB1_PATH);
                break;
            default:
                isSourceEffect = true;
                break;
        }
        return isSourceEffect;
    }

    /**
     * 检测当前的音源是否是在前台播放
     *
     * @param currentModeSource 当前的音源
     * @return true：前台  false：后台
     */
    private boolean checkIsSourceInForeground(String currentModeSource) {
        //如果当前是空的音源，则直接认为是在前台，切下一个音源
        Log.d(TAG, "checkIsSourceInForeground: currentModeSource = " + currentModeSource);
        //获取当前音源的对应的包名
        String sourcePackageName = mSourcePackageHasMap.get(currentModeSource);
        Log.d(TAG, "checkIsSourceInForeground: sourceView = " + sourcePackageName);
        //如果当前音源不在mode那几个音源里面，如视频，第三方音乐，则直接切换主管音源的界面,则直接认为是在前台，切换下一个
        if (TextUtils.isEmpty(sourcePackageName)) {
            Log.w(TAG, "checkIsSourceInForeground: sourceView is not in mode source list");
            return true;
        }
        //获取顶层的界面的包名
        String topPackageName = getTopActivityPackageName();
        Log.d(TAG, "checkIsSourceInForeground: topPackageName = " + topPackageName);
        //如果获取到顶层界面的包名是空的话，那也返回true，切换下一个音源
        if (TextUtils.isEmpty(topPackageName)) {
            Log.w(TAG, "checkIsSourceInForeground: topPackageName is null select next source");
            return true;
        }
        //判断当前顶层界面的包名和音源对应的包名是否一致的，如果一致，就认为当前音源界面在前台
        boolean isSourceInForeground = topPackageName.contains(sourcePackageName);
        Log.d(TAG, "checkIsSourceInForeground: currentModeSource = " + currentModeSource + " sourceView = " + sourcePackageName + " topPackageName = " + topPackageName);
        Log.d(TAG, "checkIsSourceInForeground: isSourceInForeground = " + isSourceInForeground);
        return isSourceInForeground;
    }


    /**
     * 获取系统顶层界面的包名
     *
     * @return topPackageName 顶层界面的包名
     */
    private String getTopActivityPackageName() {
        String topPackageName;
        ActivityManager activityManager = (ActivityManager) (AppBase.mContext.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        ComponentName componentName = runningTaskInfos.get(0).topActivity;
        topPackageName = componentName.getPackageName();
        Log.d(TAG, "getTopActivityPackageName: topPackageName = " + topPackageName);
        return topPackageName;
    }

}
