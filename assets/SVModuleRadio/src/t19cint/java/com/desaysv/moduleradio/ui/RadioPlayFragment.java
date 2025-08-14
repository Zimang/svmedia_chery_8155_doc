package com.desaysv.moduleradio.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.radio.RadioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.bean.Constants;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioPlayListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.utils.ClickUtils;
import com.desaysv.moduleradio.utils.CurrentShowFragmentUtil;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.moduleradio.view.RDSRTDialog;
import com.desaysv.moduleradio.view.RDSSettingsDialog;
import com.desaysv.moduleradio.view.cursor.OnFrequencyChangedListener;
import com.desaysv.moduleradio.view.cursor.RadioCursor;
import com.desaysv.libradio.control.RadioControlTool;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;

public class RadioPlayFragment extends BaseFragment implements View.OnClickListener, RadioPlayListAdapter.OnItemClickListener, View.OnLongClickListener {


    private ObjectAnimator singArmAni;

    private ImageView ivBack;
    private ImageView ivSingArm;
    private MyHandler mHandler;

    private TextView tvRadioPlayFreq;
    private TextView tvRadioPlayUnit;
    private TextView tvRadioPlayName;

    private ImageView ivSeekBackward;
    private ImageView ivRadioPlayPause;
    private ImageView ivSeekForward;
    private ImageView ivRadioPlayRDS;
    private ImageView ivRadioPlayCollect;
    private ImageView ivRadioPlayScan;
    private ImageView ivRadioPlayMore;
    private RDSRTDialog rdsrtDialog;

    private RDSSettingsDialog rdsSettingsDialog;
    private TextView tvRadioPlayTP;
    private TextView tvRadioPlayTA;
    private TextView tvRadioPlayTF;
    private TextView tvRadioPlayType;
    private TextView tvRadioPlayST;

    private TextView tvRadioPlayListTitle;
    private RecyclerView rvRadioPlayList;
    protected RadioPlayListAdapter playListAdapter;
    protected LinearLayoutManager playListLayoutManager;
    protected List<RadioMessage> playList = new ArrayList<>();//当前播放列表

    protected RelativeLayout rvRadioPlaySearching;
    protected ImageView ivRadioLoading;

    //刻度尺
    protected RadioCursor radioCursor;

    private TextView tvSearchNoList;//搜索无列表的提示
    private RelativeLayout rlRadioEmptyList;//无列表的提示

    private int tempBand = -1;
    private int tempFreq = -1;
    private boolean hasMulti = false;//用本地变量获取一次就好了，避免多次获取
    private boolean fromClick = false;

    private boolean isRightRudder = ProductUtils.isRightRudder();

    //收音的控制器
    protected IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    protected IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    protected IGetControlTool mGetControlTool;

    protected RadioMessage currentRadioMessage = null;

    private TypedValue value = new TypedValue();
    private TypedValue value1 = new TypedValue();

