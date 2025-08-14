package com.desaysv.videoapp.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.libdevicestatus.manager.SourceStatusManager;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.moduleusbvideo.trigger.ModuleUSBVideoTrigger;
import com.desaysv.moduleusbvideo.util.LionPointSdkUtil;
import com.desaysv.moduleusbvideo.vr.VideoVRControl;


/**
 * @author uidp5370
 * @date 2019-6-3
 * 多媒体的app，这个app集成了USB音乐，视频，图片和收音机
 * 再onCreate的时候进行了初始化
 */
public class VideoApplication extends AppBase {
    private static MyActivityLifecycle myActivityLifecycle;
    private static final String TAG = "VideoApplication";

//    private static final String RESUME_SERVICE_NAME = "com.desaysv.mediadvp.service.BootResumeSourceService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        //USB设备的初始化，由于后续逻辑都需要根据设备状态来走的，所以设备状态的初始化必须在最前面
        SourceStatusManager.getInstance().initialize(getApplicationContext());
        //各个Module的初始化
        ModuleUSBVideoTrigger.getInstance().initialize(getApplicationContext());
        myActivityLifecycle = new MyActivityLifecycle();
        registerActivityLifecycleCallbacks(myActivityLifecycle);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e(TAG, "onTerminate: ");
        ModuleUSBVideoTrigger.getInstance().unInitialize();
        unregisterActivityLifecycleCallbacks(myActivityLifecycle);
    }
    public class MyActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private int startCount;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//            Log.d("==============","======>onActivityCreated");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.d("==============","======>onActivityStarted");
            VideoVRControl.getInstance().notifyCurrentVideo();
            startCount++;
            //打开USB视频埋点
            LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_OPEN_CLICK, LionPointSdkUtil.FIELD_OPEN_TYPE, LionPointSdkUtil.FIELD_CLICK_OPEN_CLOSE);
            LionPointSdkUtil.getInstance().sendPlatformLionPoint(LionPointSdkUtil.FILED_OPSMODE, LionPointSdkUtil.FIELD_CLICK_OPEN_CLOSE, LionPointSdkUtil.KEY_APP_OPEN);
        }

        @Override
        public void onActivityResumed(Activity activity) {
//            Log.d("==============","======>onActivityResumed");
            Log.d(TAG, "onActivityResumed: "+activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityPaused(Activity activity) {
//            Log.d("==============","======>onActivityPaused");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.d("==============","======>onActivityStopped");
            Log.d(TAG, "onActivityStopped: "+activity.getClass().getSimpleName());
            //应用内界面之间的跳转就不用传值给讯飞
            if(!TextUtils.equals(activity.getPackageName(),VideoVRControl.getInstance().getTopActivityPackageName())){
                Log.d(TAG, "onActivityStopped: activity is fg");
                VideoVRControl.getInstance().notifyCurrentVideo();
            }
            startCount--;
            //关闭USB视频数据埋点
            LionPointSdkUtil.getInstance().sendLionPoint(LionPointSdkUtil.LION_CLOSE_CLICK, LionPointSdkUtil.FIELD_CLOSE_TYPE, LionPointSdkUtil.FIELD_CLICK_OPEN_CLOSE);
            LionPointSdkUtil.getInstance().sendPlatformLionPoint(LionPointSdkUtil.FILED_CLSMODE, LionPointSdkUtil.FIELD_CLICK_OPEN_CLOSE, LionPointSdkUtil.KEY_APP_CLOSE);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
//            Log.d("==============","======>onActivityDestroyed");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//            Log.e("==============","======>onActivitySaveInstanceState");
        }

        public int getStartCount(){
            return startCount;
        }
    }

}
