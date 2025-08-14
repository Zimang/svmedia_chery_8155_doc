package com.desaysv.usbpicture.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.rvc.VDEventRvc;
import com.desaysv.ivi.vdb.event.id.rvc.VDValueRvc;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.svlibfileoperation.FileOperationManager;
import com.desaysv.svlibfileoperation.iinterface.IFileOperationListener;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.manager.AlbumListManager;
import com.desaysv.svlibpicturebean.manager.PictureListManager;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.adapter.PhotoPagerAdapter;
import com.desaysv.usbpicture.bean.MessageBean;
import com.desaysv.usbpicture.constant.Constant;
import com.desaysv.usbpicture.photo.photoview.IPhotoViewActionListener;
import com.desaysv.usbpicture.trigger.PictureVRControl;
import com.desaysv.usbpicture.trigger.interfaces.IVRResponseOperator;
import com.desaysv.usbpicture.view.CanScrollViewPager;
import com.desaysv.usbpicture.view.CircularProgressView;
import com.desaysv.usbpicture.view.ViewPagerScroller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by LZM on 2019-9-18
 * Comment 图片浏览界面的base activity
 */
public class AlbumActivity extends Activity implements View.OnClickListener, IPhotoViewActionListener, IVRResponseOperator, IFileOperationListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final String INTENT_POSITION = "INTENT_POSITION";

    private CanScrollViewPager vpPicture;
    private PhotoPagerAdapter mPicturePagerAdapter;

    private ImageView btnBack;
    private ImageView btnFlip;
    private ImageView btnSlide;
    private ImageView btnPre;
    private ImageView btnNext;
    private ImageView stop_slide;
    private ImageView btnExport;
    private ImageView btnDelete;
    private int currentPosition;

    private TextView tv_pic_name;

    private View v_top_mask;
    private View v_bottom_mask;
    private View v_full_mask;//这个是用来让顶部操作按钮可见的
    private View v_placeholder;//占位符
    private View v_topPlaceholder;
    private ImageView iv_dockMask;

    private RelativeLayout rl_full_screen_bg;

    private RelativeLayout rl_top_control;
    private RelativeLayout rl_bottom_control;

    //幻灯片模式
    private boolean isFlipMode;
    private boolean isFlipModePlaying = false;

    //全屏模式下显示控制按钮
    private boolean showFullScreenControl = false;

    //透明度变化速度的因子
    private static final float SCROLL_ALPHA_PARAMETER = 1.5f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        initView();
        initViewListener();
        initData();
        FileOperationManager.getInstance().init(this);
        FileOperationManager.getInstance().registerOperationListener(this,this.getClass().getSimpleName());//允许后台操作

        transparent();
    }


    /**
     * 设置透明状态栏和导航栏
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void transparent(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }


    public int getLayoutResID() {
        return R.layout.activity_album;
    }


    public void initView() {
        setContentView(getLayoutResID());
        vpPicture = findViewById(R.id.vpPicture);
        btnBack = findViewById(R.id.btnBack);
        btnFlip = findViewById(R.id.btnFlip);
        btnSlide = findViewById(R.id.btnSlide);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);
        stop_slide = findViewById(R.id.stop_slide);
        myHandler = new MyHandler(this);
        rl_full_screen_bg = findViewById(R.id.rl_full_screen_bg);
        tv_pic_name = findViewById(R.id.tv_pic_name);
//        rl_mask = findViewById(R.id.rl_mask);
        rl_top_control = findViewById(R.id.rl_top_control);
        rl_bottom_control = findViewById(R.id.rl_bottom_control);
        v_top_mask = findViewById(R.id.v_top_mask);
        v_bottom_mask = findViewById(R.id.v_bottom_mask);
        btnExport = findViewById(R.id.btnExport);
        btnDelete = findViewById(R.id.btnDelete);
        v_full_mask = findViewById(R.id.v_full_mask);
        v_placeholder = findViewById(R.id.v_placeholder);
        v_topPlaceholder = findViewById(R.id.v_topPlaceholder);
        iv_dockMask = findViewById(R.id.iv_dockMask);
    }


    public void initData() {
        updatePicturePagerAdapter();
    }

    public List<FileMessage> getCurrentShowPictures(){
        return AlbumListManager.getInstance().getAlbumList();
    }

    public List<FileMessage> getCurrentOperationPicture(){
        List<FileMessage> operationList = new ArrayList<>();
        operationList.add(AlbumListManager.getInstance().getAlbumList().get(currentPosition % getCurrentShowPictures().size()));
        return operationList;
    }

    public void initViewListener() {
        btnBack.setOnClickListener(this);
        btnFlip.setOnClickListener(this);
        btnSlide.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        stop_slide.setOnClickListener(this);
        vpPicture.addOnPageChangeListener(mOnPageChangeListener);
        btnExport.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(null);
        Log.d(TAG,"onNewIntent");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mPicturePagerAdapter.updateScreenParams(getWindowManager().getMaximumWindowMetrics().getBounds().width(),getWindowManager().getMaximumWindowMetrics().getBounds().height());
        Intent intent = getIntent();
        if (intent != null) {
            int position = intent.getIntExtra(INTENT_POSITION, 0);
            currentPosition = position + 10000 * getCurrentShowPictures().size();//把位置后移到第1w个循环，这样可以实现假的列表无限循环(最多1w次循环)。因为动效的原因，无法使用T18P的算法循环处理方式。
            openPhoto(currentPosition);
        }
        PictureListManager.getInstance().attachUSB1Observer(usb1ListObserver);
        myHandler.removeCallbacks(runnableFullScreen);
        myHandler.postDelayed(runnableFullScreen, 5000);

        // 被动获取倒车状态回调（callback）
//        VDBus.getDefault().addSubscribe(VDEventRvc.RVC_STATUS); // 订阅RVC倒车状态事件,
        VDBus.getDefault().addSubscribe(VDEventRvc.VIDEO_SIGNAL); // 订阅倒车摄像头信号事件
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);

        //注册语义操作的实现者
        PictureVRControl.getInstance().registerVRResponseOperator(this);
    }

    private  VDNotifyListener mVDNotifyListener = new VDNotifyListener() {
        public void onVDNotify(VDEvent event, int threadType) {
            if (threadType == VDThreadType.MAIN_THREAD) { // 主线程回调处理
                Log.i(TAG,"onVDNotify,threadType: "+threadType + ", ID: " + event.getId());
                switch (event.getId()) {
                    case VDEventRvc.VIDEO_SIGNAL:
                        int status = event.getPayload().getInt(VDKey.STATUS);
                        Log.d(TAG,"onVDNotify,status: "+status);
                        updateSlideShowWithRVC(status);
                        break;
                }
            }
        }
    };


    private void openPhoto(int position) {
        Log.d(TAG, "openPhoto: position = " + position);
        vpPicture.resetPhoto();//切换图片时，先把上一张图片恢复
        vpPicture.setCurrentItem(currentPosition, true);
        updateBtnWithPosition();
    }


    private void updatePicturePagerAdapter() {
        Log.d(TAG, "updatePicturePagerAdapter: mPicturePagerAdapter = " + mPicturePagerAdapter);
        if (mPicturePagerAdapter == null) {
            mPicturePagerAdapter = new PhotoPagerAdapter(this,this);
            mPicturePagerAdapter.setPhotoList(vpPicture, getCurrentShowPictures());
            vpPicture.setAdapter(mPicturePagerAdapter);
            ViewPagerScroller viewPagerScroller = new ViewPagerScroller(this);
            viewPagerScroller.intiViewPager(vpPicture);
            vpPicture.setPageTransformer(true, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                        Log.d(TAG,"transformPage,position：" + position);
                        if (position < -1){//向左完全移出屏幕
                            page.setAlpha(0);
                        }else if (position <= 0){//向左移出屏幕中
                            page.setAlpha(1+position * SCROLL_ALPHA_PARAMETER);
                        }else if (position <= 1){//向右移出屏幕中
                            page.setAlpha(1-position * SCROLL_ALPHA_PARAMETER);
                        }else {//向右完全移出屏幕
                            page.setAlpha(1);
                        }
                }
            });
        } else {
            mPicturePagerAdapter.setPhotoList(vpPicture, getCurrentShowPictures());
            mPicturePagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnBack){
            if (isFlipMode) {
                exitSlide();
                exitSlideMode();
            }else if (showFullScreenControl){
                showFullScreenControl = false;
                exitFullScreen();
            }else if (isFullScreen){
                exitFullScreen();
            }else {
                finish();
            }
        }else if (v.getId() == R.id.btnFlip){
            mPicturePagerAdapter.getCurrentPhotoView().flipPhoto();
        }else if (v.getId() == R.id.btnSlide){
            startSlide();
        }else if (v.getId() == R.id.btnPre){
            currentPosition--;
            openPhoto(currentPosition);
        }else if (v.getId() == R.id.btnNext){
            currentPosition++;
            openPhoto(currentPosition);
        }else if (v.getId() == R.id.stop_slide){
            if (isFlipModePlaying) {
                stopSlide();
            }else {
                startSlide();
            }
            updateViewWithSlideMode(true);
        }else if (v.getId() == R.id.btnExport){
            FileOperationManager.getInstance().startExportFiles(this,getCurrentOperationPicture());
        }else if (v.getId() == R.id.btnDelete){
            FileOperationManager.getInstance().startDeleteFiles(this,getCurrentOperationPicture());
        }
        handleClickWithBtn();
    }

    private void stopSlide() {
        Log.d(TAG, "stopSlide");
        isFlipModePlaying = false;
        myHandler.removeMessages(MSG_START_SLIDE);
    }

    private void startSlide() {
        Log.d(TAG, "startSlide");
        startSlideRunnable();
        enterSlideMode();
    }


    private boolean isFullScreen;
    private void startSlideRunnable(){
        isFlipMode = true;
        isFlipModePlaying = true;
        myHandler.removeMessages(MSG_START_SLIDE);
        myHandler.sendEmptyMessageDelayed(MSG_START_SLIDE,5000);
    }
    public void changePic(){
        currentPosition++;
        openPhoto(currentPosition);
        mPicturePagerAdapter.updateNameVisibility(false);
        startSlideRunnable();
    }

    private static final int MSG_START_SLIDE=10;
    private static final int MSG_UPDATE_LIST=11;
    private MyHandler myHandler;

    @Override
    public void onClick(boolean isClick) {
        Log.d(TAG,"photoview click, isClick:" + isClick);
        handleClickEvent(isClick);
    }

    @Override
    public void onScaleState(int scaleType, boolean value) {
        if (!isFlipMode) {//幻灯片播放状态下，不需要处理
            myHandler.removeCallbacks(runnableFullScreen);
            myHandler.postDelayed(runnableFullScreen, 5000);
        }
    }

    @Override
    public void onPrimaryItemChanged(int position) {
        Log.d(TAG,"onPrimaryItemChanged,position:"+position);

    }

    @Override
    public void onExportState(int state, int current, int total) {
        if (state == FileOperationManager.OPERATION_STATE_SUCCESS || state == FileOperationManager.OPERATION_STATE_CANCEL){

        }else if (state == FileOperationManager.OPERATION_STATE_ERROR_SPACE){//空间不足

        }else if (state == FileOperationManager.OPERATION_STATE_ERROR_IO){//U盘中断

        }
    }

    @Override
    public void onDeleteState(int state, int current, int total) {
        if (state == FileOperationManager.OPERATION_STATE_SUCCESS || state == FileOperationManager.OPERATION_STATE_CANCEL){
            myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
        }
    }

    private static class MyHandler extends Handler{
        private WeakReference<AlbumActivity> weakReference;
        public MyHandler(AlbumActivity basePictureActivity) {
            weakReference = new WeakReference<>(basePictureActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_START_SLIDE:
                    weakReference.get().changePic();
                    break;
                case MSG_UPDATE_LIST:
                    weakReference.get().updatePicturePagerAdapter();
                    break;
            }

        }
    };

    private void exitSlide(){
        if (isFlipMode){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isFlipMode = false;
                    myHandler.removeMessages(MSG_START_SLIDE);
                }
            });
        }
    }


    /**
     *hideNavigationBar
     * 隐藏导航栏
     */
    protected void hideNavigationBar(boolean hide){
        Log.d(TAG, "hideNavigationBar: ");
        View decorView = this.getWindow().getDecorView();
        if (hide) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    public void handleClickEvent(boolean ClickOrScroll){
        Log.d(TAG,"handleClickEvent");
        myHandler.removeCallbacks(runnableFullScreen);
        myHandler.postDelayed(runnableFullScreen, 5000);
        if (ClickOrScroll && !isFlipMode) {//如果是图片浏览模式，不是幻灯片模式，也是需要隐藏
            if (isFullScreen) {
                exitSlideMode();
            } else {
                enterSlideMode();
            }

        }else if (ClickOrScroll && isFlipMode){
            updateViewWithSlideMode(true);
        }

    }


    /**
     * 全屏模式点击返回
     */
    private void exitFullScreen(){
        isFullScreen = false;
        hideNavigationBar(false);
        setFullScreenBg(false);
        tv_pic_name.setVisibility(View.VISIBLE);
        mPicturePagerAdapter.updateNameVisibility(true);
    }


    public void handleClickWithBtn(){
            myHandler.removeCallbacks(runnableFullScreen);
            myHandler.postDelayed(runnableFullScreen, 5000);
    }

    private Runnable runnableFullScreen = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"runnableFullScreen000");
            if (!isFullScreen) {
                Log.d(TAG,"runnableFullScreen");
                enterSlideMode();
            }else if (isFlipMode){//幻灯片模式下自动隐藏
                updateViewWithSlideMode(false);
            }
        }
    };


    private void enterSlideMode() {
        Log.d(TAG, "enterSlideMode");
        if (isFlipMode) {
            btnPre.setSelected(true);
            btnNext.setSelected(true);
        }
        tv_pic_name.setVisibility(View.GONE);
        isFullScreen = true;
        hideNavigationBar(true);
        mPicturePagerAdapter.updateNameVisibility(false);
        setFullScreenBg(true);
    }

    private void exitSlideMode() {
        Log.d(TAG, "exitSlideMode");
        updateViewWithSlideMode(false);
        btnPre.setSelected(false);
        btnNext.setSelected(false);
        updateBtnWithPosition();
        tv_pic_name.setVisibility(View.VISIBLE);
        isFullScreen = false;
        hideNavigationBar(false);
        mPicturePagerAdapter.updateNameVisibility(true);
        setFullScreenBg(false);
    }

    private void updateBtnWithPosition(){
        if (isFlipMode){
            return;
        }
        //UE更新为循环播放，不隐藏按键
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        PictureListManager.getInstance().detachUSB1Observer(usb1ListObserver);
        setIntent(null);
        exitSlide();
        myHandler.removeCallbacks(runnableFullScreen);
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);
        //注销语义操作的实现者
        PictureVRControl.getInstance().unregisterVRResponseOperator(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        VDBus.getDefault().release();
        FileOperationManager.getInstance().unregisterOperationListener(this.getClass().getSimpleName());
    }


    protected boolean checkUSBStatus(){// 如果有多个USB，则需要复写,默认使用USB1
        return DeviceStatusBean.getInstance().isUSB1Connect();
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, "onPageScrolled position : " + position);

        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected");
            vpPicture.resetPhoto();
            currentPosition = position;
            tv_pic_name.setText(getCurrentShowPictures().get(position % getCurrentShowPictures().size()).getFileName());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state){
                case ViewPager.SCROLL_STATE_IDLE:
                    //无动作、初始状态
                    Log.i(TAG,"---->onPageScrollStateChanged无动作");
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    //点击、滑屏
                    Log.i(TAG,"---->onPageScrollStateChanged点击、滑屏");
                    exitSlide();
                    exitSlideMode();
                    myHandler.removeCallbacks(runnableFullScreen);
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    //释放
                    Log.i(TAG,"---->onPageScrollStateChanged释放");
                    myHandler.postDelayed(runnableFullScreen, 5000);
                    break;
            }

        }
    };

    /**
     * 幻灯片状态下的UI更新
     * @param show
     */
    private void updateViewWithSlideMode(boolean show){
        if (show){
            updateBtnWithPosition();
            tv_pic_name.setVisibility(View.VISIBLE);
            btnBack.setImageAlpha(255);
            btnPre.setImageAlpha(255);
            btnNext.setImageAlpha(255);
            stop_slide.setImageAlpha(255);
            stop_slide.setImageResource(isFlipModePlaying ? R.drawable.drawable_stop : R.drawable.drawable_play);
            handleClickWithBtn();
        }else {
            if (isFlipMode) {
                btnBack.setImageAlpha(0);
                btnPre.setImageAlpha(0);
                btnNext.setImageAlpha(0);
            }
            stop_slide.setImageAlpha(0);
            tv_pic_name.setVisibility(View.GONE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setFullScreenBg(boolean isFullScreen){
        Log.d(TAG,"setFullScreenBg:"+isFullScreen);
        v_full_mask.setVisibility(isFullScreen ? View.GONE:View.VISIBLE);
        v_placeholder.setVisibility(isFullScreen ? View.GONE:View.VISIBLE);
        v_topPlaceholder.setVisibility(isFullScreen ? View.GONE:View.VISIBLE);
        iv_dockMask.setVisibility(isFullScreen ? View.GONE:View.VISIBLE);
        if (isFullScreen) {
            tv_pic_name.setSelected(true);
            btnBack.setSelected(true);
            btnPre.setSelected(true);
            btnNext.setSelected(true);
            btnFlip.setSelected(true);
            btnSlide.setSelected(true);
            btnExport.setSelected(true);
            btnDelete.setSelected(true);

            btnBack.setImageAlpha(0);
            btnPre.setImageAlpha(0);
            btnNext.setImageAlpha(0);
            btnFlip.setImageAlpha(0);
            btnSlide.setImageAlpha(0);
            stop_slide.setImageAlpha(0);
            btnExport.setImageAlpha(0);
            btnDelete.setImageAlpha(0);

            if (isFlipMode){
                v_top_mask.setVisibility(View.VISIBLE);
                v_bottom_mask.setVisibility(View.VISIBLE);
                tv_pic_name.setVisibility(View.GONE);
                stop_slide.setVisibility(View.VISIBLE);
                v_top_mask.bringToFront();
                v_bottom_mask.bringToFront();
            }else {
                v_top_mask.setVisibility(View.GONE);
                v_bottom_mask.setVisibility(View.GONE);
            }
            rl_full_screen_bg.setBackground(getResources().getDrawable(R.mipmap.picture_vague_bg));
        }else {
            rl_full_screen_bg.setBackgroundColor(Color.TRANSPARENT);
            tv_pic_name.setSelected(false);
            btnBack.setSelected(false);
            btnPre.setSelected(false);
            btnNext.setSelected(false);
            btnFlip.setSelected(false);
            btnSlide.setSelected(false);
            btnExport.setSelected(false);
            btnDelete.setSelected(false);

            btnBack.setImageAlpha(255);
            btnPre.setImageAlpha(255);
            btnNext.setImageAlpha(255);
            btnFlip.setImageAlpha(255);
            btnSlide.setImageAlpha(255);
            stop_slide.setImageAlpha(0);
            btnExport.setImageAlpha(255);
            btnDelete.setImageAlpha(255);

            v_top_mask.setVisibility(View.GONE);
            v_bottom_mask.setVisibility(View.GONE);
        }
    }

    /**
     * 倒车影像和接听电话时暂停幻灯片
     * @param status
     */
    public void updateSlideShowWithRVC(int status){
        if (VDValueRvc.RvcStatus.RVC == status){
            if (isFlipModePlaying) {
                stopSlide();
            }
            updateViewWithSlideMode(true);
        }else {
            startSlide();
        }
    }


    private Observer usb1ListObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG,"usb1ListObserver onUpdate");
            if (checkUSBStatus()) {
                myHandler.removeMessages(MSG_UPDATE_LIST);
                myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
        }
    };

    //以下为响应语义操作的实现
    @Override
    public void playOrPause() {
        Log.d(TAG,"playOrPause");
        stop_slide.performClick();
    }

    @Override
    public void play() {
        Log.d(TAG,"play");
        if (isFlipMode){//当前是幻灯片模式
            if (!isFlipModePlaying) {//当前处于暂停状态
                stop_slide.performClick();
            }
        }else {
            btnSlide.performClick();
        }
    }

    @Override
    public void pause() {
        Log.d(TAG,"Pause");
        if (isFlipMode && isFlipModePlaying){//当前是幻灯片模式,且处于播放状态
            stop_slide.performClick();
        }
    }

    @Override
    public void pre() {
        Log.d(TAG,"pre");
        btnPre.performClick();
    }

    @Override
    public void next() {
        Log.d(TAG,"next");
        btnNext.performClick();
    }

    @Override
    public void exitPreView() {
        Log.d(TAG,"exitPreView");
        finish();
    }

    //以上为响应语义操作的实现
}
