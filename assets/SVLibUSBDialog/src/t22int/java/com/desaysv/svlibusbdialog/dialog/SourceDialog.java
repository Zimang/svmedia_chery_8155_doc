package com.desaysv.svlibusbdialog.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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

import com.desaysv.svlibusbdialog.R;
import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.utils.FastBlur;

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

    //定义动画
    private ObjectAnimator loadingMusicAnimator;
    private ObjectAnimator loadingPictureAnimator;
    private ObjectAnimator loadingVideoAnimator;
    private static final int ANIMATION_DURATION = 1000;

    private RelativeLayout rl_dialog_full_bg;

    private boolean clickClose = false;//判断是否是用户点击消失

    private boolean isScreenOff = false;

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

    private String themeStr;
    public void init(){
        //判断当前主题，动态替换蒙层
        themeStr = Settings.System.getString(getContext().getContentResolver(), "com.desaysv.setting.theme.mode");
        Log.i(TAG,"themeStr:"+themeStr);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        setCanceledOnTouchOutside(true);
        if ("overlay2".equals(themeStr)){
            setContentView(R.layout.dialog_content2);
        } else if("overlay3".equals(themeStr)) {
            setContentView(R.layout.dialog_content3);
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
    }

    public void setScreenOff(boolean screenOff){
        isScreenOff = screenOff;
    }

    /**
     * 设置为模糊背景
     */
    public void setBlurBg(){
        //截图模糊处理用作背景
        if (rl_dialog_full_bg != null) {//t26没有这个
            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
                BitmapDrawable blurDrawable = FastBlur.shotScreenBlurAndDimDrawable(getContext(), 5, 0.3f);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (blurDrawable != null && !isScreenOff) {
                        rl_dialog_full_bg.setBackground(blurDrawable);
                    }else {
                        Log.i(TAG,"setBlurBg ScreenOff");
                        rl_dialog_full_bg.setBackground(null);
                    }
                });
            }).run();
        }
    }

    /**
     * 有些情况下，获取到的背景为黑色，此时需要重置为默认背景
     */
    public void setDefaultBg() {
        if (rl_dialog_full_bg != null) {//t26没有这个
            ((Runnable) () -> { //在子线程处理完毕再回到主线程设置
                new Handler(Looper.getMainLooper()).post(() -> {
                    rl_dialog_full_bg.setBackground(new BitmapDrawable(getContext().getResources(),
                            FastBlur.shotScreenBlurAndDimBitmap(5, 0.3f, BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.default_bg))));
                });
            }).run();
        }
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
        }
    }

    @Override
    public void dismiss() {
        tv_type_music.setText(R.string.dialog_type_music);
        tv_type_picture.setText(R.string.dialog_type_picture);
        tv_type_video.setText(R.string.dialog_type_video);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
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
            Log.i(TAG,"setTitleWithPath: "+path);
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
        Log.i(TAG, "setMusicScanState: " + state);
        if (Constant.Device.STATE_MOUNTED == state) {//挂载，启动扫描动画
            iv_next_music.setImageResource(R.mipmap.dialog_loading);
            startMusicAnimation();
            rl_type_music.setEnabled(false);
            tv_type_music.setEnabled(false);
            iv_icon_music.setEnabled(false);
            iv_next_music.setEnabled(false);
        } else if (Constant.Query.STATE_HAVING_DATA == state || Constant.Query.STATE_HAD_DATA == state) {//查询到有内容，停止动画
            stopMusicAnimation();
            if ("overlay2".equals(themeStr)){
                iv_next_music.setImageResource(R.drawable.usbpop_next2);
            }
            else if("overlay3".equals(themeStr)){
                iv_next_music.setImageResource(R.drawable.usbpop_next3);
            }
            else {
                iv_next_music.setImageResource(R.drawable.usbpop_next);
            }
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
            if ("overlay2".equals(themeStr)){
                iv_next_music.setImageResource(R.drawable.usbpop_next2);
            }
            else if("overlay3".equals(themeStr)){
                iv_next_music.setImageResource(R.drawable.usbpop_next3);
            }
            else {
                iv_next_music.setImageResource(R.drawable.usbpop_next);
            }
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
        Log.i(TAG, "setPictureScanState: " + state);
        if (Constant.Device.STATE_MOUNTED == state) {//挂载，启动扫描动画
            iv_next_picture.setImageResource(R.mipmap.dialog_loading);
            startPictureAnimation();
            rl_type_picture.setEnabled(false);
            tv_type_picture.setEnabled(false);
            iv_icon_picture.setEnabled(false);
            iv_next_picture.setEnabled(false);
        } else if (Constant.Query.STATE_HAVING_DATA == state || Constant.Query.STATE_HAD_DATA == state) {//查询到有内容，停止动画
            stopPictureAnimation();
            if ("overlay2".equals(themeStr)){
                iv_next_picture.setImageResource(R.drawable.usbpop_next2);
            }
            else if("overlay3".equals(themeStr)){
                iv_next_picture.setImageResource(R.drawable.usbpop_next3);
            }
            else {
                iv_next_picture.setImageResource(R.drawable.usbpop_next);
            }
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
            if ("overlay2".equals(themeStr)){
                iv_next_picture.setImageResource(R.drawable.usbpop_next2);
            }
            else if("overlay3".equals(themeStr)){
                iv_next_picture.setImageResource(R.drawable.usbpop_next3);
            }
            else {
                iv_next_picture.setImageResource(R.drawable.usbpop_next);
            }
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
        Log.i(TAG, "setVideoScanState: " + state);
        if (Constant.Device.STATE_MOUNTED == state) {//挂载，启动扫描动画
            iv_next_video.setImageResource(R.mipmap.dialog_loading);
            startVideoAnimation();
            rl_type_video.setEnabled(false);
            tv_type_video.setEnabled(false);
            iv_icon_video.setEnabled(false);
            iv_next_video.setEnabled(false);
        } else if (Constant.Query.STATE_HAVING_DATA == state || Constant.Query.STATE_HAD_DATA == state) {//查询到有内容，停止动画
            stopVideoAnimation();
            if ("overlay2".equals(themeStr)){
                iv_next_video.setImageResource(R.drawable.usbpop_next2);
            }
            else if("overlay3".equals(themeStr)){
                iv_next_video.setImageResource(R.drawable.usbpop_next3);
            }
            else {
                iv_next_video.setImageResource(R.drawable.usbpop_next);
            }
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
            if ("overlay2".equals(themeStr)){
                iv_next_video.setImageResource(R.drawable.usbpop_next2);
            }
            else if("overlay3".equals(themeStr)){
                iv_next_video.setImageResource(R.drawable.usbpop_next3);
            }
            else {
                iv_next_video.setImageResource(R.drawable.usbpop_next);
            }
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
        Log.i(TAG,"startMusicAnimation begin");
        if(!loadingMusicAnimator.isStarted()) {
            Log.i(TAG,"startMusicAnimation end");
            loadingMusicAnimator.start();
        }
    }

    /**
     * 停止Music扫描的动画
     */
    private void stopMusicAnimation(){
        Log.i(TAG,"stopMusicAnimation begin");
        if (loadingMusicAnimator != null && loadingMusicAnimator.isRunning()){
            Log.i(TAG,"stopMusicAnimation end");
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
        Log.i(TAG,"startPictureAnimation begin");
        if (!loadingPictureAnimator.isStarted()) {
            Log.i(TAG,"startPictureAnimation end");
            loadingPictureAnimator.start();
        }
    }

    /**
     * 停止Picture扫描的动画
     */
    private void stopPictureAnimation(){
        Log.i(TAG,"stopPictureAnimation begin");
        if (loadingPictureAnimator != null &&loadingPictureAnimator.isRunning()){
            Log.i(TAG,"stopPictureAnimation end");
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
        Log.i(TAG,"startVideoAnimation begin");
        if (!loadingVideoAnimator.isStarted()) {
            Log.i(TAG,"startVideoAnimation end");
            loadingVideoAnimator.start();
        }
    }

    /**
     * 停止Video扫描的动画
     */
    private void stopVideoAnimation(){
        Log.i(TAG,"stopVideoAnimation begin");
        if (loadingVideoAnimator != null && loadingVideoAnimator.isRunning()){
            Log.i(TAG,"stopVideoAnimation end");
            loadingVideoAnimator.end();
        }
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;
        float dialogWidth = resources.getDimensionPixelSize(R.dimen.dialog_bg_width);
        float dialogHeight = resources.getDimensionPixelSize(R.dimen.dialog_bg_height);
        Log.i(TAG, "onTouchEvent: screenWidth: " + screenWidth +" height: " +  screenHeight
                + " dialogWidth: " + dialogWidth +" dialogHeight: " + dialogHeight);
        //点击弹框范围外的区域，弹窗消失
        if ((((screenHeight/2 - dialogHeight/2) < event.getY()) && (event.getY() < (screenHeight/2 + dialogHeight/2))
                && ((screenWidth/2 - dialogWidth/2 < event.getX())) && (event.getX() < (screenWidth/2 + dialogWidth/2))) != true){
            Log.i(TAG, "onTouchEvent: dialogDismiss");
            clickClose = true;
            this.dismiss();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        if (id == R.id.iv_close){
            clickClose = true;
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
        dismiss();
    }


    /**
     * 跳转到Music应用
     * 需要根据实际应用和对应参数做修改
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void gotoMusic(){
        Log.i(TAG,"gotoMusic");
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
        Log.i(TAG,"gotoPicture");
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
        Log.i(TAG,"gotoVideo");
        clickClose = true;
//        Intent intent = new Intent();
//        intent.setAction(Constant.Lion.ACTION_VIDEO);
//        intent.setPackage(Constant.Lion.PKG_VIDEO);
//        intent.putExtra(Constant.Lion.NAVIGATION_KEY,Constant.Lion.NAVIGATION_USB_VIDEO);
//        getContext().startForegroundService(intent);

        Intent intent = new Intent();
        intent.setClassName(Constant.DeSay.PKG_VIDEO, Constant.DeSay.PKG_VIDEO_CLS);
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
