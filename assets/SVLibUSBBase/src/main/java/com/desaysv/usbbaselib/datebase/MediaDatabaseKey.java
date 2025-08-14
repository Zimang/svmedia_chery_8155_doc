package com.desaysv.usbbaselib.datebase;


/**
 * Created by LZM on 2020/03/09
 * 媒体的数据库的key值,由于我这边要把MediaProvider独立起来，所以需要有两份Key值，另外一份在Commonlib里面的database/MediaDataBaseKey
 *
 * @author uidp5370
 */

public class MediaDatabaseKey {
    /**
     * 在表中的id
     */
    public static final String _ID = "_id";

    /**
     * 文件的根路径，用来区分USB设备
     */
    public static final String ROOT_PATH = "root_path";

    /**
     * U盘的UUID，用来做数据存储
     */
    public static final String DEVICE_UUID = "device_uuid";

    /**
     * 文件最后被修改的日期
     */
    public static final String LAST_MODIFIED = "last_modified";

    /**
     * 多媒体名称
     */
    public static final String NAME = "name";

    /**
     * 文件名
     */
    public static final String FILENAME = "fileName";

    /**
     * 文件绝对路径
     */
    public static final String PATH = "path";

    /**
     * 作者
     */
    public static final String AUTHOR = "author";

    /**
     * 专辑名
     */
    public static final String ALBUM = "album";

    /**
     * 是否收藏
     */
    public static final String IS_LOVE = "isLove";

    /**
     * 文件名称的拼音缩写
     */
    public static final String SORT_LETTERS_NAME = "sort_letters_name";

    /**
     * 文件名称的拼音缩写
     */
    public static final String SORT_LETTERS_FILE_NAME = "sort_letters_file_name";

    /**
     * 艺术家名称缩写，用于排序
     */
    public static final String SORT_LETTERS_FOR_AUTHOR = "sort_letters_author";

    /**
     * 专辑名称缩写，用于排序
     */
    public static final String SORT_LETTERS_FOR_ALBUM = "sort_letters_album";

    /**
     * 文件大小
     */
    public static final String SIZE = "_size";

    /**
     * 音乐时长
     */
    public static final String DURATION = "duration";

    /**
     * 是否有ID3信息
     */
    public static final String IS_HAS_ID3 = "is_has_id3";
}