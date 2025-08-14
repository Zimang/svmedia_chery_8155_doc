package com.desaysv.svlibusbdialog.dialog;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.desaysv.svlibusbdialog.R;
import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.iinterface.IDeviceStateChangedListener;
import com.desaysv.svlibusbdialog.iinterface.IQueryMusicStateChangedListener;
import com.desaysv.svlibusbdialog.iinterface.IQueryPictureStateChangedListener;
import com.desaysv.svlibusbdialog.iinterface.IQueryVideoStateChangedListener;
import com.desaysv.svlibusbdialog.manager.DeviceStateManager;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.svlibusbdialog.manager.ScanStateManager;
import com.desaysv.svlibusbdialog.observer.USB1DeviceStateObserver;
import com.desaysv.svlibusbdialog.observer.USB1QueryStateObserver;
import com.desaysv.svlibusbdialog.observer.USB2DeviceStateObserver;
import com.desaysv.svlibusbdialog.observer.USB2QueryStateObserver;
import com.desaysv.svlibusbdialog.utils.ProjectUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * created by ZNB on 2022-07-15
 * USB插入时的弹窗，用于显示当前媒体数据库的扫描状态
 * 单例模式有助于在USB1、2的情况下，直接动态替换显示内容，无需再新建一个
 */
public class SourceDialogUtil{

    public static final String TAG = "SourceDialogUtil";

    public static final String AUTO_TAB = "auto_tab";//自动跳转到USB图片


    private static SourceDialogUtil mInstance;

    public static SourceDialogUtil getInstance(){
        if (mInstance == null){
            synchronized (SourceDialogUtil.class){
                if (mInstance == null){
                    mInstance = new SourceDialogUtil();
                }
            }
        }
        return mInstance;
    }

    private SourceDialog dialog;

    //双USB的情况下，需要单独给每个USB设置弹窗
    //以避免同时拔插时，出现不弹窗的情况
    private SourceDialog dialogUSB1;
    private SourceDialog dialogUSB2;



    private boolean hasInit(Context context, boolean isClickCloseUSB0, boolean isClickCloseUSB1){
        if (ProjectUtils.hasUSB2()){
            if (dialogUSB1 == null){
                dialogUSB1 = new SourceDialog(context, R.style.dialogstyle);
                dialogUSB2 = new SourceDialog(context, R.style.dialogstyle);
                dialogUSB1.setClickClose(isClickCloseUSB0);
                dialogUSB2.setClickClose(isClickCloseUSB1);
                return false;
            }else {
                return true;
            }
        }else {
            if (dialog == null){
                dialog = new SourceDialog(context, R.style.dialogstyle);
                dialog.setClickClose(isClickCloseUSB0);
                return false;
            }else {
                return true;
            }
        }
    }

    /**
     * 有些情况下，获取到的背景为黑色，此时需要重置为默认背景
     */
    public void reShowDefaultBgDialog(){
        Log.d(TAG,"reShowDefaultBgDialog");
        if (ProjectUtils.hasUSB2()){
            if (dialogUSB1.isShowing()){
                dialogUSB1.setDefaultBg();
            }
            if (dialogUSB2.isShowing()){
                dialogUSB2.setDefaultBg();
            }
        }else {
            if (dialog != null) {

                if (dialog.isShowing()) {
                    dialog.setDefaultBg();
                }
            }
        }
    }


    private boolean currentScreenOff = false;
    public void reShowDialog(boolean isScreenOff){
        Log.d(TAG,"reShowDialog,isScreenOff:"+isScreenOff);
        if (currentScreenOff == isScreenOff){//相同时不需要处理
            return;
        }
        currentScreenOff = isScreenOff;
        if (ProjectUtils.hasUSB2()){
            dialogUSB1.setScreenOff(isScreenOff);
            dialogUSB2.setScreenOff(isScreenOff);
            if (dialogUSB1.isShowing()){
                dialogUSB1.setBlurBg();
            }
            if (dialogUSB2.isShowing()){
                dialogUSB2.setBlurBg();
            }
        }else {
            if (dialog != null) {
                dialog.setScreenOff(isScreenOff);
                if (dialog.isShowing()) {
                    dialog.setBlurBg();
                }
            }
        }
    }

