package com.desaysv.moduleradio.vr;

/**
 * created by ZNB on 2023-01-11
 * 用来封装上传给语音的数据
 */
public class SVRadioResponseBean {

    private String messageType = "PUSH";

    private String focus = "radio";

    private String requestCode = "10001";

    private String version = "v1.0";

    private String operationApp = "com.desaysv.svaudioapp";

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

        private DataInfo dataInfo;

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

        public DataInfo getDataInfo() {
            return dataInfo;
        }

        public void setDataInfo(DataInfo dataInfo) {
            this.dataInfo = dataInfo;
        }

        public static class DataInfo{
            private String code = "87.5";

            private String name = "Unknown";

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

    }

    public String toString() {
        return "SVRadioResponseBean{messageType='" + this.messageType + '\'' + ", focus=" + this.focus + '\''
                + ", activeStatus=" + this.data.activeStatus + '\'' + ", sceneStatus=" + this.data.sceneStatus + '\''
                + ", dataInfo.code=" + this.data.dataInfo.code + '\'' + ", dataInfo.name=" + this.data.dataInfo.name + '\''+ '}';
    }
}
