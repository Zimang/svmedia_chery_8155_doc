package com.desaysv.moduleusbvideo.ui;

import static com.desaysv.libusbmedia.bean.MediaAction.FAST_FORWARD;
import static com.desaysv.libusbmedia.bean.MediaAction.FAST_FORWARD_STOP;
import static com.desaysv.libusbmedia.bean.MediaAction.NEXT;
import static com.desaysv.libusbmedia.bean.MediaAction.PAUSE;
import static com.desaysv.libusbmedia.bean.MediaAction.PLAY_OR_PAUSE;
import static com.desaysv.libusbmedia.bean.MediaAction.PRE;
import static com.desaysv.libusbmedia.bean.MediaAction.REWIND;
import static com.desaysv.libusbmedia.bean.MediaAction.REWIND_STOP;
import static com.desaysv.libusbmedia.bean.MediaAction.SEEKTO;
import static com.desaysv.libusbmedia.bean.MediaAction.START;
import static com.desaysv.libusbmedia.bean.MediaAction.STOP;

import android.annotation.SuppressLint;
import android.car.Car;
import android.car.media.CarAudioManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carinfo.ReadOnlyID;
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy;
import com.desaysv.ivi.vdb.IVDBusNotify;
import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDBindListener;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.carinfo.VDEventCarInfo;
import com.desaysv.ivi.vdb.event.id.rvc.VDEventRvc;
import com.desaysv.ivi.vdb.event.id.rvc.VDValueRvc;
import com.desaysv.ivi.vdb.event.id.sms.VDEventSms;
import com.desaysv.ivi.vdb.event.id.sms.VDValueAction;
import com.desaysv.ivi.vdb.event.id.sms.VDValueKey;
import com.desaysv.ivi.vdb.event.id.vehicle.VDEventVehicleHal;
import com.desaysv.ivi.vdb.event.id.vehicle.VDKeyVehicleHal;
import com.desaysv.libusbmedia.action.MediaControlAction;
import com.desaysv.libusbmedia.bean.CurrentPlayInfo;
import com.desaysv.libusbmedia.bean.MediaAction;
import com.desaysv.libusbmedia.bean.MediaPlayType;
import com.desaysv.libusbmedia.bean.MediaType;
import com.desaysv.libusbmedia.control.MediaControlTool;
import com.desaysv.libusbmedia.interfaze.IControlTool;
import com.desaysv.libusbmedia.interfaze.IMediaStatusChange;
import com.desaysv.libusbmedia.interfaze.IRequestMediaPlayer;
import com.desaysv.libusbmedia.interfaze.IStatusTool;
import com.desaysv.libusbmedia.utils.MediaPlayStatusSaveUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.utils.TimeUtils;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.adapter.USBVideoSpeedListAdapter;
import com.desaysv.moduleusbvideo.base.VideoBaseActivity;
import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.moduleusbvideo.util.LionPointSdkUtil;
import com.desaysv.moduleusbvideo.util.MediaVideoConstantUtils;
import com.desaysv.moduleusbvideo.util.SystemUtils;
import com.desaysv.moduleusbvideo.util.VideoSizeUtils;
import com.desaysv.moduleusbvideo.view.FilletViewOutlineProvider;
import com.desaysv.moduleusbvideo.view.NoviceGuidanceView;
import com.desaysv.moduleusbvideo.view.VideoPlayListPopupWindow;
import com.desaysv.moduleusbvideo.view.VideoPreview;
import com.desaysv.moduleusbvideo.view.VideoSpeedListPopupWindow;
import com.desaysv.moduleusbvideo.view.VideoSpeedTipsPopupWindow;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LZM on 2019-9-20
 * Comment USB视频播放界面的 base activity
 */
public abstract class BaseVideoPlayActivity extends VideoBaseActivity {
    private final String TAG = this.getClass().getSimpleName();

    protected static final String IS_NEED_TO_AUTO_PLAY = "is_need_to_auto_play";
    protected static final String AUTO_PLAY_PATH = "auto_play_path";

    /**
     * 默认播放
     */
    protected boolean isNeedToAutoPlay = true;
    protected String currentPath = null;

    /**
     * 硬按键的event
     */
    private VDEvent keyCodeVDEvent;

    protected Surface mSurface;
    private TextureView ttvVideo;

    private LinearLayout llPreview;
    private TextureView tvPreview;
    private RelativeLayout rlPreviewRoot;
    private LinearLayout llCurTotalTime;
    private TextView tvPreViewTime;
    private TextView tvPreViewTotal;
    private int mCurProgress;
    private int mDuration;
    private VideoPreview mVideoThumbnailsIv;

    private RelativeLayout rootView;
    private RelativeLayout rlController;
    private LinearLayout ll_video_play_top;

    private NoviceGuidanceView ngv;

    private SeekBar sbTime;
    private ImageView btnPre;
    private ImageView btnPlayOrPause;
    private ImageView btnNext;
    private TextView tvTime;
    private TextView noVideoShow;
    private TextView errorShow;
    private TextView videoName;
    private TextView tvDoubleSpeed;
    private TextView tvAnthology;

    private LinearLayout llSettingAdjust;
    private ImageView ivAdjustIcon;
    private SeekBar pbAdjustLightnessOrVolume;

    private LinearLayout llAction;

    /**
     * 是否在触摸进度
     */
    private boolean isSeekBarBeingTouch = false;

    /**
     * 是否隐藏播放控制布局
     */
    private boolean isHidePlayControl = false;

    /**
     * 手势处理，仅处理单点触控
     */
    private GestureDetector mGestureDetector;
    private float mPosX;
    private float mPosY;
    private float mCurPosX;
    private float mCurPosY;
    boolean isTouchStart = false;
    /**
     * 当前是否在进行进度调节
     */
    private boolean isChangedProgress;
    /**
     * 当前是否在进行亮度调节
     */
    private boolean isChangedBrightness;
    /**
     * 当前是否在进行音量调节
     */
    private boolean isChangedVolume;
    /**
     * 视频是否切换到了下一个
     */
    private boolean isSkipNextPosition;

    /**
     * 音量设置
     */
    private CarAudioManager carManager;
    private int volumeGroupIdMedia;

    private int mCurBrightness;
    private int mMaxBrightness;
    private int mCurMediaVolume;
    private int mMaxMediaVolume;


    private VideoPlayListPopupWindow videoPlayWindow;
    private float currentSpeed = MediaControlAction.DEFAULT_SPEED;
    private VideoSpeedListPopupWindow videoSpeedWindow;
    private USBVideoSpeedListAdapter usbVideoSpeedListAdapter;

    /**
     * 行车安全弹框
     */
    private VideoSpeedTipsPopupWindow videoSpeedTipsPopupWindow;
    /**
     * 播放且不在提示，
     * 保存当前的操作
     */
    private boolean playWithoutPrompting = false;

    /**
     * 是否T19FL项目
     */
    private boolean isT19Fl;

    /**
     * 记录播放状态
     * 用于进入RVC或360时，状态播放；
     */
    private boolean isPlaying = false;

    /**
     *
     */
    private boolean isRtl = false;

    /**
     * 第一次进入的白天黑夜主题模式，用来判断是主题模式切换
     */
    private int mCurrentUiMode;

    /**
     * intent 在切换主题重载时,没有清空intent里的数据
     * 故需要在onDestroy里主动清楚intent内的数据
     */
    private Intent intent;

    /**
     * 当前MediaPlayer
     */
    private MediaPlayer mMediaPlayer;

    /**
     * 5s控制栏消失
     */
    private final static long DELAY_TIME = 5000;
    private final static int HIDE_CONTROL = 0;
    private final static int UPDATE_VIDEO_ID3_INFO = HIDE_CONTROL + 1;
    private final static int UPDATE_VIDEO_PLAY_STATUS = UPDATE_VIDEO_ID3_INFO + 1;
    private final static int UPDATE_VIDEO_PLAY_TIME = UPDATE_VIDEO_PLAY_STATUS + 1;
    private final static int UPDATE_VIDEO_SURFACE_SHOW = UPDATE_VIDEO_PLAY_TIME + 1;
    private final static int UPDATE_VIDEO_SURFACE_HIDE = UPDATE_VIDEO_SURFACE_SHOW + 1;
    private final static int UPDATE_VIDEO_ERROR_SHOW = UPDATE_VIDEO_SURFACE_HIDE + 1;
    private final static int UPDATE_VIDEO_ERROR_HIDE = UPDATE_VIDEO_ERROR_SHOW + 1;
    /**
     * 根据视频的实际宽高，对视频大小进行拉伸
     */
    private static final int MSG_UPDATE_VIDEO_TEXTURE_VIEW_SIZE = UPDATE_VIDEO_ERROR_HIDE + 1;
    private static final int UPDATE_VIDEO_PROGRESS_IMAGE = MSG_UPDATE_VIDEO_TEXTURE_VIEW_SIZE + 1;

    private final static int UPDATE_VIDEO_HIDE = UPDATE_VIDEO_PROGRESS_IMAGE + 1;
    private final static int UPDATE_VIDEO_SHOW = UPDATE_VIDEO_HIDE + 1;

    private final static int UPDATE_VIDEO_SPEED_SHOW = UPDATE_VIDEO_SHOW + 1;
    private final static int UPDATE_VIDEO_SPEED_HIDE = UPDATE_VIDEO_SPEED_SHOW + 1;
    /**
     * 行车视频，持续低于指定速度3S后，锁定画面消失，暂停视频
     * 3s控制栏消失
     */
    private final static long SPEED_DELAY_TIME = 3000;

