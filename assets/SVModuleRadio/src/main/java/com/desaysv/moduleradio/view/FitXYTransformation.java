package com.desaysv.moduleradio.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author uidq1846
 * @desc glide 辅助全屏裁剪 显示FitXY功能
 * @time 2024-4-18 11:47
 */
public class FitXYTransformation extends BitmapTransformation {
    private final String TAG = "FitXYTransformation";
    private static final Paint DEFAULT_PAINT = new Paint(6);
    private final Lock BITMAP_DRAWABLE_LOCK = new ReentrantLock();

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap inBitmap, int width, int height) {
        int bitmapWidth = inBitmap.getWidth();
        int bitmapHeight = inBitmap.getHeight();
        Log.d(TAG, "transform: bitmapWidth = " + bitmapWidth + " bitmapHeight = " + bitmapHeight + " width = " + width + " height = " + height);
        if (bitmapWidth == width && bitmapHeight == height) {
            return inBitmap;
        } else {
            //先计算输出宽高和输入的比值
            float widthPercentage = (float) width / (float) bitmapWidth;
            float heightPercentage = (float) height / (float) bitmapHeight;
            Log.d(TAG, "transform: widthPercentage = " + widthPercentage + " heightPercentage = " + heightPercentage);
            Bitmap.Config config = getNonNullConfig(inBitmap);
            Bitmap toReuse = pool.get(width, height, config);
            setAlpha(inBitmap, toReuse);
            Matrix matrix = new Matrix();
            matrix.setScale(widthPercentage, heightPercentage);
            applyMatrix(inBitmap, toReuse, matrix);
            return toReuse;
        }
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }

    public void setAlpha(Bitmap inBitmap, Bitmap outBitmap) {
        outBitmap.setHasAlpha(inBitmap.hasAlpha());
    }

    @NonNull
    private Bitmap.Config getNonNullConfig(@NonNull Bitmap bitmap) {
        return bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
    }

    private void applyMatrix(@NonNull Bitmap inBitmap, @NonNull Bitmap targetBitmap, Matrix matrix) {
        BITMAP_DRAWABLE_LOCK.lock();
        try {
            Canvas canvas = new Canvas(targetBitmap);
            canvas.drawBitmap(inBitmap, matrix, DEFAULT_PAINT);
            clear(canvas);
        } finally {
            BITMAP_DRAWABLE_LOCK.unlock();
        }
    }

    private void clear(Canvas canvas) {
        canvas.setBitmap((Bitmap) null);
    }
}
