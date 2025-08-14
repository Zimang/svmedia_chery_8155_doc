package com.desaysv.querypicture.query;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.querypicture.utils.MimeMatcher;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.manager.VideoListManager;

public class VideoQuery extends BaseQuery{

    public VideoQuery(Context context) {
        super(context);
    }

    public VideoQuery(Context context, String dir) {
        super(context, dir);
    }

    public VideoQuery(Context context, String dir, boolean forceUpdate) {
        super(context, dir, forceUpdate);
    }

    @Override
    protected boolean filter(Cursor mCursor) {
        @SuppressLint("Range") String mimeType =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
        return !MimeMatcher.VideoMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
    }

    @Override
    protected Uri getQueryAndroidUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected Uri getQuerySVUri() {
        return Uri.parse("content://com.desaysv.mediaprovider.video/video");
    }

    @Override
    protected String getQuerySelection() {
        return MediaStore.Video.VideoColumns.DATA + " like ?";
    }

    @Override
    protected boolean HasUSB1Cache() {
        if (!forceUpdate && VideoListManager.getInstance().getHasUSB1Cache(directory)) {
            Log.d(TAG, "has cache,return");
            mCurrentFileMessageList = VideoListManager.getInstance().getCacheUSB1VideoList(directory);
            folderMessageList = VideoListManager.getInstance().getCacheUSB1FolderList(directory);
            return true;
        }
        return false;
    }

    @Override
    protected boolean HasUSB2Cache() {
        if (!forceUpdate && VideoListManager.getInstance().getHasUSB2Cache(directory)) {
            Log.d(TAG, "has cache,return");
            mCurrentFileMessageList = VideoListManager.getInstance().getCacheUSB2VideoList(directory);
            folderMessageList = VideoListManager.getInstance().getCacheUSB2FolderList(directory);
            return true;
        }
        return false;
    }

    @Override
    protected void addUSB1List() {
        VideoListManager.getInstance().addAllUSB1VideoList(mAllFileMessageList);
        VideoListManager.getInstance().addUSB1RootFolderList(folderMessageList);
        VideoListManager.getInstance().addUSB1RootVideoList(mCurrentFileMessageList);
    }

    @Override
    protected void addUSB2List() {
        VideoListManager.getInstance().addAllUSB2VideoList(mAllFileMessageList);
        VideoListManager.getInstance().addUSB2RootFolderList(folderMessageList);
        VideoListManager.getInstance().addUSB2RootVideoList(mCurrentFileMessageList);
    }

    @SuppressLint("Range")
    @Override
    protected FileMessage getSVMessage(Cursor mCursor) {
        FileMessage fileMessage = new FileMessage();
        fileMessage.setName(mCursor.getString(mCursor.getColumnIndex(MediaKey.NAME)));
        fileMessage.setPath(mCursor.getString(mCursor.getColumnIndex(MediaKey.PATH)));
        fileMessage.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaKey.FILENAME)));
        fileMessage.setAuthor(mCursor.getString(mCursor.getColumnIndex(MediaKey.AUTHOR)));
        fileMessage.setType(mCursor.getString(mCursor.getColumnIndex(MediaKey.ROOT_PATH)));
        fileMessage.setSize(mCursor.getInt(mCursor.getColumnIndex(MediaKey.SIZE)));
        fileMessage.setLastModified(mCursor.getLong(mCursor.getColumnIndex(MediaKey.LAST_MODIFIED)));
        return fileMessage;
    }

    @SuppressLint("Range")
    @Override
    protected FileMessage getAndroidMessage(Cursor mCursor) {
        FileMessage fileMessage = new FileMessage();
        fileMessage.setName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE)));
        fileMessage.setPath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)));
        fileMessage.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)));
        fileMessage.setAuthor(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.AUTHOR)));
        fileMessage.setSize(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)));
        fileMessage.setLastModified(mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED)));
        return fileMessage;
    }

    @Override
    protected void addUSB1CacheList() {
        VideoListManager.getInstance().addCacheUSB1VideoList(directory,mCurrentFileMessageList);
        VideoListManager.getInstance().addCacheUSB1FolderList(directory,folderMessageList);
    }

    @Override
    protected void addUSB2CacheList() {
        VideoListManager.getInstance().addCacheUSB2VideoList(directory,mCurrentFileMessageList);
        VideoListManager.getInstance().addCacheUSB2FolderList(directory,folderMessageList);
    }

    @Override
    protected void addCurrentUSB1List() {
        VideoListManager.getInstance().addCurrentUSB1List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCurrentUSB2List() {
        VideoListManager.getInstance().addCurrentUSB2List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCacheUSB1Folder(String path, FolderMessage folderMessage) {
        VideoListManager.getInstance().addCacheUSB1Folder(path,folderMessage);
    }

    @Override
    protected void addCacheUSB2Folder(String path, FolderMessage folderMessage) {
        VideoListManager.getInstance().addCacheUSB2Folder(path,folderMessage);
    }

    @Override
    protected boolean containUSB1Folder(String path) {
        return VideoListManager.getInstance().getHasUSB1FolderCache(path);
    }

    @Override
    protected boolean containUSB2Folder(String path) {
        return VideoListManager.getInstance().getHasUSB2FolderCache(path);
    }

    @Override
    protected void clearUSB1Cache() {
        VideoListManager.getInstance().clearUSB1Cache();
    }

    @Override
    protected void clearUSB2Cache() {
        VideoListManager.getInstance().clearUSB2Cache();
    }
}
