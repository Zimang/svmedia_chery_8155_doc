package com.desaysv.libradio.utils;

/**
 * Created by EXT10324 on 2025/7/26
 * <p>
 * 类说明：
 */

import android.util.Log;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.base.VDKey;
import com.desaysv.ivi.vdb.event.id.carstate.VDEventCarState;
import com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState;

public class PowerStatusUtil {

    private static final String TAG = "PowerStatusUtil";

    /**
     * 获取电源状态
     *
     * @return 电源状态 {@link com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState.PowerStatus}
     */
    public static int getPowerState() {
        VDEvent event = VDBus.getDefault().getOnce(VDEventCarState.POWER_STATUS);
        if (event != null && event.getPayload() != null) // 服务未绑定，会返回null
            return event.getPayload().getInt(VDKey.STATUS);
        return -1;  //-1表示无效值；
    }

    /**
     * 非AVN_OFF STR SLEEP
     * @return
     */
    public static boolean isPowerNotInStrOrSleep() {
        int powerState = getPowerState();
        Log.d(TAG,"getPowerState powerState = " + powerState);
        return powerState != VDValueCarState.PowerStatus.STATE_AVN_OFF && powerState != VDValueCarState.PowerStatus.STATE_AVN_STR && powerState != VDValueCarState.PowerStatus.STATE_AVN_SLEEP;
    }

}