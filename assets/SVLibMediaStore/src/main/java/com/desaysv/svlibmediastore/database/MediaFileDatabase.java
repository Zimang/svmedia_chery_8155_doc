package com.desaysv.svlibmediastore.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.desaysv.svlibmediastore.dao.IMediaFileDao;
import com.desaysv.svlibmediastore.entities.MediaFile;

/**
 * @author uidq1846
 * @desc 创建数据库The Room database that contains the MediaFile table
 * 该类必须带有 @Database 注解，该注解包含列出所有与数据库关联的数据实体的 entities 数组。
 * 该类必须是一个抽象类，用于扩展 RoomDatabase。
 * @time 2022-12-1 10:23
 */
@Database(entities = {MediaFile.class}, version = 1)
public abstract class MediaFileDatabase extends RoomDatabase {

    /**
     * 获取控制类
     * 对于与数据库关联的每个 DAO 类，数据库类必须定义一个具有零参数的抽象方法，并返回 DAO 类的实例。
     *
     * @return IMediaFileDao
     */
    public abstract IMediaFileDao getMediaFileDao();
}
