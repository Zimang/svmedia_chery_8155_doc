package com.desaysv.modulebtmusic.vr.bean;

public class BTMusicVRBean {

    public String user;
    public SemanticBean semantic;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public SemanticBean getSemantic() {
        return semantic;
    }

    public void setSemantic(SemanticBean semantic) {
        this.semantic = semantic;
    }

    public static class SemanticBean {
        public String action;
        public String position;
        public String type;
        public String value;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "SemanticBean{" +
                    "action='" + action + '\'' +
                    ", position='" + position + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BTMusicVRBean{" +
                "user='" + user + '\'' +
                ", semantic=" + semantic +
                '}';
    }
}
