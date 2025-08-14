package com.desaysv.moduleusbpicture.listener;

/**
 * Created by LZM on 2020-4-4
 * Comment
 */
public interface IPhotoControl {

    /**
     * 打开指定位置的图片
     *
     * @param position 指定的位置
     */
    void openPhoto(int position);

    /**
     * 下一张图片
     */
    void nextPhoto();

    /**
     * 上一张图片
     */
    void prePhoto();

    /**
     * 向左旋转图片
     */
    void spinLeftPhoto();

    /**
     * 向右旋转图片
     */
    void spinRightPhoto();

    /**
     * 放大图片
     */
    void bigPhoto();

    /**
     * 缩小图片
     */
    void smallPhoto();

    /**
     * 重置图片大小，将图片修改为默认大小
     */
    void resetPhoto();

    /**
     * 开始幻灯片
     */
    void startSlideshow();

    /**
     * 停止幻灯片
     */
    void stopSlideshow();


    /**
     * 开始或者停止幻灯片
     */
    void startOrStopSlideshow();

    /**
     * 设置幻灯片的播放时间
     *
     * @param time 播放时间
     */
    void setSlidesTime(int time);

    /**
     * 获取幻灯片的播放时间
     *
     * @return time 播放时间
     */
    int getSlidesTime();

}
