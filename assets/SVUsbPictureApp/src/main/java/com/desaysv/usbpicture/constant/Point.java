package com.desaysv.usbpicture.constant;


/**
 * created by ZNB on 2023-03-08
 * 埋点事件的常量定义
 */
public class Point {

    public static class KeyName{


        /**
         * 打开图片应用/进入应用
         */
        public static final String OpenPictureClick = "Picture.N.N.N.OpenClick";


        /**
         * 关闭图片/退出应用
         */
        public static final String ClosePictureClick = "Picture.N.N.N.CloseClick";


        /**
         * 打开/关闭图片
         * 0：打开；1：关闭
         */
        public static final String OpenCloseClick = "Picture.N.N.N.OpenCloseClick";

        /**
         * 操作图片
         * 1：查看；2：放大；3：缩小；4：旋转；5：上一个；6：下一个
         */
        public static final String PictureOperate = "Picture.USBPicture.N.N.PictureOperate";

        /**
         * 幻灯片操作事件，共用OperType这个Filed
         * 0：打开，1：关闭
         */
        public static final String SlideShowOperate = "Picture.USBPicture.N.N.SlideShowOperate";

    }

    /**
     * KeyName事件有对应的 Filed
     * 组合到埋点的时候，需要一一对应
     */
    public static class Field{
        /**
         * 打开/关闭图片
         * 0：打开；1：关闭
         */
        public static final String OperType = "OperType";

        /**
         * 操作图片
         * 1：查看；2：放大；3：缩小；4：旋转；5：上一个；6：下一个
         */
        public static final String PicOperType = "PicOperType";

        /**
         * 操作图片，图片名称
         */
        public static final String PictureName = "PictureName";

        /**
         * 打开/关闭图片
         */
        public static final String OpenType = "OpenType";
        public static final String CloseType = "CloseType";

    }


    /**
     *  Filed对应的值
     */
    public static class FieldValue{
        /**
         * 打开图片应用
         */
        public static final String OPEN = "0";

        /**
         * 关闭图片应用
         */
        public static final String CLOSE = "1";

        /**
         * 语音打开图片应用
         */
        public static final String VRACTION = "1";
        /**
         * 点击打开图片应用
         */
        public static final String CLICKACTION = "2";


        /**
         * 查看
         */
        public static final String PREVIEW = "1";

        /**
         * 放大
         */
        public static final String ENLARGE = "2";

        /**
         * 缩小
         */
        public static final String NARROW = "3";

        /**
         * 旋转
         */
        public static final String ROTATE = "4";

        /**
         * 上一个
         */
        public static final String PRE = "5";

        /**
         * 下一个
         */
        public static final String NEXT = "6";

        /**
         * 开始幻灯片
         */
        public static final String START_SLIDE = "0x00";

        /**
         * 停止幻灯片
         */
        public static final String STOP_SLIDE = "0x01";
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

        /**
         * USB连接事件
         */
        public static final String KEY_USB_Connect = "USB_Connect";
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

