package com.desaysv.usbpicture.trigger;

import android.content.Context;
import android.util.Log;

import com.desaysv.ivi.vdb.client.VDBus;
import com.desaysv.ivi.vdb.client.bind.VDServiceDef;
import com.desaysv.ivi.vdb.client.bind.VDThreadType;
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener;
import com.desaysv.ivi.vdb.event.VDEvent;
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR;
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine;
import com.desaysv.usbpicture.bean.VRJsonBean;
import com.desaysv.usbpicture.constant.VRAction;
import com.desaysv.usbpicture.trigger.interfaces.IVRResponseOperator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZNB on 2022-05-09
 * 语音控制器的处理
 * 统一封装语义的监听和解析操作
 * 应用的各个部分通过注册为实现者，实现其中的操作
 */
public class PictureVRControl {
    private static final String TAG = "PictureVRControl";

    private static PictureVRControl mInstance;
    private Gson mGson = new Gson();
    public static PictureVRControl getInstance(){
        synchronized (PictureVRControl.class){
            if (mInstance == null){
                mInstance = new PictureVRControl();
            }
            return mInstance;
        }
    }


    /**
     * 订阅语义下发事件
     */
    public void subScribeVR(Context context){
        Log.d(TAG,"subScribeVR");
        VDBus.getDefault().addSubscribe(VDEventVR.VR_PICTURE);
        VDBus.getDefault().subscribeCommit(); // 提交订阅
        VDBus.getDefault().registerVDNotifyListener(mVDNotifyListener);
        VDBus.getDefault().bindService(VDServiceDef.ServiceType.VR);
    }


    /**
     * 取消订阅语义下发事件
     */
    public void unSubScribeVR(){
        Log.d(TAG,"unSubScribeVR");
        VDBus.getDefault().unregisterVDNotifyListener(mVDNotifyListener);
    }



    /**
     * 语义事件的监听
     */
    private  VDNotifyListener mVDNotifyListener = new VDNotifyListener() {
        public void onVDNotify(VDEvent event, int threadType) {
            if (threadType == VDThreadType.MAIN_THREAD) { // 主线程回调处理
                Log.d(TAG,"onVDNotify,VDEvent: "+ event);
                switch (event.getId()) {
                    case VDEventVR.VR_PICTURE:
                        VDVRPipeLine param = VDVRPipeLine.getValue(event);
                        // 内部Json语义表定义的Key值(参考: https://docs.qq.com/sheet/DZVBUYlBFUERNTG5q?tab=o9ktwo)
                        String key = param.getKey();
                        // 内部Json语义表定义的Josn数据, 例: {"action":"OPEN","position":"F","type":"","value":""}
                        String data = param.getValue();
                        Log.d(TAG,"onVDNotify,key: "+ key + ", data: "+data);
                        parserJson(key,data);
                        break;

                    default:
                        break;
                }
            }
        }
    };

    /**
     * 语义事件解析
     * @param key
     * @param data
     */
    private void parserJson(String key, String data){
        VRJsonBean vrJsonBean = mGson.fromJson(data,VRJsonBean.class);
        Log.d(TAG,"parserJson,vrJsonBean: "+ vrJsonBean);
        String action = vrJsonBean.getSemantic().getAction();
        String type = vrJsonBean.getSemantic().getType();
        if (VRAction.KEY_CONTROL_PICTURE.equals(key)) {
            if (VRAction.ACTION_PLAY.equals(action)) {
                play();
            } else if (VRAction.ACTION_PAUSE.equals(action)) {
                pause();
            } else if (VRAction.ACTION_PRE.equals(action)) {
                pre();
            } else if (VRAction.ACTION_NEXT.equals(action)) {
                next();
            } else if (VRAction.ACTION_SHRINK.equals(action)) {
                shrink();
            } else if (VRAction.ACTION_ZOOM.equals(action)) {
                zoom();
            }
        } else if (VRAction.KEY_CONTROL_VIEW.equals(key)) {
            if (VRAction.ACTION_OPEN.equals(action)) {
                if (VRAction.TYPE_BROWSE.equals(type)) {
                    gotoPreview();
                } else if (VRAction.TYPE_IMAGE_LIST.equals(type)){
                    gotoImageList();
                }
            } else if (VRAction.ACTION_CLOSE.equals(action)){
                if (VRAction.TYPE_BROWSE.equals(type)) {
                    exitPreview();
                } else if (VRAction.TYPE_IMAGE_LIST.equals(type)){
                    exitImageList();
                }
            }
        }
    }


    /**
     * 暂停或者播放幻灯片
     */
    private void playOrPause(){
        Log.d(TAG,"playOrPause");
        for (IVRResponseOperator operator : operatorList){
            operator.playOrPause();
        }
    }

    /**
     * 播放幻灯片
     */
    private void play(){
        Log.d(TAG,"play");
        for (IVRResponseOperator operator : operatorList){
            operator.play();
        }
    }


    /**
     * 暂停幻灯片
     */
    private void pause(){
        Log.d(TAG,"pause");
        for (IVRResponseOperator operator : operatorList){
            operator.pause();
        }
    }

    /**
     * 上一张图片
     */
    private void pre(){
        Log.d(TAG,"pre");
        for (IVRResponseOperator operator : operatorList){
            operator.pre();
        }
    }

    /**
     * 下一张图片
     */
    private void next(){
        Log.d(TAG,"next");
        for (IVRResponseOperator operator : operatorList){
            operator.next();
        }
    }

    /**
     * 进入浏览模式
     */
    private void gotoPreview(){
        Log.d(TAG,"gotoPreview");
        for (IVRResponseOperator operator : operatorList){
            operator.goToPreView();
        }
    }

    /**
     * 退出浏览模式
     */
    private void exitPreview(){
        Log.d(TAG,"exitPreview");
        for (IVRResponseOperator operator : operatorList){
            operator.exitPreView();
        }
    }

    /**
     * 进入图片列表
     */
    private void gotoImageList(){
        Log.d(TAG,"gotoImageList");
        for (IVRResponseOperator operator : operatorList){
            operator.goToImageList();
        }
    }

    /**
     * 退出图片列表
     */
    private void exitImageList(){
        Log.d(TAG,"exitImageList");
        for (IVRResponseOperator operator : operatorList){
            operator.exitImageList();
        }
    }

    /**
     * 缩小图片
     */
    private void shrink(){
        Log.d(TAG,"shrink");
        for (IVRResponseOperator operator : operatorList){
//            operator.shrink();//最新需求是没有这个功能
        }
    }

    /**
     * 放大图片
     */
    private void zoom(){
        Log.d(TAG,"zoom");
        for (IVRResponseOperator operator : operatorList){
//            operator.zoom();//最新需求是没有这个功能
        }
    }



    private List<IVRResponseOperator> operatorList = new ArrayList<>();

    /**
     * 注册 响应语音操作 的具体实现者
     * @param operator
     */
    public void registerVRResponseOperator(IVRResponseOperator operator){
        operatorList.add(operator);
    }

    /**
     * 注销 响应语音操作 的具体实现者
     * @param operator
     */
    public void unregisterVRResponseOperator(IVRResponseOperator operator){
        operatorList.remove(operator);
    }


}
