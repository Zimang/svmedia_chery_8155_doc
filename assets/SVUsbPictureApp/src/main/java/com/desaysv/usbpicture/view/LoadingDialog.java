package com.desaysv.usbpicture.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.desaysv.usbpicture.R;

public class LoadingDialog extends AlertDialog {

    private ObjectAnimator loadingAnimator;

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_loading);
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(this.findViewById(R.id.iv_loading), "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (loadingAnimator != null){
            loadingAnimator.end();
        }
    }

    @Override
    public void show() {
        int width = WindowManager.LayoutParams.WRAP_CONTENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        super.show();
        // 在show之后设置宽高才有效
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = width;
        lp.height = height;
        getWindow().setAttributes(lp);
        if (loadingAnimator != null){
            loadingAnimator.start();
        }
    }
}
