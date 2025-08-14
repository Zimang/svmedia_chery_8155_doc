package com.desaysv.svlibmediaobserver.bean;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by ZNB on 2023-06-15，用于应用内数据传递，所以比较精简
 */
public class MediaInfoBean{

    /**
     * 私有构造，不让直接创建对象
     */
    private MediaInfoBean() {
    }

    /**
     * 音源
     */
    private String source = "";

    /**
     * 标题
     */
    private String title = "";

    /**
     * 专辑Uri
     */
    private String albumUri;

    /**
     * 频点值
     */
    private String freq = "";

    /**
     * band信息
     */
    private Integer band;

    /**
     * 播放状态
     */
    private boolean isPlaying;

    /**
     * 绝对路劲
     */
    private String path = "";


    /**
     * 专辑图数据，主要用于DAB
     */
    private byte[] bytes;

    /**
     * 服务id，主要用于DAB
     */
    private long serviceID;

    /**
     * 服务组件id，主要用于DAB
     */
    private int serviceComID;

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbumUri() {
        return albumUri;
    }

    public String getFreq() {
        return freq;
    }

    public Integer getBand() {
        return band;
    }

    public boolean getPlaying() {
        return isPlaying;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbumUri(String albumUri) {
        this.albumUri = albumUri;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public void setBand(Integer band) {
        this.band = band;
    }

    public void setPlaying(Boolean playing) {
        isPlaying = playing;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public long getServiceID() {
        return serviceID;
    }

    public void setServiceID(long serviceID) {
        this.serviceID = serviceID;
    }

    public int getServiceComID() {
        return serviceComID;
    }

    public void setServiceComID(int serviceComID) {
        this.serviceComID = serviceComID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaInfoBean that = (MediaInfoBean) o;
        return Objects.equals(source, that.source) && Objects.equals(path, that.path) && Objects.equals(freq, that.freq)
                && Objects.equals(band, that.band) && Objects.equals(serviceID, that.serviceID) && Objects.equals(serviceComID, that.serviceComID) && Objects.equals(title, that.title)
                && Objects.equals(isPlaying, that.isPlaying);
    }

    @Override
    public String toString() {
        return "MediaInfoBean{" +
                "source='" + source + '\'' +
                ", title='" + title + '\'' +
                ", albumUri='" + albumUri + '\'' +
                ", freq='" + freq + '\'' +
                ", band=" + band +
                ", isPlaying=" + isPlaying +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }

    public static class Builder{
        /**
         * 音源
         */
        private String source;

        /**
         * 标题
         */
        private String title;

        /**
         * 专辑Uri
         */
        private String albumUri;

        /**
         * 频点值
         */
        private String freq;

        /**
         * band信息
         */
        private Integer band;

        /**
         * 播放状态
         */
        private boolean isPlaying;


        /**
         * 专辑图数据，主要用于DAB
         */
        private byte[] bytes;

        /**
         * 服务id，主要用于DAB
         */
        private long serviceID;

        /**
         * 服务组件id，主要用于DAB
         */
        private int serviceComID;

        /**
         * 绝对路径
         */
        private String path;

        public void setSource(String source) {
            this.source = source;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setAlbumUri(String albumUri) {
            this.albumUri = albumUri;
        }

        public void setFreq(String freq) {
            this.freq = freq;
        }

        public void setBand(Integer band) {
            this.band = band;
        }

        public void setPlaying(boolean playing) {
            this.isPlaying = playing;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public void setServiceID(long serviceID) {
            this.serviceID = serviceID;
        }

        public void setServiceComID(int serviceComID) {
            this.serviceComID = serviceComID;
        }

        public  MediaInfoBean Build(){
            MediaInfoBean mediaInfoBean = new MediaInfoBean();
            if (this.source != null){
                mediaInfoBean.setSource(this.source);
            }
            if (this.title != null){
                mediaInfoBean.setTitle(this.title);
            }
            if (this.albumUri != null){
                mediaInfoBean.setAlbumUri(this.albumUri);
            }
            if (this.freq != null){
                mediaInfoBean.setFreq(this.freq);
            }
            if (this.band != null){
                mediaInfoBean.setBand(this.band);
            }
            mediaInfoBean.setPlaying(this.isPlaying);
            if (this.bytes != null){
                mediaInfoBean.setBytes(this.bytes);
            }
            if (this.path != null){
                mediaInfoBean.setPath(this.path);
            }
            mediaInfoBean.setServiceID(this.serviceID);
            mediaInfoBean.setServiceComID(this.serviceComID);
            return mediaInfoBean;
        }
    }

}
