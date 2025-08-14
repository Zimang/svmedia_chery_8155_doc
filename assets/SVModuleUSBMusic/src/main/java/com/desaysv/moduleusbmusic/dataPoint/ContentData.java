package com.desaysv.moduleusbmusic.dataPoint;

/**
 * @author uidq1846
 * @desc keyName事件所对应的Field对应的键值
 * @time 2023-4-12 18:42
 */
public class ContentData {
    private String contentKey;
    private String value;

    public ContentData() {

    }

    public ContentData(String contentKey, String value) {
        this.contentKey = contentKey;
        this.value = value;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
