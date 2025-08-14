package com.desaysv.moduleradio.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.radio.RadioManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.utils.RadioCovertUtils;
import com.desaysv.svlibtoast.ToastUtil;

import java.util.List;
import java.util.Locale;

/**
 * Created by LZM on 2019-8-3.
 * Comment 收音列表的适配器
 */
public class RadioCollectListAdapter extends RecyclerView.Adapter<RadioCollectListAdapter.ViewHolder> {

    private static final String TAG = "RadioCollectListAdapter";


    private Context mContext;
    private List<RadioMessage> mRadioMessageList;
    private OnItemClickListener itemClickListener;
    private boolean isRightRudder;

    public RadioCollectListAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        itemClickListener = listener;
        isRightRudder = ProductUtils.isRightRudder();
    }

    public void updateList(List<RadioMessage> list){
        mRadioMessageList = list;
        notifyDataSetChanged();
    }


    @Override
    public RadioCollectListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(isRightRudder ? R.layout.radio_collectlist_item_right : R.layout.radio_collectlist_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if (position == -1){
                    return;
                }
                RadioMessage radioMessage = mRadioMessageList.get(position);
                if(itemClickListener != null) {
                    itemClickListener.onItemClick(position,radioMessage);
                }
            }
        });

        viewHolder.ivItemCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if (position == -1){
                    return;
                }
                RadioMessage radioMessage = mRadioMessageList.get(position);
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.radio_cancel_collected));
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RadioMessage radioMessage = mRadioMessageList.get(position);
        Log.d(TAG, "onBindViewHolder: radioMessage = " + radioMessage);
        String frequency = "";
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_am_item_title),radioMessage.getRadioFrequency());
                    break;
                case RadioManager.BAND_FM:
                    //Android 原生Bug,俄语环境下，“.”会被变成","
                    //FM+频点值在各个语言下是一样的显示，因此强制使用英语环境显示这个字串
                    frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_fm_item_title),radioMessage.getRadioFrequency() / 1000.0);
                    break;
            }
        }else {
            frequency = radioMessage.getDabMessage().getShortProgramStationName();
            holder.tvRadioName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
        }
        holder.tvFreq.setText(frequency);
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        //更新当前播放item的图标
        if (radioMessage.getRadioBand() == currentRadioMessage.getRadioBand()
                && radioMessage.getRadioFrequency() == currentRadioMessage.getRadioFrequency()) {
            Log.d(TAG, "onBindViewHolder: mCurrentRadioMessage = " + radioMessage);
            if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
                //DAB需要多加判断
                if (radioMessage.getDabMessage().getServiceId() == currentRadioMessage.getDabMessage().getServiceId()
                    && radioMessage.getDabMessage().getServiceComponentId() == currentRadioMessage.getDabMessage().getServiceComponentId()){
                    holder.ivItemIcon.setSelected(true);
                    holder.ivItemPlay.setVisibility(View.VISIBLE);
                    holder.ivItemPlayBg.setVisibility(View.VISIBLE);
                    holder.ivItemPlay.start();
                }else {
                    holder.ivItemIcon.setSelected(false);
                    holder.ivItemPlay.setVisibility(View.GONE);
                    holder.ivItemPlayBg.setVisibility(View.GONE);
                }

            }else {
                holder.ivItemIcon.setSelected(true);
                holder.ivItemPlay.setVisibility(View.VISIBLE);
                holder.ivItemPlayBg.setVisibility(View.VISIBLE);
                if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
                    holder.ivItemPlay.start();
                } else {
                    holder.ivItemPlay.stop();
                }
                String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : null;
                if (currentRadioMessage.getRdsRadioText() != null && radioName == null){
                    radioName = currentRadioMessage.getRdsRadioText().getProgramStationName();
                }
                holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);
            }
        } else {
            holder.ivItemIcon.setSelected(false);
            holder.ivItemPlay.setVisibility(View.GONE);
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : mContext.getResources().getString(R.string.radio_station_name);
            holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);
        }
    }


    @Override
    public int getItemCount() {
        return this.mRadioMessageList == null ? 0 : this.mRadioMessageList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivItemIcon;
        AudioWaveView ivItemPlay;
        TextView tvFreq;
        TextView tvRadioName;
        ImageView ivItemCollect;
        ImageView ivItemPlayBg;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
            ivItemPlay = itemView.findViewById(R.id.ivItemPlay);
            ivItemPlay.setNeedAutoStart(false);
            tvFreq = itemView.findViewById(R.id.tvFreq);
            tvRadioName = itemView.findViewById(R.id.tvRadioName);
            ivItemCollect = itemView.findViewById(R.id.ivItemCollect);
            ivItemPlayBg = itemView.findViewById(R.id.ivItemPlayBg);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, RadioMessage radioMessage);
    }

    private AnimationDrawable mRadioItemPlayAnimation;
    public void updatePlayItemAnim() {
        Log.d(TAG, "updatePlayAnim: ");
        if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
            if (mRadioItemPlayAnimation != null && !mRadioItemPlayAnimation.isRunning()){
                Log.d(TAG, "startAnim: start ");
                mRadioItemPlayAnimation.start();
            }
        } else {
            if (mRadioItemPlayAnimation != null && mRadioItemPlayAnimation.isRunning()){
                Log.d(TAG, "startAnim: stop ");
                mRadioItemPlayAnimation.stop();
            }
        }
    }

}
