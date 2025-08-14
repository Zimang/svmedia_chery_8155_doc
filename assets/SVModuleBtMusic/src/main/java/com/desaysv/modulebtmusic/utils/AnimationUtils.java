package com.desaysv.modulebtmusic.utils;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class AnimationUtils {
    private static final String TAG = "AnimationUtils";

    private static final int STATE_PLAYING = 1; // 播放
    private static final int STATE_PAUSE = 2; // 暂停
    private static final int STATE_STOP = 3; // 停止

    /**
     * 歌曲专辑封面旋转动画
     */
    private static ObjectAnimator rotateAnim;
    private static int rotateAnimState = STATE_STOP;
    private static final Keyframe rotateAnimKeyframe1 = Keyframe.ofFloat(0f, 0);
    private static final Keyframe rotateAnimKeyframe2 = Keyframe.ofFloat(0.25f, 90);
    private static final Keyframe rotateAnimKeyframe3 = Keyframe.ofFloat(0.5f, 180);
    private static final Keyframe rotateAnimKeyframe4 = Keyframe.ofFloat(0.75f, 270);
    private static final Keyframe rotateAnimKeyframe5 = Keyframe.ofFloat(1f, 360);
    private static final PropertyValuesHolder rotateAnimPropertyValuesHolder = PropertyValuesHolder
            .ofKeyframe("rotation", rotateAnimKeyframe1, rotateAnimKeyframe2,
                    rotateAnimKeyframe3, rotateAnimKeyframe4, rotateAnimKeyframe5);

    /**
     * 专辑播放指针旋转动画
     */
    private static ObjectAnimator pointerRotateAnim;
    /**
     * 专辑播放指针旋转动画(左右镜像)
     */
    private static ObjectAnimator pointerRotateAnimFlip;

    public static void startRotateAnim(View view) {
        if (rotateAnim == null) {
            rotateAnim = ObjectAnimator.ofPropertyValuesHolder(view, rotateAnimPropertyValuesHolder);
            rotateAnim.setDuration(9000);
            rotateAnim.setInterpolator(new LinearInterpolator());//动画时间线性渐变（匀速）
            rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);//循环
            rotateAnim.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (rotateAnimState == STATE_STOP) {
            rotateAnim.start();
            rotateAnimState = STATE_PLAYING;
        } else if (rotateAnimState == STATE_PAUSE) {
            rotateAnim.resume();
            rotateAnimState = STATE_PLAYING;
        } else if (rotateAnimState == STATE_PLAYING) {
            rotateAnim.pause();
            rotateAnimState = STATE_PAUSE;
        }
    }

    public static void pauseRotateAnim() {
        if (rotateAnim != null) {
            rotateAnim.pause();
            rotateAnimState = STATE_PAUSE;
        }
    }

    public static void stopRotateAnim() {
        if (rotateAnim != null) {
            rotateAnim.end();
            rotateAnim.cancel();
            rotateAnim = null;
            rotateAnimState = STATE_STOP;
        }
    }

    public static void startPointerRotateAnim(View view, boolean isPlaying) {
        if (pointerRotateAnim == null) {
            pointerRotateAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, -20.0f);
            pointerRotateAnim.setDuration(500);
            pointerRotateAnim.setInterpolator(new LinearInterpolator());
        }
        if (isPlaying) {
            pointerRotateAnim.setFloatValues((Float) pointerRotateAnim.getAnimatedValue(), -20);
        } else {
            pointerRotateAnim.setFloatValues((Float) pointerRotateAnim.getAnimatedValue(), 0);
        }
        pointerRotateAnim.start();
        if (pointerRotateAnim.isStarted()) {
            pointerRotateAnim.resume();
        } else {
            pointerRotateAnim.start();
        }
    }

    public static void pausePointerRotateAnim() {
        if (pointerRotateAnim != null) {
            pointerRotateAnim.pause();
        }
    }

    public static void stopPointerRotateAnim() {
        if (pointerRotateAnim != null) {
            pointerRotateAnim.end();
            pointerRotateAnim.cancel();
            pointerRotateAnim = null;
        }
    }

    public static void startPointerRotateAnimFlip(View view, boolean isPlaying) {
        if (pointerRotateAnimFlip == null) {
            pointerRotateAnimFlip = ObjectAnimator.ofFloat(view, "rotation", 0f, 20.0f);
            pointerRotateAnimFlip.setDuration(500);
            pointerRotateAnimFlip.setInterpolator(new LinearInterpolator());
        }
        if (isPlaying) {
            pointerRotateAnimFlip.setFloatValues((Float) pointerRotateAnimFlip.getAnimatedValue(), 20);
        } else {
            pointerRotateAnimFlip.setFloatValues((Float) pointerRotateAnimFlip.getAnimatedValue(), 0);
        }
        pointerRotateAnimFlip.start();
        if (pointerRotateAnimFlip.isStarted()) {
            pointerRotateAnimFlip.resume();
        } else {
            pointerRotateAnimFlip.start();
        }
    }

    public static void pausePointerRotateAnimFlip() {
        if (pointerRotateAnimFlip != null) {
            pointerRotateAnimFlip.pause();
        }
    }

    public static void stopPointerRotateAnimFlip() {
        if (pointerRotateAnimFlip != null) {
            pointerRotateAnimFlip.end();
            pointerRotateAnimFlip.cancel();
            pointerRotateAnimFlip = null;
        }
    }
}
