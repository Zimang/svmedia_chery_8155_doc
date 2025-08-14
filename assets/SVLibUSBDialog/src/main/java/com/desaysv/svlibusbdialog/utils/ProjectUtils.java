package com.desaysv.svlibusbdialog.utils;

import android.util.Log;
import android.os.SystemProperties;
/**
 * created by ZNB on 2022-07-18
 * 判断项目的工具类
 * 设计之初主要用来判断项目是否是双 USB 的，后续可扩展其它功能
 * 也可以通过多渠道差分这个工具类
 */
public class ProjectUtils {

    /**
     * 是否支持USB2，即双USB
     * @return
     */
    public static boolean hasUSB2(){
        return false;
    }


    /**
     * 是否支持两个弹窗同时显示
     * 是的话，后者覆盖前者，前者不做消失处理
     * 否的话，后者覆盖前者，前者需要消失
     * @return
     */
    public static boolean isSupportTwoDialog(){
        return false;
    }

    private static final String BOOT_KEY = "sys.usbpicture.isboot";
    private static final String DEFAULT_STRING = "USBPictureFirstBoot";
    private static final String SET_STRING = "USBPictureRun";
    public static void checkNeedShowDialog(){
        String isBoot = SystemProperties.get(BOOT_KEY,DEFAULT_STRING);
        Log.d("ZNB_checkNeedShowDialog","isBoot:"+isBoot);
        if (isBoot.equals(DEFAULT_STRING)){//这个值是默认值，说明是首次起来
            SystemProperties.set(BOOT_KEY,SET_STRING);
            setNeedShowDialog(false);
        }else {
            setNeedShowDialog(true);
        }
    }

    private static boolean needShowDialog = true;

    public static void setNeedShowDialog(boolean needShow){
        needShowDialog = needShow;
    }

    public static boolean isNeedShowDialog(){
        return needShowDialog;
    }

    private static boolean isFirstUse = true;

    public static boolean isIsFirstUse() {
        return isFirstUse;
    }

    public static void setIsFirstUse(boolean isFirstUse) {
        ProjectUtils.isFirstUse = isFirstUse;
    }

}
