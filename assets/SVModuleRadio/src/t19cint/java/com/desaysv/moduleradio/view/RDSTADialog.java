package com.desaysv.moduleradio.view;

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
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.rds.RDSAnnouncement;
import com.desaysv.moduledab.utils.FastBlur;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;


public class RDSTADialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "RDSTADialog";
    private RelativeLayout rlRDSTADialog;
    private ImageView ivRDSTADialogClose;
    private TextView tvRDSTADialogContent;

    private TextView tvRDSTADialogTitle;

    public RDSTADialog(@NonNull Context context) {
        super(context);
        init();
    }

    public RDSTADialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected RDSTADialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
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
        setCanceledOnTouchOutside(true);
        setContentView(ProductUtils.isRightRudder() ? R.layout.radio_ta_dialog_right : R.layout.radio_ta_dialog);

        tvRDSTADialogTitle = findViewById(R.id.tvRDSTADialogTitle);
        rlRDSTADialog = findViewById(R.id.rlRDSTADialog);
        ivRDSTADialogClose = findViewById(R.id.ivRDSTADialogClose);
        tvRDSTADialogContent = findViewById(R.id.tvRDSTADialogContent);
        tvRDSTADialogContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        ivRDSTADialogClose.setOnClickListener(this);
    }


    public void updateTATitle(RDSAnnouncement rdsAnnouncement){
        if (tvRDSTADialogTitle != null && rdsAnnouncement != null){
            tvRDSTADialogTitle.setText(changeTypeToString(rdsAnnouncement.getAnnounceType()));
        }
    }

    public void updateTAContent(String text){
        if (tvRDSTADialogContent != null){
            tvRDSTADialogContent.setText(text);
        }
    }

    public void reShowDialog(){
        init();
        if (isShowing()) {
            show();
        }
    }

    /**
     * 设置为模糊背景
     */
    public void setBlurBg(){
        //截图模糊处理用作背景
        if (rlRDSTADialog != null) {//t26没有这个
            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
                BitmapDrawable blurDrawable = FastBlur.shotScreenBlurAndDimDrawable(getContext(), 5, 0.3f);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (blurDrawable != null) {
                        rlRDSTADialog.setBackground(blurDrawable);
                    }
                });
            }).run();
        }
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

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        dismiss();
    }

    /**
     * 通知类型 int 0:无效; 1:alarm; 2:traffic; 3:other;
     * @return
     */
    private String changeTypeToString(int announcementType){
        Log.d(TAG,"announcementType:"+announcementType);
        String s = "";
        switch (announcementType){
            case 1:
                s = getContext().getResources().getString(com.desaysv.moduledab.R.string.dab_ann_alarm);
                break;
            case 2:
                s = getContext().getResources().getString(com.desaysv.moduledab.R.string.dab_ann_ta);
                break;
            case 0:
            case 3:
                s = getContext().getResources().getString(com.desaysv.moduledab.R.string.dab_ann_other);
                break;

        }

        return s;
    }
}
