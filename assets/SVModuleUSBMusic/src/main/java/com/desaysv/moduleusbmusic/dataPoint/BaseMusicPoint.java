package com.desaysv.moduleusbmusic.dataPoint;

import com.google.gson.JsonObject;

/**
 * @author uidq1846
 * @desc BaseMusicPoint
 * @time 2023-4-12 20:19
 */
public class BaseMusicPoint implements IMusicPoint {

    @Override
    public void upload(String keyName, String content) {
        upload(keyName, content, "LocalMusic", "desaysv");
    }

    @Override
    public void upload(String keyName, String content, String app_id, String source) {
        UploadData uploadData = new UploadData();
        uploadData.setKeyName(keyName);
        uploadData.setContent(content);
        uploadData.setApp_id(app_id);
        uploadData.setSource(source);
        PointManager.getInstance().uploadData(uploadData);
    }

    /**
     * @param field       操作的动作
     * @param contentData 内容集合
     * @return String
     */
    protected String getContentString(ContentData field, ContentData... contentData) {
        JsonObject jsonObject = new JsonObject();
        if (field != null) {
            jsonObject.addProperty(field.getContentKey(), field.getValue());
        }
        if (contentData != null && contentData.length > 0) {
            for (ContentData key : contentData) {
                jsonObject.addProperty(key.getContentKey(), key.getValue());
            }
        }
        return jsonObject.toString();
    }
}
