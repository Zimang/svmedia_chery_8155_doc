package com.desaysv.libradio.bean;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.libradio.action.RadioControlAction;
import com.desaysv.mediacommonlib.base.AppBase;

/**
 *
 * @author LZM
 * @date 2019-8-5
 * Comment 收音的区域配置，以及一些标准常量的配置
 */
public class RadioConfig {
    private static final String TAG = "RadioConfig";

    private static RadioConfig instance;

    /**
     * 增加区域配置相关处理
     */

//    //美洲区域 收音芯片对应区域编号7
//    private static final int AM_AMERICAS_MAX = 1710;
//    private static final int AM_AMERICAS_MIN = 530;
//    private static final int AM_AMERICAS_STEP = 10;
//
//    private static final int FM_AMERICAS_MAX = 108000;
//    private static final int FM_AMERICAS_MIN = 87500;
//    private static final int FM_AMERICAS_STEP = 100;

//    //泰国 底层Tuner的区域配置是9
      private static final int THAILAND = 32;
      private static final int THAILAND_HAL = 9;
      private static final int AM_THAILAND_MAX = 1602;
      private static final int AM_THAILAND_MIN = 531;
      private static final int AM_THAILAND_STEP = 9;

      private static final int FM_THAILAND_MAX = 108000;
      private static final int FM_THAILAND_MIN = 87500;
      private static final int FM_THAILAND_STEP = 250;

//    //欧洲区域 收音芯片对应区域编号9
//    private static final int AM_OTHERS_MAX = 1710;
//    private static final int AM_OTHERS_MIN = 522;
//    private static final int AM_OTHERS_STEP = 9;
//
//    private static final int FM_OTHERS_MAX = 108000;
//    private static final int FM_OTHERS_MIN = 87000;
//    private static final int FM_OTHERS_STEP = 100;


    //8155的下线配置
    public static final int ASIA = 0;//00
    public static final int EURO = 1;//01
    public static final int AMERICA = 2;//10
    public static final int LATIN_AMERICA = 3;//11

    public static final int BRZ = 1;//11

    //底层Tuner的区域配置
    //亚洲 = 5   欧洲 = 1 中南美 = 7
    public static final int ASIA_HAL = 5;
    public static final int EURO_HAL = 0;
    public static final int AMERICA_HAL = 6;
    public static final int LATIN_AMERICA_HAL = 7;

    public static final int BRZ_HAL = 8;//巴西


    //亚洲区域
    private static final int AM_ASIA_MAX = 1629;
    private static final int AM_ASIA_MIN = 522;
    private static final int AM_ASIA_STEP = 9;

    private static final int FM_ASIA_MAX = 108000;
    private static final int FM_ASIA_MIN = 87500;
    private static final int FM_ASIA_STEP = 100;

    //欧洲区域
    private static final int AM_EURO_MAX = 1629;
    private static final int AM_EURO_MIN = 531;
    private static final int AM_EURO_STEP = 9;

    private static final int FM_EURO_MAX = 108000;
    private static final int FM_EURO_MIN = 87500;
    private static final int FM_EURO_STEP = 100;

    //美洲区域
    private static final int AM_AMERICAS_MAX = 1710;
    private static final int AM_AMERICAS_MIN = 530;
    private static final int AM_AMERICAS_STEP = 10;

    private static final int FM_AMERICAS_MAX = 107900;
    private static final int FM_AMERICAS_MIN = 87500;
    private static final int FM_AMERICAS_STEP = 200;

    //拉丁美洲区域
    private static final int AM_LATIN_AMERICAS_MAX = 1710;
    private static final int AM_LATIN_AMERICAS_MIN = 530;
    private static final int AM_LATIN_AMERICAS_STEP = 10;

    private static final int FM_LATIN_AMERICAS_MAX = 107900;
    private static final int FM_LATIN_AMERICAS_MIN = 87500;
    private static final int FM_LATIN_AMERICAS_STEP = 100;


    //巴西区域
    private static final int AM_BRZ_MAX = 1710;
    private static final int AM_BRZ_MIN = 530;
    private static final int AM_BRZ_STEP = 10;

    private static final int FM_BRZ_MAX = 108000;
    private static final int FM_BRZ_MIN = 76000;
    private static final int FM_BRZ_STEP = 100;


