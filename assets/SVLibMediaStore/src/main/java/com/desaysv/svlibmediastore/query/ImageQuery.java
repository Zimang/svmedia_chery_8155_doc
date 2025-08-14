package com.desaysv.svlibmediastore.query;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 图片数据查询
 * @time 2022-11-15 9:49
 */
public class ImageQuery extends BaseQuery {
    private static IQuery query;

    private ImageQuery() {
    }

    public static IQuery getInstance() {
        if (query == null) {
            synchronized (ImageQuery.class) {
                if (query == null) {
                    query = new ImageQuery();
                }
            }
        }
        return query;
    }

    @Override
    protected String[] getProjection() {
        return new String[0];
    }

    @Override
    protected String getSelection() {
        return null;
    }

    @Override
    protected String[] getSelectionArgs(String path) {
        return new String[0];
    }

    @Override
    protected String getOrder() {
        return MediaStore.Images.Media.TITLE + " ASC";
    }

    @Override
    protected List<FileMessage> query(Cursor cursor) {
        return null;
    }

    @Override
    protected Uri getMediaUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }
}
