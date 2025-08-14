package com.desaysv.moduleusbmusic.bean;

import com.desaysv.usbbaselib.bean.FileMessage;

/**
 * @author uidq1846
 * @desc 这个是文件夹和文件
 * @time 2022-12-5 10:16
 */
public class FolderItem {
    /**
     * 上级节点，用于跳转上一级
     */
    private String parentNotePath;

    /**
     * 当前目录节点
     */
    private String notePath;

    /**
     * 当前节点名称,用于显示
     */
    private String noteName;

    /**
     * 当前目录的话，子节点的size
     */
    private int childSize;

    /**
     * 当前曲目信息
     */
    private FileMessage fileMessage;

    public String getParentNotePath() {
        return parentNotePath;
    }

    public void setParentNotePath(String parentNotePath) {
        this.parentNotePath = parentNotePath;
    }

    public String getNotePath() {
        return notePath;
    }

    public void setNotePath(String notePath) {
        this.notePath = notePath;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public int getChildSize() {
        return childSize;
    }

    public void setChildSize(int childSize) {
        this.childSize = childSize;
    }

    public FileMessage getFileMessage() {
        return fileMessage;
    }

    public void setFileMessage(FileMessage fileMessage) {
        this.fileMessage = fileMessage;
    }

    public FolderItem copy(FolderItem item) {
        this.setNotePath(item.getNotePath());
        this.setNoteName(item.getNoteName());
        this.setParentNotePath(item.getParentNotePath());
        this.setFileMessage(item.getFileMessage());
        this.setChildSize(item.getChildSize());
        return this;
    }

    @Override
    public String toString() {
        return "FolderItem{" +
                "parentNotePath='" + parentNotePath + '\'' +
                ", notePath='" + notePath + '\'' +
                ", noteName='" + noteName + '\'' +
                ", childSize=" + childSize +
                ", fileMessage=" + fileMessage +
                '}';
    }
}