    public static int AM_MAX = AM_EURO_MAX;
    public static int AM_MIN = AM_EURO_MIN;
    public static int AM_STEP = AM_EURO_STEP;
    public static int FM_MAX = FM_EURO_MAX;
    public static int FM_MIN = FM_EURO_MIN;
    public static int FM_STEP = FM_EURO_STEP;

    private int region = EURO;//国际项目且带DAB，默认是欧洲

    //收音区域2的配置，优先级高于region
    //先读取收音区域2的配置，不为0的时候优先使用
    private int region2 = 0;
    private SharedPreferences spRegion;
    private SharedPreferences.Editor edRegion;
    public static final String RADIO_REGION = "radio_region";
    public static final String RADIO_REGION2 = "radio_region2";
    private boolean needResetRegion = false;


    public static RadioConfig getInstance() {
        if (instance == null) {
            synchronized (RadioConfig.class) {
                if (instance == null) {
                    instance = new RadioConfig();
                }
            }
        }
        return instance;
    }

    /**
     * 获取默认的收音区域配置
     * @return
     */
    public int getCurrentRegion(){
        CarConfigUtil.getDefault().init(AppBase.mContext);
        region = CarConfigUtil.getDefault().getConfig(Constants.ID_CAR_CONFIG_RADIO_AREA);
        region2 = CarConfigUtil.getDefault().getConfig(Constants.ID_CAR_CONFIG_RADIO_AREA2);
        Log.d(TAG,"getCurrentRegion, region: "+ region + ", region2: " + region2);
        return region2 != 0 ? region2 : region;
    }

    /**
     * 比较保存的区域和当前的区域是否一致，不一致且保存的不是 -1，说明收音区域有变化
     * 需要执行重置列表等操作
     */
    public void checkAndSetPreRegion(){
        spRegion = AppBase.mContext.getSharedPreferences(RADIO_REGION, MODE_PRIVATE);
        edRegion = spRegion.edit();
        int preRegion = spRegion.getInt(RADIO_REGION, -1);
        int preRegion2 = spRegion.getInt(RADIO_REGION2, -1);
        //下线配置变化，需要重新get一次
        region = CarConfigUtil.getDefault().getConfig(Constants.ID_CAR_CONFIG_RADIO_AREA);
        region2 = CarConfigUtil.getDefault().getConfig(Constants.ID_CAR_CONFIG_RADIO_AREA2);
        Log.d(TAG,"checkRegion, region: "+region+", preRegion: "+preRegion + ", preRegion2: " + preRegion2 + ", region2: "+region2);

        String halRegion = SystemProperties.get("persist.sys.tuner.region.type","-1");
        Log.d(TAG,"checkRegion, halRegion:"+halRegion);

        if (region2 != 0){
            if (region2 != preRegion2 && preRegion2 != -1){//收音区域变化，执行清空列表、设置Tuner区域的处理
                RadioList.getInstance().clearAllList();
                initialize(region2);
                RadioControlAction.getInstance().setRadioType(convertCarConfigRegion2ToHalRegion2(region2));
            }else if (preRegion2 == -1 || halRegion.equals("-1")){//首次使用或者底层未配置对应属性，同步区域给底层
                RadioControlAction.getInstance().setRadioType(convertCarConfigRegion2ToHalRegion2(region2));
            }
            edRegion.putInt(RADIO_REGION2, region2);
            edRegion.commit();
        }else {
            if (region != preRegion && preRegion != -1){//收音区域变化，执行清空列表、设置Tuner区域的处理
                RadioList.getInstance().clearAllList();
                initialize(region);
                RadioControlAction.getInstance().setRadioType(convertCarConfigRegionToHalRegion(region));
            }else if (preRegion == -1 || halRegion.equals("-1")){//首次使用或者底层未配置对应属性，同步区域给底层
                RadioControlAction.getInstance().setRadioType(convertCarConfigRegionToHalRegion(region));
            }
            edRegion.putInt(RADIO_REGION, region);
            edRegion.commit();
        }

    }

