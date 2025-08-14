package com.desaysv.moduleradio.ui;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.textclassifier.Log;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.adapter.RadioCollectListAdapter;
import com.desaysv.moduleradio.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class CollectListFragment extends RadioBaseListFragment implements RadioCollectListAdapter.OnItemClickListener {

    private RecyclerView rvRadioCollectList;
    private RelativeLayout rlRadioCollectEmpty;

    private RadioCollectListAdapter listAdapter;

    private boolean isItemClick = false;//判断是否item点击
    private boolean isItemClickDAB = false;//判断是否item点击DAB

    private boolean hasAM = true;
    private boolean hasMulti = false;

    private final List<RadioMessage> mCollectList = new ArrayList<>();

    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ? R.layout.radio_fragment_collect_right: R.layout.radio_fragment_collect;
    }

    @Override
    public void initView(View view) {
        if (ProductUtils.isRightRudder()) {
            view.setRotationY(180);
        }
        rvRadioCollectList = view.findViewById(R.id.rvRadioCollectList);
        //todo empty待UI给出后再补充完整
        rlRadioCollectEmpty = view.findViewById(R.id.rlRadioCollectEmpty);
        
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRadioCollectList.setLayoutManager(mGridLayoutManager);
        rvRadioCollectList.addItemDecoration(new SpaceItemDecoration(getResources().getInteger(R.integer.radio_item_star)));
        listAdapter = new RadioCollectListAdapter(getContext(), this);

        rvRadioCollectList.setAdapter(listAdapter);
        rvRadioCollectList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        hasAM = ProductUtils.hasAM();
        hasMulti = ProductUtils.hasMulti();
    }

    @Override
    public void initViewListener() {

    }

    @Override
    public void updateSearchingView(boolean isSearching) {
        Log.d(TAG, "updateSearchingView,isSearching：" + isSearching + " isHidden = " + isHidden());
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateRadioMessageView() {
        Log.d(TAG, "updateRadioMessageView isHidden = " + isHidden());
        if (isItemClick) {
            onItemClickListener.onItemCLick();
            isItemClick = false;
        } else if(isItemClickDAB){
            onItemClickListener.onItemCLickDAB();
            isItemClickDAB = false;
        } else {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updatePlayAnim() {
        Log.d(TAG, "updatePlayAnim isHidden = " + isHidden());
        listAdapter.updatePlayItemAnim();
    }

    @Override
    void updateCollectListFragment() {
        super.updateCollectListFragment();
        mCollectList.clear();
        if (hasMulti){
            mCollectList.addAll(RadioList.getInstance().getMultiCollectRadioMessageList());
            if (hasAM){
                mCollectList.addAll(RadioList.getInstance().getAMCollectRadioMessageList());
            }
        }else {
            List<RadioMessage> allAM = RadioList.getInstance().getAMCollectRadioMessageList();
            List<RadioMessage> allFM = RadioList.getInstance().getFMCollectRadioMessageList();
            mCollectList.addAll(allAM);
            mCollectList.addAll(allFM);
        }
        Log.d(TAG, "updateCollectListFragment: start");
        for (int i = 0; i < mCollectList.size(); i++) {
            Log.d(TAG, "updateCollectListFragment: " + mCollectList.get(i).toString());
        }
        Log.d(TAG, "updateCollectListFragment: end");
        if (mCollectList.size() > 0) {
            rvRadioCollectList.setVisibility(View.VISIBLE);
            rlRadioCollectEmpty.setVisibility(View.GONE);
            listAdapter.updateList(mCollectList);
        } else {
            rvRadioCollectList.setVisibility(View.GONE);
            rlRadioCollectEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(int position, RadioMessage radioMessage) {
        Log.d(TAG, "onItemClick:" + position);
        //要么等打开电台的回调收到再跳转，要么把position传给PlayActivity，再根据position拿到当前播放内容
        //不然会导致跳转到PlayActivity后，拿到当前播放内容还是上一个的情况
        //如果是正在播放的电台，那就直接跳转


        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){//当前点击的是 DAB
            if (mGetRadioStatusTool.getCurrentRadioMessage().getRadioType() == RadioMessage.DAB_TYPE){//当前播放的是 DAB
                if (radioMessage.getDabMessage().getServiceId() == mGetRadioStatusTool.getCurrentRadioMessage().getDabMessage().getServiceId()
                        && radioMessage.getDabMessage().getServiceComponentId() == mGetRadioStatusTool.getCurrentRadioMessage().getDabMessage().getServiceComponentId()
                        && radioMessage.getDabMessage().getFrequency() == mGetRadioStatusTool.getCurrentRadioMessage().getDabMessage().getFrequency()){
                    onItemClickListener.onItemCLick();
                }else {
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, radioMessage);
                    isItemClickDAB = true;
                }
            }
        }else {
            if (mGetRadioStatusTool.getCurrentRadioMessage().getRadioBand() == radioMessage.getRadioBand()
                    && mGetRadioStatusTool.getCurrentRadioMessage().getRadioFrequency() == radioMessage.getRadioFrequency()) {
                onItemClickListener.onItemCLick();
            } else {
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK, radioMessage);
                isItemClick = true;
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCollectListFragment();
        updateRadioMessageView();
    }
}
