package com.desaysv.moduleusbvideo.view;

import static android.graphics.Bitmap.Config.ARGB_8888;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.desaysv.moduleusbvideo.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 行车安全弹框
 * Create by extodc87 on 2022-11-4
 * Author: extodc87
 */
public class VideoSpeedTipsPopupWindow implements View.OnClickListener {
    private static final String TAG = "VideoSpeedTipsPopupWindow";
    private PopupWindow pwd;
    private final Activity mContext;
    private View mView;

    private RelativeLayout rooView;

    private final OnClickListener onClickListener;

    private final Handler mHandler = new Handler();
    /**
     * 5s无操作自动关闭
     */
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
//            dismiss();
        }
    };

    public VideoSpeedTipsPopupWindow(Activity context, OnClickListener onClickListener) {
        this.mContext = context;
        this.onClickListener = onClickListener;
        initView();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        Log.d(TAG, "initView: ");
        mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_speed_tips, null);
        rooView = mView.findViewById(R.id.roo_view);
        Button btnToPlay = mView.findViewById(R.id.btnToPlay);
        Button btnClose = mView.findViewById(R.id.btnClose);
        btnToPlay.setOnClickListener(this);
        btnClose.setOnClickListener(this);
    }

    /**
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    public void showPopupWindow() {
        if (null == pwd) {
            pwd = new PopupWindow(mView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        }
        if (pwd.isShowing()) {
            Log.i(TAG, "showPopupWindow: showing return");
            return;
        }
        /*rooView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: root event = " + event);
                dismiss();
                return true;
            }
        });*/
        Log.e(TAG, "showPopupWindow: mView = " + mView);
        backgroundAlpha(1.0f);
        pwd.showAtLocation(mView, Gravity.CENTER, 0, 0);
//        pwd.setBackgroundDrawable(new ColorDrawable(0));
        rooView.setBackground(new BitmapDrawable(blurBitmap(shotScreenBitmap())));
//        pwd.setOutsideTouchable(true);
        pwd.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
                mHandler.removeCallbacks(mRunnable);
            }
        });
//        mHandler.postDelayed(mRunnable, TIMER_DELAY_OFF);
    }


    /**
     * 只进行截屏操作，返回Bitmap
     *
     * @return Bitmap
     */
    public static Bitmap shotScreenBitmap() {
        //反射实现
        DisplayMetrics mDisplayMetrics = new DisplayMetrics(); //获取屏幕宽高
        int[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        Bitmap screenBmp = null;   //屏幕截图
        try {
            Class<?> demo = Class.forName("android.view.SurfaceControl");
            Method method = demo.getMethod("screenshot", Rect.class, int.class, int.class, int.class);   //framework隐藏方法
            screenBmp = (Bitmap) method.invoke(null, new Object[]{new Rect(0, 0, dims[0], dims[1]), dims[0], dims[1], 0});
            screenBmp = screenBmp.copy(ARGB_8888, true);
            Log.d(TAG, "shotScreenBitmap: finnish");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 1 ：" + e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 2 ：" + e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 3 ：" + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 4 ：" + e);
        }
        return screenBmp;
    }

    /**
     * 高斯模糊图
     *
     * @param bitmap bitmap
     * @return bitmap
     */
    private Bitmap blurBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            RenderScript mRenderScript = RenderScript.create(mContext);
            Bitmap outBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
            int len = 16;
            for (int i = 0; i < len; i++) {
                ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
                final Allocation inAllocation = Allocation.createFromBitmap(mRenderScript, bitmap),
                        outAllocation = Allocation.createFromBitmap(mRenderScript, outBitmap);
                scriptIntrinsicBlur.setRadius(5.0f); // 0-25 数值越大 越模糊
                scriptIntrinsicBlur.setInput(inAllocation);
                scriptIntrinsicBlur.forEach(outAllocation);
                outAllocation.copyTo(outBitmap);
                bitmap.recycle();
                bitmap = Bitmap.createBitmap(outBitmap, 0, 0, outBitmap.getWidth(), outBitmap.getHeight());
            }
            mRenderScript.destroy();
            Log.d(TAG, "blurBitmap: outBitmap");
            return outBitmap;
        }
        Log.d(TAG, "blurBitmap: null");
        return null;
    }

    /**
     * 隐藏PopupWindow
     */
    public void dismiss() {
        if (pwd != null && pwd.isShowing()) {
            pwd.dismiss();
        }
    }

    public boolean isShowing() {
        if (pwd != null) {
            return pwd.isShowing();
        }
        return false;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha bgAlpha 0.0-1.0
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        //0.0 - 1.0
        lp.alpha = bgAlpha;
        mContext.getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnToPlay) {
            dismiss();
            if (null != onClickListener) {
                onClickListener.onToPlayClick();
            }
        } else if (id == R.id.btnClose) {
            dismiss();
            if (null != onClickListener) {
                onClickListener.onCloseClick();
            }
        }
    }


    /**
     * 列表item点击的时候，会触发的回调
     */
    public interface OnClickListener {
        void onToPlayClick();

        void onCloseClick();
    }

}
