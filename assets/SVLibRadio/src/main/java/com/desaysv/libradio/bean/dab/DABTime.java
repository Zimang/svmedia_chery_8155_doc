package com.desaysv.libradio.bean.dab;

import androidx.annotation.NonNull;

/**
 * @author uidq1846
 * @desc DAB时间，用于校验系统设置时间
 * @time 2022-9-26 17:13
 */
public class DABTime {
    //时间状态 0 时间无效 1 时间获取成功
    private int state;
    //年
    private int year;
    //月
    private int month;
    //天
    private int day;
    //周
    private int week;
    //时
    private int hour;
    //分
    private int minute;
    //秒
    private int second;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @NonNull
    @Override
    public String toString() {
        return "year:" + this.year+ ",month:" + this.month+ ",day:" + this.day+ ",hour:" + this.hour+ ",min:" + this.minute+ ",sec:" + this.second;
    }
}
