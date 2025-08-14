package com.desaysv.usbpicture.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
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

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHold>{
    private static final String TAG = "AlbumAdapter";

    private Context mContext;
    private CopyOnWriteArrayList<FileMessage> albumList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<FileMessage> selectList = new CopyOnWriteArrayList<>();
    private IClickListener mListener;
    private boolean selectMode = false;

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
    public AlbumViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_album, parent, false);
        return new AlbumViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHold holder, int position) {
        FileMessage fileMessage = albumList.get(position);
        final String path = fileMessage.getPath();

        if (position == 0) {
            holder.ivAlbum.setContentDescription(mContext.getResources().getString(R.string.description_preview));
        }

        if (selectMode){
            holder.ivSelect.setVisibility(View.VISIBLE);
            holder.ivSelect.setSelected(fileMessage.isSelected());
        }else {
            holder.ivSelect.setVisibility(View.GONE);
        }

        RequestOptions option = new RequestOptions()
                .transforms(new CenterInside(), new RoundedCorners(ImageUtils.getCorner(mContext)))
                .bitmapTransform(new RoundedCorners(ImageUtils.getCorner(mContext)))
                .error(R.mipmap.icon_default_picture);
        Glide.with(mContext)
                .load(Uri.fromFile(new File(path)))
                .apply(option)
                .into(holder.ivAlbum);

        holder.ivAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectMode){
                    Log.d(TAG,"selectMode onClick:"+position);
                    fileMessage.setSelected(!fileMessage.isSelected());
                    updateSelect(fileMessage);
                    notifyItemChanged(position);
                    if (mListener != null) {
                        mListener.onUpdateSelect();
                    }
                }else {
                    if (mListener != null) {
                        mListener.onClick(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList == null ? 0: albumList.size();
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
