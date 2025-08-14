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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;

import java.util.ArrayList;
import java.util.List;

public class DABListAllAdapter extends RecyclerView.Adapter<DABListAllAdapter.DABListAllHolder>{

    private static final String TAG = "DABListAllAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<RadioMessage> dabList = new ArrayList<>();

    public DABListAllAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
    }


    public void updateDabList(List<RadioMessage> dabList){
        this.dabList.clear();
        this.dabList.addAll(dabList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DABListAllHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dab_listall_item, parent, false);
        return new DABListAllHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DABListAllHolder holder, int position) {
        RadioMessage radioMessage = dabList.get(position);
        if (radioMessage.getDabMessage() == null){
            return;
        }
        holder.tvProgramStationName.setText(radioMessage.getDabMessage().getShortProgramStationName());

        //如果是正在播放，则显示播放状态
        if (CompareUtils.isSameDAB(radioMessage,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage())){
            holder.tvCount.setVisibility(View.GONE);
            holder.tvDABAni.setVisibility(View.VISIBLE);
            ivItemPlay = holder.tvDABAni;
            updatePlayItemAnim();
        }else {
            //normal UI
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText(String.valueOf(position));
            holder.tvDABAni.setVisibility(View.GONE);
        }

        holder.ivCollect.setSelected(radioMessage.isCollect());

        holder.rlDABItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onClickDAB(radioMessage);
            }
        });

        holder.ivCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onCollectDAB(radioMessage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dabList == null ? 0 : dabList.size();
    }


    protected static class DABListAllHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlDABItem;
        private final AudioWaveView tvDABAni;
        private final TextView tvCount;
        private final TextView tvProgramStationName;
        private final ImageView ivCollect;

        public DABListAllHolder(@NonNull View itemView) {
            super(itemView);
            rlDABItem = itemView.findViewById(R.id.rlDABItem);
            tvDABAni = itemView.findViewById(R.id.tvDABAni);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvProgramStationName = itemView.findViewById(R.id.tvProgramStationName);
            ivCollect = itemView.findViewById(R.id.ivCollect);
            tvDABAni.setNeedAutoStart(false);
        }

    }



    private AudioWaveView ivItemPlay;
    public void updatePlayItemAnim() {
        Log.d(TAG, "updatePlayAnim: ");
        if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
            if (ivItemPlay != null) {
                Log.d(TAG, "startAnim: start ");
                ivItemPlay.start();
            }
        } else {
            if (ivItemPlay != null) {
                Log.d(TAG, "startAnim: stop ");
                ivItemPlay.stop();
            }
        }
    }
}
