package com.desaysv.usbbaselib.bean;

import java.io.Serializable;

/**
 * date ：2016-10-12
 * function：媒体文件信息
 *
 * @author uidp5370
 */
public class FileMessage implements Serializable {

    public static final String MUSIC_TYPE = "music_type";

    public static final String VIDEO_TYPE = "video_type";

    public static final String PICTURE_TYPE = "picture_type";

    @Override
    public String toString() {
        return "FileMessage{" +
                " id = '" + id + '\'' +
                " name = '" + name + '\'' +
                ", path = '" + path + '\'' +
                ", deviceUUID = '" + deviceUUID + '\'' +
                '}';
    }

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

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getIsLove() {
        return isLove;
    }

    public void setIsLove(int isLove) {
        this.isLove = isLove;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSortLettersByName() {
        return sortLettersByName;
    }

    public void setSortLettersByName(String sortLettersByName) {
        this.sortLettersByName = sortLettersByName;
    }

    public String getSortLettersByAuthor() {
        return sortLettersByAuthor;
    }

    public void setSortLettersByAuthor(String sortLettersByAuthor) {
        this.sortLettersByAuthor = sortLettersByAuthor;
    }

    public String getSortLettersByAlbum() {
        return sortLettersByAlbum;
    }

    public void setSortLettersByAlbum(String sortLettersByAlbum) {
        this.sortLettersByAlbum = sortLettersByAlbum;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public void setMediaType(String mMediaType) {
        this.mMediaType = mMediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * 数据库ID
     */
    private int id = -1;

    /**
     * 专辑图ID
     */
    private long albumId = -1;

    /**
     * U盘的唯一值，UUID
     */
    private String deviceUUID = "";

    /**
     * 文件绝对路径，文件信息，全部都有
     */
    private String rootPath = "";

    /**
     * 文件最后一次修改时间,文件信息，全部都有
     */
    private long lastModified;

    /**
     * 文件名，带后缀，文件信息，全部都有
     */
    private String fileName = "";

    /**
     * 文件名字母缩写，用于排序，全部都有
     */
    private String sortLettersByFileName = "";

    /**
     * 绝对路径，文件信息，全部都有
     */
    private String path = "";

    /**
     * 文件大小，文件信息，全部都有
     */
    private long size = -1;

    /**
     * 是否收藏，收藏属性，使用默认的，待定，看后面是否要存数据库
     */
    private int isLove = -1;

    /**
     * 歌曲名,电影名，ID3信息，只有音乐有
     */
    private String name = "";

    /**
     * 作者，ID3信息，只有音乐有
     */
    private String author = "";

    /**
     * 专辑名，ID3信息，只有音乐有
     */
    private String album = "";

    /**
     * 歌曲名拼音或者字母，用于排序，只有音乐有
     */
    private String sortLettersByName = "";

    /**
     * 艺术家拼音或者字母，用于排序，用于排序，只有音乐有
     */
    private String sortLettersByAuthor = "";

    /**
     * 专辑拼音或者字母，用于排序，只有音乐有
     */
    private String sortLettersByAlbum = "";

    /**
     * 媒体时长，ID3信息，音乐和视频都有
     */
    private int duration = -1;

    /**
     * 媒体的类型，是音乐，视频还是图片，不需要存数据库
     */
    private String mMediaType = "";

    /**
     * 媒体的具体类型
     * {@link android.media.MediaFormat}
     */
    private String mimeType = "";
}
