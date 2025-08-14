package com.desaysv.moduleusbmusic.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * @author uidq1846
 * @desc 拖动配置的ViewPager
 * @time 2022-11-17 21:02
 */
public class ScrollConfigViewPager extends ViewPager {

    private boolean canScroll = false;

    public ScrollConfigViewPager(@NonNull Context context) {
        super(context);
    }

    public ScrollConfigViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean canScroll) {
        Log.i("ScrollConfigViewPager", "setCanScroll: canScroll = " + canScroll);
        //this.canScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canScroll && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setCurrentItem(int item) {
        if (canScroll) {
            super.setCurrentItem(item);
        } else {
            super.setCurrentItem(item, false);
        }
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, canScroll ? smoothScroll : false);
    }
}
