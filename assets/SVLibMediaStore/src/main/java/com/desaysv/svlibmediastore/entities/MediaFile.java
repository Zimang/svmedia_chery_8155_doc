package com.desaysv.svlibmediastore.entities;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.desaysv.svlibmediastore.dao.RecentlyMusicDao;
import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * @author uidq1846
 * @desc 媒体文件
 * @time 2022-11-30 15:56
 */
@Entity(tableName = "recent_media", indices = {@Index("path")})
public class MediaFile {
    /**
     * 数据库ID
     */
    @PrimaryKey
    private int id = -1;

    /**
     * 专辑图ID
     */
    private long albumId = -1;

    /**
     * 专辑名，ID3信息，只有音乐有
     */
    private String album = "";

    /**
     * 作者、艺术家 ID3信息，只有音乐有
     */
    private String artist = "";

    /**
     * 文件最后一次修改时间,文件信息，全部都有
     */
    private long dateModified;

    /**
     * 绝对路径，文件信息，全部都有
     */
    private String path = "";

    /**
     * 媒体时长，ID3信息，音乐和视频都有
     */
    private int duration = -1;

    /**
     * 歌曲名,电影名，ID3信息，只有音乐有
     */
    private String name = "";

    /**
     * 文件大小，文件信息，全部都有
     */
    private long size = -1;

    /**
     * 文件绝对路径，文件信息，全部都有
     */
    private String relativePath = "";

    /**
     * U盘的唯一值，UUID
     */
    private String deviceUUID = "";

    /**
     * 媒体的具体类型
     * {@link android.media.MediaFormat}
     */
    private String mimeType = "";

    /**
     * 文件名，带后缀，文件信息，全部都有
     */
    private String fileName = "";

    /**
     * 文件名字母缩写，用于排序，全部都有
     */
    private String sortLettersByFileName = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSortLettersByFileName() {
        return sortLettersByFileName;
    }

    public void setSortLettersByFileName(String sortLettersByFileName) {
        this.sortLettersByFileName = sortLettersByFileName;
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "id=" + id +
                ", albumId=" + albumId +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", dateModified=" + dateModified +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", relativePath='" + relativePath + '\'' +
                ", deviceUUID='" + deviceUUID + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", sortLettersByFileName='" + sortLettersByFileName + '\'' +
                '}';
    }

    /**
     * fileMessage转换
     *
     * @param fileMessage FileMessage
     * @return MediaFile
     */
    public static MediaFile fileMessageToMediaFile(FileMessage fileMessage) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(fileMessage.getId());
        mediaFile.setAlbumId(fileMessage.getAlbumId());
        mediaFile.setAlbum(fileMessage.getAlbum());
        mediaFile.setArtist(fileMessage.getAuthor());
        //存储当前操作时刻的时间,唯一区别的地方
        long timeMillis = System.currentTimeMillis();
        long latestModified = RecentlyMusicDao.getInstance().getLatestModified();
        //如果最新的时间小于等于数据库里边最近的时间，则重新赋值，考虑系统时间未同步
        if (timeMillis <= latestModified) {
            Log.d("MediaFile", "fileMessageToMediaFile: timeMillis = " + timeMillis + " latestModified = " + latestModified);
            timeMillis = latestModified + 1;
        }
        mediaFile.setDateModified(timeMillis);
        mediaFile.setPath(fileMessage.getPath());
        mediaFile.setDuration(fileMessage.getDuration());
        mediaFile.setName(fileMessage.getName());
        mediaFile.setFileName(fileMessage.getFileName());
        mediaFile.setSize(fileMessage.getSize());
        mediaFile.setRelativePath(fileMessage.getRootPath());
        mediaFile.setDeviceUUID(fileMessage.getDeviceUUID());
        mediaFile.setMimeType(fileMessage.getMimeType());
        mediaFile.setSortLettersByFileName(fileMessage.getSortLettersByFileName());
        Log.d("MediaFile", "fileMessageToMediaFile: MediaFile = " + mediaFile);
        return mediaFile;
    }

    /**
     * fileMessage转换
     *
     * @param mediaFile MediaFile
     * @return FileMessage
     */
    public static FileMessage mediaFileToFileMessage(MediaFile mediaFile) {
        FileMessage fileMessage = new FileMessage();
        fileMessage.setId(mediaFile.getId());
        fileMessage.setAlbumId(mediaFile.getAlbumId());
        fileMessage.setAlbum(mediaFile.getAlbum());
        fileMessage.setAuthor(mediaFile.getArtist());
        //存储当前操作时刻的时间,唯一区别的地方
        fileMessage.setLastModified(mediaFile.getDateModified());
        fileMessage.setPath(mediaFile.getPath());
        fileMessage.setDuration(mediaFile.getDuration());
        fileMessage.setName(mediaFile.getName());
        fileMessage.setSize(mediaFile.getSize());
        fileMessage.setRootPath(mediaFile.getRelativePath());
        fileMessage.setDeviceUUID(mediaFile.getDeviceUUID());
        fileMessage.setMimeType(mediaFile.getMimeType());
        fileMessage.setFileName(mediaFile.getFileName());
        fileMessage.setSortLettersByFileName(mediaFile.getSortLettersByFileName());
        return fileMessage;
    }
}
