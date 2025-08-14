package com.desaysv.moduledab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.common.Point;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.PointTrigger;
import com.desaysv.moduledab.utils.EPGUtils;
import com.desaysv.svlibtoast.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class EPGDetailListAdapter extends RecyclerView.Adapter<EPGDetailListAdapter.EPGDetailListHolder>{

    private static final String TAG = "EPGDetailListAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<DABEPGSchedule> epgDateList = new ArrayList<>();
    private DABTime dabTime;

    public EPGDetailListAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
    }


    public void updateEPGDetailList(List<DABEPGSchedule> listDTOS, DABTime dabTime){
        this.epgDateList.clear();
        this.epgDateList.addAll(listDTOS);
        this.dabTime = dabTime;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EPGDetailListAdapter.EPGDetailListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.epg_detail_item, parent, false);
        return new EPGDetailListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EPGDetailListAdapter.EPGDetailListHolder holder, int position) {
        DABEPGSchedule dabepgSchedule = epgDateList.get(position);
        if (dabepgSchedule == null){
            return;
        }
        String hour = dabepgSchedule.getHour();
        String min = dabepgSchedule.getMin();
        if (hour.length() < 2){
            hour = "0"+hour;
        }
        if (min.length() < 2){
            min = "0"+min;
        }
        holder.tvEPGTime.setText(hour + ":" + min);
        holder.tvEPGDetailName.setText(dabepgSchedule.getProgramName());

        if (EPGUtils.isOverDue(dabepgSchedule,dabTime)){
            holder.rlEPGDetailItem.setEnabled(false);
            holder.tvEPGTime.setEnabled(false);
            holder.tvEPGDetailName.setEnabled(false);
            holder.ivEPGMark.setVisibility(View.GONE);
        }else {
            holder.rlEPGDetailItem.setEnabled(true);
            holder.tvEPGTime.setEnabled(true);
            holder.tvEPGDetailName.setEnabled(true);
            holder.ivEPGMark.setVisibility(View.VISIBLE);
            if (dabepgSchedule.isSubscribe()){
                holder.ivEPGMark.setSelected(true);
            }else {
                holder.ivEPGMark.setSelected(false);
            }
        }


        holder.rlEPGDetailItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onClickEPG(dabepgSchedule,false);
                if (dabepgSchedule.isSubscribe()){
                    ToastUtil.showToast(mContext,R.string.dab_epg_tips);
                    PointTrigger.getInstance().trackEvent(Point.KeyName.EPGClick,Point.Field.OPENTYPE,Point.FieldValue.COLLECT,Point.Field.RadioName,dabepgSchedule.getProgramName());
                }else {
                    ToastUtil.showToast(mContext,R.string.dab_cancle_epg_tips);
                    PointTrigger.getInstance().trackEvent(Point.KeyName.EPGClick,Point.Field.OPENTYPE,Point.FieldValue.UNCOLLECT,Point.Field.RadioName,dabepgSchedule.getProgramName());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return epgDateList == null ? 0 : epgDateList.size();
    }


    protected static class EPGDetailListHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlEPGDetailItem;
        private final TextView tvEPGTime;
        private final AudioWaveView awPlaying;
        private final TextView tvEPGDetailName;
        private final ImageView ivEPGMark;

        public EPGDetailListHolder(@NonNull View itemView) {
            super(itemView);
            rlEPGDetailItem = itemView.findViewById(R.id.rlEPGDetailItem);
            awPlaying = itemView.findViewById(R.id.awPlaying);
            tvEPGTime = itemView.findViewById(R.id.tvEPGTime);
            tvEPGDetailName = itemView.findViewById(R.id.tvEPGDetailName);
            ivEPGMark = itemView.findViewById(R.id.ivEPGMark);
        }

    }
}
