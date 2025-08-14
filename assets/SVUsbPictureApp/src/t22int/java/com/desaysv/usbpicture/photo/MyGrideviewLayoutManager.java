package com.desaysv.usbpicture.photo;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by uidq0338 on 2017/6/29.
 */

public class MyGrideviewLayoutManager extends GridLayoutManager {
    public MyGrideviewLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MyGrideviewLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public MyGrideviewLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
