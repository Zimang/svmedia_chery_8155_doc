package com.desaysv.usbpicture.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.manager.PictureListManager;
import com.desaysv.svlibtoast.ToastUtil;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.constant.Constant;
import com.desaysv.usbpicture.constant.Point;
import com.desaysv.usbpicture.trigger.PointTrigger;
import com.desaysv.usbpicture.utils.ImageUtils;
import com.desaysv.usbpicture.utils.ProductUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class USB1PictureFolderAdapter extends RecyclerView.Adapter<USB1PictureFolderAdapter.USBViewHold> {

    private static final String TAG = "USB1PictureFolderAdapter";
    private List<FileMessage> pictureFileMessageList;
    private Context mContext;
    private List<FolderMessage> mFileFolders;
    private List<FolderMessage> mEmptyFolders;
    private int folderNums;
    private int emptyFolderNums;
    private String currentFolderPath;
    private String currentFolderParentPath;
    private int styleType = 0;// default is all picture
    private int usbType = Constant.USBType.TYPE_USB1; //default is USB1
    private IGotoPreviewListener mListener;
    // 在查询过程中，不能点击跳转到预览界面，否则会导致数据对不上，出现数组越界的问题
    private boolean isClickFolder = false;// Added by ZNB for ALM-9126 on 2022-03-14

    public static final int FLAG_FOLDER = 0;
    public static final int FLAG_EMPTY_FOLDER = 1;

    private boolean isRightRudder = ProductUtils.isRightRudder();


    private List<Integer> backList = new ArrayList<>();//保存当前点击文件夹的位置，进入第几级，就有多少个

    //获取最新的位置
    public int getCurrentClickFolder() {
        if (backList.size() > 0){
            return backList.get(backList.size() - 1);//最后一个就是最新的那个
        }else {
            return -1;
        }
    }

    //移除已经返回的位置
    public void removeBackListTop() {
        if (backList.size() > 0) {
            backList.remove(backList.size() - 1);
        }
    }

    public USB1PictureFolderAdapter(Context context, int usbType, IGotoPreviewListener listener) {
        mContext = context;
        usbType = usbType;
        mListener = listener;
    }

    public void updatePictureListFileMessage(List<FileMessage> list, List<FolderMessage> folders, List<FolderMessage> emptyFolders) {
        pictureFileMessageList = list;
        mFileFolders = folders;
        mEmptyFolders = emptyFolders;
        isClickFolder = false;// Added by ZNB for ALM-9126 on 2022-03-14
        notifyDataSetChanged();
    }

    public void updateStyleType(int styleType){
        this.styleType = styleType;
    }

    public String getCurrentFolderParentPath(){
//        if ((mFileFolders == null || mFileFolders.size() == 0)) {//当前没有文件夹
//            Log.d(TAG,"getCurrentFolderParentPath000 currentFolderParentPath: "+currentFolderParentPath);
//            return currentFolderParentPath;
//        }
        //当前路径目录的 parent
        String queryParent = currentFolderPath.substring(0,currentFolderPath.lastIndexOf("/"));
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
        View view = LayoutInflater.from(mContext).inflate(isRightRudder ? R.layout.layout_picitem_right : R.layout.layout_picitem, parent, false);
        return new USBViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull USBViewHold holder, final int position) {
        if (position < folderNums) {//文件夹显示
            onBindViewHolderFolder(holder, position);

        }else if(position < folderNums + emptyFolderNums){
            onBindViewHolderEmptyFolder(holder, position);
        } else if (position < getItemCount()) {//文件显示
            onBindViewHolderFile(holder, position);
        }
    }

    private void onBindViewHolderFolder(USBViewHold holder, int position) {
        final FolderMessage folder = mFileFolders.get(position);
        Log.d(TAG,"文件夹名："+folder.getName());
        currentFolderParentPath = folder.getParentPath();
        Log.d(TAG,"queryParent："+currentFolderParentPath);
        holder.rl_within.setVisibility(View.GONE);
        holder.ll_emptyFolder.setVisibility(View.GONE);
        holder.picture_list_item_folder_ll.setVisibility(View.VISIBLE);
        holder.picture_list_item_folder_ll.setContentDescription(folder.getName());
        holder.picture_list_item_folder_tv_name.setText(folder.getName());
        holder.picture_list_item_folder_tv_count.setText(String.format(mContext.getResources().getString(R.string.item_count),folder.getCount()));
        holder.picture_list_item_folder_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (mListener != null){
                        mListener.clickFolder(folder.getPath(),FLAG_FOLDER);
                        backList.add(position);
                    }
            }
        });
    }

    /**
     * 对应目录的空文件夹列表
     * 奇瑞的奇葩设计
     * @param holder
     * @param position
     */
    private void onBindViewHolderEmptyFolder(USBViewHold holder, int position) {
        final FolderMessage folder = mEmptyFolders.get(position - folderNums);
        Log.d(TAG,"空文件夹名："+folder.getName());
        holder.rl_within.setVisibility(View.GONE);
        holder.picture_list_item_folder_ll.setVisibility(View.GONE);
        currentFolderParentPath = folder.getParentPath();
        holder.ll_emptyFolder.setVisibility(View.VISIBLE);
        holder.ll_emptyFolder.setContentDescription(folder.getName());
        holder.mt_emptyFolderName.setText(folder.getName());
        holder.ll_emptyFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.clickFolder(folder.getPath(),FLAG_EMPTY_FOLDER);
                    backList.add(position);
                }
            }
        });
    }




    private void onBindViewHolderFile(USBViewHold holder, final int position) {
        FileMessage fm = pictureFileMessageList.get(position - folderNums - emptyFolderNums);
        final String path = fm.getPath();
        holder.picture_list_item_folder_ll.setVisibility(View.GONE);
        holder.ll_emptyFolder.setVisibility(View.GONE);
        holder.rl_within.setVisibility(View.VISIBLE);
        currentFolderParentPath = path.substring(0,path.lastIndexOf("/"));
        if (position - folderNums - emptyFolderNums == 0){
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
                        PictureListManager.getInstance().setCurrentBadList(fm);
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
                        mListener.gotoPreView(position - folderNums - emptyFolderNums);
                        //埋点：查看图片
                        PointTrigger.getInstance().trackEvent(Point.KeyName.PictureOperate,Point.Field.PicOperType,Point.FieldValue.PREVIEW
                                ,Point.Field.PictureName,fm.getFileName());
                    }else {
//                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.bad_item));
                        mListener.clickBadItem();
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
        emptyFolderNums = mEmptyFolders != null ? mEmptyFolders.size() : 0;
        return size + folderNums + emptyFolderNums;
    }

    public class USBViewHold extends RecyclerView.ViewHolder {

        private LinearLayout picture_list_item_folder_ll;
        private TextView picture_list_item_folder_tv_name;
        private TextView picture_list_item_folder_tv_count;
        private ImageView picture_list_item_file_iv;
        private RelativeLayout rl_within;

        private LinearLayout ll_emptyFolder;
        private TextView mt_emptyFolderName;

        public USBViewHold(View itemView) {
            super(itemView);
            picture_list_item_folder_ll = itemView.findViewById(R.id.picture_list_item_folder_ll);
            picture_list_item_folder_tv_name = itemView.findViewById(R.id.picture_list_item_folder_tv_name);
            picture_list_item_folder_tv_count = itemView.findViewById(R.id.picture_list_item_folder_tv_count);
            picture_list_item_file_iv = itemView.findViewById(R.id.picture_list_item_file_iv);
            rl_within = itemView.findViewById(R.id.rl_within);

            ll_emptyFolder = itemView.findViewById(R.id.ll_emptyFolder);
            mt_emptyFolderName = itemView.findViewById(R.id.mt_emptyFolderName);
        }
    }

    public interface IGotoPreviewListener{
        void gotoPreView(int position);

        void clickFolder(String folderPath, int flag);

        void clickBadItem();
    }
}
