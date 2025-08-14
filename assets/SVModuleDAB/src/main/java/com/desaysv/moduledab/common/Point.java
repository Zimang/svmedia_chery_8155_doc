package com.desaysv.moduledab.common;


/**
 * created by ZNB on 2023-03-08
 * 埋点事件的常量定义
 */
public class Point {

    public static class KeyName {

        /**
         * 打开/关闭FM
         */
        public static final String OpenCloseClickFM = "Changting.Redio.FM.N.OpenCloseClick";

        /**
         * FM操作事件
         */
        public static final String FMOperate = "Changting.Redio.FM.N.RedioOperate";

        /**
         * FM收藏操作事件
         */
        public static final String FMCollect = "Changting.Redio.FM.Collect.CollClick";

        /**
         * FM查看列表
         */
        public static final String ListClickFM = "Localset.FM.N.N.ListClick";

        /**
         * FM搜索
         */
        public static final String FMSearch = "Changting.Redio.FM.Search.FMSearch";

        /**
         * 打开/关闭AM
         */
        public static final String OpenCloseClickAM = "Changting.Redio.AM.N.OpenCloseClick";

        /**
         * AM操作事件
         */
        public static final String AMOperate = "Changting.Redio.AM.N.RedioOperate";

        /**
         * AM收藏操作事件
         */
        public static final String AMCollect = "Changting.Redio.AM.Collect.CollClick";

        /**
         * AM查看列表
         */
        public static final String ListClickAM = "Localset.AM.N.N.ListClick";

        /**
         * FM搜索
         */
        public static final String AMSearch = "Changting.Redio.AM.Search.AMSearch";


        /**
         * 打开/关闭收藏列表
         */
        public static final String OpenCloseCollectList = "Changting.Redio.CollList.N.OpenCloseClick";


        /**
         * RDS总开关
         */
        public static final String RDSSwitchClick = "Changting.Redio.FM.RDS.RDSClick";

        /**
         * RDS TA开关
         */
        public static final String TASwitchClick = "Changting.Redio.FM.RDS.TAClick";

        /**
         * RDS AF开关
         */
        public static final String AFSwitchClick = "Changting.Redio.FM.RDS.AFClick";

        /**
         * 打开/关闭DAB
         */
        public static final String OpenCloseClickDAB = "Changting.Redio.DAB.N.OpenCloseClick";

        /**
         * DAB操作事件
         */
        public static final String DABOperate = "Changting.Redio.DAB.PlayControl.RedioOperate";

        /**
         * DAB收藏操作事件
         */
        public static final String DABCollect = "Changting.Redio.DAB.Collect.CollClick";

        /**
         * DAB搜索事件
         */
        public static final String DABSearch = "Localset.DAB.Search.N.SrchClick";

//        /**
//         * DAB查看列表
//         */
//        public static final String ListClickDAB = "Localset.DAB.List.N.ListClick";


        /**
         * DAB点击全部电台
         */
        public static final String DABListClickAll = "Changting.Redio.DAB.RedioList.RedioClick";

        /**
         * DAB点击电台列表
         */
        public static final String DABListClickList = "Changting.Redio.DAB.RedioList.ListClick";
        /**
         * DAB点击分类列表
         */
        public static final String DABListClickType = "Changting.Redio.DAB.RedioList.ClassClick";
        /**
         * DAB点击电台类别名称
         */
        public static final String DABClickType = "Changting.Redio.DAB.RedioList.ClassNeClick";

        /**
         * DAB点击我的收藏
         */
        public static final String DABListClickCollect = "Changting.Redio.DAB.RedioList.CollClick";

        /**
         * DAB设置公告
         */
        public static final String AnnSwitchClick = "Changting.Redio.DAB.DABSet.PAClick";

        /**
         * DAB公告播报
         */
        public static final String AnnType = "PAType";

        /**
         * DAB HardLink 开关
         */
        public static final String HserFollowSwitchClick = "Changting.Redio.DAB.DABSet.ServiceClick";

        /**
         * DAB SoftLink 开关
         */
        public static final String SserFollowSwitchClick = "Changting.Redio.DAB.DABSet.ServiceLinkClick";

        /**
         * DAB EPG订阅
         */
        public static final String EPGClick = "Changting.Redio.DAB.EPG.BookClick";

        /**
         * 打开应用
         */
        public static final String OpenAudioClick = "Changting.N.N.N.OpenClick";

        /**
         * 退出应用
         */
        public static final String CloseAudioClick = "Changting.N.N.N.CloseClick";


        /**
         * 语音打开应用
         */
        public static final String OpenAudioVoice = "System.Apps.N.N.OpenVoice";

        /**
         * 语音退出应用
         */
        public static final String CloseAudioVoice = "System.Apps.N.N.CloseVoice";

        /**
         * 打开收音机
         */
        public static final String OpenAudioRadioClick = "LocalRadio.N.N.N.OpenClick";

        /**
         * 退出收音机
         */
        public static final String CloseAudioRadioClick = "LocalRadio.N.N.N.CloseClick";
        /**
         * 打开蓝牙
         */
        public static final String OpenAudioBTClick = "BTMusic.N.N.N.OpenClick";

        /**
         * 退出蓝牙
         */
        public static final String CloseAudioBTClick = "BTMusic.N.N.N.CloseClick";