    /**
     * 延时250毫秒弹窗，避免截图背景是上个页面的图
     */
    private static final int UPDATE_VIDEO_SPEED_SHOW_DELAY_TIME = 250;

    private static final int DISPLAY_P = 1;
    private static final int DISPLAY_N = 3;

    //是否在播放
    private boolean mIsPlaying = false;

    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        WeakReference<BaseVideoPlayActivity> weakReference;

        MyHandler(BaseVideoPlayActivity baseVideoPlayActivity) {
            weakReference = new WeakReference<>(baseVideoPlayActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final BaseVideoPlayActivity baseVideoPlayActivity = weakReference.get();
            switch (msg.what) {
                case HIDE_CONTROL:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: HIDE_CONTROL");
                    baseVideoPlayActivity.hidePlayControl();
                    break;
                case UPDATE_VIDEO_ID3_INFO:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_ID3_INFO");
                    baseVideoPlayActivity.updateID3Info();
                    break;
                case UPDATE_VIDEO_PLAY_STATUS:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_PLAY_STATUS");
                    baseVideoPlayActivity.updatePlayStatus();
                    break;
                case UPDATE_VIDEO_PLAY_TIME:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_PLAY_TIME");
                    baseVideoPlayActivity.updatePlayTime();
                    break;
                case UPDATE_VIDEO_SURFACE_HIDE:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_SURFACE_HIDE");
                    // 显示-没有视频-提示语
                    baseVideoPlayActivity.noVideoShow.setVisibility(View.GONE);
                    break;
                case UPDATE_VIDEO_SURFACE_SHOW:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_SURFACE_SHOW");
                    // 隐藏-没有视频-提示语
                    baseVideoPlayActivity.noVideoShow.setVisibility(View.VISIBLE);
                    break;
                case UPDATE_VIDEO_ERROR_HIDE:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_ERROR_HIDE");
                    // 隐藏-错误-提示语
                    baseVideoPlayActivity.errorShow.setVisibility(View.GONE);
                    break;
                case UPDATE_VIDEO_ERROR_SHOW:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_ERROR_SHOW");
                    // 重置播放进度
                    baseVideoPlayActivity.sbTime.setMax(0);
                    baseVideoPlayActivity.sbTime.setProgress(0);
                    // 显示-错误-提示语
                    baseVideoPlayActivity.errorShow.setVisibility(View.VISIBLE);
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_ERROR_SHOW to Lion Point ");
                    FileMessage fileMessage = baseVideoPlayActivity.getVideoStatusTool().getCurrentPlayItem();
                    String fileName = null == fileMessage ? "" : fileMessage.getFileName();
                    //视频打开异常
                    LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_LOAD_ANOMALY, LionPointSdkUtil.FIELD_PAGE_NAME, fileName);
                    break;
                case UPDATE_VIDEO_HIDE:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_HIDE");
                    // 隐藏画面
                    baseVideoPlayActivity.ttvVideo.setAlpha(0);
                    break;
                case UPDATE_VIDEO_SHOW:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_SHOW");
                    // 显示画面
                    baseVideoPlayActivity.ttvVideo.setAlpha(1);
                    break;
                case MSG_UPDATE_VIDEO_TEXTURE_VIEW_SIZE:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: MSG_UPDATE_VIDEO_TEXTURE_VIEW_SIZE");
                    VideoSizeUtils.updateTextureViewSize(baseVideoPlayActivity.ttvVideo, msg.arg1, msg.arg2);
                    break;
                case UPDATE_VIDEO_PROGRESS_IMAGE:
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_PROGRESS_IMAGE");
                    baseVideoPlayActivity.updatePreview((Integer) msg.obj);
                    break;
                case UPDATE_VIDEO_SPEED_SHOW:
                    // 显示行车锁屏
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_SPEED_SHOW");
                    baseVideoPlayActivity.showSpeedTipsPopupWindow();
                    break;
                case UPDATE_VIDEO_SPEED_HIDE:
                    // 隐藏行车锁屏
                    Log.d(baseVideoPlayActivity.TAG, "handleMessage: UPDATE_VIDEO_SPEED_HIDE");
                    baseVideoPlayActivity.hideSpeedTipsPopupWindow();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLayoutResID() {
        return R.layout.usb_video_play_activity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 界面加载，判断是否有保存播放路径
        if (null != savedInstanceState) {
            String autoPlayPathTemp = savedInstanceState.getString(AUTO_PLAY_PATH);
            if (null != autoPlayPathTemp && !autoPlayPathTemp.isEmpty()) {
                currentPath = autoPlayPathTemp;
                Log.i(TAG, "onCreate: set currentPath: " + currentPath);
            }
        }
        mCurrentUiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        //出现切到后台，Activity被系统回收且没有被调用onDestroy() (savedInstanceState也为null)
        //mediaPlayer没有重置，没有设置新的surfaceView,导致有声音没有画面
        //这里选择按原有逻辑STOP去重置这种方式打个补丁
        FileMessage currentPlayFile = CurrentPlayInfo.getInstance(getMediaType()).getCurrentPlayItem();
        if (currentPlayFile != null && (currentPlayFile.getPath() != null && !currentPlayFile.getPath().isEmpty())) {
            Log.i(TAG, "onCreate: not call onDestroy STOP to reset: " + currentPlayFile.getPath());
            getVideoControl().processCommand(STOP, ChangeReasonData.UI_FINISH);
        }
        //T19FL项目倒档的时候不会抢焦点，需要单独监听RVC 360影像，暂停视频
        isT19Fl = isT19Fl();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 界面退出，保存当前播放路径
        if (null != outState) {
            Log.i(TAG, "onSaveInstanceState: put currentPath: " + currentPath);
            outState.putString(AUTO_PLAY_PATH, currentPath);
        }
    }

    @Override
    public void initView() {
        SystemUtils.setSystemUIVisible(this, false);
        Log.i(TAG, "initView: setting to systemui_swipe_show_enable 0 ");
        // 强制设置为，不允许拉起系统底部操作栏
        Settings.Global.putInt(getContentResolver(), "systemui_swipe_show_enable", 0);
        ngv = findViewById(R.id.ngv);
        rootView = findViewById(R.id.roo_view);
        rlController = findViewById(R.id.rl_controller);
        llPreview = findViewById(R.id.ll_pre_view);
        tvPreview = findViewById(R.id.tv_pre_view);
        rlPreviewRoot = findViewById(R.id.rl_pre_view_root);
        tvPreViewTime = findViewById(R.id.tv_pre_view_time);
        llCurTotalTime = findViewById(R.id.ll_cur_total_time);
        tvPreViewTotal = findViewById(R.id.tv_pre_view_total);
        ll_video_play_top = findViewById(R.id.ll_video_play_top);
        ttvVideo = findViewById(R.id.ttv_video);
        sbTime = findViewById(R.id.sb_time);
        btnPre = findViewById(R.id.btn_pre);
        btnPlayOrPause = findViewById(R.id.btn_play_or_pause);
        btnNext = findViewById(R.id.btn_next);
        tvTime = findViewById(R.id.tv_time);
        noVideoShow = findViewById(R.id.no_video_show);
        errorShow = findViewById(R.id.error_show);
        videoName = findViewById(R.id.video_name);
        tvDoubleSpeed = findViewById(R.id.tv_double_speed);
        tvAnthology = findViewById(R.id.tv_anthology);

        llSettingAdjust = findViewById(R.id.ll_setting_adjust);
        ivAdjustIcon = findViewById(R.id.iv_adjust_icon);
        pbAdjustLightnessOrVolume = findViewById(R.id.pb_adjust_progress);

        llAction = findViewById(R.id.ll_action);
    }

    @Override
    public void initData() {
        initVideoControl();
        isRtl = Constant.isRtl();
        Log.d(TAG, "initData: isRtl " + isRtl);
        mHandler = new MyHandler(this);

        //圆角
        tvPreview.setOutlineProvider(new FilletViewOutlineProvider(Constant.dp2px(this, 12)));
        tvPreview.setClipToOutline(true);
        mVideoThumbnailsIv = new VideoPreview(rlPreviewRoot, tvPreview);
        mGestureDetector = new GestureDetector(this, onGestureListener);

        // 获取存储的 亮度
        mCurBrightness = MediaVideoConstantUtils.getInstance().getScreenBacklight();
        mMaxBrightness = Constant.VIDEO_PLAY_ALPHA_MAX;
        Log.d(TAG, "initData: mCurBrightness = " + mCurBrightness + " mMaxBrightness = " + mMaxBrightness);
        setBrightness();

        // 音量
        carManager = (CarAudioManager) Car.createCar(this).getCarManager(Car.AUDIO_SERVICE);
        volumeGroupIdMedia = carManager.getVolumeGroupIdForUsage(AudioAttributes.USAGE_MEDIA);
        Log.d(TAG, "initData: volumeGroupIdMedia: " + volumeGroupIdMedia);

        // 新手引导
        int noviceGuidance = Settings.System.getInt(getContentResolver(), Constant.VIDEO_NOVICE_GUIDANCE, Constant.VIDEO_NOVICE_GUIDANCE_NOVICE);
        Log.d(TAG, "initData: noviceGuidance: " + noviceGuidance);
        if (Constant.VIDEO_NOVICE_GUIDANCE_NOVICE == noviceGuidance) {
            ngv.setVisibility(View.VISIBLE);
        } else {
            ngv.setVisibility(View.GONE);
        }

        if (isRtl) {
            //左阿语
            // 强制LTR
            // 滑动条保持国内方向LTR
            sbTime.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            //  亮度/音量控制进度条方向强制LTR
//            llSettingAdjust.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
//            pbAdjustLightnessOrVolume.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            // 返回按钮保持LTR
//            ll_video_play_top.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            // 操作按钮
            llAction.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            if (Constant.isExeed()) {
                //  亮度/音量控制进度条方向强制LTR
                llSettingAdjust.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        }else {
            // 滑动条保持国内方向LTR
            sbTime.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

            //右舵，左英
            if(!Constant.isExeed()){
                boolean leftOrRightConfig = Constant.isLeftOrRightConfig();
                if(leftOrRightConfig){
                    // 返回按钮保持LTR
                    ll_video_play_top.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    rlController.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    //  亮度/音量控制进度条方向强制LTR
                    llSettingAdjust.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    pbAdjustLightnessOrVolume.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    //这里需要针对 列表弹窗
                }
            } else {
                //  亮度/音量控制进度条方向强制LTR
                llSettingAdjust.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initViewListener() {
        super.initViewListener();
        ll_video_play_top.setOnClickListener(onClickListener);
        btnPre.setOnClickListener(onClickListener);
        btnPre.setOnLongClickListener(onLongClickListener);
        btnPre.setOnTouchListener(onTouchListener);
        btnPlayOrPause.setAccessibilityDelegate(playOrPause);
        btnPlayOrPause.setOnClickListener(onClickListener);
        btnNext.setOnClickListener(onClickListener);
        btnNext.setOnLongClickListener(onLongClickListener);
        btnNext.setOnTouchListener(onTouchListener);
        sbTime.setOnSeekBarChangeListener(onSeekBarChangeListener);
        //
        sbTime.setOnTouchListener(seekBarHeight);

        tvDoubleSpeed.setOnClickListener(onClickListener);
        tvAnthology.setOnClickListener(onClickListener);

        rootView.setOnTouchListener(rootViewTouch);

        // 视频surface 设置监听
        ttvVideo.setSurfaceTextureListener(mSurfaceTextureListener);
        // 音量 变化 监听
        carManager.registerCarVolumeCallback(carVolumeCallback);
    }

    private final View.AccessibilityDelegate playOrPause = new View.AccessibilityDelegate() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            Log.d(TAG, "onInitializeAccessibilityNodeInfo btnPlayOrPause");
            info.setChecked(mIsPlaying);
        }
    };

    /**
     * 隐藏操作栏
     */
    private void hidePlayControl() {
        Log.d(TAG, "hidePlayControl: ");
        if (mHandler.hasMessages(HIDE_CONTROL)) {
            mHandler.removeMessages(HIDE_CONTROL);
        }
        isHidePlayControl = true;
        rlController.setVisibility(View.GONE);
    }

    /**
     * 显示操作栏
     *
     * @param isStartHideControl 是否启动倒计时？
     */
    private void showPlayControl(boolean isStartHideControl) {
        Log.d(TAG, "showPlayControl: isStartHideControl: " + isStartHideControl);
        if (mHandler.hasMessages(HIDE_CONTROL)) {
            mHandler.removeMessages(HIDE_CONTROL);
        }
        isHidePlayControl = false;
        rlController.setVisibility(View.VISIBLE);
        if (isStartHideControl) {
            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL, DELAY_TIME);
        }
    }

    /**
     * 增加seekbar 触摸高度
     */
    private final View.OnTouchListener seekBarHeight = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Rect seekRect = new Rect();
            sbTime.getHitRect(seekRect);
            if ((event.getY() >= (seekRect.top - 300)) && (event.getY() <= (seekRect.bottom + 300))) {
                float y = seekRect.top + seekRect.height() / 2f;
                //seekBar only accept relative x
                float x = event.getX() - seekRect.left;
                if (x < 0) {
                    x = 0;
                } else if (x > seekRect.width()) {
                    x = seekRect.width();
                }
                MotionEvent me = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
                        event.getAction(), x, y, event.getMetaState());
                return sbTime.onTouchEvent(me);
            }
            return false;
        }
    };

