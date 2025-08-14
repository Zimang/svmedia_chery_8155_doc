package com.desaysv.moduleusbmusic.dataPoint;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author uidq1846
 * @desc 提交数据埋点的实体类
 * @time 2023-4-12 18:40
 */
public class UploadData {
    private String keyName;

    private String content;

    private String app_id;

    private String source;

    @SuppressLint("SimpleDateFormat")
    private String Timestamp = String.valueOf(System.currentTimeMillis());

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }
}
