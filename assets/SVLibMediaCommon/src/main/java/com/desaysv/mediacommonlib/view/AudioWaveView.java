package com.desaysv.mediacommonlib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.desaysv.mediacommonlib.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Android 音频跳动动画
 * https://www.jianshu.com/p/76aceacbc243
 *
 * @author extodc87
 */
public class AudioWaveView extends View {
    private static final String TAG = "AudioWaveView";

    /**
     * 正弦 规律 频次
     * 正弦有规律的获取0~1的数。
     * 默认0.3f
     */
    private static final float SING_LAW_RATE = 0.3f;
    /**
     * 默认 跳动指针的数量
     */
    private static final int DEFAULT_POINTER_NUM = 4;
    /**
     * 默认 padding 距离
     */
    private static final int DEFAULT_PADDING_SIZE = 8;
    /**
     * 默认 每个指针的宽度
     */
    private static final float DEFAULT_POINTER_WIDTH = 3f;
    /**
     * 默认指针波动速度
     */
    private static final int DEFAULT_POINTER_SPEED = 40;
    /**
     * 随机生成的线条高度随机范围
     */
    private static final int DEFAULT_HEIGHT_RANDOM = 10;
    /**
     * 圆角
     */
    private static final int DEFAULT_TOP_FILLET = 10;
    /**
     * 指针的颜色
     */
    private final int DEFAULT_POINTER_COLOR = Color.rgb(1, 249, 234);

    /**
     * 类型 底部
     */
    private final int TYPE_BOTTOM = 1;

    /**
     * 类型 中间
     */
    private final int TYPE_CENTER = 2;

    /**
     * 画笔
     */
    private Paint paint;

    /**
     * 跳动指针的集合
     */
    private List<Pointer> pointers;

    private float basePointY;

    /**
     * 指针数量
     */
    private int pointerNum;

    /**
     * 每条指针宽度
     */
    private float pointerWidth;

    /**
     * 指针间的间隙
     */
    private float pointerPadding;

    /**
     * 显示类型
     */
    private int pointerType;

    /**
     * 控制开始/停止
     * 默认执行播放
     */
    private boolean isPlaying = true;

    /**
     * 显示时是否需要自动播放
     */
    private boolean needAutoStart = true;

    /**
     * 默认的初始高度，用来在处理暂停的情况下显示规律的高矮不一
     */
    private int DEFAULT_INIT_HEIGHT = 10;

    public void setNeedAutoStart(boolean needAutoStart) {
        this.needAutoStart = needAutoStart;
    }

    /**
     * 正弦有规律
     */
    private float sineLaw;
    private float singLawRate;

    /**
     * 动画
     */
    private ObjectAnimator objectAnimator;

    private int topFillet;

    public AudioWaveView(Context context) {
        super(context);
        init(context, null);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化画笔与指针的集合
     */
    private void init(Context context, AttributeSet attrs) {
        Log.d(TAG, "init: ");
        int color = DEFAULT_POINTER_COLOR;
        int padding = dp2px(context, DEFAULT_PADDING_SIZE);
        pointerNum = DEFAULT_POINTER_NUM;
        pointerWidth = DEFAULT_POINTER_WIDTH;
        if (null != attrs) {
            @SuppressLint("Recycle") TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AudioWaveView);
            color = ta.getColor(R.styleable.AudioWaveView_pointer_color, DEFAULT_POINTER_COLOR);
            padding = (int) ta.getDimension(R.styleable.AudioWaveView_pointer_padding, padding);
            //音轨数量
            pointerNum = ta.getInt(R.styleable.AudioWaveView_pointer_num, DEFAULT_POINTER_NUM);
            // 类型，居中，或者 底部 ，默认底部
            pointerType = ta.getInt(R.styleable.AudioWaveView_pointer_type, TYPE_BOTTOM);
            //音轨宽度
            pointerWidth = ta.getDimension(R.styleable.AudioWaveView_pointer_width, DEFAULT_POINTER_WIDTH);
            //圆角
            topFillet = ta.getInt(R.styleable.AudioWaveView_pointer_top_fillet, DEFAULT_TOP_FILLET);
            //  正弦 规律 频次
            //  正弦有规律的获取0~1的数
            singLawRate = ta.getFloat(R.styleable.AudioWaveView_pointer_sing_law_rate, SING_LAW_RATE);
        }
        setPadding(padding, padding, padding, padding);
        //画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        pointers = new ArrayList<>();
    }


