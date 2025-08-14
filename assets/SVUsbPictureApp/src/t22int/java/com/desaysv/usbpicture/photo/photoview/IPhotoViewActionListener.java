package com.desaysv.usbpicture.photo.photoview;

import android.view.View;

public interface IPhotoViewActionListener {
    /**
     * 是否点击View
     * @param isClick
     */
    void onClick(boolean isClick);

    /**
     * 缩放状态，最大/最小 时置灰对应按钮
     * @param scaleType
     * @param value
     */
    void onScaleState(int scaleType, boolean value);

    /**
     * 当前的View发生变化
     * @param position
     */
    void onPrimaryItemChanged(int position);
}
