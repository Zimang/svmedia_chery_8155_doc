package com.desaysv.moduledab.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
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
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.R;
import com.desaysv.moduledab.iinterface.IDABOperationListener;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.CompareUtils;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DABMainListAdapter extends RecyclerView.Adapter<DABMainListAdapter.DABMainListHolder>{

    private static final String TAG = "DABMainListAdapter";

    private Context mContext;
    private IDABOperationListener operationListener;
    private List<RadioMessage> dabList = new ArrayList<>();

    private byte[] currentLogo;
    private boolean isRightRudder;

    public DABMainListAdapter(Context context, IDABOperationListener listener) {
        mContext = context;
        operationListener = listener;
        isRightRudder = ProductUtils.isRightRudder();
    }


    public void updateDabList(List<RadioMessage> dabList){
        this.dabList.clear();
        this.dabList.addAll(dabList);
    }

    @NonNull
    @Override
    public DABMainListAdapter.DABMainListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(isRightRudder? R.layout.dab_mainlist_item_right : R.layout.dab_mainlist_item, parent, false);
        DABMainListHolder viewHolder = new DABMainListHolder(view);
        currentLogo = null;//重新创建Holder时，需要清空原来的数据
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DABMainListAdapter.DABMainListHolder holder, int position) {
        RadioMessage radioMessage = dabList.get(position);
        if (radioMessage.getDabMessage() == null){
            return;
        }
        holder.tvProgramStationName.setText(radioMessage.getDabMessage().getShortProgramStationName());
//        holder.tvEnsembleLabel.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
        RequestOptions option = RequestOptions
                .bitmapTransform(new RoundedCorners(8))
                .error(R.mipmap.icon_logo);

        //如果是正在播放，则显示播放状态
        RadioMessage currentRadio = DABTrigger.getInstance().mRadioStatusTool.getCurrentRadioMessage();
        if (CompareUtils.isSameDAB(radioMessage,currentRadio)){
            //优先使用Sls
            DABMessage dabMessage = currentRadio.getDabMessage();
            byte[] logoDataList = dabMessage != null ? dabMessage.getSlsDataList() : null;
            //次级使用存储的Logo
            if (logoDataList == null || logoDataList.length < 1){
                logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
            }
            //最后使用当前获取到的Logo
            if (logoDataList == null){
                logoDataList = radioMessage.getDabMessage().getLogoDataList();
            }
            if (logoDataList != null && logoDataList.length > 0){
                holder.ivLogo.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
            }else {
                holder.ivLogo.setImageResource(R.mipmap.icon_logo);
            }
            playAni = holder.ivItemPlay;
            holder.ivItemPlay.setVisibility(View.VISIBLE);
            holder.ivItemPlayBg.setVisibility(View.VISIBLE);
            if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
                holder.ivItemPlay.start();
            }else {
                holder.ivItemPlay.stop();
            }
        }else {
            //非播放状态的都要设置为默认图片
            byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
            if (logoDataList != null && logoDataList.length > 0){
                holder.ivLogo.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
            }else {
                holder.ivLogo.setImageResource(R.mipmap.icon_logo);
            }
            holder.ivItemPlay.setVisibility(View.GONE);
            holder.ivItemPlayBg.setVisibility(View.GONE);
        }
        holder.rlDABItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onClickDAB(radioMessage);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dabList == null ? 0 : dabList.size();
    }


    protected static class DABMainListHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rlDABItem;
        private final ImageView ivLogo;
        private final TextView tvProgramStationName;
//        private final TextView tvEnsembleLabel;
        private final AudioWaveView ivItemPlay;
        private final ImageView ivItemPlayBg;

        public DABMainListHolder(@NonNull View itemView) {
            super(itemView);
            rlDABItem = itemView.findViewById(R.id.rlDABItem);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvProgramStationName = itemView.findViewById(R.id.tvProgramStationName);
//            tvEnsembleLabel = itemView.findViewById(R.id.tvEnsembleLabel);
            ivItemPlay = itemView.findViewById(R.id.ivItemPlay);
            ivItemPlayBg = itemView.findViewById(R.id.ivItemPlayBg);
            ivItemPlay.setNeedAutoStart(false);
        }

    }

    private AudioWaveView playAni;
    public void updatePlayItemAnim() {
        Log.d(TAG, "updatePlayAnim: ");
        if (playAni == null){
            return;
        }
        if (DABTrigger.getInstance().mRadioStatusTool.isPlaying()) {
            playAni.start();
        } else {
            Log.d(TAG, "startAnim: stop ");
            playAni.stop();
        }
    }
}
