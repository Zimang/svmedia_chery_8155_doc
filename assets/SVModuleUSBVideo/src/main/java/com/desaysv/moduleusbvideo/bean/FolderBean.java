package com.desaysv.moduleusbvideo.bean;

import androidx.annotation.NonNull;

import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * Description: 文件夹ListItem
 */
public class FolderBean {
    private static final String TAG = "FolderBean";

    private boolean isFolder = false;
    private String folderTitle = "";
    private String folderPath = "";

    private FileMessage video;

    /**
     * 文件夹构造方法
     *
     * @param isFolder isFolder
     * @param folderTitle folderTitle
     * @param folderPath folderPath
     */
    public FolderBean(boolean isFolder, String folderTitle, String folderPath) {
        this.isFolder = isFolder;
        this.folderTitle = folderTitle;
        this.folderPath = folderPath;
    }

    /**
     * 构造方法
     *
     * @param video video
     */
    public FolderBean(FileMessage video) {
        isFolder = false;
        this.video = video;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getFolderTitle() {
        return folderTitle;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public FileMessage getVideo() {
        if (video == null) {
            video = new FileMessage();
        }
        return video;
    }

    public void setVideo(FileMessage video) {
        this.video = video;
    }

    @NonNull
    @Override
    public String toString() {
        if (isFolder) {
            return "isFolder = true, folderTitle = " + this.folderTitle + ", folderPath = " + folderPath;
        } else {
            return "isFolder = false, fileMessage = " + video.toString();
        }
    }
}
