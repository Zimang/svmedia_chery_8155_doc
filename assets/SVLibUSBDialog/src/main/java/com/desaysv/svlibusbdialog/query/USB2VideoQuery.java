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

import java.util.List;

public class USB2VideoQuery extends BaseQuery {

    public USB2VideoQuery(Context mContext) {
        super(mContext);
    }

    @Override
    protected Uri getQueryUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String getQuerySelection() {
        // 增加过滤U盘系统文件夹扫描
        return MediaStore.Video.VideoColumns.RELATIVE_PATH + " != 'System Volume Information/' and " +
                MediaStore.Video.VideoColumns.DATA + " like ? and " + MimeMatcher.VideoMatcher.getMimeTypeVideo();
    }

    @Override
    protected String[] getArgs() {
        List<String> list = MimeMatcher.VideoMatcher.getMediaVideoType();
        String[] str = new String[list.size() + 1];
        str[0] = "%" + getPath() + "%";
        for (int i = 1; i <= list.size(); i++) {
            str[i] = list.get(i - 1);
        }
        return str;
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
        QueryStateManager.getInstance().setUsb2VideoState(state);
    }

    @SuppressLint("Range")
    @Override
    protected String getData(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
    }

    @Override
    protected boolean filter(Cursor mCursor, int scanState) {
        @SuppressLint("Range") String mimeType = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
        return !MimeMatcher.VideoMatcher.isSupport(mimeType);// 不支持的即表示需要过滤
    }
}
