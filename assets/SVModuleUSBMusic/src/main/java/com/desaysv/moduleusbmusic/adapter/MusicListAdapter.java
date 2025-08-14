package com.desaysv.moduleusbmusic.adapter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.mediacommonlib.utils.ProductConfig;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.utils.ImageUtils;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author uidq1846
 * @desc 这个是音乐列表的基本适配器
 * @time 2022-11-18 13:29
 */
public class MusicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = MusicListAdapter.class.getSimpleName();
    private final ContentResolver contentResolver;
    private List<FileMessage> fileMessageList;
    private MusicListItemClickListener listener;
    private Context context;
    private int currentHighLightPosition = 0;
    private final Uri ART_URI = Uri.parse("content://media/external/audio/albumart");
    //选中的视图
    private final Map<String, FileMessage> selectedMap = new HashMap<>();
    private boolean showSelectView = false;

    public MusicListAdapter(Context context, List<FileMessage> fileMessageList) {
        this.context = context;
        this.fileMessageList = fileMessageList;
        contentResolver = context.getContentResolver();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(ProductConfig.isTheme2(context) ? R.layout.music_fragment_list_item_theme2 : R.layout.music_fragment_list_item, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final FileMessage fileMessage = fileMessageList.get(position);
        Log.d(TAG, "onBindViewHolder: position = " + position + " fileMessage = " + fileMessage);
        if (fileMessage == null) {
            Log.w(TAG, "onBindViewHolder: fileMessage == null");
            return;
        }
        final MusicViewHolder viewHolder = (MusicViewHolder) holder;
        //显示歌名
        viewHolder.songName.setText(fileMessage.getName());
        //艺术家
        viewHolder.songArtist.setText(fileMessage.getAuthor());
        //是否显示勾选窗
        if (isShowSelectView()) {
            //如果选中的需要高亮显示
            viewHolder.itemSelect.setSelected(selectedMap.containsKey(fileMessage.getPath()));
            viewHolder.itemSelect.setVisibility(View.VISIBLE);
        } else {
            viewHolder.itemSelect.setVisibility(View.INVISIBLE);
        }
        //显示专辑图
        if (fileMessage.getAlbumId() >= 0) {
            Uri uri = ContentUris.withAppendedId(ART_URI, fileMessage.getAlbumId());
            //校验下是否有文件
            ImageUtils.getInstance().showImage(viewHolder.albumImage, uri, R.mipmap.music_fragment_list_album_n, R.mipmap.music_fragment_list_album_n);
        } else {
            Log.w(TAG, "onBindViewHolder: albumId error fileMessage = " + fileMessage);
        }
        //当前条目高亮
        //如果是当前则高亮
        FileMessage currentPlayItem = ModuleUSBMusicTrigger.getInstance().getCurrentIGetControlTool().getStatusTool().getCurrentPlayItem();
        //id对应当前是唯一的（不清楚重新接入USB会不会变，先这么做，速度快，有问题再改成绝对路径比较）
        if (fileMessage.getId() == currentPlayItem.getId()) {
            currentHighLightPosition = position;
        }
        //配置点击监听
        viewHolder.itemClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowSelectView()) {
                    //记录需要删除的选项,点击时选择则清除
                    if (selectedMap.containsKey(fileMessage.getPath())) {
                        selectedMap.remove(fileMessage.getPath());
                    } else {
                        selectedMap.put(fileMessage.getPath(), fileMessage);
                    }
                    //刷新当前条目
                    notifyItemChanged(position);
                    //回调到桌面
                    if (listener != null) {
                        listener.onSelectItemClick(position, selectedMap.size(), fileMessage);
                    }
                } else {
                    if (listener != null) {
                        listener.onItemClick(position, fileMessage);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileMessageList.size();
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
     * 获取传入曲目在播放的曲目在当前列表当中的位置
     * 需异步处理
     *
     * @return currentHighLightPosition
     */
    public int getPositionInShowList(FileMessage fileMessage) {
        if (fileMessageList != null) {
            for (int i = 0; i < fileMessageList.size(); i++) {
                FileMessage message = fileMessageList.get(i);
                if (message != null && message.getId() == fileMessage.getId()) {
                    return i;
                }
            }
        }
        return -1;
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
     * 是否需要显示选择圈视图
     *
     * @return boolean
     */
    public boolean isShowSelectView() {
        return showSelectView;
    }

    /**
     * 配置选择圈视图
     * 隐藏则显示/显示则隐藏
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showOrHideSelectView(boolean showSelectView) {
        Log.d(TAG, "showOrHideSelectView: showSelectView = " + showSelectView);
        this.showSelectView = showSelectView;
        if (this.showSelectView) {
            selectedMap.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 复用条目信息
     */
    private static class MusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumImage;
        private TextView songName;
        private TextView songArtist;
        private ImageButton itemClick;
        private ImageView itemSelect;

        @SuppressLint("CutPasteId")
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = itemView.findViewById(R.id.iv_album);
            songName = itemView.findViewById(R.id.tv_song_name);
            songArtist = itemView.findViewById(R.id.tv_artist);
            itemClick = itemView.findViewById(R.id.ib_click);
            itemSelect = itemView.findViewById(R.id.iv_item_select);
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
     * 提供当前的文件内容以供拷贝或者删除
     *
     * @return Map<String, Boolean>
     */
    public Map<String, FileMessage> getSelectedMap() {
        return selectedMap;
    }

    /**
     * 全选
     */
    @SuppressLint("NotifyDataSetChanged")
    public void allSelect() {
        if (fileMessageList != null) {
            for (FileMessage f : fileMessageList) {
                selectedMap.put(f.getPath(), f);
            }
            //更新布局
            notifyDataSetChanged();
        }
        //通知界面更新数量
        if (listener != null) {
            listener.onSelectItemClick(-1, selectedMap.size(), null);
        }
    }

    /**
     * 所有都取消
     */
    @SuppressLint("NotifyDataSetChanged")
    public void allCancel() {
        Log.d(TAG, "allCancel: ");
        selectedMap.clear();
        notifyDataSetChanged();
        //通知界面更新数量
        if (listener != null) {
            listener.onSelectItemClick(-2, selectedMap.size(), null);
        }
    }

    /**
     * 当前是否全选状态
     *
     * @return boolean
     */
    public boolean isAllSelect() {
        return selectedMap.size() == fileMessageList.size();
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

        /**
         * 当条目为选项时触发得回调
         *
         * @param selectPosition selectPosition
         * @param selectSize     selectSize 总大小
         * @param fileMessage    fileMessage
         */
        void onSelectItemClick(int selectPosition, int selectSize, FileMessage fileMessage);
    }
}
