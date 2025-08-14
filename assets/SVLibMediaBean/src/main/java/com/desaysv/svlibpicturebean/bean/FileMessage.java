package com.desaysv.svlibpicturebean.bean;

/**
 * 图片类型的数据结构
 */

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

public class FileMessage implements Serializable, Parcelable {

    private int _id = -1;
    private String name = "";//歌曲名,电影名
    private String time = "";//创建时间
    private String path = "";//绝对路径
    private Bitmap bitmap;//专辑图片,或者视频第一帧
    private String fileName = "";//文件名1.ext
    private String author = "";//作者
    private int isLove = -1;//是否收藏
    private String album = "";//专辑名
    private String type = "";//文件地址类型
    private FileFormat fileFormat;//文件类型
    private String album_path = "";//专辑图片,或者视频第一帧存储地址
    private int isSupport = 1;//平台是否支持
    private String sortLetters = "";// 首字母，用于排序
    private String sortLettersByAuthor;// 艺人首字母，用于排序

    private String sortLettersByAlbum;// 专辑首字母，用于排序
    private long size;//专辑图片,或者视频大小
    private long lastModified;// 上次修改时间
    private String duration; // 音乐时长
    private String mMediaId;
    private int fileDuration;//文件的总时长
    private boolean isSelected;


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getIsLove() {
        return isLove;
    }

    public void setIsLove(int isLove) {
        this.isLove = isLove;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getAlbum_path() {
        return album_path;
    }

    public void setAlbum_path(String album_path) {
        this.album_path = album_path;
    }

    public int getIsSupport() {
        return isSupport;
    }

    public void setIsSupport(int isSupport) {
        this.isSupport = isSupport;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getmMediaId() {
        return mMediaId;
    }

    public void setmMediaId(String mMediaId) {
        this.mMediaId = mMediaId;
    }

    public int getFileDuration() {
        return fileDuration;
    }

    public void setFileDuration(int fileDuration) {
        this.fileDuration = fileDuration;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._id);
        dest.writeString(this.name);
        dest.writeString(this.time);
        dest.writeString(this.path);
        dest.writeParcelable(this.bitmap, flags);
        dest.writeString(this.fileName);
        dest.writeString(this.author);
        dest.writeInt(this.isLove);
        dest.writeString(this.album);
        dest.writeString(this.type);
        dest.writeInt(this.fileFormat == null ? -1 : this.fileFormat.ordinal());
        dest.writeString(this.album_path);
        dest.writeInt(this.isSupport);
        dest.writeString(this.sortLetters);
        dest.writeString(this.sortLettersByAuthor);
        dest.writeString(this.sortLettersByAlbum);
        dest.writeLong(this.size);
        dest.writeLong(this.lastModified);
        dest.writeString(this.duration);
        dest.writeInt(this.fileDuration);
        dest.writeBoolean(this.isSelected);
    }

    public enum FileFormat{
        FOLDER,//文件夹
        MUSIC,//音乐文件
        VIDEO,//视频文件
        PICTURE,//图片文件
        TXT,//TXT文件
        OTHER,//其他文件
        ALL
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMessage that = (FileMessage) o;
        return _id == that._id &&
                isLove == that.isLove &&
                isSupport == that.isSupport &&
                size == that.size &&
                lastModified == that.lastModified &&
                Objects.equals(name, that.name) &&
                Objects.equals(time, that.time) &&
                Objects.equals(path, that.path) &&
                Objects.equals(bitmap, that.bitmap) &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(author, that.author) &&
                Objects.equals(album, that.album) &&
                Objects.equals(type, that.type) &&
                fileFormat == that.fileFormat &&
                Objects.equals(album_path, that.album_path) &&
                Objects.equals(sortLetters, that.sortLetters) &&
                Objects.equals(sortLettersByAuthor, that.sortLettersByAuthor) &&
                Objects.equals(duration, that.duration) &&
                Objects.equals(sortLettersByAlbum, that.sortLettersByAlbum) &&
                Objects.equals(fileDuration, that.fileDuration)&&
                isSelected == that.isSelected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, name, time, path,
                bitmap, fileName, author, isLove,
                album, type, fileFormat, album_path,
                isSupport, sortLetters, sortLettersByAuthor,
                sortLettersByAlbum, size, lastModified, duration, fileDuration,isSelected);
    }

    public FileMessage() {
    }

    protected FileMessage(Parcel in) {
        this._id = in.readInt();
        this.name = in.readString();
        this.time = in.readString();
        this.path = in.readString();
        this.bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        this.fileName = in.readString();
        this.author = in.readString();
        this.isLove = in.readInt();
        this.album = in.readString();
        this.type = in.readString();
        int tmpFileFormat = in.readInt();
        this.fileFormat = tmpFileFormat == -1 ? null : FileFormat.values()[tmpFileFormat];
        this.album_path = in.readString();
        this.isSupport = in.readInt();
        this.sortLetters = in.readString();
        this.sortLettersByAuthor = in.readString();
        this.sortLettersByAlbum = in.readString();
        this.size = in.readLong();
        this.lastModified = in.readLong();
        this.duration = in.readString();
        this.fileDuration = in.readInt();
        this.isSelected = in.readBoolean();
    }

    public static final Parcelable.Creator<FileMessage> CREATOR = new Parcelable.Creator<FileMessage>() {
        @Override
        public FileMessage createFromParcel(Parcel source) {
            return new FileMessage(source);
        }

        @Override
        public FileMessage[] newArray(int size) {
            return new FileMessage[size];
        }
    };
}
