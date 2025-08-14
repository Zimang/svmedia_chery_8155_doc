package com.desaysv.libradio.bean;

/**
 * Created by LZM on 2019-7-10.
 * Comment 用来区分收音控制的枚举类型
 */
public enum RadioAction {

    /**
     * 打开收音
     */
    OPEN_RADIO,
    /**
     * 向前跳转
     */
    SEEK_FORWARD,
    /**
     * 向后跳转
     */
    SEEK_BACKWARD,
    /**
     * 向前前进一格
     */
    STEP_FORWARD,
    /**
     * 向后后退一格
     */
    STEP_BACKWARD,
    /**
     * 播放
     */
    PLAY,
    /**
     * 暂停
     */
    PAUSE,
    /**
     * 暂停或者播放，内部逻辑已经实现
     */
    PLAY_OR_PAUSE,
    /**
     * 搜索，搜索的是当前的电台
     */
    AST,
    /**
     * 搜索，搜索指定电台FM\AM
     */
    SPECIFIES_AST,
    /**
     * 搜索全部的有效电台列表，先搜索FM,在搜索AM
     */
    AST_ALL,
    /**
     * 停止搜索 -- 收音界面进入退出的时候，搜索过程中出现控制逻辑的触发，收音切换音源的时候都会触发
     */
    STOP_AST,
    /**
     * 收藏电台
     */
    CHANGE_COLLECT,
    /**
     * 取消收藏电台
     */
    CANCEL_COLLECT,

    /**
     * 固定的收藏，后续等代码提交之后，会修改命名
     */
    COLLECT,

    SET_DAB_ANN_SWITCH,

    SET_RDS_SETTING_SWITCH,

    SET_HEADLINE_ACTION,

    RQ_PTY_SCAN,

    /**
     * 开始进入步进模式
     */
    START_STEP_MODE,

    /**
     * 退出步进模式
     */
    STOP_STEP_MODE,

    /**
     * 开始快速向前步进模式
     */
    START_FAST_STEP_NEXT,

    /**
     * 停止快速向前步进模式
     */
    STOP_FAST_STEP_NEXT,

    /**
     * 开始快速向后步进模式
     */
    START_FAST_STEP_PRE,

    /**
     * 停止快速向后步进模式
     */
    STOP_FAST_STEP_PRE,

    /**
     * 释放焦点
     * 用于DAB公告、TA的场景
     */
    RELEASE,

    /**
     * 主动获取底层DAB信息
     */
    GET_DAB_INFO,

    /**
     * 组合搜索，先搜索FM，接着搜索DAB，
     * 适用于欧盟不带AM的项目
     */
    MULTI_AST,

    /**
     * 组合下一曲，即组合列表的下一曲
     */
    MULTI_SEEK_FORWARD,

    /**
     * 组合上一曲，即组合列表的上一曲
     */
    MULTI_SEEK_BACKWARD,

    /**
     * 组合向前前进一格，即组合列表的下一曲
     */
    MULTI_STEP_FORWARD,

    /**
     * 组合向向后后退一格，即组合列表的下一曲
     */
    MULTI_STEP_BACKWARD,

    /**
     * 后台扫描，不申请焦点
     */
    BACKGROUND_AST,

    /**
     * 设置搜台条件
     */
    SET_QUALITY_CONDITION,
}
