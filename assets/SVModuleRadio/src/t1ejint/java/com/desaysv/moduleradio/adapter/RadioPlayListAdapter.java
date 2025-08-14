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
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.moduleradio.utils.ClickUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Created by LZM on 2019-8-3.
 * Comment 收音列表的适配器
 */
public class RadioPlayListAdapter extends RecyclerView.Adapter<RadioPlayListAdapter.ViewHolder> {

    private static final String TAG = "RadioPlayListAdapter";


    private Context mContext;
    private List<RadioMessage> mRadioMessageList = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public RadioPlayListAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        itemClickListener = listener;
    }

    public void updateList(List<RadioMessage> list) {
        if (isFromCollectClick && !isCollectList){
            isFromCollectClick = false;
            return;
        }
        mRadioMessageList.clear();
        mRadioMessageList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.radio_playlist_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if (position == -1){
                    return;
                }
                RadioMessage radioMessage = mRadioMessageList.get(position);
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });

        viewHolder.ivItemCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ClickUtils.isAllowClick()){
                    return;
                }
                int position = viewHolder.getAdapterPosition();
                if (position == -1){
                    return;
                }
                isFromCollectClick = true;
                RadioMessage radioMessage = mRadioMessageList.get(position);
                if (v.isSelected()) {
                    v.setSelected(false);
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
                }else {
                    if (radioMessage.getRadioBand() == RadioManager.BAND_AM){
                        if (RadioList.getInstance().getAMCollectRadioMessageList().size() > 29){
                            ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.dab_collect_fully));
                            return;
                        }
                    }else if (radioMessage.getRadioBand() == RadioManager.BAND_FM){
                        if (RadioList.getInstance().getFMCollectRadioMessageList().size() > 29){
                            return;
                        }
                    }
                    ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.COLLECT, ChangeReasonData.CLICK, radioMessage);
                    v.setSelected(true);
                }
            }
        });
        return viewHolder;
    }

    private boolean isFromCollectClick = false;

    private boolean isCollectList = false;

    public void setCollectList(boolean collectList) {
        isCollectList = collectList;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RadioMessage radioMessage = mRadioMessageList.get(position);
        Log.d(TAG, "onBindViewHolder: radioMessage = " + radioMessage);
        String frequency = "";
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    frequency = frequency + String.format(mContext.getResources().getString(R.string.radio_am_item_title), radioMessage.getRadioFrequency());
                    break;
                case RadioManager.BAND_FM:
                    //Android 原生Bug,俄语环境下，“.”会被变成","
                    //FM+频点值在各个语言下是一样的显示，因此强制使用英语环境显示这个字串
                    frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_fm_item_title), radioMessage.getRadioFrequency() / 1000.0);
                    break;
            }
        } else {
            frequency = radioMessage.getDabMessage().getShortProgramStationName();
            holder.tvRadioName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
        }
        holder.tvFreq.setText(frequency);
        holder.ivItemCollect.setSelected(radioMessage.isCollect());
        if (position < 9){
            holder.tvItemNum.setText("0"+ (position + 1));
        }else {
            holder.tvItemNum.setText(String.valueOf(position+1));
        }
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        //更新当前播放item的图标
        if (radioMessage.getRadioBand() == currentRadioMessage.getRadioBand()
                && radioMessage.getRadioFrequency() == currentRadioMessage.getRadioFrequency()) {
            Log.d(TAG, "onBindViewHolder: mCurrentRadioMessage = " + radioMessage);
            ivItemPlay = holder.ivItemPlay;
            if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE) {
                //DAB需要多加判断
                if (radioMessage.getDabMessage().getServiceId() == currentRadioMessage.getDabMessage().getServiceId()
                        && radioMessage.getDabMessage().getServiceComponentId() == currentRadioMessage.getDabMessage().getServiceComponentId()) {
                    holder.ivItemPlay.setVisibility(View.VISIBLE);
                    holder.tvItemNum.setVisibility(View.GONE);
                    holder.tvFreq.setSelected(true);
                    updatePlayItemAnim();
                } else {
                    holder.ivItemPlay.setVisibility(View.GONE);
                    holder.tvItemNum.setVisibility(View.VISIBLE);
                    holder.tvFreq.setSelected(false);
                }

            } else {
                holder.ivItemPlay.setVisibility(View.VISIBLE);
                holder.tvItemNum.setVisibility(View.GONE);
                holder.tvFreq.setSelected(true);
                if (mRadioItemPlayAnimation != holder.ivItemPlay.getBackground()) {
                    mRadioItemPlayAnimation = (AnimationDrawable) holder.ivItemPlay.getBackground();
                }
                updatePlayItemAnim();
                updatePlayItemAnim();
                String radioName = mContext.getResources().getString(R.string.radio_station_name);
                if (currentRadioMessage.getRdsRadioText() != null){
                    radioName = currentRadioMessage.getRdsRadioText().getProgramStationName();
                }
                holder.tvRadioName.setText(radioName == null ? mContext.getResources().getString(R.string.radio_station_name): radioName);
            }
        } else {
            holder.ivItemPlay.setVisibility(View.GONE);
            holder.tvItemNum.setVisibility(View.VISIBLE);
            holder.tvFreq.setSelected(false);
            holder.tvRadioName.setText(mContext.getResources().getString(R.string.radio_station_name));
        }
    }


    @Override
    public int getItemCount() {
        return this.mRadioMessageList == null ? 0 : this.mRadioMessageList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        AudioWaveView ivItemPlay;
        TextView tvFreq;
        TextView tvRadioName;
        ImageView ivItemCollect;
        TextView tvItemNum;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivItemPlay = itemView.findViewById(R.id.ivItemPlay);
            tvFreq = itemView.findViewById(R.id.tvFreq);
            tvRadioName = itemView.findViewById(R.id.tvRadioName);
            ivItemCollect = itemView.findViewById(R.id.ivItemCollect);
            tvItemNum = itemView.findViewById(R.id.tvItemNum);
            ivItemPlay.setNeedAutoStart(false);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private AudioWaveView ivItemPlay;

    private AnimationDrawable mRadioItemPlayAnimation;

    public void updatePlayItemAnim() {
        Log.d(TAG, "updatePlayAnim: ");
        if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
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
