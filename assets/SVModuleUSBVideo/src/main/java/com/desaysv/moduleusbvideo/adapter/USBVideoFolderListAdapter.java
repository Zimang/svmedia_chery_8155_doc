package com.desaysv.moduleusbvideo.adapter;

import android.content.Context;
import android.text.TextUtils;
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
import com.desaysv.moduleusbvideo.adapter.diffutil.VideoFolderDiffCallBack;
import com.desaysv.moduleusbvideo.bean.FolderBean;
import com.desaysv.moduleusbvideo.businesslogic.listsearch.USBVideoFolderListData;
import com.desaysv.moduleusbvideo.util.Constant;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.videoapp.glidev.GlideApp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Create by extodc87 on 2022-11-2
 * Author: extodc87
 */
public class USBVideoFolderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "USBVideoFolderListAdapter";

    private final Context mContext;
    private final List<FolderBean> folderBeanList = new ArrayList<>();
    //进行加锁处理
    private final ReentrantReadWriteLock mReentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock readLock = mReentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = mReentrantLock.writeLock();

    private final FolderItemClickListener folderItemClickListener;

    private String folderPath = "";
    private VideoFolderDiffCallBack videoFolderDiffCallBack;

    public static final int NOT_IN_LIST = -1;
    private String currentPath = null;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;
    private boolean isFooterView = false;

    private boolean isRtl = false;

    public USBVideoFolderListAdapter(Context context, FolderItemClickListener folderItemClickListener) {
        mContext = context;
        this.folderItemClickListener = folderItemClickListener;
        if (null == videoFolderDiffCallBack) {
            videoFolderDiffCallBack = new VideoFolderDiffCallBack();
        }
        isRtl = Constant.isRtl();
        Log.i(TAG, "USBVideoAllListAdapter: isRtl: " + isRtl);
    }

    public void setFooterView(boolean footerView) {
        Log.d(TAG, "setFooterView() called with: footerView = [" + footerView + "], isFooterView = [" + isFooterView + "]");
        isFooterView = footerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        if (TYPE_FOOTER == viewType) {
            View footView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_foot_item, parent, false);
            return new FootViewHolder(footView);
        }
        View mView = LayoutInflater.from(mContext).inflate(R.layout.usb_video_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(mView);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderBean folderBean;
                readLock.lock();
                try {
                    int position = holder.getAdapterPosition();
                    Log.i(TAG, "onClick: position: " + position);
                    folderBean = folderBeanList.get(position);
                } finally {
                    readLock.unlock();
                }
                Log.i(TAG, "onClick: folderBean: " + folderBean);
                if (null == folderBean) {
                    Log.e(TAG, "onClick: folderBean is null");
                    return;
                }
                if (folderBean.isFolder()) {
                    //如果点击的文件夹的话
                    if (folderItemClickListener != null) {
                        folderItemClickListener.onFolderClick(folderBean.getFolderPath());
                    }
                } else {
                    if (folderItemClickListener != null) {
                        folderItemClickListener.onItemClick(folderBean.getVideo());
                    }
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
        int base = this.folderBeanList == null ? 0 : this.folderBeanList.size();
        return isFooterView && base > 0 ? base + 1 : base;
    }

    @Override
    public int getItemViewType(int position) {
        return isFooterView && position == getItemCount() - 1 ? TYPE_FOOTER : TYPE_NORMAL;
    }

    private void onBindViewHolderToFile(RecyclerView.ViewHolder rvHolder, final int position) {
        ViewHolder holder = (ViewHolder) rvHolder;
        FolderBean folderBean;
        readLock.lock();
        try {
            folderBean = folderBeanList.get(position);
        } finally {
            readLock.unlock();
        }
        if (null == folderBean) {
            Log.e(TAG, "onBindViewHolderToFile: position: " + position + " , folderBean is null ");
            return;
        }
        holder.ivPlaying.setVisibility(View.GONE);
        if (folderBean.isFolder()) {
            GlideApp.with(mContext).load("")
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transform(new CenterCrop(), new RoundedCorners(10))
                    .placeholder(R.mipmap.usb_video_folder)
                    .error(R.mipmap.usb_video_folder)
                    .dontAnimate()
                    .into(holder.ivThumbnail);
            holder.tvVideoName.setText(folderBean.getFolderTitle());
        } else {
            FileMessage fileMessage = folderBean.getVideo();
            String videoName = fileMessage.getFileName();
            final String path = fileMessage.getPath();
            holder.tvVideoName.setText(videoName);

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.mipmap.usb_video_media)
                    .transform(new CenterCrop(), new RoundedCorners(10));
            if (!Constant.isT19CFlavor()) {
                requestOptions = requestOptions.placeholder(R.mipmap.usb_video_media);
            }

            GlideApp.with(mContext).setDefaultRequestOptions(requestOptions).load(fileMessage)
//                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                    .transform(new CenterCrop(), new RoundedCorners(10))
//                    .placeholder(R.mipmap.usb_video_media)
//                    .error(R.mipmap.usb_video_media)
                    .into(holder.ivThumbnail);


            if (path.equals(currentPath)) {
                // 当前播放歌曲
                Log.d(TAG, "onBindViewHolderToFile: position = " + position + " " + fileMessage.getFileName());
                holder.ivPlaying.setVisibility(View.VISIBLE);
            }
        }
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

    /**
     * 设置文件夹列表
     *
     * @param path 文件夹路径
     */
    public void setFolderBeanListPath(String path) {
        Log.d(TAG, "setFolderBeanListPath：folderPath = " + folderPath + " path = " + path);
        if (null == path || TextUtils.isEmpty(path)) {
            Log.e(TAG, "setFolderBeanListPath: path is empty");
            return;
        }
        writeLock.lock();
        DiffUtil.DiffResult diffResult = null;
        try {
            //设置的路径不一样，才需要从新获取数据
            List<FolderBean> folderBeanList = folderItemClickListener.getFolderBeanList(path);
            videoFolderDiffCallBack.setData(this.folderBeanList, folderBeanList);
            //传入一个规则DiffUtil.Callback对象，和是否检测移动item的 boolean变量，得到DiffUtil.DiffResult 的对象
            diffResult = DiffUtil.calculateDiff(videoFolderDiffCallBack, true);
            this.folderBeanList.clear();
            this.folderBeanList.addAll(folderBeanList);
            folderPath = path;
            folderItemClickListener.onFolderPathChange(folderPath);
            Log.i(TAG, "setFolderBeanListPath: folderBeanList = " + this.folderBeanList.size());
        } finally {
            if (null != diffResult) {
                //分发更新到RecyclerView Adapter;利用DiffUtil.DiffResult对象的dispatchUpdatesTo（）方法，传入RecyclerView的Adapter，
                diffResult.dispatchUpdatesTo(this);
            }
            writeLock.unlock();
        }
    }

    /**
     * 获取当前显示的文件夹路径
     *
     * @return folderPath
     */
    public String getFolderPath() {
        return folderPath;
    }

    /**
     * 回到上一个文件夹
     *
     * @return true:root; false: no root
     */
    public boolean moveToParentFolder() {
        String str = USBVideoFolderListData.getInstance().getParentPath(folderPath);
        Log.d(TAG, "moveToParentFolder: str = " + str);
        int index = str.lastIndexOf("/");
        if (index < 1) {
            Log.w(TAG, "moveToParentFolder: folderPath is root");
            return true;
        }
        //如果不是根路径，那就设置路径
        setFolderBeanListPath(str);
        return false;
    }

    /**
     * 获取文件在列表中所在的位置
     *
     * @param path 媒体文件的信息
     * @return 媒体文件所在的位置
     */
    public int getFileMessagePosition(String path) {
        int position = NOT_IN_LIST;
        if (null == folderBeanList || null == path) {
            Log.e(TAG, "getFileMessagePosition: folderBeanList is null or path is null ");
            return position;
        }
        for (int i = 0; i < folderBeanList.size(); i++) {
            FolderBean folderBean = folderBeanList.get(i);
            if (!folderBean.isFolder()) {
                if (folderBean.getVideo().getPath().equals(path)) {
                    position = i;
                    break;
                }
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
    public interface FolderItemClickListener {

        /**
         * adapter的路径发送改变的时候，会触发的回调
         *
         * @param folderPath 文件夹路径
         */
        void onFolderPathChange(String folderPath);

        /**
         * 从界面那里获取文件夹列表
         *
         * @param path 文件夹列表的路径
         * @return 文件夹列表
         */
        List<FolderBean> getFolderBeanList(String path);

        /**
         * 文件夹里面的文件被点击了
         *
         * @param path 路径
         */
        void onFolderClick(String path);

        /**
         * 列表的点击事件
         */
        void onItemClick(FileMessage fileMessage);
    }
}
