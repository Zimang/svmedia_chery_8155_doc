package com.desaysv.libradio.interfaze;


import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.rds.RDSAnnouncement;
import com.desaysv.libradio.bean.rds.RDSFlagInfo;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;

/**
 * Created by LZM on 2019-6-12.
 * Comment 收音状态变化的回调
 */

public interface IRadioStatusChange {

    /**
     * 当前电台发生变化
     */
    void onCurrentRadioMessageChange();

    /**
     * 当前播放状态法生变化
     *
     * @param isPlaying isPlaying
     */
    void onPlayStatusChange(boolean isPlaying);

    /**
     * 电台列表法生改变
     *
     * @param band band
     */
    void onAstListChanged(int band);

    /**
     * 搜索有效电台回调
     *
     * @param isSearching isSearching
     */
    void onSearchStatusChange(boolean isSearching);

    /**
     * 上下台变化回调
     *
     * @param isSeeking isSeeking
     */
    void onSeekStatusChange(boolean isSeeking);

    /**
     * 通知ANN提示发生变化
     *
     * @param notify DABAnnNotify
     */
    void onAnnNotify(DABAnnNotify notify);

    /**
     * RDS标签变化
     *
     * @param info RDSFlagInfo
     */
    void onRDSFlagInfoChange(RDSFlagInfo info);

    /**
     * RDS公告状态变化
     * @param rdsAnnouncement
     */
    default void onRDSAnnNotify(RDSAnnouncement rdsAnnouncement){

    }

    /**
     * DAB时间变化变化
     * @param dabTime
     */
    default void onDABTimeNotify(DABTime dabTime){

    }

    /**
     * RDS设置变化
     * @param rdsSettingsSwitch
     */
    default void onRDSSettingsStatus(RDSSettingsSwitch rdsSettingsSwitch){

    }

    default void onDABLogoChanged(byte[] logoByte){}

    /**
     * 初始化成功的回调
     */
    default void onInitSuccessCallback(){

    }

    /**
     * DAB信号状态的回调
     */
    default void onDABSignalChanged(int signalValue){

    }

    /**
     * 收银区域变化的回调
     */
    default void onRadioRegionChanged(){

    }
}
