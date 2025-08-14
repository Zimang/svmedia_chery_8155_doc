package com.desaysv.svlibfileoperation.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.R;

public class DeleteDialog extends Dialog{
    private static final String TAG = "DeleteDialog";

    //定义动画
    private ObjectAnimator loadingMusicAnimator;
    private static final int ANIMATION_DURATION = 1000;

    private ImageView iv_deleting;


    public DeleteDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public DeleteDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected DeleteDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.layout_delete);
        iv_deleting = findViewById(R.id.iv_deleting);
    }

    @Override
    public void show() {
        if (!isShowing()) {
            int width = WindowManager.LayoutParams.WRAP_CONTENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            super.show();
            // 在show之后设置宽高才有效
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = width;
            lp.height = height;
            getWindow().setAttributes(lp);
        }
    }


    /**
     * 启动动画
     */
    public void startAnimation(){
        if (loadingMusicAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingMusicAnimator = ObjectAnimator.ofFloat(iv_deleting, "rotation", 0f, 360f);
            loadingMusicAnimator.setDuration(ANIMATION_DURATION);
            loadingMusicAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingMusicAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingMusicAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if(!loadingMusicAnimator.isStarted()) {
            loadingMusicAnimator.start();
        }
    }

    /**
     * 停止动画
     */
    public void stopAnimation(){
        if (loadingMusicAnimator != null && loadingMusicAnimator.isStarted()){
            loadingMusicAnimator.end();
        }
    }

}
