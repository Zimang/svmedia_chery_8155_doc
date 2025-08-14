package com.desaysv.moduleusbvideo.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.adapter.USBVideoSpeedListAdapter;
import com.desaysv.moduleusbvideo.util.Constant;

/**
 * 视频倍速设置
 * Create by extodc87 on 2022-11-4
 * Author: extodc87
 */
public class VideoSpeedListPopupWindow {
    private static final String TAG = "VideoSpeedListPopupWindow";
    private PopupWindow pwd;
    private final Activity mContext;
    private View mView;

    private LinearLayout rooView;
    private RecyclerView rcv_list;

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

    public VideoSpeedListPopupWindow(Activity context) {
        this.mContext = context;
        initView();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        Log.d(TAG, "initView: ");
        mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_play_list, null);
        rooView = mView.findViewById(R.id.roo_view);
        rcv_list = mView.findViewById(R.id.rcv_list);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        rcv_list.setLayoutManager(layoutManager);
    }

    /**
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    public void showPopupWindow(USBVideoSpeedListAdapter usbVideoSpeedListAdapter) {
        if (null == pwd) {
            pwd = new PopupWindow(mView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
            pwd.setAnimationStyle(R.style.popupAnimation);
        }
        rcv_list.setAdapter(usbVideoSpeedListAdapter);
        rcv_list.requestFocus();
        rooView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: root event = " + event);
                dismiss();
                return true;
            }
        });
        backgroundAlpha(1.0f);
        pwd.showAtLocation(mView, Gravity.END, Constant.POSITION_X, Constant.POSITION_Y);
        pwd.setBackgroundDrawable(new ColorDrawable(0));
//        pwd.setFocusable(true);
//        pwd.setClippingEnabled(false);
        pwd.setOutsideTouchable(false);
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
     * 隐藏PopupWindow
     */
    public void dismiss() {
        if (pwd != null && pwd.isShowing()) {
            pwd.dismiss();
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha bgAlpha 0.0-1.0
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        //0.0-1.0
        lp.alpha = bgAlpha;
        mContext.getWindow().setAttributes(lp);
    }
}
