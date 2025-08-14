package com.desaysv.usbpicture.ui;

import static com.desaysv.ivi.extra.project.carinfo.proxy.Constants.SpiMsgType.MSG_ON_CHANGE_EVENT;
import static com.desaysv.ivi.extra.project.carinfo.proxy.Constants.SpiMsgType.MSG_TIMING_ARRIVAL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import android.view.MotionEvent;
import com.desaysv.ivi.extra.project.carinfo.ReadOnlyID;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoHelper;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.carinfo.VDEventCarInfo;
import com.desaysv.ivi.vdb.event.id.carstate.VDEventCarState;
import com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState;
import com.desaysv.ivi.vdb.event.id.rvc.VDEventRvc;
import com.desaysv.ivi.vdb.event.id.rvc.VDValueRvc;
import com.desaysv.ivi.vdb.event.id.vehicle.VDEventVehicleHal;
import com.desaysv.ivi.vdb.event.id.vehicle.VDKeyVehicleHal;
import com.desaysv.libdevicestatus.bean.DeviceConstants;
import com.desaysv.libdevicestatus.bean.DeviceStatusBean;
import com.desaysv.libdevicestatus.listener.DeviceListener;
import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.manager.PictureListManager;
import com.desaysv.usbbaselib.bean.USBConstants;
import com.desaysv.usbbaselib.observer.Observer;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.adapter.PhotoPagerAdapter;
import com.desaysv.usbpicture.bean.MessageBean;
import com.desaysv.usbpicture.constant.Constant;
import com.desaysv.usbpicture.fragment.BaseUSBPictureListFragment;
import com.desaysv.usbpicture.photo.photoview.IPhotoViewActionListener;
import com.desaysv.usbpicture.photo.photoview.PhotoView;
import com.desaysv.usbpicture.trigger.PictureVRControl;
import com.desaysv.usbpicture.trigger.VDBRVCControl;
import com.desaysv.usbpicture.trigger.interfaces.IRVCResponseOperator;
import com.desaysv.usbpicture.trigger.interfaces.IVRResponseOperator;
import com.desaysv.usbpicture.utils.ProductUtils;
import com.desaysv.usbpicture.utils.SharedPreferencesUtils;
import com.desaysv.usbpicture.view.CanScrollViewPager;
import com.desaysv.usbpicture.view.CircularProgressView;
import com.desaysv.usbpicture.view.ViewPagerScroller;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import com.desaysv.usbpicture.constant.Point;
import com.desaysv.usbpicture.trigger.PointTrigger;

/**
 * Created by LZM on 2019-9-18
 * Comment 图片浏览界面的base activity
 */
public class BasePictureActivity extends Activity implements View.OnClickListener, IPhotoViewActionListener, IVRResponseOperator, IRVCResponseOperator {

    private final String TAG = this.getClass().getSimpleName();

    public static final String INTENT_POSITION = "INTENT_POSITION";
    public static final String INTENT_STYLE_TYPE = "INTENT_STYLE_TYPE";
    public static final String INTENT_USB_TYPE = "INTENT_USB_TYPE";
    public static final String INTENT_FOLDER_PATH = "INTENT_FOLDER_PATH";

    private CanScrollViewPager vpPicture;
    private PhotoPagerAdapter mPicturePagerAdapter;
    private CircularProgressView time_progress;
    private ImageView btnBack;
    private ImageView btnNarrow;
    private ImageView btnEnlarge;
    private ImageView btnFlip;
    private ImageView btnSlide;
    private ImageView btnPre;
    private ImageView btnNext;
    private ImageView stop_slide;
    private int currentPosition;
    private int styleType = 0;
    private int usbType = 0;
    private String path;

    private LinearLayout ll_top_control;
    private LinearLayout ll_bottom_control;

    private RelativeLayout rl_full_screen_bg;
//    private RelativeLayout rl_mask;

    //幻灯片模式
    private boolean isFlipMode;
    private boolean isFlipModePlaying = false;

    private boolean needLimitSpeed = false;
    private int limitSpeed = 15;

    //判断处于幻灯片模式时是否需要显示名称，例如显示了幻灯片时的操作栏
    private boolean needShowNameOnFlipMode = false;

    private RelativeLayout roo_view;
    private Button btnToPlay;
    private Button btnClose;

