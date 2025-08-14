package com.desaysv.svlibmediastore.query;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.media.MediaFormat;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author uidq1846
 * @desc 视频数据查询
 * @time 2022-11-15 9:49
 */
public class VideoQuery extends BaseQuery {
    private static final String TAG = "VideoQuery";

    private static IQuery query;

    private VideoQuery() {
    }

    public static IQuery getInstance() {
        if (query == null) {
            synchronized (VideoQuery.class) {
                if (query == null) {
                    query = new VideoQuery();
                }
            }
        }
        return query;
    }



    //默认需要显示的列
    @SuppressLint("InlinedApi")
    public static final String[] AUDIO_PROJECTION = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.RELATIVE_PATH,
            MediaStore.Video.Media.VOLUME_NAME,
            MediaStore.Video.Media.MIME_TYPE
    };

    //默认需要判断的选项
    public static final String MIME_TYPE_SELECTION = "( "
            + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " or " + MediaStore.Video.VideoColumns.MIME_TYPE + " = ?"
            + " ) ";

    public static final String DATA_SELECTION = "( "
            + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " or " + MediaStore.Video.VideoColumns.DATA + " like ?"
            + " ) ";


    @Override
    protected String[] getProjection() {
        return AUDIO_PROJECTION;
    }

    @Override
    protected String getSelection() {
        // 增加过滤U盘系统文件夹扫描
        String selection = " and " + MediaStore.Video.VideoColumns.DATA + " like ?"
                + " and " + MediaStore.Video.VideoColumns.RELATIVE_PATH + " != 'System Volume Information/'";
        if (isDataType) {
            selection = DATA_SELECTION + selection;
        } else {
            selection = MIME_TYPE_SELECTION + selection;
        }
        Log.d(TAG, "getSelection: selection = " + selection+" isDataType = "+isDataType);
        return selection;
    }

    @Override
    protected String[] getSelectionArgs(String path) {
        if (isDataType) {
            return new String[]{
                    "%.avi",//avi
                    "%.mp4", //mp4
                    "%.m4v",//m4v
                    "%.3gp",//3gp
                    "%.mov",//mov
                    "%.mkv",//mkv
                    "%.mpeg",//mpeg
                    "%.mpg",//mpg
                    "%.vob",//vob
                    "%.asf", //asf
                    "%.wmv", //wmv
                    "%.rm", //rm
                    "%.rmvb",//rmvb
                    "%.flv",//flv
                    path + "%"
            };
        } else {
            return new String[]{
                    "video/avi",//avi
                    "video/x-msvideo",//avi
                    "video/mp4", //mp4
                    "video/x-m4v",//m4v
                    MediaFormat.MIMETYPE_VIDEO_H263,//3gp
                    "video/quicktime",//mov
                    "video/x-matroska",//mkv
                    "video/mpeg",//mpeg/mpg
                    "video/mp2p",//mpeg/mpg
                    "video/x-ms-vob",//vob
                    "video/x-ms-asf", //asf
                    "video/x-ms-wmv", //wmv
                    "video/vnd.rn-realmedia", //rm
                    "video/vnd.rn-realvideo",//rmvb
                    "video/x-flv",//flv
                    path + "%"
            };
        }
    }

    @Override
    protected String getOrder() {
        return MediaStore.Video.Media.TITLE + " ASC";
    }

    @Override
    protected List<FileMessage> query(Cursor cursor) {
        List<FileMessage> videoList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileMessage fm = new FileMessage();
                fm.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
                fm.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                fm.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                fm.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
                fm.setFileName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                fm.setLastModified(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)));
                fm.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)));
                fm.setMediaType(FileMessage.VIDEO_TYPE);
                videoList.add(fm);
            }
            cursor.close();
        }
        return videoList;
    }

    @Override
    protected Uri getMediaUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }
}
