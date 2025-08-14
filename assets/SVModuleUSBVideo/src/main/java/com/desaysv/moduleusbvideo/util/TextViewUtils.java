package com.desaysv.moduleusbvideo.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.desaysv.moduleusbvideo.R;

/**
 * 用于textView 下，增加图片 绘制显示
 * Create by extodc87 on 2022-11-1
 * Author: extodc87
 */
public class TextViewUtils {
    private static final String TAG = "TextViewUtils";

    private static final class InstanceHolder {
        static final TextViewUtils instance = new TextViewUtils();
    }

    public static TextViewUtils getInstance() {
        return InstanceHolder.instance;
    }

    private TextViewUtils() {
        Log.d(TAG, "TextViewUtils: ");
    }


    public void setHeightLightItem(final Context mContext, final TextView view, final int resId) {
        //先设定一个默认宽度，在页面上显示占位，避免view加载完成后设置后底部亮度会有一个平移操作
        int buttonWidth = 155;
        setCompoundDrawables(mContext, view, resId, buttonWidth);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                Log.d(TAG, "onGlobalLayout: width : " + view.getMeasuredWidth());
                setCompoundDrawables(mContext, view, resId, view.getMeasuredWidth());
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setCompoundDrawables(Context mContext, TextView view, int resId, int measuredWidth) {
        if (null == mContext) {
            return;
        }
        Drawable drawable = ContextCompat.getDrawable(mContext, resId);
        if (null == drawable) {
            return;
        }
        int integer = mContext.getResources().getInteger(R.integer.tab_bg_height);
        Log.i(TAG, "setCompoundDrawables: integer: " + integer);
        drawable.setBounds(0, 0, measuredWidth, drawable.getMinimumHeight() - integer);
        Drawable[] compoundDrawables = view.getCompoundDrawables();
        view.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], drawable);
        //[SVAppMedia][Logic][本地音乐 多余UI显示][5123][Testable]
        // 使用相同尺寸的背景图
        view.setCompoundDrawablePadding(-65);
//        Log.d(TAG, "setCompoundDrawables: " + view.getText().toString());
    }
}
