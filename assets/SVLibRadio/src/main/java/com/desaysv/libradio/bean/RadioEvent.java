package com.desaysv.libradio.bean;

/**
 * @author uidq1846
 * @desc 和底层约定好数值
 * @time 2022-9-27 14:14
 */
public class RadioEvent {

    /**
     * 打开DAB，准备播放状态
     */
    public static class PreparePlay {

        /**
         * 未知
         */
        public static final int NONE = 0;

        /**
         * 播放操作
         */
        public static final int PLAY = NONE + 1;
    }

    /**
     * Ann状态
     */
    public static class AnnNotify {

        /**
         * DAB弹窗通知TA的显示状态
         */
        public static class AnnShowStatus {

            /**
             * 隐藏
             */
            public static final int HIDE = 0;

            /**
             * 显示
             */
            public static final int SHOW = HIDE + 1;
        }

        /**
         * DAB弹窗通知TA的类型
         */
        public static class AnnType {

            /**
             * 无效
             */
            public static final int INVALID = 0;

            /**
             * 警报
             */
            public static final int ALARM = INVALID + 1;

            /**
             * 交通
             */
            public static final int TRAFFIC = ALARM + 1;

            /**
             * 其它
             */
            public static final int OTHER = TRAFFIC + 1;
        }
    }

    /**
     * 扫描状态
     */
    public static class ScanStatus {
        /**
         * 当前不在搜台
         */
        public static final int RAW_RADIO_NODE = 0;

        /**
         * 扫描当中
         */
        public static final int RAW_RADIO_SCANING = RAW_RADIO_NODE + 1;

        /**
         * 下一曲
         */
        public static final int RAW_RADIO_SEEK_DOWN = RAW_RADIO_SCANING + 1;

        /**
         * 上一曲
         */
        public static final int RAW_RADIO_SEEK_UP = RAW_RADIO_SEEK_DOWN + 1;
    }

    /**
     * mute状态
     */
    public static class MuteStatus {
        /**
         * unmute
         */
        public static final int UNMUTE = 0;

        /**
         * mute
         */
        public static final int MUTE = UNMUTE + 1;
    }

    /**
     * 电台音源
     */
    public static class RadioSourceType {

        /**
         * FM_AM
         */
        public static final int FM_AM = 0;

        /**
         * DAB
         */
        public static final int DAB = FM_AM + 1;
    }

    /**
     * RDS ProgramType
     */
    public static class RDSProgramType {

        /**
         * 无类型或者没有定义
         */
        public static final int NO_TYPE_OR_UNDEFINED = 0;

        /**
         * 新闻
         */
        public static final int NEWS = NO_TYPE_OR_UNDEFINED + 1;

        /**
         * 时事
         */
        public static final int CURRENT_AFFAIRS = NEWS + 1;

        /**
         * 信息
         */
        public static final int INFORMATION = CURRENT_AFFAIRS + 1;

        /**
         * 运动
         */
        public static final int SPORT = INFORMATION + 1;

        /**
         * 教育
         */
        public static final int EDUCATION = SPORT + 1;

        /**
         * 戏剧
         */
        public static final int DRAMA = EDUCATION + 1;

        /**
         * 文化
         */
        public static final int CULTURE = DRAMA + 1;

        /**
         * 科学
         */
        public static final int SCIENCE = CULTURE + 1;

        /**
         * 多样
         */
        public static final int VARIED = SCIENCE + 1;

        /**
         * 流行音乐
         */
        public static final int POP_MUSIC = VARIED + 1;

        /**
         * 摇滚音乐
         */
        public static final int ROCK_MUSIC = POP_MUSIC + 1;

        /**
         * 易听
         */
        public static final int EASY_LISTENING_MUSIC = ROCK_MUSIC + 1;

        /**
         * 轻古典
         */
        public static final int LIGHT_CLASSICAL = EASY_LISTENING_MUSIC + 1;

        /**
         * 严肃的经典
         */
        public static final int SERIOUS_CLASSICAL = LIGHT_CLASSICAL + 1;

        /**
         * 其它音乐
         */
        public static final int OTHER_MUSIC = SERIOUS_CLASSICAL + 1;

        /**
         * 天气
         */
        public static final int WEATHER = OTHER_MUSIC + 1;

