package com.desaysv.moduleradio.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.moduleradio.R;


public class DeleteHistoryDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "DeleteHistoryDialog";

    private TextView tvDeleteConfirm;
    private TextView tvDeleteCancel;

    public DeleteHistoryDialog(@NonNull Context context) {
        super(context);
    }

    public DeleteHistoryDialog(@NonNull Context context, int themeResId, OnDeleteClickListener deleteClickListener) {
        super(context, themeResId);
        this.onDeleteClickListener = deleteClickListener;
        init();
    }

    protected DeleteHistoryDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.radio_delete_history_dialog);
        tvDeleteConfirm = findViewById(R.id.tvDeleteConfirm);
        tvDeleteCancel = findViewById(R.id.tvDeleteCancel);
        tvDeleteConfirm.setOnClickListener(this);
        tvDeleteCancel.setOnClickListener(this);
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
        if (id == R.id.tvDeleteConfirm){
            RadioList.getInstance().deleteSearchHistoryList();
            if (onDeleteClickListener != null){
                onDeleteClickListener.onDeleteClick();
            }
        }
        dismiss();
    }

    private OnDeleteClickListener onDeleteClickListener;
    public interface OnDeleteClickListener {
        void onDeleteClick();
    }
}
