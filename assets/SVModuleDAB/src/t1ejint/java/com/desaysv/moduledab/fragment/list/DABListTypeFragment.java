package com.desaysv.moduledab.fragment.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.desaysv.moduledab.utils.CtgUtils;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.view.SpaceItemDecoration;

import java.util.List;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表的类别分类列表
 */
public class DABListTypeFragment extends DABListBaseFragment {

    private static final String TAG = "DABListTypeFragment";

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
        currentListType = Constant.LIST_TYPE_CATEGORY;
        rvDABList = view.findViewById(R.id.rvDABList);
        rvDABSecondList = view.findViewById(R.id.rvDABSecondList);
        dabListCtgAdapter = new DABListCtgAdapter(getContext(),this,Constant.LIST_TYPE_CATEGORY);
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
            currentId = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getProgramType();
        }
    }

    @Override
    public void updateCurrentListType(RadioMessage radioMessage, boolean enterNextList) {
        if (enterNextList){
            currentId = radioMessage.getDabMessage().getProgramType();
            currentListType = Constant.LIST_TYPE_CATEGORY_SUB;
        }else {
            currentListType = Constant.LIST_TYPE_CATEGORY;
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
        dabListCtgAdapter.updateDabList(ListUtils.collectWithProgramType(RadioList.getInstance().getDABEffectRadioMessageList()));
        dabSecondListAdapter.updateDabList(ListUtils.filterWithProgramType(RadioList.getInstance().getDABEffectRadioMessageList(),currentId));
    }

    @Override
    public void updateCurrentInfo() {
        super.updateCurrentInfo();
        dabListCtgAdapter.notifyDataSetChanged();
        dabSecondListAdapter.notifyDataSetChanged();
    }
}
