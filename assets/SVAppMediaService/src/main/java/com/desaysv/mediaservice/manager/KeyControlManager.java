package com.desaysv.mediaservice.manager;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import com.desaysv.svmediaservicelogic.systemcontrol.HardKeyControl;


/**
 * Created by LZM on 2020-2-21
 * Comment 加入按键控制的逻辑
 */
public class KeyControlManager {

    private static final String TAG = "KeyControlManager";

    private static KeyControlManager instance;

    private Context mContext;

    //private KeyPolicyManager mKeyPolicyManager = null;

    private static final String HARD_KEY_CONTROL = "hard_key_control";

    public static KeyControlManager getInstance() {
        if (instance == null) {
            synchronized (KeyControlManager.class) {
                if (instance == null) {
                    instance = new KeyControlManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化业务逻辑
     *
     * @param context
     */
    public void initialize(Context context) {
        mContext = context;
        //mKeyPolicyManager = KeyPolicyManager.get(mContext);
        //registerKeyInfo();
    }


/*    *//**
     * 注册硬按键功能
     *//*
    private void registerKeyInfo() {
        if (mKeyPolicyManager != null) {
            mKeyPolicyManager.registerKeyCallBack(mKeyCallBackListener, mContext.getClass().getName(), mContext.getPackageName());
        }
    }


    *//**
     * 硬按键回调
     *//*
    private KeyPolicyManager.OnKeyCallBackListener mKeyCallBackListener = new KeyPolicyManager.OnKeyCallBackListener() {
        @Override
        public void onKeyEventCallBack(KeyEvent keyEvent) {
            Log.d(TAG, "onKeyEventCallBack: keyEvent = " + keyEvent);
            //由于不需要响应按键的长短按，所以直接在up的时候，才进行触发
            HardKeyControl.getInstance().setHardKeyEvent(keyEvent);
        }
    };*/

}
