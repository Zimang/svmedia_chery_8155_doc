package com.desaysv.moduledab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.common.Constant;
import com.desaysv.moduledab.fragment.DABListBaseFragment;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.CtgUtils;

import java.util.ArrayList;
import java.util.List;

public class DABListCtgAdapter extends RecyclerView.Adapter<DABListCtgAdapter.DABListAllHolder>{

    private static final String TAG = "DABListCtgAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<RadioMessage> dabList = new ArrayList<>();
    private int type = Constant.LIST_TYPE_ENSEMBLE;

    public DABListCtgAdapter(Context context, IDABOperationListener listener, int type) {
        mContext = context;
        operationListener = listener;
        this.type = type;
    }


    public void updateDabList(List<RadioMessage> dabList){
        this.dabList.clear();
        this.dabList.addAll(dabList);
    }

    @NonNull
    @Override
    public DABListCtgAdapter.DABListAllHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (type == Constant.LIST_TYPE_ENSEMBLE){
            view = LayoutInflater.from(mContext).inflate(R.layout.dab_listesem_item, parent, false);
        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.dab_listctg_item, parent, false);
        }
        return new DABListAllHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DABListCtgAdapter.DABListAllHolder holder, int position) {
        RadioMessage radioMessage = dabList.get(position);
        if (radioMessage.getDabMessage() == null){
            return;
        }
        if (type == Constant.LIST_TYPE_ENSEMBLE){
            holder.tvListCtgName.setText(radioMessage.getDabMessage().getEnsembleLabel());
            //如果是正在播放，则显示播放状态
            //这里应该判断集合
            if (CompareUtils.isSameDABEnsemble(radioMessage,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage())){
                //todo
                //playing UI
            }else {
                //normal UI
            }
        }else {
            holder.tvListCtgName.setText(CtgUtils.changeTypeToString(mContext,radioMessage.getDabMessage().getProgramType()));
            //如果是正在播放，则显示播放状态
            //这里应该判断类型
            if (CompareUtils.isSameDABType(radioMessage,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage())){
                //todo
                //playing UI
            }else {
                //normal UI
            }
        }

        holder.rlDABSecondListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onClickDAB(radioMessage,true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dabList == null ? 0 : dabList.size();
    }


    protected static class DABListAllHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlDABSecondListItem;
        private final TextView tvListCtgName;

        public DABListAllHolder(@NonNull View itemView) {
            super(itemView);
            rlDABSecondListItem = itemView.findViewById(R.id.rlDABSecondListItem);
            tvListCtgName = itemView.findViewById(R.id.tvListCtgName);
        }

    }
}
