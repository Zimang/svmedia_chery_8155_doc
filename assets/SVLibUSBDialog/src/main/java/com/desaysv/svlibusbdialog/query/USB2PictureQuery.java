package com.desaysv.svlibusbdialog.query;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.svlibusbdialog.manager.ScanStateManager;

public class USB2PictureQuery extends BaseQuery{

    public USB2PictureQuery(Context mContext) {
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
        return Constant.PATH.PATH_USB2;
    }

    @Override
    protected int getScanState() {
        return ScanStateManager.getInstance().getUsb2State();
    }

    @Override
    protected void notifyQueryState(int state) {
        QueryStateManager.getInstance().setUsb2PictureState(state);
    }

    @SuppressLint("Range")
    @Override
    protected String getData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
    }
}
