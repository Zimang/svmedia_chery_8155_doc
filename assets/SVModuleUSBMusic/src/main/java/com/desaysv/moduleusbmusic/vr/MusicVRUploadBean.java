package com.desaysv.moduleusbmusic.vr;

/**
 * @author uidq1846
 * @desc 媒体上传语音对应jscn字段
 * @time 2023-2-7 11:04
 */
public class MusicVRUploadBean{
    private String messageType = "PUSH";

    private String focus = "music";

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
            private String artist;
            private String song;

            public String getArtist() {
                return artist;
            }

            public void setArtist(String artist) {
                this.artist = artist;
            }

            public String getSong() {
                return song;
            }

            public void setSong(String song) {
                this.song = song;
            }
        }

    }

    public String toString() {
        return "MusicVRUploadBean{messageType='" + this.messageType + '\'' + ", focus=" + this.focus + '\''
                + ", activeStatus=" + this.data.activeStatus + '\'' + ", sceneStatus=" + this.data.sceneStatus + '\''
                + ", dataInfo.song=" + this.data.dataInfo.song + '\'' + ", dataInfo.artist=" + this.data.dataInfo.artist + '\''+ '}';
    }

}
