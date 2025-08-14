package com.desaysv.svlibusbdialog.query;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.svlibusbdialog.manager.ScanStateManager;
import com.desaysv.svlibusbdialog.utils.MimeMatcher;

public class USB1PictureQuery extends BaseQuery{

    public USB1PictureQuery(Context mContext) {
        super(mContext);
    }

    @Override
    protected Uri getQueryUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String getQuerySelection() {
        return MediaStore.Images.ImageColumns.DATA + " like ?";
    }

    @Override
    protected String[] getArgs() {
        return new String[]{"%" + getPath() + "%"};
    }

    @Override
    protected String getPath() {
        return Constant.PATH.PATH_USB1;
    }

    @Override
    protected int getScanState() {
        return ScanStateManager.getInstance().getUsb1State();
    }

    @Override
    protected void notifyQueryState(int state) {
        QueryStateManager.getInstance().setUsb1PictureState(state);
    }


    @Override
    protected boolean filter(Cursor mCursor, int scanState) {
        @SuppressLint("Range") String mimeType =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
        return !MimeMatcher.ImageMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
    }

    @SuppressLint("Range")
    @Override
    protected String getData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
    }
}
