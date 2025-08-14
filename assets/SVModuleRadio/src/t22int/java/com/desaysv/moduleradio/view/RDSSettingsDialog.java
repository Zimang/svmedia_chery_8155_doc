package com.desaysv.moduleradio.view;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.utils.FastBlur;
import com.sy.swbt.SettingSwitchView;


public class RDSSettingsDialog extends Dialog implements View.OnClickListener, SettingSwitchView.OnCheckedChangeListener {
    private static final String TAG = "RDSSettingsDialog";


    private ImageView ivRdsClose;
    private SettingSwitchView scRdsTotal;
    private SettingSwitchView scRdsRec;
    private SettingSwitchView scRdsAf;
    private SettingSwitchView scRdsTa;

    private Context mContext;


    private SharedPreferences spRDS;
    private SharedPreferences.Editor edRDS;
    public static final String RDS_TOTAL_SWITCH = "rds_total_switch";

    //收音的控制器
    protected IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    protected IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    protected IGetControlTool mGetControlTool;

    //模糊背景
    protected RelativeLayout rlSettingBg;

    public RDSSettingsDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init(mContext);
    }

    public RDSSettingsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init(mContext);
    }

    protected RDSSettingsDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
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
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        //getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.radio_rds_dialog);

        rlSettingBg=findViewById(R.id.rl_dialog_full_bg);
        ivRdsClose = findViewById(R.id.iv_rds_close);
        scRdsTotal = findViewById(R.id.sc_rds_total);
        scRdsRec = findViewById(R.id.sc_rds_rec);
        scRdsAf = findViewById(R.id.sc_rds_af);
        scRdsTa = findViewById(R.id.sc_rds_ta);

        ivRdsClose.setOnClickListener(this);
        scRdsTotal.setOnCheckedChangeListener(this);
        scRdsRec.setOnCheckedChangeListener(this);
        scRdsAf.setOnCheckedChangeListener(this);
        scRdsTa.setOnCheckedChangeListener(this);

        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mGetControlTool = ModuleRadioTrigger.getInstance().mGetControlTool;

        mContext = context;

        spRDS = context.getSharedPreferences(RDS_TOTAL_SWITCH, MODE_PRIVATE);
        edRDS = spRDS.edit();
    }

    @Override
    public void show() {
        if (!isShowing()) {
            setBlurBg();
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
            updateRDSState();
        }
    }

    /**
     * 设置为模糊背景
     */
    public void setBlurBg(){
        //截图模糊处理用作背景
        if (rlSettingBg != null) {//t26没有这个
            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
                BitmapDrawable blurDrawable = FastBlur.shotScreenBlurAndDimDrawable(getContext(), 5, 0.3f);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (blurDrawable != null) {
                        rlSettingBg.setBackground(blurDrawable);
                    }else {
                        Log.d(TAG,"setBlurBg ScreenOff");
                        rlSettingBg.setBackground(null);
                    }
                });
            }).run();
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

    RDSSettingsSwitch rdsSettingsSwitch;
    /**
     * 初始化和设置变化的时候，从底层获取更新一下状态
     */
    private void updateRDSState() {
        rdsSettingsSwitch = mGetRadioStatusTool.getRDSSettingsSwitchStatus();
        Log.d(TAG, "updateRDSState,rdsSettingsSwitch:" + rdsSettingsSwitch);
        if (rdsSettingsSwitch == null) {
            return;
        }
        scRdsTotal.setChecked(rdsSettingsSwitch.getRds() == 1);
        scRdsRec.setChecked(rdsSettingsSwitch.getReg() == 1);
        scRdsAf.setChecked(rdsSettingsSwitch.getAf() == 1);
        scRdsTa.setChecked(rdsSettingsSwitch.getTa() == 1);
        if (!getTotalState()) {//总开关关闭
            updateView(false);
        } else {
            updateView(true);
        }
    }


    public boolean getTotalState() {
        return spRDS.getBoolean(RDS_TOTAL_SWITCH, false);
    }


    private void updateView(boolean isCheck) {
        Log.d(TAG, "updateView,isCheck:" + isCheck);
        if (isCheck) {
            //保持关闭并放开
            scRdsTotal.setChecked(true);
            scRdsRec.setEnabled(true);
            scRdsAf.setEnabled(true);
            scRdsTa.setEnabled(true);
        } else {
            //关闭并置灰
            scRdsRec.setEnabled(false);
            scRdsAf.setEnabled(false);
            scRdsTa.setEnabled(false);
        }
    }

    /**
     * 根据设置状态，保存到SP
     * 主要是RDSTotal这个总开关
     *
     * @param isCheck
     */
    private void saveTotalState(boolean isCheck) {
        Log.d(TAG, "saveState,isCheck:" + isCheck);
        edRDS.putBoolean(RDS_TOTAL_SWITCH, isCheck);
        edRDS.commit();
    }

    /**
     * 设置某项RDS的状态
     *
     * @param position
     * @param isChecked
     */
    private void setRDSItem(int position, boolean isChecked) {
        Log.d(TAG, "setRDSItem,position:" + position + ",isChecked:" + isChecked);
        //转换成可设置给底层的对象
        if (rdsSettingsSwitch == null){
            rdsSettingsSwitch = new RDSSettingsSwitch();
        }
        switch (position) {
            case RadioConstants.RDS.POSITION_RDS:
                rdsSettingsSwitch.setRds(isChecked ? 1 : 0);
                break;
            case RadioConstants.RDS.POSITION_REC:
                rdsSettingsSwitch.setReg(isChecked ? 1 : 0);
                break;
            case RadioConstants.RDS.POSITION_AF:
                rdsSettingsSwitch.setAf(isChecked ? 1 : 0);
                break;
            case RadioConstants.RDS.POSITION_TA:
                rdsSettingsSwitch.setTa(isChecked ? 1 : 0);
                break;
        }
        mRadioControl.processCommand(RadioAction.SET_RDS_SETTING_SWITCH, ChangeReasonData.CLICK, rdsSettingsSwitch);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        Log.d(TAG, "onCheckedChanged,isChecked:" + isChecked);
        if (id == R.id.sc_rds_total) {
            saveTotalState(isChecked);
            updateView(isChecked);
            setRDSItem(RadioConstants.RDS.POSITION_RDS, isChecked);
            //埋点：打开/关闭 RDS
            PointTrigger.getInstance().trackEvent(Point.KeyName.RDSSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.CLOSE : Point.FieldValue.OPEN);//注意这里反过来
        } else if (id == R.id.sc_rds_rec) {
            setRDSItem(RadioConstants.RDS.POSITION_REC, isChecked);
        } else if (id == R.id.sc_rds_af) {
            setRDSItem(RadioConstants.RDS.POSITION_AF, isChecked);
            //埋点：打开/关闭 RDS
            PointTrigger.getInstance().trackEvent(Point.KeyName.AFSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.CLOSE : Point.FieldValue.OPEN);//注意这里反过来
        } else if (id == R.id.sc_rds_ta) {
            setRDSItem(RadioConstants.RDS.POSITION_TA, isChecked);
            //埋点：打开/关闭 RDS
            PointTrigger.getInstance().trackEvent(Point.KeyName.TASwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.CLOSE : Point.FieldValue.OPEN);//注意这里反过来
        }
    }
}
