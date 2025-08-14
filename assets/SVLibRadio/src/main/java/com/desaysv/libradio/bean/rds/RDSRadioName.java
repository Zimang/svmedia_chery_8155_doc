package com.desaysv.libradio.bean.rds;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;

import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.libradio.utils.ASCIITools;

import java.io.Serializable;

/**
 * @author uidq4079
 * @desc 底层解析当前获取到的全部RDS名称，应用做对应保存，显示时从对应列表取出
 * @time 2024-02-06
 */
public class RDSRadioName implements Parcelable, Serializable {
    //电台名称
    private String programStationName;
    //电台频率
    private int frequency;

    private int[] psn;

    public RDSRadioName() {
    }

    public int[] getPsn() {
        return psn;
    }

    public void setPsn(int[] psn) {
        this.psn = psn;
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

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    protected RDSRadioName(Parcel in) {
        programStationName = in.readString();
        frequency = in.readInt();
        psn = in.createIntArray();
    }

    public static final Creator<RDSRadioName> CREATOR = new Creator<RDSRadioName>() {
        @Override
        public RDSRadioName createFromParcel(Parcel in) {
            return new RDSRadioName(in);
        }

        @Override
        public RDSRadioName[] newArray(int size) {
            return new RDSRadioName[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(programStationName);
        dest.writeInt(frequency);
        dest.writeIntArray(psn);
    }

    @Override
    public String toString() {
        return "RDSRadioName{" +
                ", programStationName='" + programStationName + '\'' +
                ", frequency='" + frequency + '\'' +
                '}';
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RDSRadioName that = (RDSRadioName) obj;
        if (frequency == that.getFrequency() && psn == that.psn && programStationName.equals(that.programStationName)){
            return true;
        }
        return false;
    }
}