    private static final int GEAR_SELECTION_P = 0x1;//Display P
    private static final int GEAR_SELECTION_R = 0x2;//Display R
    private static final int GEAR_SELECTION_N = 0x3;//Display N
    private static final int GEAR_SELECTION_D = 0x4;//Display D

    private final CarInfoHelper mCarInfoHelper = new CarInfoHelper();
    private final int[] mReadonlyIds = new int[] {
            ReadOnlyID.ID_GEARBOX_STATE,                    // 变速箱档位

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        initView();
        initViewListener();
        initData();
    }



    public int getLayoutResID() {
        return R.layout.usb_picture_activity;
    }


    public void initView() {
        setContentView(getLayoutResID());
        vpPicture = findViewById(R.id.vpPicture);
        btnBack = findViewById(R.id.btnBack);
        btnNarrow = findViewById(R.id.btnNarrow);
        btnNarrow.setEnabled(false);//初始就是最小缩放了
        btnEnlarge = findViewById(R.id.btnEnlarge);
        btnFlip = findViewById(R.id.btnFlip);
        btnSlide = findViewById(R.id.btnSlide);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);
        stop_slide = findViewById(R.id.stop_slide);
        time_progress = findViewById(R.id.time_progress);
        myHandler = new MyHandler(this);
        rl_full_screen_bg = findViewById(R.id.rl_full_screen_bg);

        ll_bottom_control = findViewById(R.id.ll_bottom_control);
        ll_top_control = findViewById(R.id.ll_top_control);

        roo_view = findViewById(R.id.roo_view);
        btnToPlay = findViewById(R.id.btnToPlay);
        btnClose = findViewById(R.id.btnClose);
        roo_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: can not click bg");
                return true;
            }
        });
