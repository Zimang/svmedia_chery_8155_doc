package com.desaysv.moduleusbmusic.ui.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.desaysv.svliblyrics.R;
import com.desaysv.svliblyrics.lyrics.DimenUtils;
import com.desaysv.svliblyrics.lyrics.ILyricsView;
import com.desaysv.svliblyrics.lyrics.LyricsRow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author uidq1846
 * @desc 用于歌词显示的view，支持拖动等，当前只显示一行，其它形式看onDraw当中屏蔽的代码
 * 关键还是lrc歌词的格式匹配规则，解析方式等，具体看LyricsParser
 * 这个视图应该由具体的UI模块实现
 * @time 2020-12-17 14:54
 */
public class LyricsView extends View implements ILyricsView {
    private final String TAG = "LyricsView";
    private final String DEFAULT_TEXT;
    /***移动一句歌词的持续时间***/
    private final int DURATION_AUTO_SCROLL = 300;
    /***停止触摸 如果View需要滚动的持续时间***/
    private final int DURATION_RESET = 400;
    //默认的值
    private final float SIZE_TEXT_DEFAULT; // Variables 文字大小
    private final float SIZE_TEXT_CUR_DEFAULT;
    private final float SIZE_TIME_DEFAULT;
    private final float PADDING_DEFAULT;
    private final float PADDING_TIME_DEFAULT;

    private final float LEVEL_ONE_PADDING;
    private final float LEVEL_TWO_PADDING;
    //歌词scale缩放比例
    private final float MIN_SCALE; // 最小缩放
    private final float MAX_SCALE; // 最大缩放
    //宽高参数
    private int mWidth;
    private int mHeight;
    private Context mContext;
    private Scroller mScroller;
    private List<LyricsRow> lyricsRows;
    private int mTouchSlop;
    private float mDX, mDY; // ActionDown的坐标(dx,dy)
    private float mLastY; // TouchEvent最后一次坐标(lastX,lastY)
    private boolean mDVaild;
    private boolean mCanDrag;

    private int mColorText;
    private int mColorTextCur;
    private int mColorTime;
    private float mSizeText; // Variables 文字大小
    private float mSizeTextCur;
    private float mSizeTime;
    private float mPadding;
    // 一级padding，用于中间歌词的，广汽这边中间级padding会比较大
    private float mLevelOnePadding;
    // 二级padding，用于其他歌词的
    private float mLevelTwoPadding;
    private float mPaddingTime;
    private Paint mPaint; // 仅用于普通文字的画笔
    private Paint mPaintCur; // 仅用于当前高亮文字的画笔
    private Paint mPaintLine; // 仅用于画线的画笔

    private int mRowCount;
    private int mLastRow = -1;
    private int mCurRow = -1;

    private int mRowHeight; // One row height(text+padding)
    // 二级歌词的paint的一半高度
    private int mOtherOffsetY;
    // 歌词是否可以拖动
    private boolean isCanSliding = false;
    // 当前音乐播放状态
    private boolean isPlay;
    // 中间那一行的高度
    private int mCenterRowHeight;
    // 其他歌词的高度
    private int mOtherRowHeight;
    private ValueAnimator mAnimation;
    private float mTextOffsetX; // 当前歌词水平滚动偏移
    private float mScaleFactor; // 进度因子
    private boolean mWithLine;
    //进行加锁处理
    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    //读锁，排斥写操作
    private final Lock readLock = reentrantLock.readLock();
    //写锁，排斥读与写操作
    private final Lock writeLock = reentrantLock.writeLock();

    private OnClickListener mOnClickListener;
    private OnSeekChangeListener mOnSeekChangeListener;

    static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private final WeakReference<LyricsView> reference;

