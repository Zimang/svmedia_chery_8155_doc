package com.desaysv.modulebtmusic;

/**
 * Common公共类
 * 同步app/bt/SW-HC-BT/Codes/CHERY仓库
 */
public class BaseConstants {
    public static final String TAG = "BT-";

    /**
     * 服务绑定状态
     */
    public class Service {
        public final static int BIND_FAIL = 0;      //服务连接成功
        public final static int BIND_SUCCESS = BIND_FAIL + 1; //服务连接失败
    }

    /**
     * 接听电话类型
     */
    public class AcceptCallType {
        /**
         * 只有一路电话时使用这个类型，若使用了其它类型是会强制转换为 CALL_ACCEPT_NONE
         * 若多路电话时使用了这个类型，会强制转换为 CALL_ACCEPT_HOLD
         */
        public static final int CALL_ACCEPT_NONE = 0;
        /**
         * 接听第二路电话，保持第一路电话
         */
        public static final int CALL_ACCEPT_HOLD = 1;         //接听第二路电话，保持第一路电话
        /**
         * 接听第二路来电，挂断第一通电话
         */
        public static final int CALL_ACCEPT_TERMINATE = 2;   //接听第二路来电，挂断第一通电话
    }

    /**
     * 蓝牙开关状态
     */
    public class BtSwitchState {
        /**
         * 蓝牙关
         */
        public static final int STATE_OFF = 10; //蓝牙关
        /**
         * 蓝牙开
         */
        public static final int STATE_ON = 12;  //蓝牙开
        /**
         * 蓝牙关闭中
         */
        public static final int STATE_TURNING_OFF = 13; //蓝牙关闭中
        /**
         * 蓝牙打开中
         */
        public static final int STATE_TURNING_ON = 11;  //蓝牙打开中
    }

    /**
     * 电话私密扬声状态类型
     */
    public class AudioState {
        /**
         * 私密，即手机
         */
        public static final int STATE_AUDIO_DISCONNECTED = 0;   //私密，即手机
        /**
         * 扬声连接中
         */
        public static final int STATE_AUDIO_CONNECTING = 1;     //扬声连接中
        /**
         * 扬声，即车机
         */
        public static final int STATE_AUDIO_CONNECTED = 2;      //扬声，即车机
    }

    /**
     * HFP、PBAP、A2DP、MAP等协议的连接状态
     */
    public class ProfileConnectionState {
        /**
         * 已连接
         */
        public static final int STATE_CONNECTED = 2;        //已连接
        /**
         * 连接中
         */
        public static final int STATE_CONNECTING = 1;       //连接中
        /**
         * 已断开
         */
        public static final int STATE_DISCONNECTED = 0;     //已断开
        /**
         * 断开中
         */
        public static final int STATE_DISCONNECTING = 3;    //断开中
    }

    /**
     * 加载电话本、通话记录数据库缓存类型
     */
    public class LoadPbapType {
        /**
         * 无效的通话记录类型，
         **/
        public static final int TYPE_INVALID = -1;
        /**
         * 加载数据库通讯录
         */
        public static final int TYPE_LOAD_CONTACT_FROM_DB = TYPE_INVALID + 1;

        /**
         * 加载数据库通话记录
         */
        public static final int TYPE_LOAD_CALLLOG_FORM_DB = TYPE_LOAD_CONTACT_FROM_DB + 1;
        /**
         * 加载数据库缓存通讯录 + 通话记录
         */
        public static final int TYPE_LOAD_ALL_FROM_DB = TYPE_LOAD_CALLLOG_FORM_DB + 1;
    }

    /**
     * 同步手机电话本类型
     */
    public class SyncPbapType {

