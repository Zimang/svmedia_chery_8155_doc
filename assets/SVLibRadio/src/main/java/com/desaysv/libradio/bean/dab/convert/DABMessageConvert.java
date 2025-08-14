package com.desaysv.libradio.bean.dab.convert;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * created by ZNB on 2022-12-20
 * HAL回传的数据结构类型可能会变动
 * 因此增加一层转接头直接对接HAL的数据
 * 再把数据转成APP需要的类型
 * 举个栗子：
 * HAL给的数据结构是 int[]，但是应用要用的是 String，不想在用到的地方都去修改的话，做个数据转接层来保证数据结构的稳定性就很有必要
 * 如果HAL给的数据变动，那么只要修改转接层的处理就行，无须修改应用层繁多的引用
 */
public class DABMessageConvert implements Parcelable{

    private static final String TAG = "DABMessageConvert";

/*    参数名	类型	说明
    frequency	int	频点值
    ensembleId	string 集合ID
    programStationName	int[]	电台(服务名称)
    ensembleLabel	int[]	电台集合名称，也是电台类别
    ProgramType	int	电台类型，具体数值对应的类型定义见XXX说明
    serviceId	int	服务ID
    serviceComponentId	int	服务组件ID

    dynamicLabel	int[]	电台详细信息
    dynamicPlusLabel	int[]	ID3信息*/


    private int frequency;
    private int ensembleId;
    private int[] programStationName;
    private int[] ensembleLabel;
    private int programType;
    private long serviceId;
    private int serviceComponentId;
    private int[] dynamicLabel;
    private int[] dynamicPlusLabel;

    private int subServiceFlag;//子电台标志

    /**
     * 名字简称的规则，
     * 需要转成 01010 的形式，取低16位
     * 1表示对应的位置需要，0表示不要
     */
    private int proStaNameFlag;

    private int ensembleLabelFlag;

    public DABMessageConvert() {
    }


    protected DABMessageConvert(Parcel in) {
        frequency = in.readInt();
        ensembleId = in.readInt();
        programStationName = in.createIntArray();
        ensembleLabel = in.createIntArray();
        serviceId = in.readLong();
        serviceComponentId = in.readInt();
        programType = in.readInt();
        dynamicLabel = in.createIntArray();
        dynamicPlusLabel = in.createIntArray();
        proStaNameFlag = in.readInt();
        ensembleLabelFlag = in.readInt();
        subServiceFlag = in.readInt();
    }

    public static final Parcelable.Creator<DABMessageConvert> CREATOR = new Parcelable.Creator<DABMessageConvert>() {
        @Override
        public DABMessageConvert createFromParcel(Parcel in) {
            return new DABMessageConvert(in);
        }

        @Override
        public DABMessageConvert[] newArray(int size) {
            return new DABMessageConvert[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(frequency);
        dest.writeInt(ensembleId);
        dest.writeIntArray(programStationName);
        dest.writeIntArray(ensembleLabel);
        dest.writeLong(serviceId);
        dest.writeInt(serviceComponentId);
        dest.writeInt(programType);
        dest.writeIntArray(dynamicLabel);
        dest.writeIntArray(dynamicPlusLabel);
        dest.writeInt(proStaNameFlag);
        dest.writeInt(ensembleLabelFlag);
        dest.writeInt(subServiceFlag);
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getEnsembleId() {
        return ensembleId;
    }

    public void setEnsembleId(int ensembleId) {
        this.ensembleId = ensembleId;
    }

    public int[] getProgramStationName() {
        return programStationName;
    }

    public void setProgramStationName(int[] programStationName) {
        this.programStationName = programStationName;
    }

    public int[] getEnsembleLabel() {
        return ensembleLabel;
    }

    public void setEnsembleLabel(int[] ensembleLabel) {
        this.ensembleLabel = ensembleLabel;
    }

    public int getProgramType() {
        return programType;
    }

    public void setProgramType(int programType) {
        this.programType = programType;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getServiceComponentId() {
        return serviceComponentId;
    }

    public void setServiceComponentId(int serviceComponentId) {
        this.serviceComponentId = serviceComponentId;
    }

    public int[] getDynamicLabel() {
        return dynamicLabel;
    }

    public void setDynamicLabel(int[] dynamicLabel) {
        this.dynamicLabel = dynamicLabel;
    }

    public int[] getDynamicPlusLabel() {
        return dynamicPlusLabel;
    }

    public void setDynamicPlusLabel(int[] dynamicPlusLabel) {
        this.dynamicPlusLabel = dynamicPlusLabel;
    }

    public int getProStaNameFlag() {
        return proStaNameFlag;
    }

    public void setProStaNameFlag(int proStaNameFlag) {
        this.proStaNameFlag = proStaNameFlag;
    }

    public int getEnsembleLabelFlag() {
        return ensembleLabelFlag;
    }

    public void setEnsembleLabelFlag(int ensembleLabelFlag) {
        this.ensembleLabelFlag = ensembleLabelFlag;
    }

    public int getSubServiceFlag() {
        return subServiceFlag;
    }

    public void setSubServiceFlag(int subServiceFlag) {
        this.subServiceFlag = subServiceFlag;
    }
}
