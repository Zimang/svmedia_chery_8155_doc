package com.desaysv.moduleradio.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;


public class ParameterDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "ParameterDialog";


    private TextView tvLevelDown;
    private TextView tvLevelValue;
    private TextView tvLevelUp;

    private TextView tvNsnDown;
    private TextView tvNsnValue;
    private TextView tvNsnUp;

    private TextView tvWamDown;
    private TextView tvWamValue;
    private TextView tvWamUp;

    private TextView tvOffsetDown;
    private TextView tvOffsetValue;
    private TextView tvOffsetUp;

    private TextView tvBdwDown;
    private TextView tvBdwValue;
    private TextView tvBdwUp;

    private TextView tvModDown;
    private TextView tvModValue;
    private TextView tvModUp;

    private TextView tvDABDown;
    private TextView tvDABValue;
    private TextView tvDABUp;

    private TextView tvSetCancel;
    private TextView tvSetConfirm;

    private int levelValue;
    private int nsnValue;
    private int wamValue;
    private int offsetValue;
    private int bdwValue;
    private int modValue;
    private int dABValue;


    public ParameterDialog(@NonNull Context context) {
        super(context);
    }

    public ParameterDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected ParameterDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init(){
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.radio_parameter_settins_dialog);
        tvLevelDown = findViewById(R.id.tvLevelDown);
        tvLevelValue = findViewById(R.id.tvLevelValue);
        tvLevelUp = findViewById(R.id.tvLevelUp);

        tvNsnDown = findViewById(R.id.tvNsnDown);
        tvNsnValue = findViewById(R.id.tvNsnValue);
        tvNsnUp = findViewById(R.id.tvNsnUp);

        tvWamDown = findViewById(R.id.tvWamDown);
        tvWamValue = findViewById(R.id.tvWamValue);
        tvWamUp = findViewById(R.id.tvWamUp);

        tvOffsetDown = findViewById(R.id.tvOffsetDown);
        tvOffsetValue = findViewById(R.id.tvOffsetValue);
        tvOffsetUp = findViewById(R.id.tvOffsetUp);

        tvBdwDown = findViewById(R.id.tvBdwDown);
        tvBdwValue = findViewById(R.id.tvBdwValue);
        tvBdwUp = findViewById(R.id.tvBdwUp);

        tvModDown = findViewById(R.id.tvModDown);
        tvModValue = findViewById(R.id.tvModValue);
        tvModUp = findViewById(R.id.tvModUp);

        tvDABDown = findViewById(R.id.tvDABDown);
        tvDABValue = findViewById(R.id.tvDABValue);
        tvDABUp = findViewById(R.id.tvDABUp);

        tvSetCancel = findViewById(R.id.tvSetCancel);
        tvSetConfirm = findViewById(R.id.tvSetConfirm);

        tvLevelDown.setOnClickListener(this);
        tvLevelUp.setOnClickListener(this);

        tvNsnDown.setOnClickListener(this);
        tvNsnUp.setOnClickListener(this);

        tvWamDown.setOnClickListener(this);
        tvWamUp.setOnClickListener(this);

        tvOffsetDown.setOnClickListener(this);
        tvOffsetUp.setOnClickListener(this);

        tvBdwDown.setOnClickListener(this);
        tvBdwUp.setOnClickListener(this);
        tvModDown.setOnClickListener(this);
        tvModUp.setOnClickListener(this);

        tvDABDown.setOnClickListener(this);
        tvDABUp.setOnClickListener(this);

        tvSetCancel.setOnClickListener(this);
        tvSetConfirm.setOnClickListener(this);
    }

    private void showCurrentQualityCondition(){
        RadioParameter radioParameter = CurrentRadioInfo.getInstance().getRadioParameter();
        Log.d(TAG,"showCurrentQualityCondition,radioParameter:"+radioParameter);
        if (radioParameter == null){
            return;
        }

        levelValue = radioParameter.getLevel();
        nsnValue = radioParameter.getNsn();
        wamValue = radioParameter.getWam();
        offsetValue = radioParameter.getOffset();
        bdwValue = radioParameter.getBdw();
        modValue = radioParameter.getMode();
        dABValue = radioParameter.getQlevel();

        tvLevelValue.setText("LevelValue: "+levelValue);
        tvNsnValue.setText("nsnValue: " + nsnValue);
        tvWamValue.setText("wamValue: " + wamValue);
        tvOffsetValue.setText("offsetValue: " + offsetValue);
        tvBdwValue.setText("bdwValue: " + bdwValue);
        tvModValue.setText("modValue: " + modValue);
        tvDABValue.setText("dABValue: " + dABValue);
    }

    private void updateCurrentCondition(){
        tvLevelValue.setText("LevelValue: "+levelValue);
        tvNsnValue.setText("nsnValue: " + nsnValue);
        tvWamValue.setText("wamValue: " + wamValue);
        tvOffsetValue.setText("offsetValue: " + offsetValue);
        tvBdwValue.setText("bdwValue: " + bdwValue);
        tvModValue.setText("modValue: " + modValue);
        tvDABValue.setText("dABValue: " + dABValue);
    }

    private void setCondition(){
        RadioParameter radioParameter = new RadioParameter();
        radioParameter.setLevel(levelValue);
        radioParameter.setNsn(nsnValue);
        radioParameter.setWam(wamValue);
        radioParameter.setOffset(offsetValue);
        radioParameter.setBdw(bdwValue);
        //radioParameter.setMode(modValue);
        radioParameter.setQlevel(dABValue);
        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.SET_QUALITY_CONDITION, ChangeReasonData.CLICK,radioParameter);
    }


    @Override
    public void show() {
        if (!isShowing()) {
            int width = WindowManager.LayoutParams.WRAP_CONTENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            Window window = getWindow();
            showCurrentQualityCondition();
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
        if (id == R.id.tvLevelDown){
            levelValue--;
        }else if (id == R.id.tvLevelUp){
            levelValue++;
        }
        if (id == R.id.tvNsnDown){
            nsnValue--;
        }else if (id == R.id.tvNsnUp){
            nsnValue++;
        }
        if (id == R.id.tvWamDown){
            wamValue--;
        }else if (id == R.id.tvWamUp){
            wamValue++;
        }
        if (id == R.id.tvOffsetDown){
            offsetValue--;
        }else if (id == R.id.tvOffsetUp){
            offsetValue++;
        }
        if (id == R.id.tvBdwDown){
            bdwValue--;
        }else if (id == R.id.tvBdwUp){
            bdwValue++;
        }
        if (id == R.id.tvModDown){
            modValue--;
        }else if (id == R.id.tvModUp){
            modValue++;
        }
        if (id == R.id.tvDABDown){
            dABValue--;
        }else if (id == R.id.tvDABUp){
            dABValue++;
        }
        updateCurrentCondition();

        if (id == R.id.tvSetCancel){
            dismiss();
        }else if (id == R.id.tvSetConfirm){
            setCondition();
            dismiss();
        }
    }

}