        public static final int TYPE_INVALID = -1;
        /**
         * 加载手机通讯录
         */
        public static final int TYPE_SYNC_CONTACT_FORM_PHONE = TYPE_INVALID + 1;
        /**
         * 加载手机通话记录
         **/
        public static final int TYPE_SYNC_CALLLOG_FROM_PHONE = TYPE_SYNC_CONTACT_FORM_PHONE + 1;
        /**
         * 下载通话记录+通讯录
         */
        public static final int TYPE_SYNC_ALL_FROM_PHONE = TYPE_SYNC_CALLLOG_FROM_PHONE + 1;
    }

    /**
     * 通话状态
     */
    public class CallState {
        /**
         * 接听状态
         */
        public static final int CALL_STATE_ACTIVE = 0;
        /**
         * hold住状态
         */
        public static final int CALL_STATE_HELD = 1;
        /**
         * 去电中
         */
        public static final int CALL_STATE_DIALING = 2;
        /**
         * 去电成功
         */
        public static final int CALL_STATE_ALERTING = 3;
        /**
         * 来电中
         */
        public static final int CALL_STATE_INCOMING = 4;
        /**
         * 等待状态
         */
        public static final int CALL_STATE_WAITING = 5;
        public static final int CALL_STATE_HELD_BY_RESPONSE_AND_HOLD = 6;
        /**
         * 挂断中
         */
        public static final int CALL_STATE_TERMINATED = 7;
        /**
         * 未接
         */
        public static final int CALL_STATE_MISSCALL = 8;
    }

    /**
     * 通讯录电话类型
     */
    public class ContactNumberType {
        public static final int TYPE_ASSISTANT = 19;
        public static final int TYPE_CALLBACK = 8;
        public static final int TYPE_CAR = 9;
        public static final int TYPE_COMPANY_MAIN = 10;
        /**
         * 传真-家庭
         */
        public static final int TYPE_FAX_HOME = 5;
        /**
         * 传真-工作
         */
        public static final int TYPE_FAX_WORK = 4;
        /**
         * 家庭
         */
        public static final int TYPE_HOME = 1;
        public static final int TYPE_ISDN = 11;
        public static final int TYPE_MAIN = 12;
        public static final int TYPE_MMS = 20;
        /**
         * 手机
         */
        public static final int TYPE_MOBILE = 2;
        public static final int TYPE_OTHER = 7;
        public static final int TYPE_OTHER_FAX = 13;
        public static final int TYPE_PAGER = 6;
        public static final int TYPE_RADIO = 14;
        public static final int TYPE_TELEX = 15;
        public static final int TYPE_TTY_TDD = 16;
        /**
         * 固话-工作
         */
        public static final int TYPE_WORK = 3;
        /**
         * 手机-工作
         */
        public static final int TYPE_WORK_MOBILE = 17;
        public static final int TYPE_WORK_PAGER = 18;
    }

    /**
     * 分组数据传输状态
     */
    public class LoadDBState {
        /**
         * 传输开始
         */
        public final static int STATE_LOAD_DB_START = 0;
        /**
         * 传输完成
         */
        public final static int STATE_LOAD_DB_FINISH = STATE_LOAD_DB_START + 1;
    }

    /**
     * 加载数据库缓存类型
     */
    public class LoadDBType {
        /**
         * 加载通讯录
         */
        public final static int TYPE_LOAD_CONTACT = 0;
        /**
         * 加载通话记录
         */
        public final static int TYPE_LOAD_CALLLOG = TYPE_LOAD_CONTACT + 1;
        /**
         * 加载模糊匹配结果
         */
        public final static int TYPE_LOAD_FUZZY = TYPE_LOAD_CALLLOG + 1;
    }

    /**
     * 短信下载类型
     */
    public class LoadSmsType {
        /**
         * 收件箱
         */
        public static final int TYPE_INBOX = 0;
        /**
         * 发送箱
         */
        public static final int TYPE_OUTBOX = 1;
        public static final int TYPE_SENT = 2;
        public static final int TYPE_DELETED = 3;
        public static final int TYPE_DRAFT = 4;
    }

    /**
     * 电话本的相关语法
     */
    public class PhoneSchemeType {
        /**
         * URI scheme for telephone number URIs.
         */
        public static final String SCHEME_TEL = "tel";

