package com.desaysv.svlibusbdialog.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.manager.DeviceStateManager;
import com.desaysv.svlibusbdialog.manager.QueryManager;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.svlibusbdialog.manager.ScanStateManager;
import com.desaysv.svlibusbdialog.utils.ProjectUtils;


/**
 * @author uidp5370
 * @date 2019-6-3
 * USB设备状态变化的回调接口
 */

public class USBStateReceiver extends BroadcastReceiver {

    private static final String TAG = "USBStateReceiver";

    private static USBStateReceiver mInstance = new USBStateReceiver();

    public static USBStateReceiver getInstance(){
        if (mInstance == null){
            synchronized (USBStateReceiver.class){
                if (mInstance == null){
                    mInstance = new USBStateReceiver();
                }
            }
        }
        return mInstance;
    }

    //这里有个问题：静态广播的onReceive可能在 init之前就收到了，此时Handler为空，所以要加上判空处理
    //后续看下是否能优化
    public void init(Context context){
        myHandler = new MyHandler(context);
    }


    private final static String MEDIA_MOUNTED = Intent.ACTION_MEDIA_MOUNTED;
    private final static String MEDIA_UNMOUNTED = Intent.ACTION_MEDIA_UNMOUNTED;
    private final static String MEDIA_EJECT = Intent.ACTION_MEDIA_EJECT;
    private final static String MEDIA_SCAN_START = Intent.ACTION_MEDIA_SCANNER_STARTED;
    private final static String MEDIA_SCAN_FINISH = Intent.ACTION_MEDIA_SCANNER_FINISHED;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action = " + action);
        String path = intent.getData().toString();

