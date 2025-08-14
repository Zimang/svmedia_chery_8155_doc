package com.desaysv.modulebtmusic.bean;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @Description: 设备信息 device info
 * Common公共bean
 */
public class SVDevice implements Parcelable, Serializable {
    /**
     * 给UI层预留的专用字段
     */
    private Object uiCustomized;

    public Object getUiCustomized() {
        return uiCustomized;
    }

    public void setUiCustomized(Object uiCustomized) {
        this.uiCustomized = uiCustomized;
    }

    /**
     * 设备UUID
     */
    private ParcelUuid[] UUID;

    /**
     * 设备ID
     */
    private String deviceId = "";

    /**
     * 设备地址
     */
    private String address = "";
    /**
     * 设备名称
     */
    private String name = "";
    /**
     * 配对时间
     */
    private String pairTime = "";
    /**
     * 同步电话本时间
     */
    private String syncTime = "";
    /**
     * 设备类型
     */
    private int type;

    private int bondState;

    private int hfpState;

    private int a2dpState;

    private int mapState;

    private int pbapState;

    private boolean isFirst;

    private int deviceClass;

    public SVDevice() {
    }

    @SuppressLint("NewApi")
    protected SVDevice(Parcel in) {
        deviceId = in.readString();
        address = in.readString();
        name = in.readString();
        pairTime = in.readString();
        syncTime = in.readString();
        type = in.readInt();
        bondState = in.readInt();
        hfpState = in.readInt();
        a2dpState = in.readInt();
        mapState = in.readInt();
        pbapState = in.readInt();
        isFirst = in.readByte() != 0;
        Parcelable[] parcelables = in.readParcelableArray(ParcelUuid.class.getClassLoader());
        if (parcelables != null) {
            UUID = Arrays.copyOf(parcelables, parcelables.length, ParcelUuid[].class);
        }
        deviceClass = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(pairTime);
        dest.writeString(syncTime);
        dest.writeInt(type);
        dest.writeInt(bondState);
        dest.writeInt(hfpState);
        dest.writeInt(a2dpState);
        dest.writeInt(mapState);
        dest.writeInt(pbapState);
        dest.writeByte((byte) (isFirst ? 1 : 0));
        dest.writeParcelableArray(UUID, flags);
        dest.writeInt(deviceClass);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SVDevice> CREATOR = new Creator<SVDevice>() {
        @Override
        public SVDevice createFromParcel(Parcel in) {
            return new SVDevice(in);
        }

        @Override
        public SVDevice[] newArray(int size) {
            return new SVDevice[size];
        }
    };

    public ParcelUuid[] getUUID() {
        return UUID;
    }

    public void setUUID(ParcelUuid[] UUID) {
        this.UUID = UUID;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPairTime() {
        return pairTime;
    }

    public void setPairTime(String pairTime) {
        this.pairTime = pairTime;
    }

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    public int getHfpState() {
        return hfpState;
    }

    public void setHfpState(int hfpState) {
        this.hfpState = hfpState;
    }

    public int getA2dpState() {
        return a2dpState;
    }

    public void setA2dpState(int a2dpState) {
        this.a2dpState = a2dpState;
    }

    public int getMapState() {
        return mapState;
    }

    public void setMapState(int mapState) {
        this.mapState = mapState;
    }

    public int getPbapState() {
        return pbapState;
    }

    public void setPbapState(int pbapState) {
        this.pbapState = pbapState;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public void setDeviceClass(int deviceClass) {
        this.deviceClass = deviceClass;
    }

    public int getDeviceClass() {
        return deviceClass;
    }
}