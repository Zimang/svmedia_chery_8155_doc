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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;

import java.util.ArrayList;
import java.util.List;

public class EPGDateListAdapter extends RecyclerView.Adapter<EPGDateListAdapter.EPGDateListHolder>{

    private static final String TAG = "EPGDateListAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<DABEPGSchedule> epgDateList = new ArrayList<>();
    private int selectedPosition = 0;//默认选中位置

    public EPGDateListAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
    }


    public void updateEPGDateList(List<DABEPGSchedule> listDTOS){
        this.epgDateList.clear();
        this.epgDateList.addAll(listDTOS);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EPGDateListAdapter.EPGDateListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.epg_date_item, parent, false);
        return new EPGDateListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EPGDateListAdapter.EPGDateListHolder holder, int position) {
        DABEPGSchedule dabepgSchedule = epgDateList.get(position);
        if (dabepgSchedule == null){
            return;
        }
        String date = String.format(mContext.getResources().getString(R.string.dab_epg_format),dabepgSchedule.getMonth(),dabepgSchedule.getDay());
        holder.tvEPGDate.setText(date);

        if (position == selectedPosition){
            holder.rlEPGDateItem.setSelected(true);
        }else {
            holder.rlEPGDateItem.setSelected(false);
        }

        holder.rlEPGDateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = holder.getAdapterPosition();
                operationListener.onClickEPG(dabepgSchedule,true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return epgDateList == null ? 0 : epgDateList.size();
    }


    protected static class EPGDateListHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlEPGDateItem;
        private final TextView tvEPGDate;

        public EPGDateListHolder(@NonNull View itemView) {
            super(itemView);
            rlEPGDateItem = itemView.findViewById(R.id.rlEPGDateItem);
            tvEPGDate = itemView.findViewById(R.id.tvEPGDate);
        }

    }
}
