package com.desaysv.moduleusbpicture.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbpicture.R;


import java.util.List;

import com.desaysv.moduleusbpicture.ui.USB1PictureActivity;
import com.desaysv.moduleusbpicture.ui.USB2PictureActivity;
import com.desaysv.usbbaselib.bean.FileMessage;
import com.desaysv.usbbaselib.bean.USBConstants;


/**
 * Created by uidp5370 on 2019-5-15.
 * 图片列表的适配器
 */

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.PictureViewHolder> {

    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private View mView;
    private List<FileMessage> mList;
    private String mPath;


    public PictureListAdapter(Context context, List<FileMessage> list, String path) {
        mContext = context;
        mList = list;
        mPath = path;
    }


    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.usb_picture_list_item, parent, false);
        return new PictureViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(final PictureViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        FileMessage fileMessage = mList.get(position);
        holder.imThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Log.d(TAG, "onClick: position = " + position);
                switch (mPath) {
                    case USBConstants.USBPath.USB0_PATH:
                        USB1PictureActivity.startUSB1PictureActivity(AppBase.mContext, position);
                        break;
                    case USBConstants.USBPath.USB1_PATH:
                        USB2PictureActivity.startUSB2PictureActivity(AppBase.mContext, position);
                        break;
                }
            }
        });
        Glide.with(mContext).load(fileMessage.getPath()).into(holder.imThumbnail);

    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        } else {
            return 0;
        }
    }


    class PictureViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imThumbnail;

        PictureViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imThumbnail = itemView.findViewById(R.id.imThumbnail);
            imThumbnail.setFocusable(false);
        }
    }


}
