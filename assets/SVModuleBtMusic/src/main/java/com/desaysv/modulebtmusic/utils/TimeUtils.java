package com.desaysv.modulebtmusic.utils;

/**
 * 通用时间工具类
 */
public class TimeUtils {
    /**
     * 将int类型数字转换成时分秒的格式数据
     *
     * @param second
     * @return HH:mm:ss
     */
    public static String secondToTime(int second) {
        String timeStr;
        int hour;
        int minute;
        if (second < 0) {
            return "";
        } else if (second == 0) {
            return "00:00";
        } else {
            minute = second / 60;
            if (second < 60) {
                timeStr = "00:" + unitFormat(second);
            } else if (minute < 60) {
                second = second % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                minute = minute % 60;
                second = second - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String millisecondToTime(long millisecond) {
        return secondToTime((int) (millisecond / 1000));
    }

    /**
     * 时分秒的格式转换
     *
     * @param i
     * @return
     */
    public static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10) {
            retStr = "0" + i;
        } else {
            retStr = "" + i;
        }
        return retStr;
    }
}