        UpdateListener(LyricsView view) {
            this.reference = new WeakReference<>(view);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            LyricsView view = reference.get();
            if (view == null || view.mContext == null
                    || view.mContext instanceof Activity && ((Activity) view.mContext).isFinishing()) {
                return;
            }
            view.mTextOffsetX = (float) animation.getAnimatedValue();
            view.invalidate();
        }
    }

    public LyricsView(Context context) {
        this(context, null);
    }

    public LyricsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ResourceAsColor")
    public LyricsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LyricsView);
        mColorText = typedArray.getColor(R.styleable.LyricsView_textColor, Color.parseColor("#4577B7"));
        mColorTextCur = typedArray.getColor(R.styleable.LyricsView_textColorShow, Color.parseColor("#FF4081"));
        mColorTime = typedArray.getColor(R.styleable.LyricsView_textColorTime, Color.parseColor("#FF4081"));
        mSizeText = SIZE_TEXT_DEFAULT = typedArray.getDimension(R.styleable.LyricsView_textSize, 30);
        mSizeTextCur = SIZE_TEXT_CUR_DEFAULT = typedArray.getDimension(R.styleable.LyricsView_textShowSize, 40);
        mSizeTime = SIZE_TIME_DEFAULT = typedArray.getDimension(R.styleable.LyricsView_textTimeSize, 20);
        mPadding = PADDING_DEFAULT = typedArray.getDimension(R.styleable.LyricsView_lrcPadding, 30);
        mLevelOnePadding = LEVEL_ONE_PADDING = typedArray.getDimension(R.styleable.LyricsView_level1Padding, 23);
        mLevelTwoPadding = LEVEL_TWO_PADDING = typedArray.getDimension(R.styleable.LyricsView_level2Padding, 17);
        MIN_SCALE = typedArray.getFloat(R.styleable.LyricsView_lrcMinScale, 0.7f);
        MAX_SCALE = typedArray.getFloat(R.styleable.LyricsView_lrcMaxScale, 2.0f);
        DEFAULT_TEXT = typedArray.getString(R.styleable.LyricsView_defaultLyrics);
        typedArray.recycle();
        mPaddingTime = PADDING_TIME_DEFAULT = DimenUtils.dp2px(context, 20);
        init(context);
    }

    @Override
    public void init(Context context) {
        mContext = context;
        lyricsRows = new ArrayList<>();
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setColor(mColorText);
        mPaintCur = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCur.setTextAlign(Paint.Align.LEFT);
        mPaintCur.setColor(mColorTextCur);
        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLine.setTextAlign(Paint.Align.LEFT);
        mPaintLine.setColor(mColorTime);
        resetValues();
        initAnim();
    }

    @Override
    public void setLyricsRows(List<LyricsRow> lyricsRow) {
        if (lyricsRows != null) {
            forceFinished();
            mLastRow = -1;
            mCurRow = -1;
            writeLock.lock();
            try {
                this.lyricsRows.clear();
                this.lyricsRows.addAll(lyricsRow);
            } finally {
                writeLock.unlock();
            }
            scrollTo(0, 0);
            invalidate();
        }
    }

    private void initAnim() {
        mAnimation = new ValueAnimator();
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.addUpdateListener(new UpdateListener(this));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 显示默认的文字 => 暂无歌词
        //无歌词时显示白色40%透明度，有歌词中间显示白色
        if (lyricsRows.size() <= 0) {
            // 画默认的显示文字,暂时和当前播放一个色
            mPaintCur.setColor(mColorText);
            drawLine(canvas, mPaintCur,
                    mHeight / 2 + DimenUtils.getTextHeight(mPaint) / 2,
                    DEFAULT_TEXT);
            return;
        } else {
            mPaintCur.setColor(mColorTextCur);
        }
        readLock.lock();
        try {
            //视图的y轴中心位置
            float cy = mHeight / 2;
            int minRaw = 0;
            int maxRaw = lyricsRows.size() - 1;
            float rowY;
            if (mCurRow >= 0) {
                Log.d(TAG, "onDraw: mCurRowContent = " + lyricsRows.get(mCurRow).toString() + " mCurRow = " + mCurRow);
            }
            for (int i = minRaw; i <= maxRaw; i++) {
                // 画出来的第一行歌词的y坐标
                rowY = cy + i * mOtherRowHeight - mOtherOffsetY - mLevelOnePadding;
                String text = lyricsRows.get(i).getContent();//获取到高亮歌词
                if (mCurRow == i) {
                    // 画高亮歌词
                    // 因为有缩放效果，所有需要动态设置歌词的字体大小
                    //float size = mSizeTextCur + (mSizeTextCur - mSizeText) * mScaleFactor;
                    mPaintCur.setTextSize(SIZE_TEXT_CUR_DEFAULT);
                    // 用画笔测量歌词的宽度
                    float textWidth = mPaintCur.measureText(text);
                    // 当歌词大于宽度且当前音乐是在播放状态才需要水平滚动，否则不需要
                    if (textWidth > mWidth && isPlay) {
                        // 如果歌词宽度大于view的宽，则需要动态设置歌词的起始x坐标，以实现水平滚动
                        canvas.drawText(text, mTextOffsetX, rowY, mPaintCur);
                    } else {
                        // 如果歌词宽度小于view的宽，则让歌词居中显示
                        float rowX = (mWidth - textWidth) / 2;
                        //设置不居中
                        canvas.drawText(text, rowX, rowY, mPaintCur);
                    }
                } else {
                    //其它歌词
                    if (i == mLastRow) {
                        // 画高亮歌词的上一句
                        // 因为有缩放效果，所有需要动态设置歌词的字体大小
                        float size = mSizeTextCur - (mSizeTextCur - mSizeText) * mScaleFactor;
                        mPaint.setTextSize(size);
                    } else {
                        // 画其他的歌词
                        mPaint.setTextSize(mSizeText);
                    }
                    float textWidth = mPaint.measureText(text);
                    float rowX = (getWidth() - textWidth) / 2;
                    // 如果计算出的cx为负数,将cx置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
                    if (rowX < 0) {
                        rowX = 0;
                    }
                    // 实现颜色渐变
                    //mPaint.setColor(0x10000000 * (mRowCount - Math.abs(i - mCurRow)) * 3 + mColorText);
                    //大于中间行数的偏移
                    canvas.drawText(text, rowX, rowY, mPaint);
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 设置播放音乐播放状态
     *
     * @param isPlay
     */
    public void setPlayStatus(boolean isPlay) {
        Log.d(TAG, "setPlayStatus: isPlay = " + isPlay);
        this.isPlay = isPlay;
    }

    /**
     * 当前歌词能否拖动
     *
     * @return
     */
    public boolean isCanSliding() {
        return isCanSliding;
    }

    /**
     * 设置是否允许滑动，默认是不允许滑动的
     *
     * @param isCanSliding
     */
    public void setCanSliding(boolean isCanSliding) {
        Log.d(TAG, "setCanSmooth: this.isCanSliding = " + this.isCanSliding + ", isCanSliding = " + isCanSliding);
        this.isCanSliding = isCanSliding;
    }

    /**
     * drawLine
     *
     * @param canvas canvas
     * @param paint  paint
     * @param y      y
     * @param text   text
     */
    private void drawLine(Canvas canvas, Paint paint, float y, String text) {
        float textWidth = paint.measureText(text);
        float textX = (mWidth - textWidth) / 2;
        if (textX < 0) {
            textX = 0;
        }
        canvas.drawText(text, textX, y, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        // 初始化将要绘制的歌词行数,因为不需要将所有歌词画出来
        mRowCount = (mHeight - mCenterRowHeight) / mOtherRowHeight + 1;
        Log.d(TAG, "onMeasure: mRowCount = " + mRowCount + ", mHeight = " + mHeight + " , mWidth = " + mWidth +
                ", mCenterRowHeight = " + mCenterRowHeight + ", mOtherRowHeight = " + mOtherRowHeight);
        setMeasuredDimension(mWidth, mHeight);
    }

    private void smoothScrollTo(int dstY, int duration) {
        int oldScrollY = getScrollY();
        int offset = dstY - oldScrollY;
        //设置滑动到指定位置，如过不需要显示滑动过程则duration设置为0
        mScroller.startScroll(getScrollX(), oldScrollY, getScrollX(), offset, duration);
        invalidate();
    }

    private void forceFinished() {
        stopHorizontalScrollLrc();
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            mScaleFactor = mScroller.timePassed() * 3f / DURATION_AUTO_SCROLL;
            if (mScaleFactor > 1) {
                mScaleFactor = 1;
            }
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lyricsRows.size() <= 0) {
            return false;
        }
        float eX = event.getX();
        float eY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isCanSliding) {
                    Log.d(TAG, "onTouchEvent: Don't allowed to slide, return!");
                    return false;
                }
                Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                mDX = event.getX();
                mDY = mLastY = event.getY();
                mDVaild = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mDVaild && (Math.abs(eX - mDX) > mTouchSlop || Math.abs(eY - mDY) > mTouchSlop)) {
                    // 点击无效
                    mDVaild = false;
                }
                if (!mCanDrag && Math.abs(eY - mDY) > mTouchSlop
                        && Math.abs(eY - mDY) > Math.abs(eX - mDX)) {
                    mCanDrag = true;
                    forceFinished();
                    stopHorizontalScrollLrc();
                    mScaleFactor = 1;
                    mWithLine = true;
                }
                if (mCanDrag) {
                    // 偏移量
                    float offset = eY - mLastY;
                    scrollBy(getScrollX(), -(int) offset);
                    int curRow = (getScrollY() + mRowHeight / 2) / mRowHeight;
                    curRow = Math.max(curRow, 0);
                    curRow = Math.min(curRow, lyricsRows.size() - 1);
//                    seekTo(lyricsRows.get(curRow).getTime(), true);
                }
                mLastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouchEvent: ACTION_UP or ACTION_CANCEL");
                if (mDVaild) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick();
                    }
                } else {
                    if (mCanDrag) {
                        readLock.lock();
                        try {
                            if (lyricsRows.size() > 0 && mCurRow < lyricsRows.size()
                                    && mOnSeekChangeListener != null) {
                                mOnSeekChangeListener.onProgressChanged(lyricsRows.get(mCurRow).getTime());
                            }
                        } finally {
                            readLock.unlock();
                        }
                    }
                }
                if (getScrollY() < 0) {
                    smoothScrollTo(0, DURATION_RESET);
                } else {
                    int offset = (int) (lyricsRows.size() * mRowHeight - mPadding);
                    if (getScrollY() > offset) {
                        smoothScrollTo(offset, DURATION_RESET);
                    }
                }
                resetTouch();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void resetTouch() {
        mDX = mDY = 0;
        mDVaild = mCanDrag = false;
        mWithLine = false;
    }

    /**
     * 设置 setLyricsRows
     *
     * @param lyricsRows lyricsRows
     * @param progress   progress
     */
    public void setLyricsRows(List<LyricsRow> lyricsRows, int progress) {
        if (progress <= 0) {
            setLyricsRows(lyricsRows);
            return;
        }
        if (lyricsRows != null) {
            forceFinished();
            mLastRow = -1;
            mCurRow = -1;
            writeLock.lock();
            try {
                this.lyricsRows.clear();
                this.lyricsRows.addAll(lyricsRows);
            } finally {
                writeLock.unlock();
            }
            seekTo(progress, true);
            invalidate();
        }
    }

    @Override
    public void seekTo(int progress, boolean fromUser) {
        if (lyricsRows.size() <= 0) {
            return;
        }
        // 如果是由seekbar的进度改变触发 并且这时候处于拖动状态，则返回
        if (!fromUser && mCanDrag) {
            return;
        }
        readLock.lock();
        try {
            int size = lyricsRows.size();
            for (int i = size - 1; i >= 0; i--) {
                LyricsRow lyricsRow = lyricsRows.get(i);
                //如果当前已经是这个位置则不再响应,这里还有个问题，就是当前的lyricsRows并非时长小到大排序的
                if (progress >= lyricsRow.getTime()) {
                    if (mCurRow != i) {
                        mLastRow = mCurRow;
                        mCurRow = i;
                        Log.d(TAG, "seekTo: progress = " + progress + " lyricsRow = " + lyricsRow.toString());
                        handleProgress(lyricsRow, fromUser);
                    }
                    break;
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 设置歌词，这里改为通过开启子线程更新歌词数据，防止歌词过大
     *
     * @param
     */
    public void updateLyrics(int lyricsIndex, boolean isFromUser) {
        if (lyricsRows.size() == 0) {
            Log.d(TAG, "updateLyrics: list.size == 0");
            return;
        }
        readLock.lock();
        try {
            if (lyricsIndex > lyricsRows.size() - 1) {
                Log.w(TAG, "updateLyrics: lyricsIndex = " + lyricsIndex + " size = " + lyricsRows.size());
                return;
            }
            Log.d(TAG, "updateLyrics: lyrics = " + lyricsRows.get(lyricsIndex) + ", lyricsIndex = " + lyricsIndex);
            if (mCurRow != lyricsIndex) {
                mLastRow = mCurRow;
                mCurRow = lyricsIndex;
                invalidate();
                handleProgress(lyricsRows.get(lyricsIndex), isFromUser);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setLyricsScale(float factor) {
        if (factor < 0 || factor > 1) {
            return;
        }
        factor = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * factor;
        mSizeText = SIZE_TEXT_DEFAULT * factor;
        mSizeTextCur = SIZE_TEXT_CUR_DEFAULT * factor;
        mSizeTime = SIZE_TIME_DEFAULT * factor;
        mPadding = PADDING_DEFAULT * factor;
        mPaddingTime = PADDING_TIME_DEFAULT * factor;
        resetValues();
        forceFinished();
        if (mCurRow != -1) {
            scrollTo(getScrollX(), mCurRow * mRowHeight);
        }
        invalidate();
    }

    private void handleProgress(LyricsRow lyricsRow, boolean fromUser) {
        if (mCanDrag) {
            invalidate();
            return;
        }
        int dstY = (mCurRow - 1) * mOtherRowHeight + mCenterRowHeight;
        if (fromUser) {
            forceFinished();
            scrollTo(getScrollX(), dstY);
            invalidate();
        } else {
            smoothScrollTo(dstY, DURATION_AUTO_SCROLL);
            // 如果高亮歌词的宽度大于View的宽，就需要开启属性动画，让它水平滚动
            float textWidth = mPaintCur.measureText(lyricsRow.getContent());
            if (textWidth > mWidth) {
                startHorizontalScrollLrc(mWidth - textWidth, (long) (lyricsRow.getTotalTime() * 0.6));
            }
        }
    }

    private void resetValues() {
        mPaint.setTextSize(mSizeText);
        mPaintCur.setTextSize(mSizeTextCur);
        mPaintLine.setTextSize(mSizeTime);
//        mOffsetY = (int) (DimenUtils.getTextHeight(mPaintCur) / 2);
        mRowHeight = (int) (DimenUtils.getTextHeight(mPaintCur) + mPadding);
        //padding包括上下两部分
        //mCenterRowHeight = (int) (DimenUtils.getTextHeight(mPaintCur) + mLevelOnePadding * 2);
        //mOtherRowHeight = (int) (DimenUtils.getTextHeight(mPaint) + mLevelTwoPadding * 2);
        mCenterRowHeight = 80;
        mOtherRowHeight = 80;
        // offset也包括两部分，中间正在播放的offset比较大，其他歌词比较小
        mOtherOffsetY = (int) (DimenUtils.getTextHeight(mPaint) / 2);
    }

    @Override
    public void reset() {
        forceFinished();
        mLastRow = -1;
        mCurRow = -1;
        writeLock.lock();
        try {
            lyricsRows.clear();
        } finally {
            writeLock.unlock();
        }
        scrollTo(0, 0);
        invalidate();
    }

    private void startHorizontalScrollLrc(float endX, long duration) {
        if (mAnimation == null) {
            initAnim();
        } else {
            stopHorizontalScrollLrc();
        }
        mAnimation.setFloatValues(0, endX);
        mAnimation.setDuration(duration);
        // 延迟执行属性动画
        mAnimation.setStartDelay((long) (duration * 0.3));
        mAnimation.start();
    }

    private void stopHorizontalScrollLrc() {
        if (mAnimation != null) {
            mTextOffsetX = 0;
            mAnimation.cancel();
        }
    }

    public interface OnClickListener {
        void onClick();
    }

    public interface OnSeekChangeListener {
        void onProgressChanged(int progress);
    }

    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    public void setOnSeekChangeListener(OnSeekChangeListener l) {
        this.mOnSeekChangeListener = l;
    }
}
