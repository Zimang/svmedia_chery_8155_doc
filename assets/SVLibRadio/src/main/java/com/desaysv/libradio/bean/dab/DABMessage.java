package com.desaysv.libradio.bean.dab;

import android.os.Parcel;
import android.os.Parcelable;

import com.desaysv.libradio.bean.RadioList;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author uidq1846
 * @desc 数字电台信息
 * @time 2022-9-22 19:20
 */
public class DABMessage implements Parcelable, Serializable {
    //频点值 PS：字段是和底层约定好的有些需要上抛就都给出去吧
    private int frequency;
    //电台名称
    private String programStationName;
    //电台所属类别
    private String ensembleLabel;
    //电台所属集合
    private int programType;
    //电台id
    private long serviceId;
    //电台组件id
    private int serviceComponentId;
    //集合id
    private int ensembleId;
    //集合名称中可做简称字符标识（共16bit，bit =1表示对应字符可显示）
    private int ensembleLableFlag;
    //服务名称中可做简称字符标识（共16bit，bit =1表示对应字符可显示）
    private int proStaNameFlag;
    //表示当前服务是否是sub 服务属性  0 非sub服务属性 1 子sub服务属性
    private int subServiceFlag;
    //数据长度，logo数据
    private int logoLen;
    //数据数组
    private byte[] logoDataList;
    //数据长度，专辑图数据
    private int slsLen;
    //数据数组
    private byte[] slsDataList;
    //radio text
    private String dynamicLabel;
    //radio text plus，这里一般是多条文本信息拼接组装成为一条显示
    private String dynamicPlusLabel;

    //电台名称的简称
    //优先使用简称
    private String shortProgramStationName;

    //电台集合名称的简称
    //优先使用简称
    private String shortEnsembleLabel;

