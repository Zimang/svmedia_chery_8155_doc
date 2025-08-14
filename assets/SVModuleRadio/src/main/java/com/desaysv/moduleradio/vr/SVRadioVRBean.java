package com.desaysv.moduleradio.vr;

/**
 * created by ZNB on 2022-12-23
 * 解析语义的String为对应的数据
 */
public class SVRadioVRBean {
    //给个默认值，避免空指针
    public String user = "";//对Radio来讲，user不需要

    public SemanticBean semantic;//实际有效的数据都在这里

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

    public String toString() {
        return "SVRadioVRBean{user='" + this.user + '\'' + ", action=" + this.semantic.action + '\'' + ", value=" + this.semantic.value + '\'' + ", type=" + this.semantic.type + '\'' + ", position=" + this.semantic.position + '\'' + '}';
    }

    public static class SemanticBean{
        //给个默认值，避免空指针
        public String action = "";
        public String position = "";
        public String type = "";
        public String value = "";

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
    }

}
