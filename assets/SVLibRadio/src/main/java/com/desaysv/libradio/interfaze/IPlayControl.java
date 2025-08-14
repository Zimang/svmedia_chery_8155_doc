package com.desaysv.libradio.interfaze;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;

/**
 * Created by LZM on 2019-7-9.
 * Comment 播放控制器接口
 */
public interface IPlayControl {

    void openRadio(RadioMessage radioMessage);

    void seekForward();

    void seekBackward();

    void stepForward();

    void stepBackward();

    void play();

    void pause();

    void ast();

    void stopAst();

    void setDABAnnStatus(DABAnnSwitch dabAnnSwitch);

    void setRDSSettingsStatus(RDSSettingsSwitch rdsSettingsStatus);

    void specifiesAst(RadioMessage radioMessage);

    /**
     * 进入步进模式
     */
    void startStepMode();

    /**
     * 退出步进模式
     */
    void stopStepMode();

    /**
     * 启动快速向前步进
     */
    void startFastPreStep();


    /**
     * 停止快速向前步进
     */
    void stopFastPreStep();

    /**
     * 启动快速向前步进
     */
    void startFastNextStep();

    /**
     * 停止快速向前步进
     */
    void stopFastNextStep();

    /**
     * 停止步进搜索
     */
    void stopSeek();

    /**
     * 组合搜索，先搜索FM，接着搜索DAB，
     * 适用于欧盟不带AM的项目
     */
    void multiAst();


    /**
     * 组合下一曲，即组合列表的下一曲
     */
    void multiSeekForward();

    /**
     * 组合上一曲，即组合列表的上一曲
     */
    void multiSeekBackward();

    /**
     * 组合向前前进一格，即组合列表的下一曲
     */
    void multiStepForward();

    /**
     * 组合向向后后退一格，即组合列表的下一曲
     */
    void multiStepBackward();

    void openTTSRadio(RadioMessage radioMessage);

    void setQualityCondition(RadioParameter radioParameter);

    void playTTS();
}