        /**
         * URI scheme for voicemail URIs.
         */
        public static final String SCHEME_VOICEMAIL = "voicemail";

        /**
         * URI scheme for SIP URIs.
         */
        public static final String SCHEME_SIP = "sip";
    }

    /**
     * 设备配对状态
     */
    public class PairState {
        /**
         * 配对成功
         */
        public static final int BOND_SUCCESS = 0;
        /**
         * 未配对、删除/取消配对
         */
        public static final int BOND_NONE = 10;
        /**
         * 配对请求
         */
        public static final int BOND_BONDING = 11;
        /**
         * 已配对
         */
        public static final int BOND_BONDED = 12;
    }

    /**
     * 搜索状态
     */
    public class ScanState {
        /**
         * 搜索开始
         */
        public final static int SCAN_START = 0;
        /**
         * 搜索结束
         */
        public final static int SCAN_END = SCAN_START + 1;
    }

    /**
     * 扫描模式
     */
    public class ScanMode {
        /**
         * 无法从远程蓝牙设备发现或连接该设备。
         */
        public final static int SCAN_MODE_NONE = 20;
        /**
         * 无法从远程设备搜索到该设备，但可以从远程设备的配对列表进行连接
         */
        public final static int SCAN_MODE_CONNECTABLE = 21;
        /**
         * 既可被发现也可以被连接
         */
        public final static int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;
    }

    /**
     * 共享内存数据id
     */
    public class ShareMemoryId {

    }

    /**
     * 持久层数据id
     */
    public class SharedPreferenceId {

    }

    /**
     * 搜索类型
     */
    public class SearchType {
        /**
         * 数字检索
         */
        public final static int TYPE_SEARCH_NUMBER = 0;
        /**
         * 字符串检索
         */
        public final static int TYPE_SEARCH_STRING = TYPE_SEARCH_NUMBER + 1;
    }

    /**
     * 蓝牙音乐的播放状态
     */
    public class PlayState {

        /**
         * 默认的播放状态，表示没有媒体已添加，或者没有内容可播放
         */
        public final static int STATE_NONE = 0;

        /**
         * 当前已停止的状态
         */
        public final static int STATE_STOPPED = 1;

        /**
         * 当前已暂停的状态
         */
        public final static int STATE_PAUSED = 2;

        /**
         * 当前正在播放的状态
         */
        public final static int STATE_PLAYING = 3;

        /**
         * 当前正在快速转发的状态
         */
        public final static int STATE_FAST_FORWARDING = 4;

        /**
         * 当前正在倒带的状态
         */
        public final static int STATE_REWINDING = 5;

        /**
         * 当前正在缓冲并将开始播放的状态缓冲了足够的数据时
         */
        public final static int STATE_BUFFERING = 6;

        /**
         * 当前处于错误状态的状态
         */
        public final static int STATE_ERROR = 7;

        /**
         * State indicating the class doing playback is currently connecting to a
         * route. Depending on the implementation you may return to the previous
         * state when the connection finishes or enter {@link #STATE_NONE}. If
         * the connection failed {@link #STATE_ERROR} should be used.
         * <p>
         * On devices earlier than API 21, this will appear as {@link #STATE_BUFFERING}
         * </p>
         */
        public final static int STATE_CONNECTING = 8;

        /**
         * 当前正在跳到上一个项目的状态
         */
        public final static int STATE_SKIPPING_TO_PREVIOUS = 9;

        /**
         * 当前正在跳到下一个项目的状态
         */
        public final static int STATE_SKIPPING_TO_NEXT = 10;

    }

