package com.desaysv.moduleradio.view.cursor;


import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.radio.RadioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.desaysv.moduleradio.R;

import java.util.List;

public abstract class CursorView extends View {
    protected final String TAG = getClass().getSimpleName();
    public static final float NONE_FREQUENCY = -1;
    private OnFrequencyChangedListener frequencyChangedListener;

    final int NONE_OFFSET = 0;
    final int NONE_LOCATION = -1;

    protected final int HORIZONTAL = 0;
    protected final int VERTICAL = 1;

    protected Adapter mCursorAdapter;

    /**
     * 手势解析器，根据手指滑动距离同步刻度尺的滚动
     */
    private GestureDetector mGestureDetector;
    /**
     * 处理惯性滚动，停止滑动的时候可以匀速停止刻度尺的滚动
     */
    private Scroller mScroller;

    /**
     * 当前刻度尺中间位置所指向的刻度值所在刻度值列表中的位置
     * 刻度值列表{@link Adapter#getItemList()}
     */
    protected int mCurrentPosition;

    /**
     * 刻度尺的可见宽度
     */
    protected int mCursorViewWidth;
    /**
     * 刻度尺的可见高度
     */
    protected int mCursorViewHeight;

    /**
     * 刻度尺可见宽度的中间位置（像素点）
     */
    protected int mCenterLocation = -1;

    /**
     * 滑动刻度尺时，每个刻度与刻度尺起始坐标的偏移量
     * 如果是向左滑动，该值会是正数
     * 如果是向右滑动，该值会是负数
     */
    protected int mCursorOffsetToStart = 0;

    /**
     * 刻度尺中可见刻度的数量
     */
    protected int mVisibleItemsSize;

    //*************** start 刻度尺的一些属性 可通过XML配置 *******************
    /**
     * 刻度尺的朝向
     */
    protected int orientation = HORIZONTAL;
    /**
     * 每个刻度占的宽度，
     * 需要比刻度线段绘制的宽度要大，才能有空隙
     */
    protected float itemWidth = 15F;
    /**
     * 频点线段宽度
     */
    protected float lineWidth = 1F;
    /**
     * 频点线段高度（小刻度）
     */
    protected float lineHeight = 10F;
    /**
     * 频点线段在原高度上增加多少（大刻度与小刻度高度的差值）
     */
    protected float linePlusGap = 10F;
    /**
     * 小刻度的线段颜色值
     */
    protected int lineColor = Color.parseColor("#000000");
    /**
     * 大刻度的线段颜色值
     */
    protected int linePlusColor = Color.parseColor("#000000");
    /**
     * 频点值的字体颜色
     */
    protected int textColor = Color.parseColor("#000000");
    /**
     * 频点值的字体大小
     */
    protected float textSize = 20.0F;
    /**
     * 一个大的刻度分为多少个小的刻度，原来计算哪个刻度需要绘制刻度值
     */
    protected int divisions = 10;
    //*************** end 刻度尺的一些属性 可通过XML配置 *******************

    /**
     * 显示频点值的区域，用来测量绘制频点值时所需的偏移
     */
    protected final Rect frequencyRect = new Rect();
    /**
     * 该频点值的显示是否需要转化成整型，默认是浮点型
     */
    protected boolean isIntegerValue;
    /**
     * 当前中点刻度的频率值，用来在列表数据发生变化的时候重新定位
     */
    protected float mCenterItemFrequency;

    protected Paint linePainter;
    protected Paint linePlusPainter;
    protected Paint frequencyPainter;

    private ArgbEvaluator evaluator;
    protected int[] colors;

