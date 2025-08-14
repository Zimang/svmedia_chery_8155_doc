package com.desaysv.libdevicestatus.bean;

/**
 * Created by LZM on 2020-11-16
 * Comment 路径用来进行使用的标志位
 *
 * @author uidp5370
 */
public class DeviceConstants {

    public static class DevicePath {
        /**
         * USB0设备的路径
         */
        public static final String USB0_PATH = "/storage/usb0";

        /**
         * USB1设备的路径
         */
        public static final String USB1_PATH = "/storage/usb1";

        /**
         * 蓝牙的设备路径，用来映射，不是真实路径
         */
        public static final String BT_PATH = "/storage/bt";

        /**
         * carplay的设备路径，用来映射，不是真实路径
         */
        public static final String CARPLAY_PATH = "/storage/carpaly";

        /**
         * carlife的设备路径，用来映射，不是真实路径
         */
        public static final String CARLIFE_PATH = "/storage/carlife";
    }



}
