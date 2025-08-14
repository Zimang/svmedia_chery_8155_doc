package com.desaysv.libradio.interfaze;

import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.dab.DABBerMessage;
import com.desaysv.libradio.bean.dab.DABEPGScheduleList;
import com.desaysv.libradio.bean.dab.DABHeadlineMessage;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LZM
 * @date 2019-7-9
 * Comment 给info类的数据获取的接口，不对外。
 */
public interface IPlayStatusTool {

    /**
     * 获取是否是播放状态
     *
     * @return true 播放; false 暂停
     */
    boolean isPlaying();

    /**
     * 获取当前搜索到的列表信息
     *
     * @param band 获取的频段
     * @return 搜索的列表
     */
    List<RadioMessage> getAstList(int band);

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
     * 获取DABBer信息
     *
     * @return DABBerMessage
     */
    DABBerMessage getDABBerMessage();

    /**
     * 获取预约电台时间列表
     * @return DABEPGScheduleList
     */
    DABEPGScheduleList getEPGList();

    /**
     * 主动获取场强、信号强度信息
     * [0]频点；[1]信号强度；[2]信噪比；[3]多路径干扰；[4]频偏；[5]带宽；[6]调制度
     * @return RadioManager.ProgramInfo
     */
    ArrayList<Integer> getProgramInfo();

    /**
     * 点击EPG弹窗进行播放时，主动获取一下底层播放信息
     *
     * @return RadioMessage 电台的播放信息
     */
    default RadioMessage getCurrentRadioMessageWithEPG(){
        return null;
    };

    default RadioParameter getRadioParameter(){
        return null;
    }
}
