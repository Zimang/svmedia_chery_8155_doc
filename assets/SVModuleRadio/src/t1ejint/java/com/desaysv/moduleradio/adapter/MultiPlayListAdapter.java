package com.desaysv.moduleradio.adapter;

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
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.mediacommonlib.ui.MarqueeTextView;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.utils.RadioCovertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MultiPlayListAdapter extends RecyclerView.Adapter<MultiPlayListAdapter.DABPlayListHolder>{

    private static final String TAG = "MultiPlayListAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<RadioMessage> dabList = new ArrayList<>();

    //用来区分是否可以点击操作收藏按钮
    //T22保持不可点击
    private boolean canClickCollect = true;

    public MultiPlayListAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
    }


    public void setCanClickCollect(boolean canClickCollect) {
        this.canClickCollect = canClickCollect;
    }

    public void updateDabList(List<RadioMessage> dabList){
        this.dabList.clear();
        this.dabList.addAll(dabList);
    }

    @NonNull
    @Override
    public MultiPlayListAdapter.DABPlayListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.multi_playlist_item, parent, false);
        return new DABPlayListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiPlayListAdapter.DABPlayListHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder,position:"+position);
        RadioMessage radioMessage = dabList.get(position);
        holder.tvDABPlayListName.setNeedMarquee(false);
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE){
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
            holder.tvDABPlayListName.setText(radioName != null && radioName.trim().length() > 1 ? radioName : String.format(Locale.ENGLISH,mContext.getResources().getString(com.desaysv.moduleradio.R.string.radio_fm_item_title), radioMessage.getRadioFrequency() / 1000.0));
            holder.tvMultiType.setText("FM");
            //如果是正在播放，则显示播放状态
            if (radioMessage.getRadioFrequency() == DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getRadioFrequency()) {
//                RDSRadioText rdsRadioText = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getRdsRadioText();
//                if (rdsRadioText != null){
//                    String currentRDSName = rdsRadioText.getProgramStationName();
//                    if (currentRDSName != null && currentRDSName.trim().length() > 1){
//                        holder.tvDABPlayListName.setText(currentRDSName);
//                    }
//                }
                holder.tvDABPlayListAni.setVisibility(View.VISIBLE);
                holder.tvDABPlayListName.setNeedMarquee(true);
                if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
                    holder.tvDABPlayListAni.start();
                } else {
                    holder.tvDABPlayListAni.stop();
                }
                holder.rlDABPlayItem.setSelected(true);
                holder.tvDABPlayListName.setSelected(true);
            } else {
                holder.tvDABPlayListAni.setVisibility(View.GONE);
                holder.rlDABPlayItem.setSelected(false);
                holder.tvDABPlayListName.setSelected(false);
            }
        }else {
            holder.tvDABPlayListName.setText(radioMessage.getDabMessage().getShortProgramStationName());
            holder.tvMultiType.setText("DAB");
            //如果是正在播放，则显示播放状态
            if (CompareUtils.isSameDAB(radioMessage, DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage())) {
                holder.tvDABPlayListAni.setVisibility(View.VISIBLE);
                holder.tvDABPlayListName.setNeedMarquee(true);
                if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
                    holder.tvDABPlayListAni.start();
                } else {
                    holder.tvDABPlayListAni.stop();
                }
                holder.rlDABPlayItem.setSelected(true);
                holder.tvDABPlayListName.setSelected(true);
            } else {
                holder.tvDABPlayListAni.setVisibility(View.GONE);
                holder.rlDABPlayItem.setSelected(false);
                holder.tvDABPlayListName.setSelected(false);
            }
        }

        if (radioMessage.isCollect()){
            holder.ivDABPlayListLike.setSelected(true);
        }else {
            holder.ivDABPlayListLike.setSelected(false);
        }

        holder.rlDABPlayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onClickDAB(radioMessage);
            }
        });

        if (canClickCollect) {
            holder.ivDABPlayListLike.setOnClickListener(new View.OnClickListener() {
                @Override
                //按照UI来看，这里应该是只能取消收藏
                public void onClick(View v) {
                    operationListener.onCollectDAB(radioMessage);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dabList == null ? 0 : dabList.size();
    }


    protected static class DABPlayListHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlDABPlayItem;
        private final MarqueeTextView tvDABPlayListName;
        private final ImageView ivDABPlayListLike;
        private final AudioWaveView tvDABPlayListAni;
        private final TextView tvMultiType;

        public DABPlayListHolder(@NonNull View itemView) {
            super(itemView);
            rlDABPlayItem = itemView.findViewById(R.id.rlDABPlayItem);
            tvDABPlayListName = itemView.findViewById(R.id.tvDABPlayListName);
            ivDABPlayListLike = itemView.findViewById(R.id.ivDABPlayListLike);
            tvDABPlayListAni = itemView.findViewById(R.id.tvDABPlayListAni);
            tvMultiType = itemView.findViewById(R.id.tvMultiType);
            tvDABPlayListAni.setNeedAutoStart(false);
        }

    }
}
