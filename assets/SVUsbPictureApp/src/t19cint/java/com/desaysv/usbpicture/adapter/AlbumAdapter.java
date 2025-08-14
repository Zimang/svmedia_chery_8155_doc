package com.desaysv.usbpicture.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.utils.ImageUtils;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "AlbumAdapter";

    private Context mContext;
    private CopyOnWriteArrayList<FileMessage> albumList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<FileMessage> selectList = new CopyOnWriteArrayList<>();
    private IClickListener mListener;
    private boolean selectMode = false;
    private static final int HOLDER_TYPE_ITEM = 0;
    private static final int HOLDER_TYPE_FOOTER = 1;

    public AlbumAdapter(Context mContext, IClickListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
    }

    public void updateList(CopyOnWriteArrayList<FileMessage> list){
        albumList.clear();
        albumList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HOLDER_TYPE_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_album, parent, false);
            return new AlbumViewHold(view);
        }else if (viewType == HOLDER_TYPE_FOOTER){
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_load_more, parent, false);
            return new FootViewHold(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AlbumViewHold) {
            AlbumViewHold albumViewHold = (AlbumViewHold) holder;
            FileMessage fileMessage = albumList.get(position);
            final String path = fileMessage.getPath();

            if (position == 0) {
                albumViewHold.ivAlbum.setContentDescription(mContext.getResources().getString(R.string.description_preview));
            }

            if (selectMode) {
                albumViewHold.ivSelect.setVisibility(View.VISIBLE);
                albumViewHold.ivSelect.setSelected(fileMessage.isSelected());
            } else {
                albumViewHold.ivSelect.setVisibility(View.GONE);
            }

            RequestOptions option = new RequestOptions()
                    .transforms(new CenterInside(), new RoundedCorners(ImageUtils.getCorner(mContext)))
                    .bitmapTransform(new RoundedCorners(ImageUtils.getCorner(mContext)))
                    .error(R.mipmap.icon_default_picture);
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(path)))
                    .apply(option)
                    .into(albumViewHold.ivAlbum);

            albumViewHold.ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectMode) {
                        Log.d(TAG, "selectMode onClick:" + position);
                        fileMessage.setSelected(!fileMessage.isSelected());
                        updateSelect(fileMessage);
                        notifyItemChanged(position);
                        if (mListener != null) {
                            mListener.onUpdateSelect();
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onClick(position);
                        }
                    }
                }
            });
        }else if (holder instanceof FootViewHold){
            FootViewHold footViewHold = (FootViewHold) holder;
            switch (loadState){
                case STATE_LOADING:
                    footViewHold.startAnimation(true);
                    footViewHold.rl_loadMore.setVisibility(View.VISIBLE);
                    break;
                case STATE_LOADED:
                default:
                    footViewHold.startAnimation(false);
                    footViewHold.rl_loadMore.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return albumList == null ? 0: albumList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()){
            return HOLDER_TYPE_FOOTER;
        }else {
            return HOLDER_TYPE_ITEM;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager){
            ((GridLayoutManager)recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == HOLDER_TYPE_FOOTER ? 5 : 1;
                }
            });
        }
    }

    public class AlbumViewHold extends RecyclerView.ViewHolder{
        private RelativeLayout rlAlbumItem;
        private ImageView ivAlbum;
        private ImageView ivSelect;
        public AlbumViewHold(@NonNull View itemView) {
            super(itemView);
            ivAlbum = itemView.findViewById(R.id.iv_album);
            ivSelect = itemView.findViewById(R.id.iv_select);
            rlAlbumItem = itemView.findViewById(R.id.rl_album_item);
        }
    }

    public static final int STATE_LOADING = 0;//加载中，未加载完成
    public static final int STATE_LOADED = 1;//加载完成
    private int loadState = STATE_LOADED;//数据加载状态，需要相册数据提供方确认交互方式之后，才可以进一步设计
    public void setLoadState(int state){
        Log.d(TAG,"setLoadState:"+state);
        loadState = state;
        notifyDataSetChanged();
    }

    public class FootViewHold extends RecyclerView.ViewHolder{
        private RelativeLayout rl_loadMore;
        private ImageView iv_loadMore;
        private ObjectAnimator loadingAnimator;
        public FootViewHold(@NonNull View itemView) {
            super(itemView);
            rl_loadMore = itemView.findViewById(R.id.rl_loadMore);
            iv_loadMore = itemView.findViewById(R.id.iv_loadMore);
            loadingAnimator = ObjectAnimator.ofFloat(iv_loadMore, "rotation", 0f, 360f);
            loadingAnimator.setDuration(1000);
            loadingAnimator.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            loadingAnimator.setRepeatCount(ObjectAnimator.INFINITE);//循环
            loadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }

        public void startAnimation(boolean animate){
            Log.d(TAG,"startAnimation:"+animate);
            if (animate){
                if (!loadingAnimator.isStarted()) {
                    loadingAnimator.start();
                }
            }else {
                if (loadingAnimator.isStarted()) {
                    loadingAnimator.end();
                }
            }
        }
    }


    public interface IClickListener{
        /**
         * 点击跳转到预览
         * @param position
         */
        void onClick(int position);

        /**
         * 选中状态
         */
        void onUpdateSelect();
    }

    /**
     * 返回编辑状态
     * @return
     */
    public boolean isSelectMode() {
        return selectMode;
    }

    /**
     * 进入/取消 编辑模式
     * @param selectMode
     */
    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        for (FileMessage fileMessage : selectList){//选中状态需要重置
            fileMessage.setSelected(false);
        }
        selectList.clear();
        notifyDataSetChanged();
    }

    /**
     * 重置选择模式
     */
    public void resetSelectMode(){
        selectMode = false;
    }


    /**
     * 更新单个的选中状态
     * @param fileMessage
     */
    private void updateSelect(FileMessage fileMessage){
        if (fileMessage.isSelected()){
            selectList.add(fileMessage);
        }else {
            selectList.remove(fileMessage);
        }
    }

    /**
     * 更新全选状态
     * @param selectAll
     */
    public void updateAllSelect(boolean selectAll){
        for (FileMessage fileMessage : albumList){
            fileMessage.setSelected(selectAll);
        }
        if (selectAll){
            selectList.clear();
            selectList.addAll(albumList);
        }else {
            selectList.clear();
        }
        notifyDataSetChanged();
    }


    /**
     * 获取选中状态的列表
     * @return
     */
    public CopyOnWriteArrayList<FileMessage> getSelectList(){
        return selectList;
    }
}
