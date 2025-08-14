package com.desaysv.svliblyrics.lyrics;

import android.content.Context;
import android.graphics.Paint;

/**
 * @author uidq1846
 * @desc
 * @time 2020-12-17 15:43
 */
public class DimenUtils {
    /**
     * 通过Paint获取测绘的宽高
     *
     * @param mPaint mPaint
     * @return int TextHeight
     */
    public static int getTextHeight(Paint mPaint) {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        return (int) (fontMetrics.bottom - fontMetrics.top);
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dp      dp
     * @return px
     */
    public static float dp2px(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
