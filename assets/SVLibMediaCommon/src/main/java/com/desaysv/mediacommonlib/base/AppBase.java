package com.desaysv.mediacommonlib.base;

import android.app.Application;
import android.content.Context;
import androidx.annotation.CallSuper;

/**
 * Created by uidp5370 on 2019-6-3.
 * AppBase 继承了Application，主要的作用是将application的context作为一个静态变量抽出
 */

public class AppBase extends Application {

    public static Context mContext;

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
