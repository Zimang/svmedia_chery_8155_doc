package com.desaysv.svlibusbdialog.constant;

/**
 * 常量的全局定义
 */
public class Constant {

    /**
     * 标志谁发送的广播
     */
    public static final String FLAG_DIALOG_CLOSE_KEY= "com.desaysv.svlibusbdialog.close.flag";


    /**
     * 标志谁发送的广播
     */
    public static final String FLAG_DIALOG_CLOSE_NAME = "com.desaysv.svlibusbdialog.dialog.SourceDialog";

    public static class PATH{
        /**
         * USB1的挂载路径
         */
        public static final String PATH_USB1 = "/storage/usb0";

        /**
         * USB2的挂载路径
         */
        public static final String PATH_USB2 = "/storage/usb1";
    }



    public static class Device{
        /**
         * 收到挂载广播，开始扫描动画
         */
        public static final int STATE_MOUNTED = 0;

        /**
         * 收到卸载广播，停止动画并dismiss
         */
        public static final int STATE_UNMOUNTED = 1;

    }


    public static class Scanner{

        /**
         * 媒体库开始扫描
         */
        public static final int STATE_START = 2;

        /**
         * 媒体库结束扫描
         */
        public static final int STATE_FINISH = 3;

    }


    public static class Query{

        /**
         * 扫描过程中查询到数据
         */
        public static final int STATE_HAVING_DATA = 4;

        /**
         * 扫描过程中没有查询到数据
         */
        public static final int STATE_HAVING_NO_DATA = 5;

        /**
         * 扫描结束查询到数据
         */
        public static final int STATE_HAD_DATA = 6;

        /**
         * 扫描结束没有查询到数据
         */
        public static final int STATE_HAD_NO_DATA = 7;

        /**
         * scan_start广播丢失，导致没有启动查询
         */
        public static final int STATE_NO_QUERY = -1;
    }

    /**
     *  * Dialog跳转到雄狮的参数定义
     */
    public static class Lion{

        /**
         * 导航到对应界面的Key
         */
        public static final String NAVIGATION_KEY = "NAVIGATION_TO";

        /**
         * 畅听的包名
         */
        public static final String PKG_MUSIC = "com.lion.media";

        /**
         * 畅听的服务Action
         */
        public static final String ACTION_MUSIC = "com.lion.media.center.service";

        /**
         * 畅听的主页类名
         */
        public static final String PKG_MUSIC_CLS = "com.lion.business.home.ui.HomeActivity";

        /**
         * 指定畅听显示USB音乐
         */
        public static final int NAVIGATION_USB_MUSIC = 1;//畅听的USB音乐

        /**
         * 畅影的包名
         */
        public static final String PKG_VIDEO = "com.lion.video";

        /**
         * 畅影的服务Action
         */
        public static final String ACTION_VIDEO = "com.lion.video.voice.VoiceService";

        /**
         * 畅影的主页类名
         */
        public static final String PKG_VIDEO_CLS = "com.lion.video.activity.MainActivity";

        /**
         * 指定畅听显示USB视频
         */
        public static final int NAVIGATION_USB_VIDEO = 2;//畅影的USB视频

    }

    /**
     *  * Dialog跳转到DeSay的参数定义
     */
    public static class DeSay{
        /**
         * 音乐的包名
         */
        public static final String PKG_MUSIC = "com.desaysv.svaudioapp";

        /**
         * 音乐的类名
         */
        public static final String PKG_MUSIC_CLS = "com.desaysv.svaudioapp.ui.MainActivity";

        /**
         * 拉起界面使用的Key
         */
        public static final String SOURCE_KEY = "source_key";


        /**
         * 导航到对应界面的Key
         */
        public static final String NAVIGATION_KEY = "NavigationFlag";

        /**
         * 导航到对应界面的列表页
         */
        public static final int LIST_VIEW = 1;

        /**
         * USB0音乐的音频焦点
         */
        public static final String USB0_MUSIC_SOURCE = "usb0";

        /**
         * 音乐的包名
         */
        public static final String PKG_VIDEO = "com.desaysv.videoapp";

        /**
         * 音乐的类名
         */
        public static final String PKG_VIDEO_CLS = "com.desaysv.moduleusbvideo.ui.VideoMainActivity";

    }

}