    /**
     * 打开电台至少需要以下三项状态
     *
     * @param frequency          frequency
     * @param serviceId          serviceId
     * @param serviceComponentId serviceComponentId
     */
    public DABMessage(int frequency, long serviceId, int serviceComponentId) {
        setFrequency(frequency);
        setServiceId(serviceId);
        setServiceComponentId(serviceComponentId);
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
     * 电台(服务名称)
     *
     * @return programStationName
     */
    public String getProgramStationName() {
        return programStationName;
    }

    /**
     * 电台(服务名称)
     *
     * @param programStationName programStationName
     */
    public void setProgramStationName(String programStationName) {
        this.programStationName = programStationName;
    }

    /**
     * 电台集合名称，也是电台类别
     *
     * @return ensembleLabel
     */
    public String getEnsembleLabel() {
        return ensembleLabel;
    }

    /**
     * 电台集合名称，也是电台类别
     *
     * @param ensembleLabel ensembleLabel
     */
    public void setEnsembleLabel(String ensembleLabel) {
        this.ensembleLabel = ensembleLabel;
    }

    /**
     * 电台类型，具体数值对应的类型定义见XXX说明
     *
     * @return programType
     */
    public int getProgramType() {
        return programType;
    }

    /**
     * 电台类型，具体数值对应的类型定义见XXX说明
     *
     * @param programType programType
     */
    public void setProgramType(int programType) {
        this.programType = programType;
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

    /**
     * 集合ID
     *
     * @return ensembleId
     */
    public int getEnsembleId() {
        return ensembleId;
    }

    /**
     * 集合ID
     *
     * @param ensembleId ensembleId
     */
    public void setEnsembleId(int ensembleId) {
        this.ensembleId = ensembleId;
    }

    /**
     * 集合名称中可做简称字符标识（共16bit，bit =1表示对应字符可显示）
     *
     * @return ensembleLableFlag
     */
    public int getEnsembleLableFlag() {
        return ensembleLableFlag;
    }

    /**
     * 集合名称中可做简称字符标识（共16bit，bit =1表示对应字符可显示）
     *
     * @param ensembleLableFlag ensembleLableFlag
     */
    public void setEnsembleLableFlag(int ensembleLableFlag) {
        this.ensembleLableFlag = ensembleLableFlag;
    }

    /**
     * 服务名称中可做简称字符标识（共16bit，bit =1表示对应字符可显示）
     *
     * @return proStaNameFlag
     */
    public int getProStaNameFlag() {
        return proStaNameFlag;
    }

    /**
     * 服务名称中可做简称字符标识（共16bit，bit =1表示对应字符可显示）
     *
     * @param proStaNameFlag proStaNameFlag
     */
    public void setProStaNameFlag(int proStaNameFlag) {
        this.proStaNameFlag = proStaNameFlag;
    }

    /**
     * 表示当前服务是否是sub 服务属性
     * Int ： 0 非sub服务属性
     * 1 子sub服务属性
     */
    public int getSubServiceFlag() {
        return subServiceFlag;
    }

    /**
     * 表示当前服务是否是sub 服务属性
     * Int ： 0 非sub服务属性
     * 1 子sub服务属性
     */
    public void setSubServiceFlag(int subServiceFlag) {
        this.subServiceFlag = subServiceFlag;
    }

    public int getLogoLen() {
        return logoLen;
    }

    public void setLogoLen(int logoLen) {
        this.logoLen = logoLen;
    }

    public byte[] getLogoDataList() {
        return logoDataList;
    }

    public void setLogoDataList(byte[] logoDataList) {
        this.logoDataList = logoDataList;
    }

    public int getSlsLen() {
        return slsLen;
    }

    public void setSlsLen(int slsLen) {
        this.slsLen = slsLen;
    }

    /**
     * 没有数据就从保存在内存中的DABSliderShowList中取
     * @return
     */
    public byte[] getSlsDataList() {
        if(slsDataList == null || slsDataList.length == 0) {
            DABSliderShow dabSlideShow = RadioList.getInstance().getDabSlideShow(this);
            if(dabSlideShow != null) {
                slsLen = dabSlideShow.getSlsLen();
                slsDataList = dabSlideShow.getSlsDataList();
            }
        }
        return slsDataList;
    }

    public void setSlsDataList(byte[] slsDataList) {
        this.slsDataList = slsDataList;
    }

    public String getDynamicLabel() {
        return dynamicLabel;
    }

    public void setDynamicLabel(String dynamicLabel) {
        this.dynamicLabel = dynamicLabel;
    }

    public String getDynamicPlusLabel() {
        return dynamicPlusLabel;
    }

    public void setDynamicPlusLabel(String dynamicPlusLabel) {
        this.dynamicPlusLabel = dynamicPlusLabel;
    }

    public String getShortProgramStationName() {
        return shortProgramStationName;
    }

    public void setShortProgramStationName(String shortProgramStationName) {
        this.shortProgramStationName = shortProgramStationName;
    }

    public String getShortEnsembleLabel() {
        return shortEnsembleLabel;
    }

    public void setShortEnsembleLabel(String shortEnsembleLabel) {
        this.shortEnsembleLabel = shortEnsembleLabel;
    }

    protected DABMessage(Parcel in) {
        frequency = in.readInt();
        programStationName = in.readString();
        ensembleLabel = in.readString();
        programType = in.readInt();
        serviceId = in.readLong();
        serviceComponentId = in.readInt();
        ensembleId = in.readInt();
        ensembleLableFlag = in.readInt();
        proStaNameFlag = in.readInt();
        subServiceFlag = in.readInt();
        logoLen = in.readInt();
        logoDataList = in.createByteArray();
        slsLen = in.readInt();
        slsDataList = in.createByteArray();
        dynamicLabel = in.readString();
        dynamicPlusLabel = in.readString();
        shortProgramStationName = in.readString();
        shortEnsembleLabel = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(frequency);
        dest.writeString(programStationName);
        dest.writeString(ensembleLabel);
        dest.writeInt(programType);
        dest.writeLong(serviceId);
        dest.writeInt(serviceComponentId);
        dest.writeInt(ensembleId);
        dest.writeInt(ensembleLableFlag);
        dest.writeInt(proStaNameFlag);
        dest.writeInt(subServiceFlag);
        dest.writeInt(logoLen);
        dest.writeByteArray(logoDataList);
        dest.writeInt(slsLen);
        dest.writeByteArray(slsDataList);
        dest.writeString(dynamicLabel);
        dest.writeString(dynamicPlusLabel);
        dest.writeString(shortProgramStationName);
        dest.writeString(shortEnsembleLabel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DABMessage> CREATOR = new Creator<DABMessage>() {
        @Override
        public DABMessage createFromParcel(Parcel in) {
            return new DABMessage(in);
        }

        @Override
        public DABMessage[] newArray(int size) {
            return new DABMessage[size];
        }
    };

    @Override
    public String toString() {
        return "DABMessage{" +
                "frequency=" + frequency +
                ", programStationName='" + programStationName + '\'' +
                ", ensembleLabel='" + ensembleLabel + '\'' +
                ", programType=" + programType +
                ", serviceId=" + serviceId +
                ", serviceComponentId=" + serviceComponentId +
                ", ensembleId=" + ensembleId +
                ", ensembleLableFlag=" + ensembleLableFlag +
                ", proStaNameFlag=" + proStaNameFlag +
                ", subServiceFlag=" + subServiceFlag +
                ", logoLen=" + logoLen +
//                ", logoDataList=" + Arrays.toString(logoDataList) +
                ", slsLen=" + slsLen +
//                ", slsDataList=" + Arrays.toString(slsDataList) +
                ", dynamicLabel='" + dynamicLabel + '\'' +
                ", dynamicPlusLabel='" + dynamicPlusLabel + '\'' +
                '}';
    }
}
