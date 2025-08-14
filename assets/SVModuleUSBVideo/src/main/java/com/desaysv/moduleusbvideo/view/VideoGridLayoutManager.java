package com.desaysv.moduleusbvideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Create by extodc87 on 2023-8-29
 * Author: extodc87
 * Android 已知BUG
 * https://developer.aliyun.com/article/663573
 */
public class VideoGridLayoutManager extends GridLayoutManager {
    private static final String TAG = "VideoGridLayoutManager";

    public VideoGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public VideoGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public VideoGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //这里加个异常处理 ，经测试可以避免闪退，具体副作用暂时还没有发现，可以作为临时解决方案
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            Log.e(TAG, "onLayoutChildren: ", e);
            e.printStackTrace();
        }
    }
}
