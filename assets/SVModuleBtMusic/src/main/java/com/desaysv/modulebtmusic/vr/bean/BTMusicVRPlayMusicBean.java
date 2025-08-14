package com.desaysv.modulebtmusic.vr.bean;

public class BTMusicVRPlayMusicBean {

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
        public String name;
        public String artist;
        public String action;
        public String album;
        public String position;
        public String source;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "SemanticBean{" +
                    "name='" + name + '\'' +
                    "artist='" + artist + '\'' +
                    "action='" + action + '\'' +
                    "album='" + album + '\'' +
                    "position='" + position + '\'' +
                    "source='" + source + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BTMusicVRPlayMusicBean{" +
                "user='" + user + '\'' +
                ", semantic=" + semantic +
                '}';
    }
}
