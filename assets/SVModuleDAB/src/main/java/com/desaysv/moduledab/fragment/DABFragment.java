package com.desaysv.moduledab.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.interfaze.IRadioMessageListChange;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABMainListAdapter;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.common.DABMsg;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduledab.view.SpaceItemDecoration;
import com.desaysv.svlibtoast.ToastUtil;

import java.lang.ref.WeakReference;

/**
 * created by ZNB on 2022-10-22
 * 有些共板项目或者下线配置为不支持DAB
 * 因此DAB单列出来做成一个类似插件的模块，和 Radio共用同一个 SVLibRadio
 * 需要的时候直接对接到对应的界面，不需要的就不使用
 */
public class DABFragment extends Fragment implements IDABOperationListener {

    private static final String TAG = "DABFragment";

    private RecyclerView rlDABMainList;
    private DABMainListAdapter dabMainListAdapter;
    private GridLayoutManager layoutManager;
    private DABFragmentHandler mHandler;
    private boolean needScan = true;//默认需要扫描

    private RelativeLayout rlDABScanning;
    private ImageView ivRadioLoading;
    private RelativeLayout rlDABEmpty;

    private boolean isItemClick = false;//判断是否item点击

    private boolean needUpdateListWhenResume = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(ProductUtils.isRightRudder() ? R.layout.fragment_dab_layout_right :R.layout.fragment_dab_layout, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        initView(view);
        initData();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        //监听电台内容的变化
        DABTrigger.getInstance().mGetControlTool.registerRadioStatusChangeListener(iRadioStatusChange);
        //监听电台列表的变化
        RadioList.getInstance().registerRadioListListener(iRadioMessageListChange);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startScanIfNeed();
        updateAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mHandler.removeMessages(DABMsg.MSG_SCAN_TIMEOUT);
        // 界面不可见是，设置界面搜索状态为false
        updateSearchingView(false);
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_FINISH);
        needUpdateListWhenResume = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        DABTrigger.getInstance().mGetControlTool.unregisterRadioStatusChangerListener(iRadioStatusChange);
        RadioList.getInstance().unRegisterRadioListListener(iRadioMessageListChange);
        if (null != loadingAnimator) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
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

