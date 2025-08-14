
package com.desaysv.mediacommonlib.bean;

/**
 * @author uidp5370
 * @date 2019-6-12
 * 原因变化的集合，后期如果需要可以添加
 * 数字小的优先级高，假如由于高优先级原因导致的暂停，低优先级的播放无法打断。
 */

public class ChangeReasonData {
    /**
     * 默认状态，优先级最低
     */
    public static ChangeReason NA = new ChangeReason("NA", 100);

    /**
     * 静音，由于静音导致的状态，现在基本上弃用
     */
    public static ChangeReason MUTE = new ChangeReason("MUTE", 4);

    /**
     * 音源变化，由于音源变化导致的控制逻辑触发
     */
    public static ChangeReason SOURCE = new ChangeReason("SOURCE", 4);

    /**
     * 音源变化，由于音源焦点丢失变化导致的控制逻辑触发
     */
    public static ChangeReason LONG_LOSS_SOURCE = new ChangeReason("LONG_LOSS_SOURCE", 2);            //音源长丢失
    /**
     * 倒车状态变化，基本上弃用
     */
    public static ChangeReason RVC = new ChangeReason("RVC", 3);

    /**
     * 电源变化，基本上弃用
     */
    public static ChangeReason POWER = new ChangeReason("POWER", 4);

    /**
     * 自动播放，由于界面进入导致的恢复播放的逻辑
     */
    public static ChangeReason AUTO_PLAY = new ChangeReason("AUTO_PLAY", 0);

    /**
     * 自动播放完成，之前的特殊逻辑定义，基本上弃用
     */
    public static ChangeReason AUTO_PLAY_COLLECT = new ChangeReason("AUTO_PLAY_COLLECT", 0);

    /**
     * 上汽用的，点击播放全部的原因
     */
    public static ChangeReason PLAY_ALL = new ChangeReason("PLAY_ALL", 0);

    /**
     * 界面销毁，视频特有
     */
    public static ChangeReason UI_DESTROY = new ChangeReason("UI_DESTROY", 0);

    /**
     * 界面退出，视频特有
     */
    public static ChangeReason UI_FINISH = new ChangeReason("UI_FINISH", 1);

    /**
     * 界面恢复，并且优先级要比较低,因为界面恢复可能是不能恢复播放的
     */
    public static ChangeReason UI_START = new ChangeReason("UI_START", 1);

    /**
     * 点击按钮
     */
    public static ChangeReason CLICK = new ChangeReason("CLICK", 0);
    /**
     * 视频界面的双击
     */
    public static ChangeReason DOUBLE_CLICK = new ChangeReason("DOUBLE_CLICK", 0);
    /**
     * 点击item导致的
     */
    public static ChangeReason CLICK_ITEM = new ChangeReason("CLICK_ITEM", 0);

    /**
     * 播放相同媒体文件
     */
    public static ChangeReason CLICK_SAME_ITEM = new ChangeReason("CLICK_SAME_ITEM", 0);
    /**
     * 音乐，视频onPrepare，准备完成
     */
    public static ChangeReason ON_PREPARE = new ChangeReason("ON_PREPARE", 8);
    /**
     * 电话控制，蓝牙音乐特有，基本上弃用
     */
    public static ChangeReason PHONE_CONTROL = new ChangeReason("PHONE_CONTROL", 0);
    /**
     * 由于媒体异常导致控制，
     */
    public static ChangeReason ON_ERROR = new ChangeReason("ON_ERROR", 0);
    /**
     * 播放结束，播放结束导致的媒体控制
     */
    public static ChangeReason ON_COMPLETE = new ChangeReason("ON_COMPLETE", 0);
    /**
     * 上一曲
     */
    public static ChangeReason PRE = new ChangeReason("PRE", 0);
    /**
     * 下一曲
     */
    public static ChangeReason NEXT = new ChangeReason("NEXT", 0);
    /**
     * 硬按键
     */
    public static ChangeReason HARD_KEY = new ChangeReason("HARD_KEY", 0);
    /**
     * 跳转
     */
    public static ChangeReason SEEK_TO = new ChangeReason("SEEK_TO", 0);
    /**
     * U盘1拔出
     */
    public static ChangeReason USB1_EJECT = new ChangeReason("USB1_EJECT", 0);
    /**
     * U盘2拔出
     */
    public static ChangeReason USB2_EJECT = new ChangeReason("USB2_EJECT", 0);
    /**
     * 进入STR状态释放mediaplay
     */
    public static ChangeReason ENTER_STR_RELEASE = new ChangeReason("ENTER_STR_RELEASE", 0);
    /**
     * 蓝牙断开
     */
    public static ChangeReason BT_EJECT = new ChangeReason("BT_EJECT", 0);
    /**
     * 源恢复
     */
    public static ChangeReason RECOVER_SOURCE = new ChangeReason("RECOVER_SOURCE", 0);
    /**
     * 快进快退
     */
    public static ChangeReason AUTO_SEEK = new ChangeReason("AUTO_SEEK", 0);
    /**
     * 屏幕手势控制
     */
    public static ChangeReason GESTURE_CONTROL = new ChangeReason("GESTURE_CONTROL", 0);
    /**
     * 滑动列表
     */
    public static ChangeReason SLIDING_LIST = new ChangeReason("SLIDING_LIST", 0);
    /**
     * 语音控制
     */
    public static ChangeReason VR_CONTROL = new ChangeReason("VR_CONTROL", 0);

