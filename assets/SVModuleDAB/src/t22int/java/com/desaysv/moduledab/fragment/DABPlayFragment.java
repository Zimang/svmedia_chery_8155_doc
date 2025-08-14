package com.desaysv.moduledab.fragment;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABPlayListAdapter;
import com.desaysv.moduledab.common.DABMsg;
import com.desaysv.moduledab.dialog.RTDialog;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.CtgUtils;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.svlibtoast.ToastUtil;
import com.sy.swbt.SettingSwitchView;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import com.desaysv.moduledab.utils.ClickUtils;

public class DABPlayFragment extends BaseFragment implements View.OnClickListener, IDABOperationListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "DABPlayFragment";

    private ImageView ivDABPlayerbg;
    private ImageView ivBack;
    private ImageView ivDABPause;
    private ImageView ivLogo;
    private TextView tvProgramStationName;
    private TextView tvEnsembleLabel;
    private TextView tvProgramType;
    private TextView tvDABListCount;

    private ImageView ivDABPre;
    private ImageView ivDABNext;
    private ImageView ivDABList;//进入DAB的细分列表
    private ImageView ivDABLike;//执行收藏/取消收藏 操作
    private ImageView ivDABSettings;//进入DAB的设置项
    private ImageView ivDABRT;//显示RT信息
    private ImageView ivDABSearch;
    private TextView tvEmptyView;
    private RelativeLayout rvDABPlaySearching;
    private ImageView ivDABLoading;
    private TextView tvEPG;//显示EPG信息
    private RelativeLayout rlDABPlay;
    private RelativeLayout rlDABSettings;
    private ScrollView slDABSettings;
    private ImageView ivDABSettingsBack;
    private ImageView ivSingArm;
    private ObjectAnimator singArmAni;

    private RecyclerView rlDABPlayList;
    private DABPlayListAdapter playListAdapter;

    private DABPlayHandler mHandler;

    private RTDialog rtDialog;

    private RelativeLayout rlDABNoSignal;


    /*以下是DAB公告设置部分*/
    //sf
    private RelativeLayout rlDABSF;//这个是用来响应点击整个项的情况
    private SettingSwitchView shDABSF;//这个是用来响应点击对应开关的情况

    //soft link
    private RelativeLayout rlDABSFSL;
    private SettingSwitchView shDABSFSL;

    //alarm
    private RelativeLayout rlDABAlarm;
    private SettingSwitchView shDABAlarm;

    //ta
    private RelativeLayout rlDABRTF;
    private SettingSwitchView shDABRTF;//TA

    //tf
    private RelativeLayout rlDABTF;
    private SettingSwitchView shDABTF;//Transport

    //warning
    private RelativeLayout rlDABWarning;
    private SettingSwitchView shDABWarning;

    //news
    private RelativeLayout rlDABNews;
    private SettingSwitchView shDABNews;

    //weather
    private RelativeLayout rlDABWeather;
    private SettingSwitchView shDABWeather;

    //event
    private RelativeLayout rlDABEvent;
    private SettingSwitchView shDABEvent;

    //special
    private RelativeLayout rlDABSpecial;
    private SettingSwitchView shDABSpecial;

    //program
    private RelativeLayout rlDABProgram;
    private SettingSwitchView shDABProgram;

    //sport
    private RelativeLayout rlDABSport;
    private SettingSwitchView shDABSport;

    //finance
    private RelativeLayout rlDABFinance;
    private SettingSwitchView shDABFinance;

    //DAB公告设置的数据结构，设置的内容都集中封装到里面传递给底层，底层也是返回同样的数据结构
    private DABAnnSwitch dabAnnSwitch;
    /*以上是DAB公告设置部分*/

    private byte[] currentLogo;

    private boolean isRTLView;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_dabplay_layout;
    }

    @Override
    public void initView(View view) {
        ivLogo = view.findViewById(R.id.ivLogo);
        tvProgramStationName = view.findViewById(R.id.tvProgramStationName);
        tvEnsembleLabel = view.findViewById(R.id.tvEnsembleLabel);
        ivDABPlayerbg = view.findViewById(R.id.ivDABPlayerbg);
        String theme = ProductUtils.getTheme(getContext());
        if ("overlay2".equals(theme)){
            ivDABPlayerbg.setVisibility(View.GONE);
        }
        tvProgramType = view.findViewById(R.id.tvProgramType);
        ivBack = view.findViewById(R.id.ivBack);
        ivDABPause = view.findViewById(R.id.ivDABPause);

        ivDABPre = view.findViewById(R.id.ivDABPre);
        ivDABNext = view.findViewById(R.id.ivDABNext);
        ivDABList = view.findViewById(R.id.ivDABList);
        ivDABLike = view.findViewById(R.id.ivDABLike);
        ivDABSettings = view.findViewById(R.id.ivDABSettings);
        ivDABRT = view.findViewById(R.id.ivDABRT);
        ivDABSearch = view.findViewById(R.id.ivDABSearch);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        rvDABPlaySearching = view.findViewById(R.id.rvDABPlaySearching);
        ivDABLoading = view.findViewById(R.id.ivDABLoading);
        tvEPG = view.findViewById(R.id.tvEPG);
        rlDABPlay = view.findViewById(R.id.rlDABPlay);
        rlDABSettings = view.findViewById(R.id.rlDABSettings);
        ivDABSettingsBack = view.findViewById(R.id.ivDABSettingsBack);
        slDABSettings = view.findViewById(R.id.slDABSettings);
        tvDABListCount = view.findViewById(R.id.tvDABListCount);
        ivSingArm = view.findViewById(R.id.ivSingArm);
        rlDABNoSignal = view.findViewById(R.id.rlDABNoSignal);

        rtDialog = new RTDialog(getContext(),R.style.dialogstyle);

        rlDABPlayList = view.findViewById(R.id.rlDABPlayList);
        rlDABPlayList.setLayoutManager(new LinearLayoutManager(getContext()));
        playListAdapter = new DABPlayListAdapter(getContext(),this);
        rlDABPlayList.setAdapter(playListAdapter);
        mHandler = new DABPlayHandler(this);

        //初始化设置项
        //sf
        rlDABSF = view.findViewById(R.id.rlDABSF);
        shDABSF = view.findViewById(R.id.shDABSF);
        //soft link
        rlDABSFSL = view.findViewById(R.id.rlDABSFSL);
        shDABSFSL = view.findViewById(R.id.shDABSFSL);
        //alarm
        rlDABAlarm = view.findViewById(R.id.rlDABAlarm);
        shDABAlarm = view.findViewById(R.id.shDABAlarm);
        //ta
        rlDABRTF = view.findViewById(R.id.rlDABRTF);
        shDABRTF = view.findViewById(R.id.shDABRTF);
        //transport
        rlDABTF = view.findViewById(R.id.rlDABTF);
        shDABTF = view.findViewById(R.id.shDABTF);
        //warning
        rlDABWarning = view.findViewById(R.id.rlDABWarning);
        shDABWarning = view.findViewById(R.id.shDABWarning);
        //news
        rlDABNews = view.findViewById(R.id.rlDABNews);
        shDABNews = view.findViewById(R.id.shDABNews);
        //weather
        rlDABWeather = view.findViewById(R.id.rlDABWeather);
        shDABWeather = view.findViewById(R.id.shDABWeather);
        //event
        rlDABEvent = view.findViewById(R.id.rlDABEvent);
        shDABEvent = view.findViewById(R.id.shDABEvent);
        //special
        rlDABSpecial = view.findViewById(R.id.rlDABSpecial);
        shDABSpecial = view.findViewById(R.id.shDABSpecial);
        //program
        rlDABProgram = view.findViewById(R.id.rlDABProgram);
        shDABProgram = view.findViewById(R.id.shDABProgram);
        //sport
        rlDABSport = view.findViewById(R.id.rlDABSport);
        shDABSport = view.findViewById(R.id.shDABSport);
        //finance
        rlDABFinance = view.findViewById(R.id.rlDABFinance);
        shDABFinance = view.findViewById(R.id.shDABFinance);

        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        isRTLView = ProductUtils.isRtlView(getContext());
    }

    @Override
    public void initData() {
    }

    @Override
    public void initViewListener() {
        ivBack.setOnClickListener(this);
        ivDABPre.setOnClickListener(this);
        ivDABPause.setOnClickListener(this);
        ivDABNext.setOnClickListener(this);
        ivDABList.setOnClickListener(this);
        ivDABLike.setOnClickListener(this);
        ivDABSettings.setOnClickListener(this);
        ivDABRT.setOnClickListener(this);
        ivDABSearch.setOnClickListener(this);
        tvEPG.setOnClickListener(this);
        ivDABSettingsBack.setOnClickListener(this);

        rlDABSF.setOnClickListener(this);
        shDABSF.setOnCheckedChangeListener(this);
        //soft link
        rlDABSFSL.setOnClickListener(this);
        shDABSFSL.setOnCheckedChangeListener(this);
        //alarm
        rlDABAlarm.setOnClickListener(this);
        shDABAlarm.setOnCheckedChangeListener(this);
        //ta
        rlDABRTF.setOnClickListener(this);
        shDABRTF.setOnCheckedChangeListener(this);
        //transport
        rlDABTF.setOnClickListener(this);
        shDABTF.setOnCheckedChangeListener(this);
        //warning
        rlDABWarning.setOnClickListener(this);
        shDABWarning.setOnCheckedChangeListener(this);
        //news
        rlDABNews.setOnClickListener(this);
        shDABNews.setOnCheckedChangeListener(this);
        //weather
        rlDABWeather.setOnClickListener(this);
        shDABWeather.setOnCheckedChangeListener(this);
        //event
        rlDABEvent.setOnClickListener(this);
        shDABEvent.setOnCheckedChangeListener(this);
        //special
        rlDABSpecial.setOnClickListener(this);
        shDABSpecial.setOnCheckedChangeListener(this);
        //program
        rlDABProgram.setOnClickListener(this);
        shDABProgram.setOnCheckedChangeListener(this);
        //sport
        rlDABSport.setOnClickListener(this);
        shDABSport.setOnCheckedChangeListener(this);
        //finance
        rlDABFinance.setOnClickListener(this);
        shDABFinance.setOnCheckedChangeListener(this);

        ivDABPre.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        ivDABNext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        updateAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        if (null != loadingAnimator) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        currentLogo = null;
        if (singArmAni != null){
            singArmAni.cancel();
            singArmAni = null;
        }
    }

    @Override
    public void onClickDAB(RadioMessage radioMessage) {
        Log.d(TAG, "onClickDAB");
        Message message = new Message();
        message.what = DABMsg.MSG_CLICK_DAB;
        message.obj = radioMessage;
        mHandler.sendMessage(message);
    }

    @Override
    public void onCollectDAB(RadioMessage radioMessage) {
        Log.d(TAG, "onCollectDAB,radioMessage：" + radioMessage);
        Message message = new Message();
        message.what = DABMsg.MSG_COLLECT_DAB;
        message.obj = radioMessage;
        mHandler.sendMessage(message);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.ivDABPre == id){
            if (isRTLView) {
                //next
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_FORWARD,ChangeReasonData.CLICK);
                //埋点：下一曲
                PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.NEXT
                        ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                        ,Point.Field.RadioName,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                        ,Point.Field.Mhz,String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
            }else {
                //pre
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_BACKWARD, ChangeReasonData.CLICK);
                //埋点：上一曲
                PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate, Point.Field.PlayOperType, Point.FieldValue.PRE
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                        , Point.Field.Mhz, String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
            }
        }else if (R.id.ivDABNext == id){
            if (isRTLView) {
                //pre
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_BACKWARD, ChangeReasonData.CLICK);
                //埋点：上一曲
                PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate, Point.Field.PlayOperType, Point.FieldValue.PRE
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                        , Point.Field.Mhz, String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
            } else {
                //next
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STEP_FORWARD, ChangeReasonData.CLICK);
                //埋点：下一曲
                PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate, Point.Field.PlayOperType, Point.FieldValue.NEXT
                        , Point.Field.OperStyle, Point.FieldValue.OpeCLick
                        , Point.Field.RadioName, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                        , Point.Field.Mhz, String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
            }
        }else if (R.id.ivDABPause == id){
            //play/stop
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.PLAY_OR_PAUSE,ChangeReasonData.CLICK);

            //埋点：播放/暂停
            if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()){
                PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.PLAY
                        ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                        ,Point.Field.RadioName,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                        ,Point.Field.Mhz,String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
            }else {
                PointTrigger.getInstance().trackEvent(Point.KeyName.DABOperate,Point.Field.PlayOperType,Point.FieldValue.PAUSE
                        ,Point.Field.OperStyle,Point.FieldValue.OpeCLick
                        ,Point.Field.RadioName,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                        ,Point.Field.Mhz,String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
            }

        }else if (R.id.ivBack == id){
            ivBack.setVisibility(View.GONE);
            startLoadingAni(false);
            backClickListener.onDABBackClick();
        }else if (R.id.ivDABLike == id){
            if (ClickUtils.isAllowClick()) {
                if (DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().isCollect()) {
                    DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage());
                    //埋点：取消收藏
                    PointTrigger.getInstance().trackEvent(Point.KeyName.DABCollect,Point.Field.CollOperType,Point.FieldValue.UNCOLLECT
                            ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                            ,Point.Field.RadioName,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                            ,Point.Field.Mhz,String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
                } else {
                    if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 19) {
                        ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
                    } else {
                        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage());
                        //埋点：收藏
                        PointTrigger.getInstance().trackEvent(Point.KeyName.DABCollect,Point.Field.CollOperType,Point.FieldValue.COLLECT
                                ,Point.Field.OperStyle,Point.FieldValue.OpeVR
                                ,Point.Field.RadioName,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramStationName()
                                ,Point.Field.Mhz,String.valueOf(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()));
                    }
                }
            }
        }else if (R.id.ivDABSettings == id){
            updateViewEnterSettings(true);
        }else if (R.id.ivDABRT == id){
            rtDialog.show();//先show，show过之后才可以更新内容，否则内容不会更新
            if (DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage() != null){
                if (DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage() != null){
                    rtDialog.updateDetails(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getDynamicLabel());
                }
            }
        }else if (R.id.ivDABSettingsBack == id){
            updateViewEnterSettings(false);
        }else if (R.id.ivDABList == id){
            backClickListener.onDABPlayEnterListClick();
        }else if (R.id.tvEPG == id){
            backClickListener.onDABPlayEnterEPGClick();
        }else if (R.id.ivDABSearch == id){
            if (ClickUtils.isAllowClick()) {
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.AST, ChangeReasonData.CLICK);
            }
        }
    }

    /**
     * 进入或者退出DAB设置界面时，更新UI
     * @param enter
     */
    private void updateViewEnterSettings(boolean enter){
        Log.d(TAG,"updateViewEnterSettings:"+enter);
        if (enter){
            slDABSettings.scrollTo(0,0);
            rlDABSettings.setVisibility(View.VISIBLE);
            rlDABPlay.setVisibility(View.GONE);
            ivBack.setVisibility(View.GONE);
        }else {
            rlDABSettings.setVisibility(View.GONE);
            rlDABPlay.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.VISIBLE);
        }
    }


    private void updateAll(){
        updateCurrentRadio();
        updateEffectList();
        updateCollectList();
        updateDABSettings();
        updatePlayStatues();
        updateSearchStatues(false);//重新起来的时候，肯定处于非搜索状态
    }

    /**
     * 更新DAB设置项的显示
     */
    private static final int DAB_SETTING_ON = 1;
    private static final int DAB_SETTING_OFF = 0;
    public void updateDABSettings(){
        dabAnnSwitch = DABTrigger.getInstance().mRadioStatusTool.getDABAnnSwitchStatus();
        if (dabAnnSwitch != null) {//默认使用系统返回的值
            shDABSF.setChecked(dabAnnSwitch.getServiceFollow() == DAB_SETTING_ON);
            shDABSFSL.setChecked(dabAnnSwitch.getServiceFollow() == DAB_SETTING_ON);
            shDABAlarm.setChecked(dabAnnSwitch.getAlarm() == DAB_SETTING_ON);
            shDABRTF.setChecked(dabAnnSwitch.getRoadTrafficFlash() == DAB_SETTING_ON);
            shDABTF.setChecked(dabAnnSwitch.getTransportFlash() == DAB_SETTING_ON);
            shDABWarning.setChecked(dabAnnSwitch.getWarning() == DAB_SETTING_ON);
            shDABNews.setChecked(dabAnnSwitch.getNewsFlash() == DAB_SETTING_ON);
            shDABWeather.setChecked(dabAnnSwitch.getAreaWeatherFlash() == DAB_SETTING_ON);
            shDABEvent.setChecked(dabAnnSwitch.getEventAnnouncement() == DAB_SETTING_ON);
            shDABSpecial.setChecked(dabAnnSwitch.getSpecialEvent() == DAB_SETTING_ON);
            shDABProgram.setChecked(dabAnnSwitch.getProgramInformation() == DAB_SETTING_ON);
            shDABSport.setChecked(dabAnnSwitch.getSportReport() == DAB_SETTING_ON);
            shDABFinance.setChecked(dabAnnSwitch.getFinancialReport() == DAB_SETTING_ON);
        }
    }


    /**
     * 更新当前播放内容
     */
    private void updateCurrentRadio(){
        RadioMessage radioMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG,"updateCurrentRadio，radioMessage："+radioMessage);
        if (radioMessage.getDabMessage() == null){

        }else {

            if ((radioMessage.getDabMessage().getProgramStationName() == null && radioMessage.getDabMessage().getEnsembleLabel() == null)
                    || (radioMessage.getDabMessage().getProgramStationName().length() < 1 && radioMessage.getDabMessage().getEnsembleLabel().length() < 1)){
                rlDABNoSignal.setVisibility(View.VISIBLE);
            }else {
                rlDABNoSignal.setVisibility(View.GONE);
                //优先使用Sls
                byte[] logoList = radioMessage.getDabMessage().getSlsDataList();
                //次级使用存储的Logo
                if (logoList == null || logoList.length < 1){
                    logoList = ListUtils.getOppositeDABLogo(radioMessage);
                }
                //最后使用当前获取到的Logo
                if (logoList == null){
                    logoList = radioMessage.getDabMessage().getLogoDataList();
                }
                if (currentLogo != null && Arrays.equals(currentLogo, logoList)){
                    Log.d(TAG,"updateCurrentRadio，currentLogo is same");
                }else {
                    Log.d(TAG,"updateCurrentRadio，update currentLogo");
                    if ((currentLogo == null || currentLogo.length == 0) && (logoList == null || logoList.length == 0)){

                    }else {
                        RequestOptions option = RequestOptions
                                .bitmapTransform(new RoundedCorners(8))
                                .error(R.mipmap.img_play_dab);
                        Glide.with(this).load(logoList)
                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .apply(option)
                                .into(ivLogo);
                    }
                }
                currentLogo = logoList;

                tvProgramStationName.setText(radioMessage.getDabMessage().getShortProgramStationName());
                tvEnsembleLabel.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
                tvProgramType.setText(CtgUtils.changeTypeToString(getContext(), radioMessage.getDabMessage().getProgramType()));
                if (radioMessage.getDabMessage().getDynamicLabel() != null && radioMessage.getDabMessage().getDynamicLabel().length() > 1) {// RT 内容不为空
                    Log.d(TAG, "RT 内容不为空");
                    ivDABRT.setEnabled(true);
                    rtDialog.updateDetails(radioMessage.getDabMessage().getDynamicLabel());
                } else {
                    ivDABRT.setEnabled(false);
                }
                // 刷新列表 ，更新当前播放高亮位置
                playListAdapter.notifyDataSetChanged();
                ivDABLike.setSelected(radioMessage.isCollect());

                ivDABLike.post(new Runnable() {
                    @Override
                    public void run() {
                        if (RadioList.getInstance().getDABEffectRadioMessageList().size() != 0) {
                            for (int i = 0; i < RadioList.getInstance().getDABEffectRadioMessageList().size(); i++) {//找到当前播放项在列表的位置
                                if (CompareUtils.isSameDAB(radioMessage,RadioList.getInstance().getDABEffectRadioMessageList().get(i))){
                                    rlDABPlayList.smoothScrollToPosition(i);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 更新播放状态
     */
    private void updatePlayStatues(){
        boolean isPlaying = DABTrigger.getInstance().mRadioStatusTool.isPlaying();
        ivDABPause.setSelected(!isPlaying);
        playListAdapter.notifyDataSetChanged();
        if (isPlaying){
            // 初始化旋转动画，旋转中心默认为控件中点
            singArmAni = ObjectAnimator.ofFloat(ivSingArm, "rotation", 0f, -30f);
        }else {
            singArmAni = ObjectAnimator.ofFloat(ivSingArm, "rotation", -30f, 0f);
        }
        singArmAni.setDuration(1000);
        singArmAni.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
        singArmAni.start();
    }



    /**
     * 根据搜索状态更新界面
     *
     * @param isSearching isSearching
     */
    public void updateSearchStatues(boolean isSearching) {
        if (isSearching) {
            rlDABPlayList.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.GONE);
            startLoadingAni(true);
        } else {
            startLoadingAni(false);
            rlDABPlayList.setVisibility(View.VISIBLE);
        }
    }

    private ObjectAnimator loadingAnimator;

    protected void startLoadingAni(boolean start) {
        Log.d(TAG, "startLoadingAni: start = " + start);
        if (loadingAnimator == null) {
            // 初始化旋转动画，旋转中心默认为控件中点
            loadingAnimator = ObjectAnimator.ofFloat(ivDABLoading, "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        Log.d(TAG, "startLoadingAni: isStarted = " + loadingAnimator.isStarted() + " isRunning = " + loadingAnimator.isRunning());
        if (start) {
            rvDABPlaySearching.setVisibility(View.VISIBLE);
            Log.d(TAG, "startLoadingAni: start");
            loadingAnimator.start();
        } else {
            loadingAnimator.cancel();
            rvDABPlaySearching.setVisibility(View.GONE);
            Log.d(TAG, "startLoadingAni: end");
        }
    }

    /**
     * 有效列表更新
     */
    private void updateEffectList(){
        Log.d(TAG,"updateEffectList");
        if (DABTrigger.getInstance().mRadioStatusTool.isSearching()){
            Log.d(TAG,"updateEffectList when searching,return");
            tvDABListCount.setText(String.format(getResources().getString(R.string.dab_station_list), playListAdapter.getItemCount()));
            return;
        }
        List<RadioMessage> currentList = RadioList.getInstance().getDABEffectRadioMessageList();//ListUtils.getCurrentPlayList(getContext());
        playListAdapter.updateDabList(currentList);
        playListAdapter.notifyDataSetChanged();
        tvDABListCount.setText(String.format(getResources().getString(R.string.dab_station_list),playListAdapter.getItemCount()));
        if (currentList.size() < 1){
            tvEmptyView.setVisibility(View.VISIBLE);
            rlDABPlayList.setVisibility(View.GONE);
        }else {
            tvEmptyView.setVisibility(View.GONE);
            rlDABPlayList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 收藏列表更新
     */
    private void updateCollectList(){
        Log.d(TAG,"updateCollectList");
        playListAdapter.notifyDataSetChanged();
        ivDABLike.setSelected(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().isCollect());
    }

    public void updateLogo(byte[] logoByte){
        if (currentLogo != null && Arrays.equals(currentLogo, logoByte)){

        }else {
            RequestOptions option = RequestOptions
                    .bitmapTransform(new RoundedCorners(8))
                    .error(R.mipmap.img_play_dab);
            Glide.with(getContext()).load(logoByte)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .apply(option)
                    .into(ivLogo);
            currentLogo = logoByte;
        }
    }


    /**
     * 处理点击事件
     * @param radioMessage
     */
    public void handleClickDAB(RadioMessage radioMessage){
        Log.d(TAG,"handleClickDAB,radioMessage:"+radioMessage);
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
    }

    /**
     * 处理收藏逻辑
     * @param radioMessage
     */
    private void handleCollectDAB(RadioMessage radioMessage){
        Log.d(TAG,"handleCollectDAB,radioMessage:"+radioMessage);
        if (radioMessage.isCollect()) {
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
        }else {
            if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 19){
                ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
            }else {
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, radioMessage);
            }
        }
    }


    private final IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG,"onCurrentRadioMessageChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_RADIO);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG,"onPlayStatusChange,isPlaying:"+isPlaying);
            Message msg = new Message();
            msg.what = DABMsg.MSG_UPDATE_PLAY_STATUES;
            msg.obj = isPlaying;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG,"onAstListChanged");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {
            Log.d(TAG,"onSearchStatusChange");
            Message msg = new Message();
            msg.what = DABMsg.MSG_UPDATE_SEARCH;
            msg.obj = isSearching;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG,"onSeekStatusChange");
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {
            Log.d(TAG,"onAnnNotify");
        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG,"onRDSFlagInfoChange");
        }

        @Override
        public void onDABLogoChanged(byte[] logoByte) {
//            Message msg = new Message();
//            msg.what = DABMsg.MSG_UPDATE_LOGO;
//            msg.obj = logoByte;
//            mHandler.sendMessage(msg);
        }
    };


    private final IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG,"onFMCollectListChange");
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG,"onAMCollectListChange");
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG,"onDABCollectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_COLLECT_LIST);
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG,"onFMEffectListChange");
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG,"onAMEffectListChange");
        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG,"onDABEffectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG,"onFMAllListChange");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG,"onAMAllListChange");
        }

        @Override
        public void onDABAllListChange() {
            Log.d(TAG,"onDABAllListChange");
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG,"onCheckedChanged,isChecked:"+isChecked);
        int id = buttonView.getId();
        if (dabAnnSwitch != null) {
            if (id == R.id.shDABSF){//HardLink，需要HAL确认协议
                dabAnnSwitch.setServiceFollow(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：HardLink
                PointTrigger.getInstance().trackEvent(Point.KeyName.HserFollowSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.CLOSE:Point.FieldValue.OPEN);
                //埋点：HardLink
                PointTrigger.getInstance().trackEvent(Point.KeyName.HserFollowSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABSFSL) {//SoftLink，需要HAL确认协议
                dabAnnSwitch.setServiceFollow(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：SoftLink
                PointTrigger.getInstance().trackEvent(Point.KeyName.SserFollowSwitchClick,Point.Field.Switchflg,isChecked ? Point.FieldValue.CLOSE:Point.FieldValue.OPEN);
            } else if (id == R.id.shDABAlarm) {
                dabAnnSwitch.setAlarm(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Alarm
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_ALARM,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (buttonView.getId() == R.id.shDABRTF) {
                dabAnnSwitch.setRoadTrafficFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：TA
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_TA,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABTF) {
                dabAnnSwitch.setTransportFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：TF
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_TF,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABWarning) {
                dabAnnSwitch.setWarning(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Waring
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Warning,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABNews) {
                dabAnnSwitch.setNewsFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：News
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_News,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABWeather) {
                dabAnnSwitch.setAreaWeatherFlash(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Weather
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Weather,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABEvent) {
                dabAnnSwitch.setEventAnnouncement(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Event
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Event,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABSpecial) {
                dabAnnSwitch.setSpecialEvent(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Special
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Special,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABProgram) {
                dabAnnSwitch.setProgramInformation(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Program
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Program,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABSport) {
                dabAnnSwitch.setSportReport(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Sport
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Sport,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            } else if (id == R.id.shDABFinance) {
                dabAnnSwitch.setFinancialReport(isChecked ? DAB_SETTING_ON : DAB_SETTING_OFF);
                //埋点：Finance
                PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,Point.FieldValue.ANN_Finance,Point.Field.Switchflg,isChecked ? Point.FieldValue.OPEN : Point.FieldValue.CLOSE);
            }
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.SET_DAB_ANN_SWITCH,ChangeReasonData.CLICK,dabAnnSwitch);

            //埋点：公告设置
            PointTrigger.getInstance().trackEvent(Point.KeyName.AnnSwitchClick,Point.Field.AnnType,isChecked ? Point.FieldValue.CLOSE:Point.FieldValue.OPEN);
        }
    }


    private static class DABPlayHandler extends Handler {

        private WeakReference<DABPlayFragment> weakReference;

        public DABPlayHandler(DABPlayFragment dabPlayFragment){
            weakReference = new WeakReference<>(dabPlayFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final DABPlayFragment dabPlayFragment = weakReference.get();
            Log.d(TAG,"handleMessage:"+msg.what);
            if (dabPlayFragment == null || dabPlayFragment.isDetached()){
                Log.d(TAG, "dabPlayFragment.isDetach");
                return;
            }
            switch (msg.what){
                case DABMsg.MSG_UPDATE_RADIO:
                    dabPlayFragment.updateCurrentRadio();
                    break;
                case DABMsg.MSG_UPDATE_DAB_EFFECT_LIST:
                    dabPlayFragment.updateEffectList();
                    break;
                case DABMsg.MSG_UPDATE_DAB_COLLECT_LIST:
                    dabPlayFragment.updateCollectList();
                    break;
                case DABMsg.MSG_CLICK_DAB:
                    dabPlayFragment.handleClickDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_COLLECT_DAB:
                    dabPlayFragment.handleCollectDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_UPDATE_PLAY_STATUES:
                    dabPlayFragment.updatePlayStatues();
                    break;
                case DABMsg.MSG_UPDATE_SEARCH:
                    dabPlayFragment.updateSearchStatues((Boolean) msg.obj);
                    break;
                case DABMsg.MSG_UPDATE_LOGO:
                    dabPlayFragment.updateLogo((byte[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 左上角返回按钮的点击事件
     */
    private IOnBackClickListener backClickListener;

    public void setBackClickListener(IOnBackClickListener backClickListener){
        this.backClickListener = backClickListener;
    }

    public interface IOnBackClickListener{

        void onDABBackClick();

        /**
         * 在DAB播放界面进入到列表页
         */
        void onDABPlayEnterListClick();

        /**
         * 在DAB播放界面进入到EPG
         */
        void onDABPlayEnterEPGClick();
    }
}