    public CursorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CursorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化一些从XML配置的属性，并根据属性设置小刻度、大刻度、频点值的画笔
     */
    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CursorView);
        orientation = ta.getInt(R.styleable.CursorView_android_orientation, orientation);
        itemWidth = ta.getDimension(R.styleable.CursorView_itemWidth, itemWidth);
        lineWidth = ta.getDimension(R.styleable.CursorView_lineWidth, lineWidth);
        lineHeight = ta.getDimension(R.styleable.CursorView_lineHeight, lineHeight);
        linePlusGap = ta.getDimension(R.styleable.CursorView_linePlusGap, linePlusGap);
        lineColor = ta.getColor(R.styleable.CursorView_lineColor, lineColor);
        linePlusColor = ta.getColor(R.styleable.CursorView_linePlusColor, linePlusColor);
        textSize = ta.getDimension(R.styleable.CursorView_android_textSize, textSize);
        textColor = ta.getInt(R.styleable.CursorView_android_textColor, textColor);
        divisions = ta.getInt(R.styleable.CursorView_divisions, divisions);
        ta.recycle();

        linePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePainter.setColor(lineColor);
        linePainter.setStrokeWidth(lineWidth);
        linePlusPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePlusPainter.setColor(linePlusColor);
        linePlusPainter.setStrokeWidth(lineWidth);
        frequencyPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        frequencyPainter.setColor(textColor);
        frequencyPainter.setTextSize(textSize);

        evaluator = new ArgbEvaluator();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 刻度尺加载完成设置一下手势滑动的监听
        mScroller = new Scroller(getContext());
        mGestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCursorViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mCursorViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mCenterLocation == NONE_LOCATION) {
            // 第一次加载、或刻度尺数据发生变化时重置以下变量
            mCursorOffsetToStart = NONE_OFFSET;
            mCenterLocation = mCursorViewWidth / 2;
            mVisibleItemsSize = (int) (mCursorViewWidth / itemWidth);
            colors = new int[mVisibleItemsSize];
            int color;
            // 初始化可视范围内刻度尺线段的颜色，显示的时候就不需要再做计算了
            for (int i = 0; i < colors.length; i++) {
                if (i <= mVisibleItemsSize / 2) {
                    color = (int) evaluator.evaluate(2 * (float) i / mVisibleItemsSize,
                            getContext().getColor(R.color.radio_dividing_item_color), lineColor);
                } else {
                    color = (int) evaluator.evaluate(2 * (float) i / mVisibleItemsSize - 1,
                            lineColor, getContext().getColor(R.color.radio_dividing_item_color));
                }
                colors[i] = color;
            }
        }
    }

    /**
     * 刻度列表发生变化时通知更新刻度尺
     */
    public void notifyDataSetChanged() {
        mCenterLocation = NONE_LOCATION;
        if (mCursorAdapter != null) {
            // 将当前的刻度位置定位到上一次的显示的位置
            mCurrentPosition = mCursorAdapter.getItemList().indexOf(mCenterItemFrequency);
        }
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: ");
        // 计算第一个可见的刻度值在列表中的位置
        int firstVisiblePosition = mCurrentPosition - mVisibleItemsSize / 2;
        if (mCurrentPosition <= mVisibleItemsSize / 2) {
            // 如果当前刻度值所在列表位置小于可见刻度值数量的一半，
            // 说明所有可见刻度值并不是连续的，
            // 左边的刻度值是列表的尾部，右侧的刻度值是列表的头部
            // 所以这里需要加上整个列表的数量才是第一个可见刻度在列表上的真正位置
            firstVisiblePosition += mCursorAdapter.getItemCount();
        }
        for (int i = 0; i < mVisibleItemsSize; i++) {
            // 从第一个可见刻度开始绘制所有可见的刻度
            drawCursorItem(canvas, firstVisiblePosition, i);
        }
    }

    /**
     * 绘制一个刻度
     *
     * @param canvas               刻度尺的画布
     * @param firstVisiblePosition 第一个可见刻度所在刻度列表中的位置（下标）
     * @param positionOffset       当前要绘制的刻度与第一个可见刻度相差了几个刻度
     */
    private void drawCursorItem(Canvas canvas, int firstVisiblePosition, int positionOffset) {

        if (mCurrentPosition < mVisibleItemsSize / 2){//到达左边界
            Log.d(TAG,"到达左边界");
            if (positionOffset < (mVisibleItemsSize / 2 - mCurrentPosition)){//左侧不画
                return;
            }
        }
        if (mCurrentPosition > mCursorAdapter.getItemCount() -1 - mVisibleItemsSize / 2){//到达右边界
            Log.d(TAG,"到达右边界");
            if (positionOffset > (mCursorAdapter.getItemCount() -1 - mCurrentPosition + mVisibleItemsSize / 2)){//右侧不画
                return;
            }
        }

        // 当前绘制刻度在刻度列表中的真正位置（下标）
        int position = (firstVisiblePosition + positionOffset) % mCursorAdapter.getItemCount();
        // 当前绘制刻度的刻度值
        float frequency = mCursorAdapter.getItemList().get(position);
        // 是否需要在刻度线段下方绘制刻度值
        boolean needDrawFrequency = position % divisions == 0;
        drawFrequencyLine(canvas, needDrawFrequency, positionOffset);
        // 是否需要在刻度线段下方绘制刻度值
        boolean needDrawFrequency2 = position % (divisions * 2) == 0;
        if (needDrawFrequency2) {
            // 在绘制刻度值前先将刻度值转为字符串
            String frequencyStr;
            // 是否将浮点型的刻度值先转为整型，因为AM频段下的频点不需要带小数点
            if (isIntegerValue) {
                frequencyStr = String.valueOf((int) frequency);
            } else {
                frequencyStr = String.valueOf(frequency);
            }
            drawFrequencyValue(canvas, frequencyStr, positionOffset);
        }
        // 绘制时打印太多了，屏蔽掉
        // Log.d(TAG, "drawCursorItem: position = " + position + ", frequency = " + frequency);
    }

    /**
     * 绘制刻度的线段
     *
     * @param canvas            刻度尺的画布
     * @param needDrawFrequency 是否需要绘制刻度值，原来确认是否是大刻度，true：大刻度 false：小刻度
     * @param positionOffset    当前要绘制的刻度与第一个可见刻度相差了几个刻度
     */
    protected abstract void drawFrequencyLine(Canvas canvas, boolean needDrawFrequency, int positionOffset);

    /**
     * 绘制刻度频点值
     *
     * @param canvas         刻度尺画布
     * @param frequencyStr   刻度值
     * @param positionOffset 当前要绘制的刻度与第一个可见刻度相差了几个刻度
     */
    protected abstract void drawFrequencyValue(Canvas canvas, String frequencyStr, int positionOffset);

    protected void scrollCursor(float distance) {
        if (mCurrentPosition <= 0){//到达左边界
            if (distance > 0){//左滑
                scrollDistance(distance);
                Log.d(TAG, "scrollCursor: end mCursorOffsetToLeft = " + mCursorOffsetToStart +
                        ", mCurrentPosition = " + mCurrentPosition);
                invalidate();
            }
        }else if (mCurrentPosition >= mCursorAdapter.getItemCount() -1){//到达右边界
            if (distance < 0){//右滑
                scrollDistance(distance);
                Log.d(TAG, "scrollCursor: end mCursorOffsetToLeft = " + mCursorOffsetToStart +
                        ", mCurrentPosition = " + mCurrentPosition);
                invalidate();
            }
        }else {
            scrollDistance(distance);
            Log.d(TAG, "scrollCursor: end mCursorOffsetToLeft = " + mCursorOffsetToStart +
                    ", mCurrentPosition = " + mCurrentPosition);
            invalidate();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handled = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //手指抬起时定位到最近的一个刻度值
                if (mScroller.isFinished()) {
                    Log.d(TAG, "dispatchTouchEvent: ACTION_UP");
                    determineScrollLocationAndNotify();
                }
                handled = true;
                break;
        }
        return handled;
    }

    /**
     * 滑动手势处理
     */
    private final SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i(TAG, "onScroll distanceX = " + distanceX + ", distanceY = " + distanceY);
            if (orientation == HORIZONTAL) {
                // 跟随手指滑动，distanceX左滑为正，右滑为负
                scrollCursor(distanceX);
            } else {
                scrollCursor(distanceY);
            }
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: ");
            if (!mScroller.computeScrollOffset()) {
                Log.d(TAG, "onFling: computeScrollOffset");
//                determineScrollLocationAndNotify();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }
    };

    /**
     * 确定滚动后的位置，并通知变化
     */
    private void determineScrollLocationAndNotify() {
        if (mCursorAdapter.getItemList().isEmpty()) {
            // 刻度列表空了，直接通知刷新刻度尺
            notifyDataSetChanged();
            return;
        }
        scrollDistance(-mCursorOffsetToStart);
        // 有效刻度与当前滑动到的位置的偏移，通过增加偏移来查找距离当前刻度最近的有效刻度
        int positionOffset = 1;
        while (mCursorAdapter.getItemList().get(mCurrentPosition) == NONE_FREQUENCY) {
            // 因为列表中的尾部可能包含了一些为了做首尾偏差的无效刻度，
            // 避免首位刻度都是大刻度挨得太近，导致刻度值重叠的情况
            // 需要找到距离最近的一个有效刻度的位置
            int leftPosition = (mCurrentPosition - positionOffset) % mCursorAdapter.getItemCount();
            float leftFreq = mCursorAdapter.getItemList().get(leftPosition);
            int rightPosition = (mCurrentPosition + positionOffset) % mCursorAdapter.getItemCount();
            float rightFreq = mCursorAdapter.getItemList().get(rightPosition);
            if (leftFreq == NONE_FREQUENCY && rightFreq != NONE_FREQUENCY) {
                mCurrentPosition = rightPosition;
            } else if (leftFreq != NONE_FREQUENCY && rightFreq != NONE_FREQUENCY) {
                // 左右都有有效刻度，优先取右侧的刻度
                mCurrentPosition = rightPosition;
            } else if (leftFreq != NONE_FREQUENCY && rightFreq == NONE_FREQUENCY) {
                mCurrentPosition = leftPosition;
            } else {
                positionOffset++;
            }
        }
        Log.d(TAG, "fling: end mCursorOffsetToLeft = " + mCursorOffsetToStart +
                ", mCurrentPosition = " + mCurrentPosition);
        invalidate();
        // 如果频点没变化就不通知了
        Float tmpFreq = mCursorAdapter.getItemList().get(mCurrentPosition);
        if (tmpFreq.equals(mCenterItemFrequency)) {
            return;
        }
        mCenterItemFrequency = tmpFreq;
        if (frequencyChangedListener != null) {
            frequencyChangedListener.onChangedAndOpenIt(mCursorAdapter.getBand(), mCenterItemFrequency);
        }
    }

    protected void scrollDistance(float distance) {
        mCursorOffsetToStart += distance;
        // 滚动的距离可以偏移几个刻度
        int offsetItems = (int) (Math.abs(mCursorOffsetToStart) / itemWidth);
        if (mCursorOffsetToStart >= itemWidth) {
            // 左滑大于等于一个刻度了
            // 将当前刻度位置增加偏移的刻度数量
            mCurrentPosition += offsetItems;
            // 偏移距离取消增加的几个刻度的总宽度
            mCursorOffsetToStart -= offsetItems * itemWidth;
        } else if (mCursorOffsetToStart < 0 && Math.abs(mCursorOffsetToStart) >= itemWidth) {
            // 右滑大于等于一个刻度了
            // 将当前刻度位置减少偏移的刻度数量
            mCurrentPosition -= offsetItems;
            // 偏移距离加上增加的几个刻度的总宽度
            mCursorOffsetToStart += offsetItems * itemWidth;
        }
        if (mCurrentPosition <= 0) {
            // mCurrentPosition < 0 的情况会出现在右滑的时候
            // 这时候mCurrentPosition - offsetItems就会出现小于0的情况
            // 当它小于0的时候，说明此时的position变成了刻度列表的尾部区间
            // 也就是说mCurrentPosition是此时position与列表最后一个刻度的偏移
            // 需要将mCurrentPosition从倒数的位置变为正的位置，所以加上列表的数量
            mCurrentPosition = 0;//mCursorAdapter.getItemCount() + mCurrentPosition;
        }else if (mCurrentPosition >= mCursorAdapter.getItemCount() -1){
            mCurrentPosition = mCursorAdapter.getItemCount() -1;
        }
        // 最后取余确保下标不越界
        // 因为当手动滚动到起始刻度值时，mCurrentPosition不是0，而是mCursorAdapter.getItemCount()
        mCurrentPosition %= mCursorAdapter.getItemCount();
    }

    public void scrollToFrequency(float frequency) {
        // 从刻度列表中找到刻度值对应的下标位置
        // 然后刷新绘制刻度尺到该刻度值的位置
        List<Float> itemList = mCursorAdapter.getItemList();
        for (int i = 0; i < itemList.size(); i++) {
            float freq = itemList.get(i);
            if (freq == frequency) {
                mCurrentPosition = i;
                mCenterItemFrequency = freq;
                invalidate();
                break;
            }
        }
    }

    public void setAdapter(Adapter adapter) {
        Log.d(TAG, "setAdapter: ");
        this.mCursorAdapter = adapter;
        // AM频段的话需要将刻度值显示为整数类型
        isIntegerValue = adapter.getBand() == RadioManager.BAND_AM;
        requestLayout();
    }

    public void setOnFrequencyChangedListener(OnFrequencyChangedListener frequencyChangedListener) {
        this.frequencyChangedListener = frequencyChangedListener;
    }

}
