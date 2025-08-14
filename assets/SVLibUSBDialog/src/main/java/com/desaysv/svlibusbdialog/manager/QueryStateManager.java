package com.desaysv.svlibusbdialog.manager;

import static com.desaysv.svlibusbdialog.constant.Constant.Query.STATE_NO_QUERY;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.observer.USB1QueryStateObserver;
import com.desaysv.svlibusbdialog.observer.USB2QueryStateObserver;
import com.desaysv.svlibusbdialog.receiver.DialogStateReceiver;
import com.desaysv.svlibusbdialog.receiver.USBStateReceiver;

/**
 * 设置连接状态的管理器
 */
public class QueryStateManager {

    private static QueryStateManager mInstance;
    private static final String TAG = "QueryStateManager";

    public static QueryStateManager getInstance(){
        if (mInstance == null){
            synchronized (QueryStateManager.class){
                if (mInstance == null){
                    mInstance = new QueryStateManager();
                }
            }
        }
        return mInstance;
    }

    private int usb1MusicState = STATE_NO_QUERY;
    private int usb2MusicState = STATE_NO_QUERY;
    private int usb1PictureState = STATE_NO_QUERY;
    private int usb2PictureState = STATE_NO_QUERY;
    private int usb1VideoState = STATE_NO_QUERY;
    private int usb2VideoState = STATE_NO_QUERY;

    /**
     * 初始化注册媒体扫描广播
     */
    public void init(Context context){
        USBStateReceiver.getInstance().init(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        context.registerReceiver(USBStateReceiver.getInstance(), intentFilter);
    }

    public int getUsb1MusicState() {
        Log.d(TAG,"getUsb1MusicState:"+usb1MusicState);
        return usb1MusicState;
    }

    public void setUsb1MusicState(int usb1MusicState) {
        this.usb1MusicState = usb1MusicState;
        USB1QueryStateObserver.getInstance().notifyMusicObserver();
    }

    public int getUsb2MusicState() {
        Log.d(TAG,"getUsb2MusicState:"+usb2MusicState);
        return usb2MusicState;
    }

    public void setUsb2MusicState(int usb2MusicState) {
        this.usb2MusicState = usb2MusicState;
        USB2QueryStateObserver.getInstance().notifyMusicObserver();
    }

    public int getUsb1PictureState() {
        Log.d(TAG,"getUsb1PictureState:"+usb1PictureState);
        return usb1PictureState;
    }

    public void setUsb1PictureState(int usb1PictureState) {
        this.usb1PictureState = usb1PictureState;
        USB1QueryStateObserver.getInstance().notifyPictureObserver();
    }

    public int getUsb2PictureState() {
        Log.d(TAG,"getUsb2PictureState:"+usb2PictureState);
        return usb2PictureState;
    }

    public void setUsb2PictureState(int usb2PictureState) {
        this.usb2PictureState = usb2PictureState;
        USB2QueryStateObserver.getInstance().notifyPictureObserver();
    }

    public int getUsb1VideoState() {
        Log.d(TAG,"getUsb1VideoState:"+usb1VideoState);
        return usb1VideoState;
    }

    public void setUsb1VideoState(int usb1VideoState) {
        this.usb1VideoState = usb1VideoState;
        USB1QueryStateObserver.getInstance().notifyVideoObserver();
    }

    public int getUsb2VideoState() {
        Log.d(TAG,"getUsb2VideoState:"+usb2VideoState);
        return usb2VideoState;
    }

    public void setUsb2VideoState(int usb2VideoState) {
        this.usb2VideoState = usb2VideoState;
        USB2QueryStateObserver.getInstance().notifyVideoObserver();
    }
}
