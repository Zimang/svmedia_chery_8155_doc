package com.desaysv.usbpicture.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.usbpicture.R;
import com.desaysv.usbpicture.bean.MessageBean;
import com.desaysv.usbpicture.photo.photoview.IPhotoViewActionListener;
import com.desaysv.usbpicture.photo.photoview.OnScaleChangedListener;
import com.desaysv.usbpicture.photo.photoview.PhotoView;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Created by LZM on 2020-4-4
 * Comment viewPager的适配器
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private static final String TAG = "PhotoPagerAdapter";

    private List<FileMessage> mPhotoList;

    private Context mContext;
    private IPhotoViewActionListener mListener;

    private int screenWidth;
    private int screenHeight;

    public PhotoPagerAdapter(Context context, IPhotoViewActionListener listener) {
        mContext = context;
        mListener = listener;
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


    private ViewPager viewPager;
    public void setPhotoList(ViewPager viewPager, List<FileMessage> photoList) {
        mPhotoList = photoList;
        this.viewPager = viewPager;
    }

    /**
     * 根据屏幕的实际大小，进行缩放处理
     * 否则全屏时无法做到全屏播放
     * @param width
     * @param height
     */
    public void updateScreenParams(int width, int height){
        screenWidth = width;
        screenHeight = height;
    }

    private boolean zoom = true;
    public void setZoomable(boolean zoomable){
        zoom = zoomable;
        Log.d(TAG,"setZoomable:"+zoomable);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem: position = " + position + " size = " + mPhotoList.size());
        View view = View.inflate(container.getContext(), R.layout.usb_picture_viewpager_item, null);
        FileMessage fileMessage = mPhotoList.get(position % mPhotoList.size());
        //为了实现列表的无限循环，所以加入了这个列表的变化逻辑

        final TextView tv_pic_name = view.findViewById(R.id.tv_pic_name);
        tv_pic_name.setText(fileMessage.getFileName());
//        if (isFullScreen){
//            tv_pic_name.setVisibility(View.GONE);
//        }else {
//            tv_pic_name.setVisibility(View.VISIBLE);
//        }

        final PhotoView photoView = view.findViewById(R.id.pv_photo_view);
        photoView.setZoomable(zoom);
        photoView.updateScreenParams(screenWidth,screenHeight);
        photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        photoView.setOnActionListener(mListener);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                EventBus.getDefault().post(new MessageBean(true));
                if (mListener != null){
                    mListener.onClick(true);
                }
            }
        });
        photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
//                EventBus.getDefault().post(new MessageBean(true));
                if (mListener != null){
                    mListener.onClick(true);
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                photoView.doubleTapPhoto();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return true;
            }
        });
        photoView.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                Log.d(TAG,"photoView.getScale()"+photoView.getScale());
                if (photoView.getScale()>photoView.getMinimumScale()){
//                    EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,false));
                    if (mListener != null){
                        mListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO,false);
                    }
                }else {
//                    EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_NARROW_PHOTO,true));
                    if (mListener != null){
                        mListener.onScaleState(MessageBean.CAN_NOT_NARROW_PHOTO,true);
                    }
                }
                if (photoView.getScale()<photoView.getMaximumScale()){
//                    EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,false));
                    if (mListener != null){
                        mListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO,false);
                    }
                }else {
//                    EventBus.getDefault().post(new MessageBean(MessageBean.CAN_NOT_ENLARGE_PHOTO,true));
                    if (mListener != null){
                        mListener.onScaleState(MessageBean.CAN_NOT_ENLARGE_PHOTO,true);
                    }
                }
            }
        });
        //显示视频的缩略图（Glide直接支持
        RequestOptions option;
        //只有小于3M才设置为原始尺寸，太大了会报错Canvas: trying to draw too large
        //这个阈值保守一点，预防万一
        if (fileMessage.getSize() < 2500000){
            Log.d(TAG,"size:"+fileMessage.getSize());
            option = new RequestOptions()
//                    .bitmapTransform(new RoundedCorners(6))
                    .centerInside()
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .format(DecodeFormat.PREFER_RGB_565)//设置为这种格式去掉透明度通道，可以减少内存占有;//关键代码，加载原始大小
                    .error(R.mipmap.icon_default_picture);
        }else {
            option = new RequestOptions()
//                    .bitmapTransform(new RoundedCorners(6))
                    .centerInside()
                    .error(R.mipmap.icon_default_picture);
        }
        Glide.with(mContext)
                .load(fileMessage.getPath()).apply(option)
                .into(photoView);

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
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private View mCurrentView;

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mCurrentView = (View) object;
        mListener.onPrimaryItemChanged(position % mPhotoList.size());
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
     * 获取当前显示的名字TextView
     *
     * @return 当前显示的名字TextView
     */
    @Nullable
    public TextView getCurrentTextView() {
        if (mCurrentView != null) {
            return (TextView) mCurrentView.findViewById(R.id.tv_pic_name);
        }
        return null;
    }


    /**
     * 获取当前显示的Mask
     * Mask 其实可以覆盖在 viewpager 上，只是 UI的效果看起来是在名字的下方，
     * 因此将 Mask 放到 Item 里面
     * @return 当前显示的Mask
     */
    @Nullable
    public RelativeLayout getCurrentMask() {
        if (mCurrentView != null) {
            return (RelativeLayout) mCurrentView.findViewById(R.id.rl_mask);
        }
        return null;
    }


    /**
     * 进入全屏时隐藏对应的名字，播放幻灯片时也隐藏
     * 用这种方式是为了规避 notifyDataSetChanged 出现的闪屏问题
     * 这个方式只对当前显示的做隐藏和显示，因为其它的此时处于不可见状态，因此无须理会
     * @param visible
     */
    public void updateNameVisibility(boolean visible){
//        Objects.requireNonNull(getCurrentTextView()).setVisibility(visible ? View.VISIBLE : View.GONE);
        Objects.requireNonNull(getCurrentMask()).setVisibility(visible ? View.GONE : View.GONE);
    }

    public void updateNameVisibilityWithoutMask(boolean visible){//T26名字不在这里处理
//        Objects.requireNonNull(getCurrentTextView()).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
