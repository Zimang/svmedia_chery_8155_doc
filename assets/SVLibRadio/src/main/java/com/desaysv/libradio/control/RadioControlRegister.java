package com.desaysv.libradio.control;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.libradio.action.RadioControlAction;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.RadioConfig;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.interfaze.IControlTool;
import com.desaysv.libradio.interfaze.IGetControlTool;
import com.desaysv.libradio.interfaze.IRadioStatusChange;
import com.desaysv.libradio.interfaze.IStatusTool;
import com.desaysv.libradio.utils.RadioListSaveUtils;
import com.desaysv.libradio.utils.RadioMessageSaveUtils;
import com.desaysv.mediacommonlib.base.AppBase;

/**
 * Created by LZM on 2019-7-10.
 * Comment 收音控制器的注册系统，系统通过这个方法统一获取到收音的控制，状态获取，以及注册状态回调
 */
public class RadioControlRegister {

    private static final String TAG = "RadioControlRegister";

    private static RadioControlRegister instance;

    public static RadioControlRegister getInstance() {
        if (instance == null) {
            synchronized (RadioControlRegister.class) {
                if (instance == null) {
                    instance = new RadioControlRegister();
                }
            }
        }
        return instance;
    }

    private RadioControlRegister() {
        initialize();
    }

    /**
     * 收音库里面一些工具类的初始化
     */
    private void initialize() {
        RadioConfig.getInstance().initialize(RadioConfig.getInstance().getCurrentRegion());
        RadioMessageSaveUtils.getInstance().initialize(AppBase.mContext);
        RadioListSaveUtils.getInstance().initialize(AppBase.mContext);
        RadioList.getInstance().initialize();
        //焦点工具初始化
        AudioFocusUtils.getInstance().initialize(AppBase.mContext);
    }

    /**
     * 获取收音控制，状态获取的方法的接口的实例
     *
     * @return getControlTool
     */
    public IGetControlTool registeredRadioTool() {
        RadioControlAction.getInstance().initialize();
        return getControlTool;
    }

    /**
     * 收音控制，状态获取的方法的接口的实例
     */
    private IGetControlTool getControlTool = new IGetControlTool() {
        /**
         * 获取收音控制的接口
         * @return RadioControlTool
         */
        @Override
        public IControlTool getControlTool() {
            return RadioControlTool.getInstance();
        }

        /**
         * 获取收音状态获取的接口
         * @return CurrentPlayInfor
         */
        @Override
        public IStatusTool getStatusTool() {
            return CurrentRadioInfo.getInstance();
        }

        /**
         * 注册收音状态变化的回调
         * @param radioStatusChange 回调
         */
        @Override
        public void registerRadioStatusChangeListener(IRadioStatusChange radioStatusChange) {
            CurrentRadioInfo.getInstance().registerRadioStatusChangeListener(radioStatusChange);
        }

        /**
         * 注销收音状态变化的回调
         * @param radioStatusChange 回调
         */
        @Override
        public void unregisterRadioStatusChangerListener(IRadioStatusChange radioStatusChange) {
            CurrentRadioInfo.getInstance().unregisterRadioStatusChangerListener(radioStatusChange);
        }
    };
}
