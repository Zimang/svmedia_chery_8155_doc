package com.desaysv.moduleusbvideo.bean;

import android.widget.LinearLayout;

/**
 * Create by extodc87 on 2023-10-27
 * Author: extodc87
 */
public class SelectModeBean {
    private LinearLayout llFolder;

    private int videoFileListType;

    private SelectModeBean.ModeListener onClickListener;

    public LinearLayout getLlFolder() {
        return llFolder;
    }

    public void setLlFolder(LinearLayout llFolder) {
        this.llFolder = llFolder;
    }

    public int getVideoFileListType() {
        return videoFileListType;
    }

    public void setVideoFileListType(int videoFileListType) {
        this.videoFileListType = videoFileListType;
    }

    public SelectModeBean.ModeListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(SelectModeBean.ModeListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface ModeListener {
        void onClick(boolean isEnable);
    }
}
