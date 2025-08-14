package com.desaysv.usbpicture.constant;

/**
 * Created by ZNB on 2022-05-09
 * 定义语义操作的action
 * 后续根据语音给的协议表修正
 */
public class VRAction {

    /**
     * 用于区分一级功能，避免二级功能的action相同时不好处理
     * ：控制图片
     */
    public static final String KEY_CONTROL_PICTURE = "controlPicture";

    /**
     * 用于区分一级功能，避免二级功能的action相同时不好处理
     * ：浏览模式
     */
    public static final String KEY_CONTROL_VIEW = "controlView";


    /**
     * 播放操作
     */
    public static final String ACTION_PLAY = "PLAY";

    /**
     * 暂停操作
     */
    public static final String ACTION_PAUSE = "PAUSE";

    /**
     * 上一曲操作
     */
    public static final String ACTION_PRE = "PREVIOUS";

    /**
     * 下一曲操作
     */
    public static final String ACTION_NEXT = "NEXT";

    /**
     * 缩小图片
     */
    public static final String ACTION_SHRINK = "SHRINK";

    /**
     * 放大图片
     */
    public static final String ACTION_ZOOM = "ZOOM";

    /**
     * 打开操作
     */
    public static final String ACTION_OPEN = "OPEN";

    /**
     * 退出操作
     */
    public static final String ACTION_CLOSE = "CLOSE";

    /**
     * 打开浏览界面
     */
    public static final String TYPE_BROWSE = "BROWSE";

    /**
     * 打开列表界面
     */
    public static final String TYPE_IMAGE_LIST = "IMAGE_LIST";

}
