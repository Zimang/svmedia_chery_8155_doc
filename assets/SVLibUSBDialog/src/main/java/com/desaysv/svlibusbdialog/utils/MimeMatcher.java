package com.desaysv.svlibusbdialog.utils;

import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 匹配对应 mime 类型的媒体文件，用于维护支持的媒体类型
 * 参考原生代码 MediaFile.java定义支持的mime类型
 * /external/mime-support/mime.types 定义了mime类型
 */
public class MimeMatcher {

    public static class ImageMatcher {
        private static final HashMap<String, Boolean> imageSupportMap = new HashMap<>();

        static {
            imageSupportMap.put("image/jpeg", true);// jpg 的格式也是这个mime类型
            imageSupportMap.put("image/png", true);
            imageSupportMap.put("image/gif", true);
            imageSupportMap.put("image/x-ms-bmp", true);
        }

        public static boolean isSupport(String mime) {
            return imageSupportMap.containsKey(mime);
        }
    }


    public static class VideoMatcher {

        private static final String TAG = "VideoMatcher";
        private static final HashMap<String, Boolean> videoSupportMap = new HashMap<>();

        static {
            videoSupportMap.put("video/avi", true);//avi
            videoSupportMap.put("video/x-msvideo", true);//avi
            videoSupportMap.put("video/mp4", true);//mp4
            videoSupportMap.put("video/x-m4v", true);//m4v
            videoSupportMap.put(MediaFormat.MIMETYPE_VIDEO_H263, true);//3gp
            videoSupportMap.put("video/quicktime", true);//mov
            videoSupportMap.put("video/x-matroska", true);//mkv
            videoSupportMap.put("video/mpeg", true);//mpeg/mpg
            videoSupportMap.put("video/mp2p", true);//mpeg/mpg
            videoSupportMap.put("video/x-ms-vob", true);//vob
            videoSupportMap.put("video/x-ms-asf", true);//asf
            videoSupportMap.put("video/x-ms-wmv", true);//wmv
            videoSupportMap.put("video/vnd.rn-realmedia", true);//rm
            videoSupportMap.put("video/vnd.rn-realvideo", true);//rmvb
            videoSupportMap.put("video/x-flv", true);//flv
        }

        public static boolean isSupport(String mime) {
            return videoSupportMap.containsKey(mime);
        }

        public static List<String> getMediaVideoType() {
            List<String> list = new ArrayList<>();
            Set<String> keySet = videoSupportMap.keySet();
            keySet.forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    list.add(s);
                }
            });
            return list;
        }

        /**
         * 拼接视频Mime类型
         *
         * @return sql
         */
        public static String getMimeTypeVideo() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("(");
            String last = " or ";
            for (int i = 0; i < videoSupportMap.size(); i++) {
                buffer.append(MediaStore.Video.VideoColumns.MIME_TYPE);
                buffer.append("=");
                buffer.append(" ? ");
                buffer.append(last);
            }
            buffer.replace(buffer.length() - last.length(), buffer.length(), ")");
            Log.d(TAG, "getMimeTypeVideo: " + buffer);
            return buffer.toString();
        }
    }


    public static class MusicMatcher {
        private static final HashMap<String, Boolean> musicSupportMap = new HashMap<>();

        static {
            musicSupportMap.put("audio/mpeg", true);// mp2、mp3的格式是这个mime类型
            musicSupportMap.put("audio/ogg", true);
            musicSupportMap.put("audio/aac", true);
            musicSupportMap.put("audio/flac", true);
        }

        public static boolean isSupport(String mime) {
            return musicSupportMap.containsKey(mime);
        }
    }

}
