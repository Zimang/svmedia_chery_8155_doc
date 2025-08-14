package com.desaysv.libradio.action;

import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioTuner;
import android.media.AudioAttributes;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.audiosdk.utils.AudioFocusUtils;
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil;
import com.desaysv.ivi.extra.project.carconfig.Constants;
import com.desaysv.libradio.bean.CurrentRadioInfo;
import com.desaysv.libradio.bean.JsonNode;
import com.desaysv.libradio.bean.RadioAction;
import com.desaysv.libradio.bean.RadioEvent;
import com.desaysv.libradio.bean.RadioList;
import com.desaysv.libradio.bean.RadioMessage;
import com.desaysv.libradio.bean.RadioParameter;
import com.desaysv.libradio.bean.dab.DABAnnNotify;
import com.desaysv.libradio.bean.dab.DABAnnSwitch;
import com.desaysv.libradio.bean.dab.DABBerMessage;
import com.desaysv.libradio.bean.dab.DABDynamicLableInfo;
import com.desaysv.libradio.bean.dab.DABEPGSchedule;
import com.desaysv.libradio.bean.dab.DABEPGScheduleList;
import com.desaysv.libradio.bean.dab.DABHeadlineMessage;
import com.desaysv.libradio.bean.dab.DABLogo;
import com.desaysv.libradio.bean.dab.DABLogoList;
import com.desaysv.libradio.bean.dab.DABMessage;
import com.desaysv.libradio.bean.dab.DABMessageList;
import com.desaysv.libradio.bean.dab.DABStatus;
import com.desaysv.libradio.bean.dab.DABTime;
import com.desaysv.libradio.bean.dab.convert.DABEPGScheduleConvert;
import com.desaysv.libradio.bean.dab.convert.DABEPGScheduleListConvert;
import com.desaysv.libradio.bean.dab.convert.DABMessageConvert;
import com.desaysv.libradio.bean.dab.convert.DABMessageListConvert;
import com.desaysv.libradio.bean.rds.RDSRadioName;
import com.desaysv.libradio.bean.rds.RDSSettingsSwitch;
import com.desaysv.libradio.control.RadioControlTool;
import com.desaysv.libradio.interfaze.IPlayControl;
import com.desaysv.libradio.interfaze.IPlayStatusTool;
import com.desaysv.libradio.utils.ASCIITools;
import com.desaysv.libradio.utils.DABListSortUtils;
import com.desaysv.libradio.utils.JsonUtils;
import com.desaysv.libradio.utils.PinyinComparator;
import com.desaysv.libradio.utils.RadioConversionUtils;
import com.desaysv.libradio.utils.RadioEventValueUtils;
import com.desaysv.mediacommonlib.base.AppBase;
import com.desaysv.mediacommonlib.bean.ChangeReasonData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author uidq1846
 * @desc DAB的控制器
 * @time 2022-9-2 10:32
 */
class DABControlAction implements IPlayControl, IPlayStatusTool {
    private static final String TAG = "DABControlAction";
    private RadioTuner tuner;
    private static DABControlAction instance;

    private boolean hasMulti = false;

    private boolean isStopAst;

    /**
     * 获取单例
     *
     * @return DABControlAction
     */
    public static DABControlAction getInstance() {
        if (instance == null) {
            synchronized (DABControlAction.class) {
                if (instance == null) {
                    instance = new DABControlAction();
                }
            }
        }
        return instance;
    }

    private DABControlAction() {
        hasMulti = CarConfigUtil.getDefault().getConfig(Constants.ID_CONFIG_DAB) == 1;
    }

    public void setTuner(RadioTuner radioTuner) {
        Log.d(TAG, "setTuner: radioTuner = " + radioTuner);
        this.tuner = radioTuner;
        //初始化的时候清空logo
        if (CurrentRadioInfo.getInstance().getCurrentRadioMessage().getDabMessage() != null) {
            CurrentRadioInfo.getInstance().getCurrentRadioMessage().getDabMessage().setLogoDataList(null);
        }
    }

