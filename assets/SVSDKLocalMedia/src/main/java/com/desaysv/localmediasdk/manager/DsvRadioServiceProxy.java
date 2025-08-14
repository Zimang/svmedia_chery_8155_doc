package com.desaysv.localmediasdk.manager;


import com.desaysv.localmediasdk.bean.PackageConfig;
import com.desaysv.localmediasdk.remote.BaseRemoteManager;
import com.desaysv.localmediasdk.remote.DsvRadioRemoteManager;


/**
 * Created by LZM on 2020-3-18
 * Comment 用来做服务绑定，绑定成功之后，将AIDL的binder给到各个控制管理类
 */
public class DsvRadioServiceProxy extends BaseServiceProxy{

    private static final String TAG = "DsvRadioServiceProxy";

    //这里包名可以根据需求变化绑定的个数
    private static final String SERVICE_PACKAGE_NAME = PackageConfig.RADIO_APP_PACKAGE;

    private static final String SERVICE_CLASS_NAME = PackageConfig.RADIO_APP_AIDL_SERVICE;


    private static DsvRadioServiceProxy instance;


    public static DsvRadioServiceProxy getInstance() {
        if (instance == null) {
            synchronized (DsvRadioServiceProxy.class) {
                if (instance == null) {
                    instance = new DsvRadioServiceProxy();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getPackageName() {
        return SERVICE_PACKAGE_NAME;
    }

    @Override
    protected String getClassName() {
        return SERVICE_CLASS_NAME;
    }

    @Override
    protected BaseRemoteManager getRemoteManager() {
        return DsvRadioRemoteManager.getInstance();
    }

    private DsvRadioServiceProxy() {
        super();
    }

}
