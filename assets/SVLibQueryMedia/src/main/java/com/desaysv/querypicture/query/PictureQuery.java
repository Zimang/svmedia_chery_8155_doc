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
import com.desaysv.svlibpicturebean.manager.PictureListManager;

public class PictureQuery extends BaseQuery{

    public PictureQuery(Context context) {
        super(context);
    }

    public PictureQuery(Context context, String dir) {
        super(context, dir);
    }

    public PictureQuery(Context context, String dir, boolean forceUpdate) {
        super(context, dir, forceUpdate);
    }

    @Override
    protected boolean filter(Cursor mCursor) {
        @SuppressLint("Range") String mimeType =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
        if (isEU){
            @SuppressLint("Range") String data =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            return !MimeMatcher.ImageMatcher.isSupportExt(data.substring(data.lastIndexOf(".")));
        }
        return !MimeMatcher.ImageMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
    }

    @Override
    protected Uri getQueryAndroidUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected Uri getQuerySVUri() {
        return Uri.parse("content://com.desaysv.mediaprovider.picture/picture");
    }

    @Override
    protected String getQuerySelection() {
        return MediaStore.Images.ImageColumns.DATA + " like ?";
    }

    @Override
    protected boolean HasUSB1Cache() {
//        if (!forceUpdate && PictureListManager.getInstance().getHasUSB1Cache(directory)) {
//            Log.d(TAG, "has cache,return,directory :" + directory);
//            mCurrentFileMessageList = PictureListManager.getInstance().getCacheUSB1PictureList(directory);
//            folderMessageList = PictureListManager.getInstance().getCacheUSB1FolderList(directory);
//            Log.d(TAG, "has cache,return,mCurrentFileMessageList :" + mCurrentFileMessageList.size());
//            return true;
//        }
        return false;
    }

    @Override
    protected boolean HasUSB2Cache() {
//        if (!forceUpdate && PictureListManager.getInstance().getHasUSB2Cache(directory)) {
//            Log.d(TAG, "has cache,return");
//            mCurrentFileMessageList = PictureListManager.getInstance().getCacheUSB2PictureList(directory);
//            folderMessageList = PictureListManager.getInstance().getCacheUSB2FolderList(directory);
//            return true;
//        }
        return false;
    }

    @Override
    protected void addUSB1List() {
        PictureListManager.getInstance().addAllUSB1PictureList(mAllFileMessageList);
        PictureListManager.getInstance().addUSB1RootFolderList(folderMessageList);
        PictureListManager.getInstance().addUSB1RootPictureList(mCurrentFileMessageList);
    }

    @Override
    protected void addUSB2List() {
        PictureListManager.getInstance().addAllUSB2PictureList(mAllFileMessageList);
        PictureListManager.getInstance().addUSB2RootFolderList(folderMessageList);
        PictureListManager.getInstance().addUSB2RootPictureList(mCurrentFileMessageList);
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
        fileMessage.setName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE)));
        fileMessage.setPath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
        fileMessage.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)));
        fileMessage.setAuthor(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.AUTHOR)));
        fileMessage.setSize(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)));
        fileMessage.setLastModified(mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)));
        return fileMessage;
    }

    @Override
    protected void addUSB1CacheList() {
        PictureListManager.getInstance().addCacheUSB1PictureList(directory,mCurrentFileMessageList);
        PictureListManager.getInstance().addCacheUSB1FolderList(directory,folderMessageList);
    }

    @Override
    protected void addUSB2CacheList() {
        PictureListManager.getInstance().addCacheUSB2PictureList(directory,mCurrentFileMessageList);
        PictureListManager.getInstance().addCacheUSB2FolderList(directory,folderMessageList);
    }

    @Override
    protected void addCurrentUSB1List() {
        PictureListManager.getInstance().addCurrentUSB1List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCurrentUSB2List() {
        PictureListManager.getInstance().addCurrentUSB2List(mCurrentFileMessageList,folderMessageList);
    }

    @Override
    protected void addCacheUSB1Folder(String path, FolderMessage folderMessage) {
        PictureListManager.getInstance().addCacheUSB1Folder(path,folderMessage);
    }

    @Override
    protected void addCacheUSB2Folder(String path, FolderMessage folderMessage) {
        PictureListManager.getInstance().addCacheUSB2Folder(path,folderMessage);
    }

    @Override
    protected boolean containUSB1Folder(String path) {
        return PictureListManager.getInstance().getHasUSB1FolderCache(path);
    }

    @Override
    protected boolean containUSB2Folder(String path) {
        return PictureListManager.getInstance().getHasUSB2FolderCache(path);
    }

    @Override
    protected void clearUSB1Cache() {
        PictureListManager.getInstance().clearUSB1Cache();
    }

    @Override
    protected void clearUSB2Cache() {
        PictureListManager.getInstance().clearUSB2Cache();
    }
}
