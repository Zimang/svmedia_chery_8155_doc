package com.desaysv.moduleusbvideo.util;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;

import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;

import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.moduleusbvideo.BuildConfig;
import com.desaysv.moduleusbvideo.R;

import java.util.Locale;

/**
 * Create by extodc87 on 2022-11-2
 * Author: extodc87
 */
public class Constant {
    private static final String TAG = "Constant";

    //FLAVOR 当前渠道
    public static final String CHERY_8155_INT_T18FL3 = "chery_8155_int_t18fl3";
    public static final String CHERY_8155_INT_T19C = "chery_8155_int_t19c";
    public static final String CHERY_8155_INT_T22 = "chery_8155_int_t22";

    public static boolean isT18Fl3Flavor() {
        return CHERY_8155_INT_T18FL3.equalsIgnoreCase(BuildConfig.FLAVOR);
    }

    public static boolean isT19CFlavor() {
        return CHERY_8155_INT_T19C.equalsIgnoreCase(BuildConfig.FLAVOR);
    }

    public static boolean isT22Flavor() {
        return CHERY_8155_INT_T22.equalsIgnoreCase(BuildConfig.FLAVOR);
    }


    /**
     * 行车速度限制
     */
    public static final float RUNNING_SPEED_LIMIT_15 = 15.0f;
    public static final float RUNNING_SPEED_LIMIT_5 = 5.0f;
    public static final float RUNNING_SPEED_LIMIT_0 = 0.0f;
    public static float RUNNING_SPEED = RUNNING_SPEED_LIMIT_15;

    /**
     * 国家码
     */
    //中国 15km/h
    private static final int CHINA_COUNTRY_CODE = 0;
    // 英国 5km/h
    private static final int BRITAIN_COUNTRY_CODE = 1;
    //伊拉克 15km/h
    private static final int IRAQ_COUNTRY_CODE = 2;
    //智利/秘鲁 15km/h
    private static final int CHILE_COUNTRY_CODE = 4;
    //巴西 5km/h
    private static final int BRAZIL_COUNTRY_CODE = 9;
    //中亚 15km/h
    private static final int CENTRAL_ASIA_COUNTRY_CODE = 10;
    //智利/秘鲁 15km/h
    private static final int PERU_COUNTRY_CODE = 13;
    //埃及 15km/h
    private static final int EGYPT_COUNTRY_CODE = 16;
    //乌克兰 15km/h
    private static final int UKRAINE_COUNTRY_CODE = 19;
    //厄瓜多尔 15km/h
    private static final int ECUADOR_COUNTRY_CODE = 20;
    //墨西哥 15km/h
    private static final int MEXICO_COUNTRY_CODE = 21;
    //巴基斯坦 5km/h
    private static final int PAKISTAN_COUNTRY_CODE = 31;
    //泰国 15km/h
    private static final int THAILAND_COUNTRY_CODE = 32;
    //印度 5km/h
    private static final int INDIA_COUNTRY_CODE = 35;
    //澳大利亚 0km/h
    public static final int AUSTRALIA_COUNTRY_CODE = 37;
    // 新西兰 0km/h
    public static final int NZ_COUNTRY_CODE = 36;
    //土耳其 5km/h
    private static final int TURKEY_COUNTRY_CODE = 39;
    //越南 15km/h
    private static final int VIETNAM_COUNTRY_CODE = 43;
    //斯里兰卡 5km/h
    //private static final int SRI_LANKA_COUNTRY_CODE = 56;
    //新加坡 5km/h
    //private static final int SINGAPORE_COUNTRY_CODE = 57;
    //中国香港 5km/h
    //private static final int HONG_KONG_COUNTRY_CODE = 58;
    //中国澳门 15km/h
    //private static final int MACAO_COUNTRY_CODE = 59;
    //中国台湾 15km/h
    //private static final int TAIWAN_COUNTRY_CODE = 60;
    //约旦 15km/h
    //private static final int JORDAN_COUNTRY_CODE = 61;

    private static int countryCode;

    public static final int POSITION_X = 0;
    public static final int POSITION_Y = 300;


    public final static int VIEW_TYPE_USB1 = 5;

    public final static int VIEW_TYPE_USB2 = 6;

    /**
     * 默认亮度
     */
    public static final int VIDEO_PLAY_ALPHA_DEFAULT = 50;

    /**
     * 最大亮度
     */
    public static final int VIDEO_PLAY_ALPHA_MAX = 100;
    /**
     * 最小亮度（view alpha 不能设定为0 ，为0 view透明，activity显示空白）
     */
    public static final int VIDEO_PLAY_ALPHA_MIN = 20;
    /**
     * 判断有效的滑动距离
     */
    public static final int EFFECT_SLIDE_DISTANCE = 150;
    /**
     * 最小手势滑动距离滑动超过100才去调节亮度
     */
    public final static float MIN_MOVE_DISTANCE = 80f;
    /**
     * 音量最大值39，在小幅度滑动时计算的音量变化小于1，音量无变化，体验上就是滑动调节音量无效。
     * 将音量增加10倍，滑动效果明显
     */
    public static final int VOLUME_MULTIPLE = 10;

