package com.desaysv.moduledab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DABListAllAdapter extends RecyclerView.Adapter<DABListAllAdapter.DABListAllHolder>{

    private static final String TAG = "DABListAllAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<RadioMessage> dabList = new ArrayList<>();
    private byte[] currentLogo;

    private boolean isRightRudder;
    public DABListAllAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
        isRightRudder = ProductUtils.isRightRudder();
    }


    public void updateDabList(List<RadioMessage> dabList){
        this.dabList.clear();
        this.dabList.addAll(dabList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DABListAllAdapter.DABListAllHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(isRightRudder ? R.layout.dab_listall_item_right : R.layout.dab_listall_item, parent, false);
        currentLogo = null;//重新创建Holder时，需要清空原来的数据
        return new DABListAllHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DABListAllAdapter.DABListAllHolder holder, int position) {
        RadioMessage radioMessage = dabList.get(position);
        if (radioMessage.getDabMessage() == null){
            return;
        }
        holder.tvProgramStationName.setText(radioMessage.getDabMessage().getShortProgramStationName());

        RequestOptions option = RequestOptions
                .bitmapTransform(new RoundedCorners(8))
                .error(R.mipmap.icon_logo);

        //如果是正在播放，则显示播放状态
        if (CompareUtils.isSameDAB(radioMessage,DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage())){
            //优先使用Sls
            byte[] logoDataList = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getSlsDataList();
            //次级使用存储的Logo
            if (logoDataList == null || logoDataList.length < 1){
                logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
            }
            //最后使用当前获取到的Logo
            if (logoDataList == null){
                logoDataList = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage().getDabMessage().getLogoDataList();
            }
            if (logoDataList != null && logoDataList.length > 0){
                holder.ivLogo.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
            }else {
                holder.ivLogo.setImageResource(R.mipmap.icon_logo);
            }
            holder.avDABListAni.setVisibility(View.VISIBLE);
            holder.ivItemPlayBg.setVisibility(View.VISIBLE);
            if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
                holder.avDABListAni.start();
            }else {
                holder.avDABListAni.stop();
            }
        }else {
            byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
            if (logoDataList != null && logoDataList.length > 0){
                holder.ivLogo.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
            }else {
                holder.ivLogo.setImageResource(R.mipmap.icon_logo);
            }
            holder.avDABListAni.setVisibility(View.GONE);
            holder.ivItemPlayBg.setVisibility(View.GONE);
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
        private final ImageView ivLogo;
        private final TextView tvProgramStationName;
        private final ImageView ivCollect;

        private final AudioWaveView avDABListAni;

        private final ImageView ivItemPlayBg;

        public DABListAllHolder(@NonNull View itemView) {
            super(itemView);
            rlDABItem = itemView.findViewById(R.id.rlDABItem);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvProgramStationName = itemView.findViewById(R.id.tvProgramStationName);
            ivCollect = itemView.findViewById(R.id.ivCollect);
            avDABListAni = itemView.findViewById(R.id.avDABListAni);
            ivItemPlayBg = itemView.findViewById(R.id.ivItemPlayBg);
        }

    }
}
