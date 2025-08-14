package com.desaysv.moduleusbmusic.listener;

/**
 * @author uidq1846
 * @desc Fragment界面变化的接口，主要用于控制主页面切换到对应的状态
 * @time 2022-11-17 20:51
 */
public interface IFragmentActionListener {

    /**
     * 操作动作
     */
    void onActionChange(FragmentAction action, int targetPage, int fromPage);

    /**
     * 操作动作，携带信息
     */
    void onActionChange(FragmentAction action, int targetPage, int fromPage, Object data);
}
