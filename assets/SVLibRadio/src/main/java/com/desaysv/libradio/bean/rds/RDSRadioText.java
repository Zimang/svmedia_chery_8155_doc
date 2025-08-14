package com.desaysv.libradio.bean.rds;

import android.os.Parcel;
import android.os.Parcelable;


import java.io.Serializable;

/**
 * @author uidq1846
 * @desc RDS电台携带的信息
 * @time 2022-9-22 19:52
 */
public class RDSRadioText implements Parcelable, Serializable {
    //电台名称
    private String programStationName;
    //电台类型
    private int programType;
    //文本信息
    private String radioText;

    public RDSRadioText() {
    }

    /**
     * 电台名称
     *
     * @return programStationName
     */
    public String getProgramStationName() {
        return programStationName;
    }

    /**
     * 电台名称
     *
     * @param programStationName programStationName
     */
    public void setProgramStationName(String programStationName) {
        this.programStationName = programStationName;
    }

    /**
     * 电台节目的类型，具体数值对应的类型定义见XXX说明
     *
     */
    public int getProgramType() {
        return programType;
    }

    /**
     * 电台节目的类型，具体数值对应的类型定义见XXX说明
     *
     */
    public void setProgramType(int programType) {
        this.programType = programType;
    }

    /**
     * 电台节目的文本介绍信息
     *
     * @return radioText
     */
    public String getRadioText() {
        return radioText;
    }

    /**
     * 电台节目的文本介绍信息
     *
     * @param radioText radioText
     */
    public void setRadioText(String radioText) {
        this.radioText = radioText;
    }

    protected RDSRadioText(Parcel in) {
        programStationName = in.readString();
        programType = in.readInt();
        radioText = in.readString();
    }

    public static final Parcelable.Creator<RDSRadioText> CREATOR = new Parcelable.Creator<RDSRadioText>() {
        @Override
        public RDSRadioText createFromParcel(Parcel in) {
            return new RDSRadioText(in);
        }

        @Override
        public RDSRadioText[] newArray(int size) {
            return new RDSRadioText[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(programStationName);
        dest.writeInt(programType);
        dest.writeString(radioText);
    }

    @Override
    public String toString() {
        return "RDSRadioText{" +
                ", programStationName='" + programStationName + '\'' +
                ", programType=" + programType +
                ", radioText='" + radioText + '\'' +
                '}';
    }
}
