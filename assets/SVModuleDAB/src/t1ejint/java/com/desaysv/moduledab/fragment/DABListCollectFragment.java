package com.desaysv.moduledab.fragment;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABListAllAdapter;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.fragment.DABListBaseFragment;

import java.util.List;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表的收藏列表
 */
public class DABListCollectFragment extends DABListBaseFragment {

    private static final String TAG = "DABListCollectFragment";
    private RecyclerView rvDABList;
    private DABListAllAdapter dabListAllAdapter;
    private RelativeLayout rlDABEmpty;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_dab_listall_layout;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        rlDABEmpty = view.findViewById(R.id.rlDABEmpty);
        rvDABList = view.findViewById(R.id.rvDABList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDABList.setLayoutManager(layoutManager);
        dabListAllAdapter = new DABListAllAdapter(getContext(),this);
        rvDABList.setAdapter(dabListAllAdapter);
    }
    @Override
    public void updateCurrentListType(RadioMessage radioMessage, boolean enterNextList) {
        //do nothing
    }
    @Override
    public void updateList() {
        if (getCurrentShowList() == null || getCurrentShowList().size() < 1){
            rvDABList.setVisibility(View.GONE);
            rlDABEmpty.setVisibility(View.VISIBLE);
        }else {
            rlDABEmpty.setVisibility(View.GONE);
            rvDABList.setVisibility(View.VISIBLE);
            dabListAllAdapter.updateDabList(getCurrentShowList());
        }
    }

    @Override
    public int getCurrentListType() {
        return Constant.LIST_TYPE_COLLECT;
    }

    @Override
    public List<RadioMessage> getCurrentShowList() {
        return RadioList.getInstance().getDABCollectRadioMessageList();
    }

    @Override
    public void updateCurrentInfo() {
        super.updateCurrentInfo();
        dabListAllAdapter.notifyDataSetChanged();
    }

    @Override
    public void updatePlayStatus() {
        super.updatePlayStatus();
        dabListAllAdapter.updatePlayItemAnim();
    }
}