    /**
     * 单例模式，因此提供一个重置为空的方法
     * 重置后，再重新初始化
     * 用于配置变更时，例如 白天/黑夜 模式切换
     */
    public void reInitDialog(Context context){
        Log.d(TAG,"resetDialog");
        if (ProjectUtils.hasUSB2()){
            if (dialogUSB1 != null) {
                dialogUSB1.init();
            }
            if (dialogUSB2 != null) {
                dialogUSB2.init();
            }
        }else {
            if (dialog != null) {
                dialog.init();
            }
        }
    }


    /**
     * 初始化Dialog，必须先执行
     * @isClickCloseUSB0 重新初始化的时候，需要根据上一个状态进行赋值，
     * 以避免在广播周期(MOUNTED--START)内，切换UIMode等导致弹窗出现多次
     */
    public void initDialog(Context context, boolean isClickCloseUSB0, boolean isClickCloseUSB1){
        if (!hasInit(context,isClickCloseUSB0,isClickCloseUSB1)){
            myHandler = new MyHandler(this);

            //注册对设备插拔、媒体查询状态的监听
            USB1DeviceStateObserver.getInstance().attachObserver(this.getClass().getSimpleName(), new IDeviceStateChangedListener() {
                @Override
                public void onDeviceStateUpdate() {
                    if (DeviceStateManager.getInstance().getUsb1State() == Constant.Device.STATE_MOUNTED){
                        Log.d(TAG,"USB1 STATE_MOUNTED");
                        if (ProjectUtils.hasUSB2()){
                            showUSB1Dialog();
                        }else {
                            showDialog();
                        }
                    }else if (DeviceStateManager.getInstance().getUsb1State() == Constant.Device.STATE_UNMOUNTED){
                        Log.d(TAG,"USB1 STATE_UNMOUNTED");
                        if (ProjectUtils.hasUSB2()){
                            Log.d(TAG,"USB1 STATE_UNMOUNTED:" + dialogUSB1.isShowing());
                            dismissUSB1Dialog();
                        }else {
                            Log.d(TAG,"USB1 STATE_UNMOUNTED:" + dialog.isShowing());
                            dismissDialog();
                        }
                    }
                }
            });

            USB2DeviceStateObserver.getInstance().attachObserver(this.getClass().getSimpleName(), new IDeviceStateChangedListener() {
                @Override
                public void onDeviceStateUpdate() {
                    if (DeviceStateManager.getInstance().getUsb2State() == Constant.Device.STATE_MOUNTED){
                        Log.d(TAG,"USB2 STATE_MOUNTED");
                        if (ProjectUtils.hasUSB2()){
                            showUSB2Dialog();
                        }else {
                            showDialog();
                        }
                    }else if (DeviceStateManager.getInstance().getUsb2State() == Constant.Device.STATE_UNMOUNTED){
                        Log.d(TAG,"USB2 STATE_UNMOUNTED");
                        if (ProjectUtils.hasUSB2()){
                            dismissUSB2Dialog();
                        }else {
                            dismissDialog();
                        }
                    }
                }
            });


            USB1QueryStateObserver.getInstance().attachMusicObserver(this.getClass().getSimpleName(), new IQueryMusicStateChangedListener() {
                @Override
                public void onQueryMusicStateUpdate() {
                    myHandler.sendEmptyMessage(MSG_UPDATE_USB1_MUSIC_QUERY_STATE);
                }
            });

            USB2QueryStateObserver.getInstance().attachMusicObserver(this.getClass().getSimpleName(), new IQueryMusicStateChangedListener() {
                @Override
                public void onQueryMusicStateUpdate() {
                    myHandler.sendEmptyMessage(MSG_UPDATE_USB2_MUSIC_QUERY_STATE);
                }
            });

            USB1QueryStateObserver.getInstance().attachPictureObserver(this.getClass().getSimpleName(), new IQueryPictureStateChangedListener() {
                @Override
                public void onQueryPictureStateUpdate() {
                    myHandler.sendEmptyMessage(MSG_UPDATE_USB1_PICTURE_QUERY_STATE);
                }
            });

            USB2QueryStateObserver.getInstance().attachPictureObserver(this.getClass().getSimpleName(), new IQueryPictureStateChangedListener() {
                @Override
                public void onQueryPictureStateUpdate() {
                    myHandler.sendEmptyMessage(MSG_UPDATE_USB2_PICTURE_QUERY_STATE);
                }
            });

            USB1QueryStateObserver.getInstance().attachVideoObserver(this.getClass().getSimpleName(), new IQueryVideoStateChangedListener() {
                @Override
                public void onQueryVideoStateUpdate() {
                    myHandler.sendEmptyMessage(MSG_UPDATE_USB1_VIDEO_QUERY_STATE);
                }
            });

            USB2QueryStateObserver.getInstance().attachVideoObserver(this.getClass().getSimpleName(), new IQueryVideoStateChangedListener() {
                @Override
                public void onQueryVideoStateUpdate() {
                    myHandler.sendEmptyMessage(MSG_UPDATE_USB2_VIDEO_QUERY_STATE);
                }
            });
        }

    }

