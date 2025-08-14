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

import com.desaysv.mediacommonlib.utils.MediaThreadPoolExecutorUtils;
import com.desaysv.mediacommonlib.utils.ProductConfig;
import com.desaysv.moduleusbmusic.R;
import com.desaysv.moduleusbmusic.bean.FolderItem;
import com.desaysv.moduleusbmusic.trigger.ModuleUSBMusicTrigger;
import com.desaysv.moduleusbmusic.utils.ImageUtils;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author uidq1846
 * @desc 这个是USB音乐文件夹列表的基本适配器
 * @time 2022-11-18 13:29
 */
public class MusicFolderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = MusicFolderListAdapter.class.getSimpleName();
    private final ContentResolver contentResolver;
    private List<FolderItem> folderItems;
    private Map<String, List<FolderItem>> folderMap;
    private MusicListItemClickListener listener;
    private Context context;
    private static final int TYPE_FOLDER = 0;
    private static final int TYPE_MUSIC = 1;
    private int currentHighLightPosition = 0;
    private final Uri ART_URI = Uri.parse("content://media/external/audio/albumart");
    //用来做暂时缓存文件节点
    private final FolderItem currentFolder = new FolderItem();
    //当前文件夹的返回栈逻辑
    private final LinkedList<FolderItem> folderItemBackQ = new LinkedList<>();
    //选中的视图
    private final Map<String, FileMessage> selectedMap = new HashMap<>();
    private boolean showSelectView = false;

    /**
     * 创建方法
     *
     * @param context   context
     * @param folderMap folderMap
     */
    public MusicFolderListAdapter(Context context, Map<String, List<FolderItem>> folderMap) {
        this.context = context;
        this.folderMap = folderMap;
        contentResolver = context.getContentResolver();
        initRootNote();
        toNodeFolder(currentFolder.getNotePath());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        if (viewType == TYPE_FOLDER) {
            View folderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_fragment_folder_list_item, parent, false);
            return new FolderViewHolder(folderView);
        }
        View musicView = LayoutInflater.from(parent.getContext()).inflate(ProductConfig.isTheme2(context) ? R.layout.music_fragment_list_item_theme2 : R.layout.music_fragment_list_item, parent, false);
        return new MusicViewHolder(musicView);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (folderItems.size() <= position) {
            Log.w(TAG, "onBindViewHolder: folderItems has change,need notifyDataSetChanged");
            notifyDataSetChanged();
            return;
        }
        final FolderItem folderItem = folderItems.get(position);
        final FileMessage fileMessage = folderItem.getFileMessage();
        //为null则说明当前是文件夹
        if (fileMessage != null) {
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
                        if (listener != null) {
                            listener.onSelectItemClick(position, selectedMap.size(), folderItem);
                        }
                    } else {
                        MediaThreadPoolExecutorUtils.getInstance().submit(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onItemClick(position - (getFolderItems().size() - getFileMessage().size()), getFileMessage());
                                }
                            }
                        });
                    }
                }
            });
        } else {
            //音乐就不打了，只打文件夹的
            Log.d(TAG, "onBindViewHolder: position = " + position + " folderItem = " + folderItem.toString());
            final FolderViewHolder viewHolder = (FolderViewHolder) holder;
            //子目录的size
            List<FolderItem> folderItems = folderMap.get(folderItem.getNotePath());
            if (folderItems == null) {
                viewHolder.folderItemSize.setText(0 + context.getString(R.string.usb_music_folder_item_size_text));
            } else {
                viewHolder.folderItemSize.setText(folderItems.size() + context.getString(R.string.usb_music_folder_item_size_text));
            }
            //是否显示勾选窗
            if (isShowSelectView()) {
                //如果选中的需要高亮显示
                viewHolder.itemSelect.setSelected(selectedMap.containsKey(folderItem.getNotePath()));
                viewHolder.itemSelect.setVisibility(View.VISIBLE);
            } else {
                viewHolder.itemSelect.setVisibility(View.INVISIBLE);
            }
            viewHolder.folderName.setText(folderItem.getNoteName());
            //配置点击监听
            viewHolder.itemClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowSelectView()) {
                        //记录需要删除的选项,点击时选择则清除
                        if (selectedMap.containsKey(folderItem.getNotePath())) {
                            selectedMap.remove(folderItem.getNotePath());
                        } else {
                            selectedMap.put(folderItem.getNotePath(), null);
                        }
                        //刷新当前条目
                        notifyItemChanged(position);
                        if (listener != null) {
                            listener.onSelectItemClick(position, selectedMap.size(), folderItem);
                        }
                    } else {
                        //添加返回栈信息
                        folderItemBackQ.add(new FolderItem().copy(currentFolder));
                        //缓存新的信息
                        currentFolder.copy(folderItem);
                        Log.d(TAG, "onClick: " + folderItem.getNotePath() + " " + folderItem.getNoteName());
                        toNodeFolder(folderItem.getNotePath());
                    }
                }
            });
        }
    }

    /**
     * 找寻下个目录
     *
     * @param childNotePath childNotePath
     */
    @SuppressLint("NotifyDataSetChanged")
    private void toNodeFolder(String childNotePath) {
        Log.d(TAG, "toNodeFolder: childNotePath = " + childNotePath);
        setFolderItems(folderMap.get(childNotePath));
        if (listener != null) {
            listener.onFolderClick(currentFolder);
        }
    }

    @Override
    public int getItemCount() {
        if (folderItems == null) {
            return 0;
        } else {
            return folderItems.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (folderItems != null) {
            FileMessage fileMessage = folderItems.get(position).getFileMessage();
            if (fileMessage == null) {
                return TYPE_FOLDER;
            }
        }
        return TYPE_MUSIC;
    }

    private List<FolderItem> getFolderItems() {
        return folderItems;
    }

    /**
     * 返回上一层级
     */
    public void backToParentFolder() {
        //这里需要更新下上一个文件信息
        FolderItem folderItem = folderItemBackQ.pollLast();
        if (folderItem != null) {
            currentFolder.copy(folderItem);
        } else {
            initRootNote();
        }
        Log.d(TAG, "backToParentFolder: " + currentFolder.getParentNotePath());
        toNodeFolder(currentFolder.getNotePath());
    }

    /**
     * 直接退到根目录
     */
    public void backToRootFolder() {
        initRootNote();
        folderItemBackQ.clear();
        toNodeFolder(currentFolder.getNotePath());
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
        // TODO: 2022-12-16 需要隐藏的画，需要清除一些做法
        if (this.showSelectView) {
            selectedMap.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 获取当前歌曲列表
     *
     * @return List<FileMessage>
     */
    private List<FileMessage> getFileMessage() {
        List<FileMessage> fileMessages = new ArrayList<>();
        if (getFolderItems() != null) {
            for (FolderItem item : getFolderItems()) {
                if (item.getFileMessage() == null) {
                    continue;
                }
                fileMessages.add(item.getFileMessage());
            }
        }
        return fileMessages;
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
        if (getFolderItems() != null) {
            for (int i = 0; i < getFolderItems().size(); i++) {
                FolderItem folderItem = getFolderItems().get(i);
                if (folderItem.getFileMessage() != null && folderItem.getFileMessage().getId() == fileMessage.getId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 更改列表
     *
     * @param folderItems fileMessageList
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setFolderItems(List<FolderItem> folderItems) {
        Log.d(TAG, "setFileMessageList: " + folderItems);
        if (folderItems == null) {
            this.folderItems = new ArrayList<>();
        } else {
            this.folderItems = folderItems;
        }
        notifyDataSetChanged();
    }

    /**
     * 更改列表
     *
     * @param folderMap fileMessageList
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setFolders(Map<String, List<FolderItem>> folderMap) {
        Log.d(TAG, "setFileMessageList: " + folderMap.toString());
        this.folderMap = folderMap;
        //这里更新的是map，还需更新folderItems才行
        List<FolderItem> folderItems = folderMap.get(currentFolder.getNotePath());
        //看看map有无对应的
        if (folderItems == null) {
            //这里还需考量，要是不同USB，存在相同的路径节点咋整
            initRootNote();
            folderItemBackQ.clear();
            folderItems = new ArrayList<>();
        }
        Log.d(TAG, "setFolders: currentFolder = " + currentFolder);
        this.folderItems = folderItems;
        notifyDataSetChanged();
    }

    /**
     * 初始化构建根节点
     */
    private void initRootNote() {
        currentFolder.setParentNotePath("/");
        currentFolder.setNotePath("/");
        currentFolder.setNoteName("");
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
     * 复用条目信息
     */
    private static class FolderViewHolder extends RecyclerView.ViewHolder {
        private ImageView folderImage;
        private TextView folderName;
        private TextView folderItemSize;
        private ImageButton itemClick;
        private ImageView itemSelect;

        @SuppressLint("CutPasteId")
        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderImage = itemView.findViewById(R.id.iv_folder_icon);
            folderName = itemView.findViewById(R.id.tv_folder_name);
            folderItemSize = itemView.findViewById(R.id.tv_folder_item_size);
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
     * @return Map<String, List < FolderItem>>
     */
    public Map<String, List<FolderItem>> getFolderMap() {
        return folderMap;
    }

    /**
     * 提供当前的文件内容以供拷贝或者删除
     *
     * @return Map<Integer, FolderItem>
     */
    public Map<String, FileMessage> getSelectedMap() {
        return selectedMap;
    }

    /**
     * 全选
     */
    @SuppressLint("NotifyDataSetChanged")
    public void allSelect() {
        Log.d(TAG, "allSelect: ");
        if (folderItems != null) {
            for (FolderItem f : folderItems) {
                FileMessage fileMessage = f.getFileMessage();
                if (fileMessage == null) {
                    selectedMap.put(f.getNotePath(), null);
                } else {
                    selectedMap.put(fileMessage.getPath(), fileMessage);
                }
            }
            //更新布局
            notifyDataSetChanged();
            //通知界面更新数量
            if (listener != null) {
                listener.onSelectItemClick(-1, selectedMap.size(), null);
            }
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
        return selectedMap.size() == folderItems.size();
    }

    /**
     * 接口变化回调
     */
    public interface MusicListItemClickListener {

        /**
         * 当前条目发生变化
         *
         * @param fileMessages fileMessage
         */
        void onItemClick(int position, List<FileMessage> fileMessages);

        /**
         * 点击了文件夹
         */
        void onFolderClick(FolderItem folderItem);

        /**
         * 当条目为选项时触发得回调
         *
         * @param selectPosition selectPosition
         * @param selectSize     selectSize 总大小
         * @param folderItem     FolderItem
         */
        void onSelectItemClick(int selectPosition, int selectSize, FolderItem folderItem);
    }
}
