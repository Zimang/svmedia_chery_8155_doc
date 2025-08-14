package com.desaysv.moduledab.fragment;

import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABListAllAdapter;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduledab.view.SpaceItemDecoration;

import java.util.List;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表的全部列表
 */
public class DABListAllFragment extends DABListBaseFragment{

    private static final String TAG = "DABListAllFragment";
    private RecyclerView rvDABList;
    private DABListAllAdapter dabListAllAdapter;

    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ?  R.layout.fragment_dab_listall_layout_right: R.layout.fragment_dab_listall_layout;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        rvDABList = view.findViewById(R.id.rvDABList);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvDABList.addItemDecoration( new SpaceItemDecoration(getResources().getInteger(R.integer.dab_item_star)));
        rvDABList.setLayoutManager(layoutManager);
        dabListAllAdapter = new DABListAllAdapter(getContext(),this);
        rvDABList.setAdapter(dabListAllAdapter);
    }

    @Override
    public void updateList() {
        dabListAllAdapter.updateDabList(getCurrentShowList());
    }

    @Override
    public void updateCurrentListType(RadioMessage radioMessage, boolean enterNextList) {
        //do nothing
    }

    @Override
    public int getCurrentListType() {
        return Constant.LIST_TYPE_ALL;
    }

    @Override
    public List<RadioMessage> getCurrentShowList() {
        return RadioList.getInstance().getDABEffectRadioMessageList();
    }
}
