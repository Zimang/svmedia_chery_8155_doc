/**
 * created by ZNB on 2023-01-03
 * 造个轮子：圆形的 ImageView
 */
package com.desaysv.mediacommonlib.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.desaysv.mediacommonlib.R;

public class CircleImageView extends AppCompatImageView {


    private Paint paint;
    private Matrix matrix;
    private BitmapShader bitmapShader;
    private ScaleType scaleType = ScaleType.FIT_XY;

    public CircleImageView(@NonNull Context context) {
        this(context,null);
    }

    public CircleImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

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
        if(scaleType == ScaleType.FIT_XY) {
            matrix.setScale(getWidth() * 1.0f / bitmap.getWidth(),getHeight() * 1.0f / bitmap.getHeight());
        } else {
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
        }
        bitmapShader.setLocalMatrix(matrix);
        paint.setShader(bitmapShader);
        int radius = getWidth()>getHeight()? getHeight()/2 : getWidth()/2;
        canvas.drawCircle(getWidth()/2,getHeight()/2,radius,paint);

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