    /**
     * 左侧上下滑动 亮度
     * 右侧上下滑动 声音
     * 左右滑动 进度预览
     */
    private final View.OnTouchListener rootViewTouch = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //行车视频弹窗弹出后，屏蔽外部所有点击、滑动时间
            int pointerCount = event.getPointerCount();
//            Log.d(TAG, "onTouch: pointerCount = " + pointerCount + "  event = " + event);
            //仅处理单指触控场景，多指触控不处理
            if (pointerCount == 1) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onMotionUp();
                }
//                Log.d(TAG, "onTouch: to mGestureDetector");
                return mGestureDetector.onTouchEvent(event);
            } else {
                Log.d(TAG, "onTouch: pointerCount : " + pointerCount);
            }
            return true;
        }
    };

    /**
     * 手指抬起需要做的事情
     */
    private void onMotionUp() {
        isTouchStart = false;
        llSettingAdjust.setVisibility(View.GONE);
        // 隐藏预览
        hidePreview();

        //手指还在屏幕上，不做隐藏控制按钮的逻辑
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL, DELAY_TIME);
        Log.d(TAG, "onMotionUp: isChangedProgress " + isChangedProgress + ",isSkipNextPosition " + isSkipNextPosition);
        if (isChangedProgress) {
            // 当播放损坏视频时，滑动屏幕调节，就不设置下去
            if (!isSkipNextPosition /*&& !mHandler.hasMessages(OTHER_ERROR)*/) {
                Log.d(TAG, "onMotionUp: seekTo " + sbTime.getProgress() + " mCurProgress = " + mCurProgress);
//                videoPlayPresenter.seekTo(sbPlayProgress.getProgress(), true);
                getVideoControl().processCommand(SEEKTO, ChangeReasonData.CLICK, mCurProgress);
            } else {
                isSkipNextPosition = false;
            }
            isChangedProgress = false;
//            backgroundAlpha(1.0f);
        }
        if (isChangedBrightness) {
            Log.d(TAG, "onMotionUp: isChangedBrightness to save ");
            // 保存亮度
            MediaVideoConstantUtils.getInstance().saveScreenBacklight(mCurBrightness);
        }
        if (isChangedVolume) {
            // 数据埋点
            int current = carManager.getGroupVolume(volumeGroupIdMedia);
            Log.d(TAG, "onMotionUp: isChangedVolume to save LionPoint; current: " + current);
            LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VOLUME_SET, LionPointSdkUtil.FIELD_VOLUME_RESULT, String.valueOf(current));
        }
        isChangedVolume = false;
        isChangedBrightness = false;
    }

    /**
     * 屏幕滑动
     */
    private final GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent event) {
            //按下
            Log.d(TAG, "onDown: " + event);
            isTouchStart = true;
            mPosX = event.getX();
            mPosY = event.getY();
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            // 单指 点击
            Log.d(TAG, "onSingleTapUp: " + event);
            if (mHandler.hasMessages(HIDE_CONTROL)) {
                mHandler.removeMessages(HIDE_CONTROL);
            }
            if (!isHidePlayControl) {
                hidePlayControl();
            } else {
                showPlayControl(true);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            mCurPosY = event2.getY();
            mCurPosX = event2.getX();
            float absY = Math.abs(mPosY - mCurPosY);
            float absX = Math.abs(mPosX - mCurPosX);
            if (absY > Constant.MIN_MOVE_DISTANCE || isChangedBrightness || isChangedVolume) {
                //添加判断，保证同时仅进行一项调节，避免斜着滑动手势时，都弹出调节窗
                if (event1.getX() < rootView.getWidth() / 2f) {
                    if (isRtl) {
                        slideVolume();
                    } else {
                        slideBrightness();
                    }
                } else {
                    if (isRtl) {
                        slideBrightness();
                    } else {
                        slideVolume();
                    }
                }
                mPosY = event2.getY();
                mPosX = event2.getX();
            }
            if ((absY < absX && absX > Constant.EFFECT_SLIDE_DISTANCE) || isChangedProgress) {
                if (!isChangedBrightness && !isChangedVolume /*&& !mHandler.hasMessages(OTHER_ERROR)*/) {
                    // 如果当前满足弹出行车视频弹窗的条件，就不让他滑动快进
                    //滑动屏幕进行快进，快退
                    if (isHideSpeedTips()) {
                        progressAdjustment();
                    }
                }
                mPosY = event2.getY();
                mPosX = event2.getX();
            }
            return false;
        }
    };

    /**
     * 调整亮度
     */
    private void adjustBrightness() {
        Log.d(TAG, "adjustBrightness mCurBrightness : " + mCurBrightness + " , mMaxBrightness : " + mMaxBrightness);
        isChangedBrightness = true;
        llSettingAdjust.setVisibility(View.VISIBLE);
        float increment = rootView.getHeight() * 1.0f / mMaxBrightness;
        Log.d(TAG, "adjustBrightness increment : " + increment);
        float offset = (mPosY - mCurPosY) / increment;
        Log.d(TAG, "adjustBrightness offset : " + offset);
        int round = Math.round(offset);
        Log.d(TAG, "adjustBrightness onScroll round:" + round + ",mCurBrightness:" + mCurBrightness);
        mCurBrightness = round + mCurBrightness;
        if (mCurBrightness >= Constant.VIDEO_PLAY_ALPHA_MAX) {
            mCurBrightness = Constant.VIDEO_PLAY_ALPHA_MAX;
        }
        if (mCurBrightness <= 0) {
            mCurBrightness = 0;
        }
        setBrightness();
    }

    /**
     * 设置亮度
     */
    private void setBrightness() {
        //修改当前亮度
        if (mCurBrightness <= Constant.VIDEO_PLAY_ALPHA_MIN) {
            rootView.setAlpha(Constant.VIDEO_PLAY_ALPHA_MIN * 1.0f / Constant.VIDEO_PLAY_ALPHA_MAX);
        } else {
            rootView.setAlpha(mCurBrightness * 1.0f / Constant.VIDEO_PLAY_ALPHA_MAX);
        }
    }

    /**
     * 调整亮度
     */
    private void slideBrightness() {
        //调整亮度
        if (!isChangedProgress && !isChangedVolume) {
            pbAdjustLightnessOrVolume.setMax(mMaxBrightness);
            pbAdjustLightnessOrVolume.setProgress(mCurBrightness);
            ivAdjustIcon.setImageResource(R.mipmap.adjust_light);
            adjustBrightness();
        }
    }

    /**
     * 调整音量
     */
    private void slideVolume() {
        //调整媒体音量
        if (!isChangedBrightness && !isChangedProgress) {
            // 每次调整音量时，需要先同步一下当前系统的音量
            updateMediaVolume();
            pbAdjustLightnessOrVolume.setMax(mMaxMediaVolume);
            pbAdjustLightnessOrVolume.setProgress(mCurMediaVolume);
            ivAdjustIcon.setImageResource(R.drawable.selector_adjust_voice);
            adjustMediaVolume();
        }
    }

    /**
     * 调整媒体音量
     */
    private void adjustMediaVolume() {
        Log.d(TAG, "adjustMediaVolume mMaxMediaVolume : " + mMaxMediaVolume + " , mCurMediaVolume : " + mCurMediaVolume);
        isChangedVolume = true;
        llSettingAdjust.setVisibility(View.VISIBLE);
        float increment = rootView.getHeight() * 1.0f / mMaxMediaVolume;
        Log.d(TAG, "adjustMediaVolume increment : " + increment);
        float offset = (mPosY - mCurPosY) / increment;
        Log.d(TAG, "adjustMediaVolume offset : " + offset);
        mCurMediaVolume = Math.round(offset) + mCurMediaVolume;
        if (mCurMediaVolume >= mMaxMediaVolume) {
            mCurMediaVolume = mMaxMediaVolume;
        }
        if (mCurMediaVolume <= 0) {
            mCurMediaVolume = 0;
        }
        int value = mCurMediaVolume / Constant.VOLUME_MULTIPLE;

        Log.d(TAG, "adjustMediaVolume: set " + value);
        carManager.setGroupVolume(volumeGroupIdMedia, value, 0);
    }

    /**
     * 滑动屏幕进行快进，快退
     */
    private void progressAdjustment() {
        isChangedProgress = true;
        hidePlayControl();
        showPreview();
        boolean tempRtl = ViewCompat.getLayoutDirection(sbTime) == ViewCompat.LAYOUT_DIRECTION_RTL;
        Log.d(TAG, "progressAdjustment mCurProgress : " + mCurProgress + ",mDuration : " + mDuration + " , tempRtl: " + tempRtl);
        float increment = rootView.getWidth() * 1.0f / mDuration;
        Log.d(TAG, "progressAdjustment increment ：" + increment);
        float offset = (mCurPosX - mPosX) / increment;
        Log.d(TAG, "progressAdjustment offset : " + offset);
        // 判断少于1000ms 时，增加或减少 1000
        if (offset < 0 && offset > -1000) {
            offset = offset - 1000;
        } else if (offset < 1000) {
            offset = offset + 1000;
        }
        Log.d(TAG, "progressAdjustment: reset offset : " + offset);
        int round = Math.round(offset);
        Log.d(TAG, "progressAdjustment: round : " + round);
        // 增加判断，当前是否右舵
        if (tempRtl) {
            mCurProgress = mCurProgress - round;
        } else {
            mCurProgress = round + mCurProgress;
        }
        if (mCurProgress >= mDuration) {
            mCurProgress = mDuration;
        }
        if (mCurProgress <= 0) {
            mCurProgress = 0;
        }
        mHandler.removeMessages(UPDATE_VIDEO_PROGRESS_IMAGE);
        mHandler.sendMessage(mHandler.obtainMessage(UPDATE_VIDEO_PROGRESS_IMAGE, mCurProgress));
        Log.d(TAG, "progressAdjustment: mCurProgress = " + mCurProgress);
    }

    /**
     * 显示预览进度图
     */
    private void showPreview() {
        if (View.GONE != llPreview.getVisibility()) {
            return;
        }
        isSeekBarBeingTouch = true;
        // 隐藏进度条时间
        tvTime.setVisibility(View.GONE);
        // 显示VIEW
        llPreview.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏预览进度图
     */
    private void hidePreview() {
        if (View.VISIBLE != llPreview.getVisibility()) {
            return;
        }
        isSeekBarBeingTouch = false;
        // 显示进度条时间
        tvTime.setVisibility(View.VISIBLE);
        // 隐藏预览VIEW
        llPreview.setVisibility(View.GONE);
    }

    private boolean isT19Fl() {
        return CarConfigUtil.getDefault().isT19FL_INT() || CarConfigUtil.getDefault().isT19FL_INT_HEV();
    }

    private void bindVDS() {
        Log.d(TAG, "bindVDS: ");
        VDBus.getDefault().registerVDBindListener(vdBindListener);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VEHICLE_HAL);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.SMS);
        //T19FL项目倒档的时候不会抢焦点
        if (isT19Fl) {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.RVC);
        }
    }

    private void unBindVDS() {
        Log.d(TAG, "unBindVDS: ");
        // 解绑
        VDBus.getDefault().removeSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED);
        VDBus.getDefault().subscribeCommit();
        VDBus.getDefault().unregisterVDNotifyListener(vdNotifyListener);

        if (null != keyCodeVDEvent) {
            Log.d(TAG, "unBindVDS: unsubscribe keyCodeVDEvent");
            VDBus.getDefault().unsubscribe(keyCodeVDEvent, keyCodeEventStub);
        }

        VDBus.getDefault().unregisterVDBindListener(vdBindListener);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.VEHICLE_HAL);
        VDBus.getDefault().unbindService(VDServiceDef.ServiceType.SMS);
        if (isT19Fl) {
            VDBus.getDefault().unbindService(VDServiceDef.ServiceType.RVC);
        }
    }

    /**
     * 获取硬按键注册event
     *
     * @return VDEvent
     */
    private VDEvent getKeyCodeType() {
        Bundle payload = new Bundle();
        //设置需要监听的按键，在第二个参数：int类型数组，可以监听一个或者多个按键，按键取值详见vdbus-javadoc(附录)
        payload.putIntArray(VDKey.TYPE, new int[]{VDValueKey.KEYCODE_MEDIA_PREVIOUS, VDValueKey.KEYCODE_MEDIA_NEXT,
                VDValueKey.KEYCODE_PLAY, VDValueKey.KEYCODE_PAUSE, VDValueKey.KEYCODE_PLAY_OR_PAUSE});
        keyCodeVDEvent = new VDEvent(VDEventSms.ID_SMS_KEY_EVENT, payload);
        return keyCodeVDEvent;
    }

    private final VDBindListener vdBindListener = new VDBindListener() {
        @Override
        public void onVDConnected(VDServiceDef.ServiceType serviceType) {
            Log.d(TAG, "onVDConnected: serviceType " + serviceType);
            if (!isConnect()) {
                Log.e(TAG, "onVDConnected: The U flash drive is not connected");
                return;
            }
            //只有绑定上了对应模块的VDS，才能进行订阅
            if (serviceType == VDServiceDef.ServiceType.VEHICLE_HAL) {
                // 注册消息、绑定监听
                VDBus.getDefault().addSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED);

                VDBus.getDefault().subscribeCommit();
                VDBus.getDefault().registerVDNotifyListener(vdNotifyListener);
            } else if (serviceType == VDServiceDef.ServiceType.SMS) {
                // 硬按键
                VDBus.getDefault().subscribe(getKeyCodeType(), keyCodeEventStub);
            } else if (serviceType == VDServiceDef.ServiceType.RVC) {
                // 倒车和360
                VDBus.getDefault().addSubscribe(VDEventRvc.RVC_STATUS);
                VDBus.getDefault().addSubscribe(VDEventRvc.AVM_STATUS);

                VDBus.getDefault().subscribeCommit();
                VDBus.getDefault().registerVDNotifyListener(vdNotifyListener);
            }
        }

        @Override
        public void onVDDisconnected(VDServiceDef.ServiceType serviceType) {
            Log.d(TAG, "onVDDisconnected: " + serviceType);
        }
    };

    /**
     * 车速、硬按键控制
     */
    private final VDNotifyListener vdNotifyListener = new VDNotifyListener() {
        @Override
        public void onVDNotify(VDEvent vdEvent, int threadType) {
            Log.d(TAG, "VDB_onVDNotify: " + vdEvent + ", threadType: " + threadType);
            if (null == vdEvent || null == vdEvent.getPayload()) {
                // 服务未绑定，会返回null
                return;
            }
            int id = vdEvent.getId();
            Bundle payload = vdEvent.getPayload();
            switch (id) {
                case VDEventVehicleHal.PERF_VEHICLE_SPEED:
                    // 车速 行车视频弹框
                    speedTips(payload);
                    break;
                case VDEventSms.ID_SMS_KEY_EVENT:
                    // 硬按键
                    keyCodeVDEvent(payload);
                    break;
                case VDEventRvc.RVC_STATUS:
                case VDEventRvc.AVM_STATUS:
                    // RVC
                    rvcOrAvmVDEvent(payload);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 车速限速操作
     *
     * @param payload payload
     */
    private void speedTips(Bundle payload) {
        // 判断系统，行车视频开关
        if (!Constant.isOpenLimiter(this)) {
            Log.w(TAG, "speedTips: current limiter status is not open");
            if (videoSpeedTipsPopupWindow != null && videoSpeedTipsPopupWindow.isShowing()) {
                Log.w(TAG, "speedTips: videoSpeedTipsPopupWindow isShowing");
                videoSpeedTipsPopupWindow.dismiss();
            }
            return;
        }
        //判断当前速度是否大于限制值
        boolean isOverSpeed = isOverSpeed(payload);
        Log.d(TAG, "speedTips: isOverSpeed: " + isOverSpeed + ", playWithoutPrompting: " + playWithoutPrompting);
        if (isOverSpeed) {
            if (playWithoutPrompting) {
                Log.w(TAG, "speedTips: playWithoutPrompting , Don't show speed window is clicked over");
                return;
            }
            // 避免弹窗截图的背景图过快，可能截的是上个页面，这里做延时250毫秒
            mHandler.removeMessages(UPDATE_VIDEO_SPEED_SHOW);
            mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_SPEED_SHOW, UPDATE_VIDEO_SPEED_SHOW_DELAY_TIME);
            Log.d(TAG, "speedTips: show");
        } else {
            boolean hideSpeedTips = isHideSpeedTips();
            Log.d(TAG, "speedTips: hideSpeedTips: " + hideSpeedTips);
            // 判断当前行车视频，是否 显示，显示才需要做对应的操作
            if (!hideSpeedTips) {
                boolean hasSpeedHideMessage = mHandler.hasMessages(UPDATE_VIDEO_SPEED_HIDE);
                Log.d(TAG, "speedTips: hasSpeedHideMessage: " + hasSpeedHideMessage);
                if (!hasSpeedHideMessage) {
                    Log.w(TAG, "speedTips: start hide countdown " + SPEED_DELAY_TIME + " ms");
                    mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_SPEED_HIDE, SPEED_DELAY_TIME);
                }
            }
        }
        Log.d(TAG, "speedTips: end");
    }

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
        return gearboxStatus == DISPLAY_P;
    }

    /**
     * 判断，当前是否需要显示行车视频
     * 根据当前速度是否大于限制值 或者 挡位是否在P/N挡
     *
     * @param payload payload
     * @return true: 大于限制值 或者 挡位在R/D挡 ；false: 小于限制值 或者 挡位不在R/D挡
     */
    private boolean isOverSpeed(Bundle payload) {
        double[] data = payload.getDoubleArray(VDKeyVehicleHal.DOUBLE_VECTOR);
        Log.d(TAG, "isOverSpeed: data " + Arrays.toString(data));
        if (data.length > 0) {
            float speed = (float) (data[0] * 3.6f);
            Log.d(TAG, "isOverSpeed: original sp2023-11-7eed " + speed);
            //底层上传的车速都是整形，但是因为存在转换误差，所以需要进行四舍五入转化
            speed = Math.round(speed);
            Log.d(TAG, "isOverSpeed: processed speed " + speed + " , Constant.RUNNING_SPEED: " + Constant.RUNNING_SPEED);
            boolean isGreaterThanSpeed = (int) speed > Constant.RUNNING_SPEED;
            Log.d(TAG, "isOverSpeed: isGreaterThanSpeed: " + isGreaterThanSpeed+" ; getCountryCode "+Constant.getCountryCode());
            if (Constant.getCountryCode() == Constant.AUSTRALIA_COUNTRY_CODE
                    || Constant.getCountryCode() == Constant.NZ_COUNTRY_CODE) {
                //澳大利亚和新西兰，只要非P档,则触发限速禁播
                return !isP();
            } else {
                return isGreaterThanSpeed;
            }
        } else {
            Log.d(TAG, "isOverSpeed: data.length < 0 ");
            return false;
        }
    }

    /**
     * 当前是否 隐藏 新车提示
     *
     * @return false show, true hide
     */
    private boolean isHideSpeedTips() {
        boolean isHide = true;
        if (null != videoSpeedTipsPopupWindow) {
            isHide = !videoSpeedTipsPopupWindow.isShowing();
        }
        Log.i(TAG, "isHideSpeedTips: speed window isHide: " + isHide);
        return isHide;
    }
    private boolean isMediaPlyerPaused() {
        boolean isPlaying = false;
        if (null !=  getVideoStatusTool()) {
            isPlaying = getVideoStatusTool().isPlaying();
        }
        Log.i(TAG, "isMediaPlyerPaused:  isPlaying = " + isPlaying);
        return isPlaying;
    }
    /**
     * 显示行车视频弹框
     */
    private void showSpeedTipsPopupWindow() {
        Log.d(TAG, "showSpeedTipsPopupWindow: " + videoSpeedTipsPopupWindow);
        if (null == videoSpeedTipsPopupWindow) {
            videoSpeedTipsPopupWindow = new VideoSpeedTipsPopupWindow(this, speedOnclickListener);
        }
        videoSpeedTipsPopupWindow.showPopupWindow();
        // 没有显示行车视频时，需要执行一次暂停
        //倒车如果先是焦点被抢占暂停，暂停原因是低优先级的SOURCE，此时视频已经暂停，后面会因为抢到焦点自动播放
        //暂停时如果暂停优先级较低，需要更新暂停原因为OVER_SPEED
        if (isMediaPlyerPaused() || ChangeReasonData.OVER_SPEED.getPrority() < CurrentPlayInfo.getInstance(getMediaType()).getPauseReason().getPrority()) {
            Log.d(TAG, "showSpeedTipsPopupWindow: to pause");
            getVideoControl().processCommand(PAUSE, ChangeReasonData.OVER_SPEED);
        }
        Log.d(TAG, "showSpeedTipsPopupWindow: end");
    }

    /**
     * 隐藏行车视频弹框
     */
    private void hideSpeedTipsPopupWindow() {
        Log.d(TAG, "hideSpeedTipsPopupWindow: " + videoSpeedTipsPopupWindow);
        if (null != videoSpeedTipsPopupWindow) {
            videoSpeedTipsPopupWindow.dismiss();
            Log.d(TAG, "hideSpeedTipsPopupWindow: to pause");
            getVideoControl().processCommand(PAUSE, ChangeReasonData.OVER_SPEED);
        }
        Log.d(TAG, "hideSpeedTipsPopupWindow: end");
    }

    /**
     * 行车安全点击事件
     */
    private final VideoSpeedTipsPopupWindow.OnClickListener speedOnclickListener = new VideoSpeedTipsPopupWindow.OnClickListener() {
        @Override
        public void onToPlayClick() {
            Log.d(TAG, "onToPlayClick: ");
            playWithoutPrompting = true;
            // 清除倒计时
            mHandler.removeMessages(UPDATE_VIDEO_SPEED_HIDE);
            mHandler.removeMessages(UPDATE_VIDEO_SPEED_SHOW);
            Log.d(TAG, "onToPlayClick: remove UPDATE_VIDEO_SPEED_HIDE update by bug play wrong item");
//                getVideoControl().processCommand(START, ChangeReasonData.CLICK);
            //这里发生的原因是 没有走OPEN 操作，直接走了START，导致播放会走上一个，这里补丁使用open解决该问题
            autoStart();
        }

        @Override
        public void onCloseClick() {
            Log.e(TAG, "onCloseClick: FINISH");
            mHandler.removeMessages(UPDATE_VIDEO_SPEED_SHOW);
            BaseVideoPlayActivity.this.finish();
        }
    };

    /**
     * 硬按键
     */
    private final IVDBusNotify.Stub keyCodeEventStub = new IVDBusNotify.Stub() {
        @Override
        public void onVDBusNotify(VDEvent vdEvent) {
            Log.d(TAG, "VDB_onVDBusNotify: vdEvent = " + vdEvent);
            vdNotifyListener.onVDNotify(vdEvent, VDThreadType.MAIN_THREAD);
        }
    };

    /**
     * 硬按键操作
     *
     * @param payload payload
     */
    private void keyCodeVDEvent(Bundle payload) {
        // 硬按键
        /* 描述详见vdbus-javadoc(附录) {@link com.desaysv.ivi.vdb.event.id.sms.VDValueKey} */
        int keycode = payload.getInt(VDKey.TYPE);
        /* 描述详见vdbus-javadoc(附录) {@link com.desaysv.ivi.vdb.event.id.sms.VDValueAction} */
        int keyAction = payload.getInt(VDKey.ACTION);
        Log.d(TAG, "keyCodeEvent: keycode = " + keycode + " keyAction = " + keyAction);
        if (keyAction == VDValueAction.ACTION_RELEASE) {
            // 动作释放
            Log.d(TAG, "keyCodeEvent: ACTION_RELEASE ");
            switch (keycode) {
                case VDValueKey.KEYCODE_MEDIA_PREVIOUS:
                    lionPointControl(PRE);
                    getVideoControl().processCommand(PRE, ChangeReasonData.HARD_KEY);
                    break;
                case VDValueKey.KEYCODE_MEDIA_NEXT:
                    lionPointControl(NEXT);
                    getVideoControl().processCommand(NEXT, ChangeReasonData.HARD_KEY);
                    break;
                case VDValueKey.KEYCODE_PLAY:
                    lionPointControl(START);
                    getVideoControl().processCommand(START, ChangeReasonData.HARD_KEY);
                    break;
                case VDValueKey.KEYCODE_PAUSE:
                    lionPointControl(PAUSE);
                    getVideoControl().processCommand(PAUSE, ChangeReasonData.HARD_KEY);
                    break;
                case VDValueKey.KEYCODE_PLAY_OR_PAUSE:
                    lionPointControl(PLAY_OR_PAUSE);
                    getVideoControl().processCommand(PLAY_OR_PAUSE, ChangeReasonData.HARD_KEY);
                    break;
                default:
                    break;
            }
        } else if (keyAction == VDValueAction.ACTION_LONG_PRESS) {
            // 动作长按
            Log.d(TAG, "keyCodeEvent: ACTION_LONG_PRESS ");
        } else if (keyAction == VDValueAction.ACTION_LONG_RELEASE) {
            // 动作长按 释放
            Log.d(TAG, "keyCodeEvent: ACTION_LONG_RELEASE ");
        } else if (keyAction == VDValueAction.ACTION_SUPER_LONG_PRESS) {
            // 动作超长压机
            Log.d(TAG, "keyCodeEvent: ACTION_SUPER_LONG_PRESS ");
        }
    }

    /**
     * 倒车 or 360
     *
     * @param payload payload
     */
    private void rvcOrAvmVDEvent(Bundle payload) {
        int type = payload.getInt(VDKey.TYPE);
        int status = payload.getInt(VDKey.STATUS);
        Log.i(TAG, "rvcOrAvmVDEvent: type: " + type + ",status: " + status);
        if (VDValueRvc.RvcStatus.RVC == status || VDValueRvc.AvmStatus.STARTUP == status) {
            // 进入RVC或360,只有在播放状态时，才需要进行暂停播放
            isPlaying = getVideoStatusTool().isPlaying();
            Log.i(TAG, "rvcOrAvmVDEvent: RVC or AVM; isPlaying: " + isPlaying);
            if (isPlaying) {
                lionPointControl(PAUSE);
                getVideoControl().processCommand(PAUSE, ChangeReasonData.SOURCE);
            }
        } else {
            // 只有在进入RVC或360在播放时，才需要退出RVC或360后执行播放
            Log.i(TAG, "rvcOrAvmVDEvent: isPlaying: " + isPlaying);
            if (isPlaying) {
                lionPointControl(START);
                getVideoControl().processCommand(START, ChangeReasonData.SOURCE);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ModuleUSBVideoTrigger.getInstance().baseVideoPlayActivity = this;
        registerVideoStatusChangeListener(iMediaStatusChange);

        bindVDS();
        if (null != currentPath && !currentPath.isEmpty()) {
            Log.d(TAG, "onStart: to start video ; currentPath: " + currentPath);
            if(!Constant.isOpenLimiter(this)){
                getVideoControl().processCommand(START, ChangeReasonData.UI_START);
            }else{
                VDEvent event = VDBus.getDefault().getOnce(VDEventVehicleHal.PERF_VEHICLE_SPEED);
                Bundle payload = event.getPayload();
                Log.d(TAG, "onStartOverSpeed: payload = "+payload);
                //如果之前用户允许播放过，则当前界面周期内直接播放起来
                if(playWithoutPrompting || !isOverSpeed(payload)){
                    getVideoControl().processCommand(START, ChangeReasonData.UI_START);
                    if (videoSpeedTipsPopupWindow != null && videoSpeedTipsPopupWindow.isShowing()) {
                        Log.w(TAG, "onStart: videoSpeedTipsPopupWindow isShowing, do dismiss");
                        videoSpeedTipsPopupWindow.dismiss();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnect()) {
            Log.e(TAG, "onResume: not U is finish");
            finish();
            return;
        }
        dealIntent();
        VDEvent event = VDBus.getDefault().getOnce(VDEventVehicleHal.PERF_VEHICLE_SPEED);
        Bundle payload = event.getPayload();
        Log.d(TAG, "onResumeOverSpeed: payload = "+payload);
        // 判断系统，行车视频开关
        if(!Constant.isOpenLimiter(this)){
            autoStart();
        }else{
            //如果之前用户允许播放过，则当前界面周期内直接播放起来
            if(playWithoutPrompting || !isOverSpeed(payload)){
                autoStart();
            } else {
                Log.d(TAG, "onResumeOverSpeed: playWithoutPrompting&&isOverSpeed() = false");
                MediaPlayStatusSaveUtils.getInstance().saveMediaPlayPath(getMediaType(), currentPath);
                setPlayList();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //播放时切到后台onStop()里PAUSE时注销了iMediaStatusChange监听（播放按钮状态Icon还是播放状态），
        //再次进入行车视频PAUSE时不会再次更新Icon状态，这里主动设置Icon状态
        //onDestroy里面注销监听？
        updatePlayStatus();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged: hasFocus: " + hasFocus);
        // 当前不需要主动获取速度去弹框
        // 等待activity ，生命周期跑完后，在进行界面显示逻辑 
        // 主动获取行车锁屏；现行进行判断当前速度是显示还是隐藏；
        // 然后根据显示隐藏来判断，当前是否可以进行播放 ；隐藏则可以进行播放，显示，则不进行播放
        /*if (hasFocus) {
            VDEvent vehicleSpeed = VDBus.getDefault().getOnce(VDEventVehicleHal.PERF_VEHICLE_SPEED);
            Log.d(TAG, "onWindowFocusChanged: vehicleSpeed: " + vehicleSpeed);
            vdNotifyListener.onVDNotify(vehicleSpeed, VDThreadType.MAIN_THREAD);
        }*/
    }

    //切换白天黑夜模式，避免重新加载页面（如果重新加载mediaplayer没播放过直接seek还是会显示黑色）
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: ");
        int uiMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (uiMode != mCurrentUiMode) {
            Log.i(TAG, "onConfigurationChanged: uiMode changed");
            mCurrentUiMode = uiMode;
            resetUIColorDrawable();
            //如果显示过列表弹窗 弹窗需要重新加载才会正确显示颜色
            if(videoPlayWindow != null){
                if(videoPlayWindow.isShowing()){
                    videoPlayWindow.dismiss();
                }
                videoPlayWindow = null;
            }
        }
    }

    /**
     * 设置页面颜色和背景
     * color为@android:color/white的暂没有设置
     */
    private void resetUIColorDrawable(){
        noVideoShow.setTextColor(ContextCompat.getColor(AppBase.mContext, R.color.usbvideo_white));
        errorShow.setTextColor(ContextCompat.getColor(AppBase.mContext, R.color.usbvideo_white));

        videoName.setTextColor(ContextCompat.getColor(AppBase.mContext, R.color.usbvideo_white));

        Rect bounds = sbTime.getProgressDrawable().getBounds();
        sbTime.setProgressDrawable(ContextCompat.getDrawable(AppBase.mContext, R.drawable.progress_main));
        sbTime.getProgressDrawable().setBounds(bounds);

        tvTime.setTextColor(ContextCompat.getColor(AppBase.mContext, R.color.usbvideo_white));
        llSettingAdjust.setBackgroundResource(R.drawable.shape_slide_bg);

        Rect boundsPd = pbAdjustLightnessOrVolume.getProgressDrawable().getBounds();
        pbAdjustLightnessOrVolume.setProgressDrawable(ContextCompat.getDrawable(AppBase.mContext, R.drawable.progress_main));
        pbAdjustLightnessOrVolume.getProgressDrawable().setBounds(boundsPd);

        llCurTotalTime.setBackgroundResource(R.drawable.shape_pre_view_bg);
        tvPreViewTime.setTextColor(ContextCompat.getColor(AppBase.mContext, R.color.usbvideo_white));
        tvPreViewTotal.setTextColor(ContextCompat.getColor(AppBase.mContext, R.color.usbvideo_common_gray));
        Log.i(TAG, "resetUIColorDrawable: end");
    }

    private void dealIntent() {
        intent = getIntent();
        Log.i(TAG, "dealIntent: to intent: " + intent);
        if (intent != null) {
            isNeedToAutoPlay = intent.getBooleanExtra(IS_NEED_TO_AUTO_PLAY, true);
            if (intent.hasExtra(AUTO_PLAY_PATH)) {
                String currentPathTemp = intent.getStringExtra(AUTO_PLAY_PATH);
                Log.i(TAG, "dealIntent: currentPathTemp: " + currentPathTemp);
                if (null != currentPathTemp && !currentPathTemp.isEmpty()) {
                    currentPath = currentPathTemp;
                    Log.i(TAG, "dealIntent: to set " + currentPath);
                }
            }
            removeIntentData();
        } else {
            isNeedToAutoPlay = false;
        }
        Log.d(TAG, "dealIntent: isNeedToAutoPlay = " + isNeedToAutoPlay + " currentPath = " + currentPath);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ModuleUSBVideoTrigger.getInstance().baseVideoPlayActivity = null;
        getVideoControl().processCommand(PAUSE, ChangeReasonData.UI_FINISH);
        mHandler.removeCallbacksAndMessages(null);
        unregisterVideoStatusChangeListener(iMediaStatusChange);
        unBindVDS();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 强制设置为，允许拉起系统底部操作栏
        Settings.Global.putInt(getContentResolver(), "systemui_swipe_show_enable", 1);
        getVideoControl().processCommand(STOP, ChangeReasonData.UI_FINISH);
        if (mVideoThumbnailsIv != null) {
            mVideoThumbnailsIv.release();
        }
        dismissPlayList();
        if (null != videoSpeedWindow) {
            Log.i(TAG, "onDestroy: videoSpeedWindow.dismiss()");
            videoSpeedWindow.dismiss();
        }
        if (null != videoSpeedTipsPopupWindow) {
            Log.i(TAG, "onDestroy: videoSpeedTipsPopupWindow.dismiss()");
            videoSpeedTipsPopupWindow.dismiss();
        }
    }

    private void removeIntentData() {
        // 解决页面因切换主题重载，intent内的数据为清空导致切换了视频内容，重载后还原了。
        if (null != intent) {
            if (intent.hasExtra(IS_NEED_TO_AUTO_PLAY)) {
                Log.i(TAG, "removeIntentData: removeExtra  IS_NEED_TO_AUTO_PLAY");
                intent.removeExtra(IS_NEED_TO_AUTO_PLAY);
            }
            if (intent.hasExtra(AUTO_PLAY_PATH)) {
                Log.i(TAG, "removeIntentData: removeExtra  AUTO_PLAY_PATH");
                intent.removeExtra(AUTO_PLAY_PATH);
            }
        }
    }

    /**
     * 显示播放列表
     */
    public void showPlayList() {
        Log.i(TAG, "showPlayList: videoPlayWindow: " + videoPlayWindow);
        if (null == videoPlayWindow) {
            videoPlayWindow = new VideoPlayListPopupWindow(BaseVideoPlayActivity.this, getVideoControl(), getVideoStatusTool(), getMediaType());
        }
        videoPlayWindow.showPopupWindow();
    }

    /**
     * 退出播放列表
     */
    public void dismissPlayList() {
        Log.i(TAG, "dismissPlayList: videoPlayWindow: " + videoPlayWindow);
        if (null != videoPlayWindow) {
            videoPlayWindow.dismiss();
        }
    }

    /**
     * 自动播放 ？
     */
    private void autoStart() {
        Log.d(TAG, "autoStart: ");
        autoPlay();
        // 隐藏控制栏
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL, DELAY_TIME);
    }

    /**
     * 更新预览图
     *
     * @param progress progress
     */
    @SuppressLint("SetTextI18n")
    private void updatePreview(int progress) {
        Log.d(TAG, "updatePreview: progress = " + progress);
        mVideoThumbnailsIv.setSeekToImage(progress, currentPath);
        tvPreViewTime.setText(TimeUtils.longToTimeStr(progress));
        tvPreViewTotal.setText(TimeUtils.longToTimeStr(mDuration));
    }

    /**
     * 更新音量信息
     */
    public void updateMediaVolume() {
        if (isChangedVolume) {
            return;
        }
        int current = carManager.getGroupVolume(volumeGroupIdMedia);
        int maxVolume = carManager.getGroupMaxVolume(volumeGroupIdMedia);
        Log.d(TAG, "updateMediaVolume current:" + current + ",maxVolume:" + maxVolume);
        mCurMediaVolume = current * Constant.VOLUME_MULTIPLE;
        mMaxMediaVolume = maxVolume * Constant.VOLUME_MULTIPLE;
    }

    /**
     * 更新ID3信息
     */
    private void updateID3Info() {
        FileMessage fileMessage = getVideoStatusTool().getCurrentPlayItem();
        Log.d(TAG, "updateID3Info: fileMessage = " + fileMessage);
        String fileName = fileMessage.getFileName();
        videoName.setText(fileName);
        // 更新当前地址
        currentPath = fileMessage.getPath();
        // 重置 预览加载
        mVideoThumbnailsIv.setPath(currentPath);
        Log.i(TAG, "updateID3Info: reset currentPath: " + currentPath);
        // 数据埋点
        LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VIDEO_OPERATE, LionPointSdkUtil.FIELD_VIDEO_NAME, fileName);
    }

    /**
     * 更新播放状态
     */
    private void updatePlayStatus() {
        boolean isPlaying = getVideoStatusTool().isPlaying();
        mIsPlaying = isPlaying;
        if (isPlaying) {
            btnPlayOrPause.setImageResource(R.drawable.usb_video_pause);
            // 更新播放进度，需要更新倍速显示
            if (currentSpeed != MediaControlAction.DEFAULT_SPEED) {
                tvDoubleSpeed.setText(USBVideoSpeedListAdapter.appendX(this, String.valueOf(currentSpeed)));
            } else {
                tvDoubleSpeed.setText(this.getString(R.string.double_speed));
            }
        } else {
            btnPlayOrPause.setImageResource(R.drawable.usb_video_play);
        }
    }

    /**
     * 更新播放进度时间
     */
    @SuppressLint("SetTextI18n")
    private void updatePlayTime() {
        if (getVideoStatusTool().isPlaying() && !isChangedProgress) {
            int playTime = getVideoStatusTool().getCurrentPlayTime();
            int totalTime = getVideoStatusTool().getDuration();

//            Log.d(TAG, "updatePlayTime: playTime = " + playTime + " totalTime = " + totalTime);
            String format = String.format(getResources().getString(R.string.media_video_play_time),
                    TimeUtils.longToTimeStr(playTime), TimeUtils.longToTimeStr(totalTime));
//            tvTime.setText(TimeUtils.longToTimeStr(playTime) + " / " + TimeUtils.longToTimeStr(totalTime));
            tvTime.setText(format);
            if (!isSeekBarBeingTouch) {
                mCurProgress = playTime;
                mDuration = totalTime;

                sbTime.setMax(totalTime);
                sbTime.setProgress(playTime);
            }
        }
    }

    private final CarAudioManager.CarVolumeCallback carVolumeCallback = new CarAudioManager.CarVolumeCallback() {
        @Override
        public void onGroupVolumeChanged(int zoneId, int groupId, int flags) {
            super.onGroupVolumeChanged(zoneId, groupId, flags);
            Log.i(TAG, "onGroupVolumeChanged: zoneId: " + zoneId + " , groupId: " + groupId + " , flags: " + flags);
            if (volumeGroupIdMedia == groupId) {
                updateMediaVolume();
            }
        }
    };

    /**
     * 进度条监听
     */
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            mHandler.removeMessages(UPDATE_VIDEO_PROGRESS_IMAGE);
            mHandler.sendMessage(mHandler.obtainMessage(UPDATE_VIDEO_PROGRESS_IMAGE, progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarBeingTouch = true;
            if (mHandler.hasMessages(HIDE_CONTROL)) {
                mHandler.removeMessages(HIDE_CONTROL);
            }
            showPreview();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarBeingTouch = false;
            if (mHandler.hasMessages(HIDE_CONTROL)) {
                mHandler.removeMessages(HIDE_CONTROL);
            }
            hidePreview();
            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL, DELAY_TIME);
            getVideoControl().processCommand(SEEKTO, ChangeReasonData.CLICK, seekBar.getProgress());
        }
    };

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int id = v.getId();
                if (id == R.id.btn_pre || id == R.id.btn_next) {
                    Log.d(TAG, "onTouch: MotionEvent.ACTION_UP");
                    showPlayControl(true);
                }
                if (id == R.id.btn_pre) {
                    getVideoControl().processCommand(REWIND_STOP, ChangeReasonData.CLICK);
                } else if (id == R.id.btn_next) {
                    getVideoControl().processCommand(FAST_FORWARD_STOP, ChangeReasonData.CLICK);
                }
            }
            return false;
        }
    };

    private final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_pre || id == R.id.btn_next) {
                Log.d(TAG, "onLongClick: ");
                showPlayControl(false);
            }
            if (id == R.id.btn_pre) {
                getVideoControl().processCommand(REWIND, ChangeReasonData.CLICK);
            } else if (id == R.id.btn_next) {
                getVideoControl().processCommand(FAST_FORWARD, ChangeReasonData.CLICK);
            }
            return true;
        }
    };


    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ll_video_play_top) {
                Log.i(TAG, "onClick: ll_video_play_top FINISH");
                finish();
            } else {
                Log.d(TAG, "onClick: " + id);
                showPlayControl(true);
                if (id == R.id.btn_pre) {
                    Log.d(TAG, "onClick: btn_pre");
                    lionPointControl(PRE);
                    getVideoControl().processCommand(PRE, ChangeReasonData.CLICK);
                } else if (id == R.id.btn_play_or_pause) {
                    Log.d(TAG, "onClick: btn_play_or_pause");
                    lionPointControl(PLAY_OR_PAUSE);
                    getVideoControl().processCommand(PLAY_OR_PAUSE, ChangeReasonData.CLICK);
                } else if (id == R.id.btn_next) {
                    Log.d(TAG, "onClick: btn_next");
                    lionPointControl(NEXT);
                    getVideoControl().processCommand(NEXT, ChangeReasonData.CLICK);
                } else if (id == R.id.tv_double_speed) {
                    Log.d(TAG, "onClick: tvDoubleSpeed");
                    // 倍速
                    if (null == videoSpeedWindow) {
                        videoSpeedWindow = new VideoSpeedListPopupWindow(BaseVideoPlayActivity.this);
                    }
                    initSpeedListAdapter();
                    videoSpeedWindow.showPopupWindow(usbVideoSpeedListAdapter);
                } else if (id == R.id.tv_anthology) {
                    Log.d(TAG, "onClick: tvAnthology");
                    // 选集
                    showPlayList();
                }
            }

        }
    };

    /**
     * 数据埋点
     *
     * @param action action
     */
    private void lionPointControl(MediaAction action) {
        Log.i(TAG, "lionPointControl: " + action);
        switch (action) {
            case PLAY_OR_PAUSE:
                boolean playing = getVideoStatusTool().isPlaying();
                String field = LionPointSdkUtil.FIELD_PLAY;
                if (playing) {
                    field = LionPointSdkUtil.FIELD_PAUSE;
                }
                LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VIDEO_OPERATE, LionPointSdkUtil.FIELD_PLAY_OPER_TYPE, field);
                break;
            case START:
                LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VIDEO_OPERATE, LionPointSdkUtil.FIELD_PLAY_OPER_TYPE, LionPointSdkUtil.FIELD_PLAY);
                break;
            case PAUSE:
                LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VIDEO_OPERATE, LionPointSdkUtil.FIELD_PLAY_OPER_TYPE, LionPointSdkUtil.FIELD_PAUSE);
                break;
            case PRE:
                LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VIDEO_OPERATE, LionPointSdkUtil.FIELD_PLAY_OPER_TYPE, LionPointSdkUtil.FIELD_PRE);
                break;
            case NEXT:
                LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_VIDEO_OPERATE, LionPointSdkUtil.FIELD_PLAY_OPER_TYPE, LionPointSdkUtil.FIELD_NEXT);
                break;
            default:
                break;
        }
    }

    /**
     * 倍速 列表下的点击事件
     */
    private void initSpeedListAdapter() {
        if (null == usbVideoSpeedListAdapter) {
            usbVideoSpeedListAdapter = new USBVideoSpeedListAdapter(this, new USBVideoSpeedListAdapter.ItemClickListener() {
                @Override
                public void onItemClick(float data) {
                    currentSpeed = data;
                    // 设置倍速
                    Log.d(TAG, "onItemClick: data = " + data);
                    if (null != videoSpeedWindow) {
                        videoSpeedWindow.dismiss();
                    }
                    getVideoControl().setPlaySpeed(data);
                    getVideoControl().processCommand(START, ChangeReasonData.CLICK_ITEM);
                }
            });
        }
        usbVideoSpeedListAdapter.updateSpeed(currentSpeed);
    }


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable() called with: mMediaPlayer = [" + mMediaPlayer + "], surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");
            // 在视频显示帧前,设置视频尺寸监听；防止在来回切换主题导致视频大小没有设置
            mSurface = new Surface(surface);
            if (mMediaPlayer != null) {
                mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
                mMediaPlayer.setSurface(mSurface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final IMediaStatusChange iMediaStatusChange = new IMediaStatusChange() {
        @Override
        public void onMediaInfoChange() {
            Log.d(TAG, "onMediaInfoChange: ");
            mHandler.sendEmptyMessage(UPDATE_VIDEO_ID3_INFO);
            if (null != videoPlayWindow && videoPlayWindow.isShowing()) {
                videoPlayWindow.updateMessage();
            }
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            mHandler.sendEmptyMessage(UPDATE_VIDEO_PLAY_STATUS);
            if (null != videoPlayWindow && videoPlayWindow.isShowing()) {
                videoPlayWindow.updatePlayStatusMessage();
            }
        }

        @Override
        public void onPlayTimeChange(int currentPlayTime, int duration) {
            mHandler.sendEmptyMessage(UPDATE_VIDEO_PLAY_TIME);
        }

        @Override
        public void onMediaTypeChange(MediaPlayType mediaPlayType) {
            Log.d(TAG, "onMediaTypeChange: mediaPlayType = " + mediaPlayType);
            if (MediaPlayType.OPENING == mediaPlayType) {
                if (null != videoPlayWindow && videoPlayWindow.isShowing()) {
                    videoPlayWindow.updateLastMessage();
                }
            }
            if (MediaPlayType.NORMAL == mediaPlayType) {
                // 正常打开，显示内容
                // 隐藏错误提示语
                mHandler.sendEmptyMessage(UPDATE_VIDEO_ERROR_HIDE);
                // 隐藏没有视频内容提示语
                mHandler.sendEmptyMessage(UPDATE_VIDEO_SURFACE_HIDE);
                // 显示画面
                mHandler.sendEmptyMessage(UPDATE_VIDEO_SHOW);
            } else {
                // 错误，不需要显示画面
                // 隐藏画面
                mHandler.sendEmptyMessage(UPDATE_VIDEO_HIDE);
                // 没有视频内容提示
                mHandler.sendEmptyMessage(UPDATE_VIDEO_SURFACE_HIDE);
                // 视频错误提示
                mHandler.sendEmptyMessage(UPDATE_VIDEO_ERROR_HIDE);
                if (MediaPlayType.NO_VIDEO == mediaPlayType) {
                    // 没有视频内容提示
                    mHandler.sendEmptyMessage(UPDATE_VIDEO_SURFACE_SHOW);
                } else if (MediaPlayType.ERROR == mediaPlayType) {
                    // 需要重置播放进度
                    // 视频错误提示
                    mHandler.sendEmptyMessage(UPDATE_VIDEO_ERROR_SHOW);
                }
            }
        }

        /**
         * 媒体的专辑图片数据发生改变的时候
         */
        @Override
        public void onAlbumPicDataChange() {
            Log.d(TAG, "onAlbumPicDataChange: ");
        }

        @Override
        public void onLoopTypeChange() {
            Log.d(TAG, "onLoopTypeChange: ");
        }

        @Override
        public void onLyricsChange() {
            Log.d(TAG, "onLyricsChange: ");
        }

        @Override
        public void onPlayListChange() {
            Log.d(TAG, "onPlayListChange: ");
        }
    };

    /**
     * 视频尺寸变化时会触发的回调
     */
    private final MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged:  width == " + width + " height == " + height);
            int[] videoWH = VideoSizeUtils.getVideoWH(rootView, height, width);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_VIDEO_TEXTURE_VIEW_SIZE, videoWH[0], videoWH[1]));
        }
    };

    /**
     * 获取这个文件在当前列表中的那个位置，然后用这个位置来进行播放
     *
     * @param path 路径
     * @return int，这个路径在列表中的位置
     */
    protected int getPlayPosition(String path) {
        Log.d(TAG, "getPlayPosition: path = " + path);
        //获取当前的播放列表
        List<FileMessage> fileMessages = getVideoStatusTool().getPlayList();
        int index = MediaControlTool.NEED_TO_FIND_THE_SAVE_MEDIA;
        if (fileMessages == null || fileMessages.isEmpty()) {
            Log.e(TAG, "getPlayPosition: fileMessages is file  index = " + index);
            return index;
        }
        for (int i = 0; i < fileMessages.size(); i++) {
            if (fileMessages.get(i).getPath().equals(path)) {
                index = i;
                break;
            }
        }
        Log.d(TAG, "getPlayPosition: index = " + index);
        return index;
    }

    /**
     * USB1视频的播放器获取回调
     */
    public IRequestMediaPlayer requestVideoPlayer = new IRequestMediaPlayer() {
        @Override
        public MediaPlayer requestMediaPlayer() {
            Log.d(TAG, "requestMediaPlayer: ");
            initUSBVideoPlayer();
            return mMediaPlayer;
        }

        @Override
        public void destroyMediaPlayer() {
            Log.d(TAG, "destroyMediaPlayer: " + (mMediaPlayer == null));
            if (mMediaPlayer != null) {
                Log.d(TAG, "destroyMediaPlayer mMediaPlayer release");
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            if (mVideoThumbnailsIv != null) {
                Log.d(TAG, "destroyMediaPlayer mVideoThumbnailsIv: release");
                mVideoThumbnailsIv.release();
            }
        }
    };

    private void initUSBVideoPlayer() {
        if (mMediaPlayer == null) {
            Log.d(TAG, "initUSBVideoPlayer: 1");
            mMediaPlayer = new MediaPlayer();
            if (null != mSurface) {
                mMediaPlayer.setSurface(mSurface);
                Log.d(TAG, "initUSBVideoPlayer() mMediaPlayer.setSurface 1");
            }
            if (null != mOnVideoSizeChangedListener) {
                mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
                Log.d(TAG, "initUSBVideoPlayer() mMediaPlayer. VideoSize 1");
            }
        }
        Log.i(TAG, "initUSBVideoPlayer() called end");
    }

    protected abstract boolean isConnect();

    protected abstract void initVideoControl();

    protected abstract IControlTool getVideoControl();

    protected abstract IStatusTool getVideoStatusTool();

    protected abstract void autoPlay();

    protected abstract void setPlayList();

    protected abstract void registerVideoStatusChangeListener(IMediaStatusChange iMediaStatusChange);

    protected abstract void unregisterVideoStatusChangeListener(IMediaStatusChange iMediaStatusChange);

    protected abstract MediaType getMediaType();
}
