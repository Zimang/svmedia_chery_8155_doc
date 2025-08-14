package com.desaysv.svlibusbdialog.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.svlibusbdialog.R;
import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.utils.FastBlur;
import com.desaysv.svlibusbdialog.receiver.DialogStateReceiver;

public class SourceDialog extends Dialog implements View.OnClickListener{
    private static final String TAG = "SourceDialog";

    //定义头部View
    private ImageView iv_close;
    private TextView tv_title;

    //定义音乐类型的View
    private RelativeLayout rl_type_music;
    private ImageView iv_icon_music;
    private ImageView iv_next_music;
    private TextView tv_type_music;

    //定义图片类型的View
    private RelativeLayout rl_type_picture;
    private ImageView iv_icon_picture;
    private ImageView iv_next_picture;
    private TextView tv_type_picture;

    //定义视频类型的View
    private RelativeLayout rl_type_video;
    private ImageView iv_icon_video;
    private ImageView iv_next_video;
    private TextView tv_type_video;

    private boolean isRightRudder;

    //定义动画
    private ObjectAnimator loadingMusicAnimator;
    private ObjectAnimator loadingPictureAnimator;
    private ObjectAnimator loadingVideoAnimator;
    private static final int ANIMATION_DURATION = 1200;

    private RelativeLayout rl_dialog_full_bg;

    private boolean clickClose = false;//判断是否是用户点击消失

    public boolean isClickClose() {
        return clickClose;
    }

    public void setClickClose(boolean clickClose) {
        this.clickClose = clickClose;
    }

    public SourceDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public SourceDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected SourceDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init(){
        //根据 “奇瑞8155图层层级定义” 群上的讨论，FRM新增一个图层层级：介于负一屏与普通Dialog之间，专门给USB弹窗使用
        getWindow().setType(2105);
        //getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
//        setCanceledOnTouchOutside(true);
        CarConfigUtil.getDefault().init(getContext());
        isRightRudder = CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_LEFT_RIGHT_RUDDER_TYPE) == 1;
        Log.d(TAG,"isRightRudder:"+isRightRudder);
        if (isRightRudder){
            setContentView(R.layout.dialog_content_right);
        }else {
            setContentView(R.layout.dialog_content);
        }


        iv_close = findViewById(R.id.iv_close);
        tv_title = findViewById(R.id.tv_title);

        rl_type_music = findViewById(R.id.rl_type_music);
        iv_icon_music = findViewById(R.id.iv_icon_music);
        iv_next_music = findViewById(R.id.iv_next_music);
        tv_type_music = findViewById(R.id.tv_type_music);

        rl_type_picture = findViewById(R.id.rl_type_picture);
        iv_icon_picture = findViewById(R.id.iv_icon_picture);
        iv_next_picture = findViewById(R.id.iv_next_picture);
        tv_type_picture = findViewById(R.id.tv_type_picture);

        rl_type_video = findViewById(R.id.rl_type_video);
        iv_icon_video = findViewById(R.id.iv_icon_video);
        iv_next_video = findViewById(R.id.iv_next_video);
        tv_type_video = findViewById(R.id.tv_type_video);

        rl_dialog_full_bg = findViewById(R.id.rl_dialog_full_bg);

