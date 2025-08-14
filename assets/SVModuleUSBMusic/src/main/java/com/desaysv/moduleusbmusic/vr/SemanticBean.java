package com.desaysv.moduleusbmusic.vr;

/**
 * @author uidq1846
 * @desc 语音下发媒体具体内容项
 * @time 2023-2-9 11:22
 */
public class SemanticBean {
    private String action;
    private String value;
    private String position;
    private String type;
    private String name;
    private String source;
    private String album;
    private String artist;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "SemanticBean{" +
                "action = '" + action + '\'' +
                ", value = '" + value + '\'' +
                ", position = '" + position + '\'' +
                ", type = '" + type + '\'' +
                ", name = '" + name + '\'' +
                ", source = '" + source + '\'' +
                ", album = '" + album + '\'' +
                ", artist = '" + artist + '\'' +
                '}';
    }
}
