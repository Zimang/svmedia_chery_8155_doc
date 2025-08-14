package com.desaysv.moduleusbvideo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.desaysv.libusbmedia.utils.TimeUtils;
import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.videoapp.glidev.GlideApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2019-8-3.
 * Comment USB图片的列表适配器
 */
public class USBVideoPlayListAdapter extends RecyclerView.Adapter<USBVideoPlayListAdapter.ViewHolder> {
    private static final String TAG = "USBVideoPlayListAdapter";

    private final Context mContext;
    private final List<FileMessage> mFileMessageList = new ArrayList<>();
    private final ItemClickListener itemClickListener;
    private int currentPosition = -1;

    private String mLastPath = "";
    private boolean playStatus = false;

    private boolean isRtl = false;

    public USBVideoPlayListAdapter(Context context, List<FileMessage> fileMessageList, ItemClickListener itemClickListener) {
        mContext = context;
        mFileMessageList.addAll(fileMessageList);
        this.itemClickListener = itemClickListener;
        isRtl = Constant.isRtl();
        Log.i(TAG, "USBVideoAllListAdapter: isRtl: " + isRtl);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePlayStatus(boolean playStatus) {
        Log.d(TAG, "updatePlayStatus: playStatus: " + playStatus);
        this.playStatus = playStatus;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLastPath(String mLastPath) {
        Log.d(TAG, "updateLastPath: mLastPath: " + mLastPath);
        this.mLastPath = mLastPath;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePosition(int position) {
        Log.d(TAG, "updatePosition: position = " + position);
        currentPosition = position;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<FileMessage> fileMessageList) {
        mFileMessageList.clear();
        mFileMessageList.addAll(fileMessageList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_play_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Log.d(TAG, "onClick: position = " + position);
                if (null != itemClickListener) {
                    itemClickListener.onItemClick(position);
                }
            }
        });

        if (isRtl) {
            viewHolder.tvVideoName.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileMessage fileMessage = mFileMessageList.get(position);
        String videoName = fileMessage.getFileName();
        holder.tvVideoName.setText(videoName);
        holder.tvTime.setText(TimeUtils.longToTimeStr(fileMessage.getDuration()));
        //显示视频的缩略图（Glide直接支持）
        GlideApp.with(mContext).load(fileMessage)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(new CenterCrop(), new RoundedCorners(10))
                .placeholder(R.mipmap.usb_video_media)
                .error(R.mipmap.usb_video_media)
                .into(holder.ivPlayThumbnail);

        Log.d(TAG, "onBindViewHolder: position = " + position + " currentPosition = " + currentPosition);


        //如果路径相同，那就说明需要高亮显示
        if (position == currentPosition) {
            Log.d(TAG, "onBindViewHolder: show playStatus: " + playStatus);
            holder.fl_awv_icon.setVisibility(View.VISIBLE);
            holder.ivPlayIcon.setVisibility(View.VISIBLE);
//            holder.view.setSelected(true);
            if (playStatus) {
                holder.ivPlayIcon.start();
            } else {
                holder.ivPlayIcon.stop();
            }
        } else {
            //            holder.view.setSelected(false);
            holder.fl_awv_icon.setVisibility(View.GONE);
            holder.ivPlayIcon.setVisibility(View.GONE);

        }
        // 判断是否是上次播放路径
        if (fileMessage.getPath().equals(mLastPath)) {
            holder.fl_iv_last_bg.setVisibility(View.VISIBLE);
        } else {
            holder.fl_iv_last_bg.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return this.mFileMessageList == null ? 0 : this.mFileMessageList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        AudioWaveView ivPlayIcon;
        ImageView ivLastPlay;
        ImageView ivPlayThumbnail;
        TextView tvTime;
        TextView tvVideoName;
        FrameLayout fl_awv_icon;
        FrameLayout fl_iv_last_bg;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            ivPlayIcon.setNeedAutoStart(false);
            ivLastPlay = itemView.findViewById(R.id.ivLastPlay);
            ivPlayThumbnail = itemView.findViewById(R.id.ivPlayThumbnail);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvVideoName = itemView.findViewById(R.id.tvVideoName);
            fl_awv_icon = itemView.findViewById(R.id.fl_awv_icon);
            fl_iv_last_bg = itemView.findViewById(R.id.fl_iv_last_bg);
        }
    }

    public int getCurrentPosition() {
        Log.d(TAG, "getCurrentPosition: currentPosition = " + currentPosition);
        return currentPosition;
    }

    /**
     * 列表item点击的时候，会触发的回调
     */
    public interface ItemClickListener {
        /**
         * 列表的点击事件
         */
        void onItemClick(int position);
    }

}