        /**
         * 金融
         */
        public static final int FINANCE = WEATHER + 1;

        /**
         * 儿童频道
         */
        public static final int CHILDREN_PROGRAMMES = FINANCE + 1;

        /**
         * 社会事务
         */
        public static final int SOCIAL_AFFAIRS = CHILDREN_PROGRAMMES + 1;

        /**
         * 社会事务
         */
        public static final int RELIGION = SOCIAL_AFFAIRS + 1;

        /**
         * 来电
         */
        public static final int PHONE_IN = RELIGION + 1;

        /**
         * 旅游
         */
        public static final int TRAVEL = PHONE_IN + 1;

        /**
         * 闲暇
         */
        public static final int LEISURE = TRAVEL + 1;

        /**
         * 爵士乐
         */
        public static final int JAZZ_MUSIC = LEISURE + 1;

        /**
         * 乡村音乐
         */
        public static final int COUNTRY_MUSIC = JAZZ_MUSIC + 1;

        /**
         * 民族音乐
         */
        public static final int NATIONAL_MUSIC = COUNTRY_MUSIC + 1;

        /**
         * 老歌
         */
        public static final int OLDIES_MUSIC = NATIONAL_MUSIC + 1;

        /**
         * 民间音乐、民谣
         */
        public static final int FOLK_MUSIC = OLDIES_MUSIC + 1;

        /**
         * 记录片
         */
        public static final int DOCUMENTARY = FOLK_MUSIC + 1;

        /**
         * 警报测试
         */
        public static final int ALARM_TEST = DOCUMENTARY + 1;

        /**
         * 警报
         */
        public static final int ALARM = ALARM_TEST + 1;
    }

    /**
     * RDS选项开关状态
     */
    public static class SwitchStatus {

        /**
         * 无效值
         */
        public static final int SWITCH_INVALID = -1;

        /**
         * 关
         */
        public static final int SWITCH_OFF = SWITCH_INVALID + 1;

        /**
         * 开
         */
        public static final int SWITCH_ON = SWITCH_OFF + 1;
    }

    /**
     * 头条信息类型
     */
    public static class HeadlineType {

        /**
         * 菜单（有下级目录）
         */
        public static final int MENU_HAS_CHILD = 1;

        /**
         * 正文内容
         */
        public static final int CONTENT = MENU_HAS_CHILD + 1;

        /**
         * 无效
         */
        public static final int INVALID = CONTENT + 1;

        /**
         * 菜单，无下级目录
         */
        public static final int MENU_NO_CHILD = INVALID + 1;
    }

    /**
     * DAB 头条子项动作
     */
    public static class HeadlineAction {

        /**
         * 进入journaline的首次获取(此类型bodyOption 参数可固定0
         */
        public static final int FIRST_PAGE = 0;

        /**
         * 进入下一级页面
         */
        public static final int NEXT_PAGE = FIRST_PAGE + 1;

        /**
         * 进入上一级页面
         */
        public static final int PRE_PAGE = NEXT_PAGE + 1;

        /**
         * 停止获取
         */
        public static final int STOP_GET = PRE_PAGE + 1;
    }

    /**
     * 定义的ID事件
     */
    public static class EventID {
        /**
         * FM/AM 主动获取电台 场强、信号强度信息
         */
        public static final int EVE_PROGRAM_INFO = 6;

        /**
         * 设置电台播放
         * frequency: int类型
         * serviceId:int 类型
         * serviceComponentId：int 类型
         */
        public static final int EVT_DAB_OP_SET_DAB_STATION = 0x800;

        /**
         * 切换DAB频段
         */
        public static final int EVT_DAB_OP_SET_DAB_BAND = EVT_DAB_OP_SET_DAB_STATION + 1;

        /**
         * 设置/解除静音指令
         * mute: int 类型
         * 1：mute 0：unmute
         */
        public static final int EVT_DAB_OP_MUTE_UNMUTE = EVT_DAB_OP_SET_DAB_BAND + 1;

        /**
         * 下一台
         */
        public static final int EVT_DAB_OP_SEEK_UP = EVT_DAB_OP_MUTE_UNMUTE + 1;

        /**
         * 下一台
         */
        public static final int EVT_DAB_OP_SEEK_DOWN = EVT_DAB_OP_SEEK_UP + 1;

