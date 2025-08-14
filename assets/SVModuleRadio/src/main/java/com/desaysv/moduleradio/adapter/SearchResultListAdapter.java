package com.desaysv.moduleradio.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.hardware.radio.RadioManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduledab.trigger.DABTrigger;
import com.desaysv.moduledab.utils.ListUtils;
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
public class SearchResultListAdapter extends RecyclerView.Adapter<SearchResultListAdapter.ViewHolder> {

    private static final String TAG = "SearchResultListAdapter";

    private List<RadioMessage> resultList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener itemClickListener;

    private String queryText="";

    public SearchResultListAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        itemClickListener = listener;
    }

    public void updateList(List<RadioMessage> list, String newText){
        resultList = list;
        queryText = newText;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.radio_resultlist_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RadioMessage radioMessage = resultList.get(position);
        Log.d(TAG, "onBindViewHolder: radioMessage = " + radioMessage);
        String frequency = "";
        Spannable spannable = null;
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.dab_playani_color1));
        int start = 0;
        int end = 0;
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
                    holder.tvMultiType.setVisibility(View.VISIBLE);
                    break;
            }
        }else {
            frequency = radioMessage.getDabMessage().getShortProgramStationName();
            holder.tvRadioName.setText(radioMessage.getDabMessage().getShortEnsembleLabel());
            holder.tvMultiType.setText("DAB");
            holder.tvMultiType.setVisibility(View.VISIBLE);
        }
        holder.tvFreq.setText(frequency);
        RadioMessage currentRadioMessage = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
            if (currentRadioMessage.getRadioType() ==  RadioMessage.DAB_TYPE){
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
                    RequestOptions option = RequestOptions
                            .bitmapTransform(new RoundedCorners(24))
                            .error(R.mipmap.icon_logo);
                    Glide.with(mContext).load(logoDataList)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .apply(option)
                            .into(holder.ivItemIcon);
                }else {
                    holder.ivItemIcon.setSelected(false);
                    holder.ivItemPlay.setVisibility(View.GONE);
                    holder.ivItemPlayBg.setVisibility(View.GONE);
                    byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
                    RequestOptions option = RequestOptions
                            .bitmapTransform(new RoundedCorners(24))
                            .error(R.mipmap.icon_logo);
                    Glide.with(mContext).load(logoDataList)
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .apply(option)
                            .into(holder.ivItemIcon);
                }
            }else {
                holder.ivItemIcon.setSelected(false);
                holder.ivItemPlay.setVisibility(View.GONE);
                holder.ivItemPlayBg.setVisibility(View.GONE);
            }
        }else {
            byte[] logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
            RequestOptions option = RequestOptions
                    .bitmapTransform(new RoundedCorners(24))
                    .error(R.mipmap.icon_logo);
            Glide.with(mContext).load(logoDataList)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .apply(option)
                    .into(holder.ivItemIcon);
            holder.ivItemIcon.setSelected(false);
            holder.ivItemPlay.setVisibility(View.GONE);
            holder.ivItemPlayBg.setVisibility(View.GONE);
            String radioName = RadioCovertUtils.getOppositeRDSName(radioMessage) != null ? RadioCovertUtils.getOppositeRDSName(radioMessage) : null;
            holder.tvRadioName.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? mContext.getResources().getString(R.string.radio_station_name): radioName);
            holder.tvFreq.setText((radioName == null || radioName.length() < 1 || radioName.trim().length() < 1) ? frequency: radioName);
            if (currentRadioMessage.getRadioType() ==  RadioMessage.DAB_TYPE){
                holder.ivItemIcon.setSelected(false);
                holder.ivItemPlay.setVisibility(View.GONE);
                holder.ivItemPlayBg.setVisibility(View.GONE);
            }else {
                if (currentRadioMessage.getRadioBand() == radioMessage.getRadioBand() && currentRadioMessage.getRadioFrequency() == radioMessage.getRadioFrequency()) {
                    holder.ivItemIcon.setSelected(true);
                    holder.ivItemPlay.setVisibility(View.VISIBLE);
                    holder.ivItemPlayBg.setVisibility(View.VISIBLE);
                    if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
                        holder.ivItemPlay.start();
                    } else {
                        holder.ivItemPlay.stop();
                    }
                }
            }
        }

        spannable = new SpannableString(holder.tvFreq.getText());
        start = holder.tvFreq.getText().toString().toLowerCase().indexOf(queryText.toLowerCase());
        if (start != -1){
            end = start + queryText.length();
            spannable.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvFreq.setText(spannable);
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position, radioMessage);
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return this.resultList == null ? 0 : this.resultList.size();
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
        void onItemClick(int position, RadioMessage resultMessage);
    }


}
