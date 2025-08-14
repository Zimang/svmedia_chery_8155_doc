package com.desaysv.moduleradio.ui;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.hardware.radio.RadioManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.textclassifier.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.mediacommonlib.view.CircleImageView;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.constants.RadioConstants;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.moduleradio.view.cursor.OnFrequencyChangedListener;
import com.desaysv.moduleradio.view.cursor.RadioCursor;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.moduleradio.utils.ClickUtils;
import java.lang.ref.WeakReference;

public class FMFragment extends BaseFragment implements View.OnClickListener{

    private CircleImageView ivRadioIcon;//logo，FM/AM不存在这个
    private ImageView ivRadioLike;//收藏
    private TextView tvRadioFreq;//频点值
    private RadioCursor rcCursor;//刻度尺
    //以下RDS相关
    private TextView tvRadioTP;
    private TextView tvRadioTA;
    private TextView tvRadioTF;
    private TextView tvRadioType;
    private TextView tvRadioName;
    private TextView tvRadioRt;

    private ImageView ivSingArm;

    //收音的控制器
    private IControlTool mRadioControl;

    //获取收音控制器和状态获取器的对象
    private IStatusTool mGetRadioStatusTool;

    //收音的状态获取器
    private IGetControlTool mGetControlTool;

    private RadioMessage currentRadioMessage = null;

    private MyHandler mHandler;

    //记忆是否为首次使用，是的话，要执行自动搜索

    private boolean isFirstUse;
    private static final String spName = "isFirstUseRadio";
    private static final String spKey = "isFirstUseRadioKey";

    private SharedPreferences spRadio;

    private SharedPreferences.Editor spRadioEditor;

    @Override
    public int getLayoutResID() {
        return R.layout.radio_fm_fragment;
    }

    @Override
    public void initView(View view) {
        ivRadioIcon = view.findViewById(R.id.ivRadioIcon);
        ivRadioLike = view.findViewById(R.id.ivRadioLike);
        tvRadioFreq = view.findViewById(R.id.tvRadioFreq);
        rcCursor = view.findViewById(R.id.rcCursor);
        tvRadioTP = view.findViewById(R.id.tvRadioTP);
        tvRadioTA = view.findViewById(R.id.tvRadioTA);
        tvRadioTF = view.findViewById(R.id.tvRadioTF);
        tvRadioType = view.findViewById(R.id.tvRadioType);
        tvRadioName = view.findViewById(R.id.tvRadioName);
        tvRadioRt = view.findViewById(R.id.tvRadioRt);
        ivSingArm = view.findViewById(R.id.ivSingArm);

        mHandler = new MyHandler(this);
        mRadioControl = ModuleRadioTrigger.getInstance().mRadioControl;
        mGetRadioStatusTool = ModuleRadioTrigger.getInstance().mGetRadioStatusTool;
        mGetControlTool = ModuleRadioTrigger.getInstance().mGetControlTool;

        spRadio = getContext().getSharedPreferences(spName,MODE_PRIVATE);
        spRadioEditor = spRadio.edit();
    }

    @Override
    public void initData() {
        updateCurrentInfo();
    }