    /**
     * 默认显示弹窗，提示语默认用USB
     */
    public void showDialog(){
        if (DeviceStateManager.getInstance().getUsb1State() == Constant.Device.STATE_UNMOUNTED){
            Log.d(TAG,"showDialog on STATE_UNMOUNTED, return");
            return;
        }
        if (dialog != null && !dialog.isShowing() && !dialog.isClickClose()){
            Log.d(TAG,"showDialog");
            if (!checkAAisTopActivity(dialog.getContext())) {
                if (ProjectUtils.isNeedShowDialog()) {
                    dialog.show();
                }
            }
            if (ScanStateManager.getInstance().getUsb1State() == Constant.Scanner.STATE_START) {//如果弹窗时已经发起了扫描(即start的广播在mounted广播之前被接收到)，那就直接设置为查询结果
                Log.d(TAG,"showDialog as start scan");
                dialog.setMusicScanState(QueryStateManager.getInstance().getUsb1MusicState());
                dialog.setPictureScanState(QueryStateManager.getInstance().getUsb1PictureState());
                dialog.setVideoScanState(QueryStateManager.getInstance().getUsb1VideoState());
            }else {
                dialog.setMusicScanState(Constant.Device.STATE_MOUNTED);
                dialog.setPictureScanState(Constant.Device.STATE_MOUNTED);
                dialog.setVideoScanState(Constant.Device.STATE_MOUNTED);
            }
        }
    }

    /**
     * 消失弹窗
     */
    public void dismissDialog() {
        if (dialog != null) {
            Log.d(TAG,"dismissDialog");
            dialog.setClickClose(false);
            dialog.dismiss();
        }
    }


    /**
     * 消失弹窗
     */
    public void dismissDialogWithACTION_DIALOG_CLOSE() {
        if (dialog != null && dialog.isShowing()) {
            Log.d(TAG,"dismissDialogWithACTION_DIALOG_CLOSE");
            dialog.setClickClose(true);
            dialog.dismiss();
        }
    }

    /**
     * 显示USB1弹窗
     */
    public void showUSB1Dialog(){
        if (dialogUSB1 != null && !dialogUSB1.isShowing() && !dialogUSB1.isClickClose()){
            Log.d(TAG,"showUSB1Dialog");
            if (!ProjectUtils.isSupportTwoDialog()) {
                dialogUSB2.dismiss();
            }
            dialogUSB1.show();
            dialogUSB1.setTitleWithPath(Constant.PATH.PATH_USB1);
            if (ScanStateManager.getInstance().getUsb1State() == Constant.Scanner.STATE_START) {
                dialogUSB1.setMusicScanState(QueryStateManager.getInstance().getUsb1MusicState());
                dialogUSB1.setPictureScanState(QueryStateManager.getInstance().getUsb1PictureState());
                dialogUSB1.setVideoScanState(QueryStateManager.getInstance().getUsb1VideoState());
            }else {
                dialogUSB1.setMusicScanState(Constant.Device.STATE_MOUNTED);
                dialogUSB1.setPictureScanState(Constant.Device.STATE_MOUNTED);
                dialogUSB1.setVideoScanState(Constant.Device.STATE_MOUNTED);
            }
        }
    }

    /**
     * 消失USB1弹窗
     */
    public void dismissUSB1Dialog() {
        if (dialogUSB1 != null) {
            Log.d(TAG,"dismissUSB1Dialog");
            dialogUSB1.setClickClose(false);
            dialogUSB1.dismiss();
        }
    }

