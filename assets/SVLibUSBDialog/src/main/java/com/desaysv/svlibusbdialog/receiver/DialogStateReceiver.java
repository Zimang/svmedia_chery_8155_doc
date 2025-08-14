package com.desaysv.svlibusbdialog.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.dialog.SourceDialogUtil;


/**
 * created by ZNB on 2022-11-18
 * SystemUI发出的关闭Dialog的广播
 */

public class DialogStateReceiver extends BroadcastReceiver {

    private static final String TAG = "DialogStateReceiver";

    private static DialogStateReceiver mInstance = new DialogStateReceiver();

    public static DialogStateReceiver getInstance(){
        if (mInstance == null){
            synchronized (DialogStateReceiver.class){
                if (mInstance == null){
                    mInstance = new DialogStateReceiver();
                }
            }
        }
        return mInstance;
    }

    public final static String ACTION_DIALOG_CLOSE = "com.desaysv.systemui.ACTION_DIALOG_CLOSE";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = " + action);

        String name = intent.getStringExtra(Constant.FLAG_DIALOG_CLOSE_KEY);
        Log.d(TAG, "onReceive: name = " + name);

        switch (action) {
            case ACTION_DIALOG_CLOSE:
                if (!(Constant.FLAG_DIALOG_CLOSE_NAME.equals(name))) {//如果是USBDialog自己发送的广播，那就不处理
                    SourceDialogUtil.getInstance().dismissDialogWithACTION_DIALOG_CLOSE();
                }
                break;
        }
    }

}
