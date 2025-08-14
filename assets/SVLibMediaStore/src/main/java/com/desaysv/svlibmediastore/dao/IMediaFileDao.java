package com.desaysv.svlibmediastore.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.desaysv.svlibmediastore.entities.MediaFile;

import java.util.List;

/**
 * @author uidq1846
 * @desc 最近列表媒体数据库
 * @time 2022-11-30 19:15
 */
@Dao
public interface IMediaFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void update(MediaFile... mediaFiles);

    @Delete
    void delete(MediaFile... mediaFiles);

    @Delete
    void delete(List<MediaFile> mediaFiles);

    @Query("SELECT * FROM recent_media where id > 0 order by dateModified DESC limit:size")
    List<MediaFile> queryAll(int size);

    @Query("SELECT * FROM recent_media where id > 0 order by dateModified DESC limit:start,:end")
    List<MediaFile> queryAll(int start, int end);
}
