package com.desaysv.moduleusbmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.mediacommonlib.view.AudioWaveView;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 这个是音乐列表的基本适配器
 * @time 2022-11-18 13:29
 */
public class MusicPlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = MusicPlayListAdapter.class.getSimpleName();
    private List<FileMessage> fileMessageList;
    private MusicListItemClickListener listener;
    private Context context;
    private int currentHighLightPosition = 0;

    public MusicPlayListAdapter(Context context, List<FileMessage> fileMessageList) {
        this.context = context;
        this.fileMessageList = fileMessageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_play_fragment_list_item, parent, false);
        return new MusicViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final FileMessage fileMessage = fileMessageList.get(position);
        Log.d(TAG, "onBindViewHolder: position = " + position + " fileMessage = " + fileMessage);
        if (fileMessage == null) {
            Log.w(TAG, "onBindViewHolder: fileMessage == null");
            return;
        }
        final MusicViewHolder viewHolder = (MusicViewHolder) holder;
        viewHolder.position.setText(position + 1 + "");
        //显示歌名
        viewHolder.songName.setText(fileMessage.getName());
        //艺术家
        viewHolder.songArtist.setText(fileMessage.getAuthor());
        //如果是当前则高亮
        FileMessage currentPlayItem = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem();
        //id对应当前是唯一的（不清楚重新接入USB会不会变，先这么做，速度快，有问题再改成绝对路径比较）
        if (fileMessage.getId() == currentPlayItem.getId()) {
            currentHighLightPosition = position;
            viewHolder.itemClick.setSelected(true);
            if (ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().isPlaying()) {
                viewHolder.audioWaveView.setVisibility(View.VISIBLE);
                viewHolder.audioWaveView.start();
            } else {
                viewHolder.audioWaveView.setVisibility(View.INVISIBLE);
            }
        } else {
            viewHolder.itemClick.setSelected(false);
            viewHolder.audioWaveView.setVisibility(View.INVISIBLE);
            viewHolder.audioWaveView.stop();
        }
        //配置点击监听
        viewHolder.itemClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position, fileMessage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileMessageList.size();
    }

    /**
     * 更改列表
     *
     * @param fileMessageList fileMessageList
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setFileMessageList(List<FileMessage> fileMessageList) {
        Log.d(TAG, "setFileMessageList: ");
        this.fileMessageList = fileMessageList;
        notifyDataSetChanged();
    }

    /**
     * 当前高亮的位置
     *
     * @return currentHighLightPosition
     */
    public int getCurrentHighLightPosition() {
        return currentHighLightPosition;
    }

    /**
     * 复用条目信息
     */
    private static class MusicViewHolder extends RecyclerView.ViewHolder {
        private TextView position;
        private TextView songName;
        private TextView songArtist;
        private ImageButton itemClick;
        private AudioWaveView audioWaveView;

        @SuppressLint("CutPasteId")
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            position = itemView.findViewById(R.id.tv_position);
            songName = itemView.findViewById(R.id.tv_song_name);
            songArtist = itemView.findViewById(R.id.tv_artist);
            itemClick = itemView.findViewById(R.id.ib_click);
            audioWaveView = itemView.findViewById(R.id.av_play_state);
        }
    }

    /**
     * 配置监听
     *
     * @param listener listener
     */
    public void setItemClickListener(MusicListItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 接口变化回调
     */
    public interface MusicListItemClickListener {

        /**
         * 当前条目发生变化
         *
         * @param fileMessage fileMessage
         */
        void onItemClick(int position, FileMessage fileMessage);
    }
}
