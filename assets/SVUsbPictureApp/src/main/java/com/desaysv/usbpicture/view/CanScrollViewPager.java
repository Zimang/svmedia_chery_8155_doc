package com.desaysv.usbpicture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import com.desaysv.usbpicture.adapter.PhotoPagerAdapter;
import com.desaysv.usbpicture.photo.photoview.PhotoView;


/**
 * Created by uidq0338 on 2017/7/5.
 * 是否支持滑动
 */

public class CanScrollViewPager extends ViewPager {
    private boolean scrollable;

    public CanScrollViewPager(Context context) {
        super(context);
        scrollable = false;
    }

    public CanScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        scrollable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !scrollable && super.onTouchEvent(ev);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (!scrollable) {
            try {
                return super.onInterceptTouchEvent(arg0);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;

    }


    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }


    public void resetPhoto(){
        PhotoView mPhotoView = ((PhotoPagerAdapter)getAdapter()).getCurrentPhotoView();
        if(mPhotoView == null){
            return;
        }
        mPhotoView.resetMatrix();
    }

}
