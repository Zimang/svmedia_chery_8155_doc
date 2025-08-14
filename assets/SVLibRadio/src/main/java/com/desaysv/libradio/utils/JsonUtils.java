package com.desaysv.libradio.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author uidq1846
 * @desc 对Json进行转换封装的类
 * @time 2022-9-28 14:18
 */
public class JsonUtils {
    private static final String TAG = JsonUtils.class.getSimpleName();

    /**
     * 针对单个节点的动作，生成相应的Json字符串
     *
     * @param jsonNode  节点
     * @param jsonValue 值
     */
    public static String generateSingleNodeJson(String jsonNode, int jsonValue) {
        Map<String, Integer> map = new HashMap<>();
        map.put(jsonNode, jsonValue);
        String json = JsonUtils.generateJson(map);
        Log.d(TAG, "generateSingleNodeJson: json = " + json);
        return json;
    }

    /**
     * 根据节点创建Json
     *
     * @param o value
     * @return String
     */
    public static String generateJson(Object o) {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

    /**
     * 根据数组还原
     *
     * @param json     json
     * @param classOfT classOfT
     * @param <T>      <T>
     * @return <T> T
     */
    public static <T> T generateObject(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    /**
     * 生成节点map对象
     *
     * @param json json
     * @return Map<String, Object>
     */
    public static Map<String, Object> generateMap(String json) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }
}