    //判断是否到的收藏的上限 30
    private boolean isFullCollect(){
        if (currentRadioMessage == null){
            currentRadioMessage = mGetRadioStatusTool.getCurrentRadioMessage();
        }
        if (hasMulti) {
            return RadioList.getInstance().getAMCollectRadioMessageList().size() > 29;
        }
        return RadioList.getInstance().isFMAMFullCollect();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivBack) {
            Log.d(TAG, "onClick: ivBack");
            ivBack.setVisibility(View.GONE);
            startLoadingAni(false);
            backClickListener.onBackClick();
        } else if (id == R.id.ivRadioPlayPause) {
            Log.d(TAG, "onClick: ivRadioPlayPause");
            if (!ClickUtils.isAllowClick()){
                return;
            }
            mRadioControl.processCommand(RadioAction.PLAY_OR_PAUSE, ChangeReasonData.CLICK);
            //埋点：播放/暂停
            if (mGetRadioStatusTool.isPlaying()) {
                if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM) {
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate, Point.Field.PlayOperType, Point.FieldValue.PLAY
                            , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                            , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                            , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
                } else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM) {
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate, Point.Field.PlayOperType, Point.FieldValue.PLAY
                            , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                            , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                            , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
                }
            }else {
                if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM) {
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate, Point.Field.PlayOperType, Point.FieldValue.PAUSE
                            , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                            , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                            , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
                } else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM) {
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate, Point.Field.PlayOperType, Point.FieldValue.PAUSE
                            , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                            , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                            , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
                }
            }

        } else if (id == R.id.ivSeekBackward) {
            Log.d(TAG, "onClick: ivSeekBackward");
            if (!ClickUtils.isAllowClick()){
                return;
            }
            if(hasMulti){
                mRadioControl.processCommand(isRightRudder ? RadioAction.MULTI_SEEK_FORWARD : RadioAction.MULTI_SEEK_BACKWARD, ChangeReasonData.CLICK);
            }else {
                mRadioControl.processCommand(isRightRudder ? RadioAction.SEEK_FORWARD : RadioAction.SEEK_BACKWARD, ChangeReasonData.CLICK);
            }
            //埋点：上一曲
            if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM) {
                PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate, Point.Field.PlayOperType, Point.FieldValue.PRE
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                        , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
            } else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM) {
                PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate, Point.Field.PlayOperType, Point.FieldValue.PRE
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                        , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
            }
        } else if (id == R.id.ivSeekForward) {
            Log.d(TAG, "onClick: ivSeekForward");
            if (!ClickUtils.isAllowClick()){
                return;
            }
            if(hasMulti){
                mRadioControl.processCommand(isRightRudder ? RadioAction.MULTI_SEEK_BACKWARD : RadioAction.MULTI_SEEK_FORWARD, ChangeReasonData.CLICK);
            }else {
                mRadioControl.processCommand(isRightRudder ? RadioAction.SEEK_BACKWARD : RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
            }
            //埋点：下一曲
            if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM) {
                PointTrigger.getInstance().trackEvent(Point.KeyName.FMOperate, Point.Field.PlayOperType, Point.FieldValue.NEXT
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                        , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
            } else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM) {
                PointTrigger.getInstance().trackEvent(Point.KeyName.AMOperate, Point.Field.PlayOperType, Point.FieldValue.NEXT
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, currentRadioMessage.getCalculateFrequency()
                        , Point.Field.Mhz, currentRadioMessage.getCalculateFrequency());
            }
        } else if (id == R.id.ivRadioPlayCollect) {
            Log.d(TAG, "onClick: ivRadioPlayCollect");
            if (!v.isSelected()){
                //check is full(30)
                if(isFullCollect()){
                    ToastUtil.showToast(getContext(), getString(com.desaysv.moduledab.R.string.dab_collect_fully));
                }else {
                    mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, mGetRadioStatusTool.getCurrentRadioMessage());

                    //埋点：收藏
                    if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.FMCollect,Point.Field.CollOperType,Point.FieldValue.COLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                                ,Point.Field.RadioName,currentRadioMessage.getCalculateFrequency()
                                ,Point.Field.Mhz,currentRadioMessage.getCalculateFrequency());
                    }else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM){
                        PointTrigger.getInstance().trackEvent(Point.KeyName.AMCollect,Point.Field.CollOperType,Point.FieldValue.COLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                                ,Point.Field.RadioName,currentRadioMessage.getCalculateFrequency()
                                ,Point.Field.Mhz,currentRadioMessage.getCalculateFrequency());
                    }
                }
            }else {
                mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, mGetRadioStatusTool.getCurrentRadioMessage());
                //埋点：取消收藏
                if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.FMCollect,Point.Field.CollOperType,Point.FieldValue.UNCOLLECT
                            ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                            ,Point.Field.RadioName,currentRadioMessage.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadioMessage.getCalculateFrequency());
                }else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM){
                    PointTrigger.getInstance().trackEvent(Point.KeyName.AMCollect,Point.Field.CollOperType,Point.FieldValue.UNCOLLECT
                            ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                            ,Point.Field.RadioName,currentRadioMessage.getCalculateFrequency()
                            ,Point.Field.Mhz,currentRadioMessage.getCalculateFrequency());

                }
            }
        } else if (id == R.id.ivRadioPlayScan) {
            Log.d(TAG, "onClick: ivRadioPlayScan");
            if (!ClickUtils.isAllowClick()){
                return;
            }
            fromClick = true;
            mRadioControl.processCommand(RadioAction.AST, ChangeReasonData.CLICK);
            if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM){
                //埋点：搜索FM
                PointTrigger.getInstance().trackEvent(Point.KeyName.FMSearch,Point.Field.Mhz,mGetRadioStatusTool.getFMRadioMessage().getCalculateFrequency());
            }else if (currentRadioMessage.getRadioBand() == RadioManager.BAND_AM){
                //埋点：搜索AM
                PointTrigger.getInstance().trackEvent(Point.KeyName.AMSearch,Point.Field.Mhz,mGetRadioStatusTool.getAMRadioMessage().getCalculateFrequency());

            }
        } else if (id == R.id.ivRadioPlayRDS) {
            Log.d(TAG, "onClick: ivRadioPlayRDS");
            rdsSettingsDialog.show();
        } else if (id == R.id.ivRadioPlayMore) {
            Log.d(TAG, "onClick: ivRadioPlayMore");
            showMoreIcon();
        }else if (id == R.id.ivRadioScan){
            Log.d(TAG, "onClick: ivRadioScan");
            fromClick = true;
            mRadioControl.processCommand(RadioAction.AST, ChangeReasonData.CLICK);
            windowManager.removeViewImmediate(viewMore);
        }else if (id == R.id.ivRadioInfo){
            windowManager.removeViewImmediate(viewMore);
            rdsrtDialog.show();//先show，然后更新，不然首次会不显示更新
            if (currentRadioMessage.getRdsRadioText() != null) {
                rdsrtDialog.updateRT(currentRadioMessage.getRdsRadioText());
            }        }
    }

    @Override
    public int getLayoutResID() {
        return isRightRudder ? R.layout.radio_activity_play_right : R.layout.radio_activity_play;
    }

    @Override
    public void initView(View view) {
        hasMulti = ProductUtils.hasMulti();
        mHandler = new MyHandler(this);
        ivBack = view.findViewById(R.id.ivBack);
        ivSingArm = view.findViewById(R.id.ivSingArm);
        tvRadioPlayFreq = view.findViewById(R.id.tvRadioPlayFreq);
        tvRadioPlayUnit = view.findViewById(R.id.tvRadioPlayUnit);
        tvRadioPlayName = view.findViewById(R.id.tvRadioPlayName);
        ivSeekBackward = view.findViewById(R.id.ivSeekBackward);
        ivRadioPlayPause = view.findViewById(R.id.ivRadioPlayPause);
        ivSeekForward = view.findViewById(R.id.ivSeekForward);
        ivRadioPlayRDS = view.findViewById(R.id.ivRadioPlayRDS);
        ivRadioPlayCollect = view.findViewById(R.id.ivRadioPlayCollect);
        ivRadioPlayScan = view.findViewById(R.id.ivRadioPlayScan);
        ivRadioPlayMore = view.findViewById(R.id.ivRadioPlayMore);

        tvRadioPlayListTitle = view.findViewById(R.id.tvRadioPlayListTitle);
        rvRadioPlayList = view.findViewById(R.id.rvRadioPlayList);
        rvRadioPlaySearching = view.findViewById(R.id.rvRadioPlaySearching);
        ivRadioLoading = view.findViewById(R.id.ivRadioLoading);

        radioCursor = view.findViewById(R.id.rcCursor);

        rdsSettingsDialog = new RDSSettingsDialog(getContext(), R.style.radio_dialogstyle);
        tvRadioPlayTP = view.findViewById(R.id.tvRadioPlayTP);
        tvRadioPlayTA = view.findViewById(R.id.tvRadioPlayTA);
        tvRadioPlayTF = view.findViewById(R.id.tvRadioPlayTF);
        tvRadioPlayType = view.findViewById(R.id.tvRadioPlayType);
        tvRadioPlayST = view.findViewById(R.id.tvRadioPlayST);

        tvSearchNoList = view.findViewById(R.id.tvSearchNoList);
        rlRadioEmptyList = view.findViewById(R.id.rlRadioEmptyList);

        rdsrtDialog = new RDSRTDialog(getContext(), R.style.radio_dialogstyle);

        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mGetControlTool = ModuleRadioTrigger.getInstance().mGetControlTool;

        rvRadioPlayList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (windowManager == null){
            windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            viewMore = inflater.inflate(R.layout.radio_play_more,null);
            ivRadioScan = viewMore.findViewById(R.id.ivRadioScan);
            ivRadioInfo = viewMore.findViewById(R.id.ivRadioInfo);
            ivRadioScan.setOnClickListener(this);
            ivRadioInfo.setOnClickListener(this);
            viewMore.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    windowManager.removeViewImmediate(viewMore);
                    return false;
                }
            });
        }
    }

    @Override
    public void initData() {
        currentRadioMessage = mGetRadioStatusTool.getCurrentRadioMessage();
        isRightRudder = ProductUtils.isRightRudder();
    }

    @Override
    public void initViewListener() {
        ivBack.setOnClickListener(this);
        ivSeekBackward.setOnClickListener(this);
        ivSeekForward.setOnClickListener(this);
        ivRadioPlayPause.setOnClickListener(this);
        ivRadioPlayPause.setAccessibilityDelegate(playPauseAccessibilityDelegate);
        ivRadioPlayRDS.setOnClickListener(this);
        ivRadioPlayCollect.setOnClickListener(this);
        ivRadioPlayCollect.setAccessibilityDelegate(playCollectAccessibilityDelegate);
        ivRadioPlayScan.setOnClickListener(this);
        ivRadioPlayMore.setOnClickListener(this);

        ivSeekBackward.setOnLongClickListener(this);
        ivSeekForward.setOnLongClickListener(this);
        ivSeekBackward.setOnTouchListener(onTouchListener);
        ivSeekForward.setOnTouchListener(onTouchListener);

        playListLayoutManager = new LinearLayoutManager(getContext());
        rvRadioPlayList.setLayoutManager(playListLayoutManager);//默认就是竖向
        playListAdapter = new RadioPlayListAdapter(getContext(), this);
        rvRadioPlayList.setAdapter(playListAdapter);
        rvRadioPlayList.setHasFixedSize(true);
        rvRadioPlayList.setItemAnimator(null);
        radioCursor.setOnFrequencyChangeListener(onFrequencyListChangeListener);

        rdsSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateCurrentInfo();
            }
        });
    }

    private final View.AccessibilityDelegate playPauseAccessibilityDelegate = new View.AccessibilityDelegate() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            Log.d(TAG, "onInitializeAccessibilityNodeInfo ivRadioPlayPause");
            info.setChecked(!ivRadioPlayPause.isSelected());
        }
    };

    private final View.AccessibilityDelegate playCollectAccessibilityDelegate = new View.AccessibilityDelegate() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            Log.d(TAG, "onInitializeAccessibilityNodeInfo ivRadioPlayCollect");
            info.setChecked(ivRadioPlayCollect.isSelected());
        }
    };

    private ObjectAnimator loadingAnimator;
    protected void startLoadingAni(boolean start) {
        Log.d(TAG, "startLoadingAni: start = " + start);
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(ivRadioLoading, "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        Log.d(TAG, "startLoadingAni: isStarted = " + loadingAnimator.isStarted() + " isRunning = " + loadingAnimator.isRunning());
        if (start) {
            rvRadioPlaySearching.setVisibility(View.VISIBLE);
            if (!loadingAnimator.isRunning()) {
                Log.d(TAG, "startLoadingAni: start");
                loadingAnimator.start();
            }
        } else {
            Log.d(TAG, "startLoadingAni: end");
            loadingAnimator.cancel();
            rvRadioPlaySearching.setVisibility(View.GONE);
        }
    }



    @Override
    public void onItemClick(int position) {

    }

    /**
     * 下一曲是否长按
     */
    private boolean isNextSeekLongClick = false;

    /**
     * 上一曲是否长按
     */
    private boolean isPreSeekLongClick = false;

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        if (id == R.id.ivSeekBackward) {
            if (isRightRudder) {
                Log.d(TAG, "onLongClick: ivSeekForward");
                isNextSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_NEXT, ChangeReasonData.CLICK);
            }else {
                Log.d(TAG, "onLongClick: ivSeekBackward");
                isPreSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_PRE, ChangeReasonData.CLICK);
            }
        } else if (id == R.id.ivSeekForward) {
            if (isRightRudder) {
                Log.d(TAG, "onLongClick: ivSeekBackward");
                isPreSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_PRE, ChangeReasonData.CLICK);
            }else {
                Log.d(TAG, "onLongClick: ivSeekForward");
                isNextSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_NEXT, ChangeReasonData.CLICK);
            }
        }
        return false;
    }

    /**
     *  播放AM 跳转AM界面
     * @param radioMessage
     */
    private boolean toMultiPlayPageIfNeed(RadioMessage radioMessage) {
        Log.d(TAG,"toMultiPlayPageIfNeed: radioMessage = " + radioMessage);
        boolean toMultiPlayPage = false;
        if (hasMulti) {
            if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE || radioMessage.getRadioBand() == RadioManager.BAND_FM || radioMessage.getRadioBand() == RadioManager.BAND_FM_HD) {
                    Log.d(TAG,"toMultiPlayPageIfNeed: MSG_OPEN_Multi_PLAY_PAGE");
                    mHandler.sendEmptyMessageDelayed(RadioConstants.MSG_OPEN_Multi_PLAY_PAGE, 150);
                    toMultiPlayPage = true;
                }
            }
        }
        return toMultiPlayPage;
    }

    private void openMultiPlayPage() {
        Intent intent = new Intent();
        intent.setClassName("com.desaysv.svaudioapp", "com.desaysv.svaudioapp.ui.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.Source.SOURCE_KEY, Constants.Source.SOURCE_RADIO);
        intent.putExtra(Constants.NavigationFlag.KEY, Constants.NavigationFlag.FLAG_DAB_PLAY);
        AppBase.mContext.startActivity(intent);
    }

    /**
     * 按键触摸事件会触发的回调，主要用来实现上下搜索电台的按钮长按的逻辑
     */
    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int id = v.getId();
                if (id == R.id.ivSeekBackward) {
                    if (isRightRudder){
                        Log.d(TAG, "onTouch: ivSeekForward isNextSeekLongClick = " + isNextSeekLongClick);
                        if (isNextSeekLongClick) {
                            //如果是长按松开的，那就要进入步进模式
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.START_STEP_MODE, ChangeReasonData.CLICK);
                            //一旦松开，就调用退出快进的方法，这个方法可以多次调用，不会有问题的
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.STOP_FAST_STEP_NEXT, ChangeReasonData.CLICK);
                        }
                        isNextSeekLongClick = false;
                    }else {
                        Log.d(TAG, "onTouch: ivSeekBackward isPreSeekLongClick = " + isPreSeekLongClick);
                        if (isPreSeekLongClick) {
                            //如果是长按松开的，那就要进入步进模式
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.START_STEP_MODE, ChangeReasonData.CLICK);
                            //一旦松开，就调用退出快进的方法，这个方法可以多次调用，不会有问题的
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.STOP_FAST_STEP_PRE, ChangeReasonData.CLICK);
                        }
                        isPreSeekLongClick = false;
                    }
                } else if (id == R.id.ivSeekForward) {
                    if (isRightRudder){
                        Log.d(TAG, "onTouch: ivSeekBackward isPreSeekLongClick = " + isPreSeekLongClick);
                        if (isPreSeekLongClick) {
                            //如果是长按松开的，那就要进入步进模式
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.START_STEP_MODE, ChangeReasonData.CLICK);
                            //一旦松开，就调用退出快进的方法，这个方法可以多次调用，不会有问题的
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.STOP_FAST_STEP_PRE, ChangeReasonData.CLICK);
                        }
                        isPreSeekLongClick = false;
                    }else {
                        Log.d(TAG, "onTouch: ivSeekForward isNextSeekLongClick = " + isNextSeekLongClick);
                        if (isNextSeekLongClick) {
                            //如果是长按松开的，那就要进入步进模式
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.START_STEP_MODE, ChangeReasonData.CLICK);
                            //一旦松开，就调用退出快进的方法，这个方法可以多次调用，不会有问题的
                            ModuleRadioTrigger.getInstance().mRadioControl.
                                    processCommand(RadioAction.STOP_FAST_STEP_NEXT, ChangeReasonData.CLICK);
                        }
                        isNextSeekLongClick = false;
                    }
                }
            }
            return false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        CurrentShowFragmentUtil.isRadioPlayPageOnStart = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentOnResume = true;
        updateAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentOnResume = false;
        updateSearchStatues(false);
        ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
    }

    @Override
    public void onStop() {
        super.onStop();
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        if (null != loadingAnimator) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
        CurrentShowFragmentUtil.isRadioPlayPageOnStart = false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        if (singArmAni != null){
            singArmAni.cancel();
            singArmAni = null;
        }
        prePlayStatus = -1;
    }

    /**
     * 根据RDSFlag状态更新界面
     */
    public void updateRDSFLag(RDSFlagInfo info) {
        Log.d(TAG, "updateRDSFLag,info:" + info);
        if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM && info != null && currentRadioMessage.getRadioBand() != RadioManager.BAND_AM  && (mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getRds() == 1)) {
            tvRadioPlayTP.setVisibility(info.getTp() == 1 ? View.VISIBLE : View.GONE);
            if (mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getTa() == 1) {
                tvRadioPlayTA.setVisibility(View.VISIBLE);
            } else {
                tvRadioPlayTA.setVisibility(View.GONE);
            }
            tvRadioPlayTF.setVisibility(info.getAf() == 1 ? mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getAf() == 1 ?View.VISIBLE : View.GONE : View.GONE);
        } else {
            tvRadioPlayTP.setVisibility(View.GONE);
            tvRadioPlayTA.setVisibility(View.GONE);
            tvRadioPlayTF.setVisibility(View.GONE);
        }
    }
    
    public void updateViewWithRDSSettingsChanged(RDSSettingsSwitch rdsSettingsSwitch) {
        Log.d(TAG, "updateViewWithRDSSettingsChanged,:" + rdsSettingsSwitch);
        if (rdsSettingsSwitch == null || currentRadioMessage.getRadioBand() == RadioManager.BAND_AM) {
            return;
        }
        if (rdsSettingsSwitch.getRds() == 1 && currentRadioMessage.getRadioBand() == RadioManager.BAND_FM && mGetRadioStatusTool != null) {//RDS开关打开
            //RDS TA开关打开且当前电台有TA
            if (rdsSettingsSwitch.getTa() == 1) {
                tvRadioPlayTA.setVisibility(View.VISIBLE);
            } else {
                tvRadioPlayTA.setVisibility(View.GONE);
            }
            if (rdsSettingsSwitch.getAf() == 1 && mGetRadioStatusTool.getCurrentRDSFlagInfo() != null && mGetRadioStatusTool.getCurrentRDSFlagInfo().getAf() == 1) {
                tvRadioPlayTF.setVisibility(View.VISIBLE);
            } else {
                tvRadioPlayTF.setVisibility(View.GONE);
            }
            if (currentRadioMessage.getRdsRadioText() != null) {
                if (ivRadioInfo != null) {
                    ivRadioInfo.setEnabled(true);
                }
            }

            ivRadioPlayMore.setVisibility(View.VISIBLE);
            ivRadioPlayScan.setVisibility(View.GONE);

        } else {//RDS开关关闭，那么全部都要隐藏
            tvRadioPlayTA.setVisibility(View.GONE);
            tvRadioPlayTF.setVisibility(View.GONE);
            tvRadioPlayType.setVisibility(View.GONE);

            ivRadioPlayMore.setVisibility(View.GONE);
            ivRadioPlayScan.setVisibility(View.VISIBLE);
            if (ivRadioInfo != null) {
                ivRadioInfo.setEnabled(false);
            }
        }
    }

    /**
     * 根据搜索状态更新界面
     *
     * @param isSearching isSearching
     */
    public void updateSearchStatues(boolean isSearching) {
        if (isSearching) {
            startLoadingAni(true);
            rvRadioPlayList.setVisibility(View.GONE);
            rlRadioEmptyList.setVisibility(View.GONE);
        } else {
            startLoadingAni(false);
            rvRadioPlayList.setVisibility(View.VISIBLE);
            updateList();
        }
    }

    public void updateAll() {
        Log.d(TAG, "updateAll");
        updateList();
        updateCurrentInfo();
        updatePlayStatues();
        //更新RDS的设置状态
        updateRDSFLag(mGetRadioStatusTool.getCurrentRDSFlagInfo());
        updateViewWithRDSSettingsChanged(mGetRadioStatusTool.getRDSSettingsSwitchStatus());
        // 更新当前 搜索 状态
        boolean isSearching = mGetRadioStatusTool.isSearching();
        mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS, isSearching));
    }

    /**
     * 更新列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList() {
        Log.d(TAG, "updateList,currentRadioMessage:" + currentRadioMessage);
        if (SPUtlis.getInstance().getIsShowCollectListMode()) {
            tvRadioPlayListTitle.setText(getResources().getString(R.string.radio_collect_list));
        } else {
            tvRadioPlayListTitle.setText(getResources().getString(R.string.radio_type_list));
        }
        if (currentRadioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (currentRadioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                case RadioManager.BAND_AM_HD:
                    List<RadioMessage> amEffectRadioMessageList;
                    if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                        //显示收藏列表
                        amEffectRadioMessageList = RadioList.getInstance().getAllCollectRadioMessageList();
                    } else {
                        amEffectRadioMessageList = RadioList.getInstance().getAMEffectRadioMessageList();
                    }
                    playList.clear();
                    playList.addAll(amEffectRadioMessageList);
                    playListAdapter.updateList(amEffectRadioMessageList);
//                    tvRadioPlayListTitle.setText(String.format(getResources().getString(R.string.radio_playlist_title), amEffectRadioMessageList.size()));
                    break;
                case RadioManager.BAND_FM:
                case RadioManager.BAND_FM_HD:
                    List<RadioMessage> fmEffectRadioMessageList;
                    if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                        //显示收藏列表
                        fmEffectRadioMessageList = RadioList.getInstance().getAllCollectRadioMessageList();
                    } else {
                        fmEffectRadioMessageList = RadioList.getInstance().getFMEffectRadioMessageList();
                    }
                    playList.clear();
                    playList.addAll(fmEffectRadioMessageList);
                    playListAdapter.updateList(fmEffectRadioMessageList);
//                    tvRadioPlayListTitle.setText(String.format(getResources().getString(R.string.radio_playlist_title), fmEffectRadioMessageList.size()));
                    break;
            }
        }
        Log.d(TAG, "updateList: playList = " + playList.size());
        if (!mGetRadioStatusTool.isSearching()) {
            playListAdapter.notifyDataSetChanged();
            if (playList.size() != 0) {
                rvRadioPlayList.setVisibility(View.VISIBLE);
                rlRadioEmptyList.setVisibility(View.GONE);
                if (tempFreq != currentRadioMessage.getRadioFrequency()) {
                    for (int i = 0; i < playList.size(); i++) {//找到当前播放项在列表的位置
                        if (currentRadioMessage.getRadioFrequency() == playList.get(i).getRadioFrequency()) {
                            final int temp = i;
                            rvRadioPlayList.post(new Runnable() {
                                @Override
                                public void run() {
                                    rvRadioPlayList.smoothScrollToPosition(temp);
                                }
                            });
                        }
                    }
                }
            }else {
                rvRadioPlayList.setVisibility(View.GONE);
                rlRadioEmptyList.setVisibility(View.VISIBLE);
            }
        }
        // 当前状态
        ivRadioPlayCollect.setSelected(currentRadioMessage.isCollect());
    }

    public void updateCurrentInfo(){
        currentRadioMessage = mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "updateCurrentInfo,currentRadioMessage: " + currentRadioMessage);
        boolean toMultiPlayPage = toMultiPlayPageIfNeed(currentRadioMessage);
        if (toMultiPlayPage) {
            return;
        }
        if (currentRadioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (currentRadioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    tvRadioPlayUnit.setText(R.string.radio_khz);
                    ivRadioPlayRDS.setVisibility(View.GONE);
                    ivRadioPlayMore.setVisibility(View.GONE);
                    tvRadioPlayTP.setVisibility(View.GONE);
                    tvRadioPlayTA.setVisibility(View.GONE);
                    tvRadioPlayTF.setVisibility(View.GONE);
                    tvRadioPlayType.setVisibility(View.GONE);
                    tvRadioPlayST.setVisibility(View.GONE);
                    ivRadioPlayScan.setVisibility(View.VISIBLE);
                    break;
                case RadioManager.BAND_FM:
                    tvRadioPlayUnit.setText(R.string.radio_mhz);
                    ivRadioPlayRDS.setEnabled(true);
                    if (currentRadioMessage.isST()){
                        tvRadioPlayST.setVisibility(View.VISIBLE);
                    }else {
                        tvRadioPlayST.setVisibility(View.GONE);
                    }
                    if (rdsSettingsDialog.getTotalState()) {

//                        if (currentRadioMessage.isTP()) {
//                            tvRadioPlayTP.setVisibility(View.VISIBLE);
//                        } else {
//                            tvRadioPlayTP.setVisibility(View.GONE);
//                        }
//                        tvRadioPlayTA.setVisibility(View.VISIBLE);
//                        tvRadioPlayTF.setVisibility(View.VISIBLE);
                        tvRadioPlayType.setVisibility(View.VISIBLE);
                        tvRadioPlayName.setVisibility(View.VISIBLE);
//            ivRadioPlayRT.setVisibility(View.VISIBLE);
                    }else {
                        tvRadioPlayTP.setVisibility(View.GONE);
                        tvRadioPlayTA.setVisibility(View.GONE);
                        tvRadioPlayTF.setVisibility(View.GONE);
                        tvRadioPlayType.setVisibility(View.GONE);
                        tvRadioPlayName.setVisibility(View.GONE);
//            ivRadioPlayRT.setVisibility(View.GONE);
                    }
                    break;
            }
        }
        tvRadioPlayFreq.setText(currentRadioMessage.getCalculateFrequency());
        ivRadioPlayCollect.setSelected(currentRadioMessage.isCollect());
        String radioName = RadioCovertUtils.getOppositeRDSName(currentRadioMessage) != null ? RadioCovertUtils.getOppositeRDSName(currentRadioMessage) : null;
        if (radioName == null){
            if (currentRadioMessage.getRdsRadioText() != null && currentRadioMessage.getRdsRadioText().getProgramStationName() != null){
                tvRadioPlayName.setText(currentRadioMessage.getRdsRadioText().getProgramStationName());
            }else {
                tvRadioPlayName.setText("");
            }
        }else {
            tvRadioPlayName.setText(radioName);
        }
        // 刷新列表 ，更新当前播放高亮位置
        playListAdapter.notifyDataSetChanged();

        radioCursor.setBand(currentRadioMessage.getRadioBand());
        radioCursor.setFrequency(currentRadioMessage.getRadioFrequency());
        radioCursor.updateRadioMessage(currentRadioMessage);

        if (currentRadioMessage.getRdsRadioText() != null) {
            if (ivRadioInfo != null){
                ivRadioInfo.setEnabled(true);
            }
            rdsrtDialog.updateRT(currentRadioMessage.getRdsRadioText());
            tvRadioPlayType.setText(RadioCovertUtils.changeTypeToString(getContext(),currentRadioMessage.getRdsRadioText().getProgramType()));
        } else {
            if (ivRadioInfo != null) {
                ivRadioInfo.setEnabled(false);
            }
            tvRadioPlayType.setVisibility(View.GONE);
        }
        //兼容语音控制的场景：判断播放的band有变化时，更新一下列表
        if (tempBand != -1 && tempBand != currentRadioMessage.getRadioBand()){
            updateList();
        }
        tempBand = currentRadioMessage.getRadioBand();
        if (tempFreq != currentRadioMessage.getRadioFrequency()) {
            for (int i = 0; i < playList.size(); i++) {//找到当前播放项在列表的位置
                if (currentRadioMessage.getRadioFrequency() == playList.get(i).getRadioFrequency()) {
                    rvRadioPlayList.smoothScrollToPosition(i);
                }
            }
        }

        tempFreq = currentRadioMessage.getRadioFrequency();
    }

    private int prePlayStatus = -1;//-1表示初始值，避免首次的问题。0是暂停，1是播放
    public void updatePlayStatues(){
        boolean isPlaying = mGetRadioStatusTool.isPlaying();
        Log.d(TAG,"updatePlayStatus,isPlaying:"+isPlaying);
        if (isPlaying){
            // 初始化旋转动画，旋转中心默认为控件中点
            getResources().getValue(R.dimen.radio_ivsingarm_roate_0,value,true);
            getResources().getValue(R.dimen.radio_ivsingarm_roate_1,value1,true);

            singArmAni = ObjectAnimator.ofFloat(ivSingArm, "rotation",value.getFloat(),value1.getFloat());
        }else {
            getResources().getValue(R.dimen.radio_ivsingarm_roate_1,value,true);
            getResources().getValue(R.dimen.radio_ivsingarm_roate_0,value1,true);

            singArmAni = ObjectAnimator.ofFloat(ivSingArm, "rotation",value.getFloat(),value1.getFloat());
        }

        ivRadioPlayPause.setSelected(!isPlaying);
        playListAdapter.updatePlayItemAnim();

        if (prePlayStatus != -1 && ((prePlayStatus == 1 && isPlaying) || (prePlayStatus == 0 && !isPlaying) )){
            Log.d(TAG,"play status is same, return singArmAni");
            return;
        }
        prePlayStatus = isPlaying ? 1 : 0;
        singArmAni.setDuration(1000);
        singArmAni.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
        singArmAni.start();
    }


    private boolean isFragmentOnResume = false;
    OnFrequencyChangedListener onFrequencyListChangeListener = new OnFrequencyChangedListener() {

        @Override
        public void onChanged(int band, float frequency) {
            Log.d(TAG, "onChanged: band = " + band + " frequency = " + frequency);
            //列表滚动导致界面刷新要单独开来，避免两个刷新互相影响,这个只是滚动用的，值刷新显示，不刷新播放状态
            RadioMessage radioMessage = new RadioMessage(band, (int) frequency);
//            updateRadioFrequency(radioMessage);
        }

        @Override
        public void onChangedAndOpenIt(int band, float frequency) {
            RadioMessage radioMessage = new RadioMessage(band, (int) frequency);
            Log.d(TAG, "onChangedAndOpenIt: radioMessage = " + radioMessage + " isFragmentOnResume = " + isFragmentOnResume);
            //add by lzm 只有界面在前台的时候才能回调，不然会出现滑动过程中切换到USB音乐界面，USB音乐界面音源被抢
            if (isFragmentOnResume) {
                currentRadioMessage = radioMessage;
                mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.SLIDING_LIST, radioMessage);
            }
        }
    };

    IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST);
        }

        @Override
        public void onDABCollectListChange() {

        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange: ");
//            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG, "onAMEffectListChange: ");
//            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_AM_LIST);
        }

        @Override
        public void onDABEffectListChange() {

        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG, "onFMAllListChange: ");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG, "onAMAllListChange: ");
        }

        @Override
        public void onDABAllListChange() {

        }
    };

    IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG, "onCurrentRadioMessageChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange: ");
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged: ");
            if (band == RadioManager.BAND_FM) {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_FM_LIST);
            } else if (band == RadioManager.BAND_AM) {
                mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_RADIO_AM_LIST);
            }
        }

        @Override
        public void onSearchStatusChange(final boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange: isSearching = " + isSearching);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS, isSearching));
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG, "onSeekStatusChange: isSeeking = " + isSeeking);
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {

        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG, "onRDSFlagInfoChange: RDSFlagInfo = " + info);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG, info));
        }

        @Override
        public void onRDSSettingsStatus(RDSSettingsSwitch rdsSettingsSwitch) {
            Log.d(TAG, "onRDSSettingsStatus: rdsSettingsSwitch = " + rdsSettingsSwitch);
            //更新界面
            mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RDS_SETTINGS, rdsSettingsSwitch));
        }
    };

    private static class MyHandler extends Handler {
        WeakReference<RadioPlayFragment> weakReference;

        MyHandler(RadioPlayFragment radioPlayFragment) {
            weakReference = new WeakReference<>(radioPlayFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final RadioPlayFragment radioPlayFragment = weakReference.get();
            Log.d(radioPlayFragment.TAG, "handleMessage:" + msg.what);
            if (radioPlayFragment.isDetached() || (RadioControlTool.getInstance().isShowingTTS() && RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS != msg.what)){
                Log.d(radioPlayFragment.TAG, "radioPlayFragment.isDetach");
                return;
            }
            switch (msg.what) {
                case RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE:
                    radioPlayFragment.updateCurrentInfo();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS:
                    radioPlayFragment.updatePlayStatues();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_FM_LIST:
                case RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST:
                case RadioConstants.MSG_UPDATE_RADIO_AM_LIST:
                    radioPlayFragment.updateList();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS:
                    radioPlayFragment.updateSearchStatues((Boolean) msg.obj);
                    if (!(Boolean) msg.obj) {
                        sendEmptyMessageDelayed(RadioConstants.MSG_TIPS,200);
                    }
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG:
                    radioPlayFragment.updateRDSFLag((RDSFlagInfo) msg.obj);
                    break;
                case RadioConstants.MSG_UPDATE_RDS_SETTINGS:
                    radioPlayFragment.updateViewWithRDSSettingsChanged((RDSSettingsSwitch) msg.obj);
                    break;
                case RadioConstants.MSG_TIPS_TIMEOUT:
                    radioPlayFragment.updateSearchTips(false);
                    break;
                case RadioConstants.MSG_TIPS:
                    radioPlayFragment.updateSearchTips(true);
                    break;
                case RadioConstants.MSG_OPEN_Multi_PLAY_PAGE:
                    radioPlayFragment.openMultiPlayPage();
                    break;
                default:
                    break;
            }
        }
    }

    public void updateSearchTips(boolean show){
        Log.d(TAG,"updateSearchTips:"+show);
        if (show){
            if (!fromClick){
                return;
            }
            fromClick = false;
            List<RadioMessage> currentList = new ArrayList<>();
            switch (currentRadioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                case RadioManager.BAND_AM_HD:
                    currentList = RadioList.getInstance().getAMEffectRadioMessageList();
                    break;
                case RadioManager.BAND_FM:
                case RadioManager.BAND_FM_HD:
                    currentList = RadioList.getInstance().getFMEffectRadioMessageList();
                    break;
            }
            if (currentList.size() > 0){

            }else {
                tvSearchNoList.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(RadioConstants.MSG_TIPS_TIMEOUT,RadioConstants.TIPS_TIMEOUT);
            }
        }else {
            tvSearchNoList.setVisibility(View.GONE);
        }
    }


    /**
     * 左上角返回按钮的点击事件
     */
    private IOnBackClickListener backClickListener;

    public void setBackClickListener(IOnBackClickListener backClickListener) {
        this.backClickListener = backClickListener;
    }

    public interface IOnBackClickListener {

        void onBackClick();
    }

    //设计增加了一个More的弹窗
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View viewMore;
    private ImageView ivRadioScan;
    private ImageView ivRadioInfo;
    private void showMoreIcon(){
        if (!viewMore.isAttachedToWindow()) {//如果在显示期间有新的弹窗，会自动更新内容，不需要继续add，否则会出现 “has already been added to the window manager” 的 RuntimeException
            windowManager.addView(viewMore, layoutParams);
        }
    }

}
