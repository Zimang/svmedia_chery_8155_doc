package com.desaysv.libradio.bean.dab.convert;

import androidx.annotation.Nullable;

import com.desaysv.libradio.bean.RadioList;

import java.util.Objects;

/**
 * @author uidq1846
 * @desc DAB电台节目
 * @time 2022-9-26 15:50
 */
public class DABEPGScheduleConvert {
    //节目简介
    private int[] programName;
    //节目日期年
    private String year = "0";
    //节目日期月
    private String month = "0";
    //节目日期日
    private String day = "0";
    //节目日期时
    private String hour = "0";
    //节目日期分
    private String min = "0";
    //节目日期秒
    private String sec = "0";

    //对应DAB的电台信息
    private long serviceId;
    private int freq;
    private int serviceComponentId;

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public int getServiceComponentId() {
        return serviceComponentId;
    }

    public void setServiceComponentId(int serviceComponentId) {
        this.serviceComponentId = serviceComponentId;
    }

    public int[] getProgramName() {
        return programName;
    }

    public void setProgramName(int[] programName) {
        this.programName = programName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        if (hour.equals("0")){
            hour = "00";
        }
        this.hour = hour;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        if (min.equals("0")){
            min = "00";
        }
        this.min = min;
    }

    public String getSec() {
        return sec;
    }

    public void setSec(String sec) {
        this.sec = sec;
    }

    /**
     * 自定义相等比较方法
     * 用于和数据库保存的订阅列表进行匹配
     * @param obj
     * @return
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj){
            return true;
        }
        if (!(obj instanceof DABEPGScheduleConvert)){
            return false;
        }
        DABEPGScheduleConvert that = (DABEPGScheduleConvert) obj;
        return serviceId == that.getServiceId()
                && freq == that.getFreq()
                && serviceComponentId == that.getServiceComponentId()
                && Objects.equals(programName,that.getProgramName())
                && Objects.equals(year,that.getYear())
                && Objects.equals(month,that.getMonth())
                && Objects.equals(day,that.getDay())
                && Objects.equals(hour,that.getHour())
                && Objects.equals(min,that.getMin())
                && Objects.equals(sec,that.getSec());
    }
}
