package com.desaysv.moduleradio.view.cursor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.moduleradio.R;

public class HorizontalCursorView extends CursorView {

    public HorizontalCursorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        orientation = HORIZONTAL;
    }

    public HorizontalCursorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        orientation = HORIZONTAL;
    }

    /**
     * 绘制刻度的线段
     *
     * @param canvas            刻度尺的画布
     * @param needDrawFrequency 是否需要绘制刻度值，原来确认是否是大刻度，true：大刻度 false：小刻度
     * @param positionOffset    当前要绘制的刻度与第一个可见刻度相差了几个刻度
     */
    protected void drawFrequencyLine(Canvas canvas, boolean needDrawFrequency, int positionOffset) {
        float finalLineHeight = lineHeight;
        float startX, startY, stopX, stopY;
        Paint painter = linePainter;
        if (needDrawFrequency) {
            painter = linePlusPainter;
            // 刻度是竖直方向，而且是大刻度，高度增加一个差值
            finalLineHeight += linePlusGap;
        }
        // 计算刻度线条的绘制起始/终点坐标
        startX = (itemWidth - lineWidth) / 2 /*刻度的中间点*/
                + positionOffset * itemWidth /*当前刻度与第一个可见刻度的偏移像素*/
                - mCursorOffsetToStart        /*每个刻度都应该偏移的量，手指滑动刻度尺导致*/;
        stopX = startX;
        // 因为刻度线的底部要保持在同一水平线上，对于小刻度来说起始的纵坐标要向下偏移linePlusGap的差值
        startY = (lineHeight - finalLineHeight) + linePlusGap;
        stopY = startY + finalLineHeight;
        painter.setColor(colors[positionOffset]);
        canvas.drawLine(startX, startY, stopX, stopY, painter);
    }

    /**
     * 绘制刻度频点值
     *
     * @param canvas         刻度尺画布
     * @param frequencyStr   刻度值
     * @param positionOffset 当前要绘制的刻度与第一个可见刻度相差了几个刻度
     */
    protected void drawFrequencyValue(Canvas canvas, String frequencyStr, int positionOffset) {
        // 根据要显示的刻度值，测量其显示区域到frequencyRect
        frequencyPainter.getTextBounds(frequencyStr, 0, frequencyStr.length(), frequencyRect);
        // 从frequencyRect获取刻度值显示区域的宽度
        int frequencyValueWidth = frequencyRect.width();
        // 绘制刻度值需要的是左下顶点的坐标，不是左上的坐标
        float x, y;
        x = (itemWidth - frequencyValueWidth) / 2 /*刻度值的中间点，刻度值显示区域基本会超出刻度宽度*/
                + positionOffset * itemWidth      /*当前刻度与第一个可见刻度的偏移像素*/
                - mCursorOffsetToStart/*每个刻度都应该偏移的量，手指滑动刻度尺导致*/
                - lineWidth;
        // y计算的是刻度值显示区域的底部纵坐标，而不是从顶部算
        // 而且要减掉底部的内边距
        y = mCursorViewHeight - getPaddingBottom();
        if (Float.parseFloat(frequencyStr) == mCenterItemFrequency){
            frequencyPainter.setColor(getResources().getColor(R.color.radio_blue));
        }else {
            frequencyPainter.setColor(colors[positionOffset]);
        }
        canvas.drawText(frequencyStr, x, y, frequencyPainter);
    }

}
