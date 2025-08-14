package com.desaysv.mediacommonlib.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author uidq1846
 * @desc 音乐全局值配置
 * @time 2022-11-24 19:27
 */
public class MusicSetting {
    private static MusicSetting instance;
    private final Map<String, Integer> integerMap = new HashMap<>();
    private final Map<String, String> stringMap = new HashMap<>();

    private MusicSetting() {
    }

    /**
     * 获取单例模式
     *
     * @return MusicSetting
     */
    public static MusicSetting getInstance() {
        if (instance == null) {
            synchronized (MusicSetting.class) {
                if (instance == null) {
                    instance = new MusicSetting();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init() {

    }

    /**
     * 设置参数
     *
     * @param flag  flag
     * @param value value
     */
    public void putInt(String flag, int value) {
        integerMap.put(flag, value);
    }

    /**
     * 获取存储的int类型
     *
     * @param flag     flag
     * @param defValue defValue
     * @return int
     */
    public int getInt(String flag, int defValue) {
        Integer integer = integerMap.get(flag);
        if (integer == null) {
            return defValue;
        }
        return integer;
    }

    /**
     * 设置参数
     *
     * @param flag  flag
     * @param value value
     */
    public void putString(String flag, String value) {
        stringMap.put(flag, value);
    }

    /**
     * 获取存储的int类型
     *
     * @param flag     flag
     * @param defValue defValue
     * @return int
     */
    public String getString(String flag, String defValue) {
        String s = stringMap.get(flag);
        if (s == null) {
            return defValue;
        }
        return s;
    }
}
