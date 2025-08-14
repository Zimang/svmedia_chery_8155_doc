package com.desaysv.libusbmedia.interfaze;

/**
 * Created by uidp5370 on 2019-6-12.
 */

public interface IGetControlTool {

    /**
     * 获取媒体控制器的接口
     *
     * @return IControlTool
     */
    IControlTool getControlTool();

    /**
     * 获取媒体状态器的接口
     *
     * @return IStatusTool
     */
    IStatusTool getStatusTool();

    /**
     * 注册媒体变化的焦点回调
     *
     * @param mediaStatusChangeListener 媒体的状态回调
     */
    void registerMediaStatusChangeListener(String className, IMediaStatusChange mediaStatusChangeListener);

    /**
     * 注销媒体变化的焦点回调
     *
     */
    void unregisterMediaStatusChangerListener(String className);

}