    @Override
    public void openRadio(RadioMessage radioMessage) {
        if (null == tuner) {
            Log.d(TAG, "openRadio: tuner is null");
            return;
        }
        //先设置切换到DAB的频段、然后再打开频点
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SET_DAB_BAND));
        //打开频点
        DABMessage dabMessage = radioMessage.getDabMessage();
        //置须三项即可、避免数据过大（毕竟包含了专辑图的信息）
        DABMessage dabOpenMessage = new DABMessage(dabMessage.getFrequency(), dabMessage.getServiceId(), dabMessage.getServiceComponentId());
        String json = JsonUtils.generateJson(dabOpenMessage);
        Log.d(TAG, "openRadio: json = " + json);
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SET_DAB_STATION, json));
        if (!(dabOpenMessage.getFrequency() == CurrentRadioInfo.getInstance().getDABRadioMessage().getDabMessage().getFrequency()
                && dabOpenMessage.getServiceId() == CurrentRadioInfo.getInstance().getDABRadioMessage().getDabMessage().getServiceId()
                && dabOpenMessage.getServiceComponentId() == CurrentRadioInfo.getInstance().getDABRadioMessage().getDabMessage().getServiceComponentId())){
            dabMessage.setSlsLen(0);
            dabMessage.setSlsDataList(new byte[0]);
            CurrentRadioInfo.getInstance().clearLogo();
        }
        //手动增加这个处理，避免DAB没有回调，导致不更新信息，这样会导致preRadioMessage的保存不生效，重启时会初始化Tuner失败
        CurrentRadioInfo.getInstance().setCurrentRadioMessage(radioMessage);
    }

    @Override
    public void seekForward() {
        if (null == tuner) {
            Log.d(TAG, "seekForward: tuner is null");
            return;
        }
        Log.d(TAG, "seekForward: ");
        next();
        //tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SEEK_UP));
    }

    @Override
    public void seekBackward() {
        if (null == tuner) {
            Log.d(TAG, "seekBackward: tuner is null");
            return;
        }
        Log.d(TAG, "seekBackward: ");
//        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SEEK_DOWN));
        pre();
    }

    @Override
    public void stepForward() {
        next();
    }

    @Override
    public void stepBackward() {
        pre();
    }

    @Override
    public void play() {
        if (null == tuner) {
            Log.d(TAG, "play: tuner is null");
            return;
        }
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_MUTE_UNMUTE, JsonUtils.generateSingleNodeJson(JsonNode.MUTE_UNMUTE_NODE, RadioEvent.MuteStatus.UNMUTE)));
        CurrentRadioInfo.getInstance().setPlaying(true);
    }

    @Override
    public void pause() {
        if (null == tuner) {
            Log.d(TAG, "pause: tuner is null");
            return;
        }
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_MUTE_UNMUTE, JsonUtils.generateSingleNodeJson(JsonNode.MUTE_UNMUTE_NODE, RadioEvent.MuteStatus.MUTE)));
        CurrentRadioInfo.getInstance().setPlaying(false);
    }

    @Override
    public void ast() {
        if (null == tuner) {
            Log.d(TAG, "ast: tuner is null");
            return;
        }
        Log.d(TAG, "ast");
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_MUTE_UNMUTE, JsonUtils.generateSingleNodeJson(JsonNode.MUTE_UNMUTE_NODE, RadioEvent.MuteStatus.MUTE)));
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SCAN));
    }

    @Override
    public void stopAst() {
        if (null == tuner) {
            Log.d(TAG, "stopAst: tuner is null");
            return;
        }
        Log.d(TAG, "stopAst");
        isStopAst = true;
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_STOP_SEEK_SCAN));
    }

    @Override
    public boolean isPlaying() {
        if (null == tuner) {
            Log.d(TAG, "isPlaying: tuner is null");
            return false;
        }
        String json = getRadioJson(tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_MUTE_STATUS)));
        Log.d(TAG, "isPlaying: json = " + json);
        if (!TextUtils.isEmpty(json)) {
            DABStatus dabStatus = JsonUtils.generateObject(json, DABStatus.class);
            int mute = dabStatus.getMute();
            Log.d(TAG, "isPlaying: mute = " + mute);
            return mute == RadioEvent.MuteStatus.UNMUTE;
        }
        return false;
    }

    @Override
    public List<RadioMessage> getAstList(int band) {
        if (null == tuner) {
            Log.d(TAG, "getAstList: tuner is null");
            return null;
        }
        RadioManager.RadioEventValue radioEventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_STATION_LIST_INFO));
        String listString = getRadioJson(radioEventValue);
        Log.d(TAG, "getAstList: listString = " + listString);
        if (!TextUtils.isEmpty(listString)) {
            List<DABMessageConvert> list = JsonUtils.generateObject(listString, DABMessageListConvert.class).getDabMessageList();
            if (list == null){
                CurrentRadioInfo.getInstance().clearLogo();
                return null;
            }
            List<RadioMessage> radioMessages = new ArrayList<>();
            for (DABMessageConvert dabMessageConvert : list) {
                DABMessage dabMessage = new DABMessage(dabMessageConvert.getFrequency(),dabMessageConvert.getServiceId(),dabMessageConvert.getServiceComponentId());
                dabMessage.setEnsembleId(dabMessageConvert.getEnsembleId());
                dabMessage.setProgramType(dabMessageConvert.getProgramType());
                dabMessage.setDynamicLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getDynamicLabel())).toString());
                dabMessage.setDynamicPlusLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getDynamicPlusLabel())).toString());
                dabMessage.setSubServiceFlag(dabMessageConvert.getSubServiceFlag());

                dabMessage.setEnsembleLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getEnsembleLabel())).toString());
                dabMessage.setProgramStationName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getProgramStationName())).toString());
                dabMessage.setShortEnsembleLabel(RadioConversionUtils.getShortName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getEnsembleLabel())).toString(), dabMessageConvert.getEnsembleLabelFlag()));
                dabMessage.setShortProgramStationName(RadioConversionUtils.getShortName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getProgramStationName())).toString(), dabMessageConvert.getProStaNameFlag()));

                radioMessages.add(new RadioMessage(dabMessage));
            }
            return radioMessages;
        }
        CurrentRadioInfo.getInstance().clearLogo();
        return new ArrayList<>();
    }

    @Override
    public int getRssiValue() {
        if (null == tuner) {
            Log.d(TAG, "getRssiValue: tuner is null");
            return 0;
        }
        RadioManager.RadioEventValue radioEventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_STATION_RSSI));
        String radioJson = getRadioJson(radioEventValue);
        DABStatus dabStatus = JsonUtils.generateObject(radioJson,DABStatus.class);
        int rssiInt = dabStatus.getRssi();
        Log.d(TAG, "getRssiValue: rssiInt = " + rssiInt);
        return rssiInt;
    }

    @Override
    public void setDABAnnStatus(DABAnnSwitch dabAnnSwitch) {
        if (null == tuner) {
            Log.d(TAG, "setDABAnnStatus: tuner is null");
            return;
        }
        Log.d(TAG, "setDABAnnStatus: dabAnnSwitch = " + dabAnnSwitch);
        //配置Ann状态
        DABAnnSwitch dabAnnSwitchStatus = getDABAnnSwitchStatus();
        if (dabAnnSwitch.getAlarm() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setAlarm(dabAnnSwitch.getAlarm());
        }
        if (dabAnnSwitch.getAreaWeatherFlash() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setAreaWeatherFlash(dabAnnSwitch.getAreaWeatherFlash());
        }
        if (dabAnnSwitch.getEventAnnouncement() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setEventAnnouncement(dabAnnSwitch.getEventAnnouncement());
        }
        if (dabAnnSwitch.getFinancialReport() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setFinancialReport(dabAnnSwitch.getFinancialReport());
        }
        if (dabAnnSwitch.getNewsFlash() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setNewsFlash(dabAnnSwitch.getNewsFlash());
        }
        if (dabAnnSwitch.getProgramInformation() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setProgramInformation(dabAnnSwitch.getProgramInformation());
        }
        if (dabAnnSwitch.getServiceFollow() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setServiceFollow(dabAnnSwitch.getServiceFollow());
        }
        if (dabAnnSwitch.getSpecialEvent() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setSpecialEvent(dabAnnSwitch.getSpecialEvent());
        }
        if (dabAnnSwitch.getRoadTrafficFlash() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setRoadTrafficFlash(dabAnnSwitch.getRoadTrafficFlash());
        }
        if (dabAnnSwitch.getSportReport() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setSportReport(dabAnnSwitch.getSportReport());
        }
        if (dabAnnSwitch.getTransportFlash() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setTransportFlash(dabAnnSwitch.getTransportFlash());
        }
        if (dabAnnSwitch.getWarning() != RadioEvent.SwitchStatus.SWITCH_INVALID) {
            dabAnnSwitchStatus.setWarning(dabAnnSwitch.getWarning());
        }
        Log.d(TAG, "setDABAnnStatus: dabAnnSwitchStatus = " + dabAnnSwitchStatus);
        tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_ANNOUNCEMENT_SELECTED_STATE, JsonUtils.generateJson(dabAnnSwitchStatus)));
    }

    @Override
    public void setRDSSettingsStatus(RDSSettingsSwitch rdsSettingsStatus) {
        //RDS，无需DAB执行
    }

    @Override
    public void specifiesAst(RadioMessage radioMessage) {
        // FM\AM搜索功能
    }

    @Override
    public RDSSettingsSwitch getRDSSettingsSwitchStatus() {
        //RDS，无需DAB执行
        return null;
    }

    @Override
    public List<DABHeadlineMessage> getDABHeadlineMessage() {
        // TODO: 2022-10-11 这个还未确定具体形态，先预留如此
        return null;
    }

    @Override
    public DABTime getDABTime() {
        if (null == tuner) {
            Log.d(TAG, "getDABTime: tuner is null");
            return null;
        }
        RadioManager.RadioEventValue radioEventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_DATA_TIME_INFO));
        String radioJson = getRadioJson(radioEventValue);
        Log.d(TAG, "getDABTime: radioJson = " + radioJson);
        return JsonUtils.generateObject(radioJson, DABTime.class);
    }

    @Override
    public DABBerMessage getDABBerMessage() {
        if (null == tuner) {
            Log.d(TAG, "getDABBerMessage: tuner is null");
            return null;
        }
        RadioManager.RadioEventValue radioEventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_STATION_BER));
        String radioJson = getRadioJson(radioEventValue);
        Log.d(TAG, "getDABBerMessage: radioJson = " + radioJson);
        return JsonUtils.generateObject(radioJson, DABBerMessage.class);
    }

    @Override
    public DABEPGScheduleList getEPGList() {
        if (null == tuner) {
            Log.d(TAG, "getEPGList: tuner is null");
            return null;
        }
        RadioManager.RadioEventValue radioEventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_EPG_INFO));
        String radioJson = getRadioJson(radioEventValue);
        Log.d(TAG, "getEPGList: radioJson = " + radioJson);

        DABEPGScheduleListConvert dabepgScheduleListConvert = JsonUtils.generateObject(radioJson, DABEPGScheduleListConvert.class);

        DABEPGScheduleList dabepgScheduleList = new DABEPGScheduleList();
        dabepgScheduleList.setServiceId(dabepgScheduleListConvert.getServiceId());

        RadioMessage dabMessage = CurrentRadioInfo.getInstance().getDABRadioMessage();
        DABMessage dabMessage1 = dabMessage.getDabMessage();
        Log.d(TAG, "getEPGList: dabMessage1 = " + dabMessage1);

        List<DABEPGSchedule> listDTOS = new ArrayList<>();
        for (DABEPGScheduleConvert tempEpgScheduleListDTO : dabepgScheduleListConvert.getEpgScheduleList()){
            DABEPGSchedule epgScheduleListDTO = new DABEPGSchedule();
            epgScheduleListDTO.setProgramName(Html.fromHtml(ASCIITools.hex2Html(tempEpgScheduleListDTO.getProgramName())).toString());
            epgScheduleListDTO.setYear(tempEpgScheduleListDTO.getYear());
            epgScheduleListDTO.setMonth(tempEpgScheduleListDTO.getMonth());
            epgScheduleListDTO.setDay(tempEpgScheduleListDTO.getDay());
            epgScheduleListDTO.setHour(tempEpgScheduleListDTO.getHour());
            epgScheduleListDTO.setMin(tempEpgScheduleListDTO.getMin());
            epgScheduleListDTO.setSec(tempEpgScheduleListDTO.getSec());
            epgScheduleListDTO.setServiceId(dabMessage1.getServiceId());
            epgScheduleListDTO.setFreq(dabMessage1.getFrequency());
            epgScheduleListDTO.setServiceComponentId(dabMessage1.getServiceComponentId());
            listDTOS.add(epgScheduleListDTO);
        }
        dabepgScheduleList.setEpgScheduleList(listDTOS);

        return dabepgScheduleList;
    }

    @Override
    public ArrayList<Integer> getProgramInfo() {
        //无需DAB执行
        return null;
    }

    @Override
    public RadioMessage getCurrentRadioMessageWithEPG() {
        return IPlayStatusTool.super.getCurrentRadioMessageWithEPG();
    }

    @Override
    public RadioParameter getRadioParameter() {
        return IPlayStatusTool.super.getRadioParameter();
    }


    @Override
    public void startStepMode() {

    }

    @Override
    public void stopStepMode() {

    }

    @Override
    public void startFastPreStep() {

    }

    @Override
    public void stopFastPreStep() {

    }

    @Override
    public void startFastNextStep() {

    }

    @Override
    public void stopFastNextStep() {

    }

    /**
     * 用于首次更新电台
     * 先写，暂时无需要使用
     *
     * @return DABMessage
     */
    public DABMessage getCurrentDAB() {
        //对应的EventID是EVT_DAB_CURRENT_STATION_INFO，EPG弹窗打开时，主动获取一次
        RadioManager.RadioEventValue eventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_CURRENT_STATION_INFO));
        DABMessageConvert dabMessageConvert = JsonUtils.generateObject(getRadioJson(eventValue), DABMessageConvert.class);
        if (dabMessageConvert == null){
            return null;
        }
        DABMessage dabMessage = new DABMessage(dabMessageConvert.getFrequency(),dabMessageConvert.getServiceId(),dabMessageConvert.getServiceComponentId());
        dabMessage.setEnsembleId(dabMessageConvert.getEnsembleId());
        dabMessage.setProgramType(dabMessageConvert.getProgramType());
        dabMessage.setDynamicLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getDynamicLabel())).toString());
        dabMessage.setDynamicPlusLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getDynamicPlusLabel())).toString());
        dabMessage.setSubServiceFlag(dabMessageConvert.getSubServiceFlag());
        dabMessage.setEnsembleLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getEnsembleLabel())).toString());
        dabMessage.setProgramStationName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getProgramStationName())).toString());
        dabMessage.setShortEnsembleLabel(RadioConversionUtils.getShortName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getEnsembleLabel())).toString(), dabMessageConvert.getEnsembleLabelFlag()));
        dabMessage.setShortProgramStationName(RadioConversionUtils.getShortName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getProgramStationName())).toString(), dabMessageConvert.getProStaNameFlag()));

        return dabMessage;
    }

    /**
     * DABAnnSwitch
     * 获取ANN选项状态
     *
     * @return DABAnnSwitch
     */
    public DABAnnSwitch getDABAnnSwitchStatus() {
        if (null == tuner) {
            Log.d(TAG, "getDABAnnSwitchStatus: tuner is null");
            return null;
        }
        RadioManager.RadioEventValue eventValue = tuner.get(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_ANNOUNCEMENT_SELECTED_STATE_INFO));
        DABAnnSwitch dabAnnSwitch = JsonUtils.generateObject(getRadioJson(eventValue), DABAnnSwitch.class);
        Log.d(TAG, "getDABAnnSwitchStatus: dabAnnSwitch = " + dabAnnSwitch);
        return dabAnnSwitch;
    }

    /**
     * 处理数字电台相关回调
     *
     * @param radioEventValue radioEventValue
     */
    public void onEventNotify(RadioManager.RadioEventValue radioEventValue) {
        int eventID = radioEventValue.getEventID();
        String radioJson = getRadioJson(radioEventValue);
        Log.d(TAG, "onEventNotify: eventID = " + eventID + " radioJson = " + radioJson);
        //处理DAB的信息状态
        switch (eventID) {
            case RadioEvent.EventID.EVT_DAB_CURRENT_STATION_INFO:
                //更新当前电台信息
                DABMessageConvert dabMessageConvert = JsonUtils.generateObject(radioJson, DABMessageConvert.class);

                DABMessage dabMessage = new DABMessage(dabMessageConvert.getFrequency(),dabMessageConvert.getServiceId(),dabMessageConvert.getServiceComponentId());
                dabMessage.setEnsembleId(dabMessageConvert.getEnsembleId());
                dabMessage.setProgramType(dabMessageConvert.getProgramType());
                //dabMessage.setDynamicLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getDynamicLabel())).toString());
                //dabMessage.setDynamicPlusLabel(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getDynamicPlusLabel())).toString());
                //dabMessage.setSubServiceFlag(dabMessageConvert.getSubServiceFlag());

                //link的情况下，底层上报过来的可能是空内容，此时更新会导致界面显示空白
                //因此加个补丁处理

                String ensembleLabel = Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getEnsembleLabel())).toString();
                if (ensembleLabel != null && ensembleLabel.trim().length() > 0) {
                    dabMessage.setEnsembleLabel(ensembleLabel);
                    dabMessage.setShortEnsembleLabel(RadioConversionUtils.getShortName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getEnsembleLabel())).toString(), dabMessageConvert.getEnsembleLabelFlag()));
                }
                String stationName = Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getProgramStationName())).toString();
                if (stationName != null && stationName.trim().length() > 0) {
                    dabMessage.setProgramStationName(stationName);
                    dabMessage.setShortProgramStationName(RadioConversionUtils.getShortName(Html.fromHtml(ASCIITools.hex2Html(dabMessageConvert.getProgramStationName())).toString(), dabMessageConvert.getProStaNameFlag()));
                }

                if (CurrentRadioInfo.getInstance().getCurrentRadioMessage().getRadioType() == RadioMessage.DAB_TYPE) {
                    RadioMessage dabRadioMessage = CurrentRadioInfo.getInstance().getDABRadioMessage();
//                    if (!(dabMessage.getFrequency() == dabRadioMessage.getDabMessage().getFrequency()
//                            && dabMessage.getServiceId() == dabRadioMessage.getDabMessage().getServiceId()
//                            && dabMessage.getServiceComponentId() == dabRadioMessage.getDabMessage().getServiceComponentId())){
//                        dabRadioMessage.getDabMessage().setSlsLen(0);
//                        dabRadioMessage.getDabMessage().setSlsDataList(new byte[0]);
//                        CurrentRadioInfo.getInstance().clearLogo();
//                    }
                    dabRadioMessage.setDabMessage(dabMessage);
                    CurrentRadioInfo.getInstance().setCurrentRadioMessage(dabRadioMessage);
                }
                break;
            case RadioEvent.EventID.EVT_DAB_SEEK_SCACN_STATUS:
                //更新扫描状态
                DABStatus status = JsonUtils.generateObject(radioJson, DABStatus.class);
                int scan = status.getSeek_scan_status();
                Log.d(TAG, "onEventNotify: scan = " + scan);

                if (CurrentRadioInfo.getInstance().isSearching() || isMultiAst || isStopAst) {
                    int sourceId = AudioFocusUtils.getInstance().getCurrentMusicAudioSourceId();
                    String currentCarAudioType = AudioFocusUtils.getInstance().getCurrentAudioSourceName(AppBase.mContext);
                    Log.d(TAG, "onEventNotify DAB_SEEK_SCACN End: sourceId = " + sourceId + ",currentCarAudioType: "+currentCarAudioType);
                    if (((CurrentRadioInfo.getInstance().getCurrentRadioMessage().getDabMessage() == null
                            || (AudioAttributes.SOURCE_DAB != sourceId)) && !hasMulti)
                            || ("mute_media".equals(currentCarAudioType) || "mute_hardkey".equals(currentCarAudioType))){//时序问题，会出现切到FM/AM时 会回调DAB事件的情况
                        isStopAst = false;
                        Log.d(TAG, "onEventNotify DAB_SEEK_SCACN End, not dab ,setSearching");
                        CurrentRadioInfo.getInstance().setSearching(scan == RadioEvent.ScanStatus.RAW_RADIO_SCANING);
                        return;
                    }
                    if (hasMulti){
                        Log.d(TAG,"isMultiAst:"+isMultiAst);
                        if (isMultiAst){
                            if (RadioList.getInstance().getMultiRadioMessageList().size() > 0){
                                List<RadioMessage> SortMultiRadioMessageList  = sortWithName(RadioList.getInstance().getMultiRadioMessageList());
                                if (ChangeReasonData.AIDL != RadioControlTool.getInstance().getAstReason()) {
                                    RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AST_CHANGE_PLAY, isStopAst ? CurrentRadioInfo.getInstance().getDABorFMRadioMessage() : SortMultiRadioMessageList.get(0));
                                }
                            }else {
                                if (ChangeReasonData.AIDL != RadioControlTool.getInstance().getAstReason()) {
                                    RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AST_CHANGE_PLAY, CurrentRadioInfo.getInstance().getDABorFMRadioMessage());
                                }
                            }
                            CurrentRadioInfo.getInstance().setSearching(scan == RadioEvent.ScanStatus.RAW_RADIO_SCANING);
                            isMultiAst = false;
                        }else {

                        }

                    }else {
                        if (RadioList.getInstance().getDABEffectRadioMessageList().size() > 0){
                            if (ChangeReasonData.AIDL != RadioControlTool.getInstance().getAstReason()) {
                                RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.AST_CHANGE_PLAY, RadioList.getInstance().getDABEffectRadioMessageList().get(0));
                            }
                        }else {
                            if (ChangeReasonData.AIDL != RadioControlTool.getInstance().getAstReason()) {
                                RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.AST_CHANGE_PLAY);
                            }
                        }
                        CurrentRadioInfo.getInstance().setSearching(scan == RadioEvent.ScanStatus.RAW_RADIO_SCANING);
                    }
                }
                isStopAst = false;
                break;
            case RadioEvent.EventID.EVT_DAB_STATION_LIST_INFO:
                //更新列表信息
                RadioList.getInstance().updateEffectRadioMessageList(RadioMessage.DAB_BAND, getAstList(0));
                CurrentRadioInfo.getInstance().onAstListChanged(RadioMessage.DAB_BAND);
                //检查列表更新后，对应的当前播放信息是否需要更新
                RadioMessage current = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
                Log.d(TAG,"updatelist,currnt is"+current);
                if (current.getRadioType() == RadioMessage.DAB_TYPE){
                    DABMessage currentDAB = current.getDabMessage();
                    for (RadioMessage radioMessage : RadioList.getInstance().getDABEffectRadioMessageList()){
                        if ((radioMessage.getDabMessage().getFrequency() == currentDAB.getFrequency()
                                && radioMessage.getDabMessage().getServiceId() == currentDAB.getServiceId()
                                && radioMessage.getDabMessage().getServiceComponentId() == currentDAB.getServiceComponentId())){
                            Log.d(TAG,"current list contain the current dab, check need update");
                            current.getDabMessage().setProgramType(radioMessage.getDabMessage().getProgramType());
                            current.getDabMessage().setProgramStationName(radioMessage.getDabMessage().getProgramStationName());
                            current.getDabMessage().setShortProgramStationName(radioMessage.getDabMessage().getShortProgramStationName());
                            CurrentRadioInfo.getInstance().setCurrentRadioMessage(current);
                        }
                    }
                }
                break;
            case RadioEvent.EventID.EVT_DAB_ANNOUNCEMENT_NOTIFY:
                DABAnnNotify dabAnnNotify = JsonUtils.generateObject(radioJson, DABAnnNotify.class);
                CurrentRadioInfo.getInstance().setAnnNotifyStatus(dabAnnNotify);
                break;
            case RadioEvent.EventID.EVT_DAB_MUTE_STATUS:
                //播放状态、应该不用重新设置，这里只是打印下状态
                DABStatus dabStatus = JsonUtils.generateObject(radioJson, DABStatus.class);
                int mute = dabStatus.getMute();
                Log.d(TAG, "onEventNotify: mute = " + mute);
                break;
            case RadioEvent.EventID.EVT_DAB_DYNAMIC_LABEL_INFO:
                //DAB的详细信息
                DABDynamicLableInfo labelMessage = JsonUtils.generateObject(radioJson, DABDynamicLableInfo.class);
                RadioMessage currentRadioMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
                DABMessage message = currentRadioMessage.getDabMessage();
                if (message == null){//时序问题，会出现切到FM/AM时 会回调DAB事件的情况
                    return;
                }
                //底层传递的是int [],需要转换成String
                List<String> list = new ArrayList<>();
                message.setDynamicLabel(Html.fromHtml(ASCIITools.hex2Html(labelMessage.getDynamicLabel())).toString());
                for (DABDynamicLableInfo.DynamicPlusLabel label : labelMessage.getDynamicPlusLabelList()) {
                    String string = Html.fromHtml(ASCIITools.hex2Html(label.getLabel())).toString();
                    list.add(string);
                }
                message.setDynamicPlusLabel(JsonUtils.generateJson(list));
                currentRadioMessage.setDabMessage(message);
                CurrentRadioInfo.getInstance().setCurrentRadioMessage(currentRadioMessage);
                break;
            case RadioEvent.EventID.EVT_DAB_SLIDER_SHOW_INFO:
                //专辑图信息
                RadioMessage radioMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
                DABMessage currentMessage = radioMessage.getDabMessage();
                if (currentMessage == null){//时序问题，会出现切到FM/AM时 会回调DAB事件的情况
                    return;
                }
                DABMessage dabMessageSlider = JsonUtils.generateObject(radioJson, DABMessage.class);
                //保存SLIDER_SHOW到dabSliderShowList内存列表
                dabMessageSlider.setFrequency(currentMessage.getFrequency());
                dabMessageSlider.setServiceId(currentMessage.getServiceId());
                dabMessageSlider.setServiceComponentId(currentMessage.getServiceComponentId());
                RadioList.getInstance().updateDABSliderShow(dabMessageSlider);
                currentMessage.setSlsLen(dabMessageSlider.getSlsLen());
                currentMessage.setSlsDataList(dabMessageSlider.getSlsDataList());
                radioMessage.setDabMessage(currentMessage);
                CurrentRadioInfo.getInstance().setCurrentRadioMessage(radioMessage);
                CurrentRadioInfo.getInstance().setCurrentDABLogo(currentMessage.getSlsLen(),currentMessage.getSlsDataList());
                break;
            case RadioEvent.EventID.EVT_DAB_STATION_LOGO_INFO:
                //Logo图片信息
                DABMessage dabMessageLogo = JsonUtils.generateObject(radioJson, DABMessage.class);
                RadioMessage radioMessageLogo = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
                DABMessage currentLogoMessage = radioMessageLogo.getDabMessage();
                if (currentLogoMessage == null){//时序问题，会出现切到FM/AM时 会回调DAB事件的情况
                    return;
                }
                currentLogoMessage.setLogoLen(dabMessageLogo.getLogoLen());
                currentLogoMessage.setLogoDataList(dabMessageLogo.getLogoDataList());
                radioMessageLogo.setDabMessage(currentLogoMessage);
                CurrentRadioInfo.getInstance().setCurrentRadioMessage(radioMessageLogo);
                CurrentRadioInfo.getInstance().setCurrentDABLogo(dabMessageLogo.getLogoLen(),dabMessageLogo.getLogoDataList());
                break;
            case RadioEvent.EventID.EVT_DAB_PLAY_STATUS:
                //通知解mute
                DABStatus dabStatuss = JsonUtils.generateObject(radioJson, DABStatus.class);
                int play = dabStatuss.getPlay();
                Log.d(TAG, "onEventNotify: play = " + play);
                if (play == RadioEvent.PreparePlay.PLAY  && !CurrentRadioInfo.getInstance().isSearching()) {
                    int sourceId = AudioFocusUtils.getInstance().getCurrentSourceId();
                    String currentCarAudioType = AudioFocusUtils.getInstance().getCurrentAudioSourceName(AppBase.mContext);
                    Log.d(TAG, "onEventNotify: sourceId = " + sourceId + ",currentCarAudioType: "+currentCarAudioType);

                    if (AudioAttributes.SOURCE_DAB_TTS == sourceId){
                        RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.RADIO_TTS);
                    }
                    if (CurrentRadioInfo.getInstance().getCurrentRadioMessage().getDabMessage() == null
                            || (AudioAttributes.SOURCE_DAB != sourceId && ChangeReasonData.VR_CONTROL != RadioControlTool.getInstance().getOpenReason())
                            || ("mute_media".equals(currentCarAudioType) || "mute_hardkey".equals(currentCarAudioType))){//时序问题，会出现切到FM/AM时 会回调DAB事件的情况
                        return;
                    }
                    if (ChangeReasonData.TTS_RESUME_PAUSE == RadioControlTool.getInstance().getOpenReason() || ChangeReasonData.AIDL == RadioControlTool.getInstance().getAstReason()){
                        return;
                    }
                    RadioControlTool.getInstance().processCommand(RadioAction.PLAY, ChangeReasonData.AST_CHANGE_PLAY);
                }
                break;
            case RadioEvent.EventID.EVT_DAB_DATA_TIME_INFO:
                //底层主动通知DAB时间
                DABTime dabTime = JsonUtils.generateObject(radioJson, DABTime.class);
                Log.d(TAG, "onDABTimeChange: dabTime = " + dabTime);
                CurrentRadioInfo.getInstance().onDABTimeChange(dabTime);
                break;
            case RadioEvent.EventID.EVT_DAB_SIGNAL_STATUS:
                //底层主动通知DAB信号状态
                DABStatus dabSignal = JsonUtils.generateObject(radioJson, DABStatus.class);
                int signalFlag = dabSignal.getSignalStatus();
                Log.d(TAG, "onEventNotify: EVT_DAB_SIGNAL_STATUS = " + signalFlag);
                CurrentRadioInfo.getInstance().onDABSignalChanged(signalFlag);
                break;
            case RadioEvent.EventID.EVT_DAB_LOGO_LIST:
                String dablogoJson = getRadioJson(radioEventValue);
                Log.d(TAG, "onEventNotify: EVT_DAB_LOGO_LIST,dablogoJson："+dablogoJson);
                if (!TextUtils.isEmpty(dablogoJson)) {
                    List<DABLogo> dabLogoList = JsonUtils.generateObject(dablogoJson, DABLogoList.class).getDabLogoList();
                    RadioList.getInstance().updDABLogoList(dabLogoList);
                }
                break;
        }
    }

    /**
     * 通过RadioEventValue提取Json数据方法块
     *
     * @param radioEventValue RadioManager.RadioEventValue
     * @return String
     */
    private String getRadioJson(RadioManager.RadioEventValue radioEventValue) {
        if (radioEventValue == null) {
            Log.d(TAG, "getRadioJson: radioEventValue is null !");
            return null;
        }
        RadioManager.RawValue rawValue = radioEventValue.getRawValue();
        if (rawValue == null) {
            Log.d(TAG, "getRadioJson: rawValue is null !");
            return null;
        }
        return rawValue.getStringValue();
    }



    /**
     * 上一个
     */
    private void pre() {
        int position = getCurrentRadioDabMessagePosition();
        List<RadioMessage> dabMessages = RadioList.getInstance().getDABEffectRadioMessageList();
        if (dabMessages.isEmpty()) {
            Log.d(TAG, "pre: getEffectRadioMessageList isEmpty");
            return;
        }
        if (position == -1) {
            position = 0;
        } else {
            position = position - 1;
        }
        if (position < 0) {
            position = dabMessages.size() - 1;//测试弄错了，要做循环
        }
        Log.d(TAG, "pre: position = " + position);
        RadioMessage dabMessageData = dabMessages.get(position);
        if (null == dabMessageData) {
            Log.d(TAG, "pre: ");
            return;
        }
        Log.d(TAG, "pre: dabMessageData = " + dabMessageData);
        RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK,dabMessageData);
    }


    /**
     * 下一个
     */
    private void next() {
        int position = getCurrentRadioDabMessagePosition();
        List<RadioMessage> dabMessages = RadioList.getInstance().getDABEffectRadioMessageList();
        if (dabMessages.isEmpty()) {
            Log.d(TAG, "next: getEffectRadioMessageList isEmpty");
            return;
        }
        if (position == -1) {
            position = 0;
        } else {
            position = position + 1;
        }
        if (position > dabMessages.size() - 1) {
            position = 0;//测试弄错了，要做循环
        }
        Log.d(TAG, "next: position = " + position);
        RadioMessage dabMessageData = dabMessages.get(position);
        if (null == dabMessageData) {
            Log.d(TAG, "next: return dabMessageData is null ");
            return;
        }
        Log.d(TAG, "next: dabMessageData = " + dabMessageData);
        RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK,dabMessageData);
    }

    /**
     * 切到上一个集合类型进行播放
     */
    private void preEnsemble(){
        if (DABListSortUtils.isSortWithEnsemble()) {
            RadioMessage preData = DABListSortUtils.getPreEnsembleFirstData(CurrentRadioInfo.getInstance().getCurrentRadioMessage());
            Log.d(TAG, "preEnsemble: " + preData);
            if (preData != null) {
                RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK,preData);
            }
        }
    }


    /**
     * 切到下一个集合类型进行播放
     */
    private void nextEnsemble(){
        if (DABListSortUtils.isSortWithEnsemble()) {
            RadioMessage nextData = DABListSortUtils.getNextEnsembleFirstData(CurrentRadioInfo.getInstance().getCurrentRadioMessage());
            Log.d(TAG, "nextEnsemble: " + nextData);
            if (nextData != null) {
                RadioControlTool.getInstance().processCommand(RadioAction.OPEN_RADIO, ChangeReasonData.CLICK,nextData);
            }
        }
    }


    /**
     * 获取当前电台，在有效电台的位置
     *
     * @return position
     */
    private int getCurrentRadioDabMessagePosition() {
        int position = -1;
        RadioMessage currentMessage = CurrentRadioInfo.getInstance().getCurrentRadioMessage();
        Log.d(TAG, "getCurrentRadioDabMessagePosition: currentMessage = " + currentMessage);
        List<RadioMessage> dabMessages = RadioList.getInstance().getDABEffectRadioMessageList();
        for (int i = 0; i < dabMessages.size(); i++) {
            RadioMessage radioDabMessageData = dabMessages.get(i);
            if (currentMessage.getDabMessage().getFrequency() == radioDabMessageData.getDabMessage().getFrequency() && currentMessage.getDabMessage().getServiceId() == radioDabMessageData.getDabMessage().getServiceId()
                && currentMessage.getDabMessage().getServiceComponentId() == radioDabMessageData.getDabMessage().getServiceComponentId()) {
                position = i;
                break;
            }
        }
        Log.d(TAG, "getCurrentRadioDabMessagePosition: position = " + position);
        return position;
    }

    @Override
    public void stopSeek() {

    }

    private boolean isMultiAst = false;
    @Override
    public void multiAst() {
        Log.d(TAG,"multiAst:"+isMultiAst);
        isMultiAst = true;
    }

    @Override
    public void multiSeekForward() {

    }

    @Override
    public void multiSeekBackward() {

    }

    @Override
    public void multiStepForward() {

    }

    @Override
    public void multiStepBackward() {

    }

    @Override
    public void openTTSRadio(RadioMessage radioMessage) {
            if (null == tuner) {
                Log.d(TAG, "openRadio: tuner is null");
                return;
            }
            //切换到DAB的频段
            tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_SET_DAB_BAND));
            tuner.set(RadioEventValueUtils.generateEventValue(RadioEvent.EventID.EVT_DAB_OP_MUTE_UNMUTE, JsonUtils.generateSingleNodeJson(JsonNode.MUTE_UNMUTE_NODE, RadioEvent.MuteStatus.UNMUTE)));
    }

    @Override
    public void setQualityCondition(RadioParameter radioParameter) {

    }

    @Override
    public void playTTS() {

    }

    public  String getOppositeRDSName(RadioMessage radioMessage){
        for (RDSRadioName rdsRadioName : RadioList.getInstance().getRdsRadioNameList()){
            if (radioMessage.getRadioFrequency() == rdsRadioName.getFrequency()){
                Log.d(TAG,"getOppositeRDSName has one: "+radioMessage.getRadioFrequency() + " freq");
                Log.d(TAG,"getOppositeRDSName has one: "+rdsRadioName.getProgramStationName() + " NAME");
                return rdsRadioName.getProgramStationName();
            }
        }
        Log.d(TAG,"getOppositeRDSName has none");
        return null;
    }
    public  List<RadioMessage> sortWithName(List<RadioMessage> currentList){
        List<RadioMessage> tempList = new ArrayList<>();
        for (RadioMessage radioMessage : currentList) {
            if (radioMessage.getRadioType() == RadioMessage.FM_AM_TYPE) {
                String radioName = getOppositeRDSName(radioMessage);
                if (radioName == null || radioName.trim().length() < 1){
                    radioName = "FM " + (radioMessage.getRadioFrequency() / 1000.0);
                }
                radioMessage.setSortName(radioName);
            }else {
                radioMessage.setSortName(radioMessage.getDabMessage().getProgramStationName());
            }
            tempList.add(radioMessage);
        }
        // 当前目录文件夹的排序
        Collections.sort(tempList, new Comparator<RadioMessage>() {
            Comparator<String> comparator = new PinyinComparator();

            @Override
            public int compare(RadioMessage o1, RadioMessage o2) {
                if (o1.getSortName() != null && o2.getSortName() != null && o1.getSortName().startsWith("FM ")
                        && o2.getSortName().startsWith("FM ")){
                    return o1.getRadioFrequency() > o2.getRadioFrequency() ? 1 : -1;
                }
                return (o1.getSortName() == null || o2.getSortName() == null) ? 0 : comparator.compare(o1.getSortName(), o2.getSortName());
            }
        });

        return tempList;
    }
}
