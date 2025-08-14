package com.desaysv.usbpicture.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.desaysv.querypicture.QueryManager;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.bean.MessageBean;
import com.desaysv.usbpicture.constant.Constant;
import com.desaysv.usbpicture.fragment.BaseUSBPictureListFragment;
import com.desaysv.usbpicture.ui.BasePictureActivity;
import com.desaysv.usbpicture.utils.ImageUtils;

//import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import com.desaysv.usbpicture.constant.Point;
import com.desaysv.usbpicture.trigger.PointTrigger;

public class USB1PictureFolderAdapter extends RecyclerView.Adapter<USB1PictureFolderAdapter.USBViewHold> {

    private static final String TAG = "USB1PictureFolderAdapter";
    private List<FileMessage> pictureFileMessageList;
    private Context mContext;
    private List<FolderMessage> mFileFolders;
    private int folderNums;
    private String currentFolderPath;
    private String currentFolderParentPath;
    private int styleType = 0;// default is all picture
    private int usbType = Constant.USBType.TYPE_USB1; //default is USB1
    private IGotoPreviewListener mListener;
    // 在查询过程中，不能点击跳转到预览界面，否则会导致数据对不上，出现数组越界的问题
    private boolean isClickFolder = false;// Added by ZNB for ALM-9126 on 2022-03-14

    public USB1PictureFolderAdapter(Context context, int usbType, IGotoPreviewListener listener) {
        mContext = context;
        usbType = usbType;
        mListener = listener;
    }

    public void updatePictureListFileMessage(List<FileMessage> list, List<FolderMessage> folders) {
        pictureFileMessageList = list;
        mFileFolders = folders;
        isClickFolder = false;// Added by ZNB for ALM-9126 on 2022-03-14
        notifyDataSetChanged();
    }

    public void updateStyleType(int styleType){
        this.styleType = styleType;
    }

    public String getCurrentFolderParentPath(){
        if (mFileFolders == null || mFileFolders.size() == 0) {//当前没有文件夹
            Log.d(TAG,"getCurrentFolderParentPath000 currentFolderParentPath: "+currentFolderParentPath);
            return currentFolderParentPath;
        }
        //当前路径目录的 parent
        String queryParent = currentFolderParentPath.substring(0,currentFolderParentPath.lastIndexOf("/"));
        Log.d(TAG,"getCurrentFolderParentPath111 currentFolderParentPath: "+queryParent);
        return queryParent;
    }

    public String getCurrentFolderPath() {
        Log.d(TAG,"getCurrentFolderPath: "+currentFolderPath);
        return currentFolderPath;
    }

    public void setCurrentFolderPath(String parent) {
        currentFolderPath = parent;
    }

    @NonNull
    @Override
    public USBViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_picitem, parent, false);
        return new USBViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull USBViewHold holder, final int position) {
        if (position < folderNums) {//文件夹显示
            onBindViewHolderFolder(holder, position);

        } else if (position < getItemCount()) {//文件显示
            onBindViewHolderFile(holder, position);
        }
    }

    private void onBindViewHolderFolder(USBViewHold holder, int position) {
        final FolderMessage folder = mFileFolders.get(position);
        Log.d(TAG,"文件夹名："+folder.getName());
        currentFolderParentPath = folder.getParentPath();
        Log.d(TAG,"queryParent："+currentFolderParentPath);
        holder.picture_list_item_file_iv.setVisibility(View.GONE);
        holder.picture_list_item_folder_ll.setVisibility(View.VISIBLE);
        holder.picture_list_item_folder_ll.setContentDescription(folder.getName());
        holder.picture_list_item_folder_tv_name.setText(folder.getName());
        holder.picture_list_item_folder_tv_count.setText(String.format(mContext.getResources().getString(R.string.item_count),folder.getCount()));
        holder.picture_list_item_folder_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (isClickFolder) {
//                    setCurrentFolderPath(folder.getPath());
//                    Log.d(TAG, "currentFolderPath:" + currentFolderPath);
//                    isClickFolder = true;
//                    QueryManager.getInstance().startQueryUSB1PictureWithDir(currentFolderPath);
//                }
                    if (mListener != null){
                        mListener.clickFolder(folder.getPath());
                    }
            }
        });
    }

    private void onBindViewHolderFile(USBViewHold holder, final int position) {
        FileMessage fm = pictureFileMessageList.get(position - folderNums);
        final String path = fm.getPath();
        holder.picture_list_item_folder_ll.setVisibility(View.GONE);
        holder.picture_list_item_file_iv.setVisibility(View.VISIBLE);

        if (position - folderNums == 0){
            holder.picture_list_item_file_iv.setContentDescription(mContext.getResources().getString(R.string.description_preview));
        }

        //显示视频的缩略图（Glide直接支持
        RequestOptions option = new RequestOptions()
                .transforms(new CenterInside(),new RoundedCorners(ImageUtils.getCorner(mContext)))
                .bitmapTransform(new RoundedCorners(ImageUtils.getCorner(mContext)))
                .error(R.mipmap.icon_default_picture);
        Glide.with(mContext)
                .asBitmap()
                .load(Uri.fromFile(new File(path)))
                .apply(option)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Log.d(TAG,"onLoadFailed: " + e);
                        fm.setIsSupport(-1);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(holder.picture_list_item_file_iv);
        holder.picture_list_item_file_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Modified by ZNB for ALM-9126 on 2022-03-14 begin
                if (mListener != null){
                    if (fm.getIsSupport() != -1) {
                        mListener.gotoPreView(position - folderNums);
                        //埋点：查看图片
                        PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.PREVIEW
                                ,Point.Field.PictureName,fm.getFileName());
                    }else {
                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.bad_item));
                    }
                }
                // Modified by ZNB for ALM-9126 on 2022-03-14 end
            }
        });
    }

    @Override
    public int getItemCount() {
        int size = pictureFileMessageList != null ? pictureFileMessageList.size() : 0;
        folderNums = mFileFolders != null ? mFileFolders.size() : 0;
        return size + folderNums;
    }

    public class USBViewHold extends RecyclerView.ViewHolder {

        private LinearLayout picture_list_item_folder_ll;
        private TextView picture_list_item_folder_tv_name;
        private TextView picture_list_item_folder_tv_count;
        private ImageView picture_list_item_file_iv;

        public USBViewHold(View itemView) {
            super(itemView);
            picture_list_item_folder_ll = itemView.findViewById(R.id.picture_list_item_folder_ll);
            picture_list_item_folder_tv_name = itemView.findViewById(R.id.picture_list_item_folder_tv_name);
            picture_list_item_folder_tv_count = itemView.findViewById(R.id.picture_list_item_folder_tv_count);
            picture_list_item_file_iv = itemView.findViewById(R.id.picture_list_item_file_iv);
        }
    }

    public interface IGotoPreviewListener{
        void gotoPreView(int position);

        void clickFolder(String folderPath);
    }
}
