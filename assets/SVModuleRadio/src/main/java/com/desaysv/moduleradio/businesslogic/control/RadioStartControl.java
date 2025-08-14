package com.desaysv.moduleradio.businesslogic.control;

import android.util.Log;
import android.widget.Toast;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;

/**
 * Created by LZM on 2019-11-29
 * Comment 收音音源恢复的逻辑，每个类自己实现
 */
public class RadioStartControl {

    private static final String TAG = "RadioStartControl";

    private static RadioStartControl instance;

    public static RadioStartControl getInstance() {
        if (instance == null) {
            synchronized (RadioStartControl.class) {
                if (instance == null) {
                    instance = new RadioStartControl();
                }
            }
        }
        return instance;
    }

    private RadioStartControl() {

    }

    /**
     * 音源恢复，恢复播放FM
     *
     * @return boolean
     */
    public boolean startFM(boolean isForeground) {
        Log.d(TAG, "startFM: isForeground = " + isForeground);
        if (isForeground) {
            Toast.makeText(AppBase.mContext, "启动FM界面", Toast.LENGTH_SHORT).show();

        }
        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                ChangeReasonData.BOOT_RESUME, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getFMRadioMessage());
        return true;
    }


    /**
     * 音源恢复，恢复播放AM
     *
     * @return boolean
     */
    public boolean startAM(boolean isForeground) {
        Log.d(TAG, "startAM: isForeground = " + isForeground);
        if (isForeground) {
            Toast.makeText(AppBase.mContext, "启动AM界面", Toast.LENGTH_SHORT).show();
        }
        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO,
                ChangeReasonData.BOOT_RESUME, ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getAMRadioMessage());
        return true;
    }

}
