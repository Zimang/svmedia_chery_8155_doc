package com.desaysv.usbpicture.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.desaysv.ivi.extra.project.carinfo.ReadOnlyID;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
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
import com.desaysv.usbpicture.constant.Point;
import com.desaysv.usbpicture.fragment.BaseUSBPictureListFragment;
import com.desaysv.usbpicture.photo.photoview.IPhotoViewActionListener;
import com.desaysv.usbpicture.photo.photoview.PhotoView;
import com.desaysv.usbpicture.trigger.PictureVRControl;
import com.desaysv.usbpicture.trigger.PointTrigger;
import com.desaysv.usbpicture.trigger.interfaces.IVRResponseOperator;
import com.desaysv.usbpicture.utils.BlurUtils;
import com.desaysv.usbpicture.utils.ProductUtils;
import com.desaysv.usbpicture.utils.SharedPreferencesUtils;
import com.desaysv.usbpicture.view.CanScrollViewPager;
import com.desaysv.usbpicture.view.CircularProgressView;
import com.desaysv.usbpicture.view.ViewPagerScroller;

//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by LZM on 2019-9-18
 * Comment 图片浏览界面的base activity
 */
public class BasePictureActivity extends Activity implements View.OnClickListener, IPhotoViewActionListener, IVRResponseOperator {

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

    private TextView tv_pic_name;

    private View v_top_mask;
    private View v_bottom_mask;

    private View vPreViewMask;

    private RelativeLayout rl_full_screen_bg;
//    private RelativeLayout rl_mask;

    private RelativeLayout rl_top_control;
    private RelativeLayout rl_bottom_control;

    private RelativeLayout rlAll;
    private View vPreViewBottomMask;

    //幻灯片模式
    private boolean isFlipMode;
    private boolean isFlipModePlaying = false;

    //全屏模式下显示控制按钮
    private boolean showFullScreenControl = false;

    //透明度变化速度的因子
    private static final float SCROLL_ALPHA_PARAMETER = 1.5f;

    private boolean isRightRudder = ProductUtils.isRightRudder();

    private boolean isSaudi = false;
    private boolean needLimitSpeed = false;
    private int limitSpeed = 15;

    private RelativeLayout roo_view;
    private Button btnToPlay;
    private Button btnClose;
    //记录进入页面后，数据是否有改变
    private boolean hasDataChg = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VDBus.getDefault().init(getApplicationContext());
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        Log.d(TAG,"onCreate");
        initView();
        initViewListener();
        initData();
    }



    public int getLayoutResID() {
        return isRightRudder ? R.layout.usb_picture_activity_right : R.layout.usb_picture_activity;
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
        tv_pic_name = findViewById(R.id.tv_pic_name);
//        rl_mask = findViewById(R.id.rl_mask);
        rl_top_control = findViewById(R.id.rl_top_control);
        rl_bottom_control = findViewById(R.id.rl_bottom_control);
        v_top_mask = findViewById(R.id.v_top_mask);
        v_bottom_mask = findViewById(R.id.v_bottom_mask);
        vPreViewMask = findViewById(R.id.vPreViewMask);
        vPreViewBottomMask = findViewById(R.id.vPreViewBottomMask);
        rlAll = findViewById(R.id.rlAll);

        roo_view = findViewById(R.id.roo_view);
        roo_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: can not click bg");
                return true;
            }
        });
        btnToPlay = findViewById(R.id.btnToPlay);
        btnClose = findViewById(R.id.btnClose);

        setMargin(false);
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

//        needLimitSpeed = false;
        needLimitSpeed =  ProductUtils.needCheckSpeed() && SharedPreferencesUtils.getInstance().getNeedPop();
