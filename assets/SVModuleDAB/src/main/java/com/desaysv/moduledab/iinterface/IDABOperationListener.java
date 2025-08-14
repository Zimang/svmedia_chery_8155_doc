package com.desaysv.moduledab.iinterface;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;

/**
 * created by ZNB on 2022-10-17
 * 封装界面操作DAB Item 的接口
 */
public interface IDABOperationListener {

    /**
     * DAB列表点击某项时触发
     * @param radioMessage
     */
    default void onClickDAB(RadioMessage radioMessage) {}

    /**
     * DAB列表点击收藏时触发
     * @param radioMessage
     */
    default void onCollectDAB(RadioMessage radioMessage) {}

    /**
     * DAB列表点击某项时触发
     * @param radioMessage
     */
    default void onClickDAB(RadioMessage radioMessage, boolean enterNextList) {}

    /**
     * EPG 列表点击某项时触发，包含日期或者详情
     * @param dabepgSchedule
     */
    default void onClickEPG(DABEPGSchedule dabepgSchedule, boolean isDate) {}
}
