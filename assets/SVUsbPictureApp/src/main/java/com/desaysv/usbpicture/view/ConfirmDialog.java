package com.desaysv.usbpicture.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.usbpicture.R;

import java.util.List;

public class ConfirmDialog extends AlertDialog {
    private static final String TAG = "ConfirmDialog";
    private List<FileMessage> fileMessageList;

    public ConfirmDialog(@NonNull Context context) {
        super(context);
    }

    public ConfirmDialog(@NonNull Context context, int themeResId, List<FileMessage> fileMessageList) {
        super(context, themeResId);
        this.fileMessageList = fileMessageList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.layout_dialog_confirm);
        initView();
    }


    private void initView(){
        TextView tv_delete_confirm = findViewById(R.id.tv_delete_confirm);
        TextView tv_delete_cancel = findViewById(R.id.tv_delete_cancel);
        tv_delete_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"confirm delete");
                FileOperationManager.getInstance().startDeleteFiles(getContext(),fileMessageList);
                dismiss();
            }
        });

        tv_delete_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"cancel delete");
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void show() {
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
    }
}
