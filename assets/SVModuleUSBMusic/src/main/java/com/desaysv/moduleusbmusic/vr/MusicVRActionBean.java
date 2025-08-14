package com.desaysv.moduleusbmusic.vr;

/**
 * @author uidq1846
 * @desc 语音下发媒体语音对应jscn字段
 * @time 2023-2-7 11:04
 */
public class MusicVRActionBean {
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

    @Override
    public String toString() {
        return "MusicVRActionBean{" +
                "user='" + user + '\'' +
                ", semantic=" + semantic +
                '}';
    }
}
