package com.desaysv.moduleusbpicture.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.desaysv.moduleusbpicture.adapter.PhotoPagerAdapter;
import com.desaysv.moduleusbpicture.listener.IPhotoControl;
import com.desaysv.moduleusbpicture.listener.PhotoInfoListener;
import com.desaysv.moduleusbpicture.photo.HackyViewPager;
import com.desaysv.moduleusbpicture.photo.photoview.PhotoView;
import com.desaysv.moduleusbpicture.utils.SharedPreferencesUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

import static com.desaysv.moduleusbpicture.photo.photoview.PhotoViewAttacher.DEFAULT_MID_SCALE;
import static com.desaysv.moduleusbpicture.photo.photoview.PhotoViewAttacher.DEFAULT_MIN_SCALE;


/**
 * Created by LZM on 2020-4-4
 * Comment 自定义的ControlViewPager，实现
 */
public class ControlViewPager extends HackyViewPager implements IPhotoControl {

    private static final String TAG = "ControlViewPager";


    //启动幻灯片播放
    private static final int START_SLIDE_SHOW = 1;

    //幻灯片播放的时间间隔
    public static final int TWO_SECONDS = 2000;
    public static final int FIVES_SECONDS = 5000;
    public static final int TEN_SECONDS = 10000;

    //当前是否是幻灯片播放
    private boolean isPlaying = false;


    private MyHandler mHandler;

