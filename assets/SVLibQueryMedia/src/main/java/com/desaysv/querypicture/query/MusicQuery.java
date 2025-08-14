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
import com.desaysv.svlibpicturebean.manager.MusicListManager;

public class MusicQuery extends BaseQuery{

    public MusicQuery(Context context) {
        super(context);
    }

    public MusicQuery(Context context, String dir) {
        super(context, dir);
    }

    public MusicQuery(Context context, String dir, boolean forceUpdate) {
        super(context, dir, forceUpdate);
    }

    @Override
    protected boolean filter(Cursor mCursor) {
        @SuppressLint("Range") String mimeType =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE));
        return !MimeMatcher.MusicMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
    }

    @Override
    protected Uri getQueryAndroidUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected Uri getQuerySVUri() {
        return Uri.parse("content://com.desaysv.mediaprovider.music/music");
    }

    @Override
    protected String getQuerySelection() {
        return MediaStore.Audio.AudioColumns.DATA + " like ?";
    }

    @Override
    protected boolean HasUSB1Cache() {
        if (!forceUpdate && MusicListManager.getInstance().getHasUSB1Cache(directory)) {
            Log.d(TAG, "has cache,return");
            mCurrentFileMessageList = MusicListManager.getInstance().getCacheUSB1MusicList(directory);
            folderMessageList = MusicListManager.getInstance().getCacheUSB1FolderList(directory);
            return true;
        }
        return false;
    }

    @Override
    protected boolean HasUSB2Cache() {
        if (!forceUpdate && MusicListManager.getInstance().getHasUSB2Cache(directory)) {
            Log.d(TAG, "has cache,return");
            mCurrentFileMessageList = MusicListManager.getInstance().getCacheUSB2MusicList(directory);
            folderMessageList = MusicListManager.getInstance().getCacheUSB2FolderList(directory);
            return true;
        }
        return false;
    }

    @Override
    protected void addUSB1List() {
        MusicListManager.getInstance().addAllUSB1MusicList(mAllFileMessageList);
        MusicListManager.getInstance().addUSB1RootFolderList(folderMessageList);
        MusicListManager.getInstance().addUSB1RootMusicList(mCurrentFileMessageList);
    }

    @Override
    protected void addUSB2List() {
        MusicListManager.getInstance().addAllUSB2MusicList(mAllFileMessageList);
        MusicListManager.getInstance().addUSB2RootFolderList(folderMessageList);
        MusicListManager.getInstance().addUSB2RootMusicList(mCurrentFileMessageList);
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
        fileMessage.setName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
        fileMessage.setPath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
        fileMessage.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)));
        fileMessage.setAuthor(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.AUTHOR)));
        fileMessage.setSize(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)));
        fileMessage.setLastModified(mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED)));
        return fileMessage;
    }

    @Override
    protected void addUSB1CacheList() {
        MusicListManager.getInstance().addCacheUSB1MusicList(directory,mCurrentFileMessageList);
        MusicListManager.getInstance().addCacheUSB1FolderList(directory,folderMessageList);
    }

    @Override
    protected void addUSB2CacheList() {
        MusicListManager.getInstance().addCacheUSB2MusicList(directory,mCurrentFileMessageList);
        MusicListManager.getInstance().addCacheUSB2FolderList(directory,folderMessageList);
    }

    @Override
    protected void addCurrentUSB1List() {
        MusicListManager.getInstance().addCurrentUSB1List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCurrentUSB2List() {
        MusicListManager.getInstance().addCurrentUSB2List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCacheUSB1Folder(String path, FolderMessage folderMessage) {
        MusicListManager.getInstance().addCacheUSB1Folder(path,folderMessage);
    }

    @Override
    protected void addCacheUSB2Folder(String path, FolderMessage folderMessage) {
        MusicListManager.getInstance().addCacheUSB2Folder(path,folderMessage);
    }

    @Override
    protected boolean containUSB1Folder(String path) {
        return MusicListManager.getInstance().getHasUSB1FolderCache(path);
    }

    @Override
    protected boolean containUSB2Folder(String path) {
        return MusicListManager.getInstance().getHasUSB2FolderCache(path);
    }

    @Override
    protected void clearUSB1Cache() {
        MusicListManager.getInstance().clearUSB1Cache();
    }

    @Override
    protected void clearUSB2Cache() {
        MusicListManager.getInstance().clearUSB2Cache();
    }
}
