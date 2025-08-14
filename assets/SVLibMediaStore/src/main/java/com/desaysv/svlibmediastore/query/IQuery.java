package com.desaysv.svlibmediastore.query;

import android.content.Context;

import com.desaysv.usbbaselib.bean.FileMessage;

import java.util.List;

/**
 * @author uidq1846
 * @desc 查询数据的接口、用于查询原生数据库数据
 * @time 2022-11-15 9:42
 */
public interface IQuery {

    /**
     * 初始化，注入上下文对象
     *
     * @param context context
     */
    void init(Context context);

    /**
     * 查询媒体数据，默认的方式是查询全部列表
     *
     * @param storagePath 查询具体盘符数据
     * @return List<FileMessage>
     */
    List<FileMessage> queryMediaList(String storagePath);

    /**
     * 查询指定参数的数据库列表
     *
     * @param projection    projection A list of which columns to return.
     *                      Passing null will return all columns, which is inefficient.
     * @param selection     selection A filter declaring which rows to return,
     *                      formatted as an SQL WHERE clause (excluding the WHERE itself).
     *                      Passing null will return all rows for the given URI.
     * @param selectionArgs selectionArgs The values will be bound as Strings.
     * @param sortOrder     sortOrder 排序规则
     * @return List<FileMessage>
     */
    List<FileMessage> queryMediaList(String[] projection, String selection, String[] selectionArgs, String sortOrder);

    void setDataType(boolean dataType);

}
