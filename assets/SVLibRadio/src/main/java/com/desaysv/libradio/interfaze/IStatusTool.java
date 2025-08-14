package com.desaysv.libradio.interfaze;

import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.dab.DABBerMessage;
import com.desaysv.libradio.bean.dab.DABEPGScheduleList;
import com.desaysv.libradio.bean.dab.DABHeadlineMessage;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.bean.RadioMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author uidp5370
 * @date 2019-1-28
 * 对外的获取收音状态的接口
 */

public interface IStatusTool {

    /**
     * 获取是否是播放状态
     *
     * @return true：播放 false：暂停
     */
    boolean isPlaying();

    /**
     * 获取AM的电台信息
     *
     * @return RadioMessage AM的电台信息
     */
    RadioMessage getAMRadioMessage();

    /**
     * 获取FM的电台信息
     *
     * @return RadioMessage FM的电台信息
     */
    RadioMessage getFMRadioMessage();

    /**
     * 获取DAB的电台信息
     *
     * @return RadioMessage FM的电台信息
     */
    RadioMessage getDABRadioMessage();

    /**
     * 获取当前电台的播放信息
     *
     * @return RadioMessage 电台的播放信息
     */
    RadioMessage getCurrentRadioMessage();

    /**
     * 获取扫描到的电台列表
     *
     * @param band 需要获取那个频段的电台列表
     * @return List<RadioMessage> 电台列表
     */
    List<RadioMessage> getAstList(int band);

    /**
     * 当前是否是再扫描中
     *
     * @return true：当前在扫描 false：当前不在扫描
     */
    boolean isSearching();

    /**
     * 当前是否在搜台中
     *
     * @return true：当前在搜台中 false：当前不在搜台中
     */
    boolean isSeeking();

    /**
     * 获取当前的电台信息是否是打开状态
     *
     * @return true：打开 false：没有打开
     */
    boolean getTuned();

    /**
     * 获取场强信息
     *
     * @return int
     * 0：mute 1：unmute
     */
    int getRssiValue();

    /**
     * DABAnnSwitch
     *
     * @return DABAnnSwitch
     */
    DABAnnSwitch getDABAnnSwitchStatus();

    /**
     * DABAnnSwitch
     *
     * @return DABAnnSwitch
     */
    RDSSettingsSwitch getRDSSettingsSwitchStatus();

    /**
     * 获取头条信息列表
     *
     * @return List<DABHeadlineMessage>
     */
    List<DABHeadlineMessage> getDABHeadlineMessage();

    /**
     * 获取DAB时间
     *
     * @return DABTime
     */
    DABTime getDABTime();

    /**
     * 获取EPG列表
     *
     * @return DABEPGScheduleList
     */
    DABEPGScheduleList getEPGList();

    /**
     * 获取DABBer信息
     *
     * @return DABBerMessage
     */
    DABBerMessage getDABBerMessage();

    /**
     * FM/AM
     * [0]频点；[1]信号强度；[2]信噪比；[3]多路径干扰；[4]频偏；[5]带宽；[6]调制度
     *
     * @return RadioManager.ProgramInfo
     */
    ArrayList<Integer> getProgramInfo();


    /**
     * 获取切换到DAB前的电台的播放信息
     *
     * @return RadioMessage 电台的播放信息
     */
    RadioMessage getPreRadioMessage();

    /**
     * 获取当前收藏电台的播放信息
     *
     * @return RadioMessage 电台的播放信息
     */
    default RadioMessage getCurrentCollectRadioMessage() {
        return null;
    }

    default RDSFlagInfo getCurrentRDSFlagInfo(){
        return null;
    }

    /**
     * 点击EPG弹窗进行播放时，主动获取一下底层播放信息
     *
     * @return RadioMessage 电台的播放信息
     */
    default RadioMessage getCurrentRadioMessageWithEPG(){
        return null;
    };


    /**
     * 获取DAB/FM融合 播放的电台
     *
     * @return RadioMessage DAB/FM融合的电台信息
     */
    default RadioMessage getDABorFMRadioMessage(){return null;};

    /**
     * 获取底层设置的搜台条件
     * @return
     */
    default RadioParameter getRadioParameter(){
        return null;
    }
}
