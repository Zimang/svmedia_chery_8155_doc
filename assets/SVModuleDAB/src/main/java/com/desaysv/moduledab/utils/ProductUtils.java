package com.desaysv.moduledab.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.mediacommonlib.base.AppBase;

/**
 * created by ZNB on 2022-07-22
 * 用来判断项目配置情况
 */
public class ProductUtils {
    private static final String TAG = "ProductUtils";

    /**
     * 需要根据下线配置判断是否有 DAB配置
     * 有的话，加载的是带 “DAB” 的界面
     * @return
     */
    public static boolean hasDAB(){
        Log.d(TAG,"hasDAB: "+ CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB));
        return CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }

    /**
     * 需要根据下线配置判断是否有 RDS配置，需要项目提供获取该配置的方式
     * 有的话，加载的是带 “RDS” 的界面
     * @return
     */
    public static boolean hasRDS(){
        //Log.d(TAG,"hasRDS: "+ CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB));
        return true;//CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }
    /**
     * 项目新需求，有些地区没有AM
     * 需要根据下线配置或者零件号判断是否有 AM，需要项目提供获取该配置的方式
     * 有的话，加载的是带 “AM” 的界面
     * @return
     */
    public static boolean hasAM(){
        boolean isEUbyNum = CarConfigUtil.getDefault().isEUVersionByPartNum();
        Log.d(TAG,"isEUVersionByPartNum: "+ isEUbyNum);
        int countryCode = CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION);
        Log.d(TAG, "countryCode:"+countryCode);

        return !isEUbyNum || countryCode == 39/*土耳其*/ || countryCode == 40/*以色列*/ || countryCode == 41/*塞尔维亚*/ || countryCode == 1/*英国*/;
    }

    /**
     * 是否是欧盟项目
     * @return
     */
    public static boolean isEUVersionByPartNum() {
        return CarConfigUtil.getDefault().isEUVersionByPartNum();
    }

    /**
     * 项目新需求，需要融合 DAB/FM
     * 需要根据下线配置或者零件号判断，需要项目提供获取该配置的方式
     * 有的话，加载的是带 "DAB/FM” 的界面
     * @return
     */
    public static boolean hasMulti(){
        //Log.d(TAG,"hasAM: "+ CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB));
        return CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }



    public static boolean isRtlView(Context context){
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (configuration != null) {
            return configuration.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        return false;
    }

    public static String getTheme(Context context){
        String themeStr = Settings.System.getString(context.getContentResolver(), "com.desaysv.setting.theme.mode");
        Log.d(TAG,"getTheme,themeStr="+themeStr);
        return themeStr;
    }

    /**
     * 判断是否是右舵
     * @return
     */
    public static boolean isRightRudder(){
        boolean isRightRudder = CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_LEFT_RIGHT_RUDDER_TYPE) == 1;
        Log.d(TAG,"isRightRudder:"+isRightRudder);
        return isRightRudder;
    }

    /**
     * 目前除了T22/T19C/T18Fl3等沙特以外的项目，需要使用另一套埋点
     * @return
     */
    private static final int SAUDI_COUNTRY_CODE = 29;//沙特国家码
    private static final int T22_MODEL_CODE = 2;//t22 model code
    private static final int T19C_MODEL_CODE = 7;//t19c model code
    private static final int T18FL3_MODEL_CODE = 10;//t18fl3 model code

    private static final int T1E_MODEL_CODE = 3;//t1e model code

    public static boolean isSaudiPoint(){
        int modelCode = CarConfigUtil.getDefault().getConfig(Constants.ID_MODEL_CODE);
        int countryCode = CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION);
        Log.d(TAG,"modelCode:"+modelCode + ",countryCode:"+countryCode);
        if (SAUDI_COUNTRY_CODE == countryCode){
            return T22_MODEL_CODE == modelCode || T19C_MODEL_CODE == modelCode || T18FL3_MODEL_CODE == modelCode;
        }
        return false;
    }

    public static boolean isT1EPoint(){
        int modelCode = CarConfigUtil.getDefault().getConfig(Constants.ID_MODEL_CODE);

        return T1E_MODEL_CODE == modelCode;
    }

    /**
     * 判断是否是网联版
     * @return
     */
    public static boolean isNet(){
        Log.d(TAG,"isNet:"+CarConfigUtil.getDefault().isNet());
        return CarConfigUtil.getDefault().isNet();
    }

    /**
     * 判断是否是网络安全
     * @return
     */
    public static boolean isSecurity(){
        boolean isSecurity = CarConfigUtil.getDefault().isNeedCyberSecurity() ||  CarConfigUtil.getDefault().isEUVersionByPartNum();
        Log.d(TAG,"isSecurity:"+isSecurity);
        return isSecurity;
    }

}
