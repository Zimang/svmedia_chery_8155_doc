package com.desaysv.moduledab.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;

import java.util.ArrayList;
import java.util.List;

public class DABPlayListAdapter extends RecyclerView.Adapter<DABPlayListAdapter.DABPlayListHolder>{

    private static final String TAG = "DABPlayListAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<RadioMessage> dabList = new ArrayList<>();

    public DABPlayListAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
    }


    public void updateDabList(List<RadioMessage> dabList){
        this.dabList.clear();
        this.dabList.addAll(dabList);
    }

    @NonNull
    @Override
    public DABPlayListAdapter.DABPlayListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dab_playlist_item, parent, false);
        return new DABPlayListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DABPlayListAdapter.DABPlayListHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder,position:"+position);
        RadioMessage radioMessage = dabList.get(position);
        if (radioMessage.getDabMessage() == null){
            return;
        }
        holder.tvDABPlayListName.setText(radioMessage.getDabMessage().getShortProgramStationName());
        holder.tvDABPlayListEbleName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
        if (position < 9){
            holder.tvDABPlayListNum.setText("0"+ (position + 1));
        }else {
            holder.tvDABPlayListNum.setText(String.valueOf(position+1));
        }
        //如果是正在播放，则显示播放状态
        if (CompareUtils.isSameDAB(radioMessage,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage())){
             holder.tvDABPlayListAni.setVisibility(View.VISIBLE);
            if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
                holder.tvDABPlayListAni.start();
            }else {
                holder.tvDABPlayListAni.stop();
            }
             holder.rlDABPlayItem.setSelected(true);
             holder.tvDABPlayListName.setSelected(true);
             holder.tvDABPlayListNum.setVisibility(View.GONE);
        }else {
            holder.tvDABPlayListAni.setVisibility(View.GONE);
            holder.rlDABPlayItem.setSelected(false);
            holder.tvDABPlayListName.setSelected(false);
            holder.tvDABPlayListNum.setVisibility(View.VISIBLE);
        }

        if (radioMessage.isCollect()){
            holder.ivDABPlayListLike.setVisibility(View.VISIBLE);
        }else {
            holder.ivDABPlayListLike.setVisibility(View.GONE);
        }

        holder.rlDABPlayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onClickDAB(radioMessage);
            }
        });

        holder.ivDABPlayListLike.setOnClickListener(new View.OnClickListener() {
            @Override
            //按照UI来看，这里应该是只能取消收藏
            public void onClick(View v) {
                operationListener.onCollectDAB(radioMessage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dabList == null ? 0 : dabList.size();
    }


    protected static class DABPlayListHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlDABPlayItem;
        private final TextView tvDABPlayListName;
        private final ImageView ivDABPlayListLike;
        private final AudioWaveView tvDABPlayListAni;
        private final TextView tvDABPlayListNum;
        private final TextView tvDABPlayListEbleName;

        public DABPlayListHolder(@NonNull View itemView) {
            super(itemView);
            rlDABPlayItem = itemView.findViewById(R.id.rlDABPlayItem);
            tvDABPlayListName = itemView.findViewById(R.id.tvDABPlayListName);
            ivDABPlayListLike = itemView.findViewById(R.id.ivDABPlayListLike);
            tvDABPlayListAni = itemView.findViewById(R.id.tvDABPlayListAni);
            tvDABPlayListNum = itemView.findViewById(R.id.tvDABPlayListNum);
            tvDABPlayListEbleName = itemView.findViewById(R.id.tvDABPlayListEbleName);
            tvDABPlayListAni.setNeedAutoStart(false);
        }

    }
}
