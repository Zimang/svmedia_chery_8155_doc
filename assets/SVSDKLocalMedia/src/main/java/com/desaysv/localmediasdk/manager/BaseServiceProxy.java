package com.desaysv.localmediasdk.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.desaysv.localmediasdk.IAIDLMediaManager;
import com.desaysv.localmediasdk.listener.IServiceConnectCallback;
import com.desaysv.localmediasdk.remote.BaseRemoteManager;
import com.desaysv.localmediasdk.utils.MultiTaskTimer;

/**
 * Created by LZM on 2020-3-18
 * Comment
 */
public abstract class BaseServiceProxy {

    private final String TAG = this.getClass().getSimpleName();

    protected abstract String getPackageName();

    protected abstract String getClassName();

    protected abstract BaseRemoteManager getRemoteManager();

    protected Context mContext = null;

    private IAIDLMediaManager binderAidl;

    //服务连接变化的回调
    private IServiceConnectCallback mServiceConnectCallback;


    /**
     * 定时器
     */
    private MultiTaskTimer mTimer = null;
    /**
     * 定时器id
     */
    private final static int TIMER_ID_REBIND_SERVICE = 0;
    private final static int TIMER_TIME_REBIND_SERVICE = 5000;

    /**
     * 在构造函数中初始化定时器
     */
    protected BaseServiceProxy() {
        mTimer = new MultiTaskTimer(iTimerTaskHandler);
    }

    /**
     * 初始化
     *
     * @param context                上下文
     * @param serviceConnectCallback 服务连接状态的回调
     */
    public void initialize(Context context, IServiceConnectCallback serviceConnectCallback) {
        mContext = context;
        mServiceConnectCallback = serviceConnectCallback;
    }

    /**
     * 绑定服务的机制
     */
    public void bindService() {
        Log.d(TAG, "bindService: packageName = " + getPackageName());
        try {
            Intent service = new Intent();
            service.setClassName(getPackageName(), getClassName());
            mContext.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resBindService();
        }
    }

    /**
     * 绑定之后启动定时器
     */
    private void resBindService() {
        //启动重连服务定时器
        if (binderAidl == null && mTimer != null) {
            mTimer.setTimeTask(TIMER_ID_REBIND_SERVICE, TIMER_TIME_REBIND_SERVICE);
        }
    }

    /**
     * 定时器回调，在里面会再次绑定服务
     */
    private MultiTaskTimer.ITimerTaskHandler iTimerTaskHandler = new MultiTaskTimer.ITimerTaskHandler() {
        @Override
        public void onTimerTaskHandle(int taskId) {
            if (taskId == TIMER_ID_REBIND_SERVICE) {
                if (mTimer != null && mTimer.isTaskExist(TIMER_ID_REBIND_SERVICE)){
                    mTimer.cancelTimeTask(TIMER_ID_REBIND_SERVICE);
                }
                bindService();
            }
        }
    };

    /**
     * 服务连接的回调
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binderAidl = IAIDLMediaManager.Stub.asInterface(iBinder);
            getRemoteManager().initialize(binderAidl);
            Log.d(TAG, "onServiceConnected: binderAidl = " + binderAidl);
            //绑定成功，取消定时器
            if (mTimer != null && mTimer.isTaskExist(TIMER_ID_REBIND_SERVICE)) {
                mTimer.cancelTimeTask(TIMER_ID_REBIND_SERVICE);
            }
            if (mServiceConnectCallback != null) {
                mServiceConnectCallback.onServiceConnect();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
            getRemoteManager().destroy();
            if (mServiceConnectCallback != null) {
                mServiceConnectCallback.onServiceDisConnect();
            }
            //启动重连服务定时器
            if (mTimer != null) {
                mTimer.setTimeTask(TIMER_ID_REBIND_SERVICE, TIMER_TIME_REBIND_SERVICE);
            }
            binderAidl = null;
        }
    };
}
