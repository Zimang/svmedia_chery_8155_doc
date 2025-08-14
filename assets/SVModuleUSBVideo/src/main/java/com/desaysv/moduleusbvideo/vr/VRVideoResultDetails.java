package com.desaysv.moduleusbvideo.vr;

/**
 * created by ZNB for VR info new req on 2021-12-15
 * notify video status
 */
public class VRVideoResultDetails{

    private String messageType = "PUSH";

    private String focus = "video";

    private String requestCode = "10001";

    private String version = "v1.0";

    private String operationApp = "com.desaysv.videoapp";

    private Data data;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data{

        private String activeStatus = "fg";

        private String sceneStatus = "playing";//paused

        public String getActiveStatus() {
            return activeStatus;
        }

        public void setActiveStatus(String activeStatus) {
            this.activeStatus = activeStatus;
        }

        public String getSceneStatus() {
            return sceneStatus;
        }

        public void setSceneStatus(String sceneStatus) {
            this.sceneStatus = sceneStatus;
        }

    }


}
