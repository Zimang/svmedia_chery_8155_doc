package com.desaysv.moduleradio.adapter;

import android.annotation.SuppressLint;
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
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.utils.RadioCovertUtils;

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
        setHasStableIds(true);
    }

    public void updateList(List<RadioMessage> list) {
        mRadioMessageList.clear();
        mRadioMessageList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public RadioPlayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.radio_playlist_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);
//播放列表只显示，不响应收藏按键点击事件
//        viewHolder.ivItemCollect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = viewHolder.getAdapterPosition();
//                RadioMessage radioMessage = mRadioMessageList.get(position);
//                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.CANCEL_COLLECT, ChangeReasonData.CLICK, radioMessage);
//            }
//        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        RadioMessage radioMessage = mRadioMessageList.get(position);
        Log.d(TAG, "onBindViewHolder: radioMessage = " + radioMessage);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioMessage radioMessage = mRadioMessageList.get(position);
                ModuleRadioTrigger.getInstance().mRadioControl.processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK_ITEM, radioMessage);
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });


        String frequency = "";
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                    frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_am_item_title), radioMessage.getRadioFrequency());
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
        holder.ivItemCollect.setVisibility(radioMessage.isCollect() ? View.VISIBLE : View.GONE);
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
                    updatePlayItemAnim();
                } else {
                    holder.ivItemPlay.setVisibility(View.GONE);
                }

            } else {
                holder.ivItemPlay.setVisibility(View.VISIBLE);
                holder.view.setBackgroundResource(R.drawable.radio_playlist_bg);
                updatePlayItemAnim();
                String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
                if (currentRadioMessage.getRdsRadioText() != null){
                    radioName = currentRadioMessage.getRdsRadioText().getProgramStationName();
                }
                holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);
            }
        } else {
            holder.view.setBackground(null);
            holder.ivItemPlay.setVisibility(View.GONE);
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage);
            holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ?mContext.getResources().getString(R.string.radio_station_name):radioName);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
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

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivItemPlay = itemView.findViewById(R.id.ivItemPlay);
            tvFreq = itemView.findViewById(R.id.tvFreq);
            tvRadioName = itemView.findViewById(R.id.tvRadioName);
            ivItemCollect = itemView.findViewById(R.id.ivItemCollect);
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
        /*if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
            if (mRadioItemPlayAnimation != null && !mRadioItemPlayAnimation.isRunning()){
                Log.d(TAG, "startAnim: start ");
                mRadioItemPlayAnimation.start();
            }
        } else {
            if (mRadioItemPlayAnimation != null && mRadioItemPlayAnimation.isRunning()){
                Log.d(TAG, "startAnim: stop ");
                mRadioItemPlayAnimation.stop();
            }
        }*/
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
