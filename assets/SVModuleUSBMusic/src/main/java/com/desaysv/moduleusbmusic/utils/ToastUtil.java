package com.desaysv.moduleusbmusic.utils;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Create by ZYW 2019-11-18
 * Comment 顶部提示信息工具类
 */
public class ToastUtil {
    private static final String TAG = ToastUtil.class.getSimpleName();
    private static ToastUtil mInstance;
    private Toast mToast;

    public static ToastUtil getInstance() {
        if (mInstance == null) {
            synchronized (ToastUtil.class) {
                if (mInstance == null) {
                    mInstance = new ToastUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 显示在界面顶部的提示信息Toast
     *
     * @param context 上下文
     * @param msg     显示的文本信息
     */
    public void showTopToast(Context context, String msg) {
        Log.d(TAG, "showTopToast: msg = " + msg);
        showSpace(context, msg);
        //delete by ZYW 防止频繁调用不显示的问题[BUG2020040100516]
        /*if (mToast == null) {
            mToast = new Toast(context);
        }*/
        //add by lzm 显示异常歌曲的toast必须使用单例,不然会出现歌曲播放完成之后,toast栈一直刷新显示
        /*if (mToast == null) {
            mToast = new Toast(context);
        }
        mToast.cancel();
        //设置Toast显示位置，居中，向 X、Y轴偏移量均为0（该项目framework层已经配置好，不需要再设置）
        mToast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        //获取自定义视图
        View view = LayoutInflater.from(context).inflate(R.layout.usb_music_top_toast, null);
        TextView tvMessage = view.findViewById(R.id.tv_message_toast);
        //设置文本
        tvMessage.setText(msg);
        //设置视图
        mToast.setView(view);
        //设置显示时长
        mToast.setDuration(Toast.LENGTH_SHORT);
        //显示
        mToast.show();*/
    }

    private static String lastStr = "";
    private static long lastClickTime = 0;

    /**
     * 显示文本方式
     * 修复A55 等R1平台无法连续显示问题
     * @param context context
     * @param text    text
     */
    public void showSpace(Context context, String text) {
        long currentTime = SystemClock.uptimeMillis();
        if (mToast == null) {
            mToast = new Toast(context);
        }
        if (lastStr.equals(text)) {
            if (currentTime - lastClickTime > 1800) {
                mToast.cancel();
                mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                mToast.show();
                lastClickTime = currentTime;
            }
        } else {
            lastStr = text;
            mToast.cancel();
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            mToast.show();
            lastClickTime = currentTime;
        }
    }
}