        /**
         * 上层操作了公告的选中状态，需要发送此消息通知底层
         */
        public static final int EVT_DAB_OP_ANNOUNCEMENT_SELECTED_STATE = EVT_DAB_OP_SEEK_DOWN + 1;

        /**
         * 开始扫描电台
         */
        public static final int EVT_DAB_OP_SCAN = EVT_DAB_OP_ANNOUNCEMENT_SELECTED_STATE + 1;

        /**
         * 打断上下搜台或者扫描
         */
        public static final int EVT_DAB_OP_STOP_SEEK_SCAN = EVT_DAB_OP_SCAN + 1;

        /**
         * 列表信息，层可以通过发送此消息向底层请求列表信息，底层发送此消息通知上层更新列表
         * 如果是APP请求不用带内容，
         * 如果是APP接收底层更新的，需要解析参数，json见1.2
         */
        public static final int EVT_DAB_STATION_LIST_INFO = EVT_DAB_OP_STOP_SEEK_SCAN + 1;

        /**
         * 当前电台信息，上层可以通过发送此消息向底层请求当前电台，底层发送此消息通知上层更新当前电台信息
         */
        public static final int EVT_DAB_CURRENT_STATION_INFO = EVT_DAB_STATION_LIST_INFO + 1;

        /**
         * 当前搜台状态，上层可以通过发送此消息向底层请求当前搜索状态，底层发送此消息通知上层更新当前搜索状态
         * seek_scan_status：int 类型
         * 0当前不在搜台 1表示正在SCAN 2表示正在SEEK_DOWN 3表示正在SEEK_UP
         */
        public static final int EVT_DAB_SEEK_SCACN_STATUS = EVT_DAB_CURRENT_STATION_INFO + 1;

        /**
         * 公告选中状态 上层可以通过发送此消息向底层请求当前公告选中状态，底层发送此消息通知上层更新当前搜索状
         */
        public static final int EVT_DAB_ANNOUNCEMENT_SELECTED_STATE_INFO = EVT_DAB_SEEK_SCACN_STATUS + 1;

        /**
         * 底层通知上层现在需要播报公告信息
         */
        public static final int EVT_DAB_ANNOUNCEMENT_NOTIFY = EVT_DAB_ANNOUNCEMENT_SELECTED_STATE_INFO + 1;

        /**
         * EPG 时间表格  上层可以通过此消息主动请求 底层通过此消息通知上层EPG时间表格
         */
        public static final int EVT_DAB_EPG_INFO = EVT_DAB_ANNOUNCEMENT_NOTIFY + 1;

        /**
         * 专辑图片信息 上层可以通过此消息主动请求 底层通过此消息通知上层
         * slsLen ：int
         * slsDataList：char 数组
         */
        public static final int EVT_DAB_SLIDER_SHOW_INFO = EVT_DAB_EPG_INFO + 1;

        /**
         * 电台logo信息 上层可以通过此消息主动请求 底层通过此消息通知上层
         * logoLen ：int
         * logoDataList：char 数组
         */
        public static final int EVT_DAB_STATION_LOGO_INFO = EVT_DAB_SLIDER_SHOW_INFO + 1;

        /**
         * 1、	今日头条 上层可以通过此消息主动请求 底层通过此消息通知上层
         * 2、	底层获取完整的数据后可主动返回
         */
        public static final int EVT_DAB_JOURNALINE_INFO = EVT_DAB_STATION_LOGO_INFO + 1;

        /**
         * DAB 暂停播放状态 上层可以请求获取mute状态。底层主动通知mute状态
         * mute: int 类型
         * 0：mute 1：unmute
         */
        public static final int EVT_DAB_MUTE_STATUS = EVT_DAB_JOURNALINE_INFO + 1;

        /**
         * 底层主动通知DAB电台详细信息更新
         */
        public static final int EVT_DAB_DYNAMIC_LABEL_INFO = EVT_DAB_MUTE_STATUS + 1;

        /**
         * 上层主动获取电台的场强信息
         * RSSI: int 类型
         * 有正负数
         */
        public static final int EVT_DAB_STATION_RSSI = EVT_DAB_DYNAMIC_LABEL_INFO + 1;

