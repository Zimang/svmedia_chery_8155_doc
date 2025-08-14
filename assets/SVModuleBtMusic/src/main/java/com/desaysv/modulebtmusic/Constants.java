package com.desaysv.modulebtmusic;

/**
 * Common公共类
 * 同步app/bt/SW-HC-BT/Codes/CHERY仓库
 */
public class Constants extends BaseConstants {
    /**
     * 电话本同步类型
     */
    public class PbapSyncType {

        /**
         * 同步手机通讯录
         */
        public static final int PBAP_TELECOM_PB = 1;
        /**
         * 同步手机+sim卡通讯录
         */
        public static final int PBAP_CONTACT_SYNC = 7;
        /**
         * 同步sim卡通讯录
         */
        public static final int PBAP_CONTACT_SIM = 2;
        /**
         * 同步来电通话记录
         */
        public static final int PBAP_CALLLOG_ICH = 3;
        /**
         * 同步去电通话记录
         */
        public static final int PBAP_CALLLOG_OCH = 4;
        /**
         * 同步未接通话记录
         */
        public static final int PBAP_CALLLOG_MCH = 5;
        /**
         * 同步通话记录：未接+去电+来电，会下载 BaseConfig.PBAP_CALLLOG_MAX_SIZE*3 条通话记录
         */
        public static final int PBAP_CALLLOG_SYNC = 6;
        /**
         * 同步通话记录：未接+去电+来电，会下载 BaseConfig.PBAP_CALLLOG_MAX_SIZE 条通话记录
         */
        public static final int PBAP_CALLLOG_SYNC_COMBINE = 8;
    }

    /**
     * 电话本同步状态
     */
    public class PbapSyncState {
        /**
         * 同步开始
         */
        public static final int PBAP_SYNC_START = 0;
        /**
         * 同步完成
         */
        public static final int PBAP_SYNC_FINISHED = 1;
        /**
         * 不支持下载
         */
        public static final int PBAP_SYNC_NOT_SUPPORT = 2;
        /**
         * 同步停止
         */
        public static final int PBAP_SYNC_STOP = 3;
        /**
         * 请求下载超时
         */
        public static final int PBAP_ERR_PULL_PHONEBOOK_REQUEST_TIMEOUT = 4;
        /**
         * 不允许下载
         */
        public static final int PBAP_ERR_PULL_PHONEBOOK_NOT_ACCEPTEABLE = 5;
        /**
         * 同步错误
         */
        public static final int PBAP_SYNC_ERROR_NONE = 6;
        /**
         * 下载无
         */
        public static final int PBAP_SYNC_NONE = 7;
        /**
         * 写数据库完成
         */
        public static final int PBAP_WRITE_DATABASE_COMPLETE = 8;
        /**
         * 正在写原生数据库
         */
        public static final int STATE_WRITING_DATABASE = 9;
        /**
         * 空闲状态
         */
        public static final int PBAP_IDLE_SYNC = 20;
    }

    /**
     * 通话记录类型
     */
    public class CallLogType {
        /**
         * 去电
         */
        public static final int CALLLOG_TYPE_OUTGOING = 4;
        /**
         * 来电
         */
        public static final int CALLLOG_TYPE_INCOMING = 3;
        /**
         * 未接
         */
        public static final int CALLLOG_TYPE_MISSED = 5;
    }


    public class PlayListSyncState {
        public static final int STATE_DOWNLOADING = 0;
        public static final int STATE_DOWNLOAD_COMPLETE = 1;
    }

    public static final String BT_MUSIC_PAGE = "bt_music_page";
}
