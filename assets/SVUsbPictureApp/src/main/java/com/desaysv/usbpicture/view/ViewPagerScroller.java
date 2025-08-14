package com.desaysv.usbpicture.view;

import android.content.Context;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

/**
 * Created by ZNB on 2022-06-14
 * 设置 ViewPager 的 setCurrentItem的动画时长
 */

public class ViewPagerScroller extends Scroller {

    private int scrollDuration = 1200;//滑动时长

    public ViewPagerScroller(Context context) {
        super(context);
    }

    public void setScrollDuration(int scrollDuration) {
        this.scrollDuration = scrollDuration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, scrollDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, scrollDuration);
    }

    public void intiViewPager(ViewPager viewPager){
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
