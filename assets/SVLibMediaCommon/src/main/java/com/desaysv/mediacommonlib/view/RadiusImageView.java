/**
 * created by ZNB on 2023-01-03
 * 造个轮子：自定义圆角的 ImageView
 */
package com.desaysv.mediacommonlib.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.desaysv.mediacommonlib.R;

public class RadiusImageView extends AppCompatImageView {

    private float radius;

    private Paint paint;
    private Matrix matrix;
    private BitmapShader bitmapShader;

    public RadiusImageView(@NonNull Context context) {
        this(context,null);
    }

    public RadiusImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RadiusImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadiusImageView);

        radius = typedArray.getDimensionPixelOffset(R.styleable.RadiusImageView_radius,0);

        typedArray.recycle();

        matrix = new Matrix();
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (getDrawable() == null){
            return;
        }

        Bitmap bitmap = drawableToBitmap(getDrawable());
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);

        float scale = 1.0f;
        float dx = 0;
        float dy = 0;

        if (bitmap.getWidth() == getWidth() && bitmap.getHeight() == getHeight()){

        }else {
            scale = Math.max(getWidth() * 1.0f / bitmap.getWidth(), getHeight() * 1.0f / bitmap.getHeight());
        }
        if (bitmap.getWidth() * getHeight() > getWidth() * bitmap.getHeight()) {
            dx = (getWidth() - bitmap.getWidth() * scale) * 0.5f;
        } else {
            dy = (getHeight() - bitmap.getHeight() * scale) * 0.5f;
        }
        matrix.setScale(scale,scale);
        //图片居中
        matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        bitmapShader.setLocalMatrix(matrix);
        paint.setShader(bitmapShader);
        canvas.drawRoundRect(new RectF(0,0,getWidth(),getHeight()),radius,radius,paint);

//        super.onDraw(canvas);
    }

    private Bitmap drawableToBitmap(Drawable drawable){
        if (drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            return bitmapDrawable.getBitmap();
        }
        int w = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : getWidth();
        int h = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,w,h);
        drawable.draw(canvas);
        return bitmap;
    }
}
