package com.desaysv.moduledab.common;

/**
 * 定义DAB界面的常量
 */
public class Constant {
    public static final int LIST_TYPE_ALL = 0;//全部电台列表
    public static final int LIST_TYPE_ENSEMBLE = 1;//集合分类列表
    public static final int LIST_TYPE_ENSEMBLE_SUB = 2;//点击集合，进去的具体列表
    public static final int LIST_TYPE_CATEGORY = 3;//类别分类列表
    public static final int LIST_TYPE_CATEGORY_SUB = 4;//点击类别，进去的具体列表
    public static final int LIST_TYPE_COLLECT = 5;//收藏列表

    public static final String SP_PLAYLIST_NAME = "dab_playlist_name";//维护播放列表时，用来存储到SharePreference的key
    public static final String SP_PLAYLIST_TYPE_KEY = "dab_playlist_type";//维护播放列表时，用来存储到SharePreference的key
    public static final String SP_PLAYLIST_TAG_KEY = "dab_playlist_tag";//维护播放列表时，用来存储到SharePreference的key

    public static final int SCAN_TIMEOUT = 1000 * 60;
}
