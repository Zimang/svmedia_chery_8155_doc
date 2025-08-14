package com.desaysv.moduledab.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * created by ZNB on 2022-10-28
 * 点击切页不需要过场动画
 */
public class NoSmoothViewPager extends ViewPager {

    private boolean canSwipe = true;

    public NoSmoothViewPager(@NonNull Context context) {
        super(context);
    }

    public NoSmoothViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item,false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canSwipe && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canSwipe && super.onTouchEvent(ev);
    }

    public void setCanSwipe(boolean canSwipe) {
        this.canSwipe = canSwipe;
    }
}
