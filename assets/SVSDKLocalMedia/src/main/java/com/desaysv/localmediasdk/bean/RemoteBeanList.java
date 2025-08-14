package com.desaysv.localmediasdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * created by ZNB on 2022-11-16
 * 序列化的数据结构，AIDL传递列表数据时使用
 * 客户端get或服务端回调列表都可以
 */
public class RemoteBeanList implements Parcelable {

    private String source = "";//音源类型

    private String type = "";//列表类型，有效列表或者收藏列表等

    private List<RemoteBean> remoteBeanList = new ArrayList<>();

    public RemoteBeanList() {
    }

    protected RemoteBeanList(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in){
        source = in.readString();
        type = in.readString();
        remoteBeanList = in.createTypedArrayList(RemoteBean.CREATOR);//注意这里用的是RemoteBean的creator
    }

    public static final Creator<RemoteBeanList> CREATOR = new Creator<RemoteBeanList>() {
        @Override
        public RemoteBeanList createFromParcel(Parcel in) {
            return new RemoteBeanList(in);
        }

        @Override
        public RemoteBeanList[] newArray(int size) {
            return new RemoteBeanList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
        dest.writeString(type);
        dest.writeTypedList(remoteBeanList);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RemoteBean> getRemoteBeanList() {
        return remoteBeanList;
    }

    public void setRemoteBeanList(List<RemoteBean> remoteBeanList) {
        this.remoteBeanList = remoteBeanList;
    }
}
