package com.desaysv.svlibpicturebean.bean;

/**
 * 文件夹类型的数据结构
 */
public class FolderMessage {

    private String path;
    private String name;
    private String parentPath;
    private int count; //文件夹包含的图片数量

    /**
     * 文件夹包含的图片数量
     * @return
     */
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 文件夹所处的目录
     * @return
     */
    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * 文件路径
     * @return
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
