package com.desaysv.moduledab.fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.mediacommonlib.view.CircleImageView;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.common.DABMsg;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CtgUtils;
import com.desaysv.svlibtoast.ToastUtil;
import java.util.Arrays;
import java.lang.ref.WeakReference;

public class DABPlayFragment extends BaseFragment implements View.OnClickListener, IDABOperationListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "DABPlayFragment";

    private byte[] currentLogo;
    private DABPlayHandler mHandler;

    private CircleImageView ivDABLogo;
    private ImageView ivDABLike;
    private TextView tvDABName;
    private TextView tvDABPTYType;
    private TextView tvDABEnsemble;
    private TextView tvDABRT;

    private ImageView ivSingArm;
    @Override
    public int getLayoutResID() {
        return R.layout.fragment_dabplay_layout;
    }

    @Override
    public void initView(View view) {
        mHandler = new DABPlayHandler(this);
        ivDABLogo = view.findViewById(R.id.ivDABLogo);
        ivDABLike = view.findViewById(R.id.ivDABLike);
        tvDABName = view.findViewById(R.id.tvDABName);
        tvDABPTYType = view.findViewById(R.id.tvDABPTYType);
        tvDABEnsemble = view.findViewById(R.id.tvDABEnsemble);
        tvDABRT = view.findViewById(R.id.tvDABRT);
        ivSingArm = view.findViewById(R.id.ivSingArm);
    }

    @Override
    public void initData() {
        updateAll();
    }

    @Override
    public void initViewListener() {
        ivDABLike.setOnClickListener(this);
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
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.UI_START, DABTrigger.getInstance().mRadioStatusTool.getDABRadioMessage());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        currentLogo = null;
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
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
        if (R.id.ivDABLike == id){
            RadioMessage currentMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
            if (currentMessage.isCollect()){
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, currentMessage);
            }else {
                if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 19) {
                    ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
                } else {
                    DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, currentMessage);
                }
            }
        }
    }


    private void updateAll(){
        updateCurrentRadio();
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
                    || (radioMessage.getDabMessage().getProgramStationName().length() < 1 && radioMessage.getDabMessage().getEnsembleLabel().length() < 1)
                    || RadioList.getInstance().getDABEffectRadioMessageList().size() < 1){
                ToastUtil.showToast(getContext(),getString(R.string.dab_no_signal));
            }else {
                byte[] logoList = radioMessage.getDabMessage().getLogoDataList();
                if (currentLogo != null && Arrays.equals(currentLogo, logoList)){

                }else {
                    RequestOptions option = RequestOptions
                            .bitmapTransform(new RoundedCorners(8));
                    Glide.with(this).load(logoList)
                            .apply(option)
                            .into(ivDABLogo);
                }
                currentLogo = logoList;
                tvDABName.setText(radioMessage.getDabMessage().getShortProgramStationName());
                tvDABEnsemble.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
                tvDABPTYType.setText(CtgUtils.changeTypeToString(getContext(), radioMessage.getDabMessage().getProgramType()));
                if (radioMessage.getDabMessage().getDynamicLabel() != null && radioMessage.getDabMessage().getDynamicLabel().length() > 1) {// RT 内容不为空
                    tvDABRT.setText(radioMessage.getDabMessage().getDynamicLabel());
                } else {
                    tvDABRT.setText("");
                }
                ivDABLike.setSelected(radioMessage.isCollect());
            }
        }
    }

    /**
     * 更新播放状态
     */

    private boolean currentAniIsPlaying = false;
    public void updatePlayStatues() {
        if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()){
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
     * 根据搜索状态更新界面
     *
     * @param isSearching isSearching
     */
    public void updateSearchStatues(boolean isSearching) {
        if (isSearching) {
        } else {
        }
    }
    /**
     * 收藏列表更新
     */
    private void updateCollectList(){
        Log.d(TAG,"updateCollectList");
        ivDABLike.setSelected(DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().isCollect());
    }

    public void updateLogo(byte[] logoByte){
        RequestOptions option = RequestOptions
                .bitmapTransform(new RoundedCorners(8));
        Glide.with(getContext()).load(logoByte)
                .apply(option)
                .into(ivDABLogo);
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
            Message msg = new Message();
            msg.what = DABMsg.MSG_UPDATE_LOGO;
            msg.obj = logoByte;
            mHandler.sendMessage(msg);
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

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
                    break;
                case DABMsg.MSG_UPDATE_DAB_COLLECT_LIST:
                    dabPlayFragment.updateCollectList();
                    break;
                case DABMsg.MSG_CLICK_DAB:
                    break;
                case DABMsg.MSG_COLLECT_DAB:
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