    public ControlViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new MyHandler(this);
        addOnPageChangeListener(onPageChangeListener);

    }

    /**
     * 获取当前幻灯片的播放状态
     *
     * @return isPlaying true:播放 false：停止
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 设置当前幻灯片的播放状态
     *
     * @param playing true：播放 false：停止
     */
    public void setPlaying(boolean playing) {
        isPlaying = playing;
        if (mPhotoInfoListener != null) {
            mPhotoInfoListener.onPlayStatusChange(isPlaying);
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<ControlViewPager> weakReference;

        MyHandler(ControlViewPager controlViewPager) {
            weakReference = new WeakReference<>(controlViewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final ControlViewPager controlViewPager = weakReference.get();
            switch (msg.what) {
                case START_SLIDE_SHOW:
                    controlViewPager.slideShowAction();
                    break;
            }
        }
    }

    /**
     * 为了实现无限循环，所以viewPager的滑动事件，在列表的最前和最后就切回去，实现循环
     */
    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected: position = " + position);
            //滑动刷新之后，需要将图片重置
            resetPhoto();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.d(TAG, "onPageScrollStateChanged: state = " + state + " getCurrentItem = " + getCurrentItem());
            if (state == SCROLL_STATE_IDLE) {
                //如果滑动停止，列表为与
                if (getCurrentItem() == 0) {
                    int listCount = ((PhotoPagerAdapter) Objects.requireNonNull(getAdapter())).getListCount();
                    //如果是切换到第0为，则要打开列表的最后一位
                    openPhoto(listCount - 1);
                } else if (getCurrentItem() == ((PhotoPagerAdapter) Objects.requireNonNull(getAdapter())).getCount() - 1) {
                    //如果活动到列表里面的最后一位，则要打开列表里面的第0位
                    openPhoto(0);
                }
            }
        }
    };

    /**
     * 打开指定位置的图片
     *
     * @param position 指定的位置
     */
    @Override
    public void openPhoto(int position) {
        Log.d(TAG, "openPhoto: ");
        //由于要实现无线循环，所以在viewPager的前后加入了一个item，所以打开的时候，需要左移一位
        position++;
        setCurrentItem(position, false);
    }

    /**
     * 下一张图片
     */
    @Override
    public void nextPhoto() {
        Log.d(TAG, "nextPhoto: ");
        if (getAdapter() == null) {
            return;
        }
        int nextPosition = getCurrentItem() + 1;
        Log.d(TAG, "nextPhoto: nextPosition = " + nextPosition);
        setCurrentItem(nextPosition, true);
    }

    /**
     * 上一张图片
     */
    @Override
    public void prePhoto() {
        if (getAdapter() == null) {
            return;
        }
        int prePosition = getCurrentItem() - 1;
        Log.d(TAG, "prePhoto: prePosition = " + prePosition);
        setCurrentItem(prePosition, true);
    }

    /**
     * 旋转图片
     */
    @Override
    public void spinLeftPhoto() {
        Log.d(TAG, "spinLeftPhoto: ");
        if (getAdapter() == null) {
            return;
        }
        PhotoView photoView = ((PhotoPagerAdapter) getAdapter()).getCurrentPhotoView();
        Log.d(TAG, "spinLeftPhoto: photoView = " + photoView);
        if (photoView == null) {
            return;
        }
        photoView.setRotationBy(-90);
    }

    /**
     * 向右旋转图片
     */
    @Override
    public void spinRightPhoto() {
        Log.d(TAG, "spinRightPhoto: ");
        if (getAdapter() == null) {
            return;
        }
        PhotoView photoView = ((PhotoPagerAdapter) getAdapter()).getCurrentPhotoView();
        Log.d(TAG, "spinRightPhoto: photoView = " + photoView);
        if (photoView == null) {
            return;
        }
        photoView.setRotationBy(90);
    }

    /**
     * 放大图片
     */
    @Override
    public void bigPhoto() {
        Log.d(TAG, "bigPhoto: ");
        if (getAdapter() == null) {
            return;
        }
        PhotoView photoView = ((PhotoPagerAdapter) getAdapter()).getCurrentPhotoView();
        Log.d(TAG, "bigPhoto: photoView = " + photoView);
        if (photoView == null) {
            return;
        }
        float scale = photoView.getScale();
        Log.d(TAG, "bigPhoto: scale = " + scale);
        scale = (float) (scale + 0.25);
        if (scale > DEFAULT_MID_SCALE) {
            scale = DEFAULT_MID_SCALE;
        }
        photoView.setScale(scale, true);
    }

    /**
     * 缩小图片
     */
    @Override
    public void smallPhoto() {
        Log.d(TAG, "smallPhoto: ");
        if (getAdapter() == null) {
            return;
        }
        PhotoView photoView = ((PhotoPagerAdapter) getAdapter()).getCurrentPhotoView();
        Log.d(TAG, "smallPhoto: photoView = " + photoView);
        if (photoView == null) {
            return;
        }
        float scale = photoView.getScale();
        Log.d(TAG, "smallPhoto: scale = " + scale);
        scale = (float) (scale - 0.25);
        if (scale < DEFAULT_MIN_SCALE) {
            scale = DEFAULT_MIN_SCALE;
        }
        photoView.setScale(scale, true);
    }


    /**
     * 重置图片大小，将图片修改为默认大小
     */
    @Override
    public void resetPhoto() {
        Log.d(TAG, "resetPhoto: ");
        if (getAdapter() == null) {
            return;
        }
        PhotoView photoView = ((PhotoPagerAdapter) getAdapter()).getCurrentPhotoView();
        Log.d(TAG, "spinPhoto: photoView = " + photoView);
        if (photoView == null) {
            return;
        }
        photoView.resetMatrix();
    }


    /**
     * 开始幻灯片
     */
    @Override
    public void startSlideshow() {
        setPlaying(true);
        mHandler.removeMessages(START_SLIDE_SHOW);
        mHandler.sendEmptyMessageDelayed(START_SLIDE_SHOW, getSlidesTime());
    }

    /**
     * 停止幻灯片
     */
    @Override
    public void stopSlideshow() {
        setPlaying(false);
        mHandler.removeMessages(START_SLIDE_SHOW);
    }

    /**
     * 开始或者停止幻灯片
     */
    @Override
    public void startOrStopSlideshow() {
        if (isPlaying) {
            stopSlideshow();
        } else {
            startSlideshow();
        }
    }

    /**
     * 设置幻灯片的播放时间
     *
     * @param time 播放时间
     */
    @Override
    public void setSlidesTime(int time) {
        //保存当前设置的时间
        SharedPreferencesUtils.getInstance().saveSlidesTime(time);
        //重置handler的刷新时间
        mHandler.removeMessages(START_SLIDE_SHOW);
        mHandler.sendEmptyMessageDelayed(START_SLIDE_SHOW, getSlidesTime());
        //触发时间变化的回调
        if (mPhotoInfoListener != null){
            mPhotoInfoListener.onSlidesTimeChange(time);
        }
    }

    /**
     * 获取幻灯片的播放时间
     *
     * @return time 播放时间
     */
    @Override
    public int getSlidesTime() {
        return SharedPreferencesUtils.getInstance().getSlidesTime();
    }

    /**
     * 幻灯片播放的执行动作
     */
    private void slideShowAction() {
        nextPhoto();
        mHandler.removeMessages(START_SLIDE_SHOW);
        mHandler.sendEmptyMessageDelayed(START_SLIDE_SHOW, getSlidesTime());
    }

    private PhotoInfoListener mPhotoInfoListener;

    /**
     * 注册图片状态的变化回调
     *
     * @param photoInfoListener 回调
     */
    public void setPhotoInfoListener(PhotoInfoListener photoInfoListener) {
        mPhotoInfoListener = photoInfoListener;
    }

    /**
     * 注销图片状态的变化回调
     */
    public void removePhotoInfoListener() {
        mPhotoInfoListener = null;
    }
}
