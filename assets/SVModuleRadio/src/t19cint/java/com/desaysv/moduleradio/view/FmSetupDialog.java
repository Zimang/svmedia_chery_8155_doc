package com.desaysv.moduleradio.view;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.sy.swbt.SettingSwitchView;

public class FmSetupDialog extends Dialog implements View.OnClickListener, SettingSwitchView.OnCheckedChangeListener {
    private static final String TAG = "FmSetupDialog";


    private ImageView ivRdsClose;
    private SettingSwitchView scRdsAf;
    private SettingSwitchView scRdsTa;
    private TextView tv_rds_af;
    private TextView tv_rds_ta;

    private Context mContext;


    //收音的控制器
    protected IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    protected IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    protected IGetControlTool mGetControlTool;

    public FmSetupDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init(mContext);
    }

    public FmSetupDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init(mContext);
    }

    protected FmSetupDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
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
        boolean isRightRudder = ProductUtils.isRightRudder();
        setContentView(isRightRudder ? R.layout.radio_fmsetup_dialog_right : R.layout.radio_fmsetup_dialog);

        ivRdsClose = findViewById(R.id.iv_rds_close);
        scRdsAf = findViewById(R.id.sc_rds_af);
        scRdsTa = findViewById(R.id.sc_rds_ta);

        tv_rds_af = findViewById(R.id.tv_rds_af);
        tv_rds_ta = findViewById(R.id.tv_rds_ta);

        ivRdsClose.setOnClickListener(this);
        scRdsAf.setOnCheckedChangeListener(this);
        scRdsTa.setOnCheckedChangeListener(this);

        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mGetControlTool = ModuleRadioTrigger.getInstance().mGetControlTool;

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
            updateRDSState();
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
        scRdsAf.setChecked(rdsSettingsSwitch.getAf() == 1);
        scRdsTa.setChecked(rdsSettingsSwitch.getTa() == 1);
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
        if (id == R.id.sc_rds_af) {
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