//                && SharedPreferencesUtils.getInstance().getNeedCheckSpeed();

        limitSpeed = ProductUtils.getLimitSpeed();
        updatePicturePagerAdapter();
        isSaudi = ProductUtils.isSaudiPoint();
    }
    List<FileMessage> showList = new ArrayList<>();
    public List<FileMessage> getCurrentShowPictures(){
        showList.clear();
        if (styleType == BaseUSBPictureListFragment.STYLE_TYPE_ALL_PICTURE){// all pictures
            if (usbType == Constant.USBType.TYPE_USB1) {
                showList.addAll(PictureListManager.getInstance().getAllUSB1PictureList());
            }else {
                showList.addAll(PictureListManager.getInstance().getAllUSB2PictureList());
            }
        }else {
            if (usbType == Constant.USBType.TYPE_USB1) {
                if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
                    showList.addAll(PictureListManager.getInstance().getCurrentUSB1PictureList(path));
                }else {
                    showList.addAll(PictureListManager.getInstance().getCurrentUSB1PictureList());
                }
            }else {
                if (MediaKey.SUPPORT_SUBSECTION_LOADING) {
                    showList.addAll(PictureListManager.getInstance().getCurrentUSB2PictureList(path));
                }else {
                    showList.addAll(PictureListManager.getInstance().getCurrentUSB2PictureList());
                }
            }
        }
        return showList;
    }

    private List<FileMessage> filterBadItem(List<FileMessage> showList){
        List<FileMessage> list = new ArrayList<>();
        for (FileMessage fileMessage : showList){
            boolean filter = false;
            for (String badPath : PictureListManager.getInstance().getCurrentBadList()){
                if (badPath.equals(fileMessage.getPath())){
                    filter = true;
                }else {

                }
            }
            if (!filter) {
                list.add(fileMessage);
            }
        }
        Log.d(TAG,"filterBadItem,list:"+list.size());
        Log.d(TAG,"filterBadItem,showList:"+showList.size());
        return list;
    }
    List<FileMessage> realShowList;
    private int mapToRealPosition(int position){
        realShowList = filterBadItem(showList);
        Log.d(TAG,"mapToRealPosition,realShowList:"+realShowList.size());
        for (int i = 0; i <realShowList.size();i++){
            if (realShowList.get(i).getPath().equals(showList.get(position).getPath())){
                Log.d(TAG,"mapToRealPosition,position:"+position+"->"+i);
                return i;
            }
        }
        return position;
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

    @Override
    protected void onRestart() {
        super.onRestart();
        if(isFlipMode && isFlipModePlaying){
            myHandler.removeMessages(MSG_START_SLIDE);
            myHandler.sendEmptyMessageDelayed(MSG_START_SLIDE,5000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mPicturePagerAdapter.updateScreenParams(getWindowManager().getMaximumWindowMetrics().getBounds().width(),getWindowManager().getMaximumWindowMetrics().getBounds().height());
        Intent intent = getIntent();
        if (intent != null) {
            int position = intent.getIntExtra(INTENT_POSITION, 0);
            position = mapToRealPosition(position);
            currentPosition = position + 10000 * realShowList.size();//把位置后移到第1w个循环，这样可以实现假的列表无限循环(最多1w次循环)。因为动效的原因，无法使用T18P的算法循环处理方式。
            openPhoto(currentPosition);
        }
        PictureListManager.getInstance().attachUSB1Observer(usb1ListObserver);
        autoFullScreen();

        // 被动获取倒车状态回调（callback）
//        VDBus.getDefault().addSubscribe(VDEventRvc.RVC_STATUS); // 订阅RVC倒车状态事件,
        VDBus.getDefault().addSubscribe(VDEventRvc.VIDEO_SIGNAL); // 订阅倒车摄像头信号事件
        VDBus.getDefault().addSubscribe(VDEventCarState.TOD_STATUS); // 订阅屏幕信号事件
        VDBus.getDefault().addSubscribe(VDEventCarState.POWER_STATUS); // 订阅点火信号事件// 订阅车速信号事件
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);

        bindVehicleVDS();
        //注册语义操作的实现者
        PictureVRControl.getInstance().registerVRResponseOperator(this);
    }

    private void autoFullScreen() {
        myHandler.removeCallbacks(runnableFullScreen);
        myHandler.postDelayed(runnableFullScreen, 5000);
    }

    private void bindVehicleVDS() {
        Log.d(TAG, "bindVehicleVDS: ");
        VDBus.getDefault().registerVDBindListener(vdBindListener);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VEHICLE_HAL);
        //注册车速监听
        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.VEHICLE_HAL)){
            VDBus.getDefault().addSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED);
            VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
            VDBus.getDefault().subscribeCommit();
        } else {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.VEHICLE_HAL);
        }
    }

    private void unBindVehicleVDS() {
        Log.d(TAG, "unBindVehicleVDS: ");
        VDBus.getDefault().removeSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED);
        VDBus.getDefault().subscribeCommit();
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);

        VDBus.getDefault().unregisterVDBindListener(vdBindListener);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.VEHICLE_HAL);
    }

    private final VDBindListener vdBindListener = new VDBindListener() {
        @Override
        public void onVDConnected(VDServiceDef.ServiceType serviceType) {
            Log.d(TAG, "onVDConnected: serviceType " + serviceType);
            //只有绑定上了对应模块的VDS，才能进行订阅
            if (serviceType == VDServiceDef.ServiceType.VEHICLE_HAL) {
                // 注册消息、绑定监听
                VDBus.getDefault().addSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED);
                VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
                VDBus.getDefault().subscribeCommit();
            }
        }
        @Override
        public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
            Log.d(TAG, "onVDDisconnected: " + serviceType);
        }
    };

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
                                    boolean isDorRGearbox = !isP();
                                    Log.d(TAG, "onVDNotify,gearboxStatus " +",isNotP = "+isDorRGearbox);
                                    updateViewWithLimitSpeed(isDorRGearbox,false);
                                }else {
                                    updateViewWithLimitSpeed(speed > limitSpeed, false);
                                }
                            }
                        }
                        break;
                }
            }
        }
    };

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
                        setMargin(true);
                    }
                }
                roo_view.setVisibility(View.VISIBLE);
                roo_view.bringToFront();
            }
        }else {
            if (roo_view.getVisibility() != View.GONE) {
                roo_view.setVisibility(View.GONE);
                if (isFlipMode) {
                    startSlide();
                } else {
                    if (!isFullScreen) {
                        hideNavigationBar(false);
                        setMargin(false);
                    }
                    autoFullScreen();
                }
            }
        }
    }

    private void openPhoto(int position) {
        Log.d(TAG, "openPhoto: position = " + position);
//        vpPicture.resetPhoto();//切换图片时，先把上一张图片恢复
        vpPicture.setCurrentItem(currentPosition, true);
        updateBtnWithPosition();
    }


    private void updatePicturePagerAdapter() {
        Log.d(TAG, "updatePicturePagerAdapter: mPicturePagerAdapter = " + mPicturePagerAdapter);
        if (mPicturePagerAdapter == null) {
            mPicturePagerAdapter = new PhotoPagerAdapter(this,this);
            mPicturePagerAdapter.setPhotoList(vpPicture, filterBadItem(getCurrentShowPictures()));
            vpPicture.setAdapter(mPicturePagerAdapter);
            ViewPagerScroller viewPagerScroller = new ViewPagerScroller(this);
            viewPagerScroller.intiViewPager(vpPicture);
            vpPicture.setPageTransformer(true, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                        Log.d(TAG,"transformPage,position：" + position);
                        if (isFlipModePlaying){//幻灯片播放时不需要更新动画
                            return;
                        }

                        //position的大小是从 -2 到 2

                        if (position < -1){//向左完全移出屏幕
                            page.setAlpha(0);
                        }else if (position <= 0){//向左移出屏幕中
                            page.setAlpha(1+position);
                        }else if (position <= 1){//向右移出屏幕中
                            page.setAlpha(1-position);
                        }else {//向右完全移出屏幕
                            page.setAlpha(0);
                        }
                }
            });
        } else {
            mPicturePagerAdapter.setPhotoList(vpPicture, realShowList);
            mPicturePagerAdapter.notifyDataSetChanged();
        }
        time_progress.setMaxProgress(showList.size());
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
        }else if (v.getId() == R.id.btnNarrow){
            photoNarrow(mPicturePagerAdapter.getCurrentPhotoView());
            //埋点：缩小
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.NARROW
                    ,Point.Field.PictureName,realShowList.get(currentPosition % realShowList.size()).getFileName());
        }else if (v.getId() == R.id.btnEnlarge){
            photoEnlarge(mPicturePagerAdapter.getCurrentPhotoView());
            //埋点：放大
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.ENLARGE
                    ,Point.Field.PictureName,realShowList.get(currentPosition % realShowList.size()).getFileName());
        }else if (v.getId() == R.id.btnFlip){
            mPicturePagerAdapter.getCurrentPhotoView().flipPhoto();
            //埋点：旋转
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.ROTATE
                    ,Point.Field.PictureName,realShowList.get(currentPosition % realShowList.size()).getFileName());
        }else if (v.getId() == R.id.btnSlide){
            startSlide();
        }else if (v.getId() == R.id.btnPre){
            currentPosition--;
            openPhoto(currentPosition);
            //埋点：上一张
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.PRE
                    ,Point.Field.PictureName,realShowList.get(currentPosition % realShowList.size()).getFileName());
        }else if (v.getId() == R.id.btnNext){
            currentPosition++;
            openPhoto(currentPosition);
            //埋点：下一张
            PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.NEXT
                    ,Point.Field.PictureName,realShowList.get(currentPosition % realShowList.size()).getFileName());
        }else if (v.getId() == R.id.stop_slide){
            if (isFlipModePlaying) {
                stopSlide();
            }else {
                startSlide();
            }
            updateViewWithSlideMode(true);
        }else if (v.getId() == R.id.btnToPlay){
//            SharedPreferencesUtils.getInstance().saveNeedCheckSpeed(false);
            SharedPreferencesUtils.getInstance().setNeedPop(true);
            needLimitSpeed = false;
            updateViewWithLimitSpeed(false,false);
            startSlide();
        }else if (v.getId() == R.id.btnClose){
//            updateViewWithLimitSpeed(false,false);
//            exitSlide();
//            exitSlideMode();
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
        PointTrigger.getInstance().trackEvent(Point.KeyName.SlideShowOperate,Point.Field.OperType,Point.FieldValue.STOP_SLIDE);
    }

    private void startSlide() {
        Log.d(TAG, "startSlide");
        //开始幻灯片时，检查一次车速
        if (needShowLimitSpeed() == NO_NEED_SHOW_LIMIT) {
            time_progress.setProgress(currentPosition);
            startSlideRunnable();
            enterSlideMode();
            //埋点：开始幻灯片
            PointTrigger.getInstance().trackEvent(Point.KeyName.SlideShowOperate, Point.Field.PicOperType, Point.FieldValue.START_SLIDE);
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
        if (needLimitSpeed){
            if (limitSpeed == 0) {
                boolean isDorRGearbox = !isP();
                Log.d(TAG, "needShowLimitSpeed,gearboxStatus "  + ",isNotP = " + isDorRGearbox);
                updateViewWithLimitSpeed(isDorRGearbox, true);
                if (isDorRGearbox) {
                    return NEED_SHOW_LIMIT;
                } else {
                    return NO_NEED_SHOW_LIMIT;
                }
            }else {
                VDEvent vehicleSpeed = VDBus.getDefault().getOnce(VDEventVehicleHal.PERF_VEHICLE_SPEED);
                double[] data = vehicleSpeed.getPayload().getDoubleArray(VDKeyVehicleHal.DOUBLE_VECTOR);
                Log.d(TAG, "needShowLimitSpeed: data " + Arrays.toString(data));
                if (data.length > 0) {
                    float speed = (float) (data[0] * 3.6f);
                    Log.d(TAG, "needShowLimitSpeed isOverSpeed: original speed " + speed);
                    //底层上传的车速都是整形，但是因为存在转换误差，所以需要进行四舍五入转化
                    speed = Math.round(speed);
                    //没有进入过幻灯片时，isFlipMode还是false,超速拦截后没有提示，所以需要添加一个新的变量
                    updateViewWithLimitSpeed(speed > limitSpeed, true);
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

    private static final int GEAR_SELECTION_P = 0x1;//Display P
    private static final int GEAR_SELECTION_R = 0x2;//Display R
    private static final int GEAR_SELECTION_N = 0x3;//Display N
    private static final int GEAR_SELECTION_D = 0x4;//Display D
    /**
     * 是否是D挡或N挡
     * @param gearboxStatus
     * @return
     */
//    private boolean isDorRGearbox(int gearboxStatus){
//        Log.d(TAG, "isDorRGearbox: gearboxStatus = "+gearboxStatus);
//        return gearboxStatus == GEAR_SELECTION_D || gearboxStatus == GEAR_SELECTION_R || gearboxStatus == GEAR_SELECTION_N;
//    }
    /**
     * 变速箱状态
     * 0x1: Display P
     * 0x2: Display R
     * 0x3 :Display N
     * 0x4: Display D
     * @return isP
     */
    private boolean isP() {
        int gearboxStatus = CarInfoProxy.getInstance().getItemValue(VDEventCarInfo.MODULE_READONLY_INFO, ReadOnlyID.ID_GEARBOX_STATE);
        Log.d(TAG, "gearboxStatus = " + gearboxStatus);
        if (gearboxStatus == 0) {
            Log.d(TAG, "gearboxStatus == 0,error status");
            return true;
        }
        return gearboxStatus == GEAR_SELECTION_P;
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
        Log.d(TAG,"onPrimaryItemChanged,position:"+position);
//        tv_pic_name.setText(getCurrentShowPictures().get(position).getFileName());
//        setFullScreenBg(isFullScreen || isFlipMode || showFullScreenControl);

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
                    isFlipModePlaying = false;
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
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//            rl_mask.setVisibility(View.VISIBLE);
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
//        btnFlip.setVisibility(View.VISIBLE);
//        btnSlide.setVisibility(View.VISIBLE);
//        btnPre.setVisibility(View.VISIBLE);
//        btnNext.setVisibility(View.VISIBLE);
        btnPre.setSelected(false);
        btnNext.setSelected(false);
        updateBtnWithPosition();
        //btnEnlarge.setVisibility(View.VISIBLE);
        //btnNarrow.setVisibility(View.VISIBLE);
//        btnBack.setVisibility(View.VISIBLE);
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
        PointTrigger.getInstance().trackEvent(Point.KeyName.OpenPictureClick,Point.Field.OpenType,Point.FieldValue.CLICKACTION);

        //判断当前主题，动态替换蒙层
        String themeStr = Settings.System.getString(getContentResolver(), "DESAY_SETTING_LAUNCH_THEME");
        Log.d(TAG,"themeStr:"+themeStr);
        if (themeStr != null){
            if ("1".equals(themeStr)){//Steamlined
                vPreViewBottomMask.setBackgroundResource(R.mipmap.img_dock_bg);
            }else if ("0".equals(themeStr)){//class
                vPreViewBottomMask.setBackgroundResource(R.mipmap.img_dock_bg2);
            }else {

            }
        }else {
            //如果themeStr为空，那么再根据是否为沙特默认使用
            if(isSaudi){
                vPreViewBottomMask.setBackgroundResource(R.mipmap.img_dock_bg2);
            }else {
                vPreViewBottomMask.setBackgroundResource(R.mipmap.img_dock_bg);
            }
        }

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
//        exitSlide();
        myHandler.removeMessages(MSG_START_SLIDE);
        myHandler.removeCallbacks(runnableFullScreen);
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);
        unBindVehicleVDS();
        //注销语义操作的实现者
        PictureVRControl.getInstance().unregisterVRResponseOperator(this);
        PointTrigger.getInstance().trackEvent(Point.KeyName.ClosePictureClick,Point.Field.CloseType,Point.FieldValue.CLICKACTION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
//        EventBus.getDefault().unregister(this);
        DeviceStatusBean.getInstance().removeDeviceListener(deviceListener);
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
            Log.d(TAG, "onPageSelected hasDataChg = " + hasDataChg);
            currentPosition = position;
            tv_pic_name.setText(realShowList.get(position % realShowList.size()).getFileName());
            if (hasDataChg) {
                hasDataChg = false;
                myHandler.removeMessages(MSG_UPDATE_LIST);
                myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
            } else {
                vpPicture.resetPhoto();
            }
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
//                    exitSlide();
//                    exitSlideMode();
                    myHandler.removeCallbacks(runnableFullScreen);
                    if (isFlipMode) {//如果是图片浏览模式，不是幻灯片模式，也是需要隐藏
                        updateViewWithSlideMode(true);
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
//            btnBack.setVisibility(View.VISIBLE);
//            btnPre.setVisibility(View.VISIBLE);
//            btnNext.setVisibility(View.VISIBLE);
            rl_top_control.bringToFront();
            rl_bottom_control.bringToFront();
            tv_pic_name.setVisibility(View.VISIBLE);
            btnBack.setImageAlpha(255);
            btnPre.setImageAlpha(255);
            btnNext.setImageAlpha(255);
            stop_slide.setVisibility(View.VISIBLE);
            stop_slide.setImageResource(isFlipModePlaying ? R.drawable.drawable_stop : R.drawable.drawable_play);
//            time_progress.setVisibility(View.GONE);//素材显示没有这个计时了
            handleClickWithBtn();
        }else {
            if (isFlipMode) {
                if(!roo_view.isShown()){
                    rl_full_screen_bg.bringToFront();
                }
                btnBack.setImageAlpha(0);
                btnPre.setImageAlpha(0);
                btnNext.setImageAlpha(0);
            }
            stop_slide.setVisibility(View.GONE);
            tv_pic_name.setVisibility(View.GONE);
//            time_progress.setVisibility(View.GONE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setFullScreenBg(boolean isFullScreen){
        Log.d(TAG,"setFullScreenBg:"+isFullScreen);
        if (isFullScreen) {
            tv_pic_name.setSelected(true);
            btnBack.setSelected(true);
            btnPre.setSelected(true);
            btnNext.setSelected(true);
            btnFlip.setSelected(true);
            btnSlide.setSelected(true);

            btnBack.setImageAlpha(0);
            btnPre.setImageAlpha(0);
            btnNext.setImageAlpha(0);
            btnFlip.setImageAlpha(0);
            btnSlide.setImageAlpha(0);
            stop_slide.setVisibility(View.GONE);
            if (isFlipMode){
                //按照最新UI，没有Mask
//                v_top_mask.setVisibility(View.VISIBLE);
//                v_bottom_mask.setVisibility(View.VISIBLE);
                tv_pic_name.setVisibility(View.GONE);
                stop_slide.setVisibility(View.VISIBLE);
//                v_top_mask.bringToFront();
//                v_bottom_mask.bringToFront();
            }else {
                v_top_mask.setVisibility(View.GONE);
                v_bottom_mask.setVisibility(View.GONE);
            }
            vPreViewMask.setVisibility(View.GONE);
            vPreViewBottomMask.setVisibility(View.GONE);
            if (!roo_view.isFocused()) {
                Log.d(TAG,"roo_view.isFocused false");
                rl_full_screen_bg.bringToFront();
            }
            rl_full_screen_bg.setBackground(getResources().getDrawable(R.mipmap.picture_vague_bg));

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vpPicture.getLayoutParams();
            lp.topMargin = 0;
            vpPicture.setLayoutParams(lp);

            setMargin(true);

        }else {
            vPreViewMask.bringToFront();
            vPreViewBottomMask.bringToFront();
            rl_top_control.bringToFront();
            rl_bottom_control.bringToFront();
            rl_full_screen_bg.setBackgroundColor(Color.TRANSPARENT);
            tv_pic_name.setSelected(false);
            btnBack.setSelected(false);
            btnPre.setSelected(false);
            btnNext.setSelected(false);
            btnFlip.setSelected(false);
            btnSlide.setSelected(false);

            btnBack.setImageAlpha(255);
            btnPre.setImageAlpha(255);
            btnNext.setImageAlpha(255);
            btnFlip.setImageAlpha(255);
            btnSlide.setImageAlpha(255);
            stop_slide.setVisibility(View.GONE);

            v_top_mask.setVisibility(View.GONE);
            v_bottom_mask.setVisibility(View.GONE);
            vPreViewMask.setVisibility(View.VISIBLE);
            vPreViewBottomMask.setVisibility(isSaudi ? View.VISIBLE : View.GONE);
            //增加动效，避免全屏/非全屏切换时，Dock还没出现时，Mask先出来，导致视觉效果上有两层Dock栏
            TranslateAnimation translateAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE,0,TranslateAnimation.ABSOLUTE,0,TranslateAnimation.ABSOLUTE,vPreViewBottomMask.getHeight(),TranslateAnimation.ABSOLUTE,0);
            translateAnimation.setDuration(200);
            vPreViewBottomMask.startAnimation(translateAnimation);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vpPicture.getLayoutParams();
            lp.topMargin = 40;
            vpPicture.setLayoutParams(lp);

            setMargin(false);
        }
        //限速提示需要一直在最上面
        if (roo_view.isShown()){
            roo_view.bringToFront();
        }
        //进入/退出全屏时，图片需要恢复原状，此时按钮应该恢复为可用
        btnEnlarge.setEnabled(true);
//        btnNarrow.setEnabled(true);//退出全屏时是恢复原样，需要置灰缩小图标
    }

    /**
     * 倒车影像和接听电话时暂停幻灯片
     * @param status
     */
    private boolean needResumePlay = false;
    public void updateSlideShowWithRVC(int status){
        if (VDValueRvc.RvcStatus.RVC == status
            || VDValueCarState.PowerStatus.STATE_AVN_AVOFF == status
            || VDValueCarState.PowerStatus.STATE_AVN_TIME_ON == status){
            if (isFlipModePlaying) {
                needResumePlay = true;
                stopSlide();
            }
            if (isFlipMode) {
                updateViewWithSlideMode(true);
            }
        }else {
            if (needResumePlay) {
                needResumePlay = false;//阅后即焚
                startSlide();
            }
        }
    }


    private Observer usb1ListObserver = new Observer() {
        @Override
        public void onUpdate() {
            Log.d(TAG,"usb1ListObserver onUpdate");
            if (checkUSBStatus()) {
//                myHandler.removeMessages(MSG_UPDATE_LIST);
//                myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
                hasDataChg = true;
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


    private void setMargin(boolean isFullScreen){
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rlAll.getLayoutParams();
        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) rl_bottom_control.getLayoutParams();
        if (isFullScreen){
            lp.setMargins(0,0,0,0);
            lp2.setMargins(0,311,0,0);
        }else {
            lp.setMargins(0,70,0,0);
            lp2.setMargins(0,241,0,0);
        }
    }
}
