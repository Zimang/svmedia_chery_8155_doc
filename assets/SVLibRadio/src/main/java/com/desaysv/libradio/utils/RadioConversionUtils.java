package com.desaysv.libradio.utils;

import android.hardware.radio.RadioManager;
import android.util.Log;

import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioMessage;
import com.google.gson.Gson;

/**
 * @author uidq1846
 * @desc 电台间相互转换的工具类
 * @time 2021-12-24 11:05
 */
public class RadioConversionUtils {

    private static final String TAG = RadioConversionUtils.class.getSimpleName();

    /**
     * 根据频率获取当前的频段
     *
     * @return AM或者FM的频段 {@link RadioManager}
     */
    public static int frequencyGetBand(int frequency) {
        int band = -1;
        if (RadioConfig.AM_MIN <= frequency && frequency <= RadioConfig.AM_MAX) {
            band = RadioManager.BAND_AM;
        } else if (RadioConfig.FM_MIN <= frequency && frequency <= RadioConfig.FM_MAX) {
            band = RadioManager.BAND_FM;
        }
        Log.d(TAG, "getRadioBandFromFrequency: frequency = " + frequency + " band = " + band);
        return band;
    }

    /**
     * 根据规则，将原始名称转换为简称
     * @param source 原名称
     * @param flag 规则，这个是HAL直接传上来的
     * @return
     */
    public static String getShortName(String source, int flag){
        if (source == null){
            Log.d(TAG,"getShortName is null");
            return "";
        }
        Log.d(TAG,"getShortName,source: "+ source);
        //按照客户欧洲路试需求，不要用简称
/*        String[] rule = parserInt2Binary(flag).split("");
        String shortName = "";
        String[] nameArray = source.split("");
        for (int i = 0; i < rule.length; i++){
            if ("1".equals(rule[i])){
                if (i < nameArray.length) {
                    shortName = shortName + nameArray[i];
                }
            }
        }
        Log.d(TAG,"getShortName,shortName: "+ shortName);
        if (shortName.length() > 1){
            return shortName;
        }*/
        return source;
    }
    /**
     * 将 int 类型 转成 0101二进制数据
     * @param flag
     * @return
     */
    private static String parserInt2Binary(int flag){
        Log.d(TAG,"parserInt2Binary, flag: "+ flag);
        String s = Integer.toBinaryString(flag);
        Log.d(TAG,"parserInt2Binary, begin s: "+ s);
        if (s.length() > 16){//如果数据大于16位，则只取低16位
            s = s.substring(s.length() - 16);
        }else {

        }
        Log.d(TAG,"parserInt2Binary, end s: "+ s);
        return s;
    }

//    /**
//     * RadioMessage 转为VDS的 VDFmAmInfo
//     *
//     * @param message {@link RadioMessage}
//     * @return {@link VDFmAmInfo}
//     */
//    public static VDFmAmInfo radioMessageToVDFmAmInfo(RadioMessage message) {
//        VDFmAmInfo info = new VDFmAmInfo();
//        info.putFrequency(message.getRadioFrequency());
//        if (message.isCollect()) {
//            info.putCollectStatus(VDValueTuner.FmAmCollectStatus.COLLECTED);
//        } else {
//            info.putCollectStatus(VDValueTuner.FmAmCollectStatus.UNCOLLECTED);
//        }
//        int radioBand = message.getRadioBand();
//        if (radioBand == RadioManager.BAND_AM) {
//            info.putBand(VDValueTuner.FmAmBand.AM);
//        } else {
//            info.putBand(VDValueTuner.FmAmBand.FM);
//        }
//        return info;
//    }
//
//    /**
//     * RadioMessage 转为VDS的 VDFmAmInfo
//     *
//     * @param message {@link RadioMessage}
//     * @return {@link VDFmAmInfo}
//     */
//    public static VDDABInfo radioMessageToVDDABInfo(RadioMessage message) {
//        //因为DABMessage和VDDABInfo变量名称一致，所以可以偷懒这么写
//        DABMessage dabMessage = message.getDabMessage();
//        String json = new Gson().toJson(dabMessage);
//        VDDABInfo info = new Gson().fromJson(json, VDDABInfo.class);
//        info.putCollectStatus(message.isCollect() ? VDValueTuner.VDValueDAB.CollectStatus.COLLECTED : VDValueTuner.VDValueDAB.CollectStatus.UNCOLLECTED);
//        Log.d(TAG, "radioMessageToVDDABInfo: info = " + info);
//        return info;
//    }
}
