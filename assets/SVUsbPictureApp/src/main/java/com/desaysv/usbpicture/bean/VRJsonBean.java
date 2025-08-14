package com.desaysv.usbpicture.bean;

/**
* Created by ZNB on 2022-05-09
 * 解析语义的json信息
 * 如果后续项目更新了通用的实体类，
 * 那就直接用项目的
 */
public class VRJsonBean {

    @Override
    public String toString() {
        return "VRJsonBean{" +
                "user='" + user + '\'' +
                ", action=" + this.semantic.action + '\'' +
                ", value=" + this.semantic.value + '\'' +
                ", type=" + this.semantic.type + '\'' +
                ", position=" + this.semantic.position + '\''
                + '}';
    }

    private String user;
    private SemanticBean semantic;

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
        private String action;
        private String position;
        private String type;
        private String value;

        public SemanticBean() {
        }

        public String getAction() {
            return this.action;
        }

        public void setAction(String var1) {
            this.action = var1;
        }

        public String getPosition() {
            return this.position;
        }

        public void setPosition(String var1) {
            this.position = var1;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String var1) {
            this.type = var1;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String var1) {
            this.value = var1;
        }
    }
}
