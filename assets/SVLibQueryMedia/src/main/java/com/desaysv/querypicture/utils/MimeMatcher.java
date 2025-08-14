package com.desaysv.querypicture.utils;

import android.util.Log;

import java.util.HashMap;

/**
 * 匹配对应 mime 类型的媒体文件，用于维护支持的媒体类型
 * 参考原生代码 MediaFile.java定义支持的mime类型
 * /external/mime-support/mime.types 定义了mime类型
 */
public class MimeMatcher {

    public static class ImageMatcher {
        private static final HashMap<String,Boolean> imageSupportMap = new HashMap<>();

        static {
            imageSupportMap.put("image/jpeg",true);// jpg 的格式也是这个mime类型
            imageSupportMap.put("image/png",true);
            imageSupportMap.put("image/gif",true);
            imageSupportMap.put("image/x-ms-bmp",true);
        }

        public static boolean isSupport(String mime){
            return imageSupportMap.containsKey(mime);
        }

        private static final HashMap<String,Boolean> imageSupportExtMap = new HashMap<>();

        static {
            imageSupportExtMap.put(".jpeg",true);// jpg 的格式也是这个mime类型
            imageSupportExtMap.put(".jpg",true);
            imageSupportExtMap.put(".png",true);
            imageSupportExtMap.put(".gif",true);
            imageSupportExtMap.put(".bmp",true);
        }

        /**
         * 欧盟项目判断后缀
         * @return
         */
        public static boolean isSupportExt(String ext){
            Log.d("ImageMatcher","isSupportExt,ext:"+ext);
            return imageSupportExtMap.containsKey(ext.toLowerCase());
        }

    }


    public static class VideoMatcher {
        private static final HashMap<String,Boolean> videoSupportMap = new HashMap<>();

        static {
            videoSupportMap.put("video/mpeg",true);
            videoSupportMap.put("video/mp4",true);
            videoSupportMap.put("video/3gpp",true);
            videoSupportMap.put("video/3gpp2",true);
            videoSupportMap.put("video/avi",true);
        }

        public static boolean isSupport(String mime){
            return videoSupportMap.containsKey(mime);
        }
    }


    public static class MusicMatcher {
        private static final HashMap<String,Boolean> musicSupportMap = new HashMap<>();

        static {
            musicSupportMap.put("audio/mpeg",true);// mp2、mp3的格式是这个mime类型
            musicSupportMap.put("audio/ogg",true);
            musicSupportMap.put("audio/aac",true);
            musicSupportMap.put("audio/flac",true);
        }

        public static boolean isSupport(String mime){
            return musicSupportMap.containsKey(mime);
        }
    }

}