    private void initView(View view) {
        Log.d(TAG, "initView");
        if (ProductUtils.isRightRudder()) {
            view.setRotationY(180);
        }
        rlDABMainList = view.findViewById(R.id.rlDABMainList);
        layoutManager = new GridLayoutManager(getContext(), 2);
        rlDABMainList.setLayoutManager(layoutManager);
        rlDABMainList.addItemDecoration(new SpaceItemDecoration(getResources().getInteger(R.integer.dab_item_star)));
        dabMainListAdapter = new DABMainListAdapter(getContext(), this);
        rlDABMainList.setAdapter(dabMainListAdapter);
        mHandler = new DABFragmentHandler(this);
        rlDABMainList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        rlDABScanning = view.findViewById(R.id.rlDABScanning);
        ivRadioLoading = view.findViewById(R.id.ivRadioLoading);
        rlDABEmpty = view.findViewById(R.id.rlDABEmpty);

        rlDABEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan(true);
            }
        });
    }

    /**
     * 初始化
     */
    private void initData() {
        Log.d(TAG, "initData");
        dabMainListAdapter.updateDabList(RadioList.getInstance().getDABEffectRadioMessageList());
        dabMainListAdapter.notifyDataSetChanged();
    }

    private final IRadioStatusChange iRadioStatusChange = new IRadioStatusChange() {
        @Override
        public void onCurrentRadioMessageChange() {
            Log.d(TAG, "onCurrentRadioMessageChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_RADIO);
        }

        @Override
        public void onPlayStatusChange(boolean isPlaying) {
            Log.d(TAG, "onPlayStatusChange,isPlaying:" + isPlaying);
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_PLAY_STATUES);
        }

        @Override
        public void onAstListChanged(int band) {
            Log.d(TAG, "onAstListChanged");
        }

        @Override
        public void onSearchStatusChange(boolean isSearching) {
            Log.d(TAG, "onSearchStatusChange");
            mHandler.sendMessage(mHandler.obtainMessage(DABMsg.MSG_UPDATE_SEARCH, isSearching));
        }

        @Override
        public void onSeekStatusChange(boolean isSeeking) {
            Log.d(TAG, "onSeekStatusChange");
        }

        @Override
        public void onAnnNotify(DABAnnNotify notify) {
            Log.d(TAG, "onAnnNotify");
        }

        @Override
        public void onRDSFlagInfoChange(RDSFlagInfo info) {
            Log.d(TAG, "onRDSFlagInfoChange");
        }
    };


    private final IRadioMessageListChange iRadioMessageListChange = new IRadioMessageListChange() {
        @Override
        public void onFMCollectListChange() {
            Log.d(TAG, "onFMCollectListChange");
        }

        @Override
        public void onAMCollectListChange() {
            Log.d(TAG, "onAMCollectListChange");
        }

        @Override
        public void onDABCollectListChange() {
            Log.d(TAG, "onDABCollectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_COLLECT_LIST);
        }

        @Override
        public void onFMEffectListChange() {
            Log.d(TAG, "onFMEffectListChange");
        }

        @Override
        public void onAMEffectListChange() {
            Log.d(TAG, "onAMEffectListChange");
        }

        @Override
        public void onDABEffectListChange() {
            Log.d(TAG, "onDABEffectListChange");
            mHandler.sendEmptyMessage(DABMsg.MSG_UPDATE_DAB_EFFECT_LIST);
        }

        @Override
        public void onFMAllListChange() {
            Log.d(TAG, "onFMAllListChange");
        }

        @Override
        public void onAMAllListChange() {
            Log.d(TAG, "onAMAllListChange");
        }

        @Override
        public void onDABAllListChange() {
            Log.d(TAG, "onDABAllListChange");
        }
    };


    /**
     * 提供给Tab选中时调用
     */
    public void startScanIfNeed() {
        Log.d(TAG, "startScanIfNeed");

        int effectSize = RadioList.getInstance().getDABEffectRadioMessageList().size();
        if (needScan) {
            Log.d(TAG, "startScanIfNeed: effectSize = " + effectSize);
            if (effectSize <= 0) {
                startScan(false);
                Log.d(TAG, "startScanIfNeed: to scan end");
                return;
            }
        }
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.UI_START, DABTrigger.getInstance().mRadioStatusTool.getDABRadioMessage());
    }

    /**
     * 开启扫描
     */
    public void startScan(boolean forceScan) {
        Log.d(TAG, "startScan");
        if (needScan) {
            needScan = false;//进程没挂且列表为空，此时走重新扫描按钮处理
            Log.d(TAG, "startScan: first scan");
        }
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.SPECIFIES_AST, forceScan ? ChangeReasonData.CLICK : ChangeReasonData.UI_START, DABTrigger.getInstance().mRadioStatusTool.getDABRadioMessage());
        //一分钟后发送扫描超时消息
        mHandler.sendEmptyMessageDelayed(DABMsg.MSG_SCAN_TIMEOUT, Constant.SCAN_TIMEOUT);
    }


    /**
     * 恢复的时候需要重新更新
     */
    public void updateAll() {
        RadioMessage currentRadio = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "updateAll,currentRadio:" + currentRadio);
        if (currentRadio.getDabMessage() != null) {//非DAB的情况下，不要更新
            updateCurrentRadio();
            updateEffectList();
            updateCollectList();
        }
    }

    /**
     * 播放内容更新
     */
    public void updateCurrentRadio() {
        if (isItemClick){
            isItemClick = false;
            onDABItemClickListener.onDABItemCLick();
        }else {
            RadioMessage currentRadio = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
            Log.d(TAG, "updateCurrentRadio,currentRadio:" + currentRadio);
            if (currentRadio.getDabMessage() != null) {
                if (needUpdateListWhenResume) {
                    needUpdateListWhenResume = false;
                    updateEffectList();
                }else {
                    dabMainListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updatePlayAnim(){
        dabMainListAdapter.updatePlayItemAnim();
    }


    /**
     * 更新有效列表
     */
    public void updateEffectList() {
        Log.d(TAG, "updateEffectList");
        if (RadioList.getInstance().getDABEffectRadioMessageList().size() > 0) {
            rlDABEmpty.setVisibility(View.GONE);
            rlDABMainList.setVisibility(View.VISIBLE);
            dabMainListAdapter.updateDabList(RadioList.getInstance().getDABEffectRadioMessageList());
            dabMainListAdapter.notifyDataSetChanged();
        }else {
            rlDABEmpty.setVisibility(View.VISIBLE);
            rlDABMainList.setVisibility(View.GONE);
        }
    }

    /**
     * 更新收藏列表
     */
    public void updateCollectList() {
        Log.d(TAG, "updateCollectList");
        //todo 收藏列表看下UE再写
    }

    /**
     * 处理点击逻辑
     *
     * @param radioMessage
     */
    public void handleClickDAB(RadioMessage radioMessage) {
        //要么等打开电台的回调收到再跳转，要么把position传给PlayActivity，再根据position拿到当前播放内容
        //不然会导致跳转到PlayActivity后，拿到当前播放内容还是上一个的情况
        //如果是正在播放的电台，那就直接跳转
        RadioMessage currentRadioMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
        Log.d(TAG, "onItemClick: currentRadioMessage = " + currentRadioMessage);
        if (CompareUtils.isSameDAB(radioMessage,currentRadioMessage)) {
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
            onDABItemClickListener.onDABItemCLick();
        } else {
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
            isItemClick = true;
        }

    }

    /**
     * 处理收藏逻辑
     *
     * @param radioMessage
     */
    public void handleCollectDAB(RadioMessage radioMessage) {
        if (radioMessage.isCollect()) {
            DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
        } else {
            if (RadioList.getInstance().getDABCollectRadioMessageList().size() > 19){
                ToastUtil.showToast(getContext(), getString(R.string.dab_collect_fully));
            }else {
                DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, radioMessage);
            }
        }
    }

    public void updateViewWithScanTimeOut() {
        Log.d(TAG, "updateViewWithScanTimeOut: ");
        DABTrigger.getInstance().mRadioControl.processCommand(RadioAction.STOP_AST, ChangeReasonData.UI_CONTROL);
        rlDABScanning.setVisibility(View.GONE);
        updateEffectList();
    }

    public void updateSearchingView(boolean isSearching) {
        Log.d(TAG, "updateSearchingView,isSearching：" + isSearching);
        if (isSearching) {
            // 搜索中，
            rlDABMainList.setVisibility(View.GONE);
            rlDABEmpty.setVisibility(View.GONE);
        }else {
            updateEffectList();
            mHandler.removeMessages(DABMsg.MSG_SCAN_TIMEOUT);
        }
        startLoadingAni(isSearching);

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
        if (start) {
            rlDABScanning.setVisibility(View.VISIBLE);
            if (!loadingAnimator.isStarted()) {
                Log.d(TAG, "startLoadingAni: start begin");
                loadingAnimator.start();
            }
        } else {
            rlDABScanning.setVisibility(View.GONE);
            if (loadingAnimator.isStarted()) {
                Log.d(TAG, "startLoadingAni: cancel begin");
                loadingAnimator.cancel();
            }
        }
    }


    private static class DABFragmentHandler extends Handler {

        private WeakReference<DABFragment> weakReference;

        public DABFragmentHandler(DABFragment dabFragment) {
            weakReference = new WeakReference<>(dabFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            final DABFragment dabFragment = weakReference.get();
            // 只有界面显示了，才进行接收数据
            Lifecycle lifecycle = dabFragment.getLifecycle();
            Log.d(dabFragment.TAG, "handleMessage: lifecycle = " + lifecycle);
            if (null != lifecycle) {
                Lifecycle.State currentState = lifecycle.getCurrentState();
                Log.d(dabFragment.TAG, "handleMessage: currentState = " + currentState);
                if (Lifecycle.State.RESUMED != currentState) {
                    return;
                }
            }
            Log.d(TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
                case DABMsg.MSG_UPDATE_RADIO:
                    dabFragment.updateCurrentRadio();
                    break;
                case DABMsg.MSG_UPDATE_DAB_EFFECT_LIST:
                    removeMessages(DABMsg.MSG_SCAN_TIMEOUT);
                    dabFragment.updateEffectList();
                    break;
                case DABMsg.MSG_UPDATE_DAB_COLLECT_LIST:
                    dabFragment.updateCollectList();
                    break;
                case DABMsg.MSG_CLICK_DAB:
                    dabFragment.handleClickDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_COLLECT_DAB:
                    dabFragment.handleCollectDAB((RadioMessage) msg.obj);
                    break;
                case DABMsg.MSG_UPDATE_SEARCH:
                    dabFragment.updateSearchingView((Boolean) msg.obj);
                    break;
                //搜索倒计时
                case DABMsg.MSG_SCAN_TIMEOUT:
                    dabFragment.updateViewWithScanTimeOut();
                    break;
                case DABMsg.MSG_UPDATE_PLAY_STATUES:
                    dabFragment.updatePlayAnim();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 列表项点击事件，透传给主Fragment进行界面替换
     */
    protected IOnDABItemClickListener onDABItemClickListener;

    public void setOnDABItemClickListener(IOnDABItemClickListener listener) {
        this.onDABItemClickListener = listener;
    }

    public interface IOnDABItemClickListener {
        void onDABItemCLick();
    }
}
