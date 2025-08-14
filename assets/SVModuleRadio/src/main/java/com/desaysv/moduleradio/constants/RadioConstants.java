package com.desaysv.moduleradio.constants;

public class RadioConstants {

    //Fragment类型:FM、AM、COLLECT
    public static final int FRAGMENT_FM = 0;
    public static final int FRAGMENT_AM = 1;
    public static final int FRAGMENT_DAB = 2;
    public static final int FRAGMENT_COLLECT = 3;

    //界面更新消息
    public static final int MSG_UPDATE_CURRENT_PLAY_RADIO_MESSAGE = 4;
    public static final int MSG_UPDATE_RADIO_PLAY_STATUS = 5;
    public static final int MSG_UPDATE_RADIO_AM_LIST = 6;
    public static final int MSG_UPDATE_RADIO_FM_LIST = 7;
    public static final int MSG_UPDATE_RADIO_COLLECT_LIST = 8;
    public static final int MSG_UPDATE_RADIO_SEARCH_STATUS = 9;
    public static final int MSG_SCAN_TIMEOUT = 10;
    public static final int MSG_UPDATE_RADIO_RDS_FLAG = 11;
    public static final int MSG_OPEN_WITH_SCROLL = 12;
    public static final int MSG_UPDATE_RDS_SETTINGS = 13;

    public static final int MSG_TIPS_TIMEOUT = 14;

    public static final int MSG_TIPS = 15;
    public static final int MSG_UPDATE_THEME = 16;

    public static final int MSG_OPEN_AM_PLAY_PAGE = 17;

    public static final int MSG_OPEN_Multi_PLAY_PAGE = 18;


    public static final String RADIO_PAGE = "radio_page";

    //常量
    /**
     * 搜索倒计时
     */
    public static final int SCAN_TIMEOUT = 1000 * 60;

    public static final int TIPS_TIMEOUT = 1200;


    //TAB项的顺序定义
    public static class TABWithDAB{

        public static final int POSITION_DAB = 0;

        public static final int POSITION_FM = 1;

        public static final int POSITION_AM = 2;

        public static final int POSITION_COLLECT = 3;
    }

    public static class TABWithoutDAB{

        public static final int POSITION_FM = 0;

        public static final int POSITION_AM = 1;

        public static final int POSITION_COLLECT = 2;
    }

    public static class TABWithoutAM{

        public static final int POSITION_MULTI = 0;

        public static final int POSITION_COLLECT = 1;
    }

    public static class TABWithMultiAM{

        public static final int POSITION_MULTI = 0;

        public static final int POSITION_AM = 1;

        public static final int POSITION_COLLECT = 2;
    }

    /**
     * RDS设置项的位置
     */
    public static class RDS{

        public static final int POSITION_RDS = 0;

        public static final int POSITION_REC = 1;

        public static final int POSITION_AF = 2;

        public static final int POSITION_TA = 3;
    }
}
