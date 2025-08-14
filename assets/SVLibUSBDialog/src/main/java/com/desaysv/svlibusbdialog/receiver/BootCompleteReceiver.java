package com.desaysv.svlibusbdialog.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.desaysv.svlibusbdialog.utils.ProjectUtils;


/**
 * @author uidp5370
 * @date 2019-6-3
 * USB设备状态变化的回调接口
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "ZNB_BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = " + action);
        ProjectUtils.checkNeedShowDialog();
    }
}