    @Override
    public void initViewListener() {
        ivRadioLike.setOnClickListener(this);
        rcCursor.setOnFrequencyChangeListener(onFrequencyListChangeListener);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivRadioLike) {
            if (!ClickUtils.isAllowClick()){
                return;
            }
            Log.d(TAG, "onClick: ivRadioPlayCollect");
            if (!v.isSelected()) {
                if (RadioList.getInstance().getFMCollectRadioMessageList().size() > 29){
                    ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
                    return;
                }
                mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, mGetRadioStatusTool.getCurrentRadioMessage());
            } else {
                mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, mGetRadioStatusTool.getCurrentRadioMessage());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);
        mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
    }

    @Override
    public void onStop() {
        super.onStop();
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentOnResume = true;
        updateCurrentInfo();
        updateRDSFLag(mGetRadioStatusTool.getCurrentRDSFlagInfo());
        isFirstUse = spRadio.getBoolean(spKey,true);
        if (isFirstUse){
            Log.d(TAG,"isFirstUse,start ast");
            spRadioEditor.putBoolean(spKey,false);
            spRadioEditor.apply();
            mRadioControl.processCommand(RadioAction.SPECIFIES_AST, ChangeReasonData.UI_START, mGetRadioStatusTool.getFMRadioMessage());
        }else {
            Log.d(TAG,"not FirstUse,start open");
            mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.UI_START, mGetRadioStatusTool.getFMRadioMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentOnResume = false;
    }

    public void updateCurrentInfo(){
        currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG,"updateCurrentInfo,currentRadio:"+currentRadioMessage);
        if (currentRadioMessage.getRadioBand() != RadioManager.BAND_FM || currentRadioMessage.getRadioFrequency() == -1){
            return;
        }
        rcCursor.setBand(currentRadioMessage.getRadioBand());
        rcCursor.setFrequency(currentRadioMessage.getRadioFrequency());
        rcCursor.updateRadioMessage(currentRadioMessage);

        tvRadioFreq.setText(currentRadioMessage.getCalculateFrequency());
        ivRadioLike.setSelected(currentRadioMessage.isCollect());
        //RDS相关的待定
        if (currentRadioMessage.getRdsRadioText() != null && currentRadioMessage.getRdsRadioText().getProgramStationName() != null){
            tvRadioName.setText(currentRadioMessage.getRdsRadioText().getProgramStationName());
        }else {
            tvRadioName.setText("");
        }
        if (currentRadioMessage.getRdsRadioText() != null) {
            tvRadioRt.setText(currentRadioMessage.getRdsRadioText().getRadioText());
            tvRadioType.setText(RadioCovertUtils.changeTypeToString(getContext(),currentRadioMessage.getRdsRadioText().getProgramType()));
        } else {
            tvRadioRt.setText("");
            tvRadioType.setVisibility(View.GONE);
        }
//        if (currentRadioMessage.isTP()) {
//            tvRadioTP.setVisibility(View.VISIBLE);
//        } else {
//            tvRadioTP.setVisibility(View.GONE);
//        }

    }

    private boolean currentAniIsPlaying = false;
    public void updatePlayStatues() {
        if (mGetRadioStatusTool.isPlaying()){
            // 初始化旋转动画，旋转中心默认为控件中点
            Animation animation = AnimationUtils
                    .loadAnimation(getContext(),R.anim.rotation);
            animation.setFillAfter(true);
            if (!currentAniIsPlaying) {
                currentAniIsPlaying = true;
                ivSingArm.startAnimation(animation);
            }
        }else {
            Animation animation = AnimationUtils
                    .loadAnimation(getContext(),R.anim.rotation_out);
            animation.setFillAfter(true);
            if (currentAniIsPlaying) {
                currentAniIsPlaying = false;
                ivSingArm.startAnimation(animation);
            }
        }
    }

    /**
     * 更新列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList() {
        Log.d(TAG, "updateList,currentRadioMessage:" + currentRadioMessage);
        // 当前状态
        ivRadioLike.setSelected(currentRadioMessage.isCollect());
    }

    /**
     * 根据RDSFlag状态更新界面
     */
    public void updateRDSFLag(RDSFlagInfo info) {
        Log.d(TAG, "updateRDSFLag,info:" + info);
        if (info != null && (mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getRds() == 1)) {
            tvRadioTP.setVisibility(info.getTp() == 1 ? View.VISIBLE : View.GONE);
            tvRadioTA.setVisibility(mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getTa() == 1 ?View.VISIBLE :View.GONE);
            tvRadioTF.setVisibility(info.getAf() == 1 ? mGetRadioStatusTool.getRDSSettingsSwitchStatus() != null && mGetRadioStatusTool.getRDSSettingsSwitchStatus().getAf() == 1 ?View.VISIBLE : View.GONE : View.GONE);
        } else {
            tvRadioTP.setVisibility(View.GONE);
            tvRadioTA.setVisibility(View.GONE);
            tvRadioTF.setVisibility(View.GONE);
        }
    }

    public void updateViewWithRDSSettingsChanged(RDSSettingsSwitch rdsSettingsSwitch){
        Log.d(TAG,"updateViewWithRDSSettingsChanged,:"+rdsSettingsSwitch);
        if (rdsSettingsSwitch == null) {
            return;
        }
        if (rdsSettingsSwitch.getRds() == 1){//RDS开关打开
            //根据各自状态设置
            tvRadioTA.setVisibility(rdsSettingsSwitch.getTa() == 1 ?View.VISIBLE : View.GONE);
            tvRadioTF.setVisibility(rdsSettingsSwitch.getAf() == 1 ? mGetRadioStatusTool.getCurrentRDSFlagInfo() != null && mGetRadioStatusTool.getCurrentRDSFlagInfo().getAf() == 1 ?View.VISIBLE:View.GONE : View.GONE);
            if (currentRadioMessage.getRdsRadioText() != null) {
                tvRadioType.setVisibility(View.VISIBLE);
            }
        }else {//RDS开关关闭，那么全部都要隐藏
            tvRadioTA.setVisibility(View.GONE);
            tvRadioTF.setVisibility(View.GONE);
            tvRadioType.setVisibility(View.GONE);
        }
    }

    private boolean isFragmentOnResume = false;
    private OnFrequencyChangedListener onFrequencyListChangeListener = new OnFrequencyChangedListener() {

        @Override
        public void onChanged(int band, float frequency) {
            Log.d(TAG, "onChanged: band = " + band + " frequency = " + frequency);
            //列表滚动导致界面刷新要单独开来，避免两个刷新互相影响,这个只是滚动用的，值刷新显示，不刷新播放状态
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
        WeakReference<FMFragment> weakReference;

        MyHandler(FMFragment fmFragment) {
            weakReference = new WeakReference<>(fmFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final FMFragment fmFragment = weakReference.get();
            Log.d(fmFragment.TAG, "handleMessage:" + msg.what);
            if (fmFragment.isDetached()){
                Log.d(fmFragment.TAG, "radioPlayFragment.isDetach");
                return;
            }
            switch (msg.what) {
                case RadioConstants.MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE:
                    fmFragment.updateCurrentInfo();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_PLAY_STATUS:
                    fmFragment.updatePlayStatues();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_FM_LIST:
                case RadioConstants.MSG_UPDATE_RADIO_COLLECT_LIST:
                case RadioConstants.MSG_UPDATE_RADIO_AM_LIST:
                    fmFragment.updateList();
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_SEARCH_STATUS:
                    break;
                case RadioConstants.MSG_UPDATE_RADIO_RDS_FLAG:
                    fmFragment.updateRDSFLag((RDSFlagInfo) msg.obj);
                    break;
                case RadioConstants.MSG_OPEN_WITH_SCROLL:
                    break;
                case RadioConstants.MSG_UPDATE_RDS_SETTINGS:
                    fmFragment.updateViewWithRDSSettingsChanged((RDSSettingsSwitch) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}
