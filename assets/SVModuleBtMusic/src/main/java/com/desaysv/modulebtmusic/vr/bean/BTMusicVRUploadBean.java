package com.desaysv.modulebtmusic.vr.bean;

public class BTMusicVRUploadBean {

    private final String messageType = "PUSH";
    private final String focus = "music";
    private final String requestCode = "10001";
    private data data;

    public static class data {
        public static class dataInfo {
            private String artist = "";
            private String song = "";

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

            @Override
            public String toString() {
                return "dataInfo{" +
                        "artist='" + artist + '\'' +
                        ", song='" + song + '\'' +
                        '}';
            }
        }

        private dataInfo dataInfo;
        private String activeStatus = "bg";//fg
        private String sceneStatus = "pause";//playing

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

        public dataInfo getDataInfo() {
            return dataInfo;
        }

        public void setDataInfo(dataInfo dataInfo) {
            this.dataInfo = dataInfo;
        }

        @Override
        public String toString() {
            return "data{" +
                    "dataInfo='" + dataInfo + '\'' +
                    ", activeStatus='" + activeStatus + '\'' +
                    ", sceneStatus='" + sceneStatus + '\'' +
                    '}';
        }
    }

    private final String version = "v1.0";
    private final String operationApp = "system";

    public String getMessageType() {
        return messageType;
    }

    public String getFocus() {
        return focus;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public data getData() {
        return data;
    }

    public void setData(data data) {
        this.data = data;
    }

    public String getVersion() {
        return version;
    }

    public String getOperation() {
        return operationApp;
    }

    @Override
    public String toString() {
        return "BTMusicVRUploadBean{" +
                "messageType='" + messageType + '\'' +
                ", focus='" + focus + '\'' +
                ", requestCode='" + requestCode + '\'' +
                ", data='" + data + '\'' +
                ", version='" + version + '\'' +
                ", operation='" + operationApp + '\'' +
                '}';
    }
}
