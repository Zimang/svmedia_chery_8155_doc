package com.desaysv.usbpicture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class ScrollLayoutManager extends GridLayoutManager {
    private static final String TAG = "ScrollLayoutManager";

    private float speedArgs = 25f;//初始滚动速度因子

    /**
     * 动态改变这个速度因子，来达到动态跳转速度的目的
     * @param speedArgs
     */
    public void setSpeedArgs(float speedArgs) {
        this.speedArgs = speedArgs;
    }

    public ScrollLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ScrollLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        Log.d(TAG,"ScrollLayoutManager");
    }

    public ScrollLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }
    LinearSmoothScroller linearSmoothScroller;
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        Log.d(TAG,"smoothScrollToPosition");
        if (linearSmoothScroller == null) {
            linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            Log.d(TAG,"calculateSpeedPerPixel");
                            return speedArgs / displayMetrics.densityDpi;
                        }

                        @Override
                        protected int calculateTimeForDeceleration(int dx) {
                            Log.d(TAG,"calculateTimeForDeceleration: dx= "+dx);
                            return super.calculateTimeForDeceleration(dx);
                        }

                        @Override
                        protected int calculateTimeForScrolling(int dx) {
                            Log.d(TAG,"calculateTimeForScrolling: dx= "+ dx);
                            return super.calculateTimeForScrolling(dx);
                        }

                    };
        }
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }


}
