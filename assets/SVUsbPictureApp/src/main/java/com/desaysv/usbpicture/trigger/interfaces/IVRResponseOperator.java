package com.desaysv.usbpicture.trigger.interfaces;

/**
 * Created by ZNB on 2022-05-09
 * 响应 语义操作 的接口
 * 根据需要实现其中的部分
 *
 * 后续可以根据需要直接扩展接口
 */
public interface IVRResponseOperator {

    /**
     * 暂停或者播放幻灯片
     */
    default void playOrPause(){

    }

    /**
     * 播放幻灯片
     */
    default void play(){

    }

    /**
     * 暂停幻灯片
     */
    default void pause(){

    }

    /**
     * 上一张图片
     */
    default void pre(){

    }

    /**
     * 下一张图片
     */
    default void next(){

    }

    /**
     * 进入浏览模式
     */
    default void goToPreView(){

    }

    /**
     * 退出浏览模式
     */
    default void exitPreView(){

    }

    /**
     * 缩小图片
     */
    default void shrink(){

    }

    /**
     * 放大图片
     */
    default void zoom(){

    }

    /**
     * 进入图片列表
     */
    default void goToImageList(){

    }

    /**
     * 退出图片列表
     */
    default void exitImageList(){

    }
}
