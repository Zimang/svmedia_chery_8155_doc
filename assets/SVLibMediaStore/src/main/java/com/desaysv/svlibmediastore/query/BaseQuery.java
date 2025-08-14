package com.desaysv.svlibmediastore.query;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 查询抽象基本类
 * @time 2022-11-15 9:43
 */
public abstract class BaseQuery implements IQuery {
    protected final String TAG = this.getClass().getSimpleName();
    protected Context context;
    protected ContentResolver contentResolver;

    @Override
    public void init(Context context) {
        this.context = context;
        initResolver();
    }

    private void initResolver() {
        contentResolver = context.getContentResolver();
    }

    @Override
    public List<FileMessage> queryMediaList(String storagePath) {
        //把查询筛选判断放进去
        return queryMediaList(getProjection(), getSelection(), getSelectionArgs(storagePath), getOrder());
    }

    @Override
    public List<FileMessage> queryMediaList(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "queryMediaList: start");
        Cursor cursor = contentResolver.query(getMediaUri(), projection, selection, selectionArgs, sortOrder);
        List<FileMessage> queryList = query(cursor);
        cursor.close();
        return queryList;
    }

    /**
     * 获取默认查询要显示的行
     *
     * @return String[]
     */
    protected abstract String[] getProjection();

    /**
     * 获取默认的条件规则
     *
     * @return String
     */
    protected abstract String getSelection();

    /**
     * 获取默认的条件参数
     *
     * @return String[]
     */
    protected abstract String[] getSelectionArgs(String storagePath);

    /**
     * 获取默认的排序规则
     *
     * @return String
     */
    protected abstract String getOrder();

    /**
     * 获取各自对应的列表、媒体各自节点还是有谢谢许差异
     *
     * @param cursor cursor
     */
    protected abstract List<FileMessage> query(Cursor cursor);

    /**
     * 获取相应媒体的URI
     *
     * @return Uri
     */
    protected abstract Uri getMediaUri();


    public boolean isDataType = false;

    /**
     * 设置过滤文件后缀格式还是过滤mime-type格式
     * @param dataType
     */
    @Override
    public void setDataType(boolean dataType) {
        Log.i(TAG, "setDataType: dataType: " + dataType);
        isDataType = dataType;
    }
}
