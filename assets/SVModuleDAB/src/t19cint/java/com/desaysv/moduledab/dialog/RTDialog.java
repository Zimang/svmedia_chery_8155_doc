package com.desaysv.moduledab.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.desaysv.moduledab.R;
import com.desaysv.moduledab.utils.FastBlur;
import com.desaysv.moduledab.utils.ProductUtils;

public class RTDialog extends Dialog implements View.OnClickListener{

    private static final String TAG = "RTDialog";

    private RelativeLayout rlDABRTDialog;
    private ImageView ivDABRTDialogClose;
    private TextView tvDABRTDialogContent;

    public RTDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public RTDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        if (id == R.id.ivDABRTDialogClose){
            dismiss();
        }
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        setCanceledOnTouchOutside(true);
        setContentView(ProductUtils.isRightRudder() ? R.layout.dab_rt_dialog_right : R.layout.dab_rt_dialog);
        ivDABRTDialogClose = findViewById(R.id.ivDABRTDialogClose);
        tvDABRTDialogContent = findViewById(R.id.tvDABRTDialogContent);
        tvDABRTDialogContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        rlDABRTDialog = findViewById(R.id.rlDABRTDialog);
        ivDABRTDialogClose.setOnClickListener(this);
    }

    @Override
    public void show() {
        if (!isShowing()) {
            //setBlurBg();
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = WindowManager.LayoutParams.MATCH_PARENT;
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            super.show();
            // 在show之后设置宽高才有效
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = width;
            lp.height = height;
            getWindow().setAttributes(lp);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void dismiss() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        super.dismiss();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;
        float dialogWidth = resources.getDimensionPixelSize(R.dimen.dialog_bg_width);
        float dialogHeight = resources.getDimensionPixelSize(R.dimen.dialog_bg_height);
        Log.d(TAG, "onTouchEvent: screenWidth: " + screenWidth +" height: " +  screenHeight
                + " dialogWidth: " + dialogWidth +" dialogHeight: " + dialogHeight);
        //点击弹框范围外的区域，弹窗消失
        if ((((screenHeight/2 - dialogHeight/2) < event.getY()) && (event.getY() < (screenHeight/2 + dialogHeight/2))
                && ((screenWidth/2 - dialogWidth/2 < event.getX())) && (event.getX() < (screenWidth/2 + dialogWidth/2))) != true){
            Log.d(TAG, "onTouchEvent: dialogDismiss");
            this.dismiss();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 更新dab的RT信息
     * @param dabText
     */
    public void updateDetails(String dabText){
        if (dabText != null && dabText.length() > 0){
            Log.d(TAG,"updateDetails:"+dabText);
            tvDABRTDialogContent.setText(dabText);
        }
    }


    /**
     * 设置为模糊背景
     */
    private void setBlurBg(){
        //截图模糊处理用作背景
        if (rlDABRTDialog != null) {//t26没有这个
            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
                BitmapDrawable blurDrawable = FastBlur.shotScreenBlurAndDimDrawable(getContext(), 5, 0.3f);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (blurDrawable != null) {
                        rlDABRTDialog.setBackground(blurDrawable);
                    }
                });
            }).run();
        }
    }
}
