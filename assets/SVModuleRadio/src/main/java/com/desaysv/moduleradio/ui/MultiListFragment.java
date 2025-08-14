package com.desaysv.moduleradio.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.textclassifier.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioMessageListAdapter;
import com.desaysv.moduleradio.adapter.RadioMultiListAdapter;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.moduleradio.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MultiListFragment extends RadioBaseListFragment implements RadioMultiListAdapter.OnItemClickListener {

    private RecyclerView rvRadioFMList;
    private RelativeLayout rlRadioFMEmpty;
    private RelativeLayout rlRadioFMScanning;
    private ImageView ivRadioLoading;

    private RadioMultiListAdapter listAdapter;

    private final List<RadioMessage> mMultiList = new ArrayList<>();

    /**
     * 当前值，会切换到其他页面时，不能进行重置
     */
    private static boolean needScan = true;//默认需要扫描


    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ? R.layout.radio_fragment_fm_right : R.layout.radio_fragment_fm_multi;
    }

    @Override
    public void initView(View view) {
        if (ProductUtils.isRightRudder()) {
            view.setRotationY(180);
        }
        rvRadioFMList = view.findViewById(R.id.rvRadioFMList);
        rlRadioFMEmpty = view.findViewById(R.id.rlRadioFMEmpty);
        rlRadioFMScanning = view.findViewById(R.id.rlRadioFMScanning);
        ivRadioLoading = view.findViewById(R.id.ivRadioLoading);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRadioFMList.setLayoutManager(mGridLayoutManager);
        rvRadioFMList.addItemDecoration(new SpaceItemDecoration(getResources().getInteger(R.integer.radio_item_star)));
        listAdapter = new RadioMultiListAdapter(getContext(), this);
        rvRadioFMList.setItemAnimator(null);
        rvRadioFMList.setAdapter(listAdapter);

        rvRadioFMList.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    public void initViewListener() {
        rlRadioFMEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: TO AST");
                rlRadioFMEmpty.setVisibility(View.GONE);
                startScan(true);
            }
        });
    }

    @Override
    public void updateSearchingView(boolean isSearching) {
        Log.d(TAG, "updateSearchingView,isSearching：" + isSearching);
        if (isSearching) {
            // 搜索中，
            rvRadioFMList.setVisibility(View.GONE);
            rlRadioFMEmpty.setVisibility(View.GONE);
        }else {
            updateFMListFragment();
        }
        startLoadingAni(isSearching);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingAnimator != null){
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
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
            rlRadioFMScanning.setVisibility(View.VISIBLE);
            if (!loadingAnimator.isStarted()) {
                loadingAnimator.start();
            }
        } else {
            rlRadioFMScanning.setVisibility(View.GONE);
            if (loadingAnimator.isStarted()) {
                loadingAnimator.cancel();
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateRadioMessageView() {
        Log.d(TAG, "updateRadioMessageView isItemClick = " + isItemClick);
        if (mGetRadioStatusTool.isSearching()){
            return;
        }
        if (isItemClick) {
            onItemClickListener.onItemCLick();
            isItemClick = false;
        } else {
//            listAdapter.notifyDataSetChanged();
            //updateRadioMessageView()方法有2种情况调用：
            // 1.onReusme()
            // 2.CurrentRadioInfo setCurrentRadioMessage
            // 当前播放信息变化的时候调用，没有设置新list
            listAdapter.notifyLastCurrentItem();
        }
    }

    @Override
    public void updatePlayAnim() {
        Log.d(TAG, "updatePlayAnim");
//        listAdapter.notifyDataSetChanged();
        //updatePlayAnim()只有CurrentRadioInfo  setPlaying设置当前是否在播放状态调用，没有设置新list
        listAdapter.notifyLastCurrentItem();
    }

    @Override
    void updateFMListFragment() {
        Log.d(TAG, "updateFMListFragment: ");
        updateLoadingAndListView(true);
    }

    /**
     * 更新数据和页面显示
     * @param needFilterSearching 是否需要在搜索时过滤设置 超时时会自动去掉界面搜索状态，不需要过滤，其他情况过滤
     */
    private void updateLoadingAndListView(boolean needFilterSearching) {
        Log.d(TAG, "updateFMListFragment,needFilterSearching:" + needFilterSearching);
        //搜索时不需要更新
        if (needFilterSearching && ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isSearching()){
            return;
        }
        mMultiList.clear();
        mMultiList.addAll(RadioCovertUtils.sortWithName(getContext(),RadioList.getInstance().getMultiRadioMessageList()));
        Log.d(TAG, "updateFMListFragment,size:" + mMultiList.size());
        listAdapter.updateList(mMultiList);
        showList();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        startScanIfNeed();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != loadingAnimator) {
            loadingAnimator.cancel();
            loadingAnimator = null;
        }
    }

    /**
     * 提供给Tab选中时调用
     */
    public void startScanIfNeed() {
        Log.d(TAG, "startScanIfNeed: needScan = " + needScan);
        int effectSize = RadioList.getInstance().getMultiRadioMessageList().size();
        if (needScan) {
            Log.d(TAG, "startScanIfNeed: effectSize = " + effectSize);
            //打个补丁，避免实车恢复出厂设置以后，由于DAB后台刷新了列表，导致不会自动扫描，从而出现没有FM的情况
            if (effectSize <= 0 || RadioList.getInstance().getFMEffectRadioMessageList().size() < 1) {
                startScan(false);
                Log.d(TAG, "startScanIfNeed: to scan end");
                return;
            }
        }
        //切到FM时就需要打开，不管是否有有效电台
        mRadioControlTool.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.UI_START, mGetRadioStatusTool.getDABorFMRadioMessage());

        updateFMListFragment();
        showList();
        Log.d(TAG, "startScanIfNeed: end");
    }

    /**
     * 开启扫描
     */
    @Override
    public void startScan(boolean forceScan) {
        super.startScan(forceScan);
        Log.d(TAG, "startScan: ");
        mRadioControlTool.processCommand(RadioAction.MULTI_AST, forceScan ? ChangeReasonData.CLICK : ChangeReasonData.UI_START);
        if (needScan) {
            needScan = false;//进程没挂且列表为空，此时走重新扫描按钮处理
            Log.d(TAG, "startScan: first scan");
        }
        //埋点：搜索FM
        PointTrigger.getInstance().trackEvent(Point.KeyName.FMSearch,Point.Field.Mhz,mGetRadioStatusTool.getFMRadioMessage().getCalculateFrequency());
    }

    /**
     * 搜索超时
     */
    @Override
    public void updateViewWithScanTimeOut() {
        Log.d(TAG, "updateViewWithScanTimeOut: ");
        rlRadioFMScanning.setVisibility(View.GONE);
        //超时隐藏loading，但是状态是searching
        updateLoadingAndListView(false);
    }

    private void showList() {
        Log.d(TAG, "showList: " + mMultiList.size());
        if (mMultiList.size() > 0) {
            rlRadioFMEmpty.setVisibility(View.GONE);
            rvRadioFMList.setVisibility(View.VISIBLE);
            RadioMessage radioMessage = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
            android.util.Log.d(TAG,"updateEffectList scroll to position begin，radioMessage："+radioMessage);
            for (int i = 0; i < mMultiList.size(); i++) {//找到当前播放项在列表的位置
                if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE){
                    if (radioMessage.getRadioFrequency() == mMultiList.get(i).getRadioFrequency()){
                        rvRadioFMList.scrollToPosition(i);
                    }
                }else {
                    if (CompareUtils.isSameDAB(radioMessage,mMultiList.get(i))){
                        rvRadioFMList.scrollToPosition(i);
                    }
                }
            }
        } else {
            rlRadioFMEmpty.setVisibility(View.VISIBLE);
            rvRadioFMList.setVisibility(View.GONE);
        }
    }
}