    /**
     * 显示USB2弹窗
     */
    public void showUSB2Dialog(){
        if (dialogUSB2 != null && !dialogUSB2.isShowing() && !dialogUSB2.isClickClose()){
            Log.d(TAG,"showUSB2Dialog");
            if (!ProjectUtils.isSupportTwoDialog()) {
                dialogUSB1.dismiss();
            }
            dialogUSB2.show();
            dialogUSB2.setTitleWithPath(Constant.PATH.PATH_USB2);
            if (ScanStateManager.getInstance().getUsb2State() == Constant.Scanner.STATE_START) {
                dialogUSB2.setMusicScanState(QueryStateManager.getInstance().getUsb2MusicState());
                dialogUSB2.setPictureScanState(QueryStateManager.getInstance().getUsb2PictureState());
                dialogUSB2.setVideoScanState(QueryStateManager.getInstance().getUsb2VideoState());
            }else {
                dialogUSB2.setMusicScanState(Constant.Device.STATE_MOUNTED);
                dialogUSB2.setPictureScanState(Constant.Device.STATE_MOUNTED);
                dialogUSB2.setVideoScanState(Constant.Device.STATE_MOUNTED);
            }
        }
    }

    /**
     * 消失USB2弹窗
     */
    public void dismissUSB2Dialog() {
        if (dialogUSB2 != null) {
            Log.d(TAG,"dismissUSB2Dialog");
            dialogUSB2.setClickClose(false);
            dialogUSB2.dismiss();
        }
    }



    /**
     * 设置音乐类型查询状态，更新界面
     * @param state
     */
    private void setMusicQueryState(int state){
        Log.d(TAG, "setMusicQueryState: " + state);
        if (dialog != null) {
            if (!ProjectUtils.isIsFirstUse()) {
                showDialog();
            }
            dialog.setMusicScanState(state);
        }
    }

    /**
     * 设置图片类型查询状态，更新界面
     * @param state
     */
    private void setPictureQueryState(int state){
        Log.d(TAG, "setPictureQueryState: " + state);
        if (dialog != null) {
            if (!ProjectUtils.isIsFirstUse()) {
                showDialog();
            }
            dialog.setPictureScanState(state);
        }
    }

    /**
     * 设置视频类型查询状态，更新界面
     * @param state
     */
    private void setVideoQueryState(int state){
        Log.d(TAG, "setVideoQueryState: " + state);
        if (dialog != null) {
            if (!ProjectUtils.isIsFirstUse()) {
                showDialog();
            }
            dialog.setVideoScanState(state);
        }
    }


    /**
     * 设置USB1音乐类型查询状态，更新界面
     * @param state
     */
    private void setUSB1MusicQueryState(int state){
        Log.d(TAG, "setUSB1MusicQueryState: " + state);
        if (dialogUSB1 != null) {
            showUSB1Dialog();
            dialogUSB1.setMusicScanState(state);
        }
    }

    /**
     * 设置USB1图片类型查询状态，更新界面
     * @param state
     */
    private void setUSB1PictureQueryState(int state){
        Log.d(TAG, "setUSB1PictureQueryState: " + state);
        if (dialogUSB1 != null) {
            showUSB1Dialog();
            dialogUSB1.setPictureScanState(state);
        }
    }

    /**
     * 设置USB1视频类型查询状态，更新界面
     * @param state
     */
    private void setUSB1VideoQueryState(int state){
        Log.d(TAG, "setUSB1VideoQueryState: " + state);
        if (dialogUSB1 != null) {
            showUSB1Dialog();
            dialogUSB1.setVideoScanState(state);
        }
    }

    /**
     * 设置USB2音乐类型查询状态，更新界面
     * @param state
     */
    private void setUSB2MusicQueryState(int state){
        Log.d(TAG, "setUSB2MusicQueryState: " + state);
        if (dialogUSB2 != null) {
            showUSB2Dialog();
            dialogUSB2.setMusicScanState(state);
        }
    }

    /**
     * 设置USB2图片类型查询状态，更新界面
     * @param state
     */
    private void setUSB2PictureQueryState(int state){
        Log.d(TAG, "setUSB2PictureQueryState: " + state);
        if (dialogUSB2 != null) {
            showUSB2Dialog();
            dialogUSB2.setPictureScanState(state);
        }
    }

    /**
     * 设置USB2视频类型查询状态，更新界面
     * @param state
     */
    private void setUSB2VideoQueryState(int state){
        Log.d(TAG, "setUSB2VideoQueryState: " + state);
        if (dialogUSB2 != null) {
            showUSB2Dialog();
            dialogUSB2.setVideoScanState(state);
        }
    }




