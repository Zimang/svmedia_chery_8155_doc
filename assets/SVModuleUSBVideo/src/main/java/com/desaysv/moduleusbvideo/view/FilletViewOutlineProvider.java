package com.desaysv.moduleusbvideo.view;

import android.graphics.Outline;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 *  view 的圆角
 *  view.setOutlineProvider(new TextureVideoViewOutlineProvider(Constant.dp2px(this, 12)));
 *  view.setClipToOutline(true);
 */
public class FilletViewOutlineProvider extends ViewOutlineProvider {
    private static final String TAG = "TextureVideoViewOutline";

    private final float mRadius;

    public FilletViewOutlineProvider(float radius) {
        this.mRadius = radius;
        Log.d(TAG, "TextureVideoViewOutlineProvider: mRadius = " + mRadius);
    }

    @Override
    public void getOutline(View view, Outline outline) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        int leftMargin = 0;
        int topMargin = 0;
        Rect selfRect = new Rect(leftMargin, topMargin,
                rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
        outline.setRoundRect(selfRect, mRadius);
    }
}