package com.desaysv.localmediasdk.remote;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.desaysv.localmediasdk.IAIDLMediaManager;
import com.desaysv.localmediasdk.bean.PackageConfig;
import com.desaysv.localmediasdk.bean.StartSourceIntentBean;

import java.util.Iterator;
import java.util.List;

/**
 * Created by LZM on 2020-3-18
 * Comment 本地收音的对接列，AIDL注册成功之后，就会
 */
public class DsvRadioRemoteManager extends BaseRemoteManager {

    private static final String TAG = "DsvRadioRemoteManager";

    private static DsvRadioRemoteManager instance;

    /**
     * 媒体端的AIDL binder
     */
    private IAIDLMediaManager mRadioAIDLManager;

    public static DsvRadioRemoteManager getInstance() {
        if (instance == null) {
            synchronized (DsvRadioRemoteManager.class) {
                if (instance == null) {
                    instance = new DsvRadioRemoteManager();
                }
            }
        }
        return instance;
    }


    private DsvRadioRemoteManager() {

    }


    /**
     * 设置远端服务的AIDL控制器
     *
     * @param iAidlMediaManager AIDL的binder
     */
    @Override
    protected void setAidlMediaManager(IAIDLMediaManager iAidlMediaManager) {
        mRadioAIDLManager = iAidlMediaManager;
    }

    /**
     * 获取远端服务的AIDL控制器
     *
     * @return IAIDLMediaManager AIDL的binder
     */
    @Override
    protected IAIDLMediaManager getAidlMediaManager() {
        return mRadioAIDLManager;
    }

    /**
     * 需要提供一个静态方法给外部使用，用来启动媒体应用
     *
     * @param context        上下文
     * @param source         启动的音源
     * @param isForeground   前后台
     * @param isRequestFocus 是否申请音频焦点
     * @param openReason     启动的原因
     */
    public static void open(Context context, String source, boolean isForeground, boolean isRequestFocus, String openReason) {
        Log.d(TAG, "open: source = " + source + " isForeground = " + isForeground + " isRequestFocus = " + isRequestFocus + " openReason = " + openReason);
        Intent intent = new Intent(StartSourceIntentBean.DESAYSV_ACTION_START_SOURCE);
        intent.putExtra(StartSourceIntentBean.KEY_START_SOURCE, source);
        intent.putExtra(StartSourceIntentBean.KEY_IS_FOREGROUND, isForeground);
        intent.putExtra(StartSourceIntentBean.KEY_IS_REQUEST_FOCUS, isRequestFocus);
        intent.putExtra(StartSourceIntentBean.KEY_OPEN_REASON, openReason);
        intent.setPackage(PackageConfig.RADIO_APP_PACKAGE);
        context.startService(intent);
    }
}
