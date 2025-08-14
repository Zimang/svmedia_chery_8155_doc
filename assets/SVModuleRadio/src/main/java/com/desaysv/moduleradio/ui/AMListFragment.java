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

public class AMListFragment extends RadioBaseListFragment implements RadioMessageListAdapter.OnItemClickListener {

    private RecyclerView rvRadioAMList;
    private RelativeLayout rlRadioAMEmpty;
    private RelativeLayout rlRadioAMScanning;
    private ImageView ivRadioLoading;

    private RadioMessageListAdapter listAdapter;

    private final List<RadioMessage> mAMList = new ArrayList<>();

    /**
     * 当前值，会切换到其他页面时，不能进行重置
     */
    private static boolean needScan = true;//默认需要扫描

    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ? R.layout.radio_fragment_am_right: R.layout.radio_fragment_am;
    }

    @Override
    public void initView(View view) {
        if (ProductUtils.isRightRudder()) {
            view.setRotationY(180);
        }
        rvRadioAMList = view.findViewById(R.id.rvRadioAMList);
        //todo empty和scanning待UI给出后再补充完整
        rlRadioAMEmpty = view.findViewById(R.id.rlRadioAMEmpty);
        rlRadioAMScanning = view.findViewById(R.id.rlRadioAMScanning);
        ivRadioLoading = view.findViewById(R.id.ivRadioLoading);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRadioAMList.setLayoutManager(mGridLayoutManager);
        rvRadioAMList.addItemDecoration(new SpaceItemDecoration(getResources().getInteger(R.integer.radio_item_star)));
        listAdapter = new RadioMessageListAdapter(getContext(), this);
        rvRadioAMList.setItemAnimator(null);
        rvRadioAMList.setAdapter(listAdapter);

        rvRadioAMList.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    public void initViewListener() {
        rlRadioAMEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: TO AST");
                //open需要等350ms之后，才允许点击搜索，否则会导致底层执行tune和搜索有时序上的冲突，导致搜索异常
                if (ClickUtils.isAllowClickSpecialAst()) {
                    rlRadioAMEmpty.setVisibility(View.GONE);
                    startScan(true);
                }
            }
        });
        //TODO 只用于测试
//        rlRadioAMEmpty.setOnLongClickListener(new View.OnLongClickListener() {
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
            rvRadioAMList.setVisibility(View.GONE);
            rlRadioAMEmpty.setVisibility(View.GONE);
        }else {
            updateAMListFragment();
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
            rlRadioAMScanning.setVisibility(View.VISIBLE);
            if (!loadingAnimator.isStarted()) {
                loadingAnimator.start();
            }
        } else {
            rlRadioAMScanning.setVisibility(View.GONE);
            if (loadingAnimator.isStarted()) {
                loadingAnimator.cancel();
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateRadioMessageView() {
        Log.d(TAG, "updateRadioMessageView: isItemClick = " + isItemClick);
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
        Log.d(TAG, "updatePlayAnim  ");
        listAdapter.notifyDataSetChanged();
    }

    @Override
    void updateAMListFragment() {
        Log.d(TAG, "updateAMListFragment: ");
        mAMList.clear();
        mAMList.addAll(RadioList.getInstance().getAMEffectRadioMessageList());
        Log.d(TAG, "updateAMListFragment: " + mAMList.size());
        listAdapter.updateList(mAMList);
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
        int effectSize = RadioList.getInstance().getAMEffectRadioMessageList().size();
        if (needScan) {
            Log.d(TAG, "startScanIfNeed: effectSize = " + effectSize);
            if (effectSize <= 0) {
                startScan(false);
                Log.d(TAG, "startScanIfNeed: to scan end");
                return;
            }
        }
        //切到AM时就需要打开，不管是否有有效电台
        mRadioControlTool.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.UI_START, mGetRadioStatusTool.getAMRadioMessage());
        //open需要等350ms之后，才允许点击搜索，否则会导致底层执行tune和搜索有时序上的冲突，导致搜索异常
        ClickUtils.setUiStartTime();
        updateAMListFragment();
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
        mRadioControlTool.processCommand(RadioAction.SPECIFIES_AST, forceScan ?  ChangeReasonData.CLICK :  ChangeReasonData.UI_START, mGetRadioStatusTool.getAMRadioMessage());
        if (needScan) {
            needScan = false;//进程没挂且列表为空，此时走重新扫描按钮处理
            Log.d(TAG, "startScan: first scan");
        }
        //埋点：搜索AM
        PointTrigger.getInstance().trackEvent(Point.KeyName.AMSearch,Point.Field.Mhz,mGetRadioStatusTool.getAMRadioMessage().getCalculateFrequency());
    }

    @Override
    public void updateViewWithScanTimeOut() {
        Log.d(TAG, "updateViewWithScanTimeOut: ");
        rlRadioAMScanning.setVisibility(View.GONE);
        showList();
    }

    private void showList() {
        Log.d(TAG, "showList: " + mAMList.size());
        if (mAMList.size() > 0) {
            rlRadioAMEmpty.setVisibility(View.GONE);
            rvRadioAMList.setVisibility(View.VISIBLE);
        } else {
            rlRadioAMEmpty.setVisibility(View.VISIBLE);
            rvRadioAMList.setVisibility(View.GONE);
        }
    }
}
