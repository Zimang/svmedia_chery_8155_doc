package com.desaysv.localmediasdk.bean;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

/**
 * created by ZNB on 2022-11-16
 * 序列化的数据结构，AIDL传递列表数据时使用
 * 和MediaInfoBean类似，只是新建一个以做区分
 * 各个模块根据各自的数据接口可以扩展这里的内容
 * 例如 RadioMessage 需要对应转成需要的内容
 */
public class RemoteBean implements Parcelable {

    private String source;

    private String title;

    private String album;

    private String artist;

    private int duration;

    private boolean collectStatus;

    private String frequency;//频点值或者dab的名称

    private String ensembleLabel;//dab的类别

    private String serviceId;//dab的服务id

    private String componentId;//dab的组件id

    private String path;//如果是文件，则填充路径


    public RemoteBean() {
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected RemoteBean(Parcel in) {
        radFromParcel(in);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void radFromParcel(Parcel in){
        source = in.readString();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        duration = in.readInt();
        collectStatus = in.readBoolean();
        frequency = in.readString();
        ensembleLabel = in.readString();
        serviceId = in.readString();
        componentId = in.readString();
        path = in.readString();
    }

    public static final Creator<RemoteBean> CREATOR = new Creator<RemoteBean>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public RemoteBean createFromParcel(Parcel in) {
            return new RemoteBean(in);
        }

        @Override
        public RemoteBean[] newArray(int size) {
            return new RemoteBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeInt(duration);
        dest.writeBoolean(collectStatus);
        dest.writeString(frequency);
        dest.writeString(ensembleLabel);
        dest.writeString(serviceId);
        dest.writeString(componentId);
        dest.writeString(path);
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isCollectStatus() {
        return collectStatus;
    }

    public void setCollectStatus(boolean collectStatus) {
        this.collectStatus = collectStatus;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getEnsembleLabel() {
        return ensembleLabel;
    }

    public void setEnsembleLabel(String ensembleLabel) {
        this.ensembleLabel = ensembleLabel;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
