package com.desaysv.moduleusbvideo.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

/**
 * 设置全屏
 */
public class SystemUtils {
    private static final String TAG = SystemUtils.class.getSimpleName();

    /**
     * 4.2以上的整形标识
     */
    public static final int DISABLE_EXPAND = 0x00010000;
    /**
     * 4.2以下的整形标识
     */
    public static final int DISABLE_EXPAND_LOW = 0x00000001;

    /**
     * 取消StatusBar所有disable属性，即还原到最最原始状态
     */
    public static final int DISABLE_NONE = 0x00000000;

    /**
     * DISABLE_HOME | DISABLE_BACK | DISABLE_RECENT
     */
    public static final int DISABLE_NAVIGATION = 0x00400000 | 0x00200000 | 0x01000000 | 0x00010000;

    /**
     * 设置导航栏和状态栏是否可见
     */
    public static void setSystemUIVisible(Activity activity, boolean show) {
        int uiFlags;
        if (show) {
            uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else {
            uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.INVISIBLE;
        }
        uiFlags |= 0x00001000;
        activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    /**
     * 设置导航栏是否可用
     */
    public static void navigationBarEnable(Application application, boolean enable) {
        try {
            int DISABLE_NAVIGATION = 0x00200000 | 0x00400000 | 0x01000000; // |0x00010000
            //int DISABLE_NAVIGATION =  View.STATUS_BAR_DISABLE_HOME | View.STATUS_BAR_DISABLE_BACK | View.STATUS_BAR_DISABLE_RECENT;
            int DISABLE_NONE = 0x00000000;
            //获得ServiceManager类
            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", String.class);
            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, "statusbar");
            //获得IStatusBarService.Stub类
            Class<?> cStub = Class.forName("com.android.internal.statusbar.IStatusBarService$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            //调用asInterface方法获取IStatusBarService对象
            Object oIStatusBarService = asInterface.invoke(null, oRemoteService);
            //获得disable()方法
            Method disableMethod = oIStatusBarService.getClass().getMethod("disable", int.class, IBinder.class, String.class);
            //调用disable()方法
            if (enable) {
                disableMethod.invoke(oIStatusBarService, DISABLE_NONE, new Binder(), application.getPackageName());
            } else {
                disableMethod.invoke(oIStatusBarService, DISABLE_NAVIGATION, new Binder(), application.getPackageName());
            }
        } catch (Exception e) {
            Log.e(TAG, "SystemsUtil  NavigationBarEnable Exception -> " + e.toString());
        }
    }

    /**
     * 设置状态栏是否可下拉
     *
     * @param enable true 可以下拉
     */
    public static void setStatusBarEnable(Application application, boolean enable) {
        @SuppressLint("WrongConstant") Object service = application.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);

            if (enable) {
                expand.invoke(service, DISABLE_NONE);
            } else {
                int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentApiVersion <= 16) {
                    expand.invoke(service, DISABLE_EXPAND_LOW);
                } else {
                    expand.invoke(service, DISABLE_EXPAND);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 获取SD卡容量大小
     */
    public static long getSDTotalSize() {
        return getTotalSize(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * @return 获取Data路径磁盘容量大小
     */
    public static long getDataTotalSize() {
        return getTotalSize(Environment.getDataDirectory().getAbsolutePath());
    }

    public static long getSDAvailSize() {
        return getAvailSize(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * @return 获取Data路径磁盘容量大小
     */
    public static long getDataAvailSize() {
        return getAvailSize(Environment.getDataDirectory().getAbsolutePath());
    }

    /**
     * 获取指定路径的总存储空间
     *
     * @return 总内存大小
     */
    public static long getTotalSize(String materialPath) {
        long blockSize;
        long TotalBlocks;
        StatFs statFs = new StatFs(materialPath);

        blockSize = statFs.getBlockSizeLong();
        TotalBlocks = statFs.getBlockCountLong();

        return blockSize * TotalBlocks;
    }


    /**
     * 获取指定路径的可用存储空间
     *
     * @return 可用内存大小
     */
    public static long getAvailSize(String materialPath) {
        long blockSize;
        long AvailBlocks;
        StatFs statFs = new StatFs(materialPath);

        blockSize = statFs.getBlockSizeLong();
        AvailBlocks = statFs.getAvailableBlocksLong();

        return blockSize * AvailBlocks;
    }

}
