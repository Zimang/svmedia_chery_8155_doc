package com.desaysv.libradio.bean.dab;

import java.util.List;

/**
 * @author uidq1846
 * @desc 头条新闻信息
 * @time 2022-9-26 15:44
 */
public class DABHeadlineMessage {
    //新闻标题
    private String title;
    //当页的类型：1 –菜单（有下级目录）；2 –正文内容 ；3：无效 ；4：--菜单（无下级目录）
    private int type;
    //新闻内容/子标题
    private List<String> bodyList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getBodyList() {
        return bodyList;
    }

    public void setBodyList(List<String> bodyList) {
        this.bodyList = bodyList;
    }
}
