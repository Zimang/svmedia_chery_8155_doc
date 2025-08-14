package com.desaysv.moduleusbvideo.util;

import android.util.Log;
import android.view.TextureView;
import android.widget.RelativeLayout;

/**
 * Create by extodc87 on 2022-11-7
 * Author: extodc87
 */
public class VideoSizeUtils {
    private static final String TAG = "VideoPreview";

    /**
     * 获取当前视频分变率大小
     * 并且根据比例进行转换
     *
     * @param videoHeight 视频高度
     * @param videoWidth  视频宽度
     */
    public static int[] getVideoWH(RelativeLayout rootView, int videoHeight, int videoWidth) {
        double currentViewGroupWidth = rootView.getWidth();
        double currentViewGroupHeight = rootView.getHeight();
        //避免分母为0，报错
        if (videoHeight == 0) {
            videoHeight = (int) currentViewGroupHeight;
        }
        if (videoWidth == 0) {
            videoWidth = (int) currentViewGroupWidth;
        }
        Log.d(TAG, "getVideoWH vHeight==" + videoHeight + "###vWidth==" + videoWidth);
        //每次查询需要设置videoView的大小,计算出缩放后的大小
        //小窗口是760X580，需要按比例转换进行缩放
        int mVideoFullHeight;
        int mVideoFullWidth;
        //横屏的视屏
        if (videoWidth > videoHeight) {
            //如果是偏宽的视频，以宽度为基础控制高度
            if ((videoWidth / currentViewGroupWidth) >= (videoHeight / currentViewGroupHeight)) {
                mVideoFullHeight = (int) (currentViewGroupWidth / videoWidth * videoHeight);
                mVideoFullWidth = (int) currentViewGroupWidth;
            } else {
                //如果是偏高的视频，以高度为基础控制宽度
                mVideoFullHeight = (int) currentViewGroupHeight;
                mVideoFullWidth = (int) (currentViewGroupHeight / videoHeight * videoWidth);
            }
        } else {
            //正方形或者竖屏的视屏
            mVideoFullHeight = (int) currentViewGroupHeight;
            mVideoFullWidth = (int) (currentViewGroupHeight / videoHeight * videoWidth);
        }
        //这里需要设定拉伸的大小，回调给视频
        Log.d(TAG, "getVideoWH: mVideoFullHeight = " + mVideoFullHeight + " mVideoFullWidth = " + mVideoFullWidth);
        return new int[]{mVideoFullWidth, mVideoFullHeight};
    }

    /**
     * 更新TextureView的大小
     *
     * @param width  需要变换的视频宽度
     * @param height 需要变换的视频的高度
     */
    public static void updateTextureViewSize(TextureView ttvVideo, int width, int height) {
        Log.d(TAG, "updateTextureViewSize: width = " + width + " height = " + height);
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(width, height);
        rp.setMargins(0, 0, 0, 0);
        //设置居中显示
        rp.addRule(RelativeLayout.CENTER_IN_PARENT);
        ttvVideo.setLayoutParams(rp);
    }

}
