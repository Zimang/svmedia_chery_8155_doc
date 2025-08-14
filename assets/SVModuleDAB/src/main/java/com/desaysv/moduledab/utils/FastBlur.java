package com.desaysv.moduledab.utils;

import static android.graphics.Bitmap.Config.ARGB_8888;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Util code copy from https://github.com/ddwhan0123/BlurPopupWindow. Thanks for sharing
 * Created by jiajiewang on 16/7/30.
 * 高斯模糊算法及截屏图片生成
 */
public class FastBlur {
    public static final String TAG = "FastBlur";
    public static int DEFAULT_ZOOM_INT = 8;

    /**
     * 高斯模糊处理
     */
    public static Bitmap fastBlur(Bitmap sbitmap, float radiusf) {
        if (sbitmap == null)
            return null;
        int desWidth = sbitmap.getWidth() / DEFAULT_ZOOM_INT;
        int desHeight = sbitmap.getHeight() / DEFAULT_ZOOM_INT;

        //先缩放图片，增加模糊速度
        Bitmap bitmap = Bitmap.createScaledBitmap(sbitmap,
                desWidth, desHeight, false);

        int radius = (int) radiusf;
        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }



    /**
     * 合成蒙黑图片
     * @param background
     * @param dimAmount 变黑程度
     * @return
     */
    public static Bitmap getDimBitmap(Bitmap background,float dimAmount){
        if( background == null ) {
            return null;
        }

        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newbmp);
        //draw bg into
        cv.drawBitmap(background, 0, 0, null);//在 0，0坐标开始画入bg
        //draw fg into
        cv.drawColor(Color.argb(dimAmount,0,0,0));
        //save all clip
        cv.save();//保存
//        cv.save(Canvas.ALL_SAVE_FLAG);//保存
        //store
        cv.restore();//存储
        return newbmp;
    }


    /**
     * 只进行截屏操作，返回Bitmap
     * @return
     */
    public static Bitmap shotScreenBitmap(){
        //反射实现
        DisplayMetrics mDisplayMetrics = new DisplayMetrics(); //获取屏幕宽高
        int[] dims = { mDisplayMetrics.widthPixels,
                mDisplayMetrics.heightPixels };
        Bitmap screenBmp = null;   //屏幕截图
        Bitmap blurBmp = null;   //最终处理后的图片
        try {
            Class<?> demo = Class.forName("android.view.SurfaceControl");
            Method method = demo.getMethod("screenshot", Rect.class,int.class,int.class,int.class);   //framework隐藏方法
            screenBmp = (Bitmap) method.invoke(null,
                    new Object[] {new Rect(0,0,dims[0],dims[1]),dims[0],dims[1],0});
            screenBmp = screenBmp.copy(ARGB_8888,true);


        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG,"截图失败1："+e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(TAG,"截图失败2："+e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG,"截图失败3："+e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG,"截图失败4："+e);
        }
        return screenBmp;
    }

    /**
     * 截取屏幕并进行模糊以及蒙黑操作，返回Bitmap
     *
     * @param blurRadius 模糊半径
     * @param dimAmount 蒙黑程度 0f - 1f全黑
     * @return 处理后的bitmap
     */
    public static Bitmap shotScreenBlurAndDimBitmap(int blurRadius,float dimAmount){
        Bitmap screenBmp = shotScreenBitmap();   //屏幕截图
        Bitmap blurBmp = null;   //最终处理后的图片

        try{
            blurBmp  = FastBlur.fastBlur(screenBmp, blurRadius);
            if(dimAmount > 0 && dimAmount<=1){
                blurBmp = FastBlur.getDimBitmap(blurBmp,dimAmount);     //叠加变暗
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"截图模糊失败："+e);
        } finally {
            if(screenBmp != null && !screenBmp.isRecycled())
                screenBmp.recycle();
        }
        return blurBmp;
    }


    /**
     * 截取屏幕并进行模糊以及蒙黑操作，返回BitmapDrawable
     *
     * @param blurRadius 模糊半径
     * @param dimAmount 蒙黑程度 0f - 1f全黑
     * @return 处理后的BitmapDrawable
     */
    public static BitmapDrawable shotScreenBlurAndDimDrawable(Context context,int blurRadius,float dimAmount){
        Bitmap bitmap = shotScreenBlurAndDimBitmap(blurRadius,dimAmount);
        if(null == bitmap || bitmap.isRecycled()){
            return null;
        }
        return new BitmapDrawable(context.getResources(),bitmap);
    }
}
