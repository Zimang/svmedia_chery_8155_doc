package com.desaysv.svlibtoast;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ToastUtil {

    private static final String TAG = "SV_Lib_ToastUtil";

    private static volatile String oldMsg;

    private static volatile Toast toast = null;

    private static long startTime = 0;

    private static long endTime = 0;

    private static String theme = "";

    private static int gravity = Gravity.TOP;
    private static int yOffset = 10;
    private static int dayNight = -1;

    /**
     * 以预设样式显示toast
     *
     * @param context
     * @param message
     */
    public static void showToast(Context context, String message) {
        String themeTmp = Settings.System.getString(context.getContentResolver(), "com.desaysv.setting.theme.mode");
        if (themeTmp == null) {
            themeTmp = "";
        }
        int dayNightTmp = context.getResources().getConfiguration().uiMode;
        if (CarConfigUtil.getDefault().isAACertificated() && isAAShowing(context)) {
            return;
        }
        Log.d(TAG, "showToast: themeTmp = " + themeTmp + " theme = " + theme + " ,dayNightTmp:" + dayNightTmp + " ,dayNight:" + dayNight);
        if (!themeTmp.equals(theme) || dayNightTmp != dayNight) {
            theme = themeTmp;
            dayNight = dayNightTmp;
            if (toast != null)
                toast.cancel();
            toast = null;
        }
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.layout_sv_lib_toast, null);
        TextView textView = toastRoot.findViewById(R.id.tv_sv_lib_msg);
        if (toast == null) {
            Log.d(TAG, "toast == null >>> message --- " + message);
            toast = new Toast(context.getApplicationContext());//防止泄露
            textView.setText(message);
            //Toast的Y坐标是屏幕高度的1/3，不会出现不适配的问题
            toast.setGravity(gravity, 0, yOffset);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastRoot);
            toast.show();
            startTime = System.currentTimeMillis();
            endTime = startTime;
            oldMsg = message;
        } else {
            Log.d(TAG, "toast >>> message --- " + message);
            endTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                Log.d(TAG, "toast >>> same message ---");
                if (endTime - startTime <= 2000L) {
                    Log.d(TAG, "toast >>> same message --- to long");
                    return;
                }
                toast.show();
            } else {
                Log.d("ToastUtil111", "new message >>> " + message);
                toast.cancel();
                toast = new Toast(context);
                textView.setText(message);
                //获取屏幕高度
                toast.setGravity(gravity, 0, yOffset);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toastRoot);
                toast.show();
                oldMsg = message;
            }
        }
        startTime = endTime;
    }

    /**
     * 以预设样式显示toast
     *
     * @param context
     * @param message
     * @param themeTmp 主题,防止主题不同步
     */
    @Deprecated
    public static void showToast(Context context, String message, int themeTmp) {

        if (CarConfigUtil.getDefault().isAACertificated() && isAAShowing(context)) {
            return;
        }
        Log.d(TAG, "showToast: themeTmp = " + themeTmp + " theme = " + theme);
//        if (themeTmp != theme) {
//            theme = themeTmp;
        if (toast != null)
            toast.cancel();
        toast = null;
//        }
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.layout_sv_lib_toast, null);
        TextView textView = toastRoot.findViewById(R.id.tv_sv_lib_msg);
        if (toast == null) {
            Log.d(TAG, "toast == null >>> message --- " + message);
            toast = new Toast(context.getApplicationContext());//防止泄露
            textView.setText(message);
            //Toast的Y坐标是屏幕高度的1/3，不会出现不适配的问题
            toast.setGravity(gravity, 0, yOffset);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastRoot);
            toast.show();
            startTime = System.currentTimeMillis();
            oldMsg = message;
        } else {
            Log.d(TAG, "toast >>> message --- " + message);
            endTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                Log.d(TAG, "toast >>> same message ---");
                if (endTime - startTime <= 2000L) {
                    Log.d(TAG, "toast >>> same message --- to long");
                    return;
                }
                toast.show();
            } else {
                Log.d("ToastUtil111", "new message >>> " + message);
                toast.cancel();
                toast = new Toast(context);
                textView.setText(message);
                //获取屏幕高度
                toast.setGravity(gravity, 0, yOffset);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toastRoot);
                toast.show();
                oldMsg = message;
            }
        }
        startTime = endTime;
    }

    /**
     * 以预设样式显示toast
     *
     * @param context
     * @param stringId
     */
    public static void showToast(Context context, int stringId, int themeTmp) {
        if (context != null) {
            showToast(context, context.getString(stringId), themeTmp);
        }
    }

    /**
     * 以预设样式显示toast
     *
     * @param context
     * @param message
     */
    public static void showLongToast(Context context, String message) {
        String themeTmp = Settings.System.getString(context.getContentResolver(), "com.desaysv.setting.theme.mode");
        if (themeTmp == null) {
            themeTmp = "";
        }
        int dayNightTmp = context.getResources().getConfiguration().uiMode;
        Log.d(TAG, "showToast: themeTmp = " + themeTmp + " theme = " + theme + " ,dayNightTmp:" + dayNightTmp + " ,dayNight:" + dayNight);
        if (!themeTmp.equals(theme) || dayNightTmp != dayNight) {
            dayNight = dayNightTmp;
            theme = themeTmp;
            if (toast != null)
                toast.cancel();
            toast = null;
        }
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.layout_sv_lib_toast, null);
        TextView textView = toastRoot.findViewById(R.id.tv_sv_lib_msg);
        if (toast == null) {
            Log.d(TAG, "toast == null >>> message --- " + message);
            toast = new Toast(context.getApplicationContext());//防止泄露
            textView.setText(message);
            //Toast的Y坐标是屏幕高度的1/3，不会出现不适配的问题
            toast.setGravity(gravity, 0, yOffset);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(toastRoot);
            toast.show();
            startTime = System.currentTimeMillis();
            oldMsg = message;
        } else {
            Log.d(TAG, "toast >>> message --- " + message);
            endTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                Log.d(TAG, "toast >>> same message ---");
                if (endTime - startTime > Toast.LENGTH_LONG) {
                    Log.d(TAG, "toast >>> same message --- to long");
                    toast.show();
                }
            } else {
                textView.setText(message);
                //获取屏幕高度
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(toastRoot);
                toast.show();
                oldMsg = message;
            }
        }
        startTime = endTime;
    }

    /**
     * 以预设样式显示toast
     *
     * @param context
     * @param stringId
     */
    public static void showToast(Context context, int stringId) {
        if (context != null) {
            showToast(context, context.getString(stringId));
        }
    }

    /**
     * 以原生样式显示toast
     *
     * @param context
     * @param message
     */
    public static void showNormalToast(Context context, String message) {
        Log.i(TAG, "showNormalToast >>> message --- " + message);
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.show();
            startTime = System.currentTimeMillis();
        } else {
            endTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                if (endTime - startTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message;
                toast.setText(message);
                toast.show();
            }
        }
        startTime = endTime;
    }

    /**
     * show一个可以点击的toast
     *
     * @param context
     * @param message
     * @param clickListener
     */
    public static void showClickToast(Context context, String message, View.OnClickListener clickListener) {
        Log.i(TAG, "showClickToast >>> ");
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.layout_sv_lib_toast, null);
        toastRoot.setOnClickListener(clickListener);
        TextView textView = toastRoot.findViewById(R.id.tv_sv_lib_msg);
        if (toast == null) {
            Log.d(TAG, "toast == null >>> message --- " + message);
            toast = new Toast(context.getApplicationContext());//防止泄露
            toast.setGravity(gravity, 0, yOffset);
            try {
                Object mTN;
                mTN = getField(toast, "mTN");
                if (mTN != null) {
                    Object mParams = getField(mTN, "mParams");
                    if (mParams instanceof WindowManager.LayoutParams) {
                        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    }
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            textView.setText(message);
            toast.setGravity(gravity, 0, yOffset);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastRoot);
            toast.show();
            startTime = System.currentTimeMillis();
            oldMsg = message;
        } else {
            endTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                Log.d(TAG, "message.equals(oldMsg) >>> " + message);
                if (endTime - startTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                Log.d(TAG, "new message >>> " + message);
                textView.setText(message);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toastRoot);
                toast.show();
                oldMsg = message;
            }
        }
        startTime = endTime;
    }

    /**
     * show一个可以点击的toast <p/>
     * tip:该TYPE需要系统权限
     *
     * @param context
     * @param message
     * @param clickListener
     */
    public static void showClickPopUpToast(Context context, String message, View.OnClickListener clickListener) {
        Log.i(TAG, "showClickPopUpToast >>> message --- " + message);
        final WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final View toastRoot = LayoutInflater.from(context).inflate(R.layout.layout_sv_lib_toast, null);
        TextView textView = toastRoot.findViewById(R.id.tv_sv_lib_msg);
        textView.setText(message);
        toastRoot.setOnClickListener(clickListener);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                2027,//WindowManager.LayoutParams.TYPE_MAGNIFICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        & ~WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        mWindowManager.addView(toastRoot, params);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mWindowManager.removeView(toastRoot);
            }
        }, 5000);
    }

    /**
     * 反射字段
     *
     * @param object    要反射的对象
     * @param fieldName 要反射的字段名称
     */
    private static Object getField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }

    private static boolean isAAShowing(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(1);
        if (list.size() <= 0) {
            return false;
        }
        Log.i(TAG, "isAAShowing: " + list.get(0).topActivity.getClassName());
        if (list.get(0).topActivity.getClassName().equals("com.desaysv.auto.projection.MainActivity")) {
            return true;
        } else {
            return false;
        }
    }
}
