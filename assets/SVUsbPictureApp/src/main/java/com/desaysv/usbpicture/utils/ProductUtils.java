package com.desaysv.usbpicture.utils;

import android.text.method.LinkMovementMethod;
import android.util.Log;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;

import java.util.ArrayList;

/**
 * created by ZNB on 2022-07-22
 * 用来判断项目配置情况
 */
public class ProductUtils {
    private static final String TAG = "ProductUtils";

    /**
     * 需要根据下线配置判断是否有 CV-BOX配置
     * 有的话，加载的是带 “相册” 的界面
     * 没有的话，加载和T18P一样的界面
     * 根据最新策略，相册单独拉出去作为独立应用由雄狮开发
     * @return
     */
    public static boolean hasCVBox(){
        Log.d(TAG,"hasCVBox: "+ CarConfigUtil.getDefault().getConfig(Constants.ID_CAR_CONFIG_CVBOX));
        return false;//CarConfigUtil.getDefault().getConfig(Constants.ID_CAR_CONFIG_CVBOX) == 1;
    }

    /**
     * 判断是不是 T22\IC4082_18 L 项目
     * L 和 M 的 UE不一样，主界面需要差分
     * 如果是通过多渠道的话，就不需要用这个
     * 如果用下线配置，就需要这个来差分
     * @return
     */
    public static boolean isT22L(){
        return false;
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
     * 根据最新需求，部分国家需要实现限速禁播图片
     * 这里通过对应的国家码，确认是否需要
     * @return
     */
    //需要支持限速禁播的国家
    private static final int Country_ENG = 1;//英国，1
    private static final int Country_PAKISITAN = 31;//巴基斯坦，31
    private static final int Country_INDIA = 19;//印度，19
    private static final int NZ_COUNTRY_CODE = 36;// 新西兰 0km/h
    private static final int Country_AUSTRYLIA = 37;//澳大利亚，37
    private static final int Country_TURKEY = 39;//土耳其，39
    public static boolean needCheckSpeed(){
        int countryCode = CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION);
        Log.d(TAG,"needCheckSpeed,countryCode:"+countryCode);
        switch (countryCode){
            case Country_ENG:
            case Country_PAKISITAN:
            case Country_INDIA:
            case NZ_COUNTRY_CODE:
            case Country_AUSTRYLIA:
            case Country_TURKEY:
                return true;

        }
        return false;
    }

    /**
     * 根据最新需求，部分国家需要实现限速禁播图片
     * 这里通过对应的国家码，返回对应的限速值
     * @return
     */
    private static final int LIMIT_SPEED_0 = 5;//第一种限速值，5km/h
    private static final int LIMIT_SPEED_1 = 15;//第二种限速值，15km/h

    private static final int LIMIT_SPEED_2 = 0;//第三种限速值，0km/h
    public static int getLimitSpeed(){
        int countryCode = CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION);
        Log.d(TAG,"getLimitSpeed,countryCode:"+countryCode);
        switch (countryCode){
            case Country_ENG:
            case Country_INDIA:
            case Country_TURKEY:
            case Country_PAKISITAN:
                return LIMIT_SPEED_0;
            case NZ_COUNTRY_CODE:
            case Country_AUSTRYLIA:
                return LIMIT_SPEED_2;

        }
        return LIMIT_SPEED_1;
    }

}
