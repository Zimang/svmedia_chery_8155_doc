/**
 *
 */
package com.desaysv.mediacommonlib.utils;


/**
 * author : liqiang
 * date : 2018/12/17
 * email : Qing.Li.EXT@desay-svautomotive.com
 * function : for video time utils
 */
public class TimeUtils {
    /**
     * 毫秒转时分秒
     *
     * @param l
     * @return
     */
    public static String longToTimeStr(int l) {
        if (l <= 0) {
            return "00:00";
        }
        int hour = 0;
        int minute = 0;
        int second = 0;
        second = l / 1000;
        if (second > 59) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute > 59) {
            hour = minute / 60;
            minute = minute % 60;
        }

        String temp = "";
        if (hour > 0) {
            temp = getTwoLength(hour) + ":" + getTwoLength(minute) + ":" + getTwoLength(second);
        } else {
            temp = getTwoLength(minute) + ":" + getTwoLength(second);

        }
        return (temp);
    }

    /**
     * 秒转分钟
     * @param second
     * @return
     */
    public static String secondToMinute(int second) {
        if (second < 0) {
            return "00:00";
        }

        int hour = 0;
        int minute = 0;

        if (second > 59) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute > 59) {
            hour = minute / 60;
            minute = minute % 60;
        }

        String temp = "";
        if (hour > 0) {
            temp = getTwoLength(hour) + ":" + getTwoLength(minute) + ":" + getTwoLength(second);
        } else {
            temp = getTwoLength(minute) + ":" + getTwoLength(second);

        }
        return (temp);
    }

    /**
     * 将时间转化为字符串
     * @param data 时间
     * @return String 字符串的时间
     */
    private static String getTwoLength(final int data) {
        if (data < 10) {
            return "0" + data;
        } else {
            return "" + data;
        }
    }


}
