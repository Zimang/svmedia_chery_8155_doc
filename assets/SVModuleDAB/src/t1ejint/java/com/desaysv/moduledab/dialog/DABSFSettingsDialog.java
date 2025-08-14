package com.desaysv.moduledab.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.sy.swbt.SettingSwitchView;


public class DABSFSettingsDialog extends Dialog implements View.OnClickListener, SettingSwitchView.OnCheckedChangeListener {
    private static final String TAG = "DABSFSettingsDialog";


    private ImageView ivDABClose;
    private SettingSwitchView scAnnSf;
    private SettingSwitchView scAnnSFSl;


    private Context mContext;

    public DABSFSettingsDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init(mContext);
    }

    public DABSFSettingsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init(mContext);
    }

    protected DABSFSettingsDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        init(mContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(mContext);
    }

    private void init(Context context) {
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.dab_sf_settings);

        ivDABClose = findViewById(R.id.iv_dab_close);
        scAnnSf = findViewById(R.id.scAnnSf);
        scAnnSFSl = findViewById(R.id.scAnnSFSl);

        ivDABClose.setOnClickListener(this);
        scAnnSf.setOnCheckedChangeListener(this);
        scAnnSFSl.setOnCheckedChangeListener(this);
        mContext = context;

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
            updateDABSettings();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        dismiss();
    }

    /**
     * 更新DAB设置项的显示
     */
    //DAB公告设置的数据结构，设置的内容都集中封装到里面传递给底层，底层也是返回同样的数据结构
    private DABAnnSwitch dabAnnSwitch;
    private static final int DAB_SETTING_ON = 1;
    private static final int DAB_SETTING_OFF = 0;
    public void updateDABSettings(){
        dabAnnSwitch = DABTrigger.getInstance().mRadioStatusTool.getDABAnnSwitchStatus();
        if (dabAnnSwitch != null) {//默认使用系统返回的值
            scAnnSf.setChecked(dabAnnSwitch.getServiceFollow() == DAB_SETTING_ON);
            scAnnSFSl.setChecked(dabAnnSwitch.getServiceFollow() == DAB_SETTING_ON);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        Log.d(TAG,"onCheckedChanged,isChecked:"+isChecked);
        if (dabAnnSwitch != null) {
            if (id == R.id.scAnnSf){//HardLink，需要HAL确认协议
                dabAnnSwitch.setServiceFollow(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnSFSl) {//SoftLink，需要HAL确认协议
                dabAnnSwitch.setServiceFollow(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            }
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.SET_DAB_ANN_SWITCH, ChangeReasonData.CLICK,dabAnnSwitch);
        }

    }
}
