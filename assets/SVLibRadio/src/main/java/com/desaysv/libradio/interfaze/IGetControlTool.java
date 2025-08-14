package com.desaysv.libradio.interfaze;

/**
 * @author uidp5370
 * @date 2019-6-12
 * Comment 获取数据控制的接口
 */

public interface IGetControlTool {

    /**
     * 获取收音的控制器
     *
     * @return IControlTool 收音的控制器
     */
    IControlTool getControlTool();

    /**
     * 获取收音的状态获取器
     *
     * @return IStatusTool 收音的状态获取器
     */
    IStatusTool getStatusTool();

    /**
     * 注册收音状态变化的回调
     *
     * @param mediaStatusChangeListener 收音状态的变化回调
     */
    void registerRadioStatusChangeListener(IRadioStatusChange mediaStatusChangeListener);

    /**
     * 注销收音状态的变化回调
     *
     * @param mediaStatusChangeListener 收音状态的变化回调
     */
    void unregisterRadioStatusChangerListener(IRadioStatusChange mediaStatusChangeListener);

}