//        rl_mask = findViewById(R.id.rl_mask);
    }


    public void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            styleType = intent.getIntExtra(INTENT_STYLE_TYPE, 0);
            usbType = intent.getIntExtra(INTENT_USB_TYPE, 0);
            path = intent.getStringExtra(INTENT_FOLDER_PATH);
            if (path == null){
                path = USBConstants.USBPath.USB0_PATH;
            }
        }
       SharedPreferencesUtils.getInstance().initialize(this);
       needLimitSpeed = ProductUtils.needCheckSpeed() && SharedPreferencesUtils.getInstance().getNeedPop();
       limitSpeed = ProductUtils.getLimitSpeed();
        updatePicturePagerAdapter();
    }

    public List<FileMessage> getCurrentShowPictures(){
        if (styleType == BaseUSBPictureListFragment.STYLE_TYPE_ALL_PICTURE){// all pictures
            if (usbType == Constant.USBType.TYPE_USB1) {
                return PictureListManager.getInstance().getAllUSB1PictureList();
            }else {
                return PictureListManager.getInstance().getAllUSB2PictureList();
            }
        }else {
            if (usbType == Constant.USBType.TYPE_USB1) {
                if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
                    return PictureListManager.getInstance().getCurrentUSB1PictureList(path);
                }else {
                    return PictureListManager.getInstance().getCurrentUSB1PictureList();
                }
            }else {
                if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
                    return PictureListManager.getInstance().getCurrentUSB2PictureList(path);
                }else {
                    return PictureListManager.getInstance().getCurrentUSB2PictureList();
                }
            }
        }
    }

    public void initViewListener() {
        btnBack.setOnClickListener(this);
        btnNarrow.setOnClickListener(this);
        btnEnlarge.setOnClickListener(this);
        btnFlip.setOnClickListener(this);
        btnSlide.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        stop_slide.setOnClickListener(this);
        btnToPlay.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        vpPicture.addOnPageChangeListener(mOnPageChangeListener);
//        EventBus.getDefault().register(this);
        DeviceStatusBean.getInstance().addDeviceListener(deviceListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(null);
        Log.d(TAG,"onNewIntent");
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mPicturePagerAdapter.updateScreenParams(getWindowManager().getMaximumWindowMetrics().getBounds().width(),getWindowManager().getMaximumWindowMetrics().getBounds().height());
        Intent intent = getIntent();
        if (intent != null) {
            int position = intent.getIntExtra(INTENT_POSITION, 0);
            currentPosition = position + 1;//做了轮播，所以需要重新计算
            openPhoto(currentPosition);
        }
        PictureListManager.getInstance().attachUSB1Observer(usb1ListObserver);
        autoFullScreen();

        //注册语义操作的实现者
        PictureVRControl.getInstance().registerVRResponseOperator(this);

        // 被动获取倒车状态回调（callback）
//        VDBus.getDefault().addSubscribe(VDEventRvc.RVC_STATUS); // 订阅RVC倒车状态事件,
        VDBus.getDefault().addSubscribe(VDEventRvc.VIDEO_SIGNAL); // 订阅倒车摄像头信号事件
        VDBus.getDefault().addSubscribe(VDEventCarState.TOD_STATUS); // 订阅屏幕信号事件
        VDBus.getDefault().addSubscribe(VDEventCarState.POWER_STATUS); // 订阅点火信号事件
        VDBus.getDefault().addSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED); // 订阅车速信号事件
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);

        //设置档位监听
        mCarInfoHelper.listen(VDEventCarInfo.MODULE_READONLY_INFO, mReadonlyIds);
        mCarInfoHelper.start(spiListener);
    }

    private void autoFullScreen() {
        myHandler.removeCallbacks(runnableFullScreen);
        myHandler.postDelayed(runnableFullScreen, 5000);
    }

    private void openPhoto(int position) {
        Log.d(TAG, "openPhoto: position = " + position);
        mPicturePagerAdapter.setSmoothScroll(false);
        if (currentPosition < 0) {
            currentPosition = mPicturePagerAdapter.getCount() - 3;
        }

        if (currentPosition > mPicturePagerAdapter.getCount() -1 ) {
            currentPosition = 2;
        }
        vpPicture.resetPhoto();//切换图片时，先把上一张图片恢复
        vpPicture.setCurrentItem(currentPosition, false);
        updateBtnWithPosition();
    }


    private void updatePicturePagerAdapter() {
        Log.d(TAG, "updatePicturePagerAdapter: mPicturePagerAdapter = " + mPicturePagerAdapter);
        if (mPicturePagerAdapter == null) {
            mPicturePagerAdapter = new PhotoPagerAdapter(this,this);
            mPicturePagerAdapter.setPhotoList(vpPicture, getCurrentShowPictures());
            vpPicture.setAdapter(mPicturePagerAdapter);
        } else {
            mPicturePagerAdapter.setPhotoList(vpPicture, getCurrentShowPictures());
            mPicturePagerAdapter.notifyDataSetChanged();
        }
        time_progress.setMaxProgress(getCurrentShowPictures().size());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnBack){
            if (isFlipMode) {
                exitSlide();
                exitSlideMode();
            }else {
                finish();
            }
        }else if (v.getId() == R.id.btnNarrow){
            photoNarrow(mPicturePagerAdapter.getCurrentPhotoView());
            //埋点：缩小
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.NARROW
                    ,Point.Field.PictureName,String.valueOf(mPicturePagerAdapter.getCurrentTextView().getText()));
        }else if (v.getId() == R.id.btnEnlarge){
            photoEnlarge(mPicturePagerAdapter.getCurrentPhotoView());
            //埋点：放大
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.ENLARGE
                    ,Point.Field.PictureName,String.valueOf(mPicturePagerAdapter.getCurrentTextView().getText()));
        }else if (v.getId() == R.id.btnFlip){
            mPicturePagerAdapter.getCurrentPhotoView().flipPhoto();
            //埋点：旋转
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.ROTATE
                    ,Point.Field.PictureName,String.valueOf(mPicturePagerAdapter.getCurrentTextView().getText()));
        }else if (v.getId() == R.id.btnSlide){
            startSlide();
        }else if (v.getId() == R.id.btnPre){
            currentPosition--;
            openPhoto(currentPosition);
            //埋点：上一张
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.PRE
                    ,Point.Field.PictureName,String.valueOf(mPicturePagerAdapter.getCurrentTextView().getText()));
        }else if (v.getId() == R.id.btnNext){
            currentPosition++;
            openPhoto(currentPosition);
            //埋点：下一张
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.NEXT
                    ,Point.Field.PictureName,String.valueOf(mPicturePagerAdapter.getCurrentTextView().getText()));
        }else if (v.getId() == R.id.stop_slide){
            if (isFlipModePlaying) {
                stopSlide();
            }else {
                startSlide();
            }
            updateViewWithSlideMode(true);
        }else if (v.getId() == R.id.btnToPlay){
            //SharedPreferencesUtils.getInstance().saveNeedCheckSpeed(false);
            SharedPreferencesUtils.getInstance().setNeedPop(true);
            needLimitSpeed = false;
            updateViewWithLimitSpeed(false,false);
            startSlide();
        }else if (v.getId() == R.id.btnClose){
//            updateViewWithLimitSpeed(false,false);
//            exitSlide();
            finish();
            return;
        }
        handleClickWithBtn();
    }

    private void photoEnlarge(PhotoView imageView) {
        Log.d(TAG, "photoEnlarge: 放大");
        imageView.enlargePhoto();
        // SVMediaPicture操作数据埋点，使用放大
        //BuriedPointManager.getInstance().addDesayEventData(BuriedPoint.E_ENLARGE,BuriedPoint.USED);
    }

    /**
     * Bitmap缩小的方法
     */
    private void photoNarrow(PhotoView imageView) {
        Log.d(TAG, "photoNarrow: 缩小");
        imageView.narrowPhoto();
        // SVMediaPicture操作数据埋点，使用缩小
        //BuriedPointManager.getInstance().addDesayEventData(BuriedPoint.E_NARROW,BuriedPoint.USED);
    }

    private void stopSlide() {
        Log.d(TAG, "stopSlide");
        isFlipModePlaying = false;
        myHandler.removeMessages(MSG_START_SLIDE);
        //埋点：停止幻灯片
        PointTrigger.getInstance().trackEvent(Point.KeyName.SlideShowOperate,Point.Field.PicOperType,Point.FieldValue.STOP_SLIDE);
    }

    private void startSlide() {
        Log.d(TAG, "startSlide");
        if (needShowLimitSpeed() == NO_NEED_SHOW_LIMIT) {
            time_progress.setProgress(currentPosition);
            startSlideRunnable();
            enterSlideMode();
            //埋点：开始幻灯片
            PointTrigger.getInstance().trackEvent(Point.KeyName.SlideShowOperate,Point.Field.PicOperType,Point.FieldValue.START_SLIDE);
            mPicturePagerAdapter.setZoomable(false);
        }
    }

    private static final int NO_NEED_SHOW_LIMIT = 0;
    private static final int NEED_SHOW_LIMIT = 1;
    private static final int ERROR_SHOW_LIMIT = 2;
    /**
     * 是否禁播限速 0 不限速 1限速 2数据错误
     * @return
     */
    private int needShowLimitSpeed() {
        if (needLimitSpeed){//开始幻灯片时，检查一次车速
            if (limitSpeed == 0){
                int gearboxStatus = CarInfoProxy.getInstance().getItemValue(VDEventCarInfo.MODULE_READONLY_INFO, ReadOnlyID.ID_GEARBOX_STATE);
                boolean isDorRGearbox = isDorRGearbox(gearboxStatus);
                Log.d(TAG, "startSlide,gearboxStatus " + gearboxStatus+",isDorRGearbox = "+isDorRGearbox);
                updateViewWithLimitSpeed(isDorRGearbox,true);
               if (isDorRGearbox) {
                   return NEED_SHOW_LIMIT;
               } else {
                   return NO_NEED_SHOW_LIMIT;
               }
            }else {
                VDEvent vehicleSpeed = VDBus.getDefault().getOnce(VDEventVehicleHal.PERF_VEHICLE_SPEED);
                double[] data = vehicleSpeed.getPayload().getDoubleArray(VDKeyVehicleHal.DOUBLE_VECTOR);
                Log.d(TAG, "startSlide: data " + Arrays.toString(data));
                if (data.length > 0) {
                    float speed = (float) (data[0] * 3.6f);
                    Log.d(TAG, "startSlide isOverSpeed: original speed " + speed);
                    //底层上传的车速都是整形，但是因为存在转换误差，所以需要进行四舍五入转化
                    speed = Math.round(speed);
                    //没有进入过幻灯片时，isFlipMode还是false,超速拦截后没有提示，所以需要添加一个新的变量
                    updateViewWithLimitSpeed(speed > limitSpeed,true);
                    if (speed > limitSpeed) {
                        return NEED_SHOW_LIMIT;
                    } else {
                        return NO_NEED_SHOW_LIMIT;
                    }
                } else {
                    Log.w(TAG, "needShowLimitSpeed: error data.length == 0");
                    return ERROR_SHOW_LIMIT;
                }
            }
        } else {
            Log.d(TAG, "needShowLimitSpeed: needLimitSpeed == false");
            return NO_NEED_SHOW_LIMIT;
        }
    }

    /**
     * 是否是D挡或N挡
     * @param gearboxStatus
     * @return
     */
    private boolean isDorRGearbox(int gearboxStatus){
        Log.d(TAG, "isDorRGearbox: gearboxStatus = "+gearboxStatus);
        if (gearboxStatus == 0) {
            Log.d(TAG, "gearboxStatus == 0,error status");
            return false;
        }
        return gearboxStatus != GEAR_SELECTION_P;
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
        if (!needShowNameOnFlipMode) {
            mPicturePagerAdapter.updateNameVisibility(false);
        }
        time_progress.setProgress(currentPosition);
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
        if (scaleType == MessageBean.CAN_NOT_ENLARGE_PHOTO) {//不能放大，放大按钮置灰
            Log.d(TAG, "放大 eventBusMsg: flag = "+value);
            btnEnlarge.setEnabled(!value);

        } else if(scaleType == MessageBean.CAN_NOT_NARROW_PHOTO) {//不能缩小，缩小按钮置灰
            Log.d(TAG, "缩小 eventBusMsg: flag = "+value);
            btnNarrow.setEnabled(!value);

        }
    }

    @Override
    public void onPrimaryItemChanged(int position) {

    }

    @Override
    public void updateWithRVC(int status) {
        updateSlideShowWithRVC(status);
    }

    private static class MyHandler extends Handler{
        private WeakReference<BasePictureActivity> weakReference;
        public MyHandler(BasePictureActivity basePictureActivity) {
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
                    mPicturePagerAdapter.setZoomable(true);
                }
            });
        }
    }

    private void setFullScreen(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
//            rl_mask.setVisibility(View.GONE);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//            rl_mask.setVisibility(View.VISIBLE);
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void eventBusMsg(MessageBean messageBean) {
//        Log.d(TAG, "onEventBusMsg: view 点击事件 = " + messageBean.isClickAction());
//
//        handleClickEvent(messageBean.isClickAction());
//
//        if (messageBean.getType() == MessageBean.CAN_NOT_ENLARGE_PHOTO) {//不能放大，放大按钮置灰
//            Log.d(TAG, "放大 eventBusMsg: type = "+messageBean.getType()+ " flag = "+messageBean.isFlag());
//            if (messageBean.isFlag()) {
//                btnEnlarge.setEnabled(false);
//            } else {
//                btnEnlarge.setEnabled(true);
//            }
//
//        } else if(messageBean.getType() == MessageBean.CAN_NOT_NARROW_PHOTO) {//不能缩小，缩小按钮置灰
//            Log.d(TAG, "缩小 eventBusMsg: type = "+messageBean.getType()+ " flag = "+messageBean.isFlag());
//            if (messageBean.isFlag()) {
//                btnNarrow.setEnabled(false);
//            } else {
//                btnNarrow.setEnabled(true);
//            }
//
//        }
//
//    }

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
//        btnFlip.setVisibility(View.GONE);
//        btnSlide.setVisibility(View.GONE);
        if (isFlipMode) {
            btnPre.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
        }
//        btnEnlarge.setVisibility(View.GONE);
//        btnNarrow.setVisibility(View.GONE);
//        btnBack.setVisibility(View.GONE);
        isFullScreen = true;
        hideNavigationBar(true);
        mPicturePagerAdapter.updateNameVisibility(false);
        setFullScreenBg(true);
    }

    private void exitSlideMode() {
        Log.d(TAG, "exitSlideMode");
        updateViewWithSlideMode(false);
        btnFlip.setVisibility(View.VISIBLE);
        btnSlide.setVisibility(View.VISIBLE);
        btnPre.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        updateBtnWithPosition();
        btnEnlarge.setVisibility(View.VISIBLE);
        btnNarrow.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        isFullScreen = false;
        hideNavigationBar(false);
        mPicturePagerAdapter.updateNameVisibility(true);
        needShowNameOnFlipMode = false;
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
        PointTrigger.getInstance().trackEvent(Point.KeyName.OpenPictureClick,Point.Field.OpenType,Point.FieldValue.CLICKACTION);
        //onResume检测一次车速，显示限速弹窗
        needShowLimitSpeed();
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
        //注销语义操作的实现者
        PictureVRControl.getInstance().unregisterVRResponseOperator(this);
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);
        mCarInfoHelper.end();
        PointTrigger.getInstance().trackEvent(Point.KeyName.ClosePictureClick,Point.Field.CloseType,Point.FieldValue.CLICKACTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
//        EventBus.getDefault().unregister(this);
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
        //VDBus.getDefault().release();
    }


    /**
     * 显示或解除限速禁播
     * @param showLimit
     */
    private void updateViewWithLimitSpeed(boolean showLimit,boolean isOnClick){
        Log.d(TAG,"updateViewWithLimitSpeed,showLimit:"+showLimit);
        if (showLimit){
            if (roo_view.getVisibility() != View.VISIBLE) {
                if (isFlipMode){
                    Log.i(TAG,"幻灯片=="+isFlipMode);
                    stopSlide();
                } else {
                    Log.i(TAG,"isOnClick幻灯片=="+isOnClick);
                    myHandler.removeCallbacks(runnableFullScreen);
                    if (!isFullScreen) {
                        hideNavigationBar(true);
                    }
                }
                roo_view.bringToFront();
                roo_view.setVisibility(View.VISIBLE);
            }
        } else {
            if (roo_view.getVisibility() != View.GONE) {
                roo_view.setVisibility(View.GONE);
                if (isFlipMode) {
                    startSlide();
                } else {
                    if (!isFullScreen) {
                        hideNavigationBar(false);
                    }
                    autoFullScreen();
                }
            }
        }
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
                    case VDEventCarState.POWER_STATUS:
                        int powerStatus = event.getPayload().getInt(VDKey.STATUS);
                        Log.d(TAG,"onVDNotify,powerStatus: "+powerStatus);
                        updateSlideShowWithRVC(powerStatus);
                        break;
                    case VDEventVehicleHal.PERF_VEHICLE_SPEED:
                        if (needLimitSpeed){
                            double[] data = event.getPayload().getDoubleArray(VDKeyVehicleHal.DOUBLE_VECTOR);
                            Log.d(TAG, "onVDNotify: data " + Arrays.toString(data));
                            if (data.length > 0) {
                                float speed = (float) (data[0] * 3.6f);
                                Log.d(TAG, "isOverSpeed: original speed " + speed);
                                //底层上传的车速都是整形，但是因为存在转换误差，所以需要进行四舍五入转化
                                speed = Math.round(speed);
                                if(limitSpeed == 0){ //车速限制阈值为0时去判断挡位
                                    int gearboxStatus = CarInfoProxy.getInstance().getItemValue(VDEventCarInfo.MODULE_READONLY_INFO, ReadOnlyID.ID_GEARBOX_STATE);
                                    boolean isDorRGearbox = isDorRGearbox(gearboxStatus);
                                    Log.d(TAG, "onVDNotify,gearboxStatus " + gearboxStatus+",isDorRGearbox = "+isDorRGearbox);
                                    updateViewWithLimitSpeed(isDorRGearbox,false);
                                }else{
                                    updateViewWithLimitSpeed(speed > limitSpeed,false);
                                }
                            }
                        }
                        break;
                }
            }
        }
    };

    private CarInfoHelper.ISpiListener spiListener = new CarInfoHelper.ISpiListener() {
        @Override
        public void onReceiveSpi(int msgType, int moduleId, int cmdId) {
            Log.d(TAG,"onReceiveSpi,msgType = " + msgType + ", moduleId = " + moduleId + ", cmdId = " + cmdId);
            switch (msgType) {
                case MSG_TIMING_ARRIVAL:
                case MSG_ON_CHANGE_EVENT:
                    handleSpiEvent(moduleId, cmdId);
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 处理CarInfoHelper回调数据
     * @param moduleId 模块ID
     * @param cmdId command id
     */
    private void handleSpiEvent(int moduleId, int cmdId) {
        int value = CarInfoProxy.getInstance().getItemValue(moduleId, cmdId);
        int[] values = CarInfoProxy.getInstance().getItemValues(moduleId, cmdId);
        Log.d(TAG, "handleSpiEvent: moduleId = " + moduleId + " cmdId = " + cmdId + " values = " + Arrays.toString(values));
        if (moduleId == VDEventCarInfo.MODULE_READONLY_INFO) {
            if (cmdId == ReadOnlyID.ID_GEARBOX_STATE) {
                // 变速箱档位
                Log.d(TAG, "handleSpiEvent: gearBoxState = " + value);
                //变速箱状态 0x01：P档  0x02：N档  0x03：R档 0x04：D档
                if (limitSpeed == 0) {
                    updateViewWithLimitSpeed(isDorRGearbox(value), false);
                }


            }
        }
    }

    /**
     * 设备状态变化的监听回调
     */
    private DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStatusChange(String path, boolean status) {
            Log.d(TAG, "onDeviceStatusChange: path = " + path + " status = " + status);
            switch (path){
                case DeviceConstants.DevicePath.USB0_PATH:
                case DeviceConstants.DevicePath.USB1_PATH:
                    if (!status) {
                        Log.d(TAG,"onDeviceStatusChange finish");
                        finish();
                    }
                    break;
            }
        }
    };

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
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //vpPicture.resetPhoto();
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
                    if (mPicturePagerAdapter != null) {
                        mPicturePagerAdapter.setSmoothScroll(true);
                    }
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
            if (isFlipMode) {
                btnNarrow.setVisibility(View.GONE);
                btnEnlarge.setVisibility(View.GONE);
                btnFlip.setVisibility(View.GONE);
                btnSlide.setVisibility(View.GONE);
                btnBack.setVisibility(View.VISIBLE);
                btnPre.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                stop_slide.setVisibility(View.VISIBLE);
                ll_top_control.bringToFront();
                ll_bottom_control.bringToFront();
                mPicturePagerAdapter.updateNameVisibility(true);
                needShowNameOnFlipMode = true;
            }else {
                btnBack.setVisibility(View.VISIBLE);
                btnPre.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                stop_slide.setVisibility(View.VISIBLE);
            }
            stop_slide.setImageResource(isFlipModePlaying ? R.drawable.usb_picture_pause_icon : R.drawable.start);
            stop_slide.setContentDescription(isFlipModePlaying ? getResources().getString(R.string.description_pause) : getResources().getString(R.string.description_play));
            time_progress.setVisibility(View.GONE);//素材显示没有这个计时了
            handleClickWithBtn();
        }else {
            if (isFlipMode){
                vpPicture.bringToFront();
                mPicturePagerAdapter.updateNameVisibility(false);
                needShowNameOnFlipMode = false;
            }else {
                btnPre.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
                stop_slide.setVisibility(View.GONE);
            }
            time_progress.setVisibility(View.GONE);
            vpPicture.setBackgroundColor(Color.BLACK);
        }
    }

    private void setFullScreenBg(boolean isFullScreen){
        if (isFullScreen) {
            vpPicture.bringToFront();
            vpPicture.setBackgroundColor(Color.BLACK);
            rl_full_screen_bg.setBackgroundColor(Color.BLACK);
            stop_slide.setVisibility(View.VISIBLE);
        }else {
            vpPicture.setBackgroundColor(Color.TRANSPARENT);
            rl_full_screen_bg.setBackgroundColor(Color.TRANSPARENT);
            ll_top_control.bringToFront();
            ll_bottom_control.bringToFront();
            stop_slide.setVisibility(View.GONE);
        }
        //进入/退出全屏时，图片需要恢复原状，此时按钮应该恢复为可用
        btnEnlarge.setEnabled(true);
//        btnNarrow.setEnabled(true);//退出全屏时是恢复原样，需要置灰缩小图标
    }



    /**
     * 倒车影像和接听电话时暂停幻灯片
     * @param status
     */
    public void updateSlideShowWithRVC(int status){
        if (VDValueRvc.RvcStatus.RVC == status
                || VDValueCarState.PowerStatus.STATE_AVN_AVOFF == status
                || VDValueCarState.PowerStatus.STATE_AVN_TIME_ON == status){
            if (isFlipModePlaying) {
                stopSlide();
            }
            if (isFlipMode) {
                updateViewWithSlideMode(true);
            }
        }else {
            if (isFlipMode) {
                startSlide();
            }
        }
    }


    private Observer usb1ListObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG,"usb1ListObserver onUpdate");
            myHandler.removeMessages(MSG_UPDATE_LIST);
            myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
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

    @Override
    public void shrink() {
        Log.d(TAG,"shrink");
        photoNarrow(mPicturePagerAdapter.getCurrentPhotoView());
    }

    @Override
    public void zoom() {
        Log.d(TAG,"zoom");
        photoEnlarge(mPicturePagerAdapter.getCurrentPhotoView());
    }

    @Override
    public void goToImageList() {
        Log.d(TAG,"goToImageList");
        finish();
    }


    //以上为响应语义操作的实现
}