    public class ReceiverAction {
        //G6SA
        public static final String ACTION_SYNC_STATE_CHANGED = "android.bluetooth.pbap.profile.action.SYNC_STATE_CHANGED";
        public static final String ACTION_PHONEBOOK_SIZE_DETERMINED = "android.bluetooth.pbap.profile.action.PHONEBOOK_SIZE_DETERMINED";
        public static final String EXTRA_PBAP_SYNC_TYPE = "android.bluetooth.pbap.extra.SYNC_TYPE";
        public static final String EXTRA_PBAP_SYNC_STATE = "android.bluetooth.pbap.extra.SYNC_STATE";
        public static final String EXTRA_PBAP_PHONEBOOK_SIZE_TYPE = "android.bluetooth.pbap.extra.PHONEBOOK_SIZE_type";
        //G6SH
        public static final String ACTION_GET_MEDIAITEM_STATE_CHANGED = "android.bluetooth.avrcp-controller.profile.action.GET_MEDIAITEM_STATE_CHANGED";
        public static final String ACTION_DISAPPEARED = "android.bluetooth.device.action.DISAPPEARED";
        public static final String ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";
        public static final String ACTION_DOWNLOAD_STATE_CHANGED = "android.bluetooth.pbap.profile.action.DOWNLOAD_STATE_CHANGED";
        public static final String ACTION_PHONEBOOK_SIZE = "android.bluetooth.pbap.profile.action.PHONEBOOK_SIZE";
        public static final String EXTRA_DOWNLOAD_TYPE = "android.bluetooth.pbap.extra.DOWNLOAD_TYPE";
        public static final String EXTRA_DOWNLOAD_STATE = "android.bluetooth.pbap.extra.DOWNLOAD_STATE";
        public static final String ACTION_DOWNLOAD_STATE_CHANGED_MAP = "android.bluetooth.mapmce.profile.action.DOWNLOAD_STATE_CHANGED";
        public static final String ACTION_MESSAGE_SET_PROPERTY_STATE_MAP = "android.bluetooth.mapmce.profile.action.MESSAGE_SET_PROPERTY_STATE";
        public static final String EXTRA_DOWNLOAD_INDEX_MAP = "android.bluetooth.mapmce.profile.extra.DOWNLOAD_INDEX";
        public static final String EXTRA_DOWNLOAD_TOTAL_MAP = "android.bluetooth.mapmce.profile.extra.DOWNLOAD_TOTAL";
        public static final String EXTRA_DOWNLOAD_TYPE_MAP = "android.bluetooth.mapmce.profile.extra.DOWNLOAD_TYPE";
        public static final String EXTRA_DOWNLOAD_STATE_MAP = "android.bluetooth.mapmce.profile.extra.DOWNLOAD_STATE";
        public static final String EXTRA_IS_NEW_MSG_MAP = "android.bluetooth.mapmce.profile.extra.IS_NEW_MSG";
    }

    /**
     * 蓝牙相关Profile
     */
    public class ProfileType {
        /**
         * a2dp profile
         */
        public static final int A2DP_SINK = 11; //媒体音频profile
        /**
         * acrcp profile
         */
        public static final int AVRCP_CONTROLLER = 12;  //媒体控制profile
        /**
         * 电话profile
         */
        public static final int HEADSET_CLIENT = 16;    //电话profile
        /**
         * 电话本、通话记录profile
         */
        public static final int PBAP_CLIENT = 17;       //电话本、通话记录profile
        /**
         * 短信profile
         */
        public static final int MAP_CLIENT = 18;        //短信profile
    }

    /**
     * 短信同步状态
     */
    public class MapSyncState {
        public static final int STATE_IDLE = -1;
        public static final int STATE_DOWNLOADING = 0;
        public static final int STATE_DOWNLOAD_COMPLETE = 1;
        public static final int STATE_DOWNLOAD_NOT_SUPPORT = 2;
        public static final int STATE_DOWNLOAD_STOP = 3;
        public static final int STATE_DOWNLOAD_ERROR_TIMEOUT = 4;
        public static final int STATE_DOWNLOAD_ERROR_NOT_ACCEPTEABLE = 5;
        public static final int STATE_DOWNLOAD_ERROR_NONE = 6;
    }

