package com.desaysv.moduledab.fragment;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABEPGScheduleList;
import com.desaysv.mediacommonlib.ui.BaseFragment;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.adapter.EPGDateListAdapter;
import com.desaysv.moduledab.adapter.EPGDetailListAdapter;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.EPGUtils;
import com.desaysv.moduledab.utils.ProductUtils;

import java.util.List;

/**
 * created by ZNB on 2022-11-21
 * DAB播放页进去的EPG界面
 */
public class DABEPGFragment extends BaseFragment implements View.OnClickListener, IDABOperationListener {

    private static final String TAG = "DABEPGFragment";

    private ImageView ivDABEPGBack;
    private RecyclerView rvEPGDate;
    private RecyclerView rvEPGDetail;
    private EPGDateListAdapter dateListAdapter;
    private EPGDetailListAdapter detailListAdapter;
    private DABEPGScheduleList dabepgScheduleList;

    private TextView tvEmptyView;

    @Override
    public int getLayoutResID() {
        return ProductUtils.isRightRudder() ? R.layout.fragment_dab_epg_layout_right : R.layout.fragment_dab_epg_layout;
    }

    @Override
    public void initView(View view) {
        ivDABEPGBack = view.findViewById(R.id.ivDABEPGBack);
        rvEPGDate = view.findViewById(R.id.rvEPGDate);
        rvEPGDetail = view.findViewById(R.id.rvEPGDetail);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        rvEPGDate.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEPGDetail.setLayoutManager(new LinearLayoutManager(getContext()));

        dateListAdapter = new EPGDateListAdapter(getContext(),this);
        detailListAdapter = new EPGDetailListAdapter(getContext(),this);

        rvEPGDate.setAdapter(dateListAdapter);
        rvEPGDetail.setAdapter(detailListAdapter);
    }

    @Override
    public void initData() {
        dabepgScheduleList = DABTrigger.getInstance().mRadioStatusTool.getEPGList();
        if (dabepgScheduleList != null) {
            List<DABEPGSchedule> epgDateList = EPGUtils.collectWithDate(dabepgScheduleList.getEpgScheduleList());
            if (epgDateList.size() > 0) {
                tvEmptyView.setVisibility(View.GONE);
                rvEPGDate.setVisibility(View.VISIBLE);
                rvEPGDetail.setVisibility(View.VISIBLE);
                dateListAdapter.updateEPGDateList(epgDateList);
                detailListAdapter.updateEPGDetailList(EPGUtils.filterWithDate(dabepgScheduleList.getEpgScheduleList(), epgDateList.get(0)), DABTrigger.getInstance().mRadioStatusTool.getDABTime());
            }else {
                tvEmptyView.setVisibility(View.VISIBLE);
                rvEPGDate.setVisibility(View.GONE);
                rvEPGDetail.setVisibility(View.GONE);
            }
        }else {
            tvEmptyView.setVisibility(View.VISIBLE);
            rvEPGDate.setVisibility(View.GONE);
            rvEPGDetail.setVisibility(View.GONE);
        }
    }

    @Override
    public void initViewListener() {
        ivDABEPGBack.setOnClickListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.ivDABEPGBack == id){
            backClickListener.onEPGBackClick();
        }
    }


    @Override
    public void onClickEPG(DABEPGSchedule dabepgSchedule, boolean isDate) {
        if (isDate){
            //显示对应列表
            dateListAdapter.notifyDataSetChanged();
            detailListAdapter.updateEPGDetailList(EPGUtils.filterWithDate(dabepgScheduleList.getEpgScheduleList(),dabepgSchedule),DABTrigger.getInstance().mRadioStatusTool.getDABTime());
        }else {
            RadioList.getInstance().updateEPGSubscribeList(dabepgSchedule);
            detailListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 左上角返回按钮的点击事件
     */
    private IOnBackClickListener backClickListener;

    public void setBackClickListener(IOnBackClickListener backClickListener){
        this.backClickListener = backClickListener;
    }

    public interface IOnBackClickListener{

        void onEPGBackClick();
    }
}
