package com.desaysv.moduledab.fragment.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABListAllAdapter;
import com.desaysv.moduledab.adapter.DABListCtgAdapter;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.fragment.DABListBaseFragment;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.view.SpaceItemDecoration;

import java.util.List;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表的集合分类列表
 */
public class DABListEnsembleFragment extends DABListBaseFragment {

    private static final String TAG = "DABListEnsembleFragment";

    private RecyclerView rvDABList;
    private RecyclerView rvDABSecondList;
    private DABListCtgAdapter dabListCtgAdapter;//类别分类、集合分类的适配器
    private DABListAllAdapter dabSecondListAdapter;//具体类别分类、集合分类的适配器

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_dab_listctg_layout_t1ej;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        currentListType = Constant.LIST_TYPE_ENSEMBLE;
        rvDABList = view.findViewById(R.id.rvDABList);
        rvDABSecondList = view.findViewById(R.id.rvDABSecondList);
        dabListCtgAdapter = new DABListCtgAdapter(getContext(),this,Constant.LIST_TYPE_ENSEMBLE);
        dabSecondListAdapter = new DABListAllAdapter(getContext(),this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDABList.setLayoutManager(layoutManager);
        rvDABList.setAdapter(dabListCtgAdapter);

        LinearLayoutManager secondLayoutManager = new LinearLayoutManager(getContext());
        rvDABSecondList.setLayoutManager(secondLayoutManager);
        rvDABSecondList.setAdapter(dabSecondListAdapter);
    }

    @Override
    public void initViewListener() {
        super.initViewListener();
    }

    @Override
    public void initData() {
        super.initData();
        if (RadioList.getInstance().getDABEffectRadioMessageList().size() > 0) {
            currentId = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getEnsembleId();
        }
    }

    @Override
    public void updateCurrentListType(RadioMessage radioMessage, boolean enterNextList) {
        if (enterNextList){
            currentId = radioMessage.getDabMessage().getEnsembleId();
            currentListType = Constant.LIST_TYPE_ENSEMBLE_SUB;
        }else {
            currentListType = Constant.LIST_TYPE_ENSEMBLE;
        }
    }

    @Override
    public int getCurrentListType() {
        return currentListType;
    }

    @Override
    public List<RadioMessage> getCurrentShowList() {

        return RadioList.getInstance().getDABEffectRadioMessageList();
    }

    @Override
    public void updateList() {
        dabListCtgAdapter.updateDabList(ListUtils.collectWithEnsemble(RadioList.getInstance().getDABEffectRadioMessageList()));
        dabSecondListAdapter.updateDabList(ListUtils.filterWithEnsembleId(RadioList.getInstance().getDABEffectRadioMessageList(),currentId));
    }

    @Override
    public void updateCurrentInfo() {
        super.updateCurrentInfo();
        dabListCtgAdapter.notifyDataSetChanged();
        dabSecondListAdapter.notifyDataSetChanged();
    }
}
