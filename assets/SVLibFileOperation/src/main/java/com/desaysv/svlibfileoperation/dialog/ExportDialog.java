package com.desaysv.svlibfileoperation.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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

public class ExportDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "ExportDialog";

    private ImageView iv_exporting;
    private TextView tv_exporting;
    private TextView tv_cancel;


    //定义动画
    private ObjectAnimator loadingMusicAnimator;
    private static final int ANIMATION_DURATION = 1000;



    public ExportDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public ExportDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected ExportDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.layout_export);
        iv_exporting = findViewById(R.id.iv_exporting);
        tv_exporting = findViewById(R.id.tv_exporting);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(this);
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
     * 更新显示
     * @param current
     * @param total
     */
    public void updateExporting(int current, int total){
        Log.d(TAG,"updateExporting,current:"+current+",total:"+total);
        tv_exporting.setText(String.format(getContext().getResources().getString(R.string.exporting),current,total));
    }


    /**
     * 启动动画
     */
    public void startAnimation(){
        if (loadingMusicAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingMusicAnimator = ObjectAnimator.ofFloat(iv_exporting, "rotation", 0f, 360f);
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


    @Override
    public void onClick(View v) {
        FileOperationManager.getInstance().cancelExport();
        stopAnimation();
        dismiss();
    }

}
