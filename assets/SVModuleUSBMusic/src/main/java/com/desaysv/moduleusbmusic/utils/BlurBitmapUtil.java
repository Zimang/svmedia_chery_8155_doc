package com.desaysv.moduleusbmusic.utils;

import static android.graphics.Bitmap.Config.ARGB_8888;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Time: 2020-12-24
 * Author: EXTODC24
 * Description: 获取当前高斯模糊截图
 */
public class BlurBitmapUtil {
    private static final String TAG = "BlurBitmapUtil";
    private Context context;

    private static final int FINISH_BITMAP = 1;
    // 通过资源id来转换为高斯模糊图片
    private static final int FINISH_BLUR_IN_RES = FINISH_BITMAP + 1;

    private IFinishBlurBitmap iFinishBlurBitmap;

    //子线程
    private HandlerThread mHandlerThread;
    private Handler mainHandler;
    private Bitmap blurBitmap;

    public BlurBitmapUtil(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        Log.d(TAG, "init: ");
        mHandlerThread = new HandlerThread("mHandlerThread");
        mHandlerThread.start();
        mainHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case FINISH_BITMAP:
                        Log.d(TAG, "handleMessage: FINISH_BITMAP");
                        generateBlurBitmap();
                        break;
                    case FINISH_BLUR_IN_RES:
                        generateBlurBitmap((int) msg.obj);
                        break;
                    default:
                        Log.d(TAG, "handleMessage: DEFAULT");
                        break;
                }
            }
        };
    }

    /**
     * 开始
     */
    public void start() {
        Log.d(TAG, "start: ");
        mainHandler.sendEmptyMessage(FINISH_BITMAP);
    }

    /**
     * 不需要截图，直接传资源id
     *
     * @param resId
     */
    public void start(int resId) {
        Log.d(TAG, "start: ");
        mainHandler.sendMessage(mainHandler.obtainMessage(FINISH_BLUR_IN_RES, resId));
    }

    /**
     * 生成模糊背景
     */
    private void generateBlurBitmap() {
        Log.d(TAG, "generateBlurBitmap: ");
        Bitmap drawingCache = shotScreenBitmap();
        if (drawingCache == null) {
            Log.d(TAG, "generateBlurBitmap: drawingCache is null");
            return;
        }
        //ARGB_4444
        //Deprecated
        //Because of the poor quality of this configuration, it is advised to use ARGB_8888 instead.
        Bitmap bmp = drawingCache.copy(Bitmap.Config.ARGB_8888, true);
        blurBitmap = dealBitmap(context, bmp, 5);
        bmp.recycle();
        // 结束后通知主线程
        if (iFinishBlurBitmap != null) {
            iFinishBlurBitmap.finish(blurBitmap);
        }
    }

    /**
     * 生成模糊背景
     */
    private void generateBlurBitmap(int resId) {
        Log.d(TAG, "generateBlurBitmap: ");
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resId);
        blurBitmap = dealBitmap(context, bmp, 5);
        bmp.recycle();
        // 结束后通知主线程
        if (iFinishBlurBitmap != null) {
            iFinishBlurBitmap.finish(blurBitmap);
        }
    }

    /**
     * 只进行截屏操作，返回Bitmap
     *
     * @return Bitmap
     */
    public static Bitmap shotScreenBitmap() {
        //反射实现
        DisplayMetrics mDisplayMetrics = new DisplayMetrics(); //获取屏幕宽高
        int[] dims = {mDisplayMetrics.widthPixels,
                mDisplayMetrics.heightPixels};
        Bitmap screenBmp = null;   //屏幕截图
        Bitmap blurBmp = null;   //最终处理后的图片
        try {
            Class<?> demo = Class.forName("android.view.SurfaceControl");
            Method method = demo.getMethod("screenshot", Rect.class, int.class, int.class, int.class);   //framework隐藏方法
            screenBmp = (Bitmap) method.invoke(null,
                    new Object[]{new Rect(0, 0, dims[0], dims[1]), dims[0], dims[1], 0});
            screenBmp = screenBmp.copy(ARGB_8888, true);
            Log.d(TAG, "shotScreenBitmap: finnish");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 1 ：" + e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 2 ：" + e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 3 ：" + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "shotScreenBitmap error 4 ：" + e);
        }
        return screenBmp;
    }

    /**
     * 设置回调信息
     *
     * @param iFinishBlurBitmap iFinishBlurBitmap
     */
    public void setFinishBlurBitmap(IFinishBlurBitmap iFinishBlurBitmap) {
        this.iFinishBlurBitmap = iFinishBlurBitmap;
    }

    public interface IFinishBlurBitmap {
        void finish(Bitmap bitmap);
    }

    // 图片缩放比例(即模糊度)
    private static final float BITMAP_SCALE = 0.125f;

    /**
     * @param context 上下文对象
     * @param image   需要模糊的图片
     * @return 模糊处理后的Bitmap
     */
    private Bitmap dealBitmap(Context context, Bitmap image, float blurRadius) {
        // 计算图片缩小后的长宽
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);
        // 将缩小后的图片做为预渲染的图片
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        // 创建一张渲染后的输出图片
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);
        // 创建一个模糊效果的RenderScript的工具对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius);
        // 设置blurScript对象的输入内存
        blurScript.setInput(tmpIn);
        // 将输出数据保存到输出内存中
        blurScript.forEach(tmpOut);
        // 将数据填充到Allocation中
        tmpOut.copyTo(outputBitmap);
        tmpOut.destroy();
        tmpIn.destroy();
        blurScript.destroy();
        rs.destroy();
        return outputBitmap;
    }

    /**
     * 页面销毁时需要调用
     */
    public void onDestroy() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        iFinishBlurBitmap = null;
    }
}