        /**
         * 打开Music
         */
        public static final String OpenAudioMusicClick = "LocalMusic.N.N.N.OpenClick";

        /**
         * 退出Music
         */
        public static final String CloseAudioMusicClick = "LocalMusic.N.N.N.CloseClick";

    }

    /**
     * KeyName事件有对应的 Filed
     * 组合到埋点的时候，需要一一对应
     */
    public static class Field {
        /**
         * 打开/关闭
         * 0：打开；1：关闭
         */
        public static final String OperType = "OperType";

        /**
         * 操作的来源
         * 0：点击；1：语音
         */
        public static final String OperStyle = "OperStyle";

        /**
         * FM操作事件的类型
         * 1：播放；2：暂停；3：上一个；4：下一个
         */
        public static final String PlayOperType = "PlayOperType";

        /**
         * FM操作的电台名称
         */
        public static final String RadioName = "RadioName";

        /**
         * FM操作的电台频点
         */
        public static final String Mhz = "Mhz";


        /**
         * 收藏操作事件的类型
         * 0：收藏；1：取消收藏
         */
        public static final String CollOperType = "CollOperType";

        /**
         * 查看列表操作的类型
         * 1：电台列表；2：收藏列表
         */
        public static final String ListType = "ListType";

        /**
         * RDS开关操作事件的类型
         * 0：关闭；1：开启
         */
        public static final String Switchflg = "Switchflg";

        /**
         * DAB搜索
         * 0：关闭；1：开启
         */
        public static final String DABSearchType = "DABSearchType";

        /**
         * DAB公告
         * 1：报警；2：道路交通；3：other
         */
        public static final String AnnType = "AnnType";

        /**
         * EPG订阅的电台名称
         */
        public static final String ProgramName = "ProgramName";

        /**
         * 分类类别的电台名称
         */
        public static final String ClassName = "ClassName";

        /**
         * 打开/关闭
         * 1：打开；2：关闭
         */
        public static final String OPENTYPE = "OpenType";
        /**
         * 打开/关闭
         * 1：打开；2：关闭
         */
        public static final String CLOSETYPE = "CloseType";

    }


    /**
     * Filed对应的值
     * 包括字符、数值、binary格式
     */
    public static class FieldValue {

        /**
         * 打开
         */
        public static final String OPEN = "0x00";

        /**
         * 关闭
         */
        public static final String CLOSE = "0x01";

        /**
         * 点击操作
         */
        public static final String OpeCLick = "0x00";

        /**
         * 语音操作
         */
        public static final String OpeVR = "0x01";

        /**
         * 播放
         */
        public static final String PLAY = "1";

        /**
         * 暂停
         */
        public static final String PAUSE = "2";

        /**
         * 上一个
         */
        public static final String PRE = "3";

        /**
         * 下一个
         */
        public static final String NEXT = "4";

        /**
         * 查看列表
         */
        public static final String LIST_STATION = "1";

        /**
         * 查看收藏列表
         */
        public static final String LIST_COLLECT = "2";

        /**
         * 收藏
         */
        public static final String COLLECT = "0x00";

        /**
         * 取消收藏
         */
        public static final String UNCOLLECT = "0x01";

        /**
         * DAB公告播报-Alarm
         */
        public static final String ANN_ALARM = "1";

        /**
         * DAB公告播报-TA
         */
        public static final String ANN_TA = "2";

        /**
         * DAB公告播报-TF
         */
        public static final String ANN_TF = "3";

        /**
         * DAB公告播报-Warning
         */
        public static final String ANN_Warning = "4";

        /**
         * DAB公告播报-News
         */
        public static final String ANN_News = "5";

        /**
         * DAB公告播报-Weather
         */
        public static final String ANN_Weather = "6";
        /**
         * DAB公告播报-Event
         */
        public static final String ANN_Event = "7";
        /**
         * DAB公告播报-Special
         */
        public static final String ANN_Special = "8";
        /**
         * DAB公告播报-Program
         */
        public static final String ANN_Program = "9";
        /**
         * DAB公告播报-Sport
         */
        public static final String ANN_Sport = "10";
        /**
         * DAB公告播报-Finance
         */
        public static final String ANN_Finance = "11";

        /**
         * 点击操作
         */
        public static final String CLICK = "2";


    }



    public static class KEY {
        /**
         * 应用打开事件
         */
        public static final String KEY_App_Open = "App_Open";

        /**
         * 应用关闭事件
         */
        public static final String KEY_App_Close = "App_Close";


    }

    public static class Filed {
        /**
         * 应用打开方式
         */
        public static final String Filed_OpsMode = "OpsMode";

        /**
         * 应用打开时间
         */
        public static final String Filed_OpsTime = "OpsTime";

        /**
         * 应用关闭方式
         */
        public static final String Filed_ClsMode = "ClsMode";

        /**
         * 应用关闭时间
         */
        public static final String Filed_ClsTime = "ClsTime";

        /**
         * USB连接时间
         */
        public static final String Filed_ConnectTime = "ConnectTime";

    }

    public static class FiledValue {
        /**
         * 应用打开/关闭方式, 1: 语音， 2：点击
         */
        public static final String FiledValue_VOICE = "1";

        /**
         * 应用打开/关闭方式, 1: 语音， 2：点击
         */
        public static final String FiledValue_CLICK = "2";
    }
}

