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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.rds.RDSRadioText;
import com.desaysv.mediacommonlib.ui.MarqueeTextView;
import com.desaysv.mediacommonlib.view.AudioWaveView;
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
public class RadioMultiListAdapter extends RecyclerView.Adapter<RadioMultiListAdapter.ViewHolder> {

    private static final String TAG = "RadioMultiListAdapter";


    private Context mContext;
    private List<RadioMessage> mRadioMessageList;
    private OnItemClickListener itemClickListener;
    private ViewHolder mHolder;
    private boolean isRightRudder;
    private boolean hasMulti;
    //之前播放的message，避免 播放列表数据改变 ，Position无法代表之前播放的item数据
    private RadioMessage lastRadioMessage;

    public RadioMultiListAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        itemClickListener = listener;
        isRightRudder = ProductUtils.isRightRudder();
        hasMulti = ProductUtils.hasMulti();
    }

    public void updateList(List<RadioMessage> list){
        mRadioMessageList = RadioCovertUtils.sortWithName(mContext,list);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate( R.layout.radio_multilist_item, parent, false);
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
        holder.tvFreq.setNeedMarquee(false);
        holder.tvFreq.setText(frequency);
        RadioMessage currentRadioMessage000 = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        //直接复制一个新的对象，避免频繁切换 am/ dab 导致的 空指针 闪退问题
        RadioMessage currentRadioMessage = currentRadioMessage000.Clone();
        if (radioMessage.getRadioType() == RadioMessage.DAB_TYPE){
            if (currentRadioMessage.getRadioType() ==  RadioMessage.DAB_TYPE){
                if (radioMessage.getDabMessage().getFrequency() == currentRadioMessage.getDabMessage().getFrequency() && currentRadioMessage.getDabMessage() != null && radioMessage.getDabMessage().getServiceId() == currentRadioMessage.getDabMessage().getServiceId()
                        && radioMessage.getDabMessage().getServiceComponentId() == currentRadioMessage.getDabMessage().getServiceComponentId()){
                    holder.ivItemIcon.setSelected(true);
                    holder.ivItemPlay.setVisibility(View.VISIBLE);
                    holder.ivItemPlayBg.setVisibility(View.VISIBLE);
                    holder.tvFreq.setNeedMarquee(true);
                    if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
                        holder.ivItemPlay.start();
                    } else {
                        holder.ivItemPlay.stop();
                    }
                    //优先使用Sls
                    byte[] logoDataList = null;
                    //这里有个频繁切换 am/ dab 导致的 空指针 闪退问题
                    if(currentRadioMessage.getRadioType() ==  RadioMessage.DAB_TYPE && currentRadioMessage.getDabMessage()!=null){
                        logoDataList = currentRadioMessage.getDabMessage().getSlsDataList();
                    }
                    //优先使用Sls
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
                    byte[] logoDataList = null;
                    if (radioMessage.getDabMessage() != null) {
                        logoDataList = radioMessage.getDabMessage().getSlsDataList();
                    }
                    //优先使用Sls
                    //次级使用存储的Logo
                    if (logoDataList == null || logoDataList.length < 1){
                        logoDataList = ListUtils.getOppositeDABLogo(radioMessage);
                    }
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
                    holder.tvFreq.setNeedMarquee(true);
                    if (ModuleRadioTrigger.getInstance().mGetRadioStatusTool.isPlaying()) {
                        holder.ivItemPlay.start();
                    } else {
                        holder.ivItemPlay.stop();
                    }
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
        MarqueeTextView tvFreq;
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

    public void notifyLastCurrentItem(){
        notifyLastCurrentItem(mRadioMessageList);
    }

    /**
     * 局部刷新之前播放和现在播放的item
     * 如果list==null或者没有数据，是初始为空（不需要刷新）或者设置了data（还会调用其他全量刷新），此方法不刷新没影响
     */
    private void notifyLastCurrentItem(List<RadioMessage> list){
        RadioMessage currentRadioMessage000 = ModuleRadioTrigger.getInstance().mGetRadioStatusTool.getCurrentRadioMessage();
        RadioMessage currentRadioMessage = currentRadioMessage000.Clone();
        //重新找到之前播放的item在列表中的位置
        int lastPlayPosition = findPlayPosition(list, lastRadioMessage);
        //找到现在播放的item在列表中的位置
        int curPlayPosition = findPlayPosition(list, currentRadioMessage);

        Log.d(TAG,"notifyItemByPosition =" +" lastPlayPosition=" + lastPlayPosition + " curPosition="+curPlayPosition);
        if(lastPlayPosition != curPlayPosition &&
                lastPlayPosition != RecyclerView.NO_POSITION && lastPlayPosition < getItemCount()){
            notifyItemChanged(lastPlayPosition);
        }
        if(curPlayPosition != RecyclerView.NO_POSITION && curPlayPosition < getItemCount()){
            notifyItemChanged(curPlayPosition);
            lastRadioMessage = list.get(curPlayPosition);
        } else {
            lastRadioMessage = null;
        }

    }

    /**
     * 找到当前radio在列表中的位置
     * @param list
     * @param currentRadioMessage
     * @return
     */
    private int findPlayPosition(List<RadioMessage> list, RadioMessage currentRadioMessage){
        int playPosition = RecyclerView.NO_POSITION;
        if(list == null || list.isEmpty() || currentRadioMessage == null){
            return playPosition;
        }
        RadioMessage tempRm;
        if (currentRadioMessage.getRadioType() == RadioMessage.DAB_TYPE){
            if(currentRadioMessage.getDabMessage() != null){
                for(int i = 0;i < list.size();i++){
                    tempRm = list.get(i);
                    if(tempRm.getRadioType() == RadioMessage.DAB_TYPE){
                        if(tempRm.getDabMessage() == null){
                            continue;
                        }
                        if (tempRm.getDabMessage().getFrequency() == currentRadioMessage.getDabMessage().getFrequency()
                                && tempRm.getDabMessage().getServiceId() == currentRadioMessage.getDabMessage().getServiceId()
                                && tempRm.getDabMessage().getServiceComponentId() == currentRadioMessage.getDabMessage().getServiceComponentId()){
                            playPosition = i;
                            break;
                        }
                    }
                }
            }
        } else {
            for(int i = 0;i < list.size();i++){
                if(list.get(i).getRadioType() == RadioMessage.FM_AM_TYPE){
                    if (currentRadioMessage.getRadioBand() == list.get(i).getRadioBand()
                            && list.get(i).getRadioFrequency() == currentRadioMessage.getRadioFrequency()){
                        playPosition = i;
                        break;
                    }
                }
            }
        }
        return playPosition;
    }

}
