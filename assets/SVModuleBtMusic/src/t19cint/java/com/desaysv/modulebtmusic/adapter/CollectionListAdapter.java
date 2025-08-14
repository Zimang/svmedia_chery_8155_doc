package com.desaysv.modulebtmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.modulebtmusic.BaseConstants;
import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.bean.SVMusicInfo;
import com.desaysv.modulebtmusic.manager.BTMusicManager;

import java.util.ArrayList;
import java.util.List;

public class CollectionListAdapter extends BaseAdapter<SVMusicInfo, CollectionListAdapter.CollectionListViewHolder> {
    private static final String TAG = "CollectionListADT";
    private Context mContext;
    private OnItemClickListener mClickListener;

    public CollectionListAdapter(Context context, List<SVMusicInfo> list) {
        mContext = context;
        updateList(list);
    }

    public void updateList(List<SVMusicInfo> list) {
        if (list == null) {
            this.addAll(new ArrayList<>());
        } else {
            this.addAll(list);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mClickListener = listener;
    }

    @Override
    public CollectionListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collection_list_adapter, parent, false);
        return new CollectionListViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(CollectionListViewHolder holder, int position) {
        SVMusicInfo musicInfo = getItem(position);
        if (musicInfo != null) {
            holder.tv_collection_list_title.setText(musicInfo.getMediaTitle());
            holder.tv_collection_list_artist.setText(musicInfo.getArtistName());
            if (BTMusicManager.getInstance().isPlayingState(musicInfo.getPlayState())) {
                holder.rl_playing_icon.setVisibility(View.VISIBLE);
                holder.tv_collection_list_title.setTextColor(mContext.getColor(R.color.color_collection_adapter_playing));
                holder.tv_collection_list_artist.setTextColor(mContext.getColor(R.color.color_collection_adapter_playing));
            } else {
                holder.rl_playing_icon.setVisibility(View.GONE);
                holder.tv_collection_list_title.setTextColor(mContext.getColor(R.color.color_collection_adapter_title));
                holder.tv_collection_list_artist.setTextColor(mContext.getColor(R.color.color_collection_adapter_artist));
            }
        }
    }

    protected class CollectionListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        OnItemClickListener mListener;
        RelativeLayout rl_root;
        RelativeLayout rl_playing_icon;
        ImageView iv_playing_icon;
        TextView tv_collection_list_title;
        TextView tv_pay_notice;
        TextView tv_collection_list_artist;

        public CollectionListViewHolder(View view, OnItemClickListener listener) {
            super(view);
            mListener = listener;
            rl_root = view.findViewById(R.id.rl_root);
            rl_root.setOnClickListener(this);
            rl_playing_icon = view.findViewById(R.id.rl_playing_icon);
            iv_playing_icon = view.findViewById(R.id.iv_playing_icon);
            tv_collection_list_title = view.findViewById(R.id.tv_collection_list_title);
            tv_pay_notice = view.findViewById(R.id.tv_pay_notice);
            tv_collection_list_artist = view.findViewById(R.id.tv_collection_list_artist);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.rl_root) {
                mListener.onItemClick(v, getPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
