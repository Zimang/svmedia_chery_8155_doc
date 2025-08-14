package com.desaysv.usbpicture.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.usbpicture.R;

/**
 * created by ZNB on 2022-08-13
 * 自定义的一个竖向RecycleView，主要用来处理竖向的滚动条偏移
 */
public class VerticalRecycleView extends RecyclerView {

    private float verticalOffset = 15;//top偏移值

    public VerticalRecycleView(@NonNull Context context) {
        super(context);
    }

    public VerticalRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalRecycleView);
        verticalOffset = typedArray.getDimension(R.styleable.VerticalRecycleView_verticalOffset,15);
        typedArray.recycle();
    }

    public VerticalRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalRecycleView);
        verticalOffset = typedArray.getDimension(R.styleable.VerticalRecycleView_verticalOffset,15);
        typedArray.recycle();
    }

    /**
     * 算法说明：
     * https://blog.csdn.net/qq_37196887/article/details/114584573
     * @return
     */
    @Override
    public int computeVerticalScrollOffset() {
        offset = super.computeVerticalScrollOffset();//已经滚动的偏移量(指的是RecycleView的滚动，不是滚动条的滚动)
        int extent = super.computeVerticalScrollExtent();//当前显示的内容高度
        int range = super.computeVerticalScrollRange();//全部内容的总高度，实际应该是RecycleView的(减去padding)高度

        return Math.round(offset - (float)offset * verticalOffset * range / ((range - extent) * extent)
                + (float) verticalOffset * range / extent);
    }
    private int offset;
    public int getVerticalScrollOffset(){
        return offset;
    }
}