    /**
     * 界面搜索
     */
    public static ChangeReason CLICK_SEARCH = new ChangeReason("CLICK_SEARCH", 0);
    /**
     * 超速
     */
    public static ChangeReason OVER_SPEED = new ChangeReason("OVER_SPEED", 3);
    /**
     * 收音特有，搜索状态下触发控制，要停止搜索
     */
    public static ChangeReason CONTROL_ACTION = new ChangeReason("CONTROL_ACTION", 0);
    /**
     * 蓝牙手机端start失败
     */
    public static ChangeReason START_FAIL = new ChangeReason("START_FAIL", 0);
    /**
     * 静音
     */
    public static ChangeReason IPC_CONTROL = new ChangeReason("IPC_CONTROL", 0);
    /**
     * 删除收藏曲目
     */
    public static ChangeReason DELETE_COLLECT = new ChangeReason("DELETE_COLLECT", 0);
    /**
     * 点击导致的快进快退
     */
    public static ChangeReason CLICK_FAST = new ChangeReason("CLICK_FAST", 0);
    /**
     * 点击导致的快进快退
     */
    public static ChangeReason CLICK_STOP = new ChangeReason("CLICK_STOP", 0);
    /**
     * 由于AIDL导致的
     */
    public static ChangeReason AIDL = new ChangeReason("AIDL", 0);
    /**
     * 由于收音机搜索导致的，里面有一个打开电台的动作
     */
    public static ChangeReason AST_ALL = new ChangeReason("AST_ALL", 0);
    /**
     * 音源恢复
     */
    public static ChangeReason BOOT_RESUME = new ChangeReason("BOOT_RESUME", 0);

    /**
     * 启动后OPEN音源但是不播放
     */
    public static ChangeReason BOOT_RESUME_NOT_PLAY = new ChangeReason("BOOT_RESUME_NOT_PLAY", 3);

    /**
     * mode按键控制
     */
    public static ChangeReason MODE_CONTROL = new ChangeReason("MODE_CONTROL", 0);
    /**
     * 列表异常
     */
    public static ChangeReason LIST_FAIL = new ChangeReason("LIST_FAIL", 0);
    /**
     * 列表搜索结束后需要恢复播放
     */
    public static ChangeReason AST_CHANGE_PLAY = new ChangeReason("AST_CHANGE_PLAY", 0);
    /**
     * 电台打开失败的恢复
     */
    public static ChangeReason OPEN_RADIO_FAIL = new ChangeReason("OPEN_RADIO_FAIL", 4);

    /**
     * 界面控制导致的
     */
    public static ChangeReason UI_CONTROL = new ChangeReason("UI_CONTROL", 0);

    /**
     * 由于步进模式导致的步进跳转
     */
    public static ChangeReason STEP_MODE = new ChangeReason("STEP_MODE", 0);

    /**
     * 上汽的逻辑，长按上下搜台按键，会自动步进跳转
     */
    public static ChangeReason AUTO_STEP = new ChangeReason("AUTO_STEP", 0);

    /**
     * 超过5s的话，就跳转到上一曲
     */
    public static ChangeReason MORE_THEM_FIVE = new ChangeReason("MORE_THEM_FIVE", 0);

    /**
     * 当播放未成功时，重新打开
     */
    public static ChangeReason UN_PREPARE = new ChangeReason("UN_PREPARE", 0);

    /**
     * 增加播放速率控制
     */
    public static ChangeReason FAST_FORWARD = new ChangeReason("FAST_FORWARD", 0);

    /**
     * 增加播放速率控制
     */
    public static ChangeReason FAST_FORWARD_CANCAL = new ChangeReason("FAST_FORWARD_CANCAL", 0);

    /**
     * 开始快速步进。上汽特有
     */
    public static ChangeReason START_FAST_STEP = new ChangeReason("START_FAST_STEP", 0);

    /**
     * 停止快速步进。上汽特有
     */
    public static ChangeReason STOP_FAST_STEP = new ChangeReason("STOP_FAST_STEP", 0);

    /**
     * DAB/RDS 公告弹窗
     */
    public static ChangeReason RADIO_TTS = new ChangeReason("RADIO_TTS", 0);


    /**
     * DAB/RDS 公告弹窗消失时，恢复原来播放
     */
    public static ChangeReason TTS_RESUME = new ChangeReason("TTS_RESUME", 6);

    /**
     * DAB/RDS 公告弹窗消失时，恢复原来播放
     */
    public static ChangeReason TTS_RESUME_PAUSE = new ChangeReason("TTS_RESUME_PAUSE", 6);


    /**
     * DAB预约
     */
    public static ChangeReason EPG_CLICK = new ChangeReason("EPG_CLICK", 0);
}
