package com.desaysv.moduleradio.Trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.desaysv.ivi.vdb.event.id.dsp.VDEventDsp;
import com.desaysv.ivi.vdb.event.id.dsp.VDValueDsp;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.mediacommonlib.base.AppBase;

public class RemoveRecentTrigger{
    private static final String TAG = "RemoveRecentTrigger";

    private static RemoveRecentTrigger instance;

    public static RemoveRecentTrigger getInstance() {
        if (instance == null) {
            synchronized (RemoveRecentTrigger.class) {
                if (instance == null) {
                    instance = new RemoveRecentTrigger();
                }
            }
        }
        return instance;
    }

    private RemoveRecentTrigger() {

    }

    private RemoveRecentReceiver removeRecentReceiver;

    public void init(){
        removeRecentReceiver = new RemoveRecentReceiver();
        registerRemoveRecentReceiver(AppBase.mContext);
        Log.d(TAG,"init");
    }

    public void deinit(){
        unregisterRemoveRecentReceiver(AppBase.mContext);
        Log.d(TAG,"deinit");
    }

    private void registerRemoveRecentReceiver(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.action.remove_recent");
        context.registerReceiver(removeRecentReceiver,intentFilter);
    }

    private void unregisterRemoveRecentReceiver(Context context){
        context.unregisterReceiver(removeRecentReceiver);
    }



    private static class RemoveRecentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null){
                String[] packages = intent.getStringArrayExtra("packages");
                if (packages != null){
                    for (String pkg: packages){
                        Log.d(TAG,"pkg:"+pkg);
                        if (pkg.equals("com.desaysv.svaudioapp")){
                            RadioControlTool.getInstance().releaseFocusWithSTR(ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage());
                            SVVDBControl.sendVDBusEvent(VDEventDsp.CHANNEL, VDValueDsp.DspChannelType.CHANNEL_MEDIA_MUSIC, 1);
                        }
                    }
                }
            }
        }
    }
}
