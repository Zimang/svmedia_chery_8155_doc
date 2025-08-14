package com.desaysv.moduleusbvideo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.moduleusbvideo.R;

/**
 * Created by LZM on 2019-8-3.
 * Comment USB图片的列表适配器
 */
public class USBVideoSpeedListAdapter extends RecyclerView.Adapter<USBVideoSpeedListAdapter.ViewHolder> {
    private static final String TAG = "USBVideoSpeedListAdapter";

    private final Context mContext;
    private final String[] mFileMessageList;
    private final ItemClickListener itemClickListener;
    private float currentSpeed = 1.0f;

    public USBVideoSpeedListAdapter(Context context, ItemClickListener itemClickListener) {
        mContext = context;
        mFileMessageList = context.getResources().getStringArray(R.array.usb_video_DoubleSpeed);
        this.itemClickListener = itemClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSpeed(float speed) {
        Log.d(TAG, "updateSpeed: speed = " + speed);
        this.currentSpeed = speed;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_speed_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                float speed = Float.parseFloat(mFileMessageList[position]);
                Log.d(TAG, "onClick: position = " + position + " speed = " + speed);
                if (null != itemClickListener) {
                    itemClickListener.onItemClick(speed);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String data = mFileMessageList[position];
        if (String.valueOf(currentSpeed).equals(data)) {
            holder.tv_speed_name.setTextColor(mContext.getColor(R.color.select_mode_color));
        } else {
            holder.tv_speed_name.setTextColor(mContext.getColor(R.color.tab_text_color));
        }
        holder.tv_speed_name.setText(appendX(mContext, data));
    }


    @Override
    public int getItemCount() {
        return this.mFileMessageList == null ? 0 : this.mFileMessageList.length;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tv_speed_name;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tv_speed_name = itemView.findViewById(R.id.tv_speed_name);
        }
    }

    /**
     * 倍速显示 ，数字字体大，x字体小
     *
     * @param data data
     * @return SpannableString
     */
    public static SpannableString appendX(Context mContext, String data) {
        data = data + " x";
        SpannableString textSpan = new SpannableString(data);
        textSpan.setSpan(new AbsoluteSizeSpan(40), 0, data.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textSpan.setSpan(new AbsoluteSizeSpan(24), data.length() - 1, data.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textSpan.setSpan(new ForegroundColorSpan(mContext.getColor(R.color.text_color_gray)), data.length() - 1, data.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return textSpan;
    }

    /**
     * 列表item点击的时候，会触发的回调
     */
    public interface ItemClickListener {
        /**
         * 列表的点击事件
         */
        void onItemClick(float data);
    }

}
