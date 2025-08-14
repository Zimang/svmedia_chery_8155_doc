package com.desaysv.moduleradio.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;


public class RDSRTDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "RDSRTDialog";


    private ImageView ivRDSRTCLose;
    private TextView tvRDSRTTitle;
    private TextView tvRDSRTInfo;


    public RDSRTDialog(@NonNull Context context) {
        super(context);
    }

    public RDSRTDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected RDSRTDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        setCanceledOnTouchOutside(true);
        setContentView(ProductUtils.isRightRudder() ? R.layout.radio_rdsrt_dialog_right : R.layout.radio_rdsrt_dialog);
        ivRDSRTCLose = findViewById(R.id.ivRDSRTCLose);
        tvRDSRTTitle = findViewById(R.id.tvRDSRTTitle);
        tvRDSRTInfo = findViewById(R.id.tvRDSRTInfo);
        ivRDSRTCLose.setOnClickListener(this);
        tvRDSRTInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void show() {
        if (!isShowing()) {
            int width = WindowManager.LayoutParams.WRAP_CONTENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            Window window = getWindow();
            super.show();
            // 在show之后设置宽高才有效
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = width;
            lp.height = height;
            getWindow().setAttributes(lp);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        dismiss();
    }

    /**
     * 根据当前的RDS RT信息，更新界面
     * @param rdsRadioText
     */
    public void updateRT(RDSRadioText rdsRadioText){
        Log.d(TAG,"updateRT:"+rdsRadioText);
        if (tvRDSRTInfo != null) {
            tvRDSRTInfo.setText(rdsRadioText.getRadioText());
        }
    }

}
