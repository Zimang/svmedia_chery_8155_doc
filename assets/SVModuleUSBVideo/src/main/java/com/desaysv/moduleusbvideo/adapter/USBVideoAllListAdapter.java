package com.desaysv.moduleusbvideo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.desaysv.moduleusbvideo.R;
import com.desaysv.moduleusbvideo.adapter.diffutil.VideoAllDiffCallBack;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.videoapp.glidev.GlideApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by extodc87 on 2022-11-2
 * Author: extodc87
 */
public class USBVideoAllListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "USBVideoAllListAdapter";

    private final Context mContext;
    private final List<FileMessage> mFileMessageList = new ArrayList<>();
    private final ItemClickListener itemClickListener;
    private String currentPath = null;

    private VideoAllDiffCallBack videoAllDiffCallBack;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;
    private boolean isFooterView = false;

    private boolean isRtl = false;

    public USBVideoAllListAdapter(Context context, List<FileMessage> fileMessageList, ItemClickListener itemClickListener) {
        mContext = context;
        mFileMessageList.addAll(fileMessageList);
        this.itemClickListener = itemClickListener;
        isRtl = Constant.isRtl();
        Log.i(TAG, "USBVideoAllListAdapter: isRtl: " + isRtl);
    }

    public void setFooterView(boolean footerView) {
        Log.d(TAG, "setFooterView() called with: footerView = [" + footerView + "], isFooterView = [" + isFooterView + "]");
        isFooterView = footerView;
    }

    public void updateData(List<FileMessage> fileMessageList) {
        if (null == videoAllDiffCallBack) {
            videoAllDiffCallBack = new VideoAllDiffCallBack();
        }
        videoAllDiffCallBack.setData(mFileMessageList, fileMessageList);
        //传入一个规则DiffUtil.Callback对象，和是否检测移动item的 boolean变量，得到DiffUtil.DiffResult 的对象
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(videoAllDiffCallBack, true);
        mFileMessageList.clear();
        mFileMessageList.addAll(fileMessageList);
        //分发更新到RecyclerView Adapter;利用DiffUtil.DiffResult对象的dispatchUpdatesTo（）方法，传入RecyclerView的Adapter，
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: viewType = " + viewType);
        if (TYPE_FOOTER == viewType) {
            View footView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_foot_item, parent, false);
            return new FootViewHolder(footView);
        }
        View mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(mView);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: file ");
                int position = holder.getAdapterPosition();
                FileMessage fileMessage = mFileMessageList.get(position);
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(fileMessage);
                }
            }
        });
        if (isRtl) {
            holder.tvVideoName.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) return;
        onBindViewHolderToFile(holder, position);
    }

    @Override
    public int getItemCount() {
        int base = this.mFileMessageList == null ? 0 : this.mFileMessageList.size();
        return isFooterView && base > 0 ? base + 1 : base;
//        return base;
    }

    @Override
    public int getItemViewType(int position) {
        return isFooterView && position == getItemCount() - 1 ? TYPE_FOOTER : TYPE_NORMAL;
//        return super.getItemViewType(position);
    }

    //变换底部占满 footview
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager g = (GridLayoutManager) manager;
            g.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return TYPE_FOOTER == getItemViewType(position) ? g.getSpanCount() : 1;
                }
            });
        }
    }

    //  解决StaggeredGridLayoutManager占满一行
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int index = holder.getLayoutPosition();
        if (index == 0) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    private void onBindViewHolderToFile(RecyclerView.ViewHolder rvHolder, final int position) {
        ViewHolder holder = (ViewHolder) rvHolder;
        FileMessage fileMessage = mFileMessageList.get(position);
        String videoName = fileMessage.getFileName();
        holder.tvVideoName.setText(videoName);
        final String path = fileMessage.getPath();

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.usb_video_media)
                .transform(new CenterCrop(), new RoundedCorners(10))
                .dontAnimate();
        if (!Constant.isT19CFlavor()) {
            requestOptions = requestOptions.placeholder(R.mipmap.usb_video_media);
        }
        GlideApp.with(mContext).setDefaultRequestOptions(requestOptions).load(fileMessage)
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .transform(new CenterCrop(), new RoundedCorners(10))
//                .placeholder(R.mipmap.usb_video_media)
//                .error(R.mipmap.usb_video_media)
//                .dontAnimate()
                .into(holder.ivThumbnail);

        if (path.equals(currentPath)) {
            // 当前播放歌曲
            Log.d(TAG, "onBindViewHolderToFile: position = " + position + " " + fileMessage.getFileName());
            holder.ivPlaying.setVisibility(View.VISIBLE);
        } else {
            holder.ivPlaying.setVisibility(View.GONE);
        }
    }

    /**
     * 当前播放路径
     */
    public void updateCurrentPlayPath(String path) {
        if (null != currentPath && currentPath.equals(path)) {
            Log.e(TAG, "updateCurrentPlayPath: the same");
            return;
        }
        int oldPosition = getFileMessagePosition(currentPath);
        int newPosition = getFileMessagePosition(path);
        Log.d(TAG, "updateCurrentPlayPath: oldPosition: " + oldPosition + " , newPosition: " + newPosition);
        if (NOT_IN_LIST != oldPosition) {
            notifyItemChanged(oldPosition);
        }
        if (NOT_IN_LIST != newPosition) {
            notifyItemChanged(newPosition);
        }
        this.currentPath = path;
    }

    public static final int NOT_IN_LIST = -1;

    /**
     * 获取文件在列表中所在的位置
     *
     * @param path 媒体文件的信息
     * @return 媒体文件所在的位置
     */
    public int getFileMessagePosition(String path) {
        int position = NOT_IN_LIST;
        if (null == mFileMessageList || null == path) {
            Log.e(TAG, "getFileMessagePosition: folderBeanList is null or path is null ");
            return position;
        }
        for (int i = 0; i < mFileMessageList.size(); i++) {
            FileMessage temp = mFileMessageList.get(i);
            if (temp.getPath().equals(path)) {
                position = i;
                break;
            }
        }
        Log.d(TAG, "getFileMessagePosition: position = " + position);
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivThumbnail;
        ImageView ivPlaying;
        TextView tvVideoName;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvVideoName = itemView.findViewById(R.id.tv_video_name);
            ivPlaying = itemView.findViewById(R.id.ivPlaying);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {
        View view;

        FootViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
    }

    /**
     * 列表item点击的时候，会触发的回调
     */
    public interface ItemClickListener {
        /**
         * 列表的点击事件
         */
        void onItemClick(FileMessage fileMessage);
    }

}