        switch (action) {
            case MEDIA_MOUNTED:
                if (path.contains(Constant.PATH.PATH_USB1)) {
                    ProjectUtils.checkNeedShowDialog();
                    DeviceStateManager.getInstance().setUsb1State(Constant.Device.STATE_MOUNTED);
                    if (myHandler != null) {
                        myHandler.sendEmptyMessage(MSG_UPDATE_QUERY_USB0);
                    }else {
                        handleQueryUSB1(context);
                    }
                    if (ProjectUtils.isNeedShowDialog()){
                        ProjectUtils.setIsFirstUse(false);
                    }
                }else if (path.contains(Constant.PATH.PATH_USB2)){
                    ProjectUtils.checkNeedShowDialog();
                    DeviceStateManager.getInstance().setUsb2State(Constant.Device.STATE_MOUNTED);
                    if (myHandler != null) {
                        myHandler.sendEmptyMessage(MSG_UPDATE_QUERY_USB1);
                    }else {
                        handleQueryUSB2(context);
                    }
                    if (ProjectUtils.isNeedShowDialog()){
                        ProjectUtils.setIsFirstUse(false);
                    }
                }
                break;
            case MEDIA_UNMOUNTED:
            case MEDIA_EJECT:
                if (path.contains(Constant.PATH.PATH_USB1)) {
                    DeviceStateManager.getInstance().setUsb1State(Constant.Device.STATE_UNMOUNTED);
                    if (myHandler != null) {
                        myHandler.removeMessages(MSG_UPDATE_QUERY_USB0);
                    }
                    if (ProjectUtils.isNeedShowDialog()){
                        ProjectUtils.setIsFirstUse(false);
                    }
                }else if (path.contains(Constant.PATH.PATH_USB2)){
                    DeviceStateManager.getInstance().setUsb2State(Constant.Device.STATE_UNMOUNTED);
                    if (myHandler != null) {
                        myHandler.removeMessages(MSG_UPDATE_QUERY_USB1);
                    }
                    if (ProjectUtils.isNeedShowDialog()){
                        ProjectUtils.setIsFirstUse(false);
                    }
                }
                break;
            case MEDIA_SCAN_START:
                if (path.contains(Constant.PATH.PATH_USB1)) {
                    ScanStateManager.getInstance().setUsb1State(Constant.Scanner.STATE_START);
                    if (myHandler != null) {
                        myHandler.sendEmptyMessage(MSG_UPDATE_QUERY_USB0);
                    }else {
                        handleQueryUSB1(context);
                    }
                }else if (path.contains(Constant.PATH.PATH_USB2)){
                    ScanStateManager.getInstance().setUsb2State(Constant.Scanner.STATE_START);
                    if (myHandler != null) {
                        myHandler.sendEmptyMessage(MSG_UPDATE_QUERY_USB1);
                    }else {
                        handleQueryUSB2(context);
                    }
                }
                break;
            case MEDIA_SCAN_FINISH:
                if (path.contains(Constant.PATH.PATH_USB1) && DeviceStateManager.getInstance().getUsb1State() != Constant.Device.STATE_UNMOUNTED) {
                    ScanStateManager.getInstance().setUsb1State(Constant.Scanner.STATE_FINISH);
                    boolean needUpdateUSB1 = QueryStateManager.getInstance().getUsb1MusicState() == Constant.Query.STATE_HAVING_NO_DATA
                            |QueryStateManager.getInstance().getUsb1PictureState() == Constant.Query.STATE_HAVING_NO_DATA
                            |QueryStateManager.getInstance().getUsb1VideoState() == Constant.Query.STATE_HAVING_NO_DATA
                            |QueryStateManager.getInstance().getUsb1MusicState() == Constant.Query.STATE_NO_QUERY
                            |QueryStateManager.getInstance().getUsb1PictureState() == Constant.Query.STATE_NO_QUERY
                            |QueryStateManager.getInstance().getUsb1VideoState() == Constant.Query.STATE_NO_QUERY;
                    Log.d(TAG,"needUpdateUSB1:"+needUpdateUSB1);
                    if (needUpdateUSB1) {
                        handleQueryUSB1(context);
                    }
                    if (myHandler != null) {
                        myHandler.removeMessages(MSG_UPDATE_QUERY_USB0);
                    }
                }else if (path.contains(Constant.PATH.PATH_USB2) && DeviceStateManager.getInstance().getUsb2State() != Constant.Device.STATE_UNMOUNTED){
                    ScanStateManager.getInstance().setUsb2State(Constant.Scanner.STATE_FINISH);
                    boolean needUpdateUSB2 = QueryStateManager.getInstance().getUsb2MusicState() == Constant.Query.STATE_HAVING_NO_DATA
                            |QueryStateManager.getInstance().getUsb2PictureState() == Constant.Query.STATE_HAVING_NO_DATA
                            |QueryStateManager.getInstance().getUsb2VideoState() == Constant.Query.STATE_HAVING_NO_DATA
                            |QueryStateManager.getInstance().getUsb2MusicState() == Constant.Query.STATE_NO_QUERY
                            |QueryStateManager.getInstance().getUsb2PictureState() == Constant.Query.STATE_NO_QUERY
                            |QueryStateManager.getInstance().getUsb2VideoState() == Constant.Query.STATE_NO_QUERY;
                    Log.d(TAG,"needUpdateUSB2:"+needUpdateUSB2);
                    if (needUpdateUSB2) {
                        handleQueryUSB2(context);
                    }
                    if (myHandler != null) {
                        myHandler.removeMessages(MSG_UPDATE_QUERY_USB1);
                    }
                }
                break;
        }
    }

    private void handleQueryUSB1(Context context){
        QueryManager.getInstance().startQueryUSB1(context);
    }

    private void handleQueryUSB2(Context context){
        QueryManager.getInstance().startQueryUSB2(context);
    }


    /**
     * 启动一个定时器，进行分段加载
     */
    private static final int MSG_UPDATE_QUERY_USB0 = 0;
    private static final int MSG_UPDATE_QUERY_USB1 = 1;
    private static final int MSG_REMOVE_MSG_UPDATE_QUERY_USB0 = 2;
    private static final int MSG_REMOVE_MSG_UPDATE_QUERY_USB1 = 3;
    private MyHandler myHandler;
    private static class MyHandler extends Handler {
        private Context mContext;
        public MyHandler(Context context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG,"handleMessage,msg:"+this);
            switch (msg.what){
                case MSG_UPDATE_QUERY_USB0:
                    QueryManager.getInstance().startQueryUSB1(mContext);
                    sendEmptyMessageDelayed(MSG_UPDATE_QUERY_USB0, 3000);
                    break;
                case MSG_UPDATE_QUERY_USB1:
                    QueryManager.getInstance().startQueryUSB2(mContext);
                    sendEmptyMessageDelayed(MSG_UPDATE_QUERY_USB1, 3000);
                    break;
                case MSG_REMOVE_MSG_UPDATE_QUERY_USB0:
                    removeMessages(MSG_UPDATE_QUERY_USB0);
                    break;
                case MSG_REMOVE_MSG_UPDATE_QUERY_USB1:
                    removeMessages(MSG_UPDATE_QUERY_USB1);
                    break;
            }
        }
    };


}
