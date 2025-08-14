package com.desaysv.svmediaservicelogic.systemcontrol;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.desaysv.mediasdk.bean.StartSourceIntentBean;
import com.desaysv.mediasdk.manager.DsvSourceMediaManager;
import com.desaysv.svmediaservicelogic.modecontrol.ModeSourceSelectManager;

/**
 * Created by LZM on 2020-3-16
 * Comment mode 按键的控制逻辑
 * @author uidp5370
 */
public class ModeControl {

    private static final String TAG = "ModeControl";

    private static ModeControl instance;

    public static ModeControl getInstance() {
        if (instance == null) {
            synchronized (ModeControl.class) {
                if (instance == null) {
                    instance = new ModeControl();
                }
            }
        }
        return instance;
    }

    /**
     * mode按键的消抖时间
     */
    private static final int DELAY_TIME = 1000;

    /**
     * 当前mode的定时器是否在跑的标志位
     */
    private boolean isTimerRunning = false;

    /**
     * 是否在定时器跑的过程中按了mode按键，如果按理，定时器跑完之后，需要再次切换mode
     */
    private boolean isNeedToChangeModeWhenTimerEnd = false;

    /**
     * 定时器Handler的what
     */
    private static final int TIMER_CHANGE_MODE = 1;

    private Context mContext;

    /**
     * 用来实现定时器的Handler
     */
    private Handler mHandler;

    /**
     * MODE切换逻辑的构造函数，启动一个子线程的handler，用来实现mode逻辑的消抖
     */
    private ModeControl() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == TIMER_CHANGE_MODE) {
                    //定时器已经跑完了，需要将标志位清楚掉
                    Log.d(TAG, "handleMessage: isNeedToChangeModeWhenTimerEnd = " + isNeedToChangeModeWhenTimerEnd);
                    isTimerRunning = false;
                    if (isNeedToChangeModeWhenTimerEnd) {
                        //跑定时器的过程中，有mode按键触发，需要在定时器结束之后，再跑一次莫得
                        startChangeMode();
                    }
                }
            }
        };
    }


    private String mCurrentModeSource = "";

    public void setCurrentModeSource(String modeSource) {
        Log.d(TAG, "setCurrentModeSource: mCurrentModeSource = " + mCurrentModeSource + " modeSource = " + modeSource);
        mCurrentModeSource = modeSource;
    }


    /**
     * 外部启动mode按键的逻辑
     *
     * @param context 上下文，这里的上下文都是application的上下文，所以可以作为常量保存起来
     */
    public void dealModeFunction(Context context) {
        Log.d(TAG, "dealModeFunction: isTimerRunning = " + isTimerRunning);
        mContext = context;
        if (isTimerRunning) {
            //如果当前定时器在跑，那就不能响应mode按键，并且设置标志位，定时器跑完之后，要再切换一次音源
            isNeedToChangeModeWhenTimerEnd = true;
        } else {
            //如果定时器没有跑，那就说明可以直接切换
            startChangeMode();
        }

    }

    /**
     * 开始进行mode逻辑的切换
     */
    private void startChangeMode() {
        //启动消抖定时器,做过滤逻辑
        startTimer();
        //进行mode切换逻辑的判断
        String nextSource = ModeSourceSelectManager.getInstance().getNextEffectSource(mCurrentModeSource);
        Log.d(TAG, "startChangeMode: nextSource = " + nextSource);
        //这里切换后就记忆mode的当前音源，避免切换之后，在线音乐不申请音频焦点，导致mode按键切换不过去
        setCurrentModeSource(nextSource);
        //讯飞一体化媒体定制需求，mode音源对外都是
        DsvSourceMediaManager.getInstance().openSource(mContext, nextSource, true,
                StartSourceIntentBean.OPEN_REASON_MODE);
    }

    /**
     * 启动消抖定时器
     */
    private void startTimer() {
        isTimerRunning = true;
        isNeedToChangeModeWhenTimerEnd = false;
        mHandler.removeMessages(TIMER_CHANGE_MODE);
        mHandler.sendEmptyMessageDelayed(TIMER_CHANGE_MODE, DELAY_TIME);
    }

}
