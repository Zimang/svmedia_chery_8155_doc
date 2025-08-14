package com.desaysv.moduleusbpicture.adapter;

import org.jetbrains.annotations.Nullable;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.desaysv.moduleusbpicture.R;
import com.desaysv.moduleusbpicture.photo.photoview.PhotoView;
import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * Created by LZM on 2020-4-4
 * Comment viewPager的适配器
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private static final String TAG = "PhotoPagerAdapter";

    private List<FileMessage> mPhotoList;

    private Context mContext;

    public PhotoPagerAdapter(Context context) {
        mContext = context;
    }

    /**
     * 设置当前的播放列表
     *
     * @param listType  列表的类型，只用来打印log
     * @param photoList 需要播放的列表
     */
    public void setPhotoList(String listType, List<FileMessage> photoList) {
        Log.d(TAG, "setPhotoList: listType = " + listType);
        mPhotoList = photoList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem: position = " + position + " size = " + mPhotoList.size());
        View view = View.inflate(container.getContext(), R.layout.usb_picture_viewpager_item, null);
        FileMessage fileMessage;
        /*//为了实现列表的无限循环，所以加入了这个列表的变化逻辑吧
        if (position == 0) {
            //如果是第一个位置，那就那列表里面的最后一个数据
            fileMessage = mPhotoList.get(mPhotoList.size() - 1);
        } else if (position == getCount() - 1) {
            //如果是pager里面的最后一个位置，就要去列表里面的第一个数据
            fileMessage = mPhotoList.get(0);
        } else {
            fileMessage = mPhotoList.get(position - 1);
        }*/
        fileMessage = mPhotoList.get(position);
        PhotoView photoView = view.findViewById(R.id.pv_photo_view);
        Glide.with(mContext).load(fileMessage.getPath()).into(photoView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Log.d(TAG, "destroyItem: ");
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        //为了实现轮播功能，所以在列表的前后多加了一个item，实现切换循环
        return mPhotoList.size() + 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private View mCurrentView;

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mCurrentView = (View) object;
    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        //add by lzm 加入这个标志位是为了让adapter数据变化的时候，能够重新加载界面
        return POSITION_NONE;
    }

    /**
     * 获取当前显示的PhotoView
     *
     * @return 当前显示的PhotoView
     */
    @Nullable
    public PhotoView getCurrentPhotoView() {
        if (mCurrentView != null) {
            return (PhotoView) mCurrentView.findViewById(R.id.pv_photo_view);
        }
        return null;
    }

    /**
     * 由于需要做无限循环，所以需要加多一个获取当前列表数量的逻辑
     *
     * @return 列表的个数
     */
    public int getListCount() {
        return mPhotoList == null ? 0 : mPhotoList.size();
    }

}
