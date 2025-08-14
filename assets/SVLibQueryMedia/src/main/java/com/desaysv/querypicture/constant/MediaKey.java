package com.desaysv.querypicture.constant;

public class MediaKey {

    public static final String _ID = "_id";//多媒体设备类型
    public static final String TYPE = "type";//多媒体设备类型
    public static final String ROOT_PATH = "root_path";//文件的根路径，用来区分USB设备
    public static final String DEVICE_UUID = "device_uuid";//U盘的UUID，用来做数据存储
    public static final String LAST_MODIFIED = "last_modified";  //文件最后被修改的日期
    public static final String NAME = "name";//多媒体名称
    public static final String TIME = "time";//时间
    public static final String PATH = "path";//文件绝对路径
    public static final String FILENAME = "fileName";//文件名
    public static final String AUTHOR = "author";//作者
    public static final String ALBUM = "album";//专辑名
    public static final String ISLOVE = "isLove";//是否收藏
    public static final String ALBUM_PATH = "album_path";
    public static final String SORT_LETTERS_NAME = "sort_letters_name";  //ID3文件名称的拼音缩写
    public static final String IS_SUPPORTED = "isSupported";//平台是否支持
    public static final String SORT_LETTERS_FILE_NAME = "sort_letters_file_name";  //文件名称的拼音缩写
    public static final String SORT_LETTERS = "sort_letters";
    public static final String SORT_LETTERS_FOR_AUTHOR = "sort_letters_author";
    public static final String SORT_LETTERS_FOR_ALBUM = "sort_letters_album";
    public static final String SIZE = "_size";
    public static final String DURATION = "duration";// 音乐时长

    /**
     * 全局宏定义，用于区分是否启用Android原生的MediaProvider
     */
    public static final boolean SUPPORT_ANDROID_MEDIA_PROVIDER = true;

    /**
     * 全局宏定义，用于区分是否启用分段加载
     */
    public static final boolean SUPPORT_SUBSECTION_LOADING = true;
}
