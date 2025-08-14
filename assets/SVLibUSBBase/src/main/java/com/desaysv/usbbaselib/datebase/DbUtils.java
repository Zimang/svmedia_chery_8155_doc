package com.desaysv.usbbaselib.datebase;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2019-7-6.
 * Comment 数据工具
 */
public class DbUtils {

    private static final String TAG = "DbUtils";

    private DbUtils() {
    }


    /**
     * 获取视频的数据列表
     *
     * @param contentResolver 当前的Resolver
     * @param uri             媒体类型uri
     * @param sortOrder       排序
     * @param selection       限制条件
     * @param args            限制条件实体
     * @return videoList 视频的列表
     */
    public static List<FileMessage> getVideoDataBaseData(ContentResolver contentResolver, Uri uri, String sortOrder, String selection, String[] args) {
        List<FileMessage> videoList = new ArrayList<>();
        Cursor cursor = contentResolver.query(uri, null, selection, args, sortOrder);
        Log.d(TAG, "getVideoDataBaseData: cursor = " + cursor + " uri = " + uri);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileMessage fm = new FileMessage();
                fm.setId(cursor.getInt(cursor.getColumnIndex(MediaDatabaseKey._ID)));
                fm.setRootPath(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.ROOT_PATH)));
                fm.setPath(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.PATH)));
                fm.setDuration(cursor.getInt(cursor.getColumnIndex(MediaDatabaseKey.DURATION)));
                fm.setFileName(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.FILENAME)));
                fm.setLastModified(cursor.getLong(cursor.getColumnIndex(MediaDatabaseKey.LAST_MODIFIED)));
                fm.setSize(cursor.getLong(cursor.getColumnIndex(MediaDatabaseKey.SIZE)));
                fm.setMediaType(FileMessage.VIDEO_TYPE);
                videoList.add(fm);
            }
            cursor.close();
        }
        return videoList;
    }

    /**
     * 获取图片的数据列表
     *
     * @param contentResolver 当前的Resolver
     * @param uri             媒体类型uri
     * @param sortOrder       排序
     * @param selection       限制条件
     * @param args            限制条件实体
     * @return pictureList 图片列表
     */
    public static List<FileMessage> getPictureDataBaseData(ContentResolver contentResolver, Uri uri, String sortOrder, String selection, String[] args) {
        List<FileMessage> pictureList = new ArrayList<>();
        Cursor cursor = contentResolver.query(uri, null, selection, args, sortOrder);
        Log.d(TAG, "getPictureDataBaseData: cursor = " + cursor + " uri = " + uri);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileMessage fm = new FileMessage();
                fm.setId(cursor.getInt(cursor.getColumnIndex(MediaDatabaseKey._ID)));
                fm.setRootPath(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.ROOT_PATH)));
                fm.setPath(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.PATH)));
                fm.setFileName(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.FILENAME)));
                fm.setLastModified(cursor.getLong(cursor.getColumnIndex(MediaDatabaseKey.LAST_MODIFIED)));
                fm.setSize(cursor.getLong(cursor.getColumnIndex(MediaDatabaseKey.SIZE)));
                fm.setMediaType(FileMessage.PICTURE_TYPE);
                pictureList.add(fm);
            }
            cursor.close();
        }
        return pictureList;
    }


    /**
     * 获取音乐的数据列表
     *
     * @param contentResolver 当前的Resolver
     * @param uri             媒体类型uri
     * @param sortOrder       排序
     * @param selection       限制条件
     * @param args            限制条件实体
     * @return musicList 音乐列表
     */
    public static List<FileMessage> getMusicDataBaseData(ContentResolver contentResolver, Uri uri, String sortOrder, String selection, String[] args) {
        List<FileMessage> musicList = new ArrayList<>();
        Cursor cursor = contentResolver.query(uri, null, selection, args, sortOrder);
        Log.d(TAG, "getMusicDataBaseData: cursor = " + cursor + " uri = " + uri);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileMessage fm = new FileMessage();
                fm.setId(cursor.getInt(cursor.getColumnIndex(MediaDatabaseKey._ID)));
                fm.setRootPath(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.ROOT_PATH)));
                fm.setPath(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.PATH)));
                fm.setFileName(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.FILENAME)));
                fm.setName(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.NAME)));
                fm.setAuthor(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.AUTHOR)));
                fm.setAlbum(cursor.getString(cursor.getColumnIndex(MediaDatabaseKey.ALBUM)));
                fm.setDuration(cursor.getInt(cursor.getColumnIndex(MediaDatabaseKey.DURATION)));
                fm.setLastModified(cursor.getLong(cursor.getColumnIndex(MediaDatabaseKey.LAST_MODIFIED)));
                fm.setSize(cursor.getLong(cursor.getColumnIndex(MediaDatabaseKey.SIZE)));
                fm.setMediaType(FileMessage.MUSIC_TYPE);
                musicList.add(fm);
            }
            cursor.close();
        }
        return musicList;
    }



    /**
     * 获取音乐的数据列表
     *
     * @param contentResolver 当前的Resolver
     * @param sortOrder       排序
     * @param selection       限制条件
     * @param args            限制条件实体
     * @return musicList 音乐列表
     */
    public static List<FileMessage> getMusicDataBaseDataAndroid(ContentResolver contentResolver, String sortOrder, String selection, String[] args) {
        List<FileMessage> musicList = new ArrayList<>();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, args, sortOrder);
        Log.d(TAG, "getMusicDataBaseData: cursor = " + cursor);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileMessage fm = new FileMessage();
                fm.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                fm.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                fm.setFileName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                fm.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                fm.setAuthor(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.AUTHOR)));
                fm.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                fm.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                fm.setLastModified(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)));
                fm.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                fm.setMediaType(FileMessage.MUSIC_TYPE);
                musicList.add(fm);
            }
            cursor.close();
        }
        return musicList;
    }

    /**
     * 获取图片的数据列表
     *
     * @param contentResolver 当前的Resolver
     * @param sortOrder       排序
     * @param selection       限制条件
     * @param args            限制条件实体
     * @return pictureList 图片列表
     */
    public static List<FileMessage> getPictureDataBaseDataAndroid(ContentResolver contentResolver, String sortOrder, String selection, String[] args) {
        List<FileMessage> pictureList = new ArrayList<>();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, args, sortOrder);
        Log.d(TAG, "getPictureDataBaseData: cursor = " + cursor);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FileMessage fm = new FileMessage();
                fm.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                fm.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                fm.setFileName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                fm.setLastModified(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)));
                fm.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)));
                fm.setMediaType(FileMessage.PICTURE_TYPE);
                pictureList.add(fm);
            }
            cursor.close();
        }
        return pictureList;
    }


    /**
     * 获取视频的数据列表
     *
     * @param contentResolver 当前的Resolver
     * @param sortOrder       排序
     * @param selection       限制条件
     * @param args            限制条件实体
     * @return videoList 视频的列表
     */
    public static List<FileMessage> getVideoDataBaseData(ContentResolver contentResolver, String sortOrder, String selection, String[] args) {
        List<FileMessage> videoList = new ArrayList<>();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, selection, args, sortOrder);
        Log.d(TAG, "getVideoDataBaseData: cursor = " + cursor);
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
}
