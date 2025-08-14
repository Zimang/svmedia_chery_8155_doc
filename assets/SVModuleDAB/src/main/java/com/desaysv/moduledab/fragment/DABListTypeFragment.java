package com.desaysv.moduledab.fragment;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.DABListAllAdapter;
import com.desaysv.moduledab.adapter.DABListCtgAdapter;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.CtgUtils;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduledab.view.SpaceItemDecoration;

import java.util.List;

/**
 * created by ZNB on 2022-12-21
 * DAB播放页进入的电台列表的类别分类列表
 */
public class DABListTypeFragment extends DABListBaseFragment{

    private static final String TAG = "DABListTypeFragment";

    private RecyclerView rvDABList;
    private RelativeLayout rlDABSecondList;
    private ImageView ivDABSecondListBack;
    private TextView tvDABSecondListName;
    private RecyclerView rvDABSecondList;
    private DABListCtgAdapter dabListCtgAdapter;//类别分类、集合分类的适配器
    private DABListAllAdapter dabSecondListAdapter;//具体类别分类、集合分类的适配器

    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ? R.layout.fragment_dab_listctg_layout_right : R.layout.fragment_dab_listctg_layout;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        currentListType = Constant.LIST_TYPE_CATEGORY;
        rvDABList = view.findViewById(R.id.rvDABList);
        rlDABSecondList = view.findViewById(R.id.rlDABSecondList);
        ivDABSecondListBack = view.findViewById(R.id.ivDABSecondListBack);
        tvDABSecondListName = view.findViewById(R.id.tvDABSecondListName);
        rvDABSecondList = view.findViewById(R.id.rvDABSecondList);
        dabListCtgAdapter = new DABListCtgAdapter(getContext(),this,Constant.LIST_TYPE_CATEGORY);
        dabSecondListAdapter = new DABListAllAdapter(getContext(),this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        rvDABList.addItemDecoration(new SpaceItemDecoration(4,25));
        rvDABList.setLayoutManager(layoutManager);
        rvDABList.setAdapter(dabListCtgAdapter);
        rvDABList.setOverScrollMode(View.OVER_SCROLL_NEVER);

        GridLayoutManager secondLayoutManager = new GridLayoutManager(getContext(), 2);
        rvDABSecondList.setLayoutManager(secondLayoutManager);
        rvDABSecondList.setAdapter(dabSecondListAdapter);
    }

    @Override
    public void initViewListener() {
        super.initViewListener();
        ivDABSecondListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentListType(null,false);
                enterNextListener.onEnterNextList(false);
            }
        });
    }

    @Override
    public void updateCurrentListType(RadioMessage radioMessage, boolean enterNextList) {
        if (enterNextList){
            currentId = radioMessage.getDabMessage().getProgramType();
            currentListType = Constant.LIST_TYPE_CATEGORY_SUB;
            rvDABList.setVisibility(View.GONE);
            rlDABSecondList.setVisibility(View.VISIBLE);
            tvDABSecondListName.setText(CtgUtils.changeTypeToString(getContext(),radioMessage.getDabMessage().getProgramType()));

            //埋点：点击电台类别名称
            PointTrigger.getInstance().trackEvent(Point.KeyName.DABClickType,Point.Field.ClassName,CtgUtils.changeTypeToString(getContext(),radioMessage.getDabMessage().getProgramType()));
        }else {
            currentListType = Constant.LIST_TYPE_CATEGORY;
            rvDABList.setVisibility(View.VISIBLE);
            rlDABSecondList.setVisibility(View.GONE);
        }
    }

    @Override
    public int getCurrentListType() {
        return currentListType;
    }

    @Override
    public List<RadioMessage> getCurrentShowList() {
        if (currentListType == Constant.LIST_TYPE_CATEGORY){
            return ListUtils.collectWithProgramType(RadioList.getInstance().getDABEffectRadioMessageList());
        }else if (currentListType == Constant.LIST_TYPE_CATEGORY_SUB){
            return ListUtils.filterWithProgramType(RadioList.getInstance().getDABEffectRadioMessageList(),currentId);
        }
        return RadioList.getInstance().getDABEffectRadioMessageList();
    }
    @Override
    public void updateList() {
        if (currentListType == Constant.LIST_TYPE_CATEGORY){
            dabListCtgAdapter.updateDabList(getCurrentShowList());
        }else if (currentListType == Constant.LIST_TYPE_CATEGORY_SUB){
            dabSecondListAdapter.updateDabList(getCurrentShowList());
        }
    }
}
