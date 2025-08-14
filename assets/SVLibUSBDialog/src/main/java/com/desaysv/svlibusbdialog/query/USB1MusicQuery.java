package com.desaysv.svlibusbdialog.query;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.desaysv.svlibusbdialog.constant.Constant;
import com.desaysv.svlibusbdialog.manager.QueryStateManager;
import com.desaysv.svlibusbdialog.manager.ScanStateManager;

public class USB1MusicQuery extends BaseQuery{

    public USB1MusicQuery(Context mContext) {
        super(mContext);
    }

    @Override
    protected Uri getQueryUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String getQuerySelection() {
        return MediaStore.Audio.AudioColumns.DATA + " like ?";
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
        QueryStateManager.getInstance().setUsb1MusicState(state);
    }

    @SuppressLint("Range")
    @Override
    protected String getData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
    }
}