    /**
     * USB1视频 Fragment ID
     */
    public final static int USB1_FRAGMENT_ID = R.layout.usb_video_fragment_list + VIEW_TYPE_USB1;

    /**
     * USB2视频 Fragment ID
     */
    public final static int USB2_FRAGMENT_ID = R.layout.usb_video_fragment_list + VIEW_TYPE_USB2;


    /**
     * 亮度存取的key值
     */
    public static final String VIDEO_PLAY_ALPHA = "VIDEO_PLAY_ALPHA";

    /**
     * 新手指导标识
     * 0 新手， 1不是新手
     */
    public static final String VIDEO_NOVICE_GUIDANCE = "VIDEO_NOVICE_GUIDANCE";
    /**
     * 新手
     */
    public static final int VIDEO_NOVICE_GUIDANCE_NOVICE = 0;
    /**
     * 熟练
     */
    public static final int VIDEO_NOVICE_GUIDANCE_SKILLED = 1;

    /**
     * 行车视频
     * 系统设置开关
     * 1:开， 0:关
     */
    private static final String SETTING_LIMITER = "com.desaysv.setting.limiter";

    /**
     * 开
     */
    private static final int SETTING_LIMITER_OPEN = 1;
    /**
     * 关
     */
    private static final int SETTING_LIMITER_CLOSE = 0;


    /**
     * dp转px
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * 是否保持 左舵 国内 UI效果
     * T 左舵 并且  RTL ; -> 阿语或波斯语
     * F 右舵 | 左舵 英语
     */
    public static boolean isRtl() {
        // 左右舵
        boolean leftOrRightConfig = isLeftOrRightConfig(); //T:右舵   F: 左舵
        // 语言 RTL ?
        boolean isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL;
        boolean returned = false;
        // 需求1、阿语或波斯语 部分UI需要保持左舵镜像
        // 左舵 并且  RTL ; -> 阿语或波斯语
        if (!leftOrRightConfig && isRtl) {
            returned = true;
        }
        Log.d(TAG, "isRtl() called leftOrRightConfig: " + leftOrRightConfig + " , isRtl: " + isRtl + " , returned: " + returned);
        return returned;
        //返回 false 右舵或者 左舵 英语
    }


    /**
     * 左右舵
     *
     * @return T:右舵   F: 左舵
     */
    public static boolean isLeftOrRightConfig() {
        boolean isRightRudder = CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_LEFT_RIGHT_RUDDER_TYPE) == 1;
        return isRightRudder;
    }
    /**
     * isOnlyT19C
     * 针对 只对T19C|奇瑞股份 生效  |||| 也就是说 非星途系列
     * @return T:true   F: false
     */
    public static boolean isExeed() {
        boolean isExeed =  CarConfigUtil.getDefault().isT22_INT()||CarConfigUtil.getDefault().isT18P_INT();
        return isExeed;
    }

    public static int getCountryCode() {
        return countryCode;
    }

    /**
     * 判断当前行车视频开关
     *
     * @param mContext mContext
     * @return true 开， FALSE 关
     */
    public static boolean isOpenLimiter(Context mContext) {
//        Settings.Global.putInt(mContext.getContentResolver(), SETTING_LIMITER, status ? 1 : 0);
        // 默认关闭
        int limiterStatus = Settings.Global.getInt(mContext.getContentResolver(), SETTING_LIMITER, SETTING_LIMITER_OPEN);
        Log.d(TAG, "isOpenLimiter: limiterStatus: " + limiterStatus);
        return limiterStatus == SETTING_LIMITER_OPEN;
    }

    /**
     * 初始化，行车锁屏默认速度
     */
    public static void initRunningSpeed() {
        countryCode = CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION);
        switch (countryCode) {
            case NZ_COUNTRY_CODE:
            case AUSTRALIA_COUNTRY_CODE:
                RUNNING_SPEED = RUNNING_SPEED_LIMIT_0;
                break;
            case BRITAIN_COUNTRY_CODE:
            case PAKISTAN_COUNTRY_CODE:
            case INDIA_COUNTRY_CODE:
            case TURKEY_COUNTRY_CODE:
            case BRAZIL_COUNTRY_CODE:
                RUNNING_SPEED = RUNNING_SPEED_LIMIT_5;
                break;
            case CHINA_COUNTRY_CODE:
            case IRAQ_COUNTRY_CODE:
            case CHILE_COUNTRY_CODE:
            case CENTRAL_ASIA_COUNTRY_CODE:
            case PERU_COUNTRY_CODE:
            case EGYPT_COUNTRY_CODE:
            case UKRAINE_COUNTRY_CODE:
            case ECUADOR_COUNTRY_CODE:
            case MEXICO_COUNTRY_CODE:
            case THAILAND_COUNTRY_CODE:
            case VIETNAM_COUNTRY_CODE:
                RUNNING_SPEED = RUNNING_SPEED_LIMIT_15;
                break;

            default:
                RUNNING_SPEED = RUNNING_SPEED_LIMIT_15;
                break;
        }
        Log.i(TAG, "initRunningSpeed: RUNNING_SPEED: " + RUNNING_SPEED + " , countryCode: " + countryCode);
    }
}
