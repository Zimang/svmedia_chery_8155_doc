package com.desaysv.libradio.utils;

import android.hardware.radio.RadioManager;

import java.util.ArrayList;

/**
 * @author uidq1846
 * @desc 创建 RadioEventValue的工具类
 * @time 2022-9-28 16:00
 */
public class RadioEventValueUtils {

    /**
     * 空数组
     */
    public static final ArrayList ARRAY_LIST = new ArrayList<>();

    /**
     * generateEventValue
     *
     * @param eventID eventID
     * @return RadioManager.RadioEventValue
     */
    public static RadioManager.RadioEventValue generateEventValue(int eventID) {
        return generateEventValue(eventID, "");
    }

    /**
     * generateEventValue
     *
     * @param eventID eventID
     * @param json    json
     * @return RadioManager.RadioEventValue
     */
    public static RadioManager.RadioEventValue generateEventValue(int eventID, String json) {
        return generateEventValue(System.nanoTime(), eventID, ARRAY_LIST, ARRAY_LIST, ARRAY_LIST, ARRAY_LIST, json);
    }

    /**
     * generateEventValue
     *
     * @param timestamp   timestamp
     * @param eventID     eventID
     * @param int32Values int32Values
     * @param floatValues floatValues
     * @param int64Values int64Values
     * @param bytes       bytes
     * @param stringValue stringValue
     * @return RadioManager.RadioEventValue
     */
    public static RadioManager.RadioEventValue generateEventValue(long timestamp, int eventID, ArrayList<Integer> int32Values, ArrayList<Float> floatValues, ArrayList<Long> int64Values, ArrayList<Byte> bytes, String stringValue) {
        return generateEventValue(timestamp, eventID, new RadioManager.RawValue(int32Values, floatValues, int64Values, bytes, stringValue));
    }

    /**
     * generateEventValue
     *
     * @param timestamp timestamp
     * @param eventID   eventID
     * @param rawValue  rawValue
     * @return RadioManager.RadioEventValue
     */
    public static RadioManager.RadioEventValue generateEventValue(long timestamp, int eventID, RadioManager.RawValue rawValue) {
        return new RadioManager.RadioEventValue(timestamp, eventID, rawValue);
    }
}
