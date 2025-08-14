package com.desaysv.libradio.bean;

/**
 * @author uidq1846
 * @desc 关于有些只需要几个结点的json就不再创建class文件了
 * @time 2022-9-28 14:27
 */
public class JsonNode {

    /**
     * 设置选择收音源类型
     */
    public static final String RADIO_SOURCE_NODE = "source";

    /**
     * 设置选择收音源类型
     */
    public static final String MUTE_UNMUTE_NODE = "mute";

    /**
     * 电台的扫描状态
     */
    public static final String SEEK_SCAN_STATUS = "seek_scan_status";

    /**
     * 达到播放的状态，类似媒体播放当中的prepare
     */
    public static final String PREPARE_PLAY = "play";

    /**
     * 场强信息
     */
    public static final String RADIO_RSSI = "rssi";
}
