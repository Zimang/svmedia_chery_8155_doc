package com.desaysv.localmediasdk.remote;

import android.os.RemoteException;
import android.util.Log;

import com.desaysv.localmediasdk.IAIDLMediaInfoCallback;
import com.desaysv.localmediasdk.IAIDLMediaManager;
import com.desaysv.localmediasdk.bean.RemoteBeanList;
import com.desaysv.localmediasdk.listener.IRemoteMediaInfoCallBack;
import com.desaysv.localmediasdk.listener.IRemoteMediaManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LZM on 2020-3-18
 * Comment
 *
 * @author uidp5370
 */
public abstract class BaseRemoteManager implements IRemoteMediaManager {

    private final String TAG = this.getClass().getSimpleName();

    private static DsvMusicRemoteManager instance;

    /**
     * 服务端媒体的注册回调
     */
    private List<IRemoteMediaInfoCallBack> remoteMediaInfoCallbacks = new ArrayList<>();

    /**
     * 是否初始化成功
     */
    private boolean isInitSuccess;

    /**
     * 设置远端服务的AIDL控制器
     *
     * @param iAidlMediaManager AIDL的binder
     */
    protected abstract void setAidlMediaManager(IAIDLMediaManager iAidlMediaManager);


    /**
     * 获取远端服务的AIDL控制器
     *
     * @return IAIDLMediaManager AIDL的binder
     */
    protected abstract IAIDLMediaManager getAidlMediaManager();


    /**
     * 初始化逻辑，初始化的时候，需要将远程的媒体控制器设置进去
     *
     * @param iMediaManager AIDL的绑定服务
     */
    @Override
    public void initialize(IAIDLMediaManager iMediaManager) {
        Log.d(TAG, "initialize: ");
        setAidlMediaManager(iMediaManager);
        isInitSuccess = true;
        registerAidlCallback();
    }

    /**
     * 服务断开的时候，需要将媒体播放器销毁掉
     */
    @Override
    public void destroy() {
        Log.d(TAG, "destroy: ");
        setAidlMediaManager(null);
        isInitSuccess = false;
        //服务端挂了，那也要将注册AIDL服务的标志位清除掉
        isRegisterAidlCallback = false;
    }


