package com.desaysv.mediasdk.remote;

import android.content.Context;
import android.util.Log;

import com.desaysv.localmediasdk.listener.IServiceConnectCallback;
import com.desaysv.localmediasdk.manager.DsvRadioManager;
import com.desaysv.localmediasdk.remote.BaseRemoteManager;
import com.desaysv.localmediasdk.remote.DsvRadioRemoteManager;
import com.desaysv.mediasdk.bean.AudioSourceOriginBean;
import com.desaysv.mediasdk.bean.SourceConfig;
import com.desaysv.mediasdk.listener.IMediaFunction;

/**
 * Created by LZM on 2020-5-1
 * Comment 接收外部媒体控制器，进行转化，转化为内部控制的接口
 */
public class RadioRemoteFunction extends BaseRemountFunction {

    private static final String TAG = "RadioRemoteFunction";

    private static IMediaFunction instance;

    public static IMediaFunction getInstance() {
        if (instance == null) {
            synchronized (USBMusicRemoteFunction.class) {
                if (instance == null) {
                    instance = new RadioRemoteFunction();
                }
            }
        }
        return instance;
    }


    private RadioRemoteFunction() {

    }

    @Override
    void initialize(Context context, IServiceConnectCallback serviceConnectCallback) {
        DsvRadioManager.getInstance().initialize(context, serviceConnectCallback);
    }

    /**
     * 获取远程的控制器
     *
     * @return RemoteManager 媒体的远程控制器
     */
    @Override
    BaseRemoteManager getRemoteManager() {
        return DsvRadioRemoteManager.getInstance();
    }

    /**
     * 打开对于的媒体源
     *
     * @param context        上下文
     * @param source         对应的音源
     * @param isForeground   是否是前后台
     * @param isRequestFocus 是否请求音频焦点
     * @param openReason     启动的原因
     */
    @Override
    void openRemoteSource(Context context, String source, boolean isForeground, boolean isRequestFocus, String openReason) {
        DsvRadioRemoteManager.open(context, source, isForeground, true, openReason);
    }

    @Override
    String getOrigin() {
        Log.d(TAG, "getOrigin: ");
        return AudioSourceOriginBean.RADIO_ORIGIN;
    }

    /**
     * 当前对应的音源是否是有效的，如果无效的话，就不用绑定服务，也不用启动了
     *
     * @return true 有效； false 无效
     */
    @Override
    boolean isEffectSource() {
        return SourceConfig.RADIO_SOURCE_EFFECT;
    }
}
