package com.desaysv.moduleradio.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.hardware.radio.RadioManager;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.textclassifier.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.adapter.RadioMessageListAdapter;
import com.desaysv.moduleradio.utils.ClickUtils;
import com.desaysv.moduleradio.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FMListFragment extends RadioBaseListFragment {

    private RecyclerView rvRadioFMList;
    private RelativeLayout rlRadioFMEmpty;
    private RelativeLayout rlRadioFMScanning;
    private ImageView ivRadioLoading;

    private RadioMessageListAdapter listAdapter;

    private final List<RadioMessage> mFMList = new ArrayList<>();

    /**
     * 当前值，会切换到其他页面时，不能进行重置
     */
    private static boolean needScan = true;//默认需要扫描


    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ? R.layout.radio_fragment_fm_right :R.layout.radio_fragment_fm;
    }

    @Override
    public void initView(View view) {
        if (ProductUtils.isRightRudder()) {
            view.setRotationY(180);
        }
        rvRadioFMList = view.findViewById(R.id.rvRadioFMList);
        //todo empty和scanning待UI给出后再补充完整
        rlRadioFMEmpty = view.findViewById(R.id.rlRadioFMEmpty);
        rlRadioFMScanning = view.findViewById(R.id.rlRadioFMScanning);
        ivRadioLoading = view.findViewById(R.id.ivRadioLoading);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRadioFMList.setLayoutManager(mGridLayoutManager);
        rvRadioFMList.addItemDecoration(new SpaceItemDecoration(getResources().getInteger(R.integer.radio_item_star)));
        listAdapter = new RadioMessageListAdapter(getContext(), this);
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
                if (ClickUtils.isAllowClickSpecialAst()) {
                    rlRadioFMEmpty.setVisibility(View.GONE);
                    startScan(true);
                }
            }
        });
        //TODO 只用于测试
//        rlRadioFMEmpty.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Log.d(TAG, "onLongClick: ");
//                Toast.makeText(getContext(), "current is open radio", Toast.LENGTH_LONG).show();
//                if (null != onItemClickListener) {
//                    onItemClickListener.onItemCLick();
//                }
//                return false;
//            }
//        });
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
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updatePlayAnim() {
        Log.d(TAG, "updatePlayAnim");
        listAdapter.notifyDataSetChanged();
    }

    @Override
    void updateFMListFragment() {
        Log.d(TAG, "updateFMListFragment: ");
        //搜索时不需要更新
        mFMList.clear();
        mFMList.addAll(RadioList.getInstance().getFMEffectRadioMessageList());
        Log.d(TAG, "updateFMListFragment,size:" + mFMList.size());
        listAdapter.updateList(mFMList);
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
        int effectSize = RadioList.getInstance().getFMEffectRadioMessageList().size();
        if (needScan) {
            Log.d(TAG, "startScanIfNeed: effectSize = " + effectSize);
            if (effectSize <= 0) {
                startScan(false);
                Log.d(TAG, "startScanIfNeed: to scan end");
                return;
            }
        }
        //切到FM时就需要打开，不管是否有有效电台
        mRadioControlTool.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.UI_START, mGetRadioStatusTool.getFMRadioMessage());
        //open需要等350ms之后，才允许点击搜索，否则会导致底层执行tune和搜索有时序上的冲突，导致搜索异常
        ClickUtils.setUiStartTime();
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
        mRadioControlTool.processCommand(RadioAction.SPECIFIES_AST, forceScan ?  ChangeReasonData.CLICK: ChangeReasonData.UI_START, mGetRadioStatusTool.getFMRadioMessage());
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
        showList();
    }

    private void showList() {
        Log.d(TAG, "showList: " + mFMList.size());
        if (mFMList.size() > 0) {
            rlRadioFMEmpty.setVisibility(View.GONE);
            rvRadioFMList.setVisibility(View.VISIBLE);
        } else {
            rlRadioFMEmpty.setVisibility(View.VISIBLE);
            rvRadioFMList.setVisibility(View.GONE);
        }
    }
}
