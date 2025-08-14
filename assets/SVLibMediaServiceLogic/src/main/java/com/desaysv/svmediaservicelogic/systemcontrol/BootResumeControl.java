package com.desaysv.svmediaservicelogic.systemcontrol;

import android.content.Context;
import android.util.Log;
import com.desaysv.mediasdk.bean.StartSourceIntentBean;
import com.desaysv.mediasdk.manager.DsvSourceMediaManager;

/**
 * Created by LZM on 2020-3-15
 * Comment 音源恢复的控制类，对外提供的音源恢复，这里也是实现源恢复业务逻辑的地方
 * @author uidp5370
 */
public class BootResumeControl {

    private static final String TAG = "BootResumeControl";

    private static BootResumeControl instance;

    //TODO：先提供单例模式给到Service使用，看后续能否使用放射的方式，实现Logic和Service的解耦
    public static BootResumeControl getInstance() {
        if (instance == null) {
            synchronized (BootResumeControl.class) {
                if (instance == null) {
                    instance = new BootResumeControl();
                }
            }
        }
        return instance;
    }


    private BootResumeControl() {

    }


    /**
     * 提供给服务的音源恢复方法
     *
     * @param source 需要恢复的音源
     */
    //TODO: 后面看能否提供反射的方法，实现服务和module相互分离，解耦合
    public void bootResumeSource(Context context, String source) {
        Log.d(TAG, "bootResumeSource: source = " + source);
        DsvSourceMediaManager.getInstance().openSource(context, source, false,
                StartSourceIntentBean.OPEN_REASON_BOOT_RESUME);

    }

}
