package com.desaysv.querypicture.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.desaysv.querypicture.constant.MediaKey;
import com.desaysv.querypicture.utils.MimeMatcher;
import com.desaysv.svlibpicturebean.bean.FileMessage;
import com.desaysv.svlibpicturebean.bean.FolderMessage;
import com.desaysv.svlibpicturebean.manager.MusicListManager;
import com.desaysv.svlibpicturebean.manager.VideoListManager;

public class QueryVideoTask extends BaseQueryTask{
    public QueryVideoTask(Context context, IQueryResultListener listener) {
        super(context, listener);
    }

    public QueryVideoTask(Context context, IQueryResultListener listener, String dir) {
        super(context, listener, dir);
    }

    public QueryVideoTask(Context context, IQueryResultListener listener, String dir, boolean forceUpdate) {
        super(context, listener, dir,forceUpdate);
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

    /**
     * 根据系统需求文档，过滤指定类型
     * @return
     */
    @Override
    protected boolean filter() {
        @SuppressLint("Range") String mimeType =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
        Log.d(TAG,"mimeType: " +mimeType);
        return !MimeMatcher.VideoMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
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
    protected FileMessage getSVMessage() {
        FileMessage fileMessage = new FileMessage();
        fileMessage.setName(mCursor.getString(mCursor.getColumnIndex(MediaKey.NAME)));
        fileMessage.setPath(mCursor.getString(mCursor.getColumnIndex(MediaKey.PATH)));
        fileMessage.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaKey.FILENAME)));
        fileMessage.setAuthor(mCursor.getString(mCursor.getColumnIndex(MediaKey.AUTHOR)));
        fileMessage.setType(mCursor.getString(mCursor.getColumnIndex(MediaKey.ROOT_PATH)));
        fileMessage.setSize(mCursor.getInt(mCursor.getColumnIndex(MediaKey.SIZE)));
        fileMessage.setDuration(mCursor.getString(mCursor.getColumnIndex(MediaKey.DURATION)));
        fileMessage.setLastModified(mCursor.getLong(mCursor.getColumnIndex(MediaKey.LAST_MODIFIED)));
        return fileMessage;
    }

    @SuppressLint("Range")
    @Override
    protected FileMessage getAndroidMessage() {
        FileMessage fileMessage = new FileMessage();
        fileMessage.setName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE)));
        fileMessage.setPath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)));
        fileMessage.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)));
        fileMessage.setAuthor(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.AUTHOR)));
        fileMessage.setSize(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)));
        fileMessage.setDuration(mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)));
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
    protected void addCurrentUSB1List(){
        VideoListManager.getInstance().addCurrentUSB1List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCurrentUSB2List(){
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