        iv_close.setOnClickListener(this);
        rl_type_music.setOnClickListener(this);
        iv_next_music.setOnClickListener(this);
        rl_type_picture.setOnClickListener(this);
        iv_next_picture.setOnClickListener(this);
        rl_type_video.setOnClickListener(this);
        iv_next_video.setOnClickListener(this);
        //T26不需要模糊背景
        //setBlurBg();
    }

    public void setScreenOff(boolean screenOff){

    }

    /**
     * 设置为默认背景
     */
    public void setDefaultBg(){

    }

    /**
     * 设置为模糊背景
     */
    public void setBlurBg(){
        //截图模糊处理用作背景
//        if (rl_dialog_full_bg != null) {//t26没有这个
//            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
//                BitmapDrawable blurDrawable = FastBlur.shotScreenBlurAndDimDrawable(getContext(), 5, 0.3f);
//                new Handler(Looper.getMainLooper()).post(() -> {
//                    if (blurDrawable != null) {
//                        rl_dialog_full_bg.setBackground(blurDrawable);
//                    }
//                });
//            }).run();
//        }
    }

    @Override
    public void show() {
        if (!isShowing()) {
            registerDismissBroadcast();
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
        }
    }

    public final static String ACTION_DIALOG_CLOSE = "com.desaysv.systemui.ACTION_DIALOG_CLOSE";
    private boolean register = false;
    private void registerDismissBroadcast(){
        Intent intent = new Intent(ACTION_DIALOG_CLOSE);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.FLAG_DIALOG_CLOSE_KEY,Constant.FLAG_DIALOG_CLOSE_NAME);
        getContext().sendBroadcast(intent);
        Log.d(TAG,"sendBroadcast ACTION_DIALOG_CLOSE");

        register = true;
        IntentFilter dialogFilter = new IntentFilter();
        dialogFilter.addAction(DialogStateReceiver.ACTION_DIALOG_CLOSE);
        getContext().registerReceiver(DialogStateReceiver.getInstance(), dialogFilter);
    }


    @Override
    public void dismiss() {
        tv_type_music.setText(R.string.dialog_type_music);
        tv_type_picture.setText(R.string.dialog_type_picture);
        tv_type_video.setText(R.string.dialog_type_video);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (register) {
            register = false;
            getContext().unregisterReceiver(DialogStateReceiver.getInstance());
        }
        super.dismiss();
        stopMusicAnimation();
        loadingMusicAnimator = null;
        stopPictureAnimation();
        loadingPictureAnimator = null;
        stopVideoAnimation();
        loadingVideoAnimator = null;
    }

    /**
     * 根据路径显示弹窗，例如 USB1、USB2
     * @param path usb的路径
     */
    public void setTitleWithPath(String path){
        if (path != null){
            Log.d(TAG,"setTitleWithPath: "+path);
            if (path.startsWith(Constant.PATH.PATH_USB1)){
                tv_title.setText(R.string.dialog_title1);
            }else if (path.startsWith(Constant.PATH.PATH_USB2)){
                tv_title.setText(R.string.dialog_title2);
            }
        }
    }

    /**
     * 设置音乐类型扫描状态，更新界面
     * @param state
     */
    public void setMusicScanState(int state){
        Log.d(TAG, "setMusicScanState: " + state);
        if (Constant.Device.STATE_MOUNTED == state) {//挂载，启动扫描动画
            iv_next_music.setImageResource(R.mipmap.dialog_loading);
            startMusicAnimation();
            //在这里加上，用来同步进行动画
            startPictureAnimation();
            startVideoAnimation();
            rl_type_music.setEnabled(false);
            tv_type_music.setEnabled(false);
            iv_icon_music.setEnabled(false);
            iv_next_music.setEnabled(false);
        } else if (Constant.Query.STATE_HAVING_DATA == state || Constant.Query.STATE_HAD_DATA == state) {//查询到有内容，停止动画
            stopMusicAnimation();
            iv_next_music.setImageResource(isRightRudder ? R.drawable.usbpop_next_right : R.drawable.usbpop_next);
            rl_type_music.setEnabled(true);
            tv_type_music.setEnabled(true);
            iv_icon_music.setEnabled(true);
            iv_next_music.setEnabled(true);
            tv_type_music.setText(R.string.dialog_type_music);
        }else if (Constant.Query.STATE_HAVING_NO_DATA == state){//查询中没有内容，继续动画
            iv_next_music.setImageResource(R.mipmap.dialog_loading);
            startMusicAnimation();
            rl_type_music.setEnabled(false);
            tv_type_music.setEnabled(false);
            iv_icon_music.setEnabled(false);
            iv_next_music.setEnabled(false);
        }else {//卸载或者查询完毕没有内容，停止动画
            stopMusicAnimation();
            iv_next_music.setImageResource(isRightRudder ? R.drawable.usbpop_next_right : R.drawable.usbpop_next);
            rl_type_music.setEnabled(false);
            tv_type_music.setEnabled(false);
            iv_icon_music.setEnabled(false);
            iv_next_music.setEnabled(false);
            tv_type_music.setText(R.string.toast_no_music);
        }
    }

    /**
     * 设置图片类型扫描状态，更新界面
     * @param state
     */
    public void setPictureScanState(int state){
        Log.d(TAG, "setPictureScanState: " + state);
        if (Constant.Device.STATE_MOUNTED == state) {//挂载，启动扫描动画
            iv_next_picture.setImageResource(R.mipmap.dialog_loading);
            startPictureAnimation();
            startMusicAnimation();
            startVideoAnimation();
            rl_type_picture.setEnabled(false);
            tv_type_picture.setEnabled(false);
            iv_icon_picture.setEnabled(false);
            iv_next_picture.setEnabled(false);
        } else if (Constant.Query.STATE_HAVING_DATA == state || Constant.Query.STATE_HAD_DATA == state) {//查询到有内容，停止动画
            stopPictureAnimation();
            iv_next_picture.setImageResource(isRightRudder ? R.drawable.usbpop_next_right : R.drawable.usbpop_next);
            rl_type_picture.setEnabled(true);
            tv_type_picture.setEnabled(true);
            iv_icon_picture.setEnabled(true);
            iv_next_picture.setEnabled(true);
            tv_type_picture.setText(R.string.dialog_type_picture);
        }else if (Constant.Query.STATE_HAVING_NO_DATA == state){//查询中没有内容，继续动画
            iv_next_picture.setImageResource(R.mipmap.dialog_loading);
            startPictureAnimation();
            rl_type_picture.setEnabled(false);
            tv_type_picture.setEnabled(false);
            iv_icon_picture.setEnabled(false);
            iv_next_picture.setEnabled(false);
        }else {//卸载或者查询完毕没有内容，停止动画
            stopPictureAnimation();
            iv_next_picture.setImageResource(isRightRudder ? R.drawable.usbpop_next_right : R.drawable.usbpop_next);
            rl_type_picture.setEnabled(false);
            tv_type_picture.setEnabled(false);
            iv_icon_picture.setEnabled(false);
            iv_next_picture.setEnabled(false);
            tv_type_picture.setText(R.string.toast_no_picture);
        }
    }

    /**
     * 设置视频类型扫描状态，更新界面
     * @param state
     */
    public void setVideoScanState(int state){
        Log.d(TAG, "setVideoScanState: " + state);
        if (Constant.Device.STATE_MOUNTED == state) {//挂载，启动扫描动画
            iv_next_video.setImageResource(R.mipmap.dialog_loading);
            startVideoAnimation();
            startPictureAnimation();
            startMusicAnimation();
            rl_type_video.setEnabled(false);
            tv_type_video.setEnabled(false);
            iv_icon_video.setEnabled(false);
            iv_next_video.setEnabled(false);
        } else if (Constant.Query.STATE_HAVING_DATA == state || Constant.Query.STATE_HAD_DATA == state) {//查询到有内容，停止动画
            stopVideoAnimation();
            iv_next_video.setImageResource(isRightRudder ? R.drawable.usbpop_next_right : R.drawable.usbpop_next);
            rl_type_video.setEnabled(true);
            tv_type_video.setEnabled(true);
            iv_icon_video.setEnabled(true);
            iv_next_video.setEnabled(true);
            tv_type_video.setText(R.string.dialog_type_video);
        }else if (Constant.Query.STATE_HAVING_NO_DATA == state){//查询中没有内容，继续动画
            iv_next_video.setImageResource(R.mipmap.dialog_loading);
            startVideoAnimation();
            rl_type_video.setEnabled(false);
            tv_type_video.setEnabled(false);
            iv_icon_video.setEnabled(false);
            iv_next_video.setEnabled(false);
        }else {//卸载或者查询完毕没有内容，停止动画
            stopVideoAnimation();
            iv_next_video.setImageResource(isRightRudder ? R.drawable.usbpop_next_right : R.drawable.usbpop_next);
            rl_type_video.setEnabled(false);
            tv_type_video.setEnabled(false);
            iv_icon_video.setEnabled(false);
            iv_next_video.setEnabled(false);
            tv_type_video.setText(R.string.toast_no_video);
        }
    }


    /**
     * 启动Music扫描的动画
     */
    private void startMusicAnimation(){
        if (loadingMusicAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingMusicAnimator = ObjectAnimator.ofFloat(iv_next_music, "rotation", 0f, 360f);
            loadingMusicAnimator.setDuration(ANIMATION_DURATION);
            loadingMusicAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingMusicAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingMusicAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if(!loadingMusicAnimator.isStarted()) {
            loadingMusicAnimator.start();
        }
    }

    /**
     * 停止Music扫描的动画
     */
    private void stopMusicAnimation(){
        if (loadingMusicAnimator != null && loadingMusicAnimator.isRunning()){
            Log.d(TAG,"stopMusicAnimation");
            loadingMusicAnimator.end();
        }
    }

    /**
     * 启动Picture扫描的动画
     */
    private void startPictureAnimation(){
        if (loadingPictureAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingPictureAnimator = ObjectAnimator.ofFloat(iv_next_picture, "rotation", 0f, 360f);
            loadingPictureAnimator.setDuration(ANIMATION_DURATION);
            loadingPictureAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingPictureAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingPictureAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (!loadingPictureAnimator.isStarted()) {
            loadingPictureAnimator.start();
        }
    }

    /**
     * 停止Picture扫描的动画
     */
    private void stopPictureAnimation(){
        if (loadingPictureAnimator != null &&loadingPictureAnimator.isRunning()){
            Log.d(TAG,"stopPictureAnimation");
            loadingPictureAnimator.end();
        }
    }

    /**
     * 启动Video扫描的动画
     */
    private void startVideoAnimation(){
        if (loadingVideoAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingVideoAnimator = ObjectAnimator.ofFloat(iv_next_video, "rotation", 0f, 360f);
            loadingVideoAnimator.setDuration(ANIMATION_DURATION);
            loadingVideoAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingVideoAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingVideoAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (!loadingVideoAnimator.isStarted()) {
            loadingVideoAnimator.start();
        }
    }

    /**
     * 停止Video扫描的动画
     */
    private void stopVideoAnimation(){
        if (loadingVideoAnimator != null && loadingVideoAnimator.isRunning()){
            Log.d(TAG,"stopVideoAnimation");
            loadingVideoAnimator.end();
        }
    }

    private boolean needDismissByPoint = true;
    private float downX;
    private float downY;
    private static final float MIN_MOVE_DISTANCE = 50;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;
        float dialogWidth = resources.getDimensionPixelSize(R.dimen.dialog_bg_width);
        float dialogHeight = resources.getDimensionPixelSize(R.dimen.dialog_bg_height);
        Log.d(TAG, "onTouchEvent: screenWidth: " + screenWidth +" height: " +  screenHeight
                + " dialogWidth: " + dialogWidth +" dialogHeight: " + dialogHeight);
        Log.d(TAG, "onTouchEvent,action:"+event.getAction());
        if (event.getAction() == MotionEvent.ACTION_UP && needDismissByPoint){
        //点击弹框范围外的区域，弹窗消失
        if ((((screenHeight/2 - dialogHeight/2) < event.getY()) && (event.getY() < (screenHeight/2 + dialogHeight/2))
                && ((screenWidth/2 - dialogWidth/2 < event.getX())) && (event.getX() < (screenWidth/2 + dialogWidth/2))) != true){
            Log.d(TAG, "onTouchEvent: dialogDismiss");
            clickClose = true;
            this.dismiss();
        }
        }else if (event.getAction() == MotionEvent.ACTION_MOVE){
            if ((event.getX() - downX) > MIN_MOVE_DISTANCE || (event.getY() - downY) > MIN_MOVE_DISTANCE) {
                needDismissByPoint = false;
            }
        }else {
            downX = event.getX();
            downY = event.getY();
            needDismissByPoint = true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        if (id == R.id.iv_close){
            stopMusicAnimation();
            stopPictureAnimation();
            stopVideoAnimation();
        }else if (id == R.id.rl_type_music || id == R.id.iv_next_music){
            //go to music
            gotoMusic();
        }else if (id == R.id.rl_type_picture || id == R.id.iv_next_picture){
            //go to picture
            gotoPicture();
        }else if (id == R.id.rl_type_video || id == R.id.iv_next_video){
            //go to video
            gotoVideo();
        }
        clickClose = true;
        dismiss();
    }


    /**
     * 跳转到Music应用
     * 需要根据实际应用和对应参数做修改
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void gotoMusic(){
        Log.d(TAG,"gotoMusic");
        clickClose = true;
//        Intent intent = new Intent();
//        intent.setAction(Constant.Lion.ACTION_MUSIC);
//        intent.setPackage(Constant.Lion.PKG_MUSIC);
//        intent.putExtra(Constant.Lion.NAVIGATION_KEY,Constant.Lion.NAVIGATION_USB_MUSIC);
//        getContext().startForegroundService(intent);

        Intent intent = new Intent();
        intent.setClassName(Constant.DeSay.PKG_MUSIC, Constant.DeSay.PKG_MUSIC_CLS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.DeSay.SOURCE_KEY, Constant.DeSay.USB0_MUSIC_SOURCE);
        intent.putExtra(Constant.DeSay.NAVIGATION_KEY, Constant.DeSay.LIST_VIEW);
        getContext().startActivity(intent);
    }

    /**
     * 跳转到Picture应用
     * 需要根据实际应用和对应参数做修改
     */
    private void gotoPicture(){
        Log.d(TAG,"gotoPicture");
        clickClose = true;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.desaysv.usbpicture","com.desaysv.usbpicture.ui.MainActivity"));
        intent.putExtra(SourceDialogUtil.AUTO_TAB,1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    /**
     * 跳转到Video应用
     * 需要根据实际应用和对应参数做修改
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void gotoVideo(){
        Log.d(TAG,"gotoVideo");
        clickClose = true;
//        Intent intent = new Intent();
//        intent.setAction(Constant.Lion.ACTION_VIDEO);
//        intent.setPackage(Constant.Lion.PKG_VIDEO);
//        intent.putExtra(Constant.Lion.NAVIGATION_KEY,Constant.Lion.NAVIGATION_USB_VIDEO);
//        getContext().startForegroundService(intent);

        Intent intent = new Intent();
        intent.setClassName(Constant.DeSay.PKG_VIDEO, Constant.DeSay.PKG_VIDEO_CLS);
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }


    /**
     * 返回是否用户已经关掉弹窗，是的话，不再弹出
     * @return
     */
    public boolean getClickClose(){
        return clickClose;
    }

}