    /**
     * 在onLayout中做一些，宽高方面的初始化
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //获取逻辑原点的，也就是画布左下角的坐标。这里减去了paddingBottom的距离
        basePointY = getHeight() - getPaddingBottom();
        Random random = new Random();
        if (pointers != null) {
            pointers.clear();
        }

        for (int i = 0; i < pointerNum; i++) {
            /*//创建指针对象，利用0~1的随机数 乘以 可绘制区域的高度。作为每个指针的初始高度。
            //初始高度应该保持一样，不能变化，否则刚开始显示时就会有动画效果，而此时是即时绘制的，但实际动画是有 duration 的，这就会出现动画刚开始很快，然后动画恢复正常的现象
            float height = (float) (0.1 * (DEFAULT_HEIGHT_RANDOM + 1) * (getHeight() - getPaddingBottom() - getPaddingTop()));*/

            float height =  (float)(getHeight() - getPaddingBottom() - getPaddingTop())/3;
            if (i > 0){
                if (i <= pointerNum/2){
                    height = (i+1) * height;
                }else {
                    height = (pointerNum - i) * height;
                }
            }


            Pointer pointerTemp = new Pointer(height);
            pointers.add(pointerTemp);
        }
        //计算每个指针之间的间隔  总宽度 - 左右两边的padding - 所有指针占去的宽度  然后再除以间隔的数量
        pointerPadding = (getWidth() - getPaddingStart() - getPaddingEnd() - pointerWidth * pointerNum) / (pointerNum - 1);
    }

    /**
     * 开始绘画
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将x坐标移动到逻辑原点，也就是左下角
        /*
         * 逻辑坐标 原点
         */
        float basePointX = (float) getPaddingStart();
        //循环绘制每一个指针。
//        Log.d(TAG, "onDraw: pointerType = " + pointerType);
        for (int i = 0; i < pointers.size(); i++) {
            if (pointerType == TYPE_BOTTOM) {
                // 底部效果
                float top = basePointY - pointers.get(i).getHeight();
                //绘制指针圆形矩形
                RectF rectF2 = new RectF(basePointX, top, basePointX + pointerWidth, basePointY);
                canvas.drawRoundRect(rectF2, topFillet, topFillet, paint);

                // 绘制一半高度的矩形
                top = basePointY - pointers.get(i).getHeight() / 2;
                RectF rectF = new RectF(basePointX, top, basePointX + pointerWidth, basePointY);
                canvas.drawRect(rectF, paint);
            } else if (pointerType == TYPE_CENTER) {
                // 中间 效果
                int mHeight = getMeasuredHeight() / 2;
                float vHeight = pointers.get(i).getHeight() / 2;
                if (vHeight < topFillet) {
                    vHeight = topFillet;
                }
                RectF rectF2 = new RectF(
                        basePointX,
                        mHeight - vHeight,
                        basePointX + pointerWidth,
                        mHeight + vHeight);
                canvas.drawRoundRect(rectF2, topFillet, topFillet, paint);
            }
            basePointX += (pointerPadding + pointerWidth);
        }
        if (isPlaying) {
            start();
        }
    }


    /**
     * 开始播放
     */
    public void start() {
        //Log.d(TAG, "start: ");
        isPlaying = true;
        if (null == objectAnimator) {
            loadAnimator();
        }
        objectAnimator.start();
    }

    /**
     * 停止子线程，并刷新画布
     */
    public void stop() {
        Log.i(TAG, "stop: ");
        isPlaying = false;
        if (null != objectAnimator) {
            objectAnimator.cancel();
        }
    }


    /**
     * 初始化动画
     */
    private void loadAnimator() {
        PropertyValuesHolder animatorScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1f);
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, animatorScaleY);
        objectAnimator.setDuration(DEFAULT_POINTER_SPEED);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                if (!isPlaying) {
                    Log.w(TAG, "onAnimationEnd: is stop");
                    return;
                }
                // 循环对每个矩形修改高度
                for (int j = 0; j < pointers.size(); j++) { //循环改变每个指针高度
                    float rate = (float) Math.abs(Math.sin(sineLaw + j));//利用正弦有规律的获取0~1的数。
                    Pointer pointer = pointers.get(j);
                    pointer.setHeight((basePointY - getPaddingTop()) * rate); //rate 乘以 可绘制高度，来改变每个指针的高度
                }
                invalidate(); // 重新绘画
                sineLaw += singLawRate;
            }
        });
    }

    // 优化项，在不显示view的时候，停止动画
    @Override
    public void setVisibility(int visibility) {
        if (View.VISIBLE != visibility) {
            stop();
        }
        super.setVisibility(visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.i(TAG, "onDetachedFromWindow: " + needAutoStart);
        if (needAutoStart) {
            stop();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        Log.i(TAG, "onAttachedToWindow: " + needAutoStart);
        if (needAutoStart) {
            start();
        }
        super.onAttachedToWindow();
    }

    /**
     * dp转px
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources()
                .getDisplayMetrics());
    }

    /**
     * 指针类
     */
    static class Pointer {
        private float height;

        public Pointer(float height) {
            this.height = height;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }
    }
}
