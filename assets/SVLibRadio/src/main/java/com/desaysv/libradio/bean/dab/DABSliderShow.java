package com.desaysv.libradio.bean.dab;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * @author uidq4079
 * @desc 底层解析当前获取到的所有 DAB DABSliderShow，应用做对应保存，显示时没有就用保存的DABSliderShow
 * @time 2022-9-22 19:20
 */
public class DABSliderShow implements Parcelable, Serializable {
    //频点值 PS：字段是和底层约定好的有些需要上抛就都给出去吧
    private int frequency;
    //电台服务id
    private long serviceId;
    //电台组件id
    private int serviceComponentId;
    private int slsLen;
    //数据数组
    private byte[] slsDataList;

    public DABSliderShow() {
    }
    /**
     * 获取DAB的频点值
     *
     * @return frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * 赋值DAB频点值
     *
     * @param frequency frequency
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }


    /**
     * 获取服务ID
     *
     * @return serviceId
     */
    public long getServiceId() {
        return serviceId;
    }

    /**
     * 服务ID
     *
     * @param serviceId serviceId
     */
    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 服务组件ID
     *
     * @return serviceComponentId
     */
    public int getServiceComponentId() {
        return serviceComponentId;
    }

    /**
     * 服务组件ID
     *
     * @param serviceComponentId serviceComponentId
     */
    public void setServiceComponentId(int serviceComponentId) {
        this.serviceComponentId = serviceComponentId;
    }


    public int getSlsLen() {
        return slsLen;
    }

    public void setSlsLen(int slsLen) {
        this.slsLen = slsLen;
    }

    public byte[] getSlsDataList() {
        return slsDataList;
    }

    public void setSlsDataList(byte[] slsDataList) {
        this.slsDataList = slsDataList;
    }

    protected DABSliderShow(Parcel in) {
        frequency = in.readInt();
        serviceId = in.readLong();
        serviceComponentId = in.readInt();
        slsLen = in.readInt();
        slsDataList = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(frequency);
        dest.writeLong(serviceId);
        dest.writeInt(serviceComponentId);
        dest.writeInt(slsLen);
        dest.writeByteArray(slsDataList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DABSliderShow> CREATOR = new Creator<DABSliderShow>() {
        @Override
        public DABSliderShow createFromParcel(Parcel in) {
            return new DABSliderShow(in);
        }

        @Override
        public DABSliderShow[] newArray(int size) {
            return new DABSliderShow[size];
        }
    };

    @Override
    public String toString() {
        return "DABMessage{" +
                "frequency=" + frequency +
                ", serviceId=" + serviceId +
                ", serviceComponentId=" + serviceComponentId +
                ", slsLen=" + slsLen +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DABSliderShow that = (DABSliderShow) obj;
        if (frequency == that.getFrequency() && serviceId == that.getServiceId() && serviceComponentId == that.getServiceComponentId()){
            return true;
        }
        return false;
    }
}
