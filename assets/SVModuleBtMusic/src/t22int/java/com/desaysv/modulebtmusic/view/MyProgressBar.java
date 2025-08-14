package com.desaysv.modulebtmusic.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.desaysv.modulebtmusic.R;
import com.desaysv.modulebtmusic.util.ThemeUtil;

import static android.graphics.Shader.TileMode.CLAMP;

/**
 * 自定义ProgressBar
 */
public class MyProgressBar extends ProgressBar {

    protected Paint mPaint = new Paint();
    protected int mRealWidth;
    protected int mRealHeight;
    private int mTheme = -1;

    public MyProgressBar(Context context) {
        this(context, null);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//获得控件的真实宽度
        mRealHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();//获取控件的真实高度
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (mTheme == ThemeUtil.THEME_ONE) {
            firstTheme(canvas);
            return;
        } else if (mTheme == ThemeUtil.THEME_SECOND) {
            secondTheme(canvas);
            return;
        }
        firstTheme(canvas);
    }

    /**
     * 绘制主题一的进度条
     */
    private void firstTheme(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());//移动画布的位置
        float currentProgress = (getProgress() * 1.0f / getMax()) * mRealWidth;//当前进度值
        //先绘制底层的进度条，需要从低到高一层一层绘制，避免叠加导致进度条显示异常
        //绘制第一层的进度条
        Path firstPath = new Path();
        firstPath.moveTo(5, 0);
        firstPath.lineTo(mRealWidth, 0);
        firstPath.lineTo(mRealWidth - 5, mRealHeight);
        firstPath.lineTo(0, mRealHeight);
        firstPath.close();
        int[] firstProgressColor = new int[]{getResources().getColor(R.color.color_progress_bar_bg_start),
                getResources().getColor(R.color.color_progress_bar_bg_end)};
        LinearGradient firstProgressGradient = new LinearGradient(0, 0, mRealWidth, mRealHeight,
                firstProgressColor, null, CLAMP);
        mPaint.setShader(firstProgressGradient);
        canvas.drawPath(firstPath, mPaint);
        //绘制第二层的进度条
        if (currentProgress > 0) {
            Path secondPath = new Path();
            secondPath.moveTo(5, 0);
            secondPath.lineTo(currentProgress, 0);
            secondPath.lineTo(currentProgress - 5, mRealHeight);
            secondPath.lineTo(0, mRealHeight);
            secondPath.close();
            int[] secondProgressColor = new int[]{getResources().getColor(R.color.color_progress_bar_start),
                    getResources().getColor(R.color.color_progress_bar_center),
                    getResources().getColor(R.color.color_progress_bar_end)
            };
            LinearGradient secondProgressGradient = new LinearGradient(0, 0, currentProgress, mRealHeight,
                    secondProgressColor, null, CLAMP);
            mPaint.setShader(secondProgressGradient);
            canvas.drawPath(secondPath, mPaint);
        }
        canvas.restore();
    }

    /**
     * 绘制主题二的进度条
     */
    private void secondTheme(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());//移动画布的位置
        float currentProgress = (getProgress() * 1.0f / getMax()) * mRealWidth;//当前进度值
        //先绘制底层的进度条，需要从低到高一层一层绘制，避免叠加导致进度条显示异常
        //绘制第一层的进度条
        Path firstPath = new Path();
        firstPath.addRoundRect(0, mRealHeight, mRealWidth, 0, 5, 5, Path.Direction.CW);
        int[] firstProgressColor = new int[]{getResources().getColor(R.color.color_progress_bar_bg_start),
                getResources().getColor(R.color.color_progress_bar_bg_end)};
        LinearGradient firstProgressGradient = new LinearGradient(0, 0, mRealWidth, mRealHeight,
                firstProgressColor, null, CLAMP);
        mPaint.setShader(firstProgressGradient);
        canvas.drawPath(firstPath, mPaint);
        //绘制第二层的进度条
        if (currentProgress > 0) {
            Path secondPath = new Path();
            secondPath.addRoundRect(0, mRealHeight, currentProgress, 0, 5, 5, Path.Direction.CW);
            int[] secondProgressColor = new int[]{getResources().getColor(R.color.color_progress_bar_start),
                    getResources().getColor(R.color.color_progress_bar_center),
                    getResources().getColor(R.color.color_progress_bar_end)
            };
            LinearGradient secondProgressGradient = new LinearGradient(0, 0, currentProgress, mRealHeight,
                    secondProgressColor, null, CLAMP);
            mPaint.setShader(secondProgressGradient);
            canvas.drawPath(secondPath, mPaint);
        }
        canvas.restore();
    }

    /**
     * 设置Theme
     */
    public void setTheme(int Theme) {
        mTheme = Theme;
    }
}
