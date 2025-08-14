package com.desaysv.moduleradio.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.radio.RadioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.libradio.utils.SPUtlis;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioPlayListAdapter;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.utils.ClickUtils;
import com.desaysv.moduleradio.view.RDSRTDialog;
import com.desaysv.moduleradio.view.RDSSettingsDialog;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.moduleradio.view.cursor.OnFrequencyChangedListener;
import com.desaysv.moduleradio.view.cursor.RadioCursor;
import com.desaysv.moduleradio.view.cursor.RadioCursor2;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.moduledab.utils.ProductUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RadioPlayFragment extends BaseFragment implements View.OnClickListener, RadioPlayListAdapter.OnItemClickListener, View.OnLongClickListener {
    protected ImageView ivBack;//返回按钮
    protected ImageView ivSeekBackward;
    protected ImageView ivSeekForward;
    protected TextView tvRadioPlayName;

    protected TextView tvRadioPlayFreq;
    protected TextView tvRadioPlayUnit;
    protected TextView tvRadioPlayFreqMirror;

    protected ImageView ivRadioPlayCollect;//收藏按钮
    protected ImageView ivRadioPlayPause;//播放/暂停
    protected ImageView ivRadioPlayScan;//搜索电台
    protected TextView tvRadioPlayListTitle;//播放列表的标题
    protected RecyclerView rvRadioPlayList;//播放列表
    protected RadioPlayListAdapter playListAdapter;

    protected RelativeLayout rvRadioPlaySearching;
    protected ImageView ivRadioLoading;

    protected ImageView ivRadioPlayRT;
    private RDSRTDialog rdsrtDialog;

    protected ImageView ivRadioPlayRDS;
    private RDSSettingsDialog rdsSettingsDialog;

    private TextView tvRadioPlayTP;
    private TextView tvRadioPlayTA;
    private TextView tvRadioPlayTF;
    private TextView tvRadioPlayType;
    private TextView tvRadioPlayST;

    private TextView tvEmptyView;
    private RelativeLayout rlEmptyView;
    private boolean hasMulti;

    //刻度尺
    protected RadioCursor radioCursor;

    private ImageView ivRadioPlayBg;
    protected RadioCursor2 radioCursor2;//主题二的刻度尺

    protected List<RadioMessage> playList = new ArrayList<>();//当前播放列表

    // 默认需要刷新
    protected int scrollState = RecyclerView.SCROLL_STATE_IDLE;
    protected int showScrollFreq;

    protected LinearLayoutManager playListLayoutManager;


    //收音的控制器
    protected IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    protected IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    protected IGetControlTool mGetControlTool;

    protected RadioMessage currentRadioMessage = null;

    private boolean fromClick = false;

    private boolean isRTLView;

    private boolean isOverlay2 = false;

    private int tempBand = -1;

    //处理上下电台长按逻辑监听
    private View.OnTouchListener onTouchListener;

    @Override
    public int getLayoutResID() {
        return R.layout.radio_activity_play;
    }

    @Override
    public void initView(View view) {
        Log.d(TAG, "initView");
        isOverlay2 = "overlay2".equals(ProductUtils.getTheme(getContext()));
        hasMulti = ProductUtils.hasMulti();
        mHandler = new MyHandler(this);
        ivBack = view.findViewById(R.id.ivBack);
        ivSeekBackward = view.findViewById(R.id.ivSeekBackward);
        ivSeekForward = view.findViewById(R.id.ivSeekForward);
        tvRadioPlayName = view.findViewById(R.id.tvRadioPlayName);

        ivRadioPlayCollect = view.findViewById(R.id.ivRadioPlayCollect);
        ivRadioPlayPause = view.findViewById(R.id.ivRadioPlayPause);
        ivRadioPlayScan = view.findViewById(R.id.ivRadioPlayScan);
        tvRadioPlayListTitle = view.findViewById(R.id.tvRadioPlayListTitle);
        rvRadioPlayList = view.findViewById(R.id.rvRadioPlayList);
        tvRadioPlayFreq = view.findViewById(R.id.tvRadioPlayFreq);
        tvRadioPlayUnit = view.findViewById(R.id.tvRadioPlayUnit);
        tvRadioPlayFreqMirror = view.findViewById(R.id.tvRadioPlayFreqMirror);

        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mGetControlTool = ModuleRadioTrigger.getInstance().mGetControlTool;

        ivRadioPlayRDS = view.findViewById(R.id.ivRadioPlayRDS);
        rdsSettingsDialog = new RDSSettingsDialog(getContext(), R.style.radio_dialogstyle);
        rdsrtDialog = new RDSRTDialog(getContext(), R.style.radio_dialogstyle);


        rvRadioPlaySearching = view.findViewById(R.id.rvRadioPlaySearching);
        ivRadioLoading = view.findViewById(R.id.ivRadioLoading);

        ivRadioPlayRT = view.findViewById(R.id.ivRadioPlayRT);
        tvRadioPlayTP = view.findViewById(R.id.tvRadioPlayTP);
        tvRadioPlayTA = view.findViewById(R.id.tvRadioPlayTA);
        tvRadioPlayTF = view.findViewById(R.id.tvRadioPlayTF);
        tvRadioPlayType = view.findViewById(R.id.tvRadioPlayType);
        tvRadioPlayST = view.findViewById(R.id.tvRadioPlayST);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        rlEmptyView = view.findViewById(R.id.rlEmptyView);

        radioCursor = view.findViewById(R.id.rcCursor);
        ivRadioPlayBg = view.findViewById(R.id.ivRadioPlayBg);
        radioCursor2 = view.findViewById(R.id.rcCursor2);
        if (isOverlay2){
            radioCursor.setVisibility(View.GONE);
            ivRadioPlayBg.setVisibility(View.GONE);
            radioCursor2.setVisibility(View.VISIBLE);
        }else {
            radioCursor2.setVisibility(View.GONE);
            ivRadioPlayBg.setVisibility(View.VISIBLE);
            radioCursor.setVisibility(View.VISIBLE);
        }

        // 设置倒影的渐变
        int[] color = {Color.DKGRAY, Color.TRANSPARENT};
        float[] position = {0, 1};
        Shader.TileMode tile_mode = Shader.TileMode.MIRROR; // or TileMode.REPEAT;
        LinearGradient lin_grad = new LinearGradient(0, -10, 0, -90, color, position, tile_mode);
        tvRadioPlayFreqMirror.getPaint().setShader(lin_grad);
        tvRadioPlayFreqMirror.setAlpha(0.3f);

        rvRadioPlayList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        isRTLView = ProductUtils.isRtlView(getContext());
    }

    @Override
    public void initData() {
        Log.d(TAG, "initData");
    }

    @Override
    public void initViewListener() {
        Log.d(TAG, "initViewListener");
        ivBack.setOnClickListener(this);
        ivSeekBackward.setOnClickListener(this);
        ivSeekForward.setOnClickListener(this);
        ivRadioPlayCollect.setOnClickListener(this);
        ivRadioPlayPause.setOnClickListener(this);
        ivRadioPlayScan.setOnClickListener(this);
        ivRadioPlayRDS.setOnClickListener(this);
        ivRadioPlayRT.setOnClickListener(this);


        playListLayoutManager = new LinearLayoutManager(getContext());
        rvRadioPlayList.setLayoutManager(playListLayoutManager);//默认就是竖向
        playListAdapter = new RadioPlayListAdapter(getContext(), this);
        rvRadioPlayList.setAdapter(playListAdapter);
        rvRadioPlayList.setHasFixedSize(true);
        //DAB/FM融合情况，AM 上下电台操作, 是上下搜索电台，仅支持短按长按无效。
        if(!hasMulti) {
            ivSeekBackward.setOnLongClickListener(this);
            ivSeekForward.setOnLongClickListener(this);
            initPreNextOnTouchListener();
            ivSeekBackward.setOnTouchListener(onTouchListener);
            ivSeekForward.setOnTouchListener(onTouchListener);
        }

        radioCursor.setOnFrequencyChangeListener(onFrequencyListChangeListener);
        radioCursor2.setOnFrequencyChangeListener(onFrequencyListChangeListener);
        registerThemeChangedObserver();
    }

    public void openWithScroll(){
        // 不在搜索过程中
        if (!mGetRadioStatusTool.isSearching()) {
            RadioMessage radioMessage = currentRadioMessage.Clone();
            radioMessage.setRadioFrequency(showScrollFreq);
            Log.d(TAG, "onScrollStateChanged: to open " + radioMessage.getRadioFrequency());
            mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AUTO_SEEK, radioMessage);
        }
    }


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
            //这里有个问题，有时候 radioHomeFragment 还未加载好，这里就通知Activity显示图标，会导致 图标和播放界面的按钮重叠
            //所以在这里先做个隐藏处理
            ivBack.setVisibility(View.GONE);
            startLoadingAni(false);
            backClickListener.onBackClick();
        } else if (id == R.id.ivRadioPlayPause) {
            Log.d(TAG, "onClick: ivRadioPlayPause");
            if (ClickUtils.isAllowClick()){
                mRadioControl.processCommand(RadioAction.PLAY_OR_PAUSE, ChangeReasonData.CLICK);
            }
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
            if (isRTLView){
                Log.d(TAG, "onClick: ivSeekForward");
                mRadioControl.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
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
            }else {
                Log.d(TAG, "onClick: ivSeekBackward");
                mRadioControl.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.CLICK);
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
            }
        } else if (id == R.id.ivSeekForward) {
            if (isRTLView){
                Log.d(TAG, "onClick: ivSeekBackward");
                mRadioControl.processCommand(RadioAction.SEEK_BACKWARD, ChangeReasonData.CLICK);
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
            }else {
                Log.d(TAG, "onClick: ivSeekForward");
                mRadioControl.processCommand(RadioAction.SEEK_FORWARD, ChangeReasonData.CLICK);
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
            if (!ClickUtils.isAllowClick()){
                return;
            }
            Log.d(TAG, "onClick: ivRadioPlayScan");
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
        } else if (id == R.id.ivRadioPlayRT) {
            Log.d(TAG, "onClick: ivRadioPlayRT");
            rdsrtDialog.show();//先show，然后更新，不然首次会不显示更新
            if (currentRadioMessage.getRdsRadioText() != null) {
                rdsrtDialog.updateRT(currentRadioMessage.getRdsRadioText());
            }
        }
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
        if (isRTLView) {
            if (id == R.id.ivSeekBackward) {
                Log.d(TAG, "isRTLView onLongClick: ivSeekForward");
                isNextSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_NEXT, ChangeReasonData.CLICK);
            } else if (id == R.id.ivSeekForward) {
                Log.d(TAG, "isRTLView onLongClick: ivSeekBackward");
                isPreSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_PRE, ChangeReasonData.CLICK);
            }
        }else {
            if (id == R.id.ivSeekBackward) {
                Log.d(TAG, "onLongClick: ivSeekBackward");
                isPreSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_PRE, ChangeReasonData.CLICK);
            } else if (id == R.id.ivSeekForward) {
                Log.d(TAG, "onLongClick: ivSeekForward");
                isNextSeekLongClick = true;
                mRadioControl.processCommand(RadioAction.START_FAST_STEP_NEXT, ChangeReasonData.CLICK);
            }
        }
        return false;
    }

    /**
     * 按键触摸事件会触发的回调，主要用来实现上下搜索电台的按钮长按的逻辑
     */
    private void initPreNextOnTouchListener(){
        onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int id = v.getId();
                    if (isRTLView){
                        if (id == R.id.ivSeekBackward) {
                            Log.d(TAG, "isRTLView onTouch: ivSeekBackward isPreSeekLongClick = " + isNextSeekLongClick);
                            if (isNextSeekLongClick) {
                                //如果是长按松开的，那就要进入步进模式
                                ModuleRadioTrigger.getInstance().mRadioControl.
                                        processCommand(RadioAction.START_STEP_MODE, ChangeReasonData.CLICK);
                                //一旦松开，就调用退出快进的方法，这个方法可以多次调用，不会有问题的
                                ModuleRadioTrigger.getInstance().mRadioControl.
                                        processCommand(RadioAction.STOP_FAST_STEP_NEXT, ChangeReasonData.CLICK);
                            }
                            isNextSeekLongClick = false;
                        } else if (id == R.id.ivSeekForward) {
                            Log.d(TAG, "isRTLView onTouch: ivSeekForward isNextSeekLongClick = " + isPreSeekLongClick);
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
                    }else {
                        if (id == R.id.ivSeekBackward) {
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
                        } else if (id == R.id.ivSeekForward) {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentOnResume = false;
        updateSearchStatues(false);
        mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentOnResume = true;
        updateAll();
        if (isOverlay2){
            radioCursor.setVisibility(View.GONE);
            ivRadioPlayBg.setVisibility(View.GONE);
            radioCursor2.setVisibility(View.VISIBLE);
        }else {
            radioCursor2.setVisibility(View.GONE);
            ivRadioPlayBg.setVisibility(View.VISIBLE);
            radioCursor.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != loadingAnimator) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterThemeChangedObserver();
        mHandler.removeCallbacksAndMessages(null);
    }


    public void updateAll() {
        Log.d(TAG, "updateAll");
        updateCurrentInfo();
        updateList();
        //更新播放状态
        updatePlayStatues();
        //更新RDS的设置状态
        updateRDSFLag(mGetRadioStatusTool.getCurrentRDSFlagInfo());
        updateViewWithRDSSettingsChanged(mGetRadioStatusTool.getRDSSettingsSwitchStatus());
        // 更新当前 搜索 状态
        boolean isSearching = mGetRadioStatusTool.isSearching();
        mHandler.sendMessage(mHandler.obtainMessage(RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS, isSearching));
    }

    /**
     * 更新播放状态
     */
    public void updatePlayStatues() {
        ivRadioPlayPause.setSelected(!mGetRadioStatusTool.isPlaying());
        playListAdapter.updatePlayItemAnim();
    }

    /**
     * 根据搜索状态更新界面
     *
     * @param isSearching isSearching
     */
    public void updateSearchStatues(boolean isSearching) {
        if (isSearching) {
            rvRadioPlayList.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.GONE);
            rlEmptyView.setVisibility(View.GONE);
            startLoadingAni(true);
        } else {
            startLoadingAni(false);
            updateList();
        }
    }

    /**
     * 根据RDSFlag状态更新界面
     */
    public void updateRDSFLag(RDSFlagInfo info) {
        Log.d(TAG, "updateRDSFLag,info:" + info);
        if (currentRadioMessage.getRadioBand() == RadioManager.BAND_FM && info != null && (mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getRds() == 1)) {
            tvRadioPlayTP.setVisibility(info.getTp() == 1 ? View.VISIBLE : View.GONE);
            tvRadioPlayTA.setVisibility(mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getTa() == 1 && info.getTp() == 1?View.VISIBLE : View.GONE);
            tvRadioPlayTF.setVisibility(info.getAf() == 1 ? mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getAf() == 1 ?View.VISIBLE : View.GONE : View.GONE);
        } else {
            tvRadioPlayTP.setVisibility(View.GONE);
            tvRadioPlayTA.setVisibility(View.GONE);
            tvRadioPlayTF.setVisibility(View.GONE);
        }
    }

    public void updateViewWithRDSSettingsChanged(RDSSettingsSwitch rdsSettingsSwitch){
        Log.d(TAG,"updateViewWithRDSSettingsChanged,:"+rdsSettingsSwitch);
        if (rdsSettingsSwitch == null) {
            return;
        }
        if (rdsSettingsSwitch.getRds() == 1 && currentRadioMessage.getRadioBand() == RadioManager.BAND_FM){//RDS开关打开
            //根据各自状态设置
            tvRadioPlayTA.setVisibility(rdsSettingsSwitch.getTa() == 1 && mGetRadioStatusTool.getCurrentRDSFlagInfo() != null && mGetRadioStatusTool.getCurrentRDSFlagInfo().getTp() == 1?View.VISIBLE : View.GONE);
            tvRadioPlayTF.setVisibility(rdsSettingsSwitch.getAf() == 1 ? mGetRadioStatusTool.getCurrentRDSFlagInfo() != null && mGetRadioStatusTool.getCurrentRDSFlagInfo().getAf() == 1 ?View.VISIBLE:View.GONE : View.GONE);
            if (currentRadioMessage.getRdsRadioText() != null) {
                ivRadioPlayRT.setEnabled(true);
            }
        }else {//RDS开关关闭，那么全部都要隐藏
            tvRadioPlayTA.setVisibility(View.GONE);
            tvRadioPlayTF.setVisibility(View.GONE);
            tvRadioPlayType.setVisibility(View.GONE);
            ivRadioPlayRT.setEnabled(false);
        }
    }



    public void updateCollectList(){
        Log.d(TAG, "updateCollectList,currentRadioMessage:" + currentRadioMessage);
        if (SPUtlis.getInstance().getIsShowCollectListMode()) {
            updateList();
        } else {
            playListAdapter.notifyDataSetChanged();
        }
        ivRadioPlayCollect.setSelected(currentRadioMessage.isCollect());
    }

    /**
     * 更新列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList() {
        Log.d(TAG, "updateList,currentRadioMessage:" + currentRadioMessage);
        if (currentRadioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (currentRadioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                case RadioManager.BAND_AM_HD:
                    List<RadioMessage> amEffectRadioMessageList;
                    if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                        //显示收藏列表
                        amEffectRadioMessageList = RadioList.getInstance().getCurrentCollectRadioMessageList();
                    } else {
                        amEffectRadioMessageList = RadioList.getInstance().getAMEffectRadioMessageList();
                    }
                    playList.clear();
                    playList.addAll(amEffectRadioMessageList);
                    playListAdapter.updateList(playList);
                    if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                        tvRadioPlayListTitle.setText(getResources().getString(R.string.radio_collect_list));
                    } else {
                        tvRadioPlayListTitle.setText(String.format(getResources().getString(R.string.radio_playlist_title), amEffectRadioMessageList.size()));
                    }
                    break;
                case RadioManager.BAND_FM:
                case RadioManager.BAND_FM_HD:
                    List<RadioMessage> fmEffectRadioMessageList;
                    if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                        //显示收藏列表
                        fmEffectRadioMessageList = RadioList.getInstance().getCurrentCollectRadioMessageList();
                    } else {
                        fmEffectRadioMessageList = RadioList.getInstance().getFMEffectRadioMessageList();
                    }
                    playList.clear();
                    playList.addAll(fmEffectRadioMessageList);
                    playListAdapter.updateList(playList);
                    if (SPUtlis.getInstance().getIsShowCollectListMode()) {
                        tvRadioPlayListTitle.setText(getResources().getString(R.string.radio_collect_list));
                    } else {
                        tvRadioPlayListTitle.setText(String.format(getResources().getString(R.string.radio_playlist_title), fmEffectRadioMessageList.size()));
                    }
                    break;
            }
        }
            Log.d(TAG, "updateList: playList = " + playList.size());
            if (playList.size() < 1){
                if (hasMulti){
                    rlEmptyView.setVisibility(View.VISIBLE);
                }else {
                    tvEmptyView.setVisibility(View.VISIBLE);
                }
                rvRadioPlayList.setVisibility(View.GONE);
            }else {
                tvEmptyView.setVisibility(View.GONE);
                rlEmptyView.setVisibility(View.GONE);
                rvRadioPlayList.setVisibility(View.VISIBLE);
            }
            playListAdapter.notifyDataSetChanged();
            if (playList.size() != 0) {
                for (int i = 0; i < playList.size(); i++) {//找到当前播放项在列表的位置
                    if (currentRadioMessage.getRadioFrequency() == playList.get(i).getRadioFrequency()) {
                        rvRadioPlayList.scrollToPosition(i);
                    }
                }
            }
        // 当前状态
        ivRadioPlayCollect.setSelected(currentRadioMessage.isCollect());
    }

    /**
     * 更新当前播放信息，由子类实现
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateCurrentInfo() {
        currentRadioMessage = mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "updateCurrentInfo,currentRadioMessage: " + currentRadioMessage);
        if (currentRadioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (currentRadioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    tvRadioPlayUnit.setText(R.string.radio_khz);
                    ivRadioPlayRDS.setVisibility(View.GONE);
                    ivRadioPlayRT.setVisibility(View.GONE);
                    tvRadioPlayTP.setVisibility(View.GONE);
                    tvRadioPlayTA.setVisibility(View.GONE);
                    tvRadioPlayTF.setVisibility(View.GONE);
                    tvRadioPlayType.setVisibility(View.GONE);
                    tvRadioPlayST.setVisibility(View.GONE);
                    break;
                case RadioManager.BAND_FM:
                    ivRadioPlayRDS.setEnabled(true);//todo 待底层适配好RDS之后再放开
                    tvRadioPlayUnit.setText(R.string.radio_mhz);
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

        if (isOverlay2) {
            radioCursor2.setBand(currentRadioMessage.getRadioBand());
            radioCursor2.setFrequency(currentRadioMessage.getRadioFrequency());
            radioCursor2.updateRadioMessage(currentRadioMessage);
        }else {
            radioCursor.setBand(currentRadioMessage.getRadioBand());
            radioCursor.setFrequency(currentRadioMessage.getRadioFrequency());
            radioCursor.updateRadioMessage(currentRadioMessage);
        }

        tvRadioPlayFreq.setText(currentRadioMessage.getCalculateFrequency());
        tvRadioPlayFreqMirror.setText(currentRadioMessage.getCalculateFrequency());
        ivRadioPlayCollect.setSelected(currentRadioMessage.isCollect());
       //传递过来的名字有可能会出现带有空格所以需要加上判断空格的条件
        RDSRadioText rdsRadioText = currentRadioMessage.getRdsRadioText();
        if(rdsRadioText!=null){
           if ( rdsRadioText.getProgramStationName() != null|| !rdsRadioText.getProgramStationName().trim().isEmpty()){
            tvRadioPlayName.setText(currentRadioMessage.getRdsRadioText().getProgramStationName());
           }else {
               tvRadioPlayName.setText("");
           }
       }else {
           tvRadioPlayName.setText("");
       }
        if (currentRadioMessage.getRdsRadioText() != null) {
            ivRadioPlayRT.setEnabled(true);
            rdsrtDialog.updateRT(currentRadioMessage.getRdsRadioText());
            tvRadioPlayType.setText(RadioCovertUtils.changeTypeToString(getContext(),currentRadioMessage.getRdsRadioText().getProgramType()));
        } else {
            ivRadioPlayRT.setEnabled(false);
            tvRadioPlayType.setVisibility(View.GONE);
        }

        Log.d(TAG, "updateCurrentInfo: scrollState = " + scrollState);

        boolean isScrollFlag = false;
        //兼容语音控制的场景：判断播放的band有变化时，更新一下列表
        if (tempBand != -1 && tempBand != currentRadioMessage.getRadioBand()){
            isScrollFlag = true;
            updateList();
        }
        tempBand = currentRadioMessage.getRadioBand();

        //没有调用updateList()滑动列表，就判断是否滑动列表
        if(!isScrollFlag){
            // 刷新列表 ，更新当前播放高亮位置
            if (!CurrentRadioInfo.getInstance().isSearching()) {
                playListAdapter.notifyDataSetChanged();
            }
            if (!playList.isEmpty()) {
                rvRadioPlayList.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < playList.size(); i++) {//找到当前播放项在列表的位置
                            if (currentRadioMessage.getRadioFrequency() == playList.get(i).getRadioFrequency()) {
                                rvRadioPlayList.scrollToPosition(i);
                                break;
                            }
                        }
                    }
                });

            }
        }
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

        @Override
        public void onRadioRegionChanged() {
            radioCursor.notifyRadioRegionChanged();
            radioCursor2.notifyRadioRegionChanged();
        }
    };


    private MyHandler mHandler;

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick,position:" + position);
    }



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
            Log.d(TAG, "startLoadingAni: start");
            loadingAnimator.start();
        } else {
            loadingAnimator.cancel();
            rvRadioPlaySearching.setVisibility(View.GONE);
            Log.d(TAG, "startLoadingAni: end");
        }
    }


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
            if (radioPlayFragment.isDetached() || (RadioControlTool.getInstance().isShowingTTS() && msg.what != RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS && msg.what != RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG)){
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
                case RadioConstants.MSG_UPDATE_RADIO_AM_LIST:
                    radioPlayFragment.updateList();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST:
                    radioPlayFragment.updateCollectList();
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
                case RadioConstants.MSG_OPEN_WITH_SCROLL:
                    radioPlayFragment.openWithScroll();
                    break;
                case RadioConstants.MSG_UPDATE_RDS_SETTINGS:
                    radioPlayFragment.updateViewWithRDSSettingsChanged((RDSSettingsSwitch) msg.obj);
                    break;
                case RadioConstants.MSG_TIPS:
                    radioPlayFragment.updateSearchTips(true);
                    break;
                case RadioConstants.MSG_UPDATE_THEME:
                    radioPlayFragment.updateTheme();
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
                ToastUtil.showToast(getContext(), getString(R.string.radio_search_no_list));
            }
        }else {

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

    //增加主题变化的监听，以兼容fragment重载在主题设置overlay值之前，导致当次切换，cursor/bg ui还是上一个主题的
    private final ContentObserver themeChangedObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.sendEmptyMessage(RadioConstants.MSG_UPDATE_THEME);
        }
    };

    public void updateTheme(){
        boolean currentOverlay2 = "overlay2".equals(ProductUtils.getTheme(getContext()));
        Log.d(TAG,"currentOverlay2:"+currentOverlay2 + ", isOverlay2:"+isOverlay2);
        if (isOverlay2 != currentOverlay2){//不相等才需要
            isOverlay2 = currentOverlay2;
            if (isOverlay2){
                radioCursor.setVisibility(View.GONE);
                ivRadioPlayBg.setVisibility(View.GONE);
                radioCursor2.setVisibility(View.VISIBLE);
            }else {
                radioCursor2.setVisibility(View.GONE);
                ivRadioPlayBg.setVisibility(View.VISIBLE);
                radioCursor.setVisibility(View.VISIBLE);
            }   
        }
    }
    private void registerThemeChangedObserver(){
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.registerContentObserver(Settings.System.getUriFor("com.desaysv.setting.theme.mode"), true,themeChangedObserver);
    }
    private void unregisterThemeChangedObserver(){
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.unregisterContentObserver(themeChangedObserver);
    }
}