    public class SPLoadState {
        /**
         * 无效状态，无法获取SharedPreferences数据库，如：
         * 1、未初始化完成
         * 2、未绑定蓝牙服务层
         * 3、蓝牙未连接
         * 4、正在缓存从手机下载的数据并存到SharedPreferences数据库
         */
        public static final int STATE_INVALID = 0;
        /**
         * 空闲状态，此时可从SharedPreferences数据库获取数据
         */
        public static final int STATE_IDLE = 1;
        /**
         * 正在从SharedPreferences数据库的获取数据
         */
        public static final int STATE_LOADING = 2;
        /**
         * 已从SharedPreferences数据库的获取数据，当前缓存数据就是从数据库获取来的数据
         */
        public static final int STATE_LOAD_COMPLETED = 3;
    }

    /**
     * 传参时intent中的Action
     */
    public class Actions {
        public static final String ACTION_BOOT_RESUME_AUDIO = "com.desaysv.service.bluetooth.ACTION_BOOT_RESUME_AUDIO";
        public static final String ACTION_REMOTE_SERVICE_AND_LINKER = "com.desaysv.service.bluetooth.REMOTE_SERVICE_ACTION";
    }

    /**
     * 电话重拨的类型
     */
    public class RedialType {
        public static final int TYPE_REDIAL_DEFAULT = 0;
        public static final int TYPE_REDIAL_OUTGOING = 1;
        public static final int TYPE_REDIAL_INCOMING = 2;
        public static final int TYPE_REDIAL_MISSED = 3;
    }

    /**
     * 蓝牙设备是否可连接
     * PRIORITY_OFF：不可连接
     * PRIORITY_ON： 可连接
     */
    public class ProfilePriority {
        public static final int PRIORITY_OFF = 0;
        public static final int PRIORITY_ON = 100;
    }

    /**
     * 播放模式的类型
     */
    public class PlayerSettingsType {
        /**
         * Equalizer setting.
         */
        public static final int SETTING_EQUALIZER = 1;
        /**
         * Repeat setting.
         */
        public static final int SETTING_REPEAT = 2;
        /**
         * Shuffle setting.
         */
        public static final int SETTING_SHUFFLE = 4;
        /**
         * Scan mode setting.
         */
        public static final int SETTING_SCAN = 8;
    }

    /**
     * 播放模式对应的状态
     */
    public class PlayerSettingsState {
        /**
         * Invalid state. Used for returning error codes.
         */
        public static final int STATE_INVALID = -1;
        /**
         * OFF state. Denotes a general OFF state. Applies to all settings.
         */
        public static final int STATE_OFF = 0;
        /**
         * ON state. Applies to SETTING_EQUALIZER.
         */
        public static final int STATE_ON = 1;
        /**
         * Single track repeat. Applies only to SETTING_REPEAT.
         */
        public static final int STATE_SINGLE_TRACK = 2;
        /**
         * All track repeat/shuffle. Applies to SETTING_REPEAT, SETTING_SHUFFLE and SETTING_SCAN.
         */
        public static final int STATE_ALL_TRACK = 3;
        /**
         * Group repeat/shuffle. Applies to SETTING_REPEAT, SETTING_SHUFFLE and SETTING_SCAN.
         */
        public static final int STATE_GROUP = 4;
    }

    /**
     * 蓝牙开关状态
     */
    public class BluetoothState {
        /**
         * Indicates the local Bluetooth adapter is off.
         */
        public static final int STATE_OFF = 10;
        /**
         * Indicates the local Bluetooth adapter is turning on. However local
         * clients should wait for {@link #STATE_ON} before attempting to
         * use the adapter.
         */
        public static final int STATE_TURNING_ON = 11;
        /**
         * Indicates the local Bluetooth adapter is on, and ready for use.
         */
        public static final int STATE_ON = 12;
        /**
         * Indicates the local Bluetooth adapter is turning off. Local clients
         * should immediately attempt graceful disconnection of any remote links.
         */
        public static final int STATE_TURNING_OFF = 13;
    }
}