    /**
     * 将下线配置的收音区域，转化为底层Tuner的收音区域
     * @param carConfigRegion
     * @return
     */
    private int convertCarConfigRegionToHalRegion(int carConfigRegion){
        Log.d(TAG,",convertCarConfigRegionToHal,RegioncarConfigRegion:"+carConfigRegion);
        if (carConfigRegion == ASIA){
            if (isThailand()) {
                return THAILAND_HAL;
            } else {
                return ASIA_HAL;
            }
        }else if (carConfigRegion == EURO){
            return EURO_HAL;
        }else if (carConfigRegion == AMERICA){
            return AMERICA_HAL;
        }else if (carConfigRegion == LATIN_AMERICA){
            return LATIN_AMERICA_HAL;
        }else if (carConfigRegion == BRZ){
            return BRZ_HAL;
        }

        return EURO;
    }

    private boolean isThailand() {
        Log.d(TAG, "initialize() called with: country = [" + CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION) + "]" + "hasDab = [" + hasDAB() + "]");
        return CarConfigUtil.getDefault().getConfig(Constants.ID_COUNTRY_OR_REGION) == THAILAND && hasDAB();
    }

    private boolean hasDAB() {
       return CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }

    /**
     * 将下线配置的收音区域2，转化为底层Tuner的收音区域
     * @param carConfigRegion2
     * @return
     */
    private int convertCarConfigRegion2ToHalRegion2(int carConfigRegion2){
        Log.d(TAG,",convertCarConfigRegion2ToHalRegion2,carConfigRegion2:"+carConfigRegion2);
        if (carConfigRegion2 == BRZ){
            return BRZ_HAL;
        }

        return EURO;
    }


    public void initialize(int radioArea) {
        Log.d(TAG,"initialize,radioArea:"+radioArea + ", region2 = " + region2);
        if (region2 != 0){
            if (radioArea == BRZ) {
                AM_MAX = AM_BRZ_MAX;
                AM_MIN = AM_BRZ_MIN;
                AM_STEP = AM_BRZ_STEP;
                FM_MAX = FM_BRZ_MAX;
                FM_MIN = FM_BRZ_MIN;
                FM_STEP = FM_BRZ_STEP;
            }
        }else {
            if (radioArea == ASIA) {
                if (isThailand()) {
                    AM_MAX = AM_THAILAND_MAX;
                    AM_MIN = AM_THAILAND_MIN;
                    AM_STEP = AM_THAILAND_STEP;
                    FM_MAX = FM_THAILAND_MAX;
                    FM_MIN = FM_THAILAND_MIN;
                    FM_STEP = FM_THAILAND_STEP;
                } else {
                    AM_MAX = AM_ASIA_MAX;
                    AM_MIN = AM_ASIA_MIN;
                    AM_STEP = AM_ASIA_STEP;
                    FM_MAX = FM_ASIA_MAX;
                    FM_MIN = FM_ASIA_MIN;
                    FM_STEP = FM_ASIA_STEP;
                }
            }else if (radioArea == AMERICA){
                AM_MAX = AM_AMERICAS_MAX;
                AM_MIN = AM_AMERICAS_MIN;
                AM_STEP = AM_AMERICAS_STEP;
                FM_MAX = FM_AMERICAS_MAX;
                FM_MIN = FM_AMERICAS_MIN;
                FM_STEP = FM_AMERICAS_STEP;
            }else if (radioArea == EURO){
                AM_MAX = AM_EURO_MAX;
                AM_MIN = AM_EURO_MIN;
                AM_STEP = AM_EURO_STEP;
                FM_MAX = FM_EURO_MAX;
                FM_MIN = FM_EURO_MIN;
                FM_STEP = FM_EURO_STEP;
            }else if (radioArea == LATIN_AMERICA){
                AM_MAX = AM_LATIN_AMERICAS_MAX;
                AM_MIN = AM_LATIN_AMERICAS_MIN;
                AM_STEP = AM_LATIN_AMERICAS_STEP;
                FM_MAX = FM_LATIN_AMERICAS_MAX;
                FM_MIN = FM_LATIN_AMERICAS_MIN;
                FM_STEP = FM_LATIN_AMERICAS_STEP;
            }
        }
    }

    public List<Float> createFrequencyList(float min, float max, float step) {
        float gap = max - min;
        int size = (int) (gap / step);
        List<Float> list = new ArrayList<>();
        for (int i = 0; i <= size; i++) {
            list.add(min + step * i);
        }
//        // 尾部增加无效频点（值为-1），让首尾刻度不要重合
//        int emptyFrequencySize = 9 - size % 10;
//        for (int i = 0; i < emptyFrequencySize; i++) {
//            list.add(-1f);
//        }
        return list;
    }
}