        /**
         * 设置选择收音源类型
         * Source:int类型
         * 0：FM/AM
         * 1：DAB
         */
        public static final int EVT_SOURCE_SET_SWITCH = EVT_DAB_STATION_RSSI + 1;

        /**
         * 反馈DAB电台播放启动
         * Play : int 类型
         * 0：no play 1：play
         */
        public static final int EVT_DAB_PLAY_STATUS = EVT_SOURCE_SET_SWITCH + 1;

        /**
         * 反馈journaline信息获取完成状态
         */
        public static final int EVT_DAB_JOURNALINE_STATUS = EVT_DAB_PLAY_STATUS + 1;

        /**
         * 上层请求显示下一页的journaline的内容
         */
        public static final int EVT_DAB_OP_SET_DAB_JOURNALINE_OPTION = EVT_DAB_JOURNALINE_STATUS + 1;

        /**
         * 反馈有journaline内容的服务id
         * dataSid ：int类型：数据服务ID
         */
        public static final int EVT_DAB_JOURNALINE_DATASERVICE_ID = EVT_DAB_OP_SET_DAB_JOURNALINE_OPTION + 1;

        /**
         * 上层获取DAB广播的日期和时间
         * int类型：
         * state :
         * 1:时间获取成功
         * 0:时间无效
         */
        public static final int EVT_DAB_DATA_TIME_INFO = EVT_DAB_JOURNALINE_DATASERVICE_ID + 1;

        /**
         * 上层主动获取电台的BER信息
         * berSig: int 类型
         * berExp: int类型 （-6 ~0）
         */
        public static final int EVT_DAB_STATION_BER = EVT_DAB_DATA_TIME_INFO + 1;

        /**
         * 设置RDS开关
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_RDS_ON_OFF = 0xA00;

        /**
         * 设置TA开关
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_TA_ON_OFF = EVT_RDS_OP_SET_RDS_ON_OFF + 1;

        /**
         * 设置AF开关
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_AF_ON_OFF = EVT_RDS_OP_SET_TA_ON_OFF + 1;

        /**
         * 设置PTY开关
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_PTY_ON_OFF = EVT_RDS_OP_SET_AF_ON_OFF + 1;

        /**
         * 设置REG开关
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_REG_ON_OFF = EVT_RDS_OP_SET_PTY_ON_OFF + 1;

        /**
         * 设置EON开关
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_EON_ON_OFF = EVT_RDS_OP_SET_REG_ON_OFF + 1;

        /**
         * 请求PTY类型搜索
         * rds: int 类型
         * 1：on 0：off
         */
        public static final int EVT_RDS_OP_SET_PTY_SEEK_TYPE = EVT_RDS_OP_SET_EON_ON_OFF + 1;

        /**
         * 当前电台的电台名称/频率/RT/pty
         */
        public static final int EVT_RDS_CURRENT_STATION_INFO = EVT_RDS_OP_SET_PTY_SEEK_TYPE + 1;

        /**
         * 获取RDS所有项的开关状态
         */
        public static final int EVT_RDS_SETTING_OPTION = EVT_RDS_CURRENT_STATION_INFO + 1;

        /**
         * 底层通知app的RDS当前广播的状态和类型
         */
        public static final int EVT_RDS_ANNOUNCEMENT_NOTIFY = EVT_RDS_SETTING_OPTION + 1;

        /**
         * 底层通知上层RDS的状态标志的信息
         * Af/tp: int 类型
         * 1：on 0：off
         * Frequency :int 类型
         */
        public static final int EVT_RDS_STATUS_FLAG_INFO = EVT_RDS_ANNOUNCEMENT_NOTIFY + 1;

        /**
         * 底层通知上层DAB信号状态标志的信息
         * 1是low，0是good
         */
        public static final int EVT_DAB_SIGNAL_STATUS = 2076;

        /**
         * 底层通知上层RDS名称列表
         */
        public static final int EVT_RDS_PSN_LIST = 2572;

        /**
         * 底层通知上层DAB logo列表
         */
        public static final int EVT_DAB_LOGO_LIST = 2077;

        /**
         * 设置/获取搜台的条件
         */
        public static final int EVT_DAB_FM_OP_SIGNAL_QUALITY_CONDITION = 2078;
    }
}
