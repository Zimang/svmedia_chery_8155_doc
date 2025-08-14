package com.desaysv.mediacommonlib.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


/**
 * 带跑马灯效果的TextView
 * Created by uidq0303 on 2017/6/8.
 */

@SuppressLint("AppCompatCustomView")
public class MarqueeTextView extends TextView {
    private static final String TAG = "MarqueeTextView";

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean needMarquee = true;
    public void setNeedMarquee(boolean needMarquee){
        this.needMarquee = needMarquee;
    }
    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return needMarquee && true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(true, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        //super.onWindowFocusChanged(hasWindowFocus);
    }

    /**
     * Set a new text with transitive animation
     *
     * @param resId     the resource id of text
     * @param startAnim the animation of text go out
     * @param endAnim   the animation of text come in
     */
    public void setTextWithAnim(final int resId, int startAnim, final int endAnim) {
        Animation startAM = AnimationUtils.loadAnimation(getContext(), startAnim);
        startAM.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setText(resId);
                Animation endAM = AnimationUtils.loadAnimation(getContext(), endAnim);
                startAnimation(endAM);
            }
        });
        this.startAnimation(startAM);
    }

    /**
     * Set a new text with transitive animation
     *
     * @param str       the string of text
     * @param startAnim the animation of text go out
     * @param endAnim   the animation of text come in
     */
    public void setTextWithAnim(final String str, int startAnim, final int endAnim) {
        Animation startAM = AnimationUtils.loadAnimation(getContext(), startAnim);
        startAM.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setText(str);
                Animation endAM = AnimationUtils.loadAnimation(getContext(), endAnim);
                startAnimation(endAM);
            }
        });
        this.startAnimation(startAM);
    }
}
