package com.desaysv.usbbaselib.statussubject;

/**
 *
 * @author LZM
 * @date 2019-6-3
 * USB设备的连接状态和USB数据的搜索状态
 */

public enum SearchType {


    /**
     * 无数据
     */
    NO_DATA,

    /**
     * 搜索中
     */
    SEARCHING,

    /**
     * 扫描过程中，并且有数据
     */
    SEARCHING_HAVE_DATA,

    /**
     * 搜索完成，并且有数据
     */
    SEARCHED_HAVE_DATA

}
