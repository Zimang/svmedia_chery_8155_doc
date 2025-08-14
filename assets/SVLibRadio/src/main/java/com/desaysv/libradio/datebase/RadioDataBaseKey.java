package com.desaysv.libradio.datebase;

/**
 * Created by LZM on 2019-8-9.
 * Comment 数据库的key值
 */
public class RadioDataBaseKey {

    public static final String TYPE = "type";
    public static final String FREQUENCY = "frequency";
    //标志对应的频段 AM; FM
    public static final String BAND = "band";
    //DAB的处理
    /**
     * 集合ID
     */
    public static final String ENSEMBLE_ID = "ensemble_id";
    /**
     * 电台(服务名称)
     */
    public static final String PROGRAM_STATION_NAME = "program_station_name";

    /**
     * 电台集合名称，也是电台类别
     */
    public static final String ENSEMBLE_LABEL = "ensemble_label";

    /**
     * 电台类型，具体数值对应的类型定义见XXX说明
     */
    public static final String PROGRAM_TYPE = "program_type";

    /**
     * 服务ID
     */
    public static final String SERVICE_ID = "service_id";

    /**
     * 服务组件ID
     */
    public static final String SERVICE_COMPONENT_ID = "service_component_id";
    /**
     * 电台详细信息
     */
    public static final String DYNAMIC_LABEL = "dynamic_label";
    /**
     * ID3信息
     */
    public static final String DYNAMIC_PLUS_LABEL = "dynamic_plus_label";

    /**
     * 电台简称
     */
    public static final String PROGRAM_STATION_SHORT_NAME = "program_station_short_name";

    /**
     * 电台集合简称
     */
    public static final String ENSEMBLE_LABEL_SHORT_NAME = "ensemble_label_short_name";

    /**
     * 子电台标志
     */
    public static final String SUB_SERVICE_FLAG = "sub_service_flag";


    //增加一个新的Table用来保持EPG的订阅数据

    /**
     * EPG订阅的table
     */
    public static final String TABLE_EPG = "RADIO_DAB_EPG_TABLE";

    /**
     * 订阅的EPG的服务ID
     */
    public static final String EPG_SERVICE_ID = "service_id";

    /**
     * 订阅的EPG的服务频点值
     */
    public static final String EPG_FREQ = "freq";

    /**
     * 订阅的EPG的服务组件ID
     */
    public static final String EPG_SERVICE_COMPONENT_ID = "service_component_id";

    /**
     * 订阅的EPG的电台名
     */
    public static final String EPG_PROGRAM_NAME = "program_name";

    /**
     * 订阅的EPG的year信息
     */
    public static final String EPG_YEAR = "epg_year";

    /**
     * 订阅的EPG的month信息
     */
    public static final String EPG_MONTH = "epg_month";

    /**
     * 订阅的EPG的day信息
     */
    public static final String EPG_DAY = "epg_day";

    /**
     * 订阅的EPG的hour信息
     */
    public static final String EPG_HOUR = "epg_hour";

    /**
     * 订阅的EPG的minutes信息
     */
    public static final String EPG_MIN = "epg_min";

    /**
     * 订阅的EPG的sec信息
     */
    public static final String EPG_SEC = "epg_sec";


    //增加一个新的Table用来保存搜索历史
    /**
     * 搜索历史的table
     */
    public static final String TABLE_SEARCH = "RADIO_SEARCH_TABLE";

    /**
     * 搜索历史的名称
     */
    public static final String RADIO_SEARCH_NAME = "radio_search_name";

    /**
     * 搜索历史的时间
     */
    public static final String RADIO_SEARCH_TIME = "radio_search_time";

    //增加一个新的Table用来保存DAB logo 列表
    /**
     * DAB logo的table
     */
    public static final String TABLE_DAB_LOGO= "TABLE_DAB_LOGO";

    /**
     * DAB logo的长度信息
     */
    public static final String DAB_LOGO_LEN = "dab_logo_len";

    /**
     * DAB logo的byte[]信息
     */
    public static final String DAB_LOGO_DATA = "dab_logo_DATA";


    //增加一个新的Table用来保存RDS 名称 列表
    /**
     * RDS名称的table
     */
    public static final String TABLE_RDS_NAME= "TABLE_RDS_NAME";

    /**
     * RDS对应的名称
     */
    public static final String RDS_STATION_NAME = "rds_station_name";
}
