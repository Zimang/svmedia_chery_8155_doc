package com.desaysv.moduleradio.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.hardware.radio.RadioManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.ListUtils;
import com.desaysv.moduledab.utils.ProductUtils;
import com.desaysv.moduleradio.R;
import com.desaysv.moduleradio.Trigger.ModuleRadioTrigger;
import com.desaysv.moduleradio.utils.RadioCovertUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by LZM on 2019-8-3.
 * Comment 收音列表的适配器
 */
public class RadioMessageListAdapter extends RecyclerView.Adapter<RadioMessageListAdapter.ViewHolder> {

    private static final String TAG = "RadioMessageListAdapter";


    private Context mContext;
    private List<RadioMessage> mRadioMessageList;
    private OnItemClickListener itemClickListener;
    private ViewHolder mHolder;
    private boolean isRightRudder;
    private boolean hasMulti;

    public RadioMessageListAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        itemClickListener = listener;
        isRightRudder = ProductUtils.isRightRudder();
        hasMulti = ProductUtils.hasMulti();
    }

    public void updateList(List<RadioMessage> list){
        mRadioMessageList = list;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(isRightRudder ? R.layout.radio_list_item_right: R.layout.radio_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if (position != -1) {
                    RadioMessage radioMessage = mRadioMessageList.get(position);
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(position, radioMessage);
                    }
                }
            }
        });
        mHolder = viewHolder;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RadioMessage radioMessage = mRadioMessageList.get(position);
        Log.d(TAG, "onBindViewHolder: radioMessage = " + radioMessage);
        if (hasMulti){
            holder.tvMultiType.setVisibility(View.VISIBLE);
        }else {
            holder.tvMultiType.setVisibility(View.GONE);
        }
        String frequency = "";
        if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
            switch (radioMessage.getRadioBand()) {
                case RadioManager.BAND_AM:
                case RadioManager.BAND_AM_HD:
                    frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_am_item_title),radioMessage.getRadioFrequency());
                    holder.tvMultiType.setVisibility(View.GONE);
                    break;
                case RadioManager.BAND_FM:
                case RadioManager.BAND_FM_HD:
                    //Android 原生Bug,俄语环境下，“.”会被变成","
                    //FM+频点值在各个语言下是一样的显示，因此强制使用英语环境显示这个字串
                    frequency = frequency + String.format(Locale.ENGLISH,mContext.getResources().getString(R.string.radio_fm_item_title),radioMessage.getRadioFrequency() / 1000.0);
                    holder.tvMultiType.setText("FM");
                    break;
            }
        }else {
            frequency = radioMessage.getDabMessage().getShortProgramStationName();
            holder.tvRadioName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
            holder.tvMultiType.setText("DAB");
        }
        holder.tvFreq.setText(frequency);
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();

        if (currentRadioMessage.getRadioType() ==  RadioMessage.DAB_TYPE){
            Log.d(TAG, "onBindViewHolder: mCurrentRadioMessage = " + radioMessage);
            if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
                //DAB需要多加判断
                if (radioMessage.getDabMessage().getFrequency() == currentRadioMessage.getDabMessage().getFrequency() && currentRadioMessage.getDabMessage() != null && radioMessage.getDabMessage().getServiceId() == currentRadioMessage.getDabMessage().getServiceId()
                        && radioMessage.getDabMessage().getServiceComponentId() == currentRadioMessage.getDabMessage().getServiceComponentId()){
                    holder.ivItemIcon.setSelected(true);
                    holder.ivItemPlay.setVisibility(View.VISIBLE);
                    holder.ivItemPlayBg.setVisibility(View.VISIBLE);
                    if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
                        holder.ivItemPlay.start();
                    } else {
                        holder.ivItemPlay.stop();
                    }
                    //优先使用Sls
                    byte[] logoDataList = currentRadioMessage.getDabMessage().getSlsDataList();
                    //次级使用存储的Logo
                    if (logoDataList == null || logoDataList.length < 1){
                        logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
                    }
                    //最后使用当前获取到的Logo
                    if (logoDataList == null){
                        logoDataList = currentRadioMessage.getDabMessage().getLogoDataList();
                    }
                    if (logoDataList != null && logoDataList.length > 0){
                        holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
                    }else {
                        holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);
                    }
                }else {
                    holder.ivItemIcon.setSelected(false);
                    holder.ivItemPlay.setVisibility(View.GONE);
                    holder.ivItemPlayBg.setVisibility(View.GONE);
                    byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
                    if (logoDataList != null && logoDataList.length > 0){
                        holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
                    }else {
                        holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);
                    }
                }

            }else {
                byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
                if (logoDataList != null && logoDataList.length > 0){
                    holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
                }else {
                    holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);
                }
                holder.ivItemIcon.setSelected(false);
                holder.ivItemPlay.setVisibility(View.GONE);
                holder.ivItemPlayBg.setVisibility(View.GONE);
                String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : null;
                if (currentRadioMessage.getRdsRadioText() != null && radioName == null) {
                    radioName = currentRadioMessage.getRdsRadioText().getProgramStationName();
                }
                holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);
            }
        }else {
            if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
                holder.ivItemIcon.setSelected(false);
                holder.ivItemPlay.setVisibility(View.GONE);
                holder.ivItemPlayBg.setVisibility(View.GONE);
                byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
                if (logoDataList != null && logoDataList.length > 0){
                    holder.ivItemIcon.setImageBitmap(BitmapFactory.decodeByteArray(logoDataList, 0, logoDataList.length));
                }else {
                    holder.ivItemIcon.setImageResource(com.desaysv.moduledab.R.mipmap.icon_logo);
                }
            }else {
                String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : null;
                if (currentRadioMessage.getRdsRadioText() != null && radioName == null) {
                    radioName = currentRadioMessage.getRdsRadioText().getProgramStationName();
                }
                holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);
                if (currentRadioMessage.getRadioBand() == radioMessage.getRadioBand() && currentRadioMessage.getRadioFrequency() == radioMessage.getRadioFrequency()){
                    holder.ivItemIcon.setSelected(true);
                    holder.ivItemPlay.setVisibility(View.VISIBLE);
                    holder.ivItemPlayBg.setVisibility(View.VISIBLE);
                    RDSRadioText rdsRadioText = currentRadioMessage.getRdsRadioText();
                    if (rdsRadioText != null){
                        String currentRDSName = rdsRadioText.getProgramStationName();
                        if (currentRDSName != null && currentRDSName.trim().length() > 1){
                            holder.tvRadioName.setText(currentRDSName);
                        }
                    }
                    if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
                        holder.ivItemPlay.start();
                    } else {
                        holder.ivItemPlay.stop();
                    }
                }else {
                    holder.ivItemIcon.setSelected(false);
                    holder.ivItemPlay.setVisibility(View.GONE);
                    holder.ivItemPlayBg.setVisibility(View.GONE);
                }
            }
        }

    }


    @Override
    public int getItemCount() {
        return this.mRadioMessageList == null ? 0 : this.mRadioMessageList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivItemIcon;

        ImageView ivItemPlayBg;
        AudioWaveView ivItemPlay;
        TextView tvFreq;
        TextView tvRadioName;

        TextView tvMultiType;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
            ivItemPlay = itemView.findViewById(R.id.ivItemPlay);
            ivItemPlay.setNeedAutoStart(false);
            tvFreq = itemView.findViewById(R.id.tvFreq);
            tvRadioName = itemView.findViewById(R.id.tvRadioName);
            ivItemPlayBg = itemView.findViewById(R.id.ivItemPlayBg);
            tvMultiType = itemView.findViewById(R.id.tvMultiType);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, RadioMessage radioMessage);
    }

    public void updatePlayItemAnim() {
        Log.d(TAG, "updatePlayAnim: ");
        if (mHolder == null){
            return;
        }
        if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
            mHolder.ivItemPlay.start();
        } else {
            Log.d(TAG, "startAnim: stop ");
            mHolder.ivItemPlay.stop();
        }
    }

}