    //启动Handler更新界面
    private static final int MSG_UPDATE_USB1_MUSIC_QUERY_STATE= 0;
    private static final int MSG_UPDATE_USB2_MUSIC_QUERY_STATE= 1;
    private static final int MSG_UPDATE_USB1_PICTURE_QUERY_STATE= 2;
    private static final int MSG_UPDATE_USB2_PICTURE_QUERY_STATE= 3;
    private static final int MSG_UPDATE_USB1_VIDEO_QUERY_STATE= 4;
    private static final int MSG_UPDATE_USB2_VIDEO_QUERY_STATE= 5;

    private MyHandler myHandler;
    private static class MyHandler extends Handler {
        private WeakReference<SourceDialogUtil> weakReference;
        public MyHandler(SourceDialogUtil queryTrigger) {
            weakReference = new WeakReference<>(queryTrigger);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG,"handleMessage,msg:"+msg.what);
            switch (msg.what){
                case MSG_UPDATE_USB1_MUSIC_QUERY_STATE:
                    if (ProjectUtils.hasUSB2()){
                        weakReference.get().setUSB1MusicQueryState(QueryStateManager.getInstance().getUsb1MusicState());
                    }else {
                        weakReference.get().setMusicQueryState(QueryStateManager.getInstance().getUsb1MusicState());
                    }
                    break;
                case MSG_UPDATE_USB2_MUSIC_QUERY_STATE:
                    if (ProjectUtils.hasUSB2()){
                        weakReference.get().setUSB2MusicQueryState(QueryStateManager.getInstance().getUsb2MusicState());
                    }else {
                        weakReference.get().setMusicQueryState(QueryStateManager.getInstance().getUsb2MusicState());
                    }
                    break;
                case MSG_UPDATE_USB1_PICTURE_QUERY_STATE:
                    if (ProjectUtils.hasUSB2()){
                        weakReference.get().setUSB1PictureQueryState(QueryStateManager.getInstance().getUsb1PictureState());
                    }else {
                        weakReference.get().setPictureQueryState(QueryStateManager.getInstance().getUsb1PictureState());
                    }
                    break;
                case MSG_UPDATE_USB2_PICTURE_QUERY_STATE:
                    if (ProjectUtils.hasUSB2()){
                        weakReference.get().setUSB2PictureQueryState(QueryStateManager.getInstance().getUsb2PictureState());
                    }else {
                        weakReference.get().setPictureQueryState(QueryStateManager.getInstance().getUsb2PictureState());
                    }
                    break;
                case MSG_UPDATE_USB1_VIDEO_QUERY_STATE:
                    if (ProjectUtils.hasUSB2()){
                        weakReference.get().setUSB1VideoQueryState(QueryStateManager.getInstance().getUsb1VideoState());
                    }else {
                        weakReference.get().setVideoQueryState(QueryStateManager.getInstance().getUsb1VideoState());
                    }
                    break;
                case MSG_UPDATE_USB2_VIDEO_QUERY_STATE:
                    if (ProjectUtils.hasUSB2()){
                        weakReference.get().setUSB2VideoQueryState(QueryStateManager.getInstance().getUsb2VideoState());
                    }else {
                        weakReference.get().setVideoQueryState(QueryStateManager.getInstance().getUsb2VideoState());
                    }
                    break;
            }
        }
    }


    //检查互联APP 是否在最顶部
    private static final String AA_APP = "com.google.android.projection.sink";
    private static final String CP_APP = "com.desaysv.vehicle.carplayapp";
    public boolean checkAAisTopActivity(Context context){
        List<ActivityManager.RunningTaskInfo> runningTaskInfos =((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
        if (runningTaskInfos.size() <= 0) {
            return false;
        }
        ComponentName topActivity = runningTaskInfos.get(0).topActivity;
        String topPkgName = topActivity.getPackageName();
        Log.d(TAG, "checkAAisTopActivity: topActivity = " + topActivity + " topPkgName =" + topPkgName);
        if (AA_APP.equals(topPkgName) || CP_APP.equals(topPkgName)){
            Log.i(TAG, "checkAAisTopActivity: AA app is TOP");
            return true;
        }
        return false;
    }

}
