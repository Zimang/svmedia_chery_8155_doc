package com.desaysv.usbpicture.trigger.interfaces;

/**
 * Created by ZNB on 2022-06-23
 * 响应 RVC 的接口
 * 根据需要实现其中的部分
 *
 * 后续可以根据需要直接扩展接口
 */
public interface IRVCResponseOperator {

    void updateWithRVC(int status);
}
