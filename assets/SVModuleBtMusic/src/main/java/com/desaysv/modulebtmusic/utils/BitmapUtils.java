package com.desaysv.modulebtmusic.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {


    public static Bitmap createReflectedBitmap(Context context, int imgId) {
        Bitmap originalImage = BitmapFactory.decodeResource(context.getResources(), imgId);
        // 反射图片和原始图片中间的间距
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int shadowHeight = height;
        //通过矩阵对图像进行变换
        Matrix matrix = new Matrix();
        // 第一个参数为1，表示x方向上以原比例为准保持不变，正数表示方向不变。
        // 第二个参数为-1，表示y方向上以原比例为准保持不变，负数表示方向取反。
        matrix.preScale(1, -1); // 实现图片的反转

        // 创建反转后的图片Bitmap对象，图片高是原图的一半
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                0, width, shadowHeight, matrix, false);

        // 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        Path mPath = new Path();
        mPath.addCircle(width / 2, height / 2, height / 2, Path.Direction.CW);
        canvas.clipPath(mPath);
        // 将反转后的图片画到画布中
        canvas.drawBitmap(reflectionImage, 0, 0, null);

        Paint paint = new Paint();
        // 创建线性渐变LinearGradient 对象。
        int color[] = {0xfffffff, 0x10ffffff, 0x00ffffff};

        LinearGradient shader = new LinearGradient(0, 0, 0, bitmapWithReflection.getHeight()
                , color, null, Shader.TileMode.CLAMP);


        paint.setShader(shader);
        // 倒影遮罩效果
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, 0, width, bitmapWithReflection.getHeight(), paint);

        return bitmapWithReflection;
    }

    /**
     * @param originalImage 图片资源id
     * @return Bitmap 带倒影的Bitmap
     */
    public static Bitmap createReflectedBitmap(Context context, Bitmap originalImage) {

        // 反射图片和原始图片中间的间距
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int shadowHeight = height / 2;
        //通过矩阵对图像进行变换
        Matrix matrix = new Matrix();
        // 第一个参数为1，表示x方向上以原比例为准保持不变，正数表示方向不变。
        // 第二个参数为-1，表示y方向上以原比例为准保持不变，负数表示方向取反。
        matrix.preScale(1, -1); // 实现图片的反转

        // 创建反转后的图片Bitmap对象，图片高是原图的一半
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                shadowHeight, width, shadowHeight, matrix, false);

        // 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);

        Path mPath = new Path();
        mPath.addCircle(width / 2, height / 2, height / 2, Path.Direction.CW);
        canvas.clipPath(mPath);
        // 将反转后的图片画到画布中
        canvas.drawBitmap(reflectionImage, 0, 0, null);

        Paint paint = new Paint();
        // 创建线性渐变LinearGradient 对象。
        LinearGradient shader = new LinearGradient(0, originalImage
                .getHeight(), 0, bitmapWithReflection.getHeight()
                , 0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);

        paint.setShader(shader);
        // 倒影遮罩效果
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, 0, width, bitmapWithReflection.getHeight(), paint);

        return bitmapWithReflection;
    }

    /**
     * @param originalImage 图片资源id
     * @return Bitmap 带倒影的Bitmap
     */
    public static Bitmap createCircleBitmap(Bitmap originalImage) {

        // 反射图片和原始图片中间的间距
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);

        Path mPath = new Path();
        mPath.addCircle(width / 2, height / 2, height / 2, Path.Direction.CW);
        canvas.clipPath(mPath);
        // 将反转后的图片画到画布中
        canvas.drawBitmap(originalImage, 0, 0, null);

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(0, 0, width, bitmapWithReflection.getHeight(), paint);

        return bitmapWithReflection;
    }

    //将bitmap调整到指定大小
    public static Bitmap sizeBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {//这时候origin还有吗？
            origin.recycle();
        }
        return newBM;
    }

    //按比例缩放
    public static Bitmap scaleBitmap(Bitmap origin, float scale) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(scale, scale);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap cropBitmap(Bitmap bitmap) {//从中间截取一个正方形
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长

        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - cropWidth) / 2,
                (bitmap.getHeight() - cropWidth) / 2, cropWidth, cropWidth);
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {//把图片裁剪成圆形
        if (bitmap == null) {
            return null;
        }
        bitmap = cropBitmap(bitmap);//裁剪成正方形
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            float roundPx = 0.0f;
            roundPx = bitmap.getWidth();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }

    /**
     * 获取Bitmap圆角
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            final float roundPx = 2;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    /**
     * 彩图转换成灰色图片
     *
     * @param img
     * @return
     */
    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey;
                if (pixels[width * i + j] == 0) {
                    continue;
                } else grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.44 + (float) green * 0.45 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        //创建空的bitmap时，格式一定要选择ARGB_4444,或ARGB_8888,代表有Alpha通道，RGB_565格式的不显示灰度
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }


    /**
     * Bitmap cast to byte array
     *
     * @param bitmap byte[]
     */
    public static byte[] bitmapCastToByteArray(Bitmap bitmap) {
        Bitmap temp = bitmap;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        temp.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }

    /**
     * Byte array cast to bitmap
     *
     * @param img Bitmap
     */
    public static Bitmap byteArrayCastToBitmap(byte[] img) {
        if (img == null || img.length <= 0) {
            return null;
        }
        byte[] temp = img;
        Bitmap bmpout = BitmapFactory.decodeByteArray(temp, 0, temp.length);
        return bmpout;
    }

    /**
     * Bitmap cast to Drawable
     *
     * @param bitmap Drawable
     */
    public static Drawable bitmapCastToDrawable(Resources res, Bitmap bitmap) {
        Bitmap temp = bitmap;
        BitmapDrawable drawable = new BitmapDrawable(res, temp);
        return drawable;
    }

    /**
     * 将图片转换为圆形
     *
     * @param bitmap Bitmap
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float radius;//x方向半径
        float left, top, right, bottom; //源图片rect
        float dst_left, dst_top, dst_right, dst_bottom; //目标图片rect
        if (width <= height) {
            radius = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            radius = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output); //创建一个bitmap画布

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);  //对原图片裁剪计算后的新区域
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom); //目标区域
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);
        paint.setColor(color);

        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, radius, radius, paint); //将画布裁剪为一个圆形区域

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

}



