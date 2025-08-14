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


public class DABAnnSettingsDialog extends Dialog implements View.OnClickListener, SettingSwitchView.OnCheckedChangeListener {
    private static final String TAG = "DABAnnSettingsDialog";


    private ImageView ivDABClose;
    private SettingSwitchView scAnnAlarm;
    private SettingSwitchView scAnnTa;
    private SettingSwitchView scAnnTf;
    private SettingSwitchView scAnnWaring;
    private SettingSwitchView scAnnNews;
    private SettingSwitchView scAnnWeather;
    private SettingSwitchView scAnnEvent;
    private SettingSwitchView scAnnSpecial;
    private SettingSwitchView scAnnProgram;
    private SettingSwitchView scAnnSport;
    private SettingSwitchView scAnnFinancial;


    private Context mContext;

    public DABAnnSettingsDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init(mContext);
    }

    public DABAnnSettingsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init(mContext);
    }

    protected DABAnnSettingsDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
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
        setContentView(R.layout.dab_ann_settings);

        ivDABClose = findViewById(R.id.iv_dab_close);

        scAnnAlarm = findViewById(R.id.scAnnAlarm);
        scAnnTa = findViewById(R.id.scAnnTa);
        scAnnTf = findViewById(R.id.scAnnTf);
        scAnnWaring = findViewById(R.id.scAnnWaring);
        scAnnNews = findViewById(R.id.scAnnNews);
        scAnnWeather = findViewById(R.id.scAnnWeather);
        scAnnEvent = findViewById(R.id.scAnnEvent);
        scAnnSpecial = findViewById(R.id.scAnnSpecial);
        scAnnProgram = findViewById(R.id.scAnnProgram);
        scAnnSport = findViewById(R.id.scAnnSport);
        scAnnFinancial = findViewById(R.id.scAnnFinancial);

        ivDABClose.setOnClickListener(this);
        scAnnAlarm.setOnCheckedChangeListener(this);
        scAnnTa.setOnCheckedChangeListener(this);
        scAnnTf.setOnCheckedChangeListener(this);
        scAnnWaring.setOnCheckedChangeListener(this);
        scAnnNews.setOnCheckedChangeListener(this);
        scAnnWeather.setOnCheckedChangeListener(this);
        scAnnEvent.setOnCheckedChangeListener(this);
        scAnnSpecial.setOnCheckedChangeListener(this);
        scAnnProgram.setOnCheckedChangeListener(this);
        scAnnSport.setOnCheckedChangeListener(this);
        scAnnFinancial.setOnCheckedChangeListener(this);
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
            scAnnAlarm.setChecked(dabAnnSwitch.getAlarm() == DAB_SETTING_ON);
            scAnnTa.setChecked(dabAnnSwitch.getRoadTrafficFlash() == DAB_SETTING_ON);
            scAnnTf.setChecked(dabAnnSwitch.getTransportFlash() == DAB_SETTING_ON);
            scAnnWaring.setChecked(dabAnnSwitch.getWarning() == DAB_SETTING_ON);
            scAnnNews.setChecked(dabAnnSwitch.getNewsFlash() == DAB_SETTING_ON);
            scAnnWeather.setChecked(dabAnnSwitch.getAreaWeatherFlash() == DAB_SETTING_ON);
            scAnnEvent.setChecked(dabAnnSwitch.getEventAnnouncement() == DAB_SETTING_ON);
            scAnnSpecial.setChecked(dabAnnSwitch.getSpecialEvent() == DAB_SETTING_ON);
            scAnnProgram.setChecked(dabAnnSwitch.getProgramInformation() == DAB_SETTING_ON);
            scAnnSport.setChecked(dabAnnSwitch.getSportReport() == DAB_SETTING_ON);
            scAnnFinancial.setChecked(dabAnnSwitch.getFinancialReport() == DAB_SETTING_ON);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        Log.d(TAG,"onCheckedChanged,isChecked:"+isChecked);
        if (dabAnnSwitch != null) {
            if (id == R.id.scAnnAlarm) {
                dabAnnSwitch.setAlarm(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnTa) {
                dabAnnSwitch.setRoadTrafficFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnTf) {
                dabAnnSwitch.setTransportFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnWaring) {
                dabAnnSwitch.setWarning(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnNews) {
                dabAnnSwitch.setNewsFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnWeather) {
                dabAnnSwitch.setAreaWeatherFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnEvent) {
                dabAnnSwitch.setEventAnnouncement(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnSpecial) {
                dabAnnSwitch.setSpecialEvent(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnProgram) {
                dabAnnSwitch.setProgramInformation(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnSport) {
                dabAnnSwitch.setSportReport(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            } else if (id == R.id.scAnnFinancial) {
                dabAnnSwitch.setFinancialReport(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
            }
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.SET_DAB_ANN_SWITCH, ChangeReasonData.CLICK,dabAnnSwitch);
        }

    }
}
