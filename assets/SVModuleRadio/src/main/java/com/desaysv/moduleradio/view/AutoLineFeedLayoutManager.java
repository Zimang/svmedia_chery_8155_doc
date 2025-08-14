package com.desaysv.moduleradio.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.desaysv.moduleradio.R;

public class AutoLineFeedLayoutManager extends RecyclerView.LayoutManager {

    private int totalHeight = 0;

    private int itemHeight = 0;
    private int verticalScrollOffset = 0;
    private static final int SPACING_LEFTRIGHT = 71;//左右间距
    private static final int SPACING_TOPBOTTOM = 50;//上下间距
    private Context mContext;
    private boolean isRTLView = false;

    public AutoLineFeedLayoutManager(Context context) {
        setAutoMeasureEnabled(true);//layoutmanager必须调用此方法设为true才能在onMesure时自动布局
        mContext = context;
        isRTLView = isRtlView();
    }

    protected boolean isRtlView() {
        Resources resources = mContext.getResources();
        Configuration configuration = resources.getConfiguration();
        if (configuration != null) {
            return configuration.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //实际要滑动的距离
        int travel = dy;

        Log.d("znbtest", "znbtest h: " + (verticalScrollOffset + dy) + ",totalHeight: " + totalHeight);
        //如果滑动到最顶部
        if (verticalScrollOffset + dy < 0) {
            travel = -verticalScrollOffset;
        } else if (verticalScrollOffset + dy > totalHeight - getVerticalSpace()) {//如果滑动到最底部
            travel = totalHeight - getVerticalSpace() - verticalScrollOffset;
        }

        //将竖直方向的偏移量+travel
        verticalScrollOffset += travel;

        // 平移容器内的item
        offsetChildrenVertical(-travel);
        return travel;
    }

    /**
     * 获取RecyclerView在垂直方向上的可用空间，即去除了padding后的高度
     *
     * @return
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    //退出之后需要重置这个值，不然下次进来还是上次残留的页面，会出现滚动异常
    public void resetVerticalScrollOffset(){
        verticalScrollOffset = 0;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //滚动到最下方，弹出键盘，此时会重新计算高度，导致高度异常，上方出现异常空白区域，以及丢失部分搜索结果的显示
        resetVerticalScrollOffset();
        detachAndScrapAttachedViews(recycler);
        totalHeight = 0;
        int curLineWidth = 0, curLineTop = 0;//curLineWidth 累加item布局时的x轴偏移curLineTop 累加item布局时的x轴偏移
        int lastLineMaxHeight = 0;
        int right = getWidth() - getPaddingRight();

        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            //获取每个item的布局参数，计算每个item的占用位置时需要加上margin
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            int height = getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            itemHeight = height;
            curLineWidth += width;//累加当前行已有item的宽度
            if (curLineWidth <= getWidth()) {//如果累加的宽度小于等于RecyclerView的宽度，不需要换行
                if (isRTLView){
                    layoutDecorated(view, right - curLineWidth + params.leftMargin, curLineTop + params.topMargin, right - curLineWidth + params.leftMargin + width, curLineTop + height - params.bottomMargin);//布局item的真实位置
                }else {
                    layoutDecorated(view, curLineWidth - width + params.leftMargin, curLineTop + params.topMargin, curLineWidth - params.rightMargin, curLineTop + height - params.bottomMargin);//布局item的真实位置
                }
                //比较当前行多有item的最大高度，用于换行后计算item在y轴上的偏移量
                lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
            } else {//换行
                curLineWidth = width;
                if (lastLineMaxHeight == 0) {
                    lastLineMaxHeight = height;
                }
                //记录当前行top
                curLineTop += lastLineMaxHeight;
                if (isRTLView){
                    layoutDecorated(view, right - curLineWidth, curLineTop + params.topMargin, right, curLineTop + height - params.bottomMargin);//布局item的真实位置
                }else {
                    layoutDecorated(view, params.leftMargin, curLineTop + params.topMargin, width - params.rightMargin, curLineTop + height - params.bottomMargin);
                }
                lastLineMaxHeight = height;
                totalHeight += height;
            }
        }
        totalHeight = totalHeight + itemHeight;//需要加上第一次的高度
    }

    public static class HistoryItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable mDivider;

        public HistoryItemDecoration(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.history_divider);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
        }
    }
}
