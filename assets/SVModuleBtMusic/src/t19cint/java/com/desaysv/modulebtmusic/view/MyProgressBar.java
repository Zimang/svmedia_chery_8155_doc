package com.desaysv.modulebtmusic.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import com.desaysv.modulebtmusic.R;

import static android.graphics.Shader.TileMode.CLAMP;

/**
 * 自定义ProgressBar
 */
public class MyProgressBar extends ProgressBar {

    private Bitmap letterBg;

    protected Paint mBitmapPaint = new Paint();
    protected Paint mPaint = new Paint();
    protected int mRealWidth;
    protected int mRealHeight;
    protected int mRealProgressWidth;
    protected int mRealProgressStartX;
    protected int mRealProgressEndX;
    protected int mRealProgressHeight;
    protected int mRealProgressStartY;
    protected int mRealProgressEndY;

    public MyProgressBar(Context context) {
        this(context, null);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initBitmapBackground();
    }

    private float[] radiusArray = {11f, 11f, 11f, 11f, 11f, 11f, 11f, 11f};//圆角

    private void initBitmapBackground() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_player_progress_btn, null);
        letterBg = bitmap;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//获得控件的真实宽度
        mRealHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();//获取控件的真实高度
        mRealProgressWidth = mRealWidth - 60;//获取进度条的真实宽度
        mRealProgressStartX = 30;//获取进度条的开始的X坐标值
        mRealProgressEndX = mRealProgressWidth + mRealProgressStartX;//获取进度条的结束的X坐标值
        mRealProgressHeight = mRealHeight - 60;//获取进度条的真实高度
        mRealProgressStartY = mRealHeight - 40;//获取进度条的开始的Y坐标值
        mRealProgressEndY = mRealProgressStartY + 10;//获取进度条的结束的Y坐标值
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());//移动画布的位置
        float currentProgress = (getProgress() * 1.0f / getMax()) * mRealProgressWidth;//当前进度值
        //先绘制底层的进度条，需要从低到高一层一层绘制，避免叠加导致进度条显示异常
        //绘制第一层的进度条
        Path firstPath = new Path();
        firstPath.addRoundRect(new RectF(mRealProgressStartX, mRealProgressStartY, mRealProgressEndX, mRealProgressEndY), radiusArray, Path.Direction.CW);
        int[] firstProgressColor = new int[]{getResources().getColor(R.color.color_progress_bar_bg_start),
                getResources().getColor(R.color.color_progress_bar_bg_end)};
        LinearGradient firstProgressGradient = new LinearGradient(mRealProgressStartX, mRealProgressStartY, mRealProgressEndX, mRealProgressEndY,
                firstProgressColor, null, CLAMP);
        mPaint.setShader(firstProgressGradient);
        canvas.drawPath(firstPath, mPaint);
        //绘制第二层的进度条
        if (currentProgress > 0) {
            Path secondPath = new Path();
            secondPath.addRoundRect(new RectF(mRealProgressStartX, mRealProgressStartY, currentProgress + mRealProgressStartX, mRealProgressEndY), radiusArray, Path.Direction.CW);
            int[] secondProgressColor = new int[]{getResources().getColor(R.color.color_progress_bar_start),
                    getResources().getColor(R.color.color_progress_bar_center),
                    getResources().getColor(R.color.color_progress_bar_end)
            };
            LinearGradient secondProgressGradient = new LinearGradient(mRealProgressStartX, mRealProgressStartY, currentProgress, mRealProgressEndY,
                    secondProgressColor, null, CLAMP);
            mPaint.setShader(secondProgressGradient);
            canvas.drawPath(secondPath, mPaint);
        }
        if (letterBg != null) {
            //绘制第三层的进度条
            if (currentProgress > 0) {
                canvas.drawBitmap(letterBg, currentProgress, 0, mBitmapPaint);
            } else {
                canvas.drawBitmap(letterBg, 0, 0, mBitmapPaint);
            }
        }
        canvas.restore();
    }
}
