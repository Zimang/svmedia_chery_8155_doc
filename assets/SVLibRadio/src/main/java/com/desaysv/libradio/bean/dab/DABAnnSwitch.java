package com.desaysv.libradio.bean.dab;

/**
 * @author uidq1846
 * @desc DAB的通知设置开关状态不
 * @time 2022-9-26 13:46
 */
public class DABAnnSwitch {
    //警报 0：未选中 1：选中
    private int alarm = -1;
    //道路交通快讯 0：未选中 1：选中
    private int roadTrafficFlash = -1;
    //传输快讯 0：未选中 1：选中
    private int transportFlash = -1;
    //警报 0：未选中 1：选中
    private int warning = -1;
    //新闻快讯 0：未选中 1：选中
    private int newsFlash = -1;
    //地区天气快讯 0：未选中 1：选中
    private int areaWeatherFlash = -1;
    //活动公告 0：未选中 1：选中
    private int eventAnnouncement = -1;
    //特别事件 0：未选中 1：选中
    private int specialEvent = -1;
    //节目信息 0：未选中 1：选中
    private int programInformation = -1;
    //体育报道 0：未选中 1：选中
    private int sportReport = -1;
    //财经报道  0：未选中 1：选中
    private int financialReport = -1;
    //Servicelink开关 0：关 1：开
    private int serviceFollow = -1;

    public int getAlarm() {
        return alarm;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public int getRoadTrafficFlash() {
        return roadTrafficFlash;
    }

    public void setRoadTrafficFlash(int roadTrafficFlash) {
        this.roadTrafficFlash = roadTrafficFlash;
    }

    public int getTransportFlash() {
        return transportFlash;
    }

    public void setTransportFlash(int transportFlash) {
        this.transportFlash = transportFlash;
    }

    public int getWarning() {
        return warning;
    }

    public void setWarning(int warning) {
        this.warning = warning;
    }

    public int getNewsFlash() {
        return newsFlash;
    }

    public void setNewsFlash(int newsFlash) {
        this.newsFlash = newsFlash;
    }

    public int getAreaWeatherFlash() {
        return areaWeatherFlash;
    }

    public void setAreaWeatherFlash(int areaWeatherFlash) {
        this.areaWeatherFlash = areaWeatherFlash;
    }

    public int getEventAnnouncement() {
        return eventAnnouncement;
    }

    public void setEventAnnouncement(int eventAnnouncement) {
        this.eventAnnouncement = eventAnnouncement;
    }

    public int getSpecialEvent() {
        return specialEvent;
    }

    public void setSpecialEvent(int specialEvent) {
        this.specialEvent = specialEvent;
    }

    public int getProgramInformation() {
        return programInformation;
    }

    public void setProgramInformation(int programInformation) {
        this.programInformation = programInformation;
    }

    public int getSportReport() {
        return sportReport;
    }

    public void setSportReport(int sportReport) {
        this.sportReport = sportReport;
    }

    public int getFinancialReport() {
        return financialReport;
    }

    public void setFinancialReport(int financialReport) {
        this.financialReport = financialReport;
    }

    public int getServiceFollow() {
        return serviceFollow;
    }

    public void setServiceFollow(int serviceFollow) {
        this.serviceFollow = serviceFollow;
    }

    @Override
    public String toString() {
        return "DABAnnSwitch{" +
                "alarm=" + alarm +
                ", roadTrafficFlash=" + roadTrafficFlash +
                ", transportFlash=" + transportFlash +
                ", warning=" + warning +
                ", newsFlash=" + newsFlash +
                ", areaWeatherFlash=" + areaWeatherFlash +
                ", eventAnnouncement=" + eventAnnouncement +
                ", specialEvent=" + specialEvent +
                ", programInformation=" + programInformation +
                ", sportReport=" + sportReport +
                ", financialReport=" + financialReport +
                ", serviceFollow=" + serviceFollow +
                '}';
    }
}
