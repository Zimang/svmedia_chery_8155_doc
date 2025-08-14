package com.desaysv.moduleradio.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.moduleradio.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2019-8-3.
 * Comment 收音列表的适配器
 */
public class SearchHistoryListAdapter extends RecyclerView.Adapter<SearchHistoryListAdapter.ViewHolder> {

    private static final String TAG = "SearchHistoryListAdapter";

    private List<String> historyList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener itemClickListener;

    public SearchHistoryListAdapter(Context context, OnItemClickListener listener) {
        mContext = context;
        itemClickListener = listener;
    }

    public void updateList(List<String> list){
        historyList = list;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.radio_historylist_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(mView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String history = historyList.get(position);
        Log.d(TAG, "onBindViewHolder: history = " + history);
        holder.tvHistory.setText(history);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(position,history);
            }
        });
    }


    @Override
    public int getItemCount() {
        return this.historyList == null ? 0 : this.historyList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvHistory;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvHistory = itemView.findViewById(R.id.tvHistory);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String history);
    }


}
