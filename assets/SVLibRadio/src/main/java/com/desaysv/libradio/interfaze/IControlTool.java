package com.desaysv.libradio.interfaze;

import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.mediacommonlib.bean.ChangeReason;

/**
 * Created by uidp5370 on 2019-6-12.
 * 对外的控制逻辑
 */

public interface IControlTool {

    /**
     * 命令的执行模式
     *
     * @param radioAction  控制动作 上一曲，下一曲等，具体看MediaAction
     * @param changeReason 发送控制的原因，具体看ChangeReasonData
     */
    void processCommand(RadioAction radioAction, ChangeReason changeReason);

    /**
     * 命令的执行模式
     *
     * @param radioAction  控制动作 上一曲，下一曲等，具体看MediaAction
     * @param changeReason 发送控制的原因，具体看ChangeReasonData
     * @param data         控制的参数，RadioMessage
     */
    void processCommand(RadioAction radioAction, ChangeReason changeReason, RadioMessage data);

    /**
     * 命令的执行模式
     *
     * @param radioAction  控制动作 上一曲，下一曲等，具体看MediaAction
     * @param changeReason 发送控制的原因，具体看ChangeReasonData
     * @param object            Object     控制的参数,传递任意参数，由具体的业务线约定裁决
     */
    void processCommand(RadioAction radioAction, ChangeReason changeReason, Object object);
}
