package com.desaysv.localmediasdk.manager;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.desaysv.localmediasdk.bean.PackageConfig;
import com.desaysv.localmediasdk.remote.BaseRemoteManager;
import com.desaysv.localmediasdk.remote.DsvMusicRemoteManager;


/**
 * Created by LZM on 2020-3-18
 * Comment 用来做服务绑定，绑定成功之后，将AIDL的binder给到各个控制管理类
 */
public class DsvMusicServiceProxy extends BaseServiceProxy {

    private static final String TAG = "DsvMusicServiceProxy";

    //这里包名可以根据需求变化绑定的个数
    private static final String SERVICE_PACKAGE_NAME = PackageConfig.MUSIC_APP_PACKAGE;

    private static final String SERVICE_CLASS_NAME = PackageConfig.MUSIC_APP_AIDL_SERVICE;


    @SuppressLint("StaticFieldLeak")
    private static DsvMusicServiceProxy instance;

    public static DsvMusicServiceProxy getInstance() {
        if (instance == null){
            synchronized (DsvMusicServiceProxy.class ){
                if (instance == null){
                    instance = new DsvMusicServiceProxy();
                }
            }
        }
        return instance;
    }

    private DsvMusicServiceProxy(){
        super();
    }

    @Override
    protected String getPackageName() {
        boolean hasMusicAPP = hasMusicApp();
        Log.d(TAG,"getPackageName,hasMusicAPP："+ hasMusicAPP);
        if (hasMusicAPP){
            return PackageConfig.MUSIC_APP_PACKAGE_T1EJ;
        }else {
            return PackageConfig.MUSIC_APP_PACKAGE;
        }
    }

    @Override
    protected String getClassName() {
        return SERVICE_CLASS_NAME;
    }

    @Override
    protected BaseRemoteManager getRemoteManager() {
        return DsvMusicRemoteManager.getInstance();
    }

    private boolean hasMusicApp(){
        PackageManager package_manager = mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = package_manager.getApplicationInfo(PackageConfig.MUSIC_APP_PACKAGE_T1EJ, 0);
        }catch (PackageManager.NameNotFoundException e){
            return false;
        }
        return true;
    }
}