    /**
     * 发送控制命令
     *
     * @param source  需要控制的音源
     * @param command 需要控制的动作，上一曲，下一曲等
     */
    @Override
    public void sendCommand(String source, String command, String info) {
        Log.d(TAG, "sendCommand: source = " + source + " command = " + command + " info = " + info + " isInitSuccess = " + isInitSuccess);
        if (getAidlMediaManager() != null) {
            try {
                getAidlMediaManager().sendCommand(source, command, info);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public RemoteBeanList getRemoteList(String source, String type) {
        RemoteBeanList remoteBeanList = null;
        if (getAidlMediaManager() != null) {
            try {
                remoteBeanList = getAidlMediaManager().getRemoteList(source, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getRemoteList: source = " + source + " type = " + type + " isInitSuccess = " + isInitSuccess);
        return remoteBeanList;
    }

    @Override
    public String getSelectInfo(String source, String type) {
        String selectInfo = "";
        if (getAidlMediaManager() != null) {
            try {
                selectInfo = getAidlMediaManager().getSelectInfo(source, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getSelectInfo: source = " + source + " type = " + type + " isInitSuccess = " + isInitSuccess);
        return selectInfo;
    }


    /**
     * 获取图片数据
     *
     * @return byte[]
     */
    @Override
    public byte[] getPicData(String source) {
        byte[] picData = null;
        if (getAidlMediaManager() != null) {
            try {
                picData = getAidlMediaManager().getCurrentPic(source);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (picData == null) {
            Log.d(TAG, "getPicData: picData is null");
        } else {
            Log.d(TAG, "getPicData: picData = " + picData.length);
        }
        return picData;
    }

    /**
     * 注册媒体变化的回调广播
     *
     * @param iRemoteMediaInfoCallBack 数据变化的回调
     */
    @Override
    public void registerMediaInfoCallback(IRemoteMediaInfoCallBack iRemoteMediaInfoCallBack) {
        Log.d(TAG, "registerMediaInfoCallback:   isInitSuccess = " + isInitSuccess);
        if (iRemoteMediaInfoCallBack != null) {
            remoteMediaInfoCallbacks.remove(iRemoteMediaInfoCallBack);
            remoteMediaInfoCallbacks.add(iRemoteMediaInfoCallBack);
        }
        registerAidlCallback();
    }

    /**
     * 注销媒体变化的回调广播
     *
     * @param iRemoteMediaInfoCallBack 数据变化的回调
     */
    @Override
    public void unRegisterMediaInfoCallback(IRemoteMediaInfoCallBack iRemoteMediaInfoCallBack) {
        Log.d(TAG, "unRegisterMediaInfoCallback: isInitSuccess = " + isInitSuccess);
        if (iRemoteMediaInfoCallBack != null) {
            remoteMediaInfoCallbacks.remove(iRemoteMediaInfoCallBack);
        }
        //如果列表清空了，那就需要将AIDL回调注销掉
        if (remoteMediaInfoCallbacks.isEmpty()) {
            unregisterAidlCallback();
        }
    }


    /**
     * 获取是否初始化完成，如果没有初始化完成，调用方法会出现空指针异常
     *
     * @return true 初始化完成；false 初始化失败
     */
    @Override
    public boolean getInitSuccess() {
        Log.d(TAG, "getInitSuccess: isInitSuccess = " + isInitSuccess);
        return isInitSuccess;
    }

    /**
     * 是否已经注册过AIDL的回调了
     */
    private boolean isRegisterAidlCallback = false;

    /**
     * 注册AIDL回调
     * 有两个地方需要调用
     * 1. 服务绑定成功之后，如果回调个数大于1的话，要注册
     * 2. 媒体client端的回调个数大于1的时候，要注册
     * <p>
     * 然后回调注册的判断条件有3个
     * 1. 服务初始化成功
     * 2. 注册的回调列表不为空
     * 3. 之前没有注册过AIDL接口
     */
    private void registerAidlCallback() {
        Log.d(TAG, "registerAidlCallback: ");
        //是否注册过AIDL回调，如果注册过了，那就不注册了
        if (!isRegisterAidlCallback && !remoteMediaInfoCallbacks.isEmpty() && isInitSuccess) {
            try {
                getAidlMediaManager().registerMediaInfoCallback(this.getClass().getName(),
                        iAidlMediaInfoCallback);
                //注册成功了，那就说明AIDL服务注册过了
                isRegisterAidlCallback = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 注销AIDL回调
     * 需要调用地方理论上有两个
     * 1. 服务断开的时候，不过服务都断开了，还注销AIDL，没有用，所以不调用
     * 2. 回调注销的时候，如果回调个数注销到0，那就需要调用了
     */
    private void unregisterAidlCallback() {
        Log.d(TAG, "unregisterAidlCallback: ");
        if (null == getAidlMediaManager()) {
            return;
        }
        //是否注销AIDL回调，如果注销过了，那就不注销了
        try {
            getAidlMediaManager().unregisterMediaInfoCallback(this.getClass().getName(), iAidlMediaInfoCallback);
            //注销成功，那就没有注册过AIDL回调了
            isRegisterAidlCallback = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 外部AIDL的回调，在这里转化一下
     */
    IAIDLMediaInfoCallback.Stub iAidlMediaInfoCallback = new IAIDLMediaInfoCallback.Stub() {

        @Override
        public void onMediaInfoChange(String source, String InfoType, String mediaInfo) throws RemoteException {
            Log.d(TAG, "onMediaInfoChange: mediaInfoCallbacks size = " + remoteMediaInfoCallbacks.size());
            for (IRemoteMediaInfoCallBack iLocalMediaInfoCallBack : remoteMediaInfoCallbacks) {
                iLocalMediaInfoCallBack.onMediaInfoChange(source, InfoType, mediaInfo);
            }
        }

        @Override
        public void onMediaPicChange(String source, byte[] picInfo) throws RemoteException {
            Log.d(TAG, "onMediaPicChange: mediaInfoCallbacks size = " + remoteMediaInfoCallbacks.size());
            for (IRemoteMediaInfoCallBack iLocalMediaInfoCallBack : remoteMediaInfoCallbacks) {
                iLocalMediaInfoCallBack.onMediaPicChange(source, picInfo);
            }
        }

        @Override
        public void onRemoteListChanged(String source, String type, RemoteBeanList remoteList) {
            Log.d(TAG, "onRemoteListChanged: source = " + source + ", type = " + type);
            for (IRemoteMediaInfoCallBack iLocalMediaInfoCallBack : remoteMediaInfoCallbacks) {
                iLocalMediaInfoCallBack.onRemoteListChanged(source, type, remoteList);
            }
        }
    };
